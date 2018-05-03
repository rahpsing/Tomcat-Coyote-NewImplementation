// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.util.Set;
import java.nio.channels.WritableByteChannel;
import java.io.FileInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.CancelledKeyException;
import java.net.SocketTimeoutException;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;
import javax.net.ssl.SSLEngine;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;
import org.apache.tomcat.util.net.jsse.NioX509KeyManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSessionContext;
import java.security.SecureRandom;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import org.apache.tomcat.util.ExceptionUtils;
import java.net.ServerSocket;
import org.apache.tomcat.util.IntrospectionUtils;
import javax.net.ssl.SSLContext;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.nio.channels.ServerSocketChannel;
import org.apache.juli.logging.Log;

public class NioEndpoint extends AbstractEndpoint
{
    private static final Log log;
    public static final int OP_REGISTER = 256;
    public static final int OP_CALLBACK = 512;
    protected NioSelectorPool selectorPool;
    protected ServerSocketChannel serverSock;
    protected boolean useSendfile;
    protected int oomParachute;
    protected byte[] oomParachuteData;
    protected static final String oomParachuteMsg = "SEVERE:Memory usage is low, parachute is non existent, your system may start failing.";
    long lastParachuteCheck;
    protected volatile CountDownLatch stopLatch;
    protected ConcurrentLinkedQueue<SocketProcessor> processorCache;
    protected ConcurrentLinkedQueue<KeyAttachment> keyCache;
    protected ConcurrentLinkedQueue<PollerEvent> eventCache;
    protected ConcurrentLinkedQueue<NioChannel> nioChannels;
    protected int pollerThreadPriority;
    protected Handler handler;
    protected boolean useComet;
    protected int pollerThreadCount;
    protected long selectorTimeout;
    protected Poller[] pollers;
    protected AtomicInteger pollerRotater;
    protected SSLContext sslContext;
    private String[] enabledCiphers;
    private String[] enabledProtocols;
    
    public NioEndpoint() {
        this.selectorPool = new NioSelectorPool();
        this.serverSock = null;
        this.useSendfile = true;
        this.oomParachute = 1048576;
        this.oomParachuteData = null;
        this.lastParachuteCheck = System.currentTimeMillis();
        this.stopLatch = null;
        this.processorCache = new ConcurrentLinkedQueue<SocketProcessor>() {
            private static final long serialVersionUID = 1L;
            protected AtomicInteger size = new AtomicInteger(0);
            
            @Override
            public boolean offer(final SocketProcessor sc) {
                sc.reset(null, null);
                final boolean offer = NioEndpoint.this.socketProperties.getProcessorCache() == -1 || this.size.get() < NioEndpoint.this.socketProperties.getProcessorCache();
                if (NioEndpoint.this.running && !NioEndpoint.this.paused && offer) {
                    final boolean result = super.offer(sc);
                    if (result) {
                        this.size.incrementAndGet();
                    }
                    return result;
                }
                return false;
            }
            
            @Override
            public SocketProcessor poll() {
                final SocketProcessor result = super.poll();
                if (result != null) {
                    this.size.decrementAndGet();
                }
                return result;
            }
            
            @Override
            public void clear() {
                super.clear();
                this.size.set(0);
            }
        };
        this.keyCache = new ConcurrentLinkedQueue<KeyAttachment>() {
            private static final long serialVersionUID = 1L;
            protected AtomicInteger size = new AtomicInteger(0);
            
            @Override
            public boolean offer(final KeyAttachment ka) {
                ka.reset();
                final boolean offer = NioEndpoint.this.socketProperties.getKeyCache() == -1 || this.size.get() < NioEndpoint.this.socketProperties.getKeyCache();
                if (NioEndpoint.this.running && !NioEndpoint.this.paused && offer) {
                    final boolean result = super.offer(ka);
                    if (result) {
                        this.size.incrementAndGet();
                    }
                    return result;
                }
                return false;
            }
            
            @Override
            public KeyAttachment poll() {
                final KeyAttachment result = super.poll();
                if (result != null) {
                    this.size.decrementAndGet();
                }
                return result;
            }
            
            @Override
            public void clear() {
                super.clear();
                this.size.set(0);
            }
        };
        this.eventCache = new ConcurrentLinkedQueue<PollerEvent>() {
            private static final long serialVersionUID = 1L;
            protected AtomicInteger size = new AtomicInteger(0);
            
            @Override
            public boolean offer(final PollerEvent pe) {
                pe.reset();
                final boolean offer = NioEndpoint.this.socketProperties.getEventCache() == -1 || this.size.get() < NioEndpoint.this.socketProperties.getEventCache();
                if (NioEndpoint.this.running && !NioEndpoint.this.paused && offer) {
                    final boolean result = super.offer(pe);
                    if (result) {
                        this.size.incrementAndGet();
                    }
                    return result;
                }
                return false;
            }
            
            @Override
            public PollerEvent poll() {
                final PollerEvent result = super.poll();
                if (result != null) {
                    this.size.decrementAndGet();
                }
                return result;
            }
            
            @Override
            public void clear() {
                super.clear();
                this.size.set(0);
            }
        };
        this.nioChannels = new ConcurrentLinkedQueue<NioChannel>() {
            private static final long serialVersionUID = 1L;
            protected AtomicInteger size = new AtomicInteger(0);
            protected AtomicInteger bytes = new AtomicInteger(0);
            
            @Override
            public boolean offer(final NioChannel socket) {
                boolean offer = NioEndpoint.this.socketProperties.getBufferPool() == -1 || this.size.get() < NioEndpoint.this.socketProperties.getBufferPool();
                boolean b = false;
                Label_0095: {
                    Label_0094: {
                        if (offer) {
                            if (NioEndpoint.this.socketProperties.getBufferPoolSize() != -1) {
                                if (this.bytes.get() + socket.getBufferSize() >= NioEndpoint.this.socketProperties.getBufferPoolSize()) {
                                    break Label_0094;
                                }
                            }
                            b = true;
                            break Label_0095;
                        }
                    }
                    b = false;
                }
                offer = b;
                if (NioEndpoint.this.running && !NioEndpoint.this.paused && offer) {
                    final boolean result = super.offer(socket);
                    if (result) {
                        this.size.incrementAndGet();
                        this.bytes.addAndGet(socket.getBufferSize());
                    }
                    return result;
                }
                return false;
            }
            
            @Override
            public NioChannel poll() {
                final NioChannel result = super.poll();
                if (result != null) {
                    this.size.decrementAndGet();
                    this.bytes.addAndGet(-result.getBufferSize());
                }
                return result;
            }
            
            @Override
            public void clear() {
                super.clear();
                this.size.set(0);
                this.bytes.set(0);
            }
        };
        this.pollerThreadPriority = 5;
        this.handler = null;
        this.useComet = true;
        this.pollerThreadCount = Math.min(2, Runtime.getRuntime().availableProcessors());
        this.selectorTimeout = 1000L;
        this.pollers = null;
        this.pollerRotater = new AtomicInteger(0);
        this.sslContext = null;
    }
    
