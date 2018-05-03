// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import org.apache.tomcat.util.net.SSLSupport;
import org.apache.coyote.Request;
import org.apache.tomcat.util.net.SocketWrapper;
import java.util.concurrent.Executor;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketStatus;
import java.io.IOException;
import org.apache.juli.logging.Log;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import org.apache.tomcat.util.res.StringManager;
import org.apache.coyote.http11.upgrade.servlet31.WebConnection;
import org.apache.coyote.Processor;

public abstract class AbstractProcessor<S> implements Processor<S>, WebConnection
{
    protected static final StringManager sm;
    private final HttpUpgradeHandler httpUpgradeHandler;
    private final AbstractServletInputStream upgradeServletInputStream;
    private final AbstractServletOutputStream upgradeServletOutputStream;
    
    protected abstract Log getLog();
    
    protected AbstractProcessor(final HttpUpgradeHandler httpUpgradeHandler, final AbstractServletInputStream upgradeServletInputStream, final AbstractServletOutputStream upgradeServletOutputStream) {
        this.httpUpgradeHandler = httpUpgradeHandler;
        this.upgradeServletInputStream = upgradeServletInputStream;
        this.upgradeServletOutputStream = upgradeServletOutputStream;
    }
    
    @Override
    public void close() throws Exception {
        this.upgradeServletInputStream.close();
        this.upgradeServletOutputStream.close();
    }
    
    @Override
    public AbstractServletInputStream getInputStream() throws IOException {
        return this.upgradeServletInputStream;
    }
    
    @Override
    public AbstractServletOutputStream getOutputStream() throws IOException {
        return this.upgradeServletOutputStream;
    }
    
    @Override
    public final boolean isUpgrade() {
        return true;
    }
    
    @Override
    public HttpUpgradeHandler getHttpUpgradeHandler() {
        return this.httpUpgradeHandler;
    }
    
    @Override
    public final AbstractEndpoint.Handler.SocketState upgradeDispatch(final SocketStatus status) throws IOException {
        if (status == SocketStatus.OPEN_READ) {
            this.upgradeServletInputStream.onDataAvailable();
        }
        else if (status == SocketStatus.OPEN_WRITE) {
            this.upgradeServletOutputStream.onWritePossible();
        }
        else {
            if (status == SocketStatus.STOP) {
                try {
                    this.upgradeServletInputStream.close();
                }
                catch (IOException ioe) {
                    this.getLog().debug((Object)AbstractProcessor.sm.getString("abstractProcessor.isCloseFail", new Object[] { ioe }));
                }
                try {
                    this.upgradeServletOutputStream.close();
                }
                catch (IOException ioe) {
                    this.getLog().debug((Object)AbstractProcessor.sm.getString("abstractProcessor.osCloseFail", new Object[] { ioe }));
                }
                return AbstractEndpoint.Handler.SocketState.CLOSED;
            }
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        if (this.upgradeServletInputStream.isCloseRequired() || this.upgradeServletOutputStream.isCloseRequired()) {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        return AbstractEndpoint.Handler.SocketState.UPGRADED;
    }
    
    @Override
    public final void recycle(final boolean socketClosing) {
    }
    
    @Deprecated
    @Override
    public UpgradeInbound getUpgradeInbound() {
        return null;
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState upgradeDispatch() throws IOException {
        return null;
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
