// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.filters;

import java.io.IOException;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.coyote.Response;
import org.apache.coyote.OutputBuffer;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.http11.OutputFilter;

public class ChunkedOutputFilter implements OutputFilter
{
    protected static final ByteChunk END_CHUNK;
    protected OutputBuffer buffer;
    protected byte[] chunkLength;
    protected ByteChunk chunkHeader;
    
    public ChunkedOutputFilter() {
        this.chunkLength = new byte[10];
        this.chunkHeader = new ByteChunk();
        (this.chunkLength = new byte[10])[8] = 13;
        this.chunkLength[9] = 10;
    }
    
    @Override
    public int doWrite(final ByteChunk chunk, final Response res) throws IOException {
        final int result = chunk.getLength();
        if (result <= 0) {
            return 0;
        }
        int pos = 7;
        int digit;
        for (int current = result; current > 0; current /= 16, this.chunkLength[pos--] = HexUtils.getHex(digit)) {
            digit = current % 16;
        }
        this.chunkHeader.setBytes(this.chunkLength, pos + 1, 9 - pos);
        this.buffer.doWrite(this.chunkHeader, res);
        this.buffer.doWrite(chunk, res);
        this.chunkHeader.setBytes(this.chunkLength, 8, 2);
        this.buffer.doWrite(this.chunkHeader, res);
        return result;
    }
    
    @Override
    public long getBytesWritten() {
        return this.buffer.getBytesWritten();
    }
    
    @Override
    public void setResponse(final Response response) {
    }
    
    @Override
    public void setBuffer(final OutputBuffer buffer) {
        this.buffer = buffer;
    }
    
    @Override
    public long end() throws IOException {
        this.buffer.doWrite(ChunkedOutputFilter.END_CHUNK, null);
        return 0L;
    }
    
    @Override
    public void recycle() {
    }
    
    static {
        END_CHUNK = new ByteChunk();
        final byte[] END_CHUNK_BYTES = { 48, 13, 10, 13, 10 };
        ChunkedOutputFilter.END_CHUNK.setBytes(END_CHUNK_BYTES, 0, END_CHUNK_BYTES.length);
    }
}
