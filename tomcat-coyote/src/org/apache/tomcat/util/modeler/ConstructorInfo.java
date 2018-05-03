// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.MBeanConstructorInfo;

public class ConstructorInfo extends OperationInfo
{
    static final long serialVersionUID = -5735336213417238238L;
    
    public MBeanConstructorInfo createConstructorInfo() {
        if (this.info == null) {
            this.info = new MBeanConstructorInfo(this.getName(), this.getDescription(), this.getMBeanParameterInfo());
        }
        return (MBeanConstructorInfo)this.info;
    }
}
