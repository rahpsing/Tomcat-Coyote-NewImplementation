// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.ajp;

import org.apache.coyote.AbstractProcessor;
import org.apache.tomcat.util.net.SocketWrapper;
import java.util.Iterator;
import org.apache.coyote.Processor;
import java.util.Map;
import java.nio.channels.SocketChannel;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.coyote.AbstractProtocol;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.juli.logging.LogFactory;
import org.apache.coyote.Adapter;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.juli.logging.Log;

public class AjpNioProtocol extends AbstractAjpProtocol
{
    private static final Log log;
    private AjpConnectionHandler cHandler;
    
    @Override
    protected Log getLog() {
        return AjpNioProtocol.log;
    }
    
    @Override
    protected AbstractEndpoint.Handler getHandler() {
        return this.cHandler;
    }
    
    public AjpNioProtocol() {
        this.endpoint = new NioEndpoint();
        this.cHandler = new AjpConnectionHandler(this);
        ((NioEndpoint)this.endpoint).setHandler(this.cHandler);
        this.setSoLinger(-1);
        this.setSoTimeout(-1);
        this.setTcpNoDelay(true);
        ((NioEndpoint)this.endpoint).setUseSendfile(false);
    }
    
    @Override
    protected String getNamePrefix() {
        return "ajp-nio";
    }
    
    static {
        log = LogFactory.getLog((Class)AjpNioProtocol.class);
    }
    
    protected static class AjpConnectionHandler extends AbstractAjpConnectionHandler<NioChannel, AjpNioProcessor> implements NioEndpoint.Handler
    {
        protected AjpNioProtocol proto;
        
        public AjpConnectionHandler(final AjpNioProtocol proto) {
            this.proto = proto;
        }
        
        @Override
        protected AbstractProtocol getProtocol() {
            return this.proto;
        }
        
        @Override
        protected Log getLog() {
            return AjpNioProtocol.log;
        }
        
        @Override
        public SSLImplementation getSslImplementation() {
            return null;
        }
        
        @Override
        public void release(final SocketChannel socket) {
            if (AjpNioProtocol.log.isDebugEnabled()) {
                AjpNioProtocol.log.debug((Object)("Iterating through our connections to release a socket channel:" + socket));
            }
            boolean released = false;
            final Iterator<Map.Entry<NioChannel, Processor<NioChannel>>> it = (Iterator<Map.Entry<NioChannel, Processor<NioChannel>>>)this.connections.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<NioChannel, Processor<NioChannel>> entry = it.next();
                if (entry.getKey().getIOChannel() == socket) {
                    it.remove();
                    final Processor<NioChannel> result = entry.getValue();
                    result.recycle(true);
                    ((AbstractConnectionHandler<NioChannel, P>)this).unregister(result);
                    released = true;
                    break;
                }
            }
            if (AjpNioProtocol.log.isDebugEnabled()) {
                AjpNioProtocol.log.debug((Object)("Done iterating through our connections to release a socket channel:" + socket + " released:" + released));
            }
        }
        
        @Override
        public void release(final SocketWrapper<NioChannel> socket) {
            final Processor<NioChannel> processor = (Processor<NioChannel>)this.connections.remove(socket.getSocket());
            if (processor != null) {
                processor.recycle(true);
                this.recycledProcessors.offer((Processor<S>)processor);
            }
        }
        
        public void release(final SocketWrapper<NioChannel> socket, final Processor<NioChannel> processor, final boolean isSocketClosing, final boolean addToPoller) {
            processor.recycle(isSocketClosing);
            this.recycledProcessors.offer((Processor<S>)processor);
            if (addToPoller) {
                socket.getSocket().getPoller().add(socket.getSocket());
            }
        }
        
        @Override
        protected AjpNioProcessor createProcessor() {
            final AjpNioProcessor processor = new AjpNioProcessor(this.proto.packetSize, (NioEndpoint)this.proto.endpoint);
            processor.setAdapter(this.proto.adapter);
            processor.setTomcatAuthentication(this.proto.tomcatAuthentication);
            processor.setRequiredSecret(this.proto.requiredSecret);
            processor.setClientCertProvider(this.proto.getClientCertProvider());
            ((AbstractConnectionHandler<NioChannel, P>)this).register(processor);
            return processor;
        }
    }
}
