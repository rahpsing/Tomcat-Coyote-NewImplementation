// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.io.IOException;
import org.apache.tomcat.util.net.AbstractEndpoint;

@Deprecated
public interface UpgradeInbound
{
    void setUpgradeProcessor(final UpgradeProcessor<?> p0);
    
    void onUpgradeComplete();
    
    AbstractEndpoint.Handler.SocketState onData() throws IOException;
    
    void setUpgradeOutbound(final UpgradeOutbound p0);
    
    int getReadTimeout();
}
