// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.ajp;

import java.io.IOException;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import org.apache.coyote.http11.upgrade.UpgradeInbound;
import org.apache.coyote.Processor;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.res.StringManager;
import org.apache.coyote.AbstractProtocol;

public abstract class AbstractAjpProtocol extends AbstractProtocol
{
    protected static final StringManager sm;
    protected boolean tomcatAuthentication;
    protected String requiredSecret;
    protected int packetSize;
    
    public AbstractAjpProtocol() {
        this.tomcatAuthentication = true;
        this.requiredSecret = null;
        this.packetSize = 8192;
    }
    
    @Override
    protected String getProtocolName() {
        return "Ajp";
    }
    
    public boolean getTomcatAuthentication() {
        return this.tomcatAuthentication;
    }
    
    public void setTomcatAuthentication(final boolean tomcatAuthentication) {
        this.tomcatAuthentication = tomcatAuthentication;
    }
    
    public void setRequiredSecret(final String requiredSecret) {
        this.requiredSecret = requiredSecret;
    }
    
    public int getPacketSize() {
        return this.packetSize;
    }
    
    public void setPacketSize(final int packetSize) {
        if (packetSize < 8192) {
            this.packetSize = 8192;
        }
        else {
            this.packetSize = packetSize;
        }
    }
    
    static {
        sm = StringManager.getManager("org.apache.coyote.ajp");
    }
    
    protected abstract static class AbstractAjpConnectionHandler<S, P extends AbstractAjpProcessor<S>> extends AbstractConnectionHandler<S, P>
    {
        @Override
        protected void initSsl(final SocketWrapper<S> socket, final Processor<S> processor) {
        }
        
        @Override
        protected void longPoll(final SocketWrapper<S> socket, final Processor<S> processor) {
            socket.setAsync(true);
        }
        
        @Deprecated
        @Override
        protected P createUpgradeProcessor(final SocketWrapper<S> socket, final UpgradeInbound inbound) {
            return null;
        }
        
        @Override
        protected P createUpgradeProcessor(final SocketWrapper<S> socket, final HttpUpgradeHandler httpUpgradeHandler) {
            return null;
        }
    }
}
