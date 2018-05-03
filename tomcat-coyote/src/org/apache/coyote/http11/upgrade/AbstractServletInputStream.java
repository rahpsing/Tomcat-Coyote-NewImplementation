// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.io.IOException;
import org.apache.coyote.http11.upgrade.servlet31.ReadListener;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.ServletInputStream;

public abstract class AbstractServletInputStream extends ServletInputStream
{
    protected static final StringManager sm;
    private volatile boolean closeRequired;
    private volatile Boolean ready;
    private volatile ReadListener listener;
    
    public AbstractServletInputStream() {
        this.closeRequired = false;
        this.ready = Boolean.TRUE;
        this.listener = null;
    }
    
    public final boolean isFinished() {
        if (this.listener == null) {
            throw new IllegalStateException(AbstractServletInputStream.sm.getString("upgrade.sis.isFinished.ise"));
        }
        return false;
    }
    
    public final boolean isReady() {
        if (this.listener == null) {
            throw new IllegalStateException(AbstractServletInputStream.sm.getString("upgrade.sis.isReady.ise"));
        }
        if (this.ready != null) {
            return this.ready;
        }
        try {
            this.ready = this.doIsReady();
        }
        catch (IOException e) {
            this.listener.onError(e);
            this.ready = Boolean.FALSE;
        }
        return this.ready;
    }
    
    public final void setReadListener(final ReadListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(AbstractServletInputStream.sm.getString("upgrade.sis.readListener.null"));
        }
        this.listener = listener;
        this.ready = null;
    }
    
    public final int read() throws IOException {
        this.preReadChecks();
        return this.readInternal();
    }
    
    public final int readLine(final byte[] b, int off, final int len) throws IOException {
        this.preReadChecks();
        if (len <= 0) {
            return 0;
        }
        int count = 0;
        int c;
        while ((c = this.readInternal()) != -1) {
            b[off++] = (byte)c;
            ++count;
            if (c == 10 || count == len) {
                break;
            }
        }
        return (count > 0) ? count : -1;
    }
    
    public final int read(final byte[] b, final int off, final int len) throws IOException {
        this.preReadChecks();
        try {
            return this.doRead(this.listener == null, b, off, len);
        }
        catch (IOException ioe) {
            this.closeRequired = true;
            throw ioe;
        }
    }
    
    public void close() throws IOException {
        this.closeRequired = true;
        this.doClose();
    }
    
    private void preReadChecks() {
        if (this.listener != null && (this.ready == null || !this.ready)) {
            throw new IllegalStateException(AbstractServletInputStream.sm.getString("upgrade.sis.read.ise"));
        }
        this.ready = null;
    }
    
    private int readInternal() throws IOException {
        final ReadListener readListener = this.listener;
        final byte[] b = { 0 };
        int result;
        try {
            result = this.doRead(readListener == null, b, 0, 1);
        }
        catch (IOException ioe) {
            this.closeRequired = true;
            throw ioe;
        }
        if (result == 0) {
            return -1;
        }
        if (result == -1) {
            return -1;
        }
        return b[0] & 0xFF;
    }
    
    protected final void onDataAvailable() throws IOException {
        this.ready = Boolean.TRUE;
        this.listener.onDataAvailable();
    }
    
    protected final boolean isCloseRequired() {
        return this.closeRequired;
    }
    
    protected abstract boolean doIsReady() throws IOException;
    
    protected abstract int doRead(final boolean p0, final byte[] p1, final int p2, final int p3) throws IOException;
    
    protected abstract void doClose() throws IOException;
    
    static {
        sm = StringManager.getManager("org.apache.coyote.http11.upgrade");
    }
}
