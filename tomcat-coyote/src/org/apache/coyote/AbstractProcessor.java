// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import org.apache.coyote.http11.upgrade.UpgradeInbound;
import org.apache.tomcat.util.net.SocketStatus;
import java.io.IOException;
import org.apache.tomcat.util.net.SocketWrapper;
import java.util.concurrent.Executor;
import org.apache.tomcat.util.net.AbstractEndpoint;

public abstract class AbstractProcessor<S> implements ActionHook, Processor<S>
{
    protected Adapter adapter;
    protected AsyncStateMachine<S> asyncStateMachine;
    protected AbstractEndpoint endpoint;
    protected Request request;
    protected Response response;
    
    protected AbstractProcessor() {
    }
    
    public AbstractProcessor(final AbstractEndpoint endpoint) {
        this.endpoint = endpoint;
        this.asyncStateMachine = new AsyncStateMachine<S>(this);
        this.request = new Request();
        (this.response = new Response()).setHook(this);
        this.request.setResponse(this.response);
    }
    
    protected AbstractEndpoint getEndpoint() {
        return this.endpoint;
    }
    
    @Override
    public Request getRequest() {
        return this.request;
    }
    
    public void setAdapter(final Adapter adapter) {
        this.adapter = adapter;
    }
    
    public Adapter getAdapter() {
        return this.adapter;
    }
    
    @Override
    public Executor getExecutor() {
        return this.endpoint.getExecutor();
    }
    
    @Override
    public boolean isAsync() {
        return this.asyncStateMachine != null && this.asyncStateMachine.isAsync();
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState asyncPostProcess() {
        return this.asyncStateMachine.asyncPostProcess();
    }
    
    @Override
    public abstract boolean isComet();
    
    @Override
    public abstract boolean isUpgrade();
    
    @Override
    public abstract AbstractEndpoint.Handler.SocketState process(final SocketWrapper<S> p0) throws IOException;
    
    @Override
    public abstract AbstractEndpoint.Handler.SocketState event(final SocketStatus p0) throws IOException;
    
    @Override
    public abstract AbstractEndpoint.Handler.SocketState asyncDispatch(final SocketStatus p0);
    
    @Override
    public abstract AbstractEndpoint.Handler.SocketState upgradeDispatch() throws IOException;
    
    @Deprecated
    @Override
    public abstract UpgradeInbound getUpgradeInbound();
}
