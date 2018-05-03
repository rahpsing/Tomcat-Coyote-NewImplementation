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

public class BufferedInputFilter implements InputFilter
{
    private static final String ENCODING_NAME = "buffered";
    private static final ByteChunk ENCODING;
    private ByteChunk buffered;
    private ByteChunk tempRead;
    private InputBuffer buffer;
    private boolean hasRead;
    
    public BufferedInputFilter() {
        this.buffered = null;
        this.tempRead = new ByteChunk(1024);
        this.hasRead = false;
    }
    
    public void setLimit(final int limit) {
        if (this.buffered == null) {
            (this.buffered = new ByteChunk(4048)).setLimit(limit);
        }
    }
    
    @Override
    public void setRequest(final Request request) {
        try {
            while (this.buffer.doRead(this.tempRead, request) >= 0) {
                this.buffered.append(this.tempRead);
                this.tempRead.recycle();
            }
        }
        catch (IOException ioe) {
            throw new IllegalStateException("Request body too large for buffer");
        }
    }
    
    @Override
    public int doRead(final ByteChunk chunk, final Request request) throws IOException {
        if (this.hasRead || this.buffered.getLength() <= 0) {
            return -1;
        }
        chunk.setBytes(this.buffered.getBytes(), this.buffered.getStart(), this.buffered.getLength());
        this.hasRead = true;
        return chunk.getLength();
    }
    
    @Override
    public void setBuffer(final InputBuffer buffer) {
        this.buffer = buffer;
    }
    
    @Override
    public void recycle() {
        if (this.buffered != null) {
            if (this.buffered.getBuffer().length > 65536) {
                this.buffered = null;
            }
            else {
                this.buffered.recycle();
            }
        }
        this.tempRead.recycle();
        this.hasRead = false;
        this.buffer = null;
    }
    
    @Override
    public ByteChunk getEncodingName() {
        return BufferedInputFilter.ENCODING;
    }
    
    @Override
    public long end() throws IOException {
        return 0L;
    }
    
    @Override
    public int available() {
        return this.buffered.getLength();
    }
    
    static {
        (ENCODING = new ByteChunk()).setBytes("buffered".getBytes(Charset.defaultCharset()), 0, "buffered".length());
    }
}
