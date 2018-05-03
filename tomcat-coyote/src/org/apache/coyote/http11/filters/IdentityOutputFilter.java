// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.filters;

import java.io.IOException;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.http11.OutputFilter;

public class IdentityOutputFilter implements OutputFilter
{
    protected long contentLength;
    protected long remaining;
    protected OutputBuffer buffer;
    
    public IdentityOutputFilter() {
        this.contentLength = -1L;
        this.remaining = 0L;
    }
    
    @Override
    public int doWrite(final ByteChunk chunk, final Response res) throws IOException {
        int result = -1;
        if (this.contentLength >= 0L) {
            if (this.remaining > 0L) {
                result = chunk.getLength();
                if (result > this.remaining) {
                    chunk.setBytes(chunk.getBytes(), chunk.getStart(), (int)this.remaining);
                    result = (int)this.remaining;
                    this.remaining = 0L;
                }
                else {
                    this.remaining -= result;
                }
                this.buffer.doWrite(chunk, res);
            }
            else {
                chunk.recycle();
                result = -1;
            }
        }
        else {
            this.buffer.doWrite(chunk, res);
            result = chunk.getLength();
        }
        return result;
    }
    
    @Override
    public long getBytesWritten() {
        return this.buffer.getBytesWritten();
    }
    
    @Override
    public void setResponse(final Response response) {
        this.contentLength = response.getContentLengthLong();
        this.remaining = this.contentLength;
    }
    
    @Override
    public void setBuffer(final OutputBuffer buffer) {
        this.buffer = buffer;
    }
    
    @Override
    public long end() throws IOException {
        if (this.remaining > 0L) {
            return this.remaining;
        }
        return 0L;
    }
    
    @Override
    public void recycle() {
        this.contentLength = -1L;
        this.remaining = 0L;
    }
}
