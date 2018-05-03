// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler.modules;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.ManagedBean;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.management.ObjectName;
import java.util.List;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.juli.logging.Log;

public class MbeansDescriptorsSerSource extends ModelerSource
{
    private static final Log log;
    Registry registry;
    String type;
    List<ObjectName> mbeans;
    
    public MbeansDescriptorsSerSource() {
        this.mbeans = new ArrayList<ObjectName>();
    }
    
    public void setRegistry(final Registry reg) {
        this.registry = reg;
    }
    
    @Deprecated
    public void setLocation(final String loc) {
        this.location = loc;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    public void setSource(final Object source) {
        this.source = source;
    }
    
    @Override
    public List<ObjectName> loadDescriptors(final Registry registry, final String type, final Object source) throws Exception {
        this.setRegistry(registry);
        this.setType(type);
        this.setSource(source);
        this.execute();
        return this.mbeans;
    }
    
    public void execute() throws Exception {
        if (this.registry == null) {
            this.registry = Registry.getRegistry(null, null);
        }
        final long t1 = System.currentTimeMillis();
        try {
            InputStream stream = null;
            if (this.source instanceof URL) {
                stream = ((URL)this.source).openStream();
            }
            if (this.source instanceof InputStream) {
                stream = (InputStream)this.source;
            }
            if (stream == null) {
                throw new Exception("Can't process " + this.source);
            }
            final ObjectInputStream ois = new ObjectInputStream(stream);
            Thread.currentThread().setContextClassLoader(ManagedBean.class.getClassLoader());
            final Object obj = ois.readObject();
            final ManagedBean[] beans = (ManagedBean[])obj;
            for (int i = 0; i < beans.length; ++i) {
                this.registry.addManagedBean(beans[i]);
            }
        }
        catch (Exception ex) {
            MbeansDescriptorsSerSource.log.error((Object)("Error reading descriptors " + this.source + " " + ex.toString()), (Throwable)ex);
            throw ex;
        }
        final long t2 = System.currentTimeMillis();
        MbeansDescriptorsSerSource.log.info((Object)("Reading descriptors ( ser ) " + (t2 - t1)));
    }
    
    static {
        log = LogFactory.getLog((Class)MbeansDescriptorsSerSource.class);
    }
}
