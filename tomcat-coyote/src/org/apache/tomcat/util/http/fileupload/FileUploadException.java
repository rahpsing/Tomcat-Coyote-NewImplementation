// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

public class FileUploadException extends Exception
{
    private static final long serialVersionUID = -4222909057964038517L;
    
    public FileUploadException() {
    }
    
    public FileUploadException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public FileUploadException(final String message) {
        super(message);
    }
    
    public FileUploadException(final Throwable cause) {
        super(cause);
    }
}
