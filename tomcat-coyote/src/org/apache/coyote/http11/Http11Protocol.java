// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.coyote.http11.upgrade.BioProcessor;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import java.io.IOException;
import org.apache.coyote.http11.upgrade.UpgradeBioProcessor;
import org.apache.coyote.http11.upgrade.UpgradeInbound;
import org.apache.coyote.AbstractProcessor;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.coyote.Processor;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.net.SSLImplementation;
import java.net.Socket;
import org.apache.coyote.AbstractProtocol;
import org.apache.juli.logging.LogFactory;
import org.apache.coyote.Adapter;
import org.apache.tomcat.util.net.JIoEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.juli.logging.Log;

public class Http11Protocol extends AbstractHttp11JsseProtocol
{
    private static final Log log;
    protected Http11ConnectionHandler cHandler;
    private int disableKeepAlivePercentage;
    
    @Override
    protected Log getLog() {
        return Http11Protocol.log;
    }
    
    @Override
    protected AbstractEndpoint.Handler getHandler() {
        return this.cHandler;
    }
    
    public Http11Protocol() {
        this.disableKeepAlivePercentage = 75;
        this.endpoint = new JIoEndpoint();
        this.cHandler = new Http11ConnectionHandler(this);
        ((JIoEndpoint)this.endpoint).setHandler(this.cHandler);
        this.setSoLinger(-1);
        this.setSoTimeout(60000);
        this.setTcpNoDelay(true);
    }
    
    public int getDisableKeepAlivePercentage() {
        return this.disableKeepAlivePercentage;
    }
    
    public void setDisableKeepAlivePercentage(final int disableKeepAlivePercentage) {
        if (disableKeepAlivePercentage < 0) {
            this.disableKeepAlivePercentage = 0;
        }
        else if (disableKeepAlivePercentage > 100) {
            this.disableKeepAlivePercentage = 100;
        }
        else {
            this.disableKeepAlivePercentage = disableKeepAlivePercentage;
        }
    }
    
    @Override
    protected String getNamePrefix() {
        return "http-bio";
    }
    
    static {
        log = LogFactory.getLog((Class)Http11Protocol.class);
    }
    
    protected static class Http11ConnectionHandler extends AbstractConnectionHandler<Socket, Http11Processor> implements JIoEndpoint.Handler
    {
        protected Http11Protocol proto;
        
        Http11ConnectionHandler(final Http11Protocol proto) {
            this.proto = proto;
        }
        
        @Override
        protected AbstractProtocol getProtocol() {
            return this.proto;
        }
        
        @Override
        protected Log getLog() {
            return Http11Protocol.log;
        }
        
        @Override
        public SSLImplementation getSslImplementation() {
            return this.proto.sslImplementation;
        }
        
        public void release(final SocketWrapper<Socket> socket, final Processor<Socket> processor, final boolean isSocketClosing, final boolean addToPoller) {
            processor.recycle(isSocketClosing);
            this.recycledProcessors.offer((Processor<S>)processor);
        }
        
        @Override
        protected void initSsl(final SocketWrapper<Socket> socket, final Processor<Socket> processor) {
            if (this.proto.isSSLEnabled() && this.proto.sslImplementation != null) {
                processor.setSslSupport(this.proto.sslImplementation.getSSLSupport(socket.getSocket()));
            }
            else {
                processor.setSslSupport(null);
            }
        }
        
        @Override
        protected void longPoll(final SocketWrapper<Socket> socket, final Processor<Socket> processor) {
        }
        
        @Override
        protected Http11Processor createProcessor() {
            final Http11Processor processor = new Http11Processor(this.proto.getMaxHttpHeaderSize(), (JIoEndpoint)this.proto.endpoint, this.proto.getMaxTrailerSize(), this.proto.getMaxExtensionSize());
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
            processor.setDisableKeepAlivePercentage(this.proto.getDisableKeepAlivePercentage());
            ((AbstractConnectionHandler<Socket, P>)this).register(processor);
            return processor;
        }
        
        @Deprecated
        @Override
        protected Processor<Socket> createUpgradeProcessor(final SocketWrapper<Socket> socket, final UpgradeInbound inbound) throws IOException {
            return new UpgradeBioProcessor(socket, inbound);
        }
        
        @Override
        protected Processor<Socket> createUpgradeProcessor(final SocketWrapper<Socket> socket, final HttpUpgradeHandler httpUpgradeProcessor) throws IOException {
            return new BioProcessor(socket, httpUpgradeProcessor);
        }
    }
}
