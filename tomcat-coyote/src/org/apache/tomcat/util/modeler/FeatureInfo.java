// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.MBeanFeatureInfo;
import java.io.Serializable;

public class FeatureInfo implements Serializable
{
    static final long serialVersionUID = -911529176124712296L;
    protected String description;
    protected String name;
    protected MBeanFeatureInfo info;
    protected String type;
    
    public FeatureInfo() {
        this.description = null;
        this.name = null;
        this.info = null;
        this.type = null;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
}
