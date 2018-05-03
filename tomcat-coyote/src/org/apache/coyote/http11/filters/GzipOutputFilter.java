// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.filters;

import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.ByteChunk;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.coyote.OutputBuffer;
import org.apache.juli.logging.Log;
import org.apache.coyote.http11.OutputFilter;

public class GzipOutputFilter implements OutputFilter
{
    protected static Log log;
    protected OutputBuffer buffer;
    protected GZIPOutputStream compressionStream;
    protected OutputStream fakeOutputStream;
    
    public GzipOutputFilter() {
        this.compressionStream = null;
        this.fakeOutputStream = new FakeOutputStream();
    }
    
    @Override
    public int doWrite(final ByteChunk chunk, final Response res) throws IOException {
        if (this.compressionStream == null) {
            this.compressionStream = new FlushableGZIPOutputStream(this.fakeOutputStream);
        }
        this.compressionStream.write(chunk.getBytes(), chunk.getStart(), chunk.getLength());
        return chunk.getLength();
    }
    
    @Override
    public long getBytesWritten() {
        return this.buffer.getBytesWritten();
    }
    
    public void flush() {
        if (this.compressionStream != null) {
            try {
                if (GzipOutputFilter.log.isDebugEnabled()) {
                    GzipOutputFilter.log.debug((Object)"Flushing the compression stream!");
                }
                this.compressionStream.flush();
            }
            catch (IOException e) {
                if (GzipOutputFilter.log.isDebugEnabled()) {
                    GzipOutputFilter.log.debug((Object)"Ignored exception while flushing gzip filter", (Throwable)e);
                }
            }
        }
    }
    
    @Override
    public void setResponse(final Response response) {
    }
    
    @Override
    public void setBuffer(final OutputBuffer buffer) {
        this.buffer = buffer;
    }
    
    @Override
    public long end() throws IOException {
        if (this.compressionStream == null) {
            this.compressionStream = new FlushableGZIPOutputStream(this.fakeOutputStream);
        }
        this.compressionStream.finish();
        this.compressionStream.close();
        return ((OutputFilter)this.buffer).end();
    }
    
    @Override
    public void recycle() {
        this.compressionStream = null;
    }
    
    static {
        GzipOutputFilter.log = LogFactory.getLog((Class)GzipOutputFilter.class);
    }
    
    protected class FakeOutputStream extends OutputStream
    {
        protected ByteChunk outputChunk;
        protected byte[] singleByteBuffer;
        
        protected FakeOutputStream() {
            this.outputChunk = new ByteChunk();
            this.singleByteBuffer = new byte[1];
        }
        
        @Override
        public void write(final int b) throws IOException {
            this.singleByteBuffer[0] = (byte)(b & 0xFF);
            this.outputChunk.setBytes(this.singleByteBuffer, 0, 1);
            GzipOutputFilter.this.buffer.doWrite(this.outputChunk, null);
        }
        
        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            this.outputChunk.setBytes(b, off, len);
            GzipOutputFilter.this.buffer.doWrite(this.outputChunk, null);
        }
        
        @Override
        public void flush() throws IOException {
        }
        
        @Override
        public void close() throws IOException {
        }
    }
}
