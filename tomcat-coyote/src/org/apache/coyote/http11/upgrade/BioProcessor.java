// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.juli.logging.Log;
import java.net.Socket;

public class BioProcessor extends AbstractProcessor<Socket>
{
    private static final Log log;
    private static final int INFINITE_TIMEOUT = 0;
    
    @Override
    protected Log getLog() {
        return BioProcessor.log;
    }
    
    public BioProcessor(final SocketWrapper<Socket> wrapper, final HttpUpgradeHandler httpUpgradeProcessor) throws IOException {
        super(httpUpgradeProcessor, new BioServletInputStream(wrapper), new BioServletOutputStream(wrapper));
        wrapper.getSocket().setSoTimeout(0);
    }
    
    static {
        log = LogFactory.getLog((Class)BioProcessor.class);
    }
}
