// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.threads;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import org.apache.juli.logging.LogFactory;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.juli.logging.Log;

public class LimitLatch
{
    private static final Log log;
    private final Sync sync;
    private final AtomicLong count;
    private volatile long limit;
    private volatile boolean released;
    
    public LimitLatch(final long limit) {
        this.released = false;
        this.limit = limit;
        this.count = new AtomicLong(0L);
        this.sync = new Sync();
    }
    
    public long getCount() {
        return this.count.get();
    }
    
    public long getLimit() {
        return this.limit;
    }
    
    public void setLimit(final long limit) {
        this.limit = limit;
    }
    
    public void countUpOrAwait() throws InterruptedException {
        if (LimitLatch.log.isDebugEnabled()) {
            LimitLatch.log.debug((Object)("Counting up[" + Thread.currentThread().getName() + "] latch=" + this.getCount()));
        }
        this.sync.acquireSharedInterruptibly(1);
    }
    
    public long countDown() {
        this.sync.releaseShared(0);
        final long result = this.getCount();
        if (LimitLatch.log.isDebugEnabled()) {
            LimitLatch.log.debug((Object)("Counting down[" + Thread.currentThread().getName() + "] latch=" + result));
        }
        return result;
    }
    
    public boolean releaseAll() {
        this.released = true;
        return this.sync.releaseShared(0);
    }
    
    public void reset() {
        this.count.set(0L);
        this.released = false;
    }
    
    public boolean hasQueuedThreads() {
        return this.sync.hasQueuedThreads();
    }
    
    public Collection<Thread> getQueuedThreads() {
        return this.sync.getQueuedThreads();
    }
    
    static {
        log = LogFactory.getLog((Class)LimitLatch.class);
    }
    
    private class Sync extends AbstractQueuedSynchronizer
    {
        private static final long serialVersionUID = 1L;
        
        @Override
        protected int tryAcquireShared(final int ignored) {
            final long newCount = LimitLatch.this.count.incrementAndGet();
            if (!LimitLatch.this.released && newCount > LimitLatch.this.limit) {
                LimitLatch.this.count.decrementAndGet();
                return -1;
            }
            return 1;
        }
        
        @Override
        protected boolean tryReleaseShared(final int arg) {
            LimitLatch.this.count.decrementAndGet();
            return true;
        }
    }
}
