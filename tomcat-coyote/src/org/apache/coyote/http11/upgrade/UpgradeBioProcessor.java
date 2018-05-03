// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.io.IOException;
import org.apache.tomcat.util.net.SocketWrapper;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;

@Deprecated
public class UpgradeBioProcessor extends UpgradeProcessor<Socket>
{
    private final InputStream inputStream;
    private final OutputStream outputStream;
    
    public UpgradeBioProcessor(final SocketWrapper<Socket> wrapper, final UpgradeInbound upgradeInbound) throws IOException {
        super(upgradeInbound);
        int timeout = upgradeInbound.getReadTimeout();
        if (timeout < 0) {
            timeout = 0;
        }
        wrapper.getSocket().setSoTimeout(timeout);
        this.inputStream = wrapper.getSocket().getInputStream();
        this.outputStream = wrapper.getSocket().getOutputStream();
    }
    
    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
    }
    
    @Override
    public void write(final int b) throws IOException {
        this.outputStream.write(b);
    }
    
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.outputStream.write(b, off, len);
    }
    
    @Override
    public int read() throws IOException {
        return this.inputStream.read();
    }
    
    @Override
    public int read(final boolean block, final byte[] bytes, final int off, final int len) throws IOException {
        return this.inputStream.read(bytes, off, len);
    }
}
