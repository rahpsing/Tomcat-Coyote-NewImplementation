// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import java.util.HashSet;
import javax.management.NotificationFilter;

public class BaseAttributeFilter implements NotificationFilter
{
    private static final long serialVersionUID = 1L;
    private HashSet<String> names;
    
    public BaseAttributeFilter(final String name) {
        this.names = new HashSet<String>();
        if (name != null) {
            this.addAttribute(name);
        }
    }
    
    public void addAttribute(final String name) {
        synchronized (this.names) {
            this.names.add(name);
        }
    }
    
    public void clear() {
        synchronized (this.names) {
            this.names.clear();
        }
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
        if (!(notification instanceof AttributeChangeNotification)) {
            return false;
        }
        final AttributeChangeNotification acn = (AttributeChangeNotification)notification;
        if (!"jmx.attribute.change".equals(acn.getType())) {
            return false;
        }
        synchronized (this.names) {
            return this.names.size() < 1 || this.names.contains(acn.getAttributeName());
        }
    }
    
    public void removeAttribute(final String name) {
        synchronized (this.names) {
            this.names.remove(name);
        }
    }
}
