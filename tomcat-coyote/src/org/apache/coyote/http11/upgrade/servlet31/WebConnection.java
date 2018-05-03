// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade.servlet31;

import org.apache.coyote.http11.upgrade.AbstractServletOutputStream;
import java.io.IOException;
import org.apache.coyote.http11.upgrade.AbstractServletInputStream;

public interface WebConnection
{
    AbstractServletInputStream getInputStream() throws IOException;
    
    AbstractServletOutputStream getOutputStream() throws IOException;
    
    void close() throws Exception;
}
