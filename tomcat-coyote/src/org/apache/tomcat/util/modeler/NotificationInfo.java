// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.MBeanNotificationInfo;

public class NotificationInfo extends FeatureInfo
{
    static final long serialVersionUID = -6319885418912650856L;
    transient MBeanNotificationInfo info;
    protected String[] notifTypes;
    
    public NotificationInfo() {
        this.info = null;
        this.notifTypes = new String[0];
    }
    
    @Override
    public void setDescription(final String description) {
        super.setDescription(description);
        this.info = null;
    }
    
    @Override
    public void setName(final String name) {
        super.setName(name);
        this.info = null;
    }
    
    public String[] getNotifTypes() {
        return this.notifTypes;
    }
    
    public void addNotifType(final String notifType) {
        synchronized (this.notifTypes) {
            final String[] results = new String[this.notifTypes.length + 1];
            System.arraycopy(this.notifTypes, 0, results, 0, this.notifTypes.length);
            results[this.notifTypes.length] = notifType;
            this.notifTypes = results;
            this.info = null;
        }
    }
    
    public MBeanNotificationInfo createNotificationInfo() {
        if (this.info != null) {
            return this.info;
        }
        return this.info = new MBeanNotificationInfo(this.getNotifTypes(), this.getName(), this.getDescription());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotificationInfo[");
        sb.append("name=");
        sb.append(this.name);
        sb.append(", description=");
        sb.append(this.description);
        sb.append(", notifTypes=");
        sb.append(this.notifTypes.length);
        sb.append("]");
        return sb.toString();
    }
}
