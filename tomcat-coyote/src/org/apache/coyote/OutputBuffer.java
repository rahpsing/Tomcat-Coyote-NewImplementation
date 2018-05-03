// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import java.io.IOException;
import org.apache.tomcat.util.buf.ByteChunk;

public interface OutputBuffer
{
    int doWrite(final ByteChunk p0, final Response p1) throws IOException;
    
    long getBytesWritten();
}
