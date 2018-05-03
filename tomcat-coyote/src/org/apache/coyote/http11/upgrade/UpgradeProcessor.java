// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import org.apache.tomcat.util.net.SSLSupport;
import org.apache.coyote.Request;
import org.apache.tomcat.util.net.SocketWrapper;
import java.util.concurrent.Executor;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import org.apache.tomcat.util.net.AbstractEndpoint;
import java.io.IOException;
import org.apache.tomcat.util.res.StringManager;
import org.apache.coyote.Processor;

@Deprecated
public abstract class UpgradeProcessor<S> implements Processor<S>
{
    protected static final StringManager sm;
    private final UpgradeInbound upgradeInbound;
    
    protected UpgradeProcessor(final UpgradeInbound upgradeInbound) {
        (this.upgradeInbound = upgradeInbound).setUpgradeProcessor(this);
        upgradeInbound.setUpgradeOutbound(new UpgradeOutbound(this));
    }
    
    public abstract void flush() throws IOException;
    
    public abstract void write(final int p0) throws IOException;
    
    public abstract void write(final byte[] p0, final int p1, final int p2) throws IOException;
    
    public abstract int read() throws IOException;
    
    public abstract int read(final boolean p0, final byte[] p1, final int p2, final int p3) throws IOException;
    
    @Override
    public final UpgradeInbound getUpgradeInbound() {
        return this.upgradeInbound;
    }
    
    @Override
    public final AbstractEndpoint.Handler.SocketState upgradeDispatch() throws IOException {
        return this.upgradeInbound.onData();
    }
    
    @Override
    public final void recycle(final boolean socketClosing) {
    }
    
    @Override
    public HttpUpgradeHandler getHttpUpgradeHandler() {
        return null;
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState upgradeDispatch(final SocketStatus status) throws IOException {
        return null;
    }
    
    @Override
    public boolean isUpgrade() {
        return false;
    }
    
    @Override
    public final Executor getExecutor() {
        return null;
    }
    
    @Override
    public final AbstractEndpoint.Handler.SocketState process(final SocketWrapper<S> socketWrapper) throws IOException {
        return null;
    }
    
    @Override
    public final AbstractEndpoint.Handler.SocketState event(final SocketStatus status) throws IOException {
        return null;
    }
    
    @Override
    public final AbstractEndpoint.Handler.SocketState asyncDispatch(final SocketStatus status) {
        return null;
    }
    
    @Override
    public final AbstractEndpoint.Handler.SocketState asyncPostProcess() {
        return null;
    }
    
    @Override
    public final boolean isComet() {
        return false;
    }
    
    @Override
    public final boolean isAsync() {
        return false;
    }
    
    @Override
    public final Request getRequest() {
        return null;
    }
    
    @Override
    public final void setSslSupport(final SSLSupport sslSupport) {
    }
    
    static {
        sm = StringManager.getManager("org.apache.coyote.http11.upgrade");
    }
}
