// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.NioSelectorPool;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.NioChannel;

public class NioProcessor extends AbstractProcessor<NioChannel>
{
    private static final Log log;
    private static final int INFINITE_TIMEOUT = -1;
    
    @Override
    protected Log getLog() {
        return NioProcessor.log;
    }
    
    public NioProcessor(final SocketWrapper<NioChannel> wrapper, final HttpUpgradeHandler httpUpgradeProcessor, final NioSelectorPool pool) {
        super(httpUpgradeProcessor, new NioServletInputStream(wrapper, pool), new NioServletOutputStream(wrapper, pool));
        wrapper.setTimeout(-1L);
    }
    
    static {
        log = LogFactory.getLog((Class)NioProcessor.class);
    }
}
