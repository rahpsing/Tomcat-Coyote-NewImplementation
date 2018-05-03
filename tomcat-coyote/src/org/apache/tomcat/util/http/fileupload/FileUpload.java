// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

public class FileUpload extends FileUploadBase
{
    private FileItemFactory fileItemFactory;
    
    public FileUpload() {
    }
    
    public FileUpload(final FileItemFactory fileItemFactory) {
        this.fileItemFactory = fileItemFactory;
    }
    
    @Override
    public FileItemFactory getFileItemFactory() {
        return this.fileItemFactory;
    }
    
    @Override
    public void setFileItemFactory(final FileItemFactory factory) {
        this.fileItemFactory = factory;
    }
}
