// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import org.apache.tomcat.util.net.SocketStatus;

public interface Adapter
{
    void service(final Request p0, final Response p1) throws Exception;
    
    boolean event(final Request p0, final Response p1, final SocketStatus p2) throws Exception;
    
    boolean asyncDispatch(final Request p0, final Response p1, final SocketStatus p2) throws Exception;
    
    void log(final Request p0, final Response p1, final long p2);
    
    String getDomain();
}
