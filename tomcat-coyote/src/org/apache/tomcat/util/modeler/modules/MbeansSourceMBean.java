// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler.modules;

import javax.management.ObjectName;
import java.util.List;

@Deprecated
public interface MbeansSourceMBean
{
    void setSource(final Object p0);
    
    Object getSource();
    
    List<ObjectName> getMBeans();
    
    void load() throws Exception;
    
    void init() throws Exception;
    
    void save();
}
