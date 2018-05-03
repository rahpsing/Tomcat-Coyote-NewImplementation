// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import org.apache.tomcat.util.net.SSLSupport;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import org.apache.coyote.http11.upgrade.UpgradeInbound;
import org.apache.tomcat.util.net.SocketStatus;
import java.io.IOException;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import java.util.concurrent.Executor;

public interface Processor<S>
{
    Executor getExecutor();
    
    AbstractEndpoint.Handler.SocketState process(final SocketWrapper<S> p0) throws IOException;
    
    AbstractEndpoint.Handler.SocketState event(final SocketStatus p0) throws IOException;
    
    AbstractEndpoint.Handler.SocketState asyncDispatch(final SocketStatus p0);
    
    AbstractEndpoint.Handler.SocketState asyncPostProcess();
    
    @Deprecated
    UpgradeInbound getUpgradeInbound();
    
    @Deprecated
    AbstractEndpoint.Handler.SocketState upgradeDispatch() throws IOException;
    
    HttpUpgradeHandler getHttpUpgradeHandler();
    
    AbstractEndpoint.Handler.SocketState upgradeDispatch(final SocketStatus p0) throws IOException;
    
    boolean isComet();
    
    boolean isAsync();
    
    boolean isUpgrade();
    
    Request getRequest();
    
    void recycle(final boolean p0);
    
    void setSslSupport(final SSLSupport p0);
}
