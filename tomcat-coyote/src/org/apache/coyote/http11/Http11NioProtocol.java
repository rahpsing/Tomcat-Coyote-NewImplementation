// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.coyote.http11.upgrade.NioProcessor;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import java.io.IOException;
import org.apache.coyote.http11.upgrade.UpgradeNioProcessor;
import org.apache.coyote.http11.upgrade.UpgradeInbound;
import org.apache.coyote.AbstractProcessor;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SecureNioChannel;
import org.apache.tomcat.util.net.SocketWrapper;
import java.util.Iterator;
import org.apache.coyote.Processor;
import java.util.Map;
import java.nio.channels.SocketChannel;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.coyote.AbstractProtocol;
import org.apache.juli.logging.LogFactory;
import org.apache.coyote.Adapter;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.juli.logging.Log;

public class Http11NioProtocol extends AbstractHttp11JsseProtocol
{
    private static final Log log;
    private Http11ConnectionHandler cHandler;
    
    @Override
    protected Log getLog() {
        return Http11NioProtocol.log;
    }
    
    @Override
    protected AbstractEndpoint.Handler getHandler() {
        return this.cHandler;
    }
    
    public Http11NioProtocol() {
        this.endpoint = new NioEndpoint();
        this.cHandler = new Http11ConnectionHandler(this);
        ((NioEndpoint)this.endpoint).setHandler(this.cHandler);
        this.setSoLinger(-1);
        this.setSoTimeout(60000);
        this.setTcpNoDelay(true);
    }
    
    public NioEndpoint getEndpoint() {
        return (NioEndpoint)this.endpoint;
    }
    
    public void setPollerThreadCount(final int count) {
        ((NioEndpoint)this.endpoint).setPollerThreadCount(count);
    }
    
    public int getPollerThreadCount() {
        return ((NioEndpoint)this.endpoint).getPollerThreadCount();
    }
    
    public void setSelectorTimeout(final long timeout) {
        ((NioEndpoint)this.endpoint).setSelectorTimeout(timeout);
    }
    
    public long getSelectorTimeout() {
        return ((NioEndpoint)this.endpoint).getSelectorTimeout();
    }
    
    public void setAcceptorThreadPriority(final int threadPriority) {
        ((NioEndpoint)this.endpoint).setAcceptorThreadPriority(threadPriority);
    }
    
    public void setPollerThreadPriority(final int threadPriority) {
        ((NioEndpoint)this.endpoint).setPollerThreadPriority(threadPriority);
    }
    
    public int getAcceptorThreadPriority() {
        return ((NioEndpoint)this.endpoint).getAcceptorThreadPriority();
    }
    
    public int getPollerThreadPriority() {
        return ((NioEndpoint)this.endpoint).getThreadPriority();
    }
    
    public boolean getUseSendfile() {
        return ((NioEndpoint)this.endpoint).getUseSendfile();
    }
    
    public void setUseSendfile(final boolean useSendfile) {
        ((NioEndpoint)this.endpoint).setUseSendfile(useSendfile);
    }
    
    public void setOomParachute(final int oomParachute) {
        ((NioEndpoint)this.endpoint).setOomParachute(oomParachute);
    }
    
    @Override
    protected String getNamePrefix() {
        return "http-nio";
    }
    
    static {
        log = LogFactory.getLog((Class)Http11NioProtocol.class);
    }
    
    protected static class Http11ConnectionHandler extends AbstractConnectionHandler<NioChannel, Http11NioProcessor> implements NioEndpoint.Handler
    {
        protected Http11NioProtocol proto;
        
        Http11ConnectionHandler(final Http11NioProtocol proto) {
            this.proto = proto;
        }
        
        @Override
        protected AbstractProtocol getProtocol() {
            return this.proto;
        }
        
        @Override
        protected Log getLog() {
            return Http11NioProtocol.log;
        }
        
        @Override
        public SSLImplementation getSslImplementation() {
            return this.proto.sslImplementation;
        }
        
        @Override
        public void release(final SocketChannel socket) {
            if (Http11NioProtocol.log.isDebugEnabled()) {
                Http11NioProtocol.log.debug((Object)("Iterating through our connections to release a socket channel:" + socket));
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
            if (Http11NioProtocol.log.isDebugEnabled()) {
                Http11NioProtocol.log.debug((Object)("Done iterating through our connections to release a socket channel:" + socket + " released:" + released));
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
        protected void initSsl(final SocketWrapper<NioChannel> socket, final Processor<NioChannel> processor) {
            if (this.proto.isSSLEnabled() && this.proto.sslImplementation != null && socket.getSocket() instanceof SecureNioChannel) {
                final SecureNioChannel ch = socket.getSocket();
                processor.setSslSupport(this.proto.sslImplementation.getSSLSupport(ch.getSslEngine().getSession()));
            }
            else {
                processor.setSslSupport(null);
            }
        }
        
        @Override
        protected void longPoll(final SocketWrapper<NioChannel> socket, final Processor<NioChannel> processor) {
            if (processor.isAsync()) {
                socket.setAsync(true);
            }
            else {
                socket.getSocket().getPoller().add(socket.getSocket());
            }
        }
        
        public Http11NioProcessor createProcessor() {
            final Http11NioProcessor processor = new Http11NioProcessor(this.proto.getMaxHttpHeaderSize(), (NioEndpoint)this.proto.endpoint, this.proto.getMaxTrailerSize(), this.proto.getMaxExtensionSize());
            processor.setAdapter(this.proto.adapter);
            processor.setMaxKeepAliveRequests(this.proto.getMaxKeepAliveRequests());
            processor.setKeepAliveTimeout(this.proto.getKeepAliveTimeout());
            processor.setConnectionUploadTimeout(this.proto.getConnectionUploadTimeout());
            processor.setDisableUploadTimeout(this.proto.getDisableUploadTimeout());
            processor.setCompressionMinSize(this.proto.getCompressionMinSize());
            processor.setCompression(this.proto.getCompression());
            processor.setNoCompressionUserAgents(this.proto.getNoCompressionUserAgents());
            processor.setCompressableMimeTypes(this.proto.getCompressableMimeTypes());
            processor.setRestrictedUserAgents(this.proto.getRestrictedUserAgents());
            processor.setSocketBuffer(this.proto.getSocketBuffer());
            processor.setMaxSavePostSize(this.proto.getMaxSavePostSize());
            processor.setServer(this.proto.getServer());
            ((AbstractConnectionHandler<NioChannel, P>)this).register(processor);
            return processor;
        }
        
        @Deprecated
        @Override
        protected Processor<NioChannel> createUpgradeProcessor(final SocketWrapper<NioChannel> socket, final UpgradeInbound inbound) throws IOException {
            return new UpgradeNioProcessor(socket, inbound, ((Http11NioProtocol)this.getProtocol()).getEndpoint().getSelectorPool());
        }
        
        @Override
        protected Processor<NioChannel> createUpgradeProcessor(final SocketWrapper<NioChannel> socket, final HttpUpgradeHandler httpUpgradeProcessor) throws IOException {
            return new NioProcessor(socket, httpUpgradeProcessor, ((Http11NioProtocol)this.getProtocol()).getEndpoint().getSelectorPool());
        }
    }
}
