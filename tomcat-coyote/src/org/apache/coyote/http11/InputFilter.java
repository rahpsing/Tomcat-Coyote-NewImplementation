// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import java.io.IOException;
import org.apache.coyote.Request;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.InputBuffer;

public interface InputFilter extends InputBuffer
{
    int doRead(final ByteChunk p0, final Request p1) throws IOException;
    
    void setRequest(final Request p0);
    
    void recycle();
    
    ByteChunk getEncodingName();
    
    void setBuffer(final InputBuffer p0);
    
    long end() throws IOException;
    
    int available();
}
