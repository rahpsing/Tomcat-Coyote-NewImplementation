// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.util.concurrent.CountDownLatch;
import java.util.Iterator;
import org.apache.tomcat.util.ExceptionUtils;
import java.nio.channels.SocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CancelledKeyException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.juli.logging.LogFactory;
import java.nio.channels.SelectionKey;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import org.apache.juli.logging.Log;

public class NioBlockingSelector
{
    private static final Log log;
    private static int threadCounter;
    protected Selector sharedSelector;
    protected BlockPoller poller;
    
    public void open(final Selector selector) {
        this.sharedSelector = selector;
        this.poller = new BlockPoller();
        this.poller.selector = this.sharedSelector;
        this.poller.setDaemon(true);
        this.poller.setName("NioBlockingSelector.BlockPoller-" + ++NioBlockingSelector.threadCounter);
        this.poller.start();
    }
    
    public void close() {
        if (this.poller != null) {
            this.poller.disable();
            this.poller.interrupt();
            this.poller = null;
        }
    }
    
    public int write(final ByteBuffer buf, final NioChannel socket, final long writeTimeout) throws IOException {
        final SelectionKey key = socket.getIOChannel().keyFor(socket.getPoller().getSelector());
        if (key == null) {
            throw new IOException("Key no longer registered");
        }
        final KeyReference reference = new KeyReference();
        final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)key.attachment();
        int written = 0;
        boolean timedout = false;
        int keycount = 1;
        long time = System.currentTimeMillis();
        try {
            while (!timedout && buf.hasRemaining()) {
                if (keycount > 0) {
                    final int cnt = socket.write(buf);
                    if (cnt == -1) {
                        throw new EOFException();
                    }
                    written += cnt;
                    if (cnt > 0) {
                        time = System.currentTimeMillis();
                        continue;
                    }
                }
                try {
                    if (att.getWriteLatch() == null || att.getWriteLatch().getCount() == 0L) {
                        att.startWriteLatch(1);
                    }
                    this.poller.add(att, 4, reference);
                    if (writeTimeout < 0L) {
                        att.awaitWriteLatch(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                    }
                    else {
                        att.awaitWriteLatch(writeTimeout, TimeUnit.MILLISECONDS);
                    }
                }
                catch (InterruptedException ignore) {
                    Thread.interrupted();
                }
                if (att.getWriteLatch() != null && att.getWriteLatch().getCount() > 0L) {
                    keycount = 0;
                }
                else {
                    keycount = 1;
                    att.resetWriteLatch();
                }
                if (writeTimeout > 0L && keycount == 0) {
                    timedout = (System.currentTimeMillis() - time >= writeTimeout);
                }
            }
            if (timedout) {
                throw new SocketTimeoutException();
            }
        }
        finally {
            this.poller.remove(att, 4);
            if (timedout && reference.key != null) {
                this.poller.cancelKey(reference.key);
            }
            reference.key = null;
        }
        return written;
    }
    
    public int read(final ByteBuffer buf, final NioChannel socket, final long readTimeout) throws IOException {
        final SelectionKey key = socket.getIOChannel().keyFor(socket.getPoller().getSelector());
        if (key == null) {
            throw new IOException("Key no longer registered");
        }
        final KeyReference reference = new KeyReference();
        final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)key.attachment();
        int read = 0;
        boolean timedout = false;
        int keycount = 1;
        final long time = System.currentTimeMillis();
        try {
            while (!timedout) {
                if (keycount > 0) {
                    read = socket.read(buf);
                    if (read == -1) {
                        throw new EOFException();
                    }
                    if (read > 0) {
                        break;
                    }
                }
                try {
                    if (att.getReadLatch() == null || att.getReadLatch().getCount() == 0L) {
                        att.startReadLatch(1);
                    }
                    this.poller.add(att, 1, reference);
                    if (readTimeout < 0L) {
                        att.awaitReadLatch(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                    }
                    else {
                        att.awaitReadLatch(readTimeout, TimeUnit.MILLISECONDS);
                    }
                }
                catch (InterruptedException ignore) {
                    Thread.interrupted();
                }
                if (att.getReadLatch() != null && att.getReadLatch().getCount() > 0L) {
                    keycount = 0;
                }
                else {
                    keycount = 1;
                    att.resetReadLatch();
                }
                if (readTimeout >= 0L && keycount == 0) {
                    timedout = (System.currentTimeMillis() - time >= readTimeout);
                }
            }
            if (timedout) {
                throw new SocketTimeoutException();
            }
        }
        finally {
            this.poller.remove(att, 1);
            if (timedout && reference.key != null) {
                this.poller.cancelKey(reference.key);
            }
            reference.key = null;
        }
        return read;
    }
    
