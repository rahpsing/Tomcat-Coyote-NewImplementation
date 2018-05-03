// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.threads;

public class TaskThread extends Thread
{
    private final long creationTime;
    
    public TaskThread(final ThreadGroup group, final Runnable target, final String name) {
        super(group, target, name);
        this.creationTime = System.currentTimeMillis();
    }
    
    public TaskThread(final ThreadGroup group, final Runnable target, final String name, final long stackSize) {
        super(group, target, name, stackSize);
        this.creationTime = System.currentTimeMillis();
    }
    
    public final long getCreationTime() {
        return this.creationTime;
    }
}
