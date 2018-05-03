// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.filters;

import java.nio.charset.Charset;
import java.io.IOException;
import org.apache.coyote.Request;
import org.apache.coyote.InputBuffer;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.http11.InputFilter;

public class IdentityInputFilter implements InputFilter
{
    protected static final String ENCODING_NAME = "identity";
    protected static final ByteChunk ENCODING;
    protected long contentLength;
    protected long remaining;
    protected InputBuffer buffer;
    protected ByteChunk endChunk;
    
    public IdentityInputFilter() {
        this.contentLength = -1L;
        this.remaining = 0L;
        this.endChunk = new ByteChunk();
    }
    
    @Deprecated
    public long getContentLength() {
        return this.contentLength;
    }
    
    @Deprecated
    public long getRemaining() {
        return this.remaining;
    }
    
    @Override
    public int doRead(final ByteChunk chunk, final Request req) throws IOException {
        int result = -1;
        if (this.contentLength >= 0L) {
            if (this.remaining > 0L) {
                final int nRead = this.buffer.doRead(chunk, req);
                if (nRead > this.remaining) {
                    chunk.setBytes(chunk.getBytes(), chunk.getStart(), (int)this.remaining);
                    result = (int)this.remaining;
                }
                else {
                    result = nRead;
                }
                this.remaining -= nRead;
            }
            else {
                chunk.recycle();
                result = -1;
            }
        }
        return result;
    }
    
    @Override
    public void setRequest(final Request request) {
        this.contentLength = request.getContentLengthLong();
        this.remaining = this.contentLength;
    }
    
    @Override
    public long end() throws IOException {
        while (this.remaining > 0L) {
            final int nread = this.buffer.doRead(this.endChunk, null);
            if (nread > 0) {
                this.remaining -= nread;
            }
            else {
                this.remaining = 0L;
            }
        }
        return -this.remaining;
    }
    
    @Override
    public int available() {
        return 0;
    }
    
    @Override
    public void setBuffer(final InputBuffer buffer) {
        this.buffer = buffer;
    }
    
    @Override
    public void recycle() {
        this.contentLength = -1L;
        this.remaining = 0L;
        this.endChunk.recycle();
    }
    
    @Override
    public ByteChunk getEncodingName() {
        return IdentityInputFilter.ENCODING;
    }
    
    static {
        (ENCODING = new ByteChunk()).setBytes("identity".getBytes(Charset.defaultCharset()), 0, "identity".length());
    }
}
