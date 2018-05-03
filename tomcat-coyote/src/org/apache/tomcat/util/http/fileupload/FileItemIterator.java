// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

import java.io.IOException;

public interface FileItemIterator
{
    boolean hasNext() throws FileUploadException, IOException;
    
    FileItemStream next() throws FileUploadException, IOException;
}
