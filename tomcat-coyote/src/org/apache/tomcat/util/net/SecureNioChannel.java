// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.nio.channels.SelectionKey;
import java.net.SocketTimeoutException;
import java.io.EOFException;
import java.nio.channels.Selector;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngine;
import java.nio.ByteBuffer;

public class SecureNioChannel extends NioChannel
{
    protected ByteBuffer netInBuffer;
    protected ByteBuffer netOutBuffer;
    protected SSLEngine sslEngine;
    protected boolean handshakeComplete;
    protected SSLEngineResult.HandshakeStatus handshakeStatus;
    protected boolean closed;
    protected boolean closing;
    protected NioSelectorPool pool;
    
    public SecureNioChannel(final SocketChannel channel, final SSLEngine engine, final ApplicationBufferHandler bufHandler, final NioSelectorPool pool) throws IOException {
        super(channel, bufHandler);
        this.handshakeComplete = false;
        this.closed = false;
        this.closing = false;
        this.sslEngine = engine;
        final int appBufSize = this.sslEngine.getSession().getApplicationBufferSize();
        final int netBufSize = this.sslEngine.getSession().getPacketBufferSize();
        if (this.netInBuffer == null) {
            this.netInBuffer = ByteBuffer.allocateDirect(netBufSize);
        }
        if (this.netOutBuffer == null) {
            this.netOutBuffer = ByteBuffer.allocateDirect(netBufSize);
        }
        this.pool = pool;
        bufHandler.expand(bufHandler.getReadBuffer(), appBufSize);
        bufHandler.expand(bufHandler.getWriteBuffer(), appBufSize);
        this.reset();
    }
    
    public void reset(final SSLEngine engine) throws IOException {
        this.sslEngine = engine;
        this.reset();
    }
    
    @Override
    public void reset() throws IOException {
        super.reset();
        this.netOutBuffer.position(0);
        this.netOutBuffer.limit(0);
        this.netInBuffer.position(0);
        this.netInBuffer.limit(0);
        this.handshakeComplete = false;
        this.closed = false;
        this.closing = false;
        this.sslEngine.beginHandshake();
        this.handshakeStatus = this.sslEngine.getHandshakeStatus();
    }
    
    @Override
    public int getBufferSize() {
        int size = super.getBufferSize();
        size += ((this.netInBuffer != null) ? this.netInBuffer.capacity() : 0);
        size += ((this.netOutBuffer != null) ? this.netOutBuffer.capacity() : 0);
        return size;
    }
    
    @Override
    public boolean flush(final boolean block, final Selector s, final long timeout) throws IOException {
        if (!block) {
            this.flush(this.netOutBuffer);
        }
        else {
            this.pool.write(this.netOutBuffer, this, s, timeout, block);
        }
        return !this.netOutBuffer.hasRemaining();
    }
    
    protected boolean flush(final ByteBuffer buf) throws IOException {
        final int remaining = buf.remaining();
        if (remaining > 0) {
            final int written = this.sc.write(buf);
            return written >= remaining;
        }
        return true;
    }
    
    @Override
    public int handshake(final boolean read, final boolean write) throws IOException {
        if (this.handshakeComplete) {
            return 0;
        }
        if (!this.flush(this.netOutBuffer)) {
            return 4;
        }
        SSLEngineResult handshake = null;
        while (!this.handshakeComplete) {
            switch (this.handshakeStatus) {
                case NOT_HANDSHAKING: {
                    throw new IOException("NOT_HANDSHAKING during handshake");
                }
                case FINISHED: {
                    this.handshakeComplete = !this.netOutBuffer.hasRemaining();
                    return this.handshakeComplete ? 0 : 4;
                }
                case NEED_WRAP: {
                    handshake = this.handshakeWrap(write);
                    if (handshake.getStatus() != SSLEngineResult.Status.OK) {
                        throw new IOException("Unexpected status:" + handshake.getStatus() + " during handshake WRAP.");
                    }
                    if (this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                        this.handshakeStatus = this.tasks();
                    }
                    if (this.handshakeStatus != SSLEngineResult.HandshakeStatus.NEED_UNWRAP || !this.flush(this.netOutBuffer)) {
                        return 4;
                    }
                }
                case NEED_UNWRAP: {
                    handshake = this.handshakeUnwrap(read);
                    if (handshake.getStatus() == SSLEngineResult.Status.OK) {
                        if (this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                            this.handshakeStatus = this.tasks();
                            continue;
                        }
                        continue;
                    }
                    else {
                        if (handshake.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                            return 1;
                        }
                        throw new IOException("Invalid handshake status:" + this.handshakeStatus + " during handshake UNWRAP.");
                    }
                    break;
                }
                case NEED_TASK: {
                    this.handshakeStatus = this.tasks();
                    continue;
                }
                default: {
                    throw new IllegalStateException("Invalid handshake status:" + this.handshakeStatus);
                }
            }
        }
        return this.handshakeComplete ? 0 : 5;
    }
    
