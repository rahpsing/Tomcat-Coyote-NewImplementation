// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import java.io.IOException;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.OutputBuffer;

public interface OutputFilter extends OutputBuffer
{
    int doWrite(final ByteChunk p0, final Response p1) throws IOException;
    
    void setResponse(final Response p0);
    
    void recycle();
    
    void setBuffer(final OutputBuffer p0);
    
    long end() throws IOException;
}
