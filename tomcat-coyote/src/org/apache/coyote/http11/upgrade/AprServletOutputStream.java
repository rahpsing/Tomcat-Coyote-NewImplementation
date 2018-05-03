// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import org.apache.tomcat.jni.OS;
import java.io.EOFException;
import org.apache.tomcat.jni.Status;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import org.apache.tomcat.jni.Socket;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.net.AprEndpoint;

public class AprServletOutputStream extends AbstractServletOutputStream
{
    private static final int SSL_OUTPUT_BUFFER_SIZE = 8192;
    private final AprEndpoint endpoint;
    private final SocketWrapper<Long> wrapper;
    private final long socket;
    private volatile boolean closed;
    private final ByteBuffer sslOutputBuffer;
    
    public AprServletOutputStream(final SocketWrapper<Long> wrapper, final AprEndpoint endpoint) {
        this.closed = false;
        this.endpoint = endpoint;
        this.wrapper = wrapper;
        this.socket = wrapper.getSocket();
        if (endpoint.isSSLEnabled()) {
            (this.sslOutputBuffer = ByteBuffer.allocateDirect(8192)).position(8192);
        }
        else {
            this.sslOutputBuffer = null;
        }
    }
    
    @Override
    protected int doWrite(final boolean block, final byte[] b, final int off, final int len) throws IOException {
        final Lock readLock = this.wrapper.getBlockingStatusReadLock();
        final ReentrantReadWriteLock.WriteLock writeLock = this.wrapper.getBlockingStatusWriteLock();
        try {
            readLock.lock();
            if (this.wrapper.getBlockingStatus() == block) {
                if (this.closed) {
                    throw new IOException(AprServletOutputStream.sm.getString("apr.closed", new Object[] { this.socket }));
                }
                return this.doWriteInternal(b, off, len);
            }
        }
        finally {
            readLock.unlock();
        }
        try {
            writeLock.lock();
            this.wrapper.setBlockingStatus(block);
            if (block) {
                Socket.timeoutSet(this.socket, this.endpoint.getSoTimeout() * 1000);
            }
            else {
                Socket.timeoutSet(this.socket, 0L);
            }
            try {
                readLock.lock();
                writeLock.unlock();
                if (this.closed) {
                    throw new IOException(AprServletOutputStream.sm.getString("apr.closed", new Object[] { this.socket }));
                }
                return this.doWriteInternal(b, off, len);
            }
            finally {
                readLock.unlock();
            }
        }
        finally {
            if (writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
    }
    
    private int doWriteInternal(final byte[] b, final int off, final int len) throws IOException {
        int start = off;
        int left = len;
        int written;
        do {
            if (this.endpoint.isSSLEnabled()) {
                if (this.sslOutputBuffer.remaining() == 0) {
                    this.sslOutputBuffer.clear();
                    if (left < 8192) {
                        this.sslOutputBuffer.put(b, start, left);
                    }
                    else {
                        this.sslOutputBuffer.put(b, start, 8192);
                    }
                    this.sslOutputBuffer.flip();
                }
                written = Socket.sendb(this.socket, this.sslOutputBuffer, this.sslOutputBuffer.position(), this.sslOutputBuffer.limit());
                if (written > 0) {
                    this.sslOutputBuffer.position(this.sslOutputBuffer.position() + written);
                }
            }
            else {
                written = Socket.send(this.socket, b, start, left);
            }
            if (Status.APR_STATUS_IS_EAGAIN(-written)) {
                written = 0;
            }
            else {
                if (-written == 70014) {
                    throw new EOFException(AprServletOutputStream.sm.getString("apr.clientAbort"));
                }
                if ((OS.IS_WIN32 || OS.IS_WIN64) && -written == 730053) {
                    throw new EOFException(AprServletOutputStream.sm.getString("apr.clientAbort"));
                }
                if (-written == 20014 && this.wrapper.isSecure()) {
                    throw new EOFException(AprServletOutputStream.sm.getString("apr.clientAbort"));
                }
                if (written < 0) {
                    throw new IOException(AprServletOutputStream.sm.getString("apr.write.error", new Object[] { -written, this.socket }));
                }
            }
            start += written;
            left -= written;
        } while (written > 0 && left > 0);
        if (left > 0) {
            this.endpoint.getPoller().add(this.socket, -1, false, true);
        }
        return len - left;
    }
    
    @Override
    protected void doFlush() throws IOException {
    }
    
    @Override
    protected void doClose() throws IOException {
        this.closed = true;
    }
}
