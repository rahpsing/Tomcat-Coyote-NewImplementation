// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.filters;

import org.apache.coyote.OutputBuffer;
import java.io.IOException;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.http11.OutputFilter;

public class VoidOutputFilter implements OutputFilter
{
    @Override
    public int doWrite(final ByteChunk chunk, final Response res) throws IOException {
        return chunk.getLength();
    }
    
    @Override
    public long getBytesWritten() {
        return 0L;
    }
    
    @Override
    public void setResponse(final Response response) {
    }
    
    @Override
    public void setBuffer(final OutputBuffer buffer) {
    }
    
    @Override
    public void recycle() {
    }
    
    @Override
    public long end() throws IOException {
        return 0L;
    }
}
