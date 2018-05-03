// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.threads;

import org.apache.juli.logging.LogFactory;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;

public class ThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor
{
    protected static final StringManager sm;
    private static final Log log;
    private final AtomicInteger submittedCount;
    private final AtomicLong lastContextStoppedTime;
    private final AtomicLong lastTimeThreadKilledItself;
    private long threadRenewalDelay;
    
    public ThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        this.submittedCount = new AtomicInteger(0);
        this.lastContextStoppedTime = new AtomicLong(0L);
        this.lastTimeThreadKilledItself = new AtomicLong(0L);
        this.threadRenewalDelay = 1000L;
    }
    
    public ThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory, final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.submittedCount = new AtomicInteger(0);
        this.lastContextStoppedTime = new AtomicLong(0L);
        this.lastTimeThreadKilledItself = new AtomicLong(0L);
        this.threadRenewalDelay = 1000L;
    }
    
    public ThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectHandler());
        this.submittedCount = new AtomicInteger(0);
        this.lastContextStoppedTime = new AtomicLong(0L);
        this.lastTimeThreadKilledItself = new AtomicLong(0L);
        this.threadRenewalDelay = 1000L;
    }
    
    public ThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new RejectHandler());
        this.submittedCount = new AtomicInteger(0);
        this.lastContextStoppedTime = new AtomicLong(0L);
        this.lastTimeThreadKilledItself = new AtomicLong(0L);
        this.threadRenewalDelay = 1000L;
    }
    
    public long getThreadRenewalDelay() {
        return this.threadRenewalDelay;
    }
    
    public void setThreadRenewalDelay(final long threadRenewalDelay) {
        this.threadRenewalDelay = threadRenewalDelay;
    }
    
    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        this.submittedCount.decrementAndGet();
        if (t == null) {
            this.stopCurrentThreadIfNeeded();
        }
    }
    
    protected void stopCurrentThreadIfNeeded() {
        if (this.currentThreadShouldBeStopped()) {
            final long lastTime = this.lastTimeThreadKilledItself.longValue();
            if (lastTime + this.threadRenewalDelay < System.currentTimeMillis() && this.lastTimeThreadKilledItself.compareAndSet(lastTime, System.currentTimeMillis() + 1L)) {
                final String msg = ThreadPoolExecutor.sm.getString("threadPoolExecutor.threadStoppedToAvoidPotentialLeak", new Object[] { Thread.currentThread().getName() });
                Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(final Thread t, final Throwable e) {
                        ThreadPoolExecutor.log.debug((Object)msg);
                    }
                });
                throw new RuntimeException(msg);
            }
        }
    }
    
    protected boolean currentThreadShouldBeStopped() {
        if (this.threadRenewalDelay >= 0L && Thread.currentThread() instanceof TaskThread) {
            final TaskThread currentTaskThread = (TaskThread)Thread.currentThread();
            if (currentTaskThread.getCreationTime() < this.lastContextStoppedTime.longValue()) {
                return true;
            }
        }
        return false;
    }
    
    public int getSubmittedCount() {
        return this.submittedCount.get();
    }
    
    @Override
    public void execute(final Runnable command) {
        this.execute(command, 0L, TimeUnit.MILLISECONDS);
    }
    
    public void execute(final Runnable command, final long timeout, final TimeUnit unit) {
        this.submittedCount.incrementAndGet();
        try {
            super.execute(command);
        }
        catch (RejectedExecutionException rx) {
            if (!(super.getQueue() instanceof TaskQueue)) {
                this.submittedCount.decrementAndGet();
                throw rx;
            }
            final TaskQueue queue = (TaskQueue)super.getQueue();
            try {
                if (!queue.force(command, timeout, unit)) {
                    this.submittedCount.decrementAndGet();
                    throw new RejectedExecutionException("Queue capacity is full.");
                }
            }
            catch (InterruptedException x) {
                this.submittedCount.decrementAndGet();
                Thread.interrupted();
                throw new RejectedExecutionException(x);
            }
        }
    }
    
    public void contextStopping() {
        this.lastContextStoppedTime.set(System.currentTimeMillis());
        final int savedCorePoolSize = this.getCorePoolSize();
        final TaskQueue taskQueue = (this.getQueue() instanceof TaskQueue) ? ((TaskQueue)this.getQueue()) : null;
        if (taskQueue != null) {
            taskQueue.setForcedRemainingCapacity(0);
        }
        this.setCorePoolSize(0);
        try {
            Thread.sleep(200L);
        }
        catch (InterruptedException ex) {}
        if (taskQueue != null) {
            taskQueue.setForcedRemainingCapacity(null);
        }
        this.setCorePoolSize(savedCorePoolSize);
    }
    
    static {
        sm = StringManager.getManager("org.apache.tomcat.util.threads.res");
        log = LogFactory.getLog((Class)ThreadPoolExecutor.class);
    }
    
    private static class RejectHandler implements RejectedExecutionHandler
    {
        @Override
        public void rejectedExecution(final Runnable r, final java.util.concurrent.ThreadPoolExecutor executor) {
            throw new RejectedExecutionException();
        }
    }
}
