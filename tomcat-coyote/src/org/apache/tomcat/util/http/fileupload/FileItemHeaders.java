// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

import java.util.Iterator;

public interface FileItemHeaders
{
    String getHeader(final String p0);
    
    Iterator<String> getHeaders(final String p0);
    
    Iterator<String> getHeaderNames();
}
