// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.io.IOException;
import java.net.Socket;
import org.apache.tomcat.util.net.SocketWrapper;
import java.io.OutputStream;

public class BioServletOutputStream extends AbstractServletOutputStream
{
    private final OutputStream os;
    
    public BioServletOutputStream(final SocketWrapper<Socket> wrapper) throws IOException {
        this.os = wrapper.getSocket().getOutputStream();
    }
    
    @Override
    protected int doWrite(final boolean block, final byte[] b, final int off, final int len) throws IOException {
        this.os.write(b, off, len);
        return len;
    }
    
    @Override
    protected void doFlush() throws IOException {
        this.os.flush();
    }
    
    @Override
    protected void doClose() throws IOException {
        this.os.close();
    }
}
