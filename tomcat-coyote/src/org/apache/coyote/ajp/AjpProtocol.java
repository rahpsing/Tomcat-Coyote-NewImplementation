// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.ajp;

import org.apache.coyote.AbstractProcessor;
import org.apache.coyote.Processor;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.coyote.AbstractProtocol;
import java.net.Socket;
import org.apache.juli.logging.LogFactory;
import org.apache.coyote.Adapter;
import org.apache.tomcat.util.net.JIoEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.juli.logging.Log;

public class AjpProtocol extends AbstractAjpProtocol
{
    private static final Log log;
    private AjpConnectionHandler cHandler;
    
    @Override
    protected Log getLog() {
        return AjpProtocol.log;
    }
    
    @Override
    protected AbstractEndpoint.Handler getHandler() {
        return this.cHandler;
    }
    
    public AjpProtocol() {
        this.endpoint = new JIoEndpoint();
        this.cHandler = new AjpConnectionHandler(this);
        ((JIoEndpoint)this.endpoint).setHandler(this.cHandler);
        this.setSoLinger(-1);
        this.setSoTimeout(-1);
        this.setTcpNoDelay(true);
    }
    
    @Override
    protected String getNamePrefix() {
        return "ajp-bio";
    }
    
    static {
        log = LogFactory.getLog((Class)AjpProtocol.class);
    }
    
    protected static class AjpConnectionHandler extends AbstractAjpConnectionHandler<Socket, AjpProcessor> implements JIoEndpoint.Handler
    {
        protected AjpProtocol proto;
        
        public AjpConnectionHandler(final AjpProtocol proto) {
            this.proto = proto;
        }
        
        @Override
        protected AbstractProtocol getProtocol() {
            return this.proto;
        }
        
        @Override
        protected Log getLog() {
            return AjpProtocol.log;
        }
        
        @Override
        public SSLImplementation getSslImplementation() {
            return null;
        }
        
        public void release(final SocketWrapper<Socket> socket, final Processor<Socket> processor, final boolean isSocketClosing, final boolean addToPoller) {
            processor.recycle(isSocketClosing);
            this.recycledProcessors.offer((Processor<S>)processor);
        }
        
        @Override
        protected AjpProcessor createProcessor() {
            final AjpProcessor processor = new AjpProcessor(this.proto.packetSize, (JIoEndpoint)this.proto.endpoint);
            processor.setAdapter(this.proto.adapter);
            processor.setTomcatAuthentication(this.proto.tomcatAuthentication);
            processor.setRequiredSecret(this.proto.requiredSecret);
            processor.setKeepAliveTimeout(this.proto.getKeepAliveTimeout());
            processor.setClientCertProvider(this.proto.getClientCertProvider());
            ((AbstractConnectionHandler<Socket, P>)this).register(processor);
            return processor;
        }
    }
}
