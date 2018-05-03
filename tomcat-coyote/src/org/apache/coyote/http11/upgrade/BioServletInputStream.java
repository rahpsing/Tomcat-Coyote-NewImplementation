// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.io.IOException;
import java.net.Socket;
import org.apache.tomcat.util.net.SocketWrapper;
import java.io.InputStream;

public class BioServletInputStream extends AbstractServletInputStream
{
    private final InputStream inputStream;
    
    public BioServletInputStream(final SocketWrapper<Socket> wrapper) throws IOException {
        this.inputStream = wrapper.getSocket().getInputStream();
    }
    
    @Override
    protected int doRead(final boolean block, final byte[] b, final int off, final int len) throws IOException {
        return this.inputStream.read(b, off, len);
    }
    
    @Override
    protected boolean doIsReady() {
        return true;
    }
    
    @Override
    protected void doClose() throws IOException {
        this.inputStream.close();
    }
}
