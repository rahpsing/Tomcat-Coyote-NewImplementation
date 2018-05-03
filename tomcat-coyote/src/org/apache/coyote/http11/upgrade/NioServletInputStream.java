// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.nio.channels.Selector;
import org.apache.tomcat.util.net.NioEndpoint;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.net.NioSelectorPool;
import org.apache.tomcat.util.net.NioChannel;

public class NioServletInputStream extends AbstractServletInputStream
{
    private final NioChannel channel;
    private final NioSelectorPool pool;
    
    public NioServletInputStream(final SocketWrapper<NioChannel> wrapper, final NioSelectorPool pool) {
        this.channel = wrapper.getSocket();
        this.pool = pool;
    }
    
    @Override
    protected boolean doIsReady() throws IOException {
        final ByteBuffer readBuffer = this.channel.getBufHandler().getReadBuffer();
        if (readBuffer.remaining() > 0) {
            return true;
        }
        readBuffer.clear();
        this.fillReadBuffer(false);
        final boolean isReady = readBuffer.position() > 0;
        readBuffer.flip();
        return isReady;
    }
    
    @Override
    protected int doRead(final boolean block, final byte[] b, final int off, final int len) throws IOException {
        final ByteBuffer readBuffer = this.channel.getBufHandler().getReadBuffer();
        final int remaining = readBuffer.remaining();
        if (remaining >= len) {
            readBuffer.get(b, off, len);
            return len;
        }
        int leftToWrite = len;
        int newOffset = off;
        if (remaining > 0) {
            readBuffer.get(b, off, remaining);
            leftToWrite -= remaining;
            newOffset += remaining;
        }
        readBuffer.clear();
        final int nRead = this.fillReadBuffer(block);
        if (nRead > 0) {
            readBuffer.flip();
            if (nRead > leftToWrite) {
                readBuffer.get(b, newOffset, leftToWrite);
                leftToWrite = 0;
            }
            else {
                readBuffer.get(b, newOffset, nRead);
                leftToWrite -= nRead;
            }
        }
        else if (nRead == 0) {
            readBuffer.flip();
        }
        else if (nRead == -1) {
            throw new EOFException();
        }
        return len - leftToWrite;
    }
    
    @Override
    protected void doClose() throws IOException {
        this.channel.close();
    }
    
    private int fillReadBuffer(final boolean block) throws IOException {
        int nRead;
        if (block) {
            Selector selector = null;
            try {
                selector = this.pool.get();
            }
            catch (IOException ex) {}
            try {
                final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)this.channel.getAttachment(false);
                if (att == null) {
                    throw new IOException("Key must be cancelled.");
                }
                nRead = this.pool.read(this.channel.getBufHandler().getReadBuffer(), this.channel, selector, att.getTimeout());
            }
            catch (EOFException eof) {
                nRead = -1;
            }
            finally {
                if (selector != null) {
                    this.pool.put(selector);
                }
            }
        }
        else {
            nRead = this.channel.read(this.channel.getBufHandler().getReadBuffer());
        }
        return nRead;
    }
}
