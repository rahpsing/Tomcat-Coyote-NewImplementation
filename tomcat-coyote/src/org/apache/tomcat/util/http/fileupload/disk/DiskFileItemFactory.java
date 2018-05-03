// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload.disk;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileCleaningTracker;
import java.io.File;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;

public class DiskFileItemFactory implements FileItemFactory
{
    public static final int DEFAULT_SIZE_THRESHOLD = 10240;
    private File repository;
    private int sizeThreshold;
    private FileCleaningTracker fileCleaningTracker;
    
    public DiskFileItemFactory() {
        this(10240, null);
    }
    
    public DiskFileItemFactory(final int sizeThreshold, final File repository) {
        this.sizeThreshold = 10240;
        this.sizeThreshold = sizeThreshold;
        this.repository = repository;
    }
    
    public File getRepository() {
        return this.repository;
    }
    
    public void setRepository(final File repository) {
        this.repository = repository;
    }
    
    public int getSizeThreshold() {
        return this.sizeThreshold;
    }
    
    public void setSizeThreshold(final int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }
    
    @Override
    public FileItem createItem(final String fieldName, final String contentType, final boolean isFormField, final String fileName) {
        final DiskFileItem result = new DiskFileItem(fieldName, contentType, isFormField, fileName, this.sizeThreshold, this.repository);
        final FileCleaningTracker tracker = this.getFileCleaningTracker();
        if (tracker != null) {
            tracker.track(result.getTempFile(), result);
        }
        return result;
    }
    
    public FileCleaningTracker getFileCleaningTracker() {
        return this.fileCleaningTracker;
    }
    
    public void setFileCleaningTracker(final FileCleaningTracker pTracker) {
        this.fileCleaningTracker = pTracker;
    }
}
