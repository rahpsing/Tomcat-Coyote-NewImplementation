// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class NioChannel implements ByteChannel
{
    protected static ByteBuffer emptyBuf;
    protected SocketChannel sc;
    protected SecureNioChannel.ApplicationBufferHandler bufHandler;
    protected NioEndpoint.Poller poller;
    protected boolean sendFile;
    
    public NioChannel(final SocketChannel channel, final SecureNioChannel.ApplicationBufferHandler bufHandler) throws IOException {
        this.sc = null;
        this.sendFile = false;
        this.sc = channel;
        this.bufHandler = bufHandler;
    }
    
    public void reset() throws IOException {
        this.bufHandler.getReadBuffer().clear();
        this.bufHandler.getWriteBuffer().clear();
        this.sendFile = false;
    }
    
    public int getBufferSize() {
        if (this.bufHandler == null) {
            return 0;
        }
        int size = 0;
        size += ((this.bufHandler.getReadBuffer() != null) ? this.bufHandler.getReadBuffer().capacity() : 0);
        size += ((this.bufHandler.getWriteBuffer() != null) ? this.bufHandler.getWriteBuffer().capacity() : 0);
        return size;
    }
    
    public boolean flush(final boolean block, final Selector s, final long timeout) throws IOException {
        return true;
    }
    
    @Override
    public void close() throws IOException {
        this.getIOChannel().socket().close();
        this.getIOChannel().close();
    }
    
    public void close(final boolean force) throws IOException {
        if (this.isOpen() || force) {
            this.close();
        }
    }
    
    @Override
    public boolean isOpen() {
        return this.sc.isOpen();
    }
    
    @Override
    public int write(final ByteBuffer src) throws IOException {
        return this.sc.write(src);
    }
    
    @Override
    public int read(final ByteBuffer dst) throws IOException {
        return this.sc.read(dst);
    }
    
    public Object getAttachment(final boolean remove) {
        final NioEndpoint.Poller pol = this.getPoller();
        final Selector sel = (pol != null) ? pol.getSelector() : null;
        final SelectionKey key = (sel != null) ? this.getIOChannel().keyFor(sel) : null;
        final Object att = (key != null) ? key.attachment() : null;
        if (key != null && att != null && remove) {
            key.attach(null);
        }
        return att;
    }
    
    public SecureNioChannel.ApplicationBufferHandler getBufHandler() {
        return this.bufHandler;
    }
    
    public NioEndpoint.Poller getPoller() {
        return this.poller;
    }
    
    public SocketChannel getIOChannel() {
        return this.sc;
    }
    
    public boolean isClosing() {
        return false;
    }
    
    public boolean isHandshakeComplete() {
        return true;
    }
    
    public int handshake(final boolean read, final boolean write) throws IOException {
        return 0;
    }
    
    public void setPoller(final NioEndpoint.Poller poller) {
        this.poller = poller;
    }
    
    public void setIOChannel(final SocketChannel IOChannel) {
        this.sc = IOChannel;
    }
    
    @Override
    public String toString() {
        return super.toString() + ":" + this.sc.toString();
    }
    
    public int getOutboundRemaining() {
        return 0;
    }
    
    public boolean flushOutbound() throws IOException {
        return false;
    }
    
    public boolean isSendFile() {
        return this.sendFile;
    }
    
    public void setSendFile(final boolean s) {
        this.sendFile = s;
    }
    
    static {
        NioChannel.emptyBuf = ByteBuffer.allocate(0);
    }
}