    public void rehandshake(final long timeout) throws IOException {
        if (this.netInBuffer.position() > 0 && this.netInBuffer.position() < this.netInBuffer.limit()) {
            throw new IOException("Network input buffer still contains data. Handshake will fail.");
        }
        if (this.netOutBuffer.position() > 0 && this.netOutBuffer.position() < this.netOutBuffer.limit()) {
            throw new IOException("Network output buffer still contains data. Handshake will fail.");
        }
        if (this.getBufHandler().getReadBuffer().position() > 0 && this.getBufHandler().getReadBuffer().position() < this.getBufHandler().getReadBuffer().limit()) {
            throw new IOException("Application input buffer still contains data. Data would have been lost.");
        }
        if (this.getBufHandler().getWriteBuffer().position() > 0 && this.getBufHandler().getWriteBuffer().position() < this.getBufHandler().getWriteBuffer().limit()) {
            throw new IOException("Application output buffer still contains data. Data would have been lost.");
        }
        this.reset();
        boolean isReadable = true;
        boolean isWriteable = true;
        boolean handshaking = true;
        Selector selector = null;
        SelectionKey key = null;
        try {
            while (handshaking) {
                final int hsStatus = this.handshake(isReadable, isWriteable);
                switch (hsStatus) {
                    case -1: {
                        throw new EOFException("EOF during handshake.");
                    }
                    case 0: {
                        handshaking = false;
                        continue;
                    }
                    default: {
                        final long now = System.currentTimeMillis();
                        if (selector == null) {
                            synchronized (Selector.class) {
                                selector = Selector.open();
                            }
                            key = this.getIOChannel().register(selector, hsStatus);
                        }
                        else {
                            key.interestOps(hsStatus);
                        }
                        final int keyCount = selector.select(timeout);
                        if (keyCount == 0 && System.currentTimeMillis() - now >= timeout) {
                            throw new SocketTimeoutException("Handshake operation timed out.");
                        }
                        isReadable = key.isReadable();
                        isWriteable = key.isWritable();
                        continue;
                    }
                }
            }
        }
        catch (IOException x) {
            throw x;
        }
        catch (Exception cx) {
            final IOException x2 = new IOException(cx);
            throw x2;
        }
        finally {
            if (key != null) {
                try {
                    key.cancel();
                }
                catch (Exception ex) {}
            }
            if (selector != null) {
                try {
                    selector.close();
                }
                catch (Exception ex2) {}
            }
        }
    }
    
    protected SSLEngineResult.HandshakeStatus tasks() {
        Runnable r = null;
        while ((r = this.sslEngine.getDelegatedTask()) != null) {
            r.run();
        }
        return this.sslEngine.getHandshakeStatus();
    }
    
    protected SSLEngineResult handshakeWrap(final boolean doWrite) throws IOException {
        this.netOutBuffer.clear();
        final SSLEngineResult result = this.sslEngine.wrap(this.bufHandler.getWriteBuffer(), this.netOutBuffer);
        this.netOutBuffer.flip();
        this.handshakeStatus = result.getHandshakeStatus();
        if (doWrite) {
            this.flush(this.netOutBuffer);
        }
        return result;
    }
    
    protected SSLEngineResult handshakeUnwrap(final boolean doread) throws IOException {
        if (this.netInBuffer.position() == this.netInBuffer.limit()) {
            this.netInBuffer.clear();
        }
        if (doread) {
            final int read = this.sc.read(this.netInBuffer);
            if (read == -1) {
                throw new IOException("EOF encountered during handshake.");
            }
        }
        boolean cont = false;
        SSLEngineResult result;
        do {
            this.netInBuffer.flip();
            result = this.sslEngine.unwrap(this.netInBuffer, this.bufHandler.getReadBuffer());
            this.netInBuffer.compact();
            this.handshakeStatus = result.getHandshakeStatus();
            if (result.getStatus() == SSLEngineResult.Status.OK && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                this.handshakeStatus = this.tasks();
            }
            cont = (result.getStatus() == SSLEngineResult.Status.OK && this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP);
        } while (cont);
        return result;
    }
    
