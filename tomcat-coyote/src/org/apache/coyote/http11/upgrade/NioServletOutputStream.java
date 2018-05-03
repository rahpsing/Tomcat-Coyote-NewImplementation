// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.nio.channels.Selector;
import org.apache.tomcat.util.net.NioEndpoint;
import java.io.IOException;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.net.NioSelectorPool;
import org.apache.tomcat.util.net.NioChannel;

public class NioServletOutputStream extends AbstractServletOutputStream
{
    private final NioChannel channel;
    private final NioSelectorPool pool;
    private final int maxWrite;
    
    public NioServletOutputStream(final SocketWrapper<NioChannel> wrapper, final NioSelectorPool pool) {
        this.channel = wrapper.getSocket();
        this.pool = pool;
        this.maxWrite = this.channel.getBufHandler().getWriteBuffer().capacity();
    }
    
    @Override
    protected int doWrite(final boolean block, final byte[] b, final int off, final int len) throws IOException {
        int leftToWrite = len;
        int count = 0;
        int offset = off;
        while (leftToWrite > 0) {
            int writeThisLoop;
            if (leftToWrite > this.maxWrite) {
                writeThisLoop = this.maxWrite;
            }
            else {
                writeThisLoop = leftToWrite;
            }
            final int writtenThisLoop = this.doWriteInternal(block, b, offset, writeThisLoop);
            count += writtenThisLoop;
            offset += writtenThisLoop;
            leftToWrite -= writtenThisLoop;
            if (writtenThisLoop < writeThisLoop) {
                break;
            }
        }
        return count;
    }
    
    private int doWriteInternal(final boolean block, final byte[] b, final int off, final int len) throws IOException {
        this.channel.getBufHandler().getWriteBuffer().clear();
        this.channel.getBufHandler().getWriteBuffer().put(b, off, len);
        this.channel.getBufHandler().getWriteBuffer().flip();
        int written = 0;
        final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)this.channel.getAttachment(false);
        if (att == null) {
            throw new IOException("Key must be cancelled");
        }
        final long writeTimeout = att.getWriteTimeout();
        Selector selector = null;
        try {
            selector = this.pool.get();
        }
        catch (IOException ex) {}
        try {
            written = this.pool.write(this.channel.getBufHandler().getWriteBuffer(), this.channel, selector, writeTimeout, block);
        }
        finally {
            if (selector != null) {
                this.pool.put(selector);
            }
        }
        if (written < len) {
            this.channel.getPoller().add(this.channel, 4);
        }
        return written;
    }
    
    @Override
    protected void doFlush() throws IOException {
        final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)this.channel.getAttachment(false);
        if (att == null) {
            throw new IOException("Key must be cancelled");
        }
        final long writeTimeout = att.getWriteTimeout();
        Selector selector = null;
        try {
            selector = this.pool.get();
        }
        catch (IOException ex) {}
        try {
            while (!this.channel.flush(true, selector, writeTimeout)) {}
        }
        finally {
            if (selector != null) {
                this.pool.put(selector);
            }
        }
    }
    
    @Override
    protected void doClose() throws IOException {
        this.channel.close();
    }
}
