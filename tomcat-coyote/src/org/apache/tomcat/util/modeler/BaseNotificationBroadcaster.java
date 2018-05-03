// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.Notification;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import java.util.Iterator;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.util.ArrayList;
import javax.management.NotificationBroadcaster;

public class BaseNotificationBroadcaster implements NotificationBroadcaster
{
    protected ArrayList<BaseNotificationBroadcasterEntry> entries;
    
    public BaseNotificationBroadcaster() {
        this.entries = new ArrayList<BaseNotificationBroadcasterEntry>();
    }
    
    @Override
    public void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws IllegalArgumentException {
        synchronized (this.entries) {
            if (filter instanceof BaseAttributeFilter) {
                final BaseAttributeFilter newFilter = (BaseAttributeFilter)filter;
                for (final BaseNotificationBroadcasterEntry item : this.entries) {
                    if (item.listener == listener && item.filter != null && item.filter instanceof BaseAttributeFilter && item.handback == handback) {
                        final BaseAttributeFilter oldFilter = (BaseAttributeFilter)item.filter;
                        final String[] newNames = newFilter.getNames();
                        final String[] oldNames = oldFilter.getNames();
                        if (newNames.length == 0) {
                            oldFilter.clear();
                        }
                        else if (oldNames.length != 0) {
                            for (int i = 0; i < newNames.length; ++i) {
                                oldFilter.addAttribute(newNames[i]);
                            }
                        }
                        return;
                    }
                }
            }
            this.entries.add(new BaseNotificationBroadcasterEntry(listener, filter, handback));
        }
    }
    
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[0];
    }
    
    @Override
    public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        synchronized (this.entries) {
            final Iterator<BaseNotificationBroadcasterEntry> items = this.entries.iterator();
            while (items.hasNext()) {
                final BaseNotificationBroadcasterEntry item = items.next();
                if (item.listener == listener) {
                    items.remove();
                }
            }
        }
    }
    
    public void removeNotificationListener(final NotificationListener listener, final Object handback) throws ListenerNotFoundException {
        this.removeNotificationListener(listener);
    }
    
    public void removeNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws ListenerNotFoundException {
        this.removeNotificationListener(listener);
    }
    
    public void sendNotification(final Notification notification) {
        synchronized (this.entries) {
            for (final BaseNotificationBroadcasterEntry item : this.entries) {
                if (item.filter != null && !item.filter.isNotificationEnabled(notification)) {
                    continue;
                }
                item.listener.handleNotification(notification, item.handback);
            }
        }
    }
}
