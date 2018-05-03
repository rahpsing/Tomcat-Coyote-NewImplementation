// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.MBeanParameterInfo;

public class ParameterInfo extends FeatureInfo
{
    static final long serialVersionUID = 2222796006787664020L;
    
    public MBeanParameterInfo createParameterInfo() {
        if (this.info == null) {
            this.info = new MBeanParameterInfo(this.getName(), this.getType(), this.getDescription());
        }
        return (MBeanParameterInfo)this.info;
    }
}
