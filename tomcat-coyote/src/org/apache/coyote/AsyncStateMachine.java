// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.res.StringManager;

public class AsyncStateMachine<S>
{
    private static final StringManager sm;
    private volatile AsyncState state;
    private AsyncContextCallback asyncCtxt;
    private Processor<S> processor;
    
    public AsyncStateMachine(final Processor<S> processor) {
        this.state = AsyncState.DISPATCHED;
        this.asyncCtxt = null;
        this.processor = processor;
    }
    
    public boolean isAsync() {
        return this.state.isAsync();
    }
    
    public boolean isAsyncDispatching() {
        return this.state.isDispatching();
    }
    
    public boolean isAsyncStarted() {
        return this.state.isStarted();
    }
    
    public boolean isAsyncTimingOut() {
        return this.state == AsyncState.TIMING_OUT;
    }
    
    public boolean isAsyncError() {
        return this.state == AsyncState.ERROR;
    }
    
    public synchronized void asyncStart(final AsyncContextCallback asyncCtxt) {
        if (this.state == AsyncState.DISPATCHED) {
            this.state = AsyncState.STARTING;
            this.asyncCtxt = asyncCtxt;
            return;
        }
        throw new IllegalStateException(AsyncStateMachine.sm.getString("asyncStateMachine.invalidAsyncState", new Object[] { "asyncStart()", this.state }));
    }
    
    public synchronized AbstractEndpoint.Handler.SocketState asyncPostProcess() {
        if (this.state == AsyncState.STARTING) {
            this.state = AsyncState.STARTED;
            return AbstractEndpoint.Handler.SocketState.LONG;
        }
        if (this.state == AsyncState.MUST_COMPLETE) {
            this.asyncCtxt.fireOnComplete();
            this.state = AsyncState.DISPATCHED;
            return AbstractEndpoint.Handler.SocketState.ASYNC_END;
        }
        if (this.state == AsyncState.COMPLETING) {
            this.asyncCtxt.fireOnComplete();
            this.state = AsyncState.DISPATCHED;
            return AbstractEndpoint.Handler.SocketState.ASYNC_END;
        }
        if (this.state == AsyncState.MUST_DISPATCH) {
            this.state = AsyncState.DISPATCHING;
            return AbstractEndpoint.Handler.SocketState.ASYNC_END;
        }
        if (this.state == AsyncState.DISPATCHING) {
            this.state = AsyncState.DISPATCHED;
            return AbstractEndpoint.Handler.SocketState.ASYNC_END;
        }
        if (this.state == AsyncState.STARTED) {
            return AbstractEndpoint.Handler.SocketState.LONG;
        }
        throw new IllegalStateException(AsyncStateMachine.sm.getString("asyncStateMachine.invalidAsyncState", new Object[] { "asyncPostProcess()", this.state }));
    }
    
    public synchronized boolean asyncComplete() {
        boolean doComplete = false;
        if (this.state == AsyncState.STARTING) {
            this.state = AsyncState.MUST_COMPLETE;
        }
        else if (this.state == AsyncState.STARTED) {
            this.state = AsyncState.COMPLETING;
            doComplete = true;
        }
        else {
            if (this.state != AsyncState.TIMING_OUT && this.state != AsyncState.ERROR) {
                throw new IllegalStateException(AsyncStateMachine.sm.getString("asyncStateMachine.invalidAsyncState", new Object[] { "asyncComplete()", this.state }));
            }
            this.state = AsyncState.MUST_COMPLETE;
        }
        return doComplete;
    }
    
    public synchronized boolean asyncTimeout() {
        if (this.state == AsyncState.STARTED) {
            this.state = AsyncState.TIMING_OUT;
            return true;
        }
        if (this.state == AsyncState.COMPLETING || this.state == AsyncState.DISPATCHED) {
            return false;
        }
        throw new IllegalStateException(AsyncStateMachine.sm.getString("asyncStateMachine.invalidAsyncState", new Object[] { "asyncTimeout()", this.state }));
    }
    
