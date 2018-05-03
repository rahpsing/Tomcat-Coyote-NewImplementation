// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.OutputStream;

public class DeferredFileOutputStream extends ThresholdingOutputStream
{
    private ByteArrayOutputStream memoryOutputStream;
    private OutputStream currentOutputStream;
    private File outputFile;
    private final String prefix;
    private final String suffix;
    private final File directory;
    
    public DeferredFileOutputStream(final int threshold, final File outputFile) {
        this(threshold, outputFile, null, null, null);
    }
    
    private DeferredFileOutputStream(final int threshold, final File outputFile, final String prefix, final String suffix, final File directory) {
        super(threshold);
        this.outputFile = outputFile;
        this.memoryOutputStream = new ByteArrayOutputStream();
        this.currentOutputStream = this.memoryOutputStream;
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = directory;
    }
    
    @Override
    protected OutputStream getStream() throws IOException {
        return this.currentOutputStream;
    }
    
    @Override
    protected void thresholdReached() throws IOException {
        if (this.prefix != null) {
            this.outputFile = File.createTempFile(this.prefix, this.suffix, this.directory);
        }
        final FileOutputStream fos = new FileOutputStream(this.outputFile);
        this.memoryOutputStream.writeTo(fos);
        this.currentOutputStream = fos;
        this.memoryOutputStream = null;
    }
    
    public boolean isInMemory() {
        return !this.isThresholdExceeded();
    }
    
    public byte[] getData() {
        if (this.memoryOutputStream != null) {
            return this.memoryOutputStream.toByteArray();
        }
        return null;
    }
    
    public File getFile() {
        return this.outputFile;
    }
    
    @Override
    public void close() throws IOException {
        super.close();
    }
}
