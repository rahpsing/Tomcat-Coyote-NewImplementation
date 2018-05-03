// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.OutputBuffer;
import java.nio.channels.SelectionKey;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import java.nio.channels.Selector;
import org.apache.tomcat.util.net.NioEndpoint;
import java.nio.ByteBuffer;
import java.io.IOException;
import org.apache.tomcat.util.http.HttpMessages;
import org.apache.coyote.Response;
import org.apache.tomcat.util.net.NioSelectorPool;
import org.apache.tomcat.util.net.NioChannel;

public class InternalNioOutputBuffer extends AbstractOutputBuffer<NioChannel>
{
    private NioChannel socket;
    private NioSelectorPool pool;
    
    public InternalNioOutputBuffer(final Response response, final int headerBufferSize) {
        this.response = response;
        this.buf = new byte[headerBufferSize];
        this.outputStreamOutputBuffer = new SocketOutputBuffer();
        this.filterLibrary = new OutputFilter[0];
        this.activeFilters = new OutputFilter[0];
        this.lastActiveFilter = -1;
        this.committed = false;
        this.finished = false;
        HttpMessages.getInstance(response.getLocale()).getMessage(200);
    }
    
    @Override
    public void flush() throws IOException {
        super.flush();
        this.flushBuffer();
    }
    
    @Override
    public void recycle() {
        super.recycle();
        if (this.socket != null) {
            this.socket.getBufHandler().getWriteBuffer().clear();
            this.socket = null;
        }
    }
    
    @Override
    public void endRequest() throws IOException {
        super.endRequest();
        this.flushBuffer();
    }
    
    @Override
    public void sendAck() throws IOException {
        if (!this.committed) {
            this.socket.getBufHandler().getWriteBuffer().put(Constants.ACK_BYTES, 0, Constants.ACK_BYTES.length);
            this.writeToSocket(this.socket.getBufHandler().getWriteBuffer(), true, true);
        }
    }
    
    private synchronized int writeToSocket(final ByteBuffer bytebuffer, final boolean block, final boolean flip) throws IOException {
        if (flip) {
            bytebuffer.flip();
        }
        int written = 0;
        final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)this.socket.getAttachment(false);
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
            written = this.pool.write(bytebuffer, this.socket, selector, writeTimeout, block);
            while (!this.socket.flush(true, selector, writeTimeout)) {}
        }
        finally {
            if (selector != null) {
                this.pool.put(selector);
            }
        }
        if (block) {
            bytebuffer.clear();
        }
        return written;
    }
    
    @Override
    public void init(final SocketWrapper<NioChannel> socketWrapper, final AbstractEndpoint endpoint) throws IOException {
        this.socket = socketWrapper.getSocket();
        this.pool = ((NioEndpoint)endpoint).getSelectorPool();
    }
    
    @Override
    protected void commit() throws IOException {
        this.committed = true;
        this.response.setCommitted(true);
        if (this.pos > 0) {
            this.addToBB(this.buf, 0, this.pos);
        }
    }
    
    private synchronized void addToBB(final byte[] buf, int offset, int length) throws IOException {
        while (length > 0) {
            int thisTime = length;
            if (this.socket.getBufHandler().getWriteBuffer().position() == this.socket.getBufHandler().getWriteBuffer().capacity() || this.socket.getBufHandler().getWriteBuffer().remaining() == 0) {
                this.flushBuffer();
            }
            if (thisTime > this.socket.getBufHandler().getWriteBuffer().remaining()) {
                thisTime = this.socket.getBufHandler().getWriteBuffer().remaining();
            }
            this.socket.getBufHandler().getWriteBuffer().put(buf, offset, thisTime);
            length -= thisTime;
            offset += thisTime;
        }
        final NioEndpoint.KeyAttachment ka = (NioEndpoint.KeyAttachment)this.socket.getAttachment(false);
        if (ka != null) {
            ka.access();
        }
    }
    
    private void flushBuffer() throws IOException {
        final SelectionKey key = this.socket.getIOChannel().keyFor(this.socket.getPoller().getSelector());
        if (key != null) {
            final NioEndpoint.KeyAttachment attach = (NioEndpoint.KeyAttachment)key.attachment();
            attach.access();
        }
        if (this.socket.getBufHandler().getWriteBuffer().position() > 0) {
            this.socket.getBufHandler().getWriteBuffer().flip();
            this.writeToSocket(this.socket.getBufHandler().getWriteBuffer(), true, false);
        }
    }
    
    protected class SocketOutputBuffer implements OutputBuffer
    {
        @Override
        public int doWrite(final ByteChunk chunk, final Response res) throws IOException {
            final int len = chunk.getLength();
            final int start = chunk.getStart();
            final byte[] b = chunk.getBuffer();
            InternalNioOutputBuffer.this.addToBB(b, start, len);
            final InternalNioOutputBuffer this$0 = InternalNioOutputBuffer.this;
            this$0.byteCount += chunk.getLength();
            return chunk.getLength();
        }
        
        @Override
        public long getBytesWritten() {
            return InternalNioOutputBuffer.this.byteCount;
        }
    }
}