    @Override
    public boolean setProperty(final String name, final String value) {
        final String selectorPoolName = "selectorPool.";
        try {
            if (name.startsWith("selectorPool.")) {
                return IntrospectionUtils.setProperty(this.selectorPool, name.substring("selectorPool.".length()), value);
            }
            return super.setProperty(name, value);
        }
        catch (Exception x) {
            NioEndpoint.log.error((Object)("Unable to set attribute \"" + name + "\" to \"" + value + "\""), (Throwable)x);
            return false;
        }
    }
    
    public void setPollerThreadPriority(final int pollerThreadPriority) {
        this.pollerThreadPriority = pollerThreadPriority;
    }
    
    public int getPollerThreadPriority() {
        return this.pollerThreadPriority;
    }
    
    public void setHandler(final Handler handler) {
        this.handler = handler;
    }
    
    public Handler getHandler() {
        return this.handler;
    }
    
    public void setUseComet(final boolean useComet) {
        this.useComet = useComet;
    }
    
    @Override
    public boolean getUseComet() {
        return this.useComet;
    }
    
    @Override
    public boolean getUseCometTimeout() {
        return this.getUseComet();
    }
    
    @Override
    public boolean getUsePolling() {
        return true;
    }
    
    public void setPollerThreadCount(final int pollerThreadCount) {
        this.pollerThreadCount = pollerThreadCount;
    }
    
    public int getPollerThreadCount() {
        return this.pollerThreadCount;
    }
    
    public void setSelectorTimeout(final long timeout) {
        this.selectorTimeout = timeout;
    }
    
    public long getSelectorTimeout() {
        return this.selectorTimeout;
    }
    
    public Poller getPoller0() {
        final int idx = Math.abs(this.pollerRotater.incrementAndGet()) % this.pollers.length;
        return this.pollers[idx];
    }
    
    public void setSelectorPool(final NioSelectorPool selectorPool) {
        this.selectorPool = selectorPool;
    }
    
    public void setSocketProperties(final SocketProperties socketProperties) {
        this.socketProperties = socketProperties;
    }
    
    public void setUseSendfile(final boolean useSendfile) {
        this.useSendfile = useSendfile;
    }
    
    public boolean getDeferAccept() {
        return false;
    }
    
    public void setOomParachute(final int oomParachute) {
        this.oomParachute = oomParachute;
    }
    
    public void setOomParachuteData(final byte[] oomParachuteData) {
        this.oomParachuteData = oomParachuteData;
    }
    
    public SSLContext getSSLContext() {
        return this.sslContext;
    }
    
    public void setSSLContext(final SSLContext c) {
        this.sslContext = c;
    }
    
    @Override
    public int getLocalPort() {
        final ServerSocketChannel ssc = this.serverSock;
        if (ssc == null) {
            return -1;
        }
        final ServerSocket s = ssc.socket();
        if (s == null) {
            return -1;
        }
        return s.getLocalPort();
    }
    
