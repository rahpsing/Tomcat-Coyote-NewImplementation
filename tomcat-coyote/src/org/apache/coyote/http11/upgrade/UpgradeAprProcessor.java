// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.io.IOException;
import org.apache.tomcat.jni.Socket;
import org.apache.tomcat.util.net.SocketWrapper;

@Deprecated
public class UpgradeAprProcessor extends UpgradeProcessor<Long>
{
    private final long socket;
    
    public UpgradeAprProcessor(final SocketWrapper<Long> wrapper, final UpgradeInbound upgradeInbound) {
        super(upgradeInbound);
        Socket.timeoutSet(wrapper.getSocket(), upgradeInbound.getReadTimeout());
        this.socket = wrapper.getSocket();
    }
    
    @Override
    public void flush() throws IOException {
    }
    
    @Override
    public void write(final int b) throws IOException {
        final int result = Socket.send(this.socket, new byte[] { (byte)b }, 0, 1);
        if (result != 1) {
            throw new IOException(UpgradeAprProcessor.sm.getString("apr.write.error", new Object[] { -result }));
        }
    }
    
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        final int result = Socket.send(this.socket, b, off, len);
        if (result != len) {
            throw new IOException(UpgradeAprProcessor.sm.getString("apr.write.error", new Object[] { -result }));
        }
    }
    
    @Override
    public int read() throws IOException {
        final byte[] bytes = { 0 };
        final int result = Socket.recv(this.socket, bytes, 0, 1);
        if (result == -1) {
            return -1;
        }
        return bytes[0] & 0xFF;
    }
    
    @Override
    public int read(final boolean block, final byte[] bytes, final int off, final int len) throws IOException {
        if (!block) {
            Socket.optSet(this.socket, 8, -1);
        }
        try {
            final int result = Socket.recv(this.socket, bytes, off, len);
            if (result > 0) {
                return result;
            }
            if (-result == 120002) {
                return 0;
            }
            throw new IOException(UpgradeAprProcessor.sm.getString("apr.read.error", new Object[] { -result }));
        }
        finally {
            if (!block) {
                Socket.optSet(this.socket, 8, 0);
            }
        }
    }
}