    public synchronized boolean asyncDispatch() {
        boolean doDispatch = false;
        if (this.state == AsyncState.STARTING) {
            this.state = AsyncState.MUST_DISPATCH;
        }
        else {
            if (this.state != AsyncState.STARTED && this.state != AsyncState.TIMING_OUT && this.state != AsyncState.ERROR) {
                throw new IllegalStateException(AsyncStateMachine.sm.getString("asyncStateMachine.invalidAsyncState", new Object[] { "asyncDispatch()", this.state }));
            }
            this.state = AsyncState.DISPATCHING;
            doDispatch = true;
        }
        return doDispatch;
    }
    
    public synchronized void asyncDispatched() {
        if (this.state == AsyncState.DISPATCHING) {
            this.state = AsyncState.DISPATCHED;
            return;
        }
        throw new IllegalStateException(AsyncStateMachine.sm.getString("asyncStateMachine.invalidAsyncState", new Object[] { "asyncDispatched()", this.state }));
    }
    
    public synchronized boolean asyncError() {
        final boolean doDispatch = false;
        if (this.state == AsyncState.DISPATCHED || this.state == AsyncState.TIMING_OUT) {
            this.state = AsyncState.ERROR;
            return doDispatch;
        }
        throw new IllegalStateException(AsyncStateMachine.sm.getString("asyncStateMachine.invalidAsyncState", new Object[] { "asyncError()", this.state }));
    }
    
    public synchronized void asyncRun(final Runnable runnable) {
        if (this.state == AsyncState.STARTING || this.state == AsyncState.STARTED) {
            ClassLoader oldCL;
            if (Constants.IS_SECURITY_ENABLED) {
                final PrivilegedAction<ClassLoader> pa = new PrivilegedGetTccl();
                oldCL = AccessController.doPrivileged(pa);
            }
            else {
                oldCL = Thread.currentThread().getContextClassLoader();
            }
            try {
                if (Constants.IS_SECURITY_ENABLED) {
                    final PrivilegedAction<Void> pa2 = new PrivilegedSetTccl(this.getClass().getClassLoader());
                    AccessController.doPrivileged(pa2);
                }
                else {
                    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                }
                this.processor.getExecutor().execute(runnable);
            }
            finally {
                if (Constants.IS_SECURITY_ENABLED) {
                    final PrivilegedAction<Void> pa3 = new PrivilegedSetTccl(oldCL);
                    AccessController.doPrivileged(pa3);
                }
                else {
                    Thread.currentThread().setContextClassLoader(oldCL);
                }
            }
            return;
        }
        throw new IllegalStateException(AsyncStateMachine.sm.getString("asyncStateMachine.invalidAsyncState", new Object[] { "asyncRun()", this.state }));
    }
    
    public synchronized void recycle() {
        this.asyncCtxt = null;
        this.state = AsyncState.DISPATCHED;
    }
    
    static {
        sm = StringManager.getManager("org.apache.coyote");
    }
    
    private enum AsyncState
    {
        DISPATCHED(false, false, false), 
        STARTING(true, true, false), 
        STARTED(true, true, false), 
        MUST_COMPLETE(true, false, false), 
        COMPLETING(true, false, false), 
        TIMING_OUT(true, false, false), 
        MUST_DISPATCH(true, true, true), 
        DISPATCHING(true, false, true), 
        ERROR(true, false, false);
        
        private boolean isAsync;
        private boolean isStarted;
        private boolean isDispatching;
        
        private AsyncState(final boolean isAsync, final boolean isStarted, final boolean isDispatching) {
            this.isAsync = isAsync;
            this.isStarted = isStarted;
            this.isDispatching = isDispatching;
        }
        
        public boolean isAsync() {
            return this.isAsync;
        }
        
        public boolean isStarted() {
            return this.isStarted;
        }
        
        public boolean isDispatching() {
            return this.isDispatching;
        }
    }
    
    private static class PrivilegedSetTccl implements PrivilegedAction<Void>
    {
        private ClassLoader cl;
        
        PrivilegedSetTccl(final ClassLoader cl) {
            this.cl = cl;
        }
        
        @Override
        public Void run() {
            Thread.currentThread().setContextClassLoader(this.cl);
            return null;
        }
    }
    
    private static class PrivilegedGetTccl implements PrivilegedAction<ClassLoader>
    {
        @Override
        public ClassLoader run() {
            return Thread.currentThread().getContextClassLoader();
        }
    }
}
