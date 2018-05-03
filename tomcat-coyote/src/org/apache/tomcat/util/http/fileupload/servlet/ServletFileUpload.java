// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload.servlet;

import java.io.IOException;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.FileItem;
import java.util.List;
import java.util.Map;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileUpload;

public class ServletFileUpload extends FileUpload
{
    private static final String POST_METHOD = "POST";
    
    public static final boolean isMultipartContent(final HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && FileUploadBase.isMultipartContent(new ServletRequestContext(request));
    }
    
    public ServletFileUpload() {
    }
    
    public ServletFileUpload(final FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }
    
    public Map<String, List<FileItem>> parseParameterMap(final HttpServletRequest request) throws FileUploadException {
        return this.parseParameterMap(new ServletRequestContext(request));
    }
    
    public FileItemIterator getItemIterator(final HttpServletRequest request) throws FileUploadException, IOException {
        return super.getItemIterator(new ServletRequestContext(request));
    }
}
