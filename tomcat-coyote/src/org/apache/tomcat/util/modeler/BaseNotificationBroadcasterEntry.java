// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.NotificationListener;
import javax.management.NotificationFilter;

class BaseNotificationBroadcasterEntry
{
    public NotificationFilter filter;
    public Object handback;
    public NotificationListener listener;
    
    public BaseNotificationBroadcasterEntry(final NotificationListener listener, final NotificationFilter filter, final Object handback) {
        this.filter = null;
        this.handback = null;
        this.listener = null;
        this.listener = listener;
        this.filter = filter;
        this.handback = handback;
    }
}
