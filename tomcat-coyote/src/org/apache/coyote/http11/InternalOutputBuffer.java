// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.coyote.OutputBuffer;
import java.io.IOException;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.coyote.Response;
import java.io.OutputStream;
import org.apache.tomcat.util.buf.ByteChunk;
import java.net.Socket;

public class InternalOutputBuffer extends AbstractOutputBuffer<Socket> implements ByteChunk.ByteOutputChannel
{
    protected OutputStream outputStream;
    private ByteChunk socketBuffer;
    private boolean useSocketBuffer;
    
    public InternalOutputBuffer(final Response response, final int headerBufferSize) {
        this.useSocketBuffer = false;
        this.response = response;
        this.buf = new byte[headerBufferSize];
        this.outputStreamOutputBuffer = new OutputStreamOutputBuffer();
        this.filterLibrary = new OutputFilter[0];
        this.activeFilters = new OutputFilter[0];
        this.lastActiveFilter = -1;
        (this.socketBuffer = new ByteChunk()).setByteOutputChannel(this);
        this.committed = false;
        this.finished = false;
    }
    
    public void setSocketBuffer(final int socketBufferSize) {
        if (socketBufferSize > 500) {
            this.useSocketBuffer = true;
            this.socketBuffer.allocate(socketBufferSize, socketBufferSize);
        }
        else {
            this.useSocketBuffer = false;
        }
    }
    
    @Override
    public void init(final SocketWrapper<Socket> socketWrapper, final AbstractEndpoint endpoint) throws IOException {
        this.outputStream = socketWrapper.getSocket().getOutputStream();
    }
    
    @Override
    public void flush() throws IOException {
        super.flush();
        if (this.useSocketBuffer) {
            this.socketBuffer.flushBuffer();
        }
    }
    
    @Override
    public void recycle() {
        super.recycle();
        this.outputStream = null;
    }
    
    @Override
    public void nextRequest() {
        super.nextRequest();
        this.socketBuffer.recycle();
    }
    
    @Override
    public void endRequest() throws IOException {
        super.endRequest();
        if (this.useSocketBuffer) {
            this.socketBuffer.flushBuffer();
        }
    }
    
    @Override
    public void sendAck() throws IOException {
        if (!this.committed) {
            this.outputStream.write(Constants.ACK_BYTES);
        }
    }
    
    @Override
    protected void commit() throws IOException {
        this.committed = true;
        this.response.setCommitted(true);
        if (this.pos > 0) {
            if (this.useSocketBuffer) {
                this.socketBuffer.append(this.buf, 0, this.pos);
            }
            else {
                this.outputStream.write(this.buf, 0, this.pos);
            }
        }
    }
    
    @Override
    public void realWriteBytes(final byte[] cbuf, final int off, final int len) throws IOException {
        if (len > 0) {
            this.outputStream.write(cbuf, off, len);
        }
    }
    
    protected class OutputStreamOutputBuffer implements OutputBuffer
    {
        @Override
        public int doWrite(final ByteChunk chunk, final Response res) throws IOException {
            final int length = chunk.getLength();
            if (InternalOutputBuffer.this.useSocketBuffer) {
                InternalOutputBuffer.this.socketBuffer.append(chunk.getBuffer(), chunk.getStart(), length);
            }
            else {
                InternalOutputBuffer.this.outputStream.write(chunk.getBuffer(), chunk.getStart(), length);
            }
            final InternalOutputBuffer this$0 = InternalOutputBuffer.this;
            this$0.byteCount += chunk.getLength();
            return chunk.getLength();
        }
        
        @Override
        public long getBytesWritten() {
            return InternalOutputBuffer.this.byteCount;
        }
    }
}