    @Override
    public void close() throws IOException {
        if (this.closing) {
            return;
        }
        this.closing = true;
        this.sslEngine.closeOutbound();
        if (!this.flush(this.netOutBuffer)) {
            throw new IOException("Remaining data in the network buffer, can't send SSL close message, force a close with close(true) instead");
        }
        this.netOutBuffer.clear();
        final SSLEngineResult handshake = this.sslEngine.wrap(this.getEmptyBuf(), this.netOutBuffer);
        if (handshake.getStatus() != SSLEngineResult.Status.CLOSED) {
            throw new IOException("Invalid close state, will not send network data.");
        }
        this.netOutBuffer.flip();
        this.flush(this.netOutBuffer);
        this.closed = (!this.netOutBuffer.hasRemaining() && handshake.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NEED_WRAP);
    }
    
    @Override
    public void close(final boolean force) throws IOException {
        try {
            this.close();
        }
        finally {
            if (force || this.closed) {
                this.closed = true;
                this.sc.socket().close();
                this.sc.close();
            }
        }
    }
    
    @Override
    public int read(final ByteBuffer dst) throws IOException {
        if (dst != this.bufHandler.getReadBuffer()) {
            throw new IllegalArgumentException("You can only read using the application read buffer provided by the handler.");
        }
        if (this.closing || this.closed) {
            return -1;
        }
        if (!this.handshakeComplete) {
            throw new IllegalStateException("Handshake incomplete, you must complete handshake before reading data.");
        }
        final int netread = this.sc.read(this.netInBuffer);
        if (netread == -1) {
            return -1;
        }
        int read = 0;
        do {
            this.netInBuffer.flip();
            final SSLEngineResult unwrap = this.sslEngine.unwrap(this.netInBuffer, dst);
            this.netInBuffer.compact();
            if (unwrap.getStatus() == SSLEngineResult.Status.OK || unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                read += unwrap.bytesProduced();
                if (unwrap.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    this.tasks();
                }
                if (unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    break;
                }
                continue;
            }
            else {
                if (unwrap.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW && read > 0) {
                    break;
                }
                throw new IOException("Unable to unwrap data, invalid status: " + unwrap.getStatus());
            }
        } while (this.netInBuffer.position() != 0);
        return read;
    }
    
    @Override
    public int write(final ByteBuffer src) throws IOException {
        if (src == this.netOutBuffer) {
            final int written = this.sc.write(src);
            return written;
        }
        if (!this.isSendFile() && src != this.bufHandler.getWriteBuffer()) {
            throw new IllegalArgumentException("You can only write using the application write buffer provided by the handler.");
        }
        if (this.closing || this.closed) {
            throw new IOException("Channel is in closing state.");
        }
        int written = 0;
        if (!this.flush(this.netOutBuffer)) {
            return written;
        }
        this.netOutBuffer.clear();
        final SSLEngineResult result = this.sslEngine.wrap(src, this.netOutBuffer);
        written = result.bytesConsumed();
        this.netOutBuffer.flip();
        if (result.getStatus() == SSLEngineResult.Status.OK) {
            if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                this.tasks();
            }
            this.flush(this.netOutBuffer);
            return written;
        }
        throw new IOException("Unable to wrap data, invalid engine state: " + result.getStatus());
    }
    
    @Override
    public int getOutboundRemaining() {
        return this.netOutBuffer.remaining();
    }
    
    @Override
    public boolean flushOutbound() throws IOException {
        final int remaining = this.netOutBuffer.remaining();
        this.flush(this.netOutBuffer);
        final int remaining2 = this.netOutBuffer.remaining();
        return remaining2 < remaining;
    }
    
    @Override
    public ApplicationBufferHandler getBufHandler() {
        return this.bufHandler;
    }
    
    @Override
    public boolean isHandshakeComplete() {
        return this.handshakeComplete;
    }
    
    @Override
    public boolean isClosing() {
        return this.closing;
    }
    
    public SSLEngine getSslEngine() {
        return this.sslEngine;
    }
    
    public ByteBuffer getEmptyBuf() {
        return SecureNioChannel.emptyBuf;
    }
    
    public void setBufHandler(final ApplicationBufferHandler bufHandler) {
        this.bufHandler = bufHandler;
    }
    
    @Override
    public SocketChannel getIOChannel() {
        return this.sc;
    }
    
    public interface ApplicationBufferHandler
    {
        ByteBuffer expand(final ByteBuffer p0, final int p1);
        
        ByteBuffer getReadBuffer();
        
        ByteBuffer getWriteBuffer();
    }
}
