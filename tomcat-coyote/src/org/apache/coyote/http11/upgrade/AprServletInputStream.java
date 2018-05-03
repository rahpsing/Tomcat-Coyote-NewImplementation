// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.io.EOFException;
import org.apache.tomcat.jni.OS;
import org.apache.tomcat.jni.Socket;
import java.io.IOException;
import org.apache.tomcat.util.net.SocketWrapper;

public class AprServletInputStream extends AbstractServletInputStream
{
    private final SocketWrapper<Long> wrapper;
    private final long socket;
    private volatile boolean eagain;
    private volatile boolean closed;
    
    public AprServletInputStream(final SocketWrapper<Long> wrapper) {
        this.eagain = false;
        this.closed = false;
        this.wrapper = wrapper;
        this.socket = wrapper.getSocket();
    }
    
    @Override
    protected int doRead(final boolean block, final byte[] b, final int off, final int len) throws IOException {
        final Lock readLock = this.wrapper.getBlockingStatusReadLock();
        final ReentrantReadWriteLock.WriteLock writeLock = this.wrapper.getBlockingStatusWriteLock();
        boolean readDone = false;
        int result = 0;
        try {
            readLock.lock();
            if (this.wrapper.getBlockingStatus() == block) {
                if (this.closed) {
                    throw new IOException(AprServletInputStream.sm.getString("apr.closed", new Object[] { this.socket }));
                }
                result = Socket.recv(this.socket, b, off, len);
                readDone = true;
            }
        }
        finally {
            readLock.unlock();
        }
        if (!readDone) {
            try {
                writeLock.lock();
                this.wrapper.setBlockingStatus(block);
                Socket.optSet(this.socket, 8, block ? 0 : 1);
                try {
                    readLock.lock();
                    writeLock.unlock();
                    if (this.closed) {
                        throw new IOException(AprServletInputStream.sm.getString("apr.closed", new Object[] { this.socket }));
                    }
                    result = Socket.recv(this.socket, b, off, len);
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
        if (result > 0) {
            this.eagain = false;
            return result;
        }
        if (-result == 120002) {
            this.eagain = true;
            return 0;
        }
        if ((OS.IS_WIN32 || OS.IS_WIN64) && -result == 730053) {
            throw new EOFException(AprServletInputStream.sm.getString("apr.clientAbort"));
        }
        if (-result == 20014 && this.wrapper.isSecure()) {
            throw new EOFException(AprServletInputStream.sm.getString("apr.clientAbort"));
        }
        throw new IOException(AprServletInputStream.sm.getString("apr.read.error", new Object[] { -result, this.socket }));
    }
    
    @Override
    protected boolean doIsReady() {
        return !this.eagain;
    }
    
    @Override
    protected void doClose() throws IOException {
        this.closed = true;
    }
}
