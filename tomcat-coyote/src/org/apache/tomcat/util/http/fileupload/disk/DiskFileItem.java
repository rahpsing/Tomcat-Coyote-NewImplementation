// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload.disk;

import java.util.UUID;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.BufferedInputStream;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import java.util.Map;
import org.apache.tomcat.util.http.fileupload.ParameterParser;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.tomcat.util.http.fileupload.FileItemHeaders;
import org.apache.tomcat.util.http.fileupload.DeferredFileOutputStream;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.http.fileupload.FileItem;

public class DiskFileItem implements FileItem
{
    public static final String DEFAULT_CHARSET = "ISO-8859-1";
    private static final String UID;
    private static final AtomicInteger COUNTER;
    private String fieldName;
    private final String contentType;
    private boolean isFormField;
    private final String fileName;
    private long size;
    private final int sizeThreshold;
    private final File repository;
    private byte[] cachedContent;
    private transient DeferredFileOutputStream dfos;
    private transient File tempFile;
    private FileItemHeaders headers;
    
    public DiskFileItem(final String fieldName, final String contentType, final boolean isFormField, final String fileName, final int sizeThreshold, final File repository) {
        this.size = -1L;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFormField = isFormField;
        this.fileName = fileName;
        this.sizeThreshold = sizeThreshold;
        this.repository = repository;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        if (!this.isInMemory()) {
            return new FileInputStream(this.dfos.getFile());
        }
        if (this.cachedContent == null) {
            this.cachedContent = this.dfos.getData();
        }
        return new ByteArrayInputStream(this.cachedContent);
    }
    
    @Override
    public String getContentType() {
        return this.contentType;
    }
    
    public String getCharSet() {
        final ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        final Map<String, String> params = parser.parse(this.getContentType(), ';');
        return params.get("charset");
    }
    
    @Override
    public String getName() {
        return Streams.checkFileName(this.fileName);
    }
    
    @Override
    public boolean isInMemory() {
        return this.cachedContent != null || this.dfos.isInMemory();
    }
    
    @Override
    public long getSize() {
        if (this.size >= 0L) {
            return this.size;
        }
        if (this.cachedContent != null) {
            return this.cachedContent.length;
        }
        if (this.dfos.isInMemory()) {
            return this.dfos.getData().length;
        }
        return this.dfos.getFile().length();
    }
    
    @Override
    public byte[] get() {
        if (this.isInMemory()) {
            if (this.cachedContent == null) {
                this.cachedContent = this.dfos.getData();
            }
            return this.cachedContent;
        }
        byte[] fileData = new byte[(int)this.getSize()];
        InputStream fis = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(this.dfos.getFile()));
            fis.read(fileData);
        }
        catch (IOException e) {
            fileData = null;
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (IOException ex) {}
            }
        }
        return fileData;
    }
    
    @Override
    public String getString(final String charset) throws UnsupportedEncodingException {
        return new String(this.get(), charset);
    }
    
    @Override
    public String getString() {
        final byte[] rawdata = this.get();
        String charset = this.getCharSet();
        if (charset == null) {
            charset = "ISO-8859-1";
        }
        try {
            return new String(rawdata, charset);
        }
        catch (UnsupportedEncodingException e) {
            return new String(rawdata);
        }
    }
    
    @Override
    public void write(final File file) throws Exception {
        if (this.isInMemory()) {
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(file);
                fout.write(this.get());
            }
            finally {
                if (fout != null) {
                    fout.close();
                }
            }
        }
        else {
            final File outputFile = this.getStoreLocation();
            if (outputFile == null) {
                throw new FileUploadException("Cannot write uploaded file to disk!");
            }
            this.size = outputFile.length();
            if (!outputFile.renameTo(file)) {
                BufferedInputStream in = null;
                BufferedOutputStream out = null;
                try {
                    in = new BufferedInputStream(new FileInputStream(outputFile));
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    IOUtils.copy(in, out);
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (IOException ex) {}
                    }
                    if (out != null) {
                        try {
                            out.close();
                        }
                        catch (IOException ex2) {}
                    }
                }
            }
        }
    }
    
    @Override
    public void delete() {
        this.cachedContent = null;
        final File outputFile = this.getStoreLocation();
        if (outputFile != null && outputFile.exists()) {
            outputFile.delete();
        }
    }
    
    @Override
    public String getFieldName() {
        return this.fieldName;
    }
    
    @Override
    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }
    
    @Override
    public boolean isFormField() {
        return this.isFormField;
    }
    
    @Override
    public void setFormField(final boolean state) {
        this.isFormField = state;
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (this.dfos == null) {
            final File outputFile = this.getTempFile();
            this.dfos = new DeferredFileOutputStream(this.sizeThreshold, outputFile);
        }
        return this.dfos;
    }
    
    public File getStoreLocation() {
        if (this.dfos == null) {
            return null;
        }
        return this.dfos.getFile();
    }
    
    @Override
    protected void finalize() {
        final File outputFile = this.dfos.getFile();
        if (outputFile != null && outputFile.exists()) {
            outputFile.delete();
        }
    }
    
    protected File getTempFile() {
        if (this.tempFile == null) {
            File tempDir = this.repository;
            if (tempDir == null) {
                tempDir = new File(System.getProperty("java.io.tmpdir"));
            }
            final String tempFileName = String.format("upload_%s_%s.tmp", DiskFileItem.UID, getUniqueId());
            this.tempFile = new File(tempDir, tempFileName);
        }
        return this.tempFile;
    }
    
    private static String getUniqueId() {
        final int limit = 100000000;
        final int current = DiskFileItem.COUNTER.getAndIncrement();
        String id = Integer.toString(current);
        if (current < 100000000) {
            id = ("00000000" + id).substring(id.length());
        }
        return id;
    }
    
    @Override
    public String toString() {
        return String.format("name=%s, StoreLocation=%s, size=%s bytes, isFormField=%s, FieldName=%s", this.getName(), this.getStoreLocation(), this.getSize(), this.isFormField(), this.getFieldName());
    }
    
    @Override
    public FileItemHeaders getHeaders() {
        return this.headers;
    }
    
    @Override
    public void setHeaders(final FileItemHeaders pHeaders) {
        this.headers = pHeaders;
    }
    
    static {
        UID = UUID.randomUUID().toString().replace('-', '_');
        COUNTER = new AtomicInteger(0);
    }
}
