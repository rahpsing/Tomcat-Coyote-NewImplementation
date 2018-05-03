// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.filters;

import java.nio.charset.Charset;
import org.apache.coyote.InputBuffer;
import java.io.IOException;
import org.apache.coyote.Request;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.http11.InputFilter;

public class VoidInputFilter implements InputFilter
{
    protected static final String ENCODING_NAME = "void";
    protected static final ByteChunk ENCODING;
    
    @Override
    public int doRead(final ByteChunk chunk, final Request req) throws IOException {
        return -1;
    }
    
    @Override
    public void setRequest(final Request request) {
    }
    
    @Override
    public void setBuffer(final InputBuffer buffer) {
    }
    
    @Override
    public void recycle() {
    }
    
    @Override
    public ByteChunk getEncodingName() {
        return VoidInputFilter.ENCODING;
    }
    
    @Override
    public long end() throws IOException {
        return 0L;
    }
    
    @Override
    public int available() {
        return 0;
    }
    
    static {
        (ENCODING = new ByteChunk()).setBytes("void".getBytes(Charset.defaultCharset()), 0, "void".length());
    }
}
