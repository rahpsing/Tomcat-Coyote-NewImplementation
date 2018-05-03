// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.ajp;

import org.apache.coyote.AbstractProcessor;
import org.apache.coyote.Processor;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.coyote.AbstractProtocol;
import org.apache.juli.logging.LogFactory;
import org.apache.coyote.Adapter;
import org.apache.tomcat.util.net.AprEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.juli.logging.Log;

public class AjpAprProtocol extends AbstractAjpProtocol
{
    private static final Log log;
    private AjpConnectionHandler cHandler;
    
    @Override
    protected Log getLog() {
        return AjpAprProtocol.log;
    }
    
    @Override
    protected AbstractEndpoint.Handler getHandler() {
        return this.cHandler;
    }
    
    @Override
    public boolean isAprRequired() {
        return true;
    }
    
    public AjpAprProtocol() {
        this.endpoint = new AprEndpoint();
        this.cHandler = new AjpConnectionHandler(this);
        ((AprEndpoint)this.endpoint).setHandler(this.cHandler);
        this.setSoLinger(-1);
        this.setSoTimeout(-1);
        this.setTcpNoDelay(true);
        ((AprEndpoint)this.endpoint).setUseSendfile(false);
    }
    
    public int getPollTime() {
        return ((AprEndpoint)this.endpoint).getPollTime();
    }
    
    public void setPollTime(final int pollTime) {
        ((AprEndpoint)this.endpoint).setPollTime(pollTime);
    }
    
    public void setPollerSize(final int pollerSize) {
        this.endpoint.setMaxConnections(pollerSize);
    }
    
    public int getPollerSize() {
        return this.endpoint.getMaxConnections();
    }
    
    @Override
    protected String getNamePrefix() {
        return "ajp-apr";
    }
    
    static {
        log = LogFactory.getLog((Class)AjpAprProtocol.class);
    }
    
    protected static class AjpConnectionHandler extends AbstractAjpConnectionHandler<Long, AjpAprProcessor> implements AprEndpoint.Handler
    {
        protected AjpAprProtocol proto;
        
        public AjpConnectionHandler(final AjpAprProtocol proto) {
            this.proto = proto;
        }
        
        @Override
        protected AbstractProtocol getProtocol() {
            return this.proto;
        }
        
        @Override
        protected Log getLog() {
            return AjpAprProtocol.log;
        }
        
        public void release(final SocketWrapper<Long> socket, final Processor<Long> processor, final boolean isSocketClosing, final boolean addToPoller) {
            processor.recycle(isSocketClosing);
            this.recycledProcessors.offer((Processor<S>)processor);
            if (addToPoller) {
                ((AprEndpoint)this.proto.endpoint).getPoller().add(socket.getSocket(), this.proto.endpoint.getKeepAliveTimeout(), true, false);
            }
        }
        
        @Override
        protected AjpAprProcessor createProcessor() {
            final AjpAprProcessor processor = new AjpAprProcessor(this.proto.packetSize, (AprEndpoint)this.proto.endpoint);
            processor.setAdapter(this.proto.adapter);
            processor.setTomcatAuthentication(this.proto.tomcatAuthentication);
            processor.setRequiredSecret(this.proto.requiredSecret);
            processor.setClientCertProvider(this.proto.getClientCertProvider());
            ((AbstractConnectionHandler<Long, P>)this).register(processor);
            return processor;
        }
    }
}
