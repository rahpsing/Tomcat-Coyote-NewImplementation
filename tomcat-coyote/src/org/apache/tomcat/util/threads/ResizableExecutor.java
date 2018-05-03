// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.threads;

import java.util.concurrent.Executor;

public interface ResizableExecutor extends Executor
{
    int getPoolSize();
    
    int getMaxThreads();
    
    int getActiveCount();
    
    boolean resizePool(final int p0, final int p1);
    
    boolean resizeQueue(final int p0);
}