    protected void checkParachute() {
        final boolean para = this.reclaimParachute(false);
        if (!para && System.currentTimeMillis() - this.lastParachuteCheck > 10000L) {
            try {
                NioEndpoint.log.fatal((Object)"SEVERE:Memory usage is low, parachute is non existent, your system may start failing.");
            }
            catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                System.err.println("SEVERE:Memory usage is low, parachute is non existent, your system may start failing.");
            }
            this.lastParachuteCheck = System.currentTimeMillis();
        }
    }
    
    protected boolean reclaimParachute(final boolean force) {
        if (this.oomParachuteData != null) {
            return true;
        }
        if (this.oomParachute > 0 && (force || Runtime.getRuntime().freeMemory() > this.oomParachute * 2)) {
            this.oomParachuteData = new byte[this.oomParachute];
        }
        return this.oomParachuteData != null;
    }
    
    protected void releaseCaches() {
        this.keyCache.clear();
        this.nioChannels.clear();
        this.processorCache.clear();
        if (this.handler != null) {
            this.handler.recycle();
        }
    }
    
    public int getKeepAliveCount() {
        if (this.pollers == null) {
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < this.pollers.length; ++i) {
            sum += this.pollers[i].getKeyCount();
        }
        return sum;
    }
    
    @Override
    public void bind() throws Exception {
        this.serverSock = ServerSocketChannel.open();
        this.socketProperties.setProperties(this.serverSock.socket());
        final InetSocketAddress addr = (this.getAddress() != null) ? new InetSocketAddress(this.getAddress(), this.getPort()) : new InetSocketAddress(this.getPort());
        this.serverSock.socket().bind(addr, this.getBacklog());
        this.serverSock.configureBlocking(true);
        this.serverSock.socket().setSoTimeout(this.getSocketProperties().getSoTimeout());
        if (this.acceptorThreadCount == 0) {
            this.acceptorThreadCount = 1;
        }
        if (this.pollerThreadCount <= 0) {
            this.pollerThreadCount = 1;
        }
        this.stopLatch = new CountDownLatch(this.pollerThreadCount);
        if (this.isSSLEnabled()) {
            final SSLUtil sslUtil = this.handler.getSslImplementation().getSSLUtil(this);
            (this.sslContext = sslUtil.createSSLContext()).init(this.wrap(sslUtil.getKeyManagers()), sslUtil.getTrustManagers(), null);
            final SSLSessionContext sessionContext = this.sslContext.getServerSessionContext();
            if (sessionContext != null) {
                sslUtil.configureSessionContext(sessionContext);
            }
            this.enabledCiphers = sslUtil.getEnableableCiphers(this.sslContext);
            this.enabledProtocols = sslUtil.getEnableableProtocols(this.sslContext);
        }
        if (this.oomParachute > 0) {
            this.reclaimParachute(true);
        }
        this.selectorPool.open();
    }
    
    public KeyManager[] wrap(final KeyManager[] managers) {
        if (managers == null) {
            return null;
        }
        final KeyManager[] result = new KeyManager[managers.length];
        for (int i = 0; i < result.length; ++i) {
            if (managers[i] instanceof X509KeyManager && this.getKeyAlias() != null) {
                result[i] = new NioX509KeyManager((X509KeyManager)managers[i], this.getKeyAlias());
            }
            else {
                result[i] = managers[i];
            }
        }
        return result;
    }
    
    @Override
    public void startInternal() throws Exception {
        if (!this.running) {
            this.running = true;
            this.paused = false;
            if (this.getExecutor() == null) {
                this.createExecutor();
            }
            this.initializeConnectionLatch();
            this.pollers = new Poller[this.getPollerThreadCount()];
            for (int i = 0; i < this.pollers.length; ++i) {
                this.pollers[i] = new Poller();
                final Thread pollerThread = new Thread(this.pollers[i], this.getName() + "-ClientPoller-" + i);
                pollerThread.setPriority(this.threadPriority);
                pollerThread.setDaemon(true);
                pollerThread.start();
            }
            this.startAcceptorThreads();
        }
    }
    
    @Override
    public void stopInternal() {
        this.releaseConnectionLatch();
        if (!this.paused) {
            this.pause();
        }
        if (this.running) {
            this.running = false;
            this.unlockAccept();
            for (int i = 0; this.pollers != null && i < this.pollers.length; ++i) {
                if (this.pollers[i] != null) {
                    this.pollers[i].destroy();
                    this.pollers[i] = null;
                }
            }
            try {
                this.stopLatch.await(this.selectorTimeout + 100L, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ex) {}
        }
        this.eventCache.clear();
        this.keyCache.clear();
        this.nioChannels.clear();
        this.processorCache.clear();
        this.shutdownExecutor();
    }
    
    @Override
    public void unbind() throws Exception {
        if (NioEndpoint.log.isDebugEnabled()) {
            NioEndpoint.log.debug((Object)("Destroy initiated for " + new InetSocketAddress(this.getAddress(), this.getPort())));
        }
        if (this.running) {
            this.stop();
        }
        this.serverSock.socket().close();
        this.serverSock.close();
        this.serverSock = null;
        this.sslContext = null;
        this.releaseCaches();
        this.selectorPool.close();
        if (NioEndpoint.log.isDebugEnabled()) {
            NioEndpoint.log.debug((Object)("Destroy completed for " + new InetSocketAddress(this.getAddress(), this.getPort())));
        }
    }
    
    public int getWriteBufSize() {
        return this.socketProperties.getTxBufSize();
    }
    
    public int getReadBufSize() {
        return this.socketProperties.getRxBufSize();
    }
    
    public NioSelectorPool getSelectorPool() {
        return this.selectorPool;
    }
    
    @Override
    public boolean getUseSendfile() {
        return this.useSendfile;
    }
    
    public int getOomParachute() {
        return this.oomParachute;
    }
    
    public byte[] getOomParachuteData() {
        return this.oomParachuteData;
    }
    
    @Override
    protected AbstractEndpoint.Acceptor createAcceptor() {
        return new Acceptor();
    }
    
    protected boolean setSocketOptions(final SocketChannel socket) {
        try {
            socket.configureBlocking(false);
            final Socket sock = socket.socket();
            this.socketProperties.setProperties(sock);
            NioChannel channel = this.nioChannels.poll();
            if (channel == null) {
                if (this.sslContext != null) {
                    final SSLEngine engine = this.createSSLEngine();
                    final int appbufsize = engine.getSession().getApplicationBufferSize();
                    final NioBufferHandler bufhandler = new NioBufferHandler(Math.max(appbufsize, this.socketProperties.getAppReadBufSize()), Math.max(appbufsize, this.socketProperties.getAppWriteBufSize()), this.socketProperties.getDirectBuffer());
                    channel = new SecureNioChannel(socket, engine, bufhandler, this.selectorPool);
                }
                else {
                    final NioBufferHandler bufhandler2 = new NioBufferHandler(this.socketProperties.getAppReadBufSize(), this.socketProperties.getAppWriteBufSize(), this.socketProperties.getDirectBuffer());
                    channel = new NioChannel(socket, bufhandler2);
                }
            }
            else {
                channel.setIOChannel(socket);
                if (channel instanceof SecureNioChannel) {
                    final SSLEngine engine = this.createSSLEngine();
                    ((SecureNioChannel)channel).reset(engine);
                }
                else {
                    channel.reset();
                }
            }
            this.getPoller0().register(channel);
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            try {
                NioEndpoint.log.error((Object)"", t);
            }
            catch (Throwable tt) {
                ExceptionUtils.handleThrowable(t);
            }
            return false;
        }
        return true;
    }
    
    protected SSLEngine createSSLEngine() {
        final SSLEngine engine = this.sslContext.createSSLEngine();
        if ("false".equals(this.getClientAuth())) {
            engine.setNeedClientAuth(false);
            engine.setWantClientAuth(false);
        }
        else if ("true".equals(this.getClientAuth()) || "yes".equals(this.getClientAuth())) {
            engine.setNeedClientAuth(true);
        }
        else if ("want".equals(this.getClientAuth())) {
            engine.setWantClientAuth(true);
        }
        engine.setUseClientMode(false);
        engine.setEnabledCipherSuites(this.enabledCiphers);
        engine.setEnabledProtocols(this.enabledProtocols);
        return engine;
    }
    
    protected boolean isWorkerAvailable() {
        return true;
    }
    
    public boolean processSocket(final NioChannel socket, final SocketStatus status, final boolean dispatch) {
        try {
            final KeyAttachment attachment = (KeyAttachment)socket.getAttachment(false);
            if (attachment == null) {
                return false;
            }
            attachment.setCometNotify(false);
            SocketProcessor sc = this.processorCache.poll();
            if (sc == null) {
                sc = new SocketProcessor(socket, status);
            }
            else {
                sc.reset(socket, status);
            }
            if (dispatch && this.getExecutor() != null) {
                this.getExecutor().execute(sc);
            }
            else {
                sc.run();
            }
        }
        catch (RejectedExecutionException rx) {
            NioEndpoint.log.warn((Object)("Socket processing request was rejected for:" + socket), (Throwable)rx);
            return false;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            NioEndpoint.log.error((Object)NioEndpoint.sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }
    
    @Override
    protected Log getLog() {
        return NioEndpoint.log;
    }
    
    private void closeSocket(final SocketChannel socket) {
        try {
            socket.socket().close();
        }
        catch (IOException ioe) {
            if (NioEndpoint.log.isDebugEnabled()) {
                NioEndpoint.log.debug((Object)"", (Throwable)ioe);
            }
        }
        try {
            socket.close();
        }
        catch (IOException ioe) {
            if (NioEndpoint.log.isDebugEnabled()) {
                NioEndpoint.log.debug((Object)"", (Throwable)ioe);
            }
        }
    }
    
    static {
        log = LogFactory.getLog((Class)NioEndpoint.class);
    }
    
    protected class Acceptor extends AbstractEndpoint.Acceptor
    {
        @Override
        public void run() {
            int errorDelay = 0;
            while (NioEndpoint.this.running) {
                while (NioEndpoint.this.paused && NioEndpoint.this.running) {
                    this.state = AcceptorState.PAUSED;
                    try {
                        Thread.sleep(50L);
                    }
                    catch (InterruptedException e) {}
                }
                if (!NioEndpoint.this.running) {
                    break;
                }
                this.state = AcceptorState.RUNNING;
                try {
                    NioEndpoint.this.countUpOrAwaitConnection();
                    SocketChannel socket = null;
                    try {
                        socket = NioEndpoint.this.serverSock.accept();
                    }
                    catch (IOException ioe) {
                        NioEndpoint.this.countDownConnection();
                        errorDelay = NioEndpoint.this.handleExceptionWithDelay(errorDelay);
                        throw ioe;
                    }
                    errorDelay = 0;
                    if (NioEndpoint.this.running && !NioEndpoint.this.paused) {
                        if (NioEndpoint.this.setSocketOptions(socket)) {
                            continue;
                        }
                        NioEndpoint.this.countDownConnection();
                        NioEndpoint.this.closeSocket(socket);
                    }
                    else {
                        NioEndpoint.this.countDownConnection();
                        NioEndpoint.this.closeSocket(socket);
                    }
                }
                catch (SocketTimeoutException sx) {}
                catch (IOException x) {
                    if (!NioEndpoint.this.running) {
                        continue;
                    }
                    NioEndpoint.log.error((Object)AbstractEndpoint.sm.getString("endpoint.accept.fail"), (Throwable)x);
                }
                catch (OutOfMemoryError oom) {
                    try {
                        NioEndpoint.this.oomParachuteData = null;
                        NioEndpoint.this.releaseCaches();
                        NioEndpoint.log.error((Object)"", (Throwable)oom);
                    }
                    catch (Throwable oomt) {
                        try {
                            try {
                                System.err.println("SEVERE:Memory usage is low, parachute is non existent, your system may start failing.");
                                oomt.printStackTrace();
                            }
                            catch (Throwable letsHopeWeDontGetHere) {
                                ExceptionUtils.handleThrowable(letsHopeWeDontGetHere);
                            }
                        }
                        catch (Throwable letsHopeWeDontGetHere) {
                            ExceptionUtils.handleThrowable(letsHopeWeDontGetHere);
                        }
                    }
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    NioEndpoint.log.error((Object)AbstractEndpoint.sm.getString("endpoint.accept.fail"), t);
                }
            }
            this.state = AcceptorState.ENDED;
        }
    }
    
    public static class PollerEvent implements Runnable
    {
        protected NioChannel socket;
        protected int interestOps;
        protected KeyAttachment key;
        
        public PollerEvent(final NioChannel ch, final KeyAttachment k, final int intOps) {
            this.reset(ch, k, intOps);
        }
        
        public void reset(final NioChannel ch, final KeyAttachment k, final int intOps) {
            this.socket = ch;
            this.interestOps = intOps;
            this.key = k;
        }
        
        public void reset() {
            this.reset(null, null, 0);
        }
        
        @Override
        public void run() {
            if (this.interestOps == 256) {
                try {
                    this.socket.getIOChannel().register(this.socket.getPoller().getSelector(), 1, this.key);
                }
                catch (Exception x) {
                    NioEndpoint.log.error((Object)"", (Throwable)x);
                }
            }
            else {
                final SelectionKey key = this.socket.getIOChannel().keyFor(this.socket.getPoller().getSelector());
                try {
                    boolean cancel = false;
                    if (key != null) {
                        final KeyAttachment att = (KeyAttachment)key.attachment();
                        if (att != null) {
                            if (att.isComet() && (this.interestOps & 0x200) == 0x200) {
                                att.setCometNotify(true);
                            }
                            else {
                                att.setCometNotify(false);
                            }
                            this.interestOps &= 0xFFFFFDFF;
                            att.access();
                            final int ops = key.interestOps() | this.interestOps;
                            att.interestOps(ops);
                            key.interestOps(ops);
                        }
                        else {
                            cancel = true;
                        }
                    }
                    else {
                        cancel = true;
                    }
                    if (cancel) {
                        this.socket.getPoller().cancelledKey(key, SocketStatus.ERROR, false);
                    }
                }
                catch (CancelledKeyException ckx) {
                    try {
                        this.socket.getPoller().cancelledKey(key, SocketStatus.DISCONNECT, true);
                    }
                    catch (Exception ex) {}
                }
            }
        }
        
        @Override
        public String toString() {
            return super.toString() + "[intOps=" + this.interestOps + "]";
        }
    }
    
    public class Poller implements Runnable
    {
        protected Selector selector;
        protected ConcurrentLinkedQueue<Runnable> events;
        protected volatile boolean close;
        protected long nextExpiration;
        protected AtomicLong wakeupCounter;
        protected volatile int keyCount;
        
        public Poller() throws IOException {
            this.events = new ConcurrentLinkedQueue<Runnable>();
            this.close = false;
            this.nextExpiration = 0L;
            this.wakeupCounter = new AtomicLong(0L);
            this.keyCount = 0;
            synchronized (Selector.class) {
                this.selector = Selector.open();
            }
        }
        
        public int getKeyCount() {
            return this.keyCount;
        }
        
        public Selector getSelector() {
            return this.selector;
        }
        
        protected void destroy() {
            this.close = true;
            this.selector.wakeup();
        }
        
        @Deprecated
        public void addEvent(final Runnable event) {
            this.events.offer(event);
            if (this.wakeupCounter.incrementAndGet() == 0L) {
                this.selector.wakeup();
            }
        }
        
        @Deprecated
        public void cometInterest(final NioChannel socket) {
            final KeyAttachment att = (KeyAttachment)socket.getAttachment(false);
            this.add(socket, att.getCometOps());
            if ((att.getCometOps() & 0x200) == 0x200) {
                this.nextExpiration = 0L;
                this.selector.wakeup();
            }
        }
        
        public void add(final NioChannel socket) {
            this.add(socket, 1);
        }
        
        public void add(final NioChannel socket, final int interestOps) {
            PollerEvent r = NioEndpoint.this.eventCache.poll();
            if (r == null) {
                r = new PollerEvent(socket, null, interestOps);
            }
            else {
                r.reset(socket, null, interestOps);
            }
            this.addEvent(r);
            if (this.close) {
                NioEndpoint.this.processSocket(socket, SocketStatus.STOP, false);
            }
        }
        
        public boolean events() {
            boolean result = false;
            Runnable r = null;
            while ((r = this.events.poll()) != null) {
                result = true;
                try {
                    r.run();
                    if (!(r instanceof PollerEvent)) {
                        continue;
                    }
                    ((PollerEvent)r).reset();
                    NioEndpoint.this.eventCache.offer((PollerEvent)r);
                }
                catch (Throwable x) {
                    NioEndpoint.log.error((Object)"", x);
                }
            }
            return result;
        }
        
        public void register(final NioChannel socket) {
            socket.setPoller(this);
            final KeyAttachment key = NioEndpoint.this.keyCache.poll();
            final KeyAttachment ka = (key != null) ? key : new KeyAttachment(socket);
            ka.reset(this, socket, NioEndpoint.this.getSocketProperties().getSoTimeout());
            ka.setKeepAliveLeft(NioEndpoint.this.getMaxKeepAliveRequests());
            ka.setSecure(NioEndpoint.this.isSSLEnabled());
            PollerEvent r = NioEndpoint.this.eventCache.poll();
            ka.interestOps(1);
            if (r == null) {
                r = new PollerEvent(socket, ka, 256);
            }
            else {
                r.reset(socket, ka, 256);
            }
            this.addEvent(r);
        }
        
        public void cancelledKey(final SelectionKey key, final SocketStatus status, final boolean dispatch) {
            try {
                if (key == null) {
                    return;
                }
                final KeyAttachment ka = (KeyAttachment)key.attachment();
                if (ka != null && ka.isComet() && status != null) {
                    ka.setComet(false);
                    if (status == SocketStatus.TIMEOUT) {
                        if (NioEndpoint.this.processSocket(ka.getChannel(), status, true)) {
                            return;
                        }
                    }
                    else {
                        NioEndpoint.this.processSocket(ka.getChannel(), status, false);
                    }
                }
                key.attach(null);
                if (ka != null) {
                    NioEndpoint.this.handler.release(ka);
                }
                else {
                    NioEndpoint.this.handler.release((SocketChannel)key.channel());
                }
                if (key.isValid()) {
                    key.cancel();
                }
                if (key.channel().isOpen()) {
                    try {
                        key.channel().close();
                    }
                    catch (Exception e) {
                        if (NioEndpoint.log.isDebugEnabled()) {
                            NioEndpoint.log.debug((Object)AbstractEndpoint.sm.getString("endpoint.debug.channelCloseFail"), (Throwable)e);
                        }
                    }
                }
                try {
                    if (ka != null) {
                        ka.getSocket().close(true);
                    }
                }
                catch (Exception e) {
                    if (NioEndpoint.log.isDebugEnabled()) {
                        NioEndpoint.log.debug((Object)AbstractEndpoint.sm.getString("endpoint.debug.socketCloseFail"), (Throwable)e);
                    }
                }
                try {
                    if (ka != null && ka.getSendfileData() != null && ka.getSendfileData().fchannel != null && ka.getSendfileData().fchannel.isOpen()) {
                        ka.getSendfileData().fchannel.close();
                    }
                }
                catch (Exception ex) {}
                if (ka != null) {
                    ka.reset();
                    NioEndpoint.this.countDownConnection();
                }
            }
            catch (Throwable e2) {
                ExceptionUtils.handleThrowable(e2);
                if (NioEndpoint.log.isDebugEnabled()) {
                    NioEndpoint.log.error((Object)"", e2);
                }
            }
        }
        
        @Override
        public void run() {
        Label_0502:
            while (true) {
                try {
                    while (true) {
                        if (NioEndpoint.this.paused && !this.close) {
                            try {
                                Thread.sleep(100L);
                            }
                            catch (InterruptedException e) {}
                        }
                        else {
                            boolean hasEvents = false;
                            if (this.close) {
                                break;
                            }
                            hasEvents = this.events();
                            try {
                                if (!this.close) {
                                    if (this.wakeupCounter.getAndSet(-1L) > 0L) {
                                        this.keyCount = this.selector.selectNow();
                                    }
                                    else {
                                        this.keyCount = this.selector.select(NioEndpoint.this.selectorTimeout);
                                    }
                                    this.wakeupCounter.set(0L);
                                }
                                if (this.close) {
                                    this.events();
                                    this.timeout(0, false);
                                    try {
                                        this.selector.close();
                                    }
                                    catch (IOException ioe) {
                                        NioEndpoint.log.error((Object)AbstractEndpoint.sm.getString("endpoint.nio.selectorCloseFail"), (Throwable)ioe);
                                    }
                                    break Label_0502;
                                }
                            }
                            catch (NullPointerException x) {
                                if (NioEndpoint.log.isDebugEnabled()) {
                                    NioEndpoint.log.debug((Object)"Possibly encountered sun bug 5076772 on windows JDK 1.5", (Throwable)x);
                                }
                                if (this.wakeupCounter == null || this.selector == null) {
                                    throw x;
                                }
                                continue;
                            }
                            catch (CancelledKeyException x2) {
                                if (NioEndpoint.log.isDebugEnabled()) {
                                    NioEndpoint.log.debug((Object)"Possibly encountered sun bug 5076772 on windows JDK 1.5", (Throwable)x2);
                                }
                                if (this.wakeupCounter == null || this.selector == null) {
                                    throw x2;
                                }
                                continue;
                            }
                            catch (Throwable x3) {
                                ExceptionUtils.handleThrowable(x3);
                                NioEndpoint.log.error((Object)"", x3);
                                continue;
                            }
                            if (this.keyCount == 0) {
                                hasEvents |= this.events();
                            }
                            final Iterator<SelectionKey> iterator = (this.keyCount > 0) ? this.selector.selectedKeys().iterator() : null;
                            while (iterator != null && iterator.hasNext()) {
                                final SelectionKey sk = iterator.next();
                                final KeyAttachment attachment = (KeyAttachment)sk.attachment();
                                if (attachment == null) {
                                    iterator.remove();
                                }
                                else {
                                    attachment.access();
                                    iterator.remove();
                                    this.processKey(sk, attachment);
                                }
                            }
                            this.timeout(this.keyCount, hasEvents);
                            if (NioEndpoint.this.oomParachute <= 0 || NioEndpoint.this.oomParachuteData != null) {
                                continue;
                            }
                            NioEndpoint.this.checkParachute();
                        }
                    }
                    this.events();
                    this.timeout(0, false);
                    try {
                        this.selector.close();
                    }
                    catch (IOException ioe) {
                        NioEndpoint.log.error((Object)AbstractEndpoint.sm.getString("endpoint.nio.selectorCloseFail"), (Throwable)ioe);
                    }
                }
                catch (OutOfMemoryError oom) {
                    try {
                        NioEndpoint.this.oomParachuteData = null;
                        NioEndpoint.this.releaseCaches();
                        NioEndpoint.log.error((Object)"", (Throwable)oom);
                    }
                    catch (Throwable oomt) {
                        try {
                            System.err.println("SEVERE:Memory usage is low, parachute is non existent, your system may start failing.");
                            oomt.printStackTrace();
                        }
                        catch (Throwable letsHopeWeDontGetHere) {
                            ExceptionUtils.handleThrowable(letsHopeWeDontGetHere);
                        }
                    }
                    continue;
                }
                break;
            }
            synchronized (this) {
                this.notifyAll();
            }
            NioEndpoint.this.stopLatch.countDown();
        }
        
        protected boolean processKey(final SelectionKey sk, final KeyAttachment attachment) {
            boolean result = true;
            try {
                if (this.close) {
                    this.cancelledKey(sk, SocketStatus.STOP, attachment.comet);
                }
                else if (sk.isValid() && attachment != null) {
                    attachment.access();
                    sk.attach(attachment);
                    final NioChannel channel = attachment.getChannel();
                    if (sk.isReadable() || sk.isWritable()) {
                        if (attachment.getSendfileData() != null) {
                            this.processSendfile(sk, attachment, false);
                        }
                        else if (NioEndpoint.this.isWorkerAvailable()) {
                            this.unreg(sk, attachment, sk.readyOps());
                            boolean closeSocket = false;
                            if (sk.isReadable() && !NioEndpoint.this.processSocket(channel, SocketStatus.OPEN_READ, true)) {
                                closeSocket = true;
                            }
                            if (!closeSocket && sk.isWritable() && !NioEndpoint.this.processSocket(channel, SocketStatus.OPEN_WRITE, true)) {
                                closeSocket = true;
                            }
                            if (closeSocket) {
                                this.cancelledKey(sk, SocketStatus.DISCONNECT, false);
                            }
                        }
                        else {
                            result = false;
                        }
                    }
                }
                else {
                    this.cancelledKey(sk, SocketStatus.ERROR, false);
                }
            }
            catch (CancelledKeyException ckx) {
                this.cancelledKey(sk, SocketStatus.ERROR, false);
            }
            catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                NioEndpoint.log.error((Object)"", t);
            }
            return result;
        }
        
        @Deprecated
        public boolean processSendfile(final SelectionKey sk, final KeyAttachment attachment, final boolean reg, final boolean event) {
            return this.processSendfile(sk, attachment, event);
        }
        
        public boolean processSendfile(final SelectionKey sk, final KeyAttachment attachment, final boolean event) {
            NioChannel sc = null;
            try {
                this.unreg(sk, attachment, sk.readyOps());
                final SendfileData sd = attachment.getSendfileData();
                if (NioEndpoint.log.isTraceEnabled()) {
                    NioEndpoint.log.trace((Object)("Processing send file for: " + sd.fileName));
                }
                if (sd.fchannel == null) {
                    final File f = new File(sd.fileName);
                    if (!f.exists()) {
                        this.cancelledKey(sk, SocketStatus.ERROR, false);
                        return false;
                    }
                    final FileInputStream fis = new FileInputStream(f);
                    sd.fchannel = fis.getChannel();
                }
                sc = attachment.getChannel();
                sc.setSendFile(true);
                final WritableByteChannel wc = (sc instanceof SecureNioChannel) ? sc : sc.getIOChannel();
                if (sc.getOutboundRemaining() > 0) {
                    if (sc.flushOutbound()) {
                        attachment.access();
                    }
                }
                else {
                    final long written = sd.fchannel.transferTo(sd.pos, sd.length, wc);
                    if (written > 0L) {
                        final SendfileData sendfileData = sd;
                        sendfileData.pos += written;
                        final SendfileData sendfileData2 = sd;
                        sendfileData2.length -= written;
                        attachment.access();
                    }
                    else if (sd.fchannel.size() <= sd.pos) {
                        throw new IOException("Sendfile configured to send more data than was available");
                    }
                }
                if (sd.length <= 0L && sc.getOutboundRemaining() <= 0) {
                    if (NioEndpoint.log.isDebugEnabled()) {
                        NioEndpoint.log.debug((Object)("Send file complete for: " + sd.fileName));
                    }
                    attachment.setSendfileData(null);
                    try {
                        sd.fchannel.close();
                    }
                    catch (Exception ex) {}
                    if (!sd.keepAlive) {
                        if (NioEndpoint.log.isDebugEnabled()) {
                            NioEndpoint.log.debug((Object)"Send file connection is being closed");
                        }
                        this.cancelledKey(sk, SocketStatus.STOP, false);
                        return false;
                    }
                    if (NioEndpoint.log.isDebugEnabled()) {
                        NioEndpoint.log.debug((Object)"Connection is keep alive, registering back for OP_READ");
                    }
                    if (event) {
                        this.add(attachment.getChannel(), 1);
                    }
                    else {
                        this.reg(sk, attachment, 1);
                    }
                }
                else {
                    if (NioEndpoint.log.isDebugEnabled()) {
                        NioEndpoint.log.debug((Object)("OP_WRITE for sendfile: " + sd.fileName));
                    }
                    if (event) {
                        this.add(attachment.getChannel(), 4);
                    }
                    else {
                        this.reg(sk, attachment, 4);
                    }
                }
            }
            catch (IOException x) {
                if (NioEndpoint.log.isDebugEnabled()) {
                    NioEndpoint.log.debug((Object)"Unable to complete sendfile request:", (Throwable)x);
                }
                this.cancelledKey(sk, SocketStatus.ERROR, false);
                return false;
            }
            catch (Throwable t) {
                NioEndpoint.log.error((Object)"", t);
                this.cancelledKey(sk, SocketStatus.ERROR, false);
                return false;
            }
            finally {
                if (sc != null) {
                    sc.setSendFile(false);
                }
            }
            return true;
        }
        
        protected void unreg(final SelectionKey sk, final KeyAttachment attachment, final int readyOps) {
            this.reg(sk, attachment, sk.interestOps() & ~readyOps);
        }
        
        protected void reg(final SelectionKey sk, final KeyAttachment attachment, final int intops) {
            sk.interestOps(intops);
            attachment.interestOps(intops);
            attachment.setCometOps(intops);
        }
        
        protected void timeout(final int keyCount, final boolean hasEvents) {
            final long now = System.currentTimeMillis();
            if ((keyCount > 0 || hasEvents) && now < this.nextExpiration && !this.close) {
                return;
            }
            final Set<SelectionKey> keys = this.selector.keys();
            int keycount = 0;
            for (final SelectionKey key : keys) {
                ++keycount;
                try {
                    final KeyAttachment ka = (KeyAttachment)key.attachment();
                    if (ka == null) {
                        this.cancelledKey(key, SocketStatus.ERROR, false);
                    }
                    else if (ka.getError()) {
                        this.cancelledKey(key, SocketStatus.ERROR, true);
                    }
                    else if (ka.isComet() && ka.getCometNotify()) {
                        ka.setCometNotify(false);
                        this.reg(key, ka, 0);
                        if (NioEndpoint.this.processSocket(ka.getChannel(), SocketStatus.OPEN_READ, true)) {
                            continue;
                        }
                        NioEndpoint.this.processSocket(ka.getChannel(), SocketStatus.DISCONNECT, true);
                    }
                    else if ((ka.interestOps() & 0x1) == 0x1 || (ka.interestOps() & 0x4) == 0x4) {
                        final long delta = now - ka.getLastAccess();
                        final long timeout = ka.getTimeout();
                        final boolean isTimedout = timeout > 0L && delta > timeout;
                        if (this.close) {
                            key.interestOps(0);
                            ka.interestOps(0);
                            this.processKey(key, ka);
                        }
                        else {
                            if (!isTimedout) {
                                continue;
                            }
                            key.interestOps(0);
                            ka.interestOps(0);
                            this.cancelledKey(key, SocketStatus.TIMEOUT, true);
                        }
                    }
                    else {
                        if (!ka.isAsync() && !ka.isComet()) {
                            continue;
                        }
                        if (this.close) {
                            key.interestOps(0);
                            ka.interestOps(0);
                            this.processKey(key, ka);
                        }
                        else {
                            if (ka.isAsync() && ka.getTimeout() <= 0L) {
                                continue;
                            }
                            final long delta = now - ka.getLastAccess();
                            final long timeout = (ka.getTimeout() == -1L) ? NioEndpoint.this.socketProperties.getSoTimeout() : ka.getTimeout();
                            final boolean isTimedout = delta > timeout;
                            if (!isTimedout) {
                                continue;
                            }
                            ka.access(Long.MAX_VALUE);
                            NioEndpoint.this.processSocket(ka.getChannel(), SocketStatus.TIMEOUT, true);
                        }
                    }
                }
                catch (CancelledKeyException ckx) {
                    this.cancelledKey(key, SocketStatus.ERROR, false);
                }
            }
            final long prevExp = this.nextExpiration;
            this.nextExpiration = System.currentTimeMillis() + NioEndpoint.this.socketProperties.getTimeoutInterval();
            if (NioEndpoint.log.isTraceEnabled()) {
                NioEndpoint.log.trace((Object)("timeout completed: keys processed=" + keycount + "; now=" + now + "; nextExpiration=" + prevExp + "; keyCount=" + keyCount + "; hasEvents=" + hasEvents + "; eval=" + (now < prevExp && (keyCount > 0 || hasEvents) && !this.close)));
            }
        }
    }
    
    public static class KeyAttachment extends SocketWrapper<NioChannel>
    {
        protected Poller poller;
        protected int interestOps;
        protected boolean comet;
        protected int cometOps;
        protected boolean cometNotify;
        protected CountDownLatch readLatch;
        protected CountDownLatch writeLatch;
        protected SendfileData sendfileData;
        private long writeTimeout;
        
        public KeyAttachment(final NioChannel channel) {
            super(channel);
            this.poller = null;
            this.interestOps = 0;
            this.comet = false;
            this.cometOps = 1;
            this.cometNotify = false;
            this.readLatch = null;
            this.writeLatch = null;
            this.sendfileData = null;
            this.writeTimeout = -1L;
        }
        
        public void reset(final Poller poller, final NioChannel channel, final long soTimeout) {
            super.reset(channel, soTimeout);
            this.cometNotify = false;
            this.cometOps = 1;
            this.interestOps = 0;
            this.poller = poller;
            this.lastRegistered = 0L;
            this.sendfileData = null;
            if (this.readLatch != null) {
                try {
                    for (int i = 0; i < (int)this.readLatch.getCount(); ++i) {
                        this.readLatch.countDown();
                    }
                }
                catch (Exception ex) {}
            }
            this.readLatch = null;
            this.sendfileData = null;
            if (this.writeLatch != null) {
                try {
                    for (int i = 0; i < (int)this.writeLatch.getCount(); ++i) {
                        this.writeLatch.countDown();
                    }
                }
                catch (Exception ex2) {}
            }
            this.writeLatch = null;
            this.setWriteTimeout(soTimeout);
        }
        
        public void reset() {
            this.reset(null, null, -1L);
        }
        
        public Poller getPoller() {
            return this.poller;
        }
        
        public void setPoller(final Poller poller) {
            this.poller = poller;
        }
        
        public void setCometNotify(final boolean notify) {
            this.cometNotify = notify;
        }
        
        public boolean getCometNotify() {
            return this.cometNotify;
        }
        
        @Deprecated
        public void setCometOps(final int ops) {
            this.cometOps = ops;
        }
        
        @Deprecated
        public int getCometOps() {
            return this.cometOps;
        }
        
        public NioChannel getChannel() {
            return this.getSocket();
        }
        
        public void setChannel(final NioChannel channel) {
            this.socket = (E)channel;
        }
        
        public int interestOps() {
            return this.interestOps;
        }
        
        public int interestOps(final int ops) {
            return this.interestOps = ops;
        }
        
        public CountDownLatch getReadLatch() {
            return this.readLatch;
        }
        
        public CountDownLatch getWriteLatch() {
            return this.writeLatch;
        }
        
        protected CountDownLatch resetLatch(final CountDownLatch latch) {
            if (latch == null || latch.getCount() == 0L) {
                return null;
            }
            throw new IllegalStateException("Latch must be at count 0");
        }
        
        public void resetReadLatch() {
            this.readLatch = this.resetLatch(this.readLatch);
        }
        
        public void resetWriteLatch() {
            this.writeLatch = this.resetLatch(this.writeLatch);
        }
        
        protected CountDownLatch startLatch(final CountDownLatch latch, final int cnt) {
            if (latch == null || latch.getCount() == 0L) {
                return new CountDownLatch(cnt);
            }
            throw new IllegalStateException("Latch must be at count 0 or null.");
        }
        
        public void startReadLatch(final int cnt) {
            this.readLatch = this.startLatch(this.readLatch, cnt);
        }
        
        public void startWriteLatch(final int cnt) {
            this.writeLatch = this.startLatch(this.writeLatch, cnt);
        }
        
        protected void awaitLatch(final CountDownLatch latch, final long timeout, final TimeUnit unit) throws InterruptedException {
            if (latch == null) {
                throw new IllegalStateException("Latch cannot be null");
            }
            latch.await(timeout, unit);
        }
        
        public void awaitReadLatch(final long timeout, final TimeUnit unit) throws InterruptedException {
            this.awaitLatch(this.readLatch, timeout, unit);
        }
        
        public void awaitWriteLatch(final long timeout, final TimeUnit unit) throws InterruptedException {
            this.awaitLatch(this.writeLatch, timeout, unit);
        }
        
        @Deprecated
        public long getLastRegistered() {
            return this.lastRegistered;
        }
        
        @Deprecated
        public void setLastRegistered(final long reg) {
            this.lastRegistered = reg;
        }
        
        public void setSendfileData(final SendfileData sf) {
            this.sendfileData = sf;
        }
        
        public SendfileData getSendfileData() {
            return this.sendfileData;
        }
        
        public void setWriteTimeout(final long writeTimeout) {
            this.writeTimeout = writeTimeout;
        }
        
        public long getWriteTimeout() {
            return this.writeTimeout;
        }
    }
    
    public static class NioBufferHandler implements SecureNioChannel.ApplicationBufferHandler
    {
        protected ByteBuffer readbuf;
        protected ByteBuffer writebuf;
        
        public NioBufferHandler(final int readsize, final int writesize, final boolean direct) {
            this.readbuf = null;
            this.writebuf = null;
            if (direct) {
                this.readbuf = ByteBuffer.allocateDirect(readsize);
                this.writebuf = ByteBuffer.allocateDirect(writesize);
            }
            else {
                this.readbuf = ByteBuffer.allocate(readsize);
                this.writebuf = ByteBuffer.allocate(writesize);
            }
        }
        
        @Override
        public ByteBuffer expand(final ByteBuffer buffer, final int remaining) {
            return buffer;
        }
        
        @Override
        public ByteBuffer getReadBuffer() {
            return this.readbuf;
        }
        
        @Override
        public ByteBuffer getWriteBuffer() {
            return this.writebuf;
        }
    }
    
    protected class SocketProcessor implements Runnable
    {
        protected NioChannel socket;
        protected SocketStatus status;
        
        public SocketProcessor(final NioChannel socket, final SocketStatus status) {
            this.socket = null;
            this.status = null;
            this.reset(socket, status);
        }
        
        public void reset(final NioChannel socket, final SocketStatus status) {
            this.socket = socket;
            this.status = status;
        }
        
        @Override
        public void run() {
            final SelectionKey key = this.socket.getIOChannel().keyFor(this.socket.getPoller().getSelector());
            KeyAttachment ka = null;
            if (key != null) {
                ka = (KeyAttachment)key.attachment();
            }
            if (ka != null && ka.isUpgraded() && SocketStatus.OPEN_WRITE == this.status) {
                synchronized (ka.getWriteThreadLock()) {
                    this.doRun(key, ka);
                }
            }
            else {
                synchronized (this.socket) {
                    this.doRun(key, ka);
                }
            }
        }
        
        private void doRun(final SelectionKey key, KeyAttachment ka) {
            final boolean launch = false;
            try {
                int handshake = -1;
                try {
                    if (key != null) {
                        if (this.socket.isHandshakeComplete() || this.status == SocketStatus.STOP) {
                            handshake = 0;
                        }
                        else {
                            handshake = this.socket.handshake(key.isReadable(), key.isWritable());
                            this.status = SocketStatus.OPEN_READ;
                        }
                    }
                }
                catch (IOException x) {
                    handshake = -1;
                    if (NioEndpoint.log.isDebugEnabled()) {
                        NioEndpoint.log.debug((Object)"Error during SSL handshake", (Throwable)x);
                    }
                }
                catch (CancelledKeyException ckx) {
                    handshake = -1;
                }
                if (handshake == 0) {
                    AbstractEndpoint.Handler.SocketState state = AbstractEndpoint.Handler.SocketState.OPEN;
                    if (this.status == null) {
                        state = NioEndpoint.this.handler.process(ka, SocketStatus.OPEN_READ);
                    }
                    else {
                        state = NioEndpoint.this.handler.process(ka, this.status);
                    }
                    if (state == AbstractEndpoint.Handler.SocketState.CLOSED) {
                        try {
                            if (ka != null) {
                                ka.setComet(false);
                            }
                            this.socket.getPoller().cancelledKey(key, SocketStatus.ERROR, false);
                            if (NioEndpoint.this.running && !NioEndpoint.this.paused) {
                                NioEndpoint.this.nioChannels.offer(this.socket);
                            }
                            this.socket = null;
                            if (NioEndpoint.this.running && !NioEndpoint.this.paused && ka != null) {
                                NioEndpoint.this.keyCache.offer(ka);
                            }
                            ka = null;
                        }
                        catch (Exception x2) {
                            NioEndpoint.log.error((Object)"", (Throwable)x2);
                        }
                    }
                }
                else if (handshake == -1) {
                    if (key != null) {
                        this.socket.getPoller().cancelledKey(key, SocketStatus.DISCONNECT, false);
                    }
                    NioEndpoint.this.nioChannels.offer(this.socket);
                    this.socket = null;
                    if (ka != null) {
                        NioEndpoint.this.keyCache.offer(ka);
                    }
                    ka = null;
                }
                else {
                    ka.getPoller().add(this.socket, handshake);
                }
            }
            catch (CancelledKeyException cx) {
                this.socket.getPoller().cancelledKey(key, null, false);
            }
            catch (OutOfMemoryError oom) {
                try {
                    NioEndpoint.this.oomParachuteData = null;
                    NioEndpoint.log.error((Object)"", (Throwable)oom);
                    if (this.socket != null) {
                        this.socket.getPoller().cancelledKey(key, SocketStatus.ERROR, false);
                    }
                    NioEndpoint.this.releaseCaches();
                }
                catch (Throwable oomt) {
                    try {
                        System.err.println("SEVERE:Memory usage is low, parachute is non existent, your system may start failing.");
                        oomt.printStackTrace();
                    }
                    catch (Throwable letsHopeWeDontGetHere) {
                        ExceptionUtils.handleThrowable(letsHopeWeDontGetHere);
                    }
                }
            }
            catch (VirtualMachineError vme) {
                ExceptionUtils.handleThrowable(vme);
            }
            catch (Throwable t) {
                NioEndpoint.log.error((Object)"", t);
                if (this.socket != null) {
                    this.socket.getPoller().cancelledKey(key, SocketStatus.ERROR, false);
                }
            }
            finally {
                if (launch) {
                    try {
                        NioEndpoint.this.getExecutor().execute(new SocketProcessor(this.socket, SocketStatus.OPEN_READ));
                    }
                    catch (NullPointerException npe) {
                        if (NioEndpoint.this.running) {
                            NioEndpoint.log.error((Object)AbstractEndpoint.sm.getString("endpoint.launch.fail"), (Throwable)npe);
                        }
                    }
                }
                this.socket = null;
                this.status = null;
                if (NioEndpoint.this.running && !NioEndpoint.this.paused) {
                    NioEndpoint.this.processorCache.offer(this);
                }
            }
        }
    }
    
    public static class SendfileData
    {
        public String fileName;
        public FileChannel fchannel;
        public long pos;
        public long length;
        public boolean keepAlive;
    }
    
    public interface Handler extends AbstractEndpoint.Handler
    {
        SocketState process(final SocketWrapper<NioChannel> p0, final SocketStatus p1);
        
        void release(final SocketWrapper<NioChannel> p0);
        
        void release(final SocketChannel p0);
        
        SSLImplementation getSslImplementation();
    }
}
