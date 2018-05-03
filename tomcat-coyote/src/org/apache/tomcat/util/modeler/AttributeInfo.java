// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.MBeanAttributeInfo;

public class AttributeInfo extends FeatureInfo
{
    static final long serialVersionUID = -2511626862303972143L;
    protected String displayName;
    protected String getMethod;
    protected String setMethod;
    protected boolean readable;
    protected boolean writeable;
    protected boolean is;
    
    public AttributeInfo() {
        this.displayName = null;
        this.getMethod = null;
        this.setMethod = null;
        this.readable = true;
        this.writeable = true;
        this.is = false;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
    
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }
    
    public String getGetMethod() {
        if (this.getMethod == null) {
            this.getMethod = this.getMethodName(this.getName(), true, this.isIs());
        }
        return this.getMethod;
    }
    
    public void setGetMethod(final String getMethod) {
        this.getMethod = getMethod;
    }
    
    public boolean isIs() {
        return this.is;
    }
    
    public void setIs(final boolean is) {
        this.is = is;
    }
    
    public boolean isReadable() {
        return this.readable;
    }
    
    public void setReadable(final boolean readable) {
        this.readable = readable;
    }
    
    public String getSetMethod() {
        if (this.setMethod == null) {
            this.setMethod = this.getMethodName(this.getName(), false, false);
        }
        return this.setMethod;
    }
    
    public void setSetMethod(final String setMethod) {
        this.setMethod = setMethod;
    }
    
    public boolean isWriteable() {
        return this.writeable;
    }
    
    public void setWriteable(final boolean writeable) {
        this.writeable = writeable;
    }
    
    MBeanAttributeInfo createAttributeInfo() {
        if (this.info == null) {
            this.info = new MBeanAttributeInfo(this.getName(), this.getType(), this.getDescription(), this.isReadable(), this.isWriteable(), false);
        }
        return (MBeanAttributeInfo)this.info;
    }
    
    private String getMethodName(final String name, final boolean getter, final boolean is) {
        final StringBuilder sb = new StringBuilder();
        if (getter) {
            if (is) {
                sb.append("is");
            }
            else {
                sb.append("get");
            }
        }
        else {
            sb.append("set");
        }
        sb.append(Character.toUpperCase(name.charAt(0)));
        sb.append(name.substring(1));
        return sb.toString();
    }
}
