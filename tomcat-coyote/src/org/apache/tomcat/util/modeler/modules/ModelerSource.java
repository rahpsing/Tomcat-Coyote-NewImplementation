// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler.modules;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.net.URL;
import java.io.InputStream;
import javax.management.ObjectName;
import java.util.List;
import org.apache.tomcat.util.modeler.Registry;

public abstract class ModelerSource
{
    protected Object source;
    @Deprecated
    protected String location;
    
    @Deprecated
    public List<ObjectName> loadDescriptors(final Registry registry, final String location, final String type, final Object source) throws Exception {
        return this.loadDescriptors(registry, type, source);
    }
    
    @Deprecated
    public void updateField(final ObjectName oname, final String name, final Object value) {
    }
    
    @Deprecated
    public void store() {
    }
    
    @Deprecated
    protected InputStream getInputStream() throws IOException {
        if (this.source instanceof URL) {
            final URL url = (URL)this.source;
            this.location = url.toString();
            return url.openStream();
        }
        if (this.source instanceof File) {
            this.location = ((File)this.source).getAbsolutePath();
            return new FileInputStream((File)this.source);
        }
        if (this.source instanceof String) {
            this.location = (String)this.source;
            return new FileInputStream((String)this.source);
        }
        if (this.source instanceof InputStream) {
            return (InputStream)this.source;
        }
        return null;
    }
    
    public abstract List<ObjectName> loadDescriptors(final Registry p0, final String p1, final Object p2) throws Exception;
}
