// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

import java.io.IOException;
import java.io.InputStream;

public interface RequestContext
{
    String getCharacterEncoding();
    
    String getContentType();
    
    InputStream getInputStream() throws IOException;
}
