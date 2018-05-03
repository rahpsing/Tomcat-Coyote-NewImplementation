// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import org.apache.tomcat.util.ExceptionUtils;
import java.io.IOException;
import org.apache.coyote.http11.upgrade.servlet31.WriteListener;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.ServletOutputStream;

public abstract class AbstractServletOutputStream extends ServletOutputStream
{
    protected static final StringManager sm;
    private final Object fireListenerLock;
    private final Object writeLock;
    private volatile boolean closeRequired;
    private volatile WriteListener listener;
    private volatile boolean fireListener;
    private volatile byte[] buffer;
    
    public AbstractServletOutputStream() {
        this.fireListenerLock = new Object();
        this.writeLock = new Object();
        this.closeRequired = false;
        this.listener = null;
        this.fireListener = false;
    }
    
    public final boolean isReady() {
        if (this.listener == null) {
            throw new IllegalStateException(AbstractServletOutputStream.sm.getString("upgrade.sos.canWrite.is"));
        }
        synchronized (this.fireListenerLock) {
            final boolean result = this.buffer == null;
            this.fireListener = !result;
            return result;
        }
    }
    
    public final void setWriteListener(final WriteListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(AbstractServletOutputStream.sm.getString("upgrade.sos.writeListener.null"));
        }
        this.listener = listener;
    }
    
    protected final boolean isCloseRequired() {
        return this.closeRequired;
    }
    
    public void write(final int b) throws IOException {
        synchronized (this.writeLock) {
            this.preWriteChecks();
            this.writeInternal(new byte[] { (byte)b }, 0, 1);
        }
    }
    
    public void write(final byte[] b, final int off, final int len) throws IOException {
        synchronized (this.writeLock) {
            this.preWriteChecks();
            this.writeInternal(b, off, len);
        }
    }
    
    public void close() throws IOException {
        this.closeRequired = true;
        this.doClose();
    }
    
    private void preWriteChecks() {
        if (this.buffer != null) {
            throw new IllegalStateException(AbstractServletOutputStream.sm.getString("upgrade.sis.write.ise"));
        }
    }
    
    private void writeInternal(final byte[] b, final int off, final int len) throws IOException {
        if (this.listener == null) {
            this.doWrite(true, b, off, len);
        }
        else {
            final int written = this.doWrite(false, b, off, len);
            if (written < len) {
                System.arraycopy(b, off + written, this.buffer = new byte[len - written], 0, len - written);
            }
            else {
                this.buffer = null;
            }
        }
    }
    
    protected final void onWritePossible() throws IOException {
        synchronized (this.writeLock) {
            try {
                this.writeInternal(this.buffer, 0, this.buffer.length);
            }
            catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                this.listener.onError(t);
                if (t instanceof IOException) {
                    throw (IOException)t;
                }
                throw new IOException(t);
            }
            boolean fire = false;
            synchronized (this.fireListenerLock) {
                if (this.buffer == null && this.fireListener) {
                    this.fireListener = false;
                    fire = true;
                }
            }
            if (fire) {
                this.listener.onWritePossible();
            }
        }
    }
    
    protected abstract int doWrite(final boolean p0, final byte[] p1, final int p2, final int p3) throws IOException;
    
    protected abstract void doFlush() throws IOException;
    
    protected abstract void doClose() throws IOException;
    
    static {
        sm = StringManager.getManager("org.apache.coyote.http11.upgrade");
    }
}
