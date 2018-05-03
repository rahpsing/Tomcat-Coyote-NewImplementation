// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.filters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class FlushableGZIPOutputStream extends GZIPOutputStream
{
    private byte[] lastByte;
    private boolean hasLastByte;
    private boolean flagReenableCompression;
    
    public FlushableGZIPOutputStream(final OutputStream os) throws IOException {
        super(os);
        this.lastByte = new byte[1];
        this.hasLastByte = false;
        this.flagReenableCompression = false;
    }
    
    @Override
    public void write(final byte[] bytes) throws IOException {
        this.write(bytes, 0, bytes.length);
    }
    
    @Override
    public synchronized void write(final byte[] bytes, final int offset, final int length) throws IOException {
        if (length > 0) {
            this.flushLastByte();
            if (length > 1) {
                this.reenableCompression();
                super.write(bytes, offset, length - 1);
            }
            this.rememberLastByte(bytes[offset + length - 1]);
        }
    }
    
    @Override
    public synchronized void write(final int i) throws IOException {
        this.flushLastByte();
        this.rememberLastByte((byte)i);
    }
    
    @Override
    public synchronized void finish() throws IOException {
        try {
            this.flushLastByte();
        }
        catch (IOException ex) {}
        super.finish();
    }
    
    @Override
    public synchronized void close() throws IOException {
        try {
            this.flushLastByte();
        }
        catch (IOException ex) {}
        super.close();
    }
    
    private void reenableCompression() {
        if (this.flagReenableCompression && !this.def.finished()) {
            this.flagReenableCompression = false;
            this.def.setLevel(-1);
        }
    }
    
    private void rememberLastByte(final byte b) {
        this.lastByte[0] = b;
        this.hasLastByte = true;
    }
    
    private void flushLastByte() throws IOException {
        if (this.hasLastByte) {
            this.reenableCompression();
            this.hasLastByte = false;
            super.write(this.lastByte, 0, 1);
        }
    }
    
    @Override
    public synchronized void flush() throws IOException {
        if (this.hasLastByte && !this.def.finished()) {
            this.def.setLevel(0);
            this.flushLastByte();
            this.flagReenableCompression = true;
        }
        this.out.flush();
    }
    
    @Override
    protected void deflate() throws IOException {
        int len;
        do {
            len = this.def.deflate(this.buf, 0, this.buf.length);
            if (len > 0) {
                this.out.write(this.buf, 0, len);
            }
        } while (len != 0);
    }
}
