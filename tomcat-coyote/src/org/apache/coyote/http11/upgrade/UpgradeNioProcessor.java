// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.nio.ByteBuffer;
import java.io.EOFException;
import java.nio.channels.Selector;
import java.io.IOException;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.net.NioSelectorPool;
import org.apache.tomcat.util.net.NioChannel;

@Deprecated
public class UpgradeNioProcessor extends UpgradeProcessor<NioChannel>
{
    private final NioChannel nioChannel;
    private final NioSelectorPool pool;
    private final int maxRead;
    private final int maxWrite;
    
    public UpgradeNioProcessor(final SocketWrapper<NioChannel> wrapper, final UpgradeInbound upgradeInbound, final NioSelectorPool pool) {
        super(upgradeInbound);
        wrapper.setTimeout(upgradeInbound.getReadTimeout());
        this.nioChannel = wrapper.getSocket();
        this.pool = pool;
        this.maxRead = this.nioChannel.getBufHandler().getReadBuffer().capacity();
        this.maxWrite = this.nioChannel.getBufHandler().getWriteBuffer().capacity();
    }
    
    @Override
    public void flush() throws IOException {
        final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)this.nioChannel.getAttachment(false);
        if (att == null) {
            throw new IOException("Key must be cancelled");
        }
        final long writeTimeout = att.getTimeout();
        Selector selector = null;
        try {
            selector = this.pool.get();
        }
        catch (IOException ex) {}
        try {
            while (!this.nioChannel.flush(true, selector, writeTimeout)) {}
        }
        finally {
            if (selector != null) {
                this.pool.put(selector);
            }
        }
    }
    
    @Override
    public void write(final int b) throws IOException {
        this.writeToSocket(new byte[] { (byte)b }, 0, 1);
    }
    
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        int written;
        for (written = 0; len - written > this.maxWrite; written += this.writeToSocket(b, off + written, this.maxWrite)) {}
        this.writeToSocket(b, off + written, len - written);
    }
    
    @Override
    public int read() throws IOException {
        final byte[] bytes = { 0 };
        final int result = this.readSocket(true, bytes, 0, 1);
        if (result == -1) {
            return -1;
        }
        return bytes[0] & 0xFF;
    }
    
    @Override
    public int read(final boolean block, final byte[] bytes, final int off, final int len) throws IOException {
        if (len > this.maxRead) {
            return this.readSocket(block, bytes, off, this.maxRead);
        }
        return this.readSocket(block, bytes, off, len);
    }
    
    private int readSocket(final boolean block, final byte[] bytes, final int offset, final int len) throws IOException {
        final ByteBuffer readBuffer = this.nioChannel.getBufHandler().getReadBuffer();
        final int remaining = readBuffer.remaining();
        if (remaining >= len) {
            readBuffer.get(bytes, offset, len);
            return len;
        }
        int leftToWrite = len;
        int newOffset = offset;
        if (remaining > 0) {
            readBuffer.get(bytes, offset, remaining);
            leftToWrite -= remaining;
            newOffset += remaining;
        }
        readBuffer.clear();
        final int nRead = this.fillReadBuffer(block);
        if (nRead > 0) {
            readBuffer.flip();
            readBuffer.limit(nRead);
            if (nRead > leftToWrite) {
                readBuffer.get(bytes, newOffset, leftToWrite);
                leftToWrite = 0;
            }
            else {
                readBuffer.get(bytes, newOffset, nRead);
                leftToWrite -= nRead;
            }
        }
        else if (nRead == 0) {
            readBuffer.flip();
            readBuffer.limit(nRead);
        }
        else if (nRead == -1) {
            throw new EOFException(UpgradeNioProcessor.sm.getString("nio.eof.error"));
        }
        return len - leftToWrite;
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
                final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)this.nioChannel.getAttachment(false);
                if (att == null) {
                    throw new IOException("Key must be cancelled.");
                }
                nRead = this.pool.read(this.nioChannel.getBufHandler().getReadBuffer(), this.nioChannel, selector, att.getTimeout());
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
            nRead = this.nioChannel.read(this.nioChannel.getBufHandler().getReadBuffer());
        }
        return nRead;
    }
    
    private synchronized int writeToSocket(final byte[] bytes, final int off, final int len) throws IOException {
        this.nioChannel.getBufHandler().getWriteBuffer().clear();
        this.nioChannel.getBufHandler().getWriteBuffer().put(bytes, off, len);
        this.nioChannel.getBufHandler().getWriteBuffer().flip();
        int written = 0;
        final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)this.nioChannel.getAttachment(false);
        if (att == null) {
            throw new IOException("Key must be cancelled");
        }
        final long writeTimeout = att.getTimeout();
        Selector selector = null;
        try {
            selector = this.pool.get();
        }
        catch (IOException ex) {}
        try {
            written = this.pool.write(this.nioChannel.getBufHandler().getWriteBuffer(), this.nioChannel, selector, writeTimeout, true);
        }
        finally {
            if (selector != null) {
                this.pool.put(selector);
            }
        }
        return written;
    }
}
