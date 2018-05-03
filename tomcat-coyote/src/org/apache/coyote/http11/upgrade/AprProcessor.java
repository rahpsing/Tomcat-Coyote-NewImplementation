// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jni.Socket;
import org.apache.tomcat.util.net.AprEndpoint;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.juli.logging.Log;

public class AprProcessor extends AbstractProcessor<Long>
{
    private static final Log log;
    private static final int INFINITE_TIMEOUT = -1;
    
    @Override
    protected Log getLog() {
        return AprProcessor.log;
    }
    
    public AprProcessor(final SocketWrapper<Long> wrapper, final HttpUpgradeHandler httpUpgradeProcessor, final AprEndpoint endpoint) {
        super(httpUpgradeProcessor, new AprServletInputStream(wrapper), new AprServletOutputStream(wrapper, endpoint));
        Socket.timeoutSet(wrapper.getSocket(), -1L);
    }
    
    static {
        log = LogFactory.getLog((Class)AprProcessor.class);
    }
}
