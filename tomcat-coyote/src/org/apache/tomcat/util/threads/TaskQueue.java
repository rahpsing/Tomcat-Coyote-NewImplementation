// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.threads;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue extends LinkedBlockingQueue<Runnable>
{
    private static final long serialVersionUID = 1L;
    private ThreadPoolExecutor parent;
    private Integer forcedRemainingCapacity;
    
    public TaskQueue() {
        this.parent = null;
        this.forcedRemainingCapacity = null;
    }
    
    public TaskQueue(final int capacity) {
        super(capacity);
        this.parent = null;
        this.forcedRemainingCapacity = null;
    }
    
    public TaskQueue(final Collection<? extends Runnable> c) {
        super(c);
        this.parent = null;
        this.forcedRemainingCapacity = null;
    }
    
    public void setParent(final ThreadPoolExecutor tp) {
        this.parent = tp;
    }
    
    public boolean force(final Runnable o) {
        if (this.parent.isShutdown()) {
            throw new RejectedExecutionException("Executor not running, can't force a command into the queue");
        }
        return super.offer(o);
    }
    
    public boolean force(final Runnable o, final long timeout, final TimeUnit unit) throws InterruptedException {
        if (this.parent.isShutdown()) {
            throw new RejectedExecutionException("Executor not running, can't force a command into the queue");
        }
        return super.offer(o, timeout, unit);
    }
    
    @Override
    public boolean offer(final Runnable o) {
        if (this.parent == null) {
            return super.offer(o);
        }
        if (this.parent.getPoolSize() == this.parent.getMaximumPoolSize()) {
            return super.offer(o);
        }
        if (this.parent.getSubmittedCount() < this.parent.getPoolSize()) {
            return super.offer(o);
        }
        return this.parent.getPoolSize() >= this.parent.getMaximumPoolSize() && super.offer(o);
    }
    
    @Override
    public Runnable poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        final Runnable runnable = super.poll(timeout, unit);
        if (runnable == null && this.parent != null) {
            this.parent.stopCurrentThreadIfNeeded();
        }
        return runnable;
    }
    
    @Override
    public Runnable take() throws InterruptedException {
        if (this.parent != null && this.parent.currentThreadShouldBeStopped()) {
            return this.poll(this.parent.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
        return super.take();
    }
    
    @Override
    public int remainingCapacity() {
        if (this.forcedRemainingCapacity != null) {
            return this.forcedRemainingCapacity;
        }
        return super.remainingCapacity();
    }
    
    public void setForcedRemainingCapacity(final Integer forcedRemainingCapacity) {
        this.forcedRemainingCapacity = forcedRemainingCapacity;
    }
}