    static {
        log = LogFactory.getLog((Class)NioBlockingSelector.class);
        NioBlockingSelector.threadCounter = 0;
    }
    
    protected static class BlockPoller extends Thread
    {
        protected volatile boolean run;
        protected Selector selector;
        protected ConcurrentLinkedQueue<Runnable> events;
        protected AtomicInteger wakeupCounter;
        
        protected BlockPoller() {
            this.run = true;
            this.selector = null;
            this.events = new ConcurrentLinkedQueue<Runnable>();
            this.wakeupCounter = new AtomicInteger(0);
        }
        
        public void disable() {
            this.run = false;
            this.selector.wakeup();
        }
        
        public void cancelKey(final SelectionKey key) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    key.cancel();
                }
            };
            this.events.offer(r);
            this.wakeup();
        }
        
        public void wakeup() {
            if (this.wakeupCounter.addAndGet(1) == 0) {
                this.selector.wakeup();
            }
        }
        
        public void cancel(final SelectionKey sk, final NioEndpoint.KeyAttachment key, final int ops) {
            if (sk != null) {
                sk.cancel();
                sk.attach(null);
                if (0x4 == (ops & 0x4)) {
                    this.countDown(key.getWriteLatch());
                }
                if (0x1 == (ops & 0x1)) {
                    this.countDown(key.getReadLatch());
                }
            }
        }
        
        public void add(final NioEndpoint.KeyAttachment key, final int ops, final KeyReference ref) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (key == null) {
                        return;
                    }
                    final NioChannel nch = key.getChannel();
                    if (nch == null) {
                        return;
                    }
                    final SocketChannel ch = nch.getIOChannel();
                    if (ch == null) {
                        return;
                    }
                    SelectionKey sk = ch.keyFor(BlockPoller.this.selector);
                    try {
                        if (sk == null) {
                            sk = ch.register(BlockPoller.this.selector, ops, key);
                            ref.key = sk;
                        }
                        else if (!sk.isValid()) {
                            BlockPoller.this.cancel(sk, key, ops);
                        }
                        else {
                            sk.interestOps(sk.interestOps() | ops);
                        }
                    }
                    catch (CancelledKeyException cx) {
                        BlockPoller.this.cancel(sk, key, ops);
                    }
                    catch (ClosedChannelException cx2) {
                        BlockPoller.this.cancel(sk, key, ops);
                    }
                }
            };
            this.events.offer(r);
            this.wakeup();
        }
        
        public void remove(final NioEndpoint.KeyAttachment key, final int ops) {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (key == null) {
                        return;
                    }
                    final NioChannel nch = key.getChannel();
                    if (nch == null) {
                        return;
                    }
                    final SocketChannel ch = nch.getIOChannel();
                    if (ch == null) {
                        return;
                    }
                    final SelectionKey sk = ch.keyFor(BlockPoller.this.selector);
                    try {
                        if (sk == null) {
                            if (0x4 == (ops & 0x4)) {
                                BlockPoller.this.countDown(key.getWriteLatch());
                            }
                            if (0x1 == (ops & 0x1)) {
                                BlockPoller.this.countDown(key.getReadLatch());
                            }
                        }
                        else if (sk.isValid()) {
                            sk.interestOps(sk.interestOps() & ~ops);
                            if (0x4 == (ops & 0x4)) {
                                BlockPoller.this.countDown(key.getWriteLatch());
                            }
                            if (0x1 == (ops & 0x1)) {
                                BlockPoller.this.countDown(key.getReadLatch());
                            }
                            if (sk.interestOps() == 0) {
                                sk.cancel();
                                sk.attach(null);
                            }
                        }
                        else {
                            sk.cancel();
                            sk.attach(null);
                        }
                    }
                    catch (CancelledKeyException cx) {
                        if (sk != null) {
                            sk.cancel();
                            sk.attach(null);
                        }
                    }
                }
            };
            this.events.offer(r);
            this.wakeup();
        }
        
        public boolean events() {
            boolean result = false;
            Runnable r = null;
            result = (this.events.size() > 0);
            while ((r = this.events.poll()) != null) {
                r.run();
                result = true;
            }
            return result;
        }
        
        @Override
        public void run() {
            while (this.run) {
                try {
                    this.events();
                    int keyCount = 0;
                    try {
                        final int i = this.wakeupCounter.get();
                        if (i > 0) {
                            keyCount = this.selector.selectNow();
                        }
                        else {
                            this.wakeupCounter.set(-1);
                            keyCount = this.selector.select(1000L);
                        }
                        this.wakeupCounter.set(0);
                        if (!this.run) {
                            break;
                        }
                    }
                    catch (NullPointerException x) {
                        if (this.selector == null) {
                            throw x;
                        }
                        if (!NioBlockingSelector.log.isDebugEnabled()) {
                            continue;
                        }
                        NioBlockingSelector.log.debug((Object)"Possibly encountered sun bug 5076772 on windows JDK 1.5", (Throwable)x);
                        continue;
                    }
                    catch (CancelledKeyException x2) {
                        if (!NioBlockingSelector.log.isDebugEnabled()) {
                            continue;
                        }
                        NioBlockingSelector.log.debug((Object)"Possibly encountered sun bug 5076772 on windows JDK 1.5", (Throwable)x2);
                        continue;
                    }
                    catch (Throwable x3) {
                        ExceptionUtils.handleThrowable(x3);
                        NioBlockingSelector.log.error((Object)"", x3);
                        continue;
                    }
                    final Iterator<SelectionKey> iterator = (keyCount > 0) ? this.selector.selectedKeys().iterator() : null;
                    while (this.run && iterator != null && iterator.hasNext()) {
                        final SelectionKey sk = iterator.next();
                        final NioEndpoint.KeyAttachment attachment = (NioEndpoint.KeyAttachment)sk.attachment();
                        try {
                            attachment.access();
                            iterator.remove();
                            sk.interestOps(sk.interestOps() & ~sk.readyOps());
                            if (sk.isReadable()) {
                                this.countDown(attachment.getReadLatch());
                            }
                            if (!sk.isWritable()) {
                                continue;
                            }
                            this.countDown(attachment.getWriteLatch());
                        }
                        catch (CancelledKeyException ckx) {
                            sk.cancel();
                            this.countDown(attachment.getReadLatch());
                            this.countDown(attachment.getWriteLatch());
                        }
                    }
                }
                catch (Throwable t) {
                    NioBlockingSelector.log.error((Object)"", t);
                }
            }
            this.events.clear();
            try {
                this.selector.selectNow();
            }
            catch (Exception ignore) {
                if (NioBlockingSelector.log.isDebugEnabled()) {
                    NioBlockingSelector.log.debug((Object)"", (Throwable)ignore);
                }
            }
            try {
                this.selector.close();
            }
            catch (Exception ignore) {
                if (NioBlockingSelector.log.isDebugEnabled()) {
                    NioBlockingSelector.log.debug((Object)"", (Throwable)ignore);
                }
            }
        }
        
        public void countDown(final CountDownLatch latch) {
            if (latch == null) {
                return;
            }
            latch.countDown();
        }
    }
    
    public static class KeyReference
    {
        SelectionKey key;
        
        public KeyReference() {
            this.key = null;
        }
        
        public void finalize() {
            if (this.key != null && this.key.isValid()) {
                NioBlockingSelector.log.warn((Object)"Possible key leak, cancelling key in the finalizer.");
                try {
                    this.key.cancel();
                }
                catch (Exception ex) {}
            }
            this.key = null;
        }
    }
}
