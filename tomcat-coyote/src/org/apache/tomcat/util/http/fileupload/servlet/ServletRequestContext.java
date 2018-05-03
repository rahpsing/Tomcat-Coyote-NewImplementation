// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload.servlet;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.UploadContext;

public class ServletRequestContext implements UploadContext
{
    private final HttpServletRequest request;
    
    public ServletRequestContext(final HttpServletRequest request) {
        this.request = request;
    }
    
    @Override
    public String getCharacterEncoding() {
        return this.request.getCharacterEncoding();
    }
    
    @Override
    public String getContentType() {
        return this.request.getContentType();
    }
    
    @Override
    public long contentLength() {
        long size;
        try {
            size = Long.parseLong(this.request.getHeader("Content-length"));
        }
        catch (NumberFormatException e) {
            size = this.request.getContentLength();
        }
        return size;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return (InputStream)this.request.getInputStream();
    }
    
    @Override
    public String toString() {
        return String.format("ContentLength=%s, ContentType=%s", this.contentLength(), this.getContentType());
    }
}
