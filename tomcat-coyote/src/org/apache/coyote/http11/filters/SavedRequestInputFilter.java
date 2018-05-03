// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.filters;

import org.apache.coyote.InputBuffer;
import java.io.IOException;
import org.apache.coyote.Request;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.http11.InputFilter;

public class SavedRequestInputFilter implements InputFilter
{
    protected ByteChunk input;
    
    public SavedRequestInputFilter(final ByteChunk input) {
        this.input = null;
        this.input = input;
    }
    
    @Override
    public int doRead(final ByteChunk chunk, final Request request) throws IOException {
        int writeLength = 0;
        if (chunk.getLimit() > 0 && chunk.getLimit() < this.input.getLength()) {
            writeLength = chunk.getLimit();
        }
        else {
            writeLength = this.input.getLength();
        }
        if (this.input.getOffset() >= this.input.getEnd()) {
            return -1;
        }
        this.input.substract(chunk.getBuffer(), 0, writeLength);
        chunk.setOffset(0);
        chunk.setEnd(writeLength);
        return writeLength;
    }
    
    @Override
    public void setRequest(final Request request) {
        request.setContentLength(this.input.getLength());
    }
    
    @Override
    public void recycle() {
        this.input = null;
    }
    
    @Override
    public ByteChunk getEncodingName() {
        return null;
    }
    
    @Override
    public void setBuffer(final InputBuffer buffer) {
    }
    
    @Override
    public int available() {
        return this.input.getLength();
    }
    
    @Override
    public long end() throws IOException {
        return 0L;
    }
}
