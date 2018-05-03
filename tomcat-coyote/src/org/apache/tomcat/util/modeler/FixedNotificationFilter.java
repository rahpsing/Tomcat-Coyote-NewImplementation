// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.Notification;
import java.util.HashSet;
import javax.management.NotificationFilter;

public class FixedNotificationFilter implements NotificationFilter
{
    private static final long serialVersionUID = 1L;
    private HashSet<String> names;
    String[] namesA;
    
    public FixedNotificationFilter(final String[] names) {
        this.names = new HashSet<String>();
        this.namesA = null;
    }
    
    public String[] getNames() {
        synchronized (this.names) {
            return this.names.toArray(new String[this.names.size()]);
        }
    }
    
    @Override
    public boolean isNotificationEnabled(final Notification notification) {
        if (notification == null) {
            return false;
        }
        synchronized (this.names) {
            return this.names.size() < 1 || this.names.contains(notification.getType());
        }
    }
}
