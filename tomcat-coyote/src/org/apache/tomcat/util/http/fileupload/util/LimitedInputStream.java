// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;

public abstract class LimitedInputStream extends FilterInputStream implements Closeable
{
    private final long sizeMax;
    private long count;
    private boolean closed;
    
    public LimitedInputStream(final InputStream pIn, final long pSizeMax) {
        super(pIn);
        this.sizeMax = pSizeMax;
    }
    
    protected abstract void raiseError(final long p0, final long p1) throws IOException;
    
    private void checkLimit() throws IOException {
        if (this.count > this.sizeMax) {
            this.raiseError(this.sizeMax, this.count);
        }
    }
    
    @Override
    public int read() throws IOException {
        final int res = super.read();
        if (res != -1) {
            ++this.count;
            this.checkLimit();
        }
        return res;
    }
    
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int res = super.read(b, off, len);
        if (res > 0) {
            this.count += res;
            this.checkLimit();
        }
        return res;
    }
    
    @Override
    public boolean isClosed() throws IOException {
        return this.closed;
    }
    
    @Override
    public void close() throws IOException {
        this.closed = true;
        super.close();
    }
}
