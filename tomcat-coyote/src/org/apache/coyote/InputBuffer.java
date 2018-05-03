// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import java.io.IOException;
import org.apache.tomcat.util.buf.ByteChunk;

public interface InputBuffer
{
    int doRead(final ByteChunk p0, final Request p1) throws IOException;
}
