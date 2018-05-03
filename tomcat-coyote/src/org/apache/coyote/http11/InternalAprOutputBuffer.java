// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.ActionCode;
import java.io.IOException;
import org.apache.tomcat.jni.Socket;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.http.HttpMessages;
import org.apache.coyote.Response;
import java.nio.ByteBuffer;

public class InternalAprOutputBuffer extends AbstractOutputBuffer<Long>
{
    private long socket;
    private ByteBuffer bbuf;
    
    public InternalAprOutputBuffer(final Response response, final int headerBufferSize) {
        this.bbuf = null;
        this.response = response;
        this.buf = new byte[headerBufferSize];
        if (headerBufferSize < 8192) {
            this.bbuf = ByteBuffer.allocateDirect(9000);
        }
        else {
            this.bbuf = ByteBuffer.allocateDirect((headerBufferSize / 1500 + 1) * 1500);
        }
        this.outputStreamOutputBuffer = new SocketOutputBuffer();
        this.filterLibrary = new OutputFilter[0];
        this.activeFilters = new OutputFilter[0];
        this.lastActiveFilter = -1;
        this.committed = false;
        this.finished = false;
        HttpMessages.getInstance(response.getLocale()).getMessage(200);
    }
    
    @Override
    public void init(final SocketWrapper<Long> socketWrapper, final AbstractEndpoint endpoint) throws IOException {
        Socket.setsbb(this.socket = socketWrapper.getSocket(), this.bbuf);
    }
    
    @Override
    public void flush() throws IOException {
        super.flush();
        this.flushBuffer();
    }
    
    @Override
    public void recycle() {
        super.recycle();
        this.bbuf.clear();
    }
    
    @Override
    public void endRequest() throws IOException {
        if (!this.committed) {
            this.response.action(ActionCode.COMMIT, null);
        }
        if (this.finished) {
            return;
        }
        if (this.lastActiveFilter != -1) {
            this.activeFilters[this.lastActiveFilter].end();
        }
        this.flushBuffer();
        this.finished = true;
    }
    
    @Override
    public void sendAck() throws IOException {
        if (!this.committed && Socket.send(this.socket, Constants.ACK_BYTES, 0, Constants.ACK_BYTES.length) < 0) {
            throw new IOException(InternalAprOutputBuffer.sm.getString("iib.failedwrite"));
        }
    }
    
    @Override
    protected void commit() throws IOException {
        this.committed = true;
        this.response.setCommitted(true);
        if (this.pos > 0) {
            this.bbuf.put(this.buf, 0, this.pos);
        }
    }
    
    private void flushBuffer() throws IOException {
        if (this.bbuf.position() > 0) {
            if (Socket.sendbb(this.socket, 0, this.bbuf.position()) < 0) {
                throw new IOException();
            }
            this.bbuf.clear();
        }
    }
    
    protected class SocketOutputBuffer implements OutputBuffer
    {
        @Override
        public int doWrite(final ByteChunk chunk, final Response res) throws IOException {
            int len = chunk.getLength();
            int start = chunk.getStart();
            final byte[] b = chunk.getBuffer();
            while (len > 0) {
                int thisTime = len;
                if (InternalAprOutputBuffer.this.bbuf.position() == InternalAprOutputBuffer.this.bbuf.capacity()) {
                    InternalAprOutputBuffer.this.flushBuffer();
                }
                if (thisTime > InternalAprOutputBuffer.this.bbuf.capacity() - InternalAprOutputBuffer.this.bbuf.position()) {
                    thisTime = InternalAprOutputBuffer.this.bbuf.capacity() - InternalAprOutputBuffer.this.bbuf.position();
                }
                InternalAprOutputBuffer.this.bbuf.put(b, start, thisTime);
                len -= thisTime;
                start += thisTime;
            }
            final InternalAprOutputBuffer this$0 = InternalAprOutputBuffer.this;
            this$0.byteCount += chunk.getLength();
            return chunk.getLength();
        }
        
        @Override
        public long getBytesWritten() {
            return InternalAprOutputBuffer.this.byteCount;
        }
    }
}
