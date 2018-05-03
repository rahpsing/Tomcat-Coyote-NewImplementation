// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.coyote.http11.upgrade.AprProcessor;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import java.io.IOException;
import org.apache.coyote.http11.upgrade.UpgradeAprProcessor;
import org.apache.coyote.http11.upgrade.UpgradeInbound;
import org.apache.coyote.AbstractProcessor;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.coyote.Processor;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.coyote.AbstractProtocol;
import org.apache.juli.logging.LogFactory;
import org.apache.coyote.Adapter;
import org.apache.tomcat.util.net.AprEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.juli.logging.Log;

public class Http11AprProtocol extends AbstractHttp11Protocol
{
    private static final Log log;
    private final Http11ConnectionHandler cHandler;
    
    @Override
    protected Log getLog() {
        return Http11AprProtocol.log;
    }
    
    @Override
    protected AbstractEndpoint.Handler getHandler() {
        return this.cHandler;
    }
    
    @Override
    public boolean isAprRequired() {
        return true;
    }
    
    public Http11AprProtocol() {
        this.endpoint = new AprEndpoint();
        this.cHandler = new Http11ConnectionHandler(this);
        ((AprEndpoint)this.endpoint).setHandler(this.cHandler);
        this.setSoLinger(-1);
        this.setSoTimeout(60000);
        this.setTcpNoDelay(true);
    }
    
    public boolean getUseSendfile() {
        return ((AprEndpoint)this.endpoint).getUseSendfile();
    }
    
    public void setUseSendfile(final boolean useSendfile) {
        ((AprEndpoint)this.endpoint).setUseSendfile(useSendfile);
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
    
    public int getSendfileSize() {
        return ((AprEndpoint)this.endpoint).getSendfileSize();
    }
    
    public void setSendfileSize(final int sendfileSize) {
        ((AprEndpoint)this.endpoint).setSendfileSize(sendfileSize);
    }
    
    public void setSendfileThreadCount(final int sendfileThreadCount) {
        ((AprEndpoint)this.endpoint).setSendfileThreadCount(sendfileThreadCount);
    }
    
    public int getSendfileThreadCount() {
        return ((AprEndpoint)this.endpoint).getSendfileThreadCount();
    }
    
    public boolean getDeferAccept() {
        return ((AprEndpoint)this.endpoint).getDeferAccept();
    }
    
    public void setDeferAccept(final boolean deferAccept) {
        ((AprEndpoint)this.endpoint).setDeferAccept(deferAccept);
    }
    
    public String getSSLProtocol() {
        return ((AprEndpoint)this.endpoint).getSSLProtocol();
    }
    
    public void setSSLProtocol(final String SSLProtocol) {
        ((AprEndpoint)this.endpoint).setSSLProtocol(SSLProtocol);
    }
    
    public String getSSLPassword() {
        return ((AprEndpoint)this.endpoint).getSSLPassword();
    }
    
    public void setSSLPassword(final String SSLPassword) {
        ((AprEndpoint)this.endpoint).setSSLPassword(SSLPassword);
    }
    
    public String getSSLCipherSuite() {
        return ((AprEndpoint)this.endpoint).getSSLCipherSuite();
    }
    
    public void setSSLCipherSuite(final String SSLCipherSuite) {
        ((AprEndpoint)this.endpoint).setSSLCipherSuite(SSLCipherSuite);
    }
    
    public boolean getSSLHonorCipherOrder() {
        return ((AprEndpoint)this.endpoint).getSSLHonorCipherOrder();
    }
    
    public void setSSLHonorCipherOrder(final boolean SSLHonorCipherOrder) {
        ((AprEndpoint)this.endpoint).setSSLHonorCipherOrder(SSLHonorCipherOrder);
    }
    
    public String getSSLCertificateFile() {
        return ((AprEndpoint)this.endpoint).getSSLCertificateFile();
    }
    
    public void setSSLCertificateFile(final String SSLCertificateFile) {
        ((AprEndpoint)this.endpoint).setSSLCertificateFile(SSLCertificateFile);
    }
    
    public String getSSLCertificateKeyFile() {
        return ((AprEndpoint)this.endpoint).getSSLCertificateKeyFile();
    }
    
    public void setSSLCertificateKeyFile(final String SSLCertificateKeyFile) {
        ((AprEndpoint)this.endpoint).setSSLCertificateKeyFile(SSLCertificateKeyFile);
    }
    
    public String getSSLCertificateChainFile() {
        return ((AprEndpoint)this.endpoint).getSSLCertificateChainFile();
    }
    
    public void setSSLCertificateChainFile(final String SSLCertificateChainFile) {
        ((AprEndpoint)this.endpoint).setSSLCertificateChainFile(SSLCertificateChainFile);
    }
    
    public String getSSLCACertificatePath() {
        return ((AprEndpoint)this.endpoint).getSSLCACertificatePath();
    }
    
    public void setSSLCACertificatePath(final String SSLCACertificatePath) {
        ((AprEndpoint)this.endpoint).setSSLCACertificatePath(SSLCACertificatePath);
    }
    
    public String getSSLCACertificateFile() {
        return ((AprEndpoint)this.endpoint).getSSLCACertificateFile();
    }
    
    public void setSSLCACertificateFile(final String SSLCACertificateFile) {
        ((AprEndpoint)this.endpoint).setSSLCACertificateFile(SSLCACertificateFile);
    }
    
    public String getSSLCARevocationPath() {
        return ((AprEndpoint)this.endpoint).getSSLCARevocationPath();
    }
    
    public void setSSLCARevocationPath(final String SSLCARevocationPath) {
        ((AprEndpoint)this.endpoint).setSSLCARevocationPath(SSLCARevocationPath);
    }
    
    public String getSSLCARevocationFile() {
        return ((AprEndpoint)this.endpoint).getSSLCARevocationFile();
    }
    
    public void setSSLCARevocationFile(final String SSLCARevocationFile) {
        ((AprEndpoint)this.endpoint).setSSLCARevocationFile(SSLCARevocationFile);
    }
    
    public String getSSLVerifyClient() {
        return ((AprEndpoint)this.endpoint).getSSLVerifyClient();
    }
    
    public void setSSLVerifyClient(final String SSLVerifyClient) {
        ((AprEndpoint)this.endpoint).setSSLVerifyClient(SSLVerifyClient);
    }
    
    public int getSSLVerifyDepth() {
        return ((AprEndpoint)this.endpoint).getSSLVerifyDepth();
    }
    
    public void setSSLVerifyDepth(final int SSLVerifyDepth) {
        ((AprEndpoint)this.endpoint).setSSLVerifyDepth(SSLVerifyDepth);
    }
    
    public boolean getSSLDisableCompression() {
        return ((AprEndpoint)this.endpoint).getSSLDisableCompression();
    }
    
    public void setSSLDisableCompression(final boolean disable) {
        ((AprEndpoint)this.endpoint).setSSLDisableCompression(disable);
    }
    
    @Override
    protected String getNamePrefix() {
        return "http-apr";
    }
    
    static {
        log = LogFactory.getLog((Class)Http11AprProtocol.class);
    }
    
    protected static class Http11ConnectionHandler extends AbstractConnectionHandler<Long, Http11AprProcessor> implements AprEndpoint.Handler
    {
        protected Http11AprProtocol proto;
        
        Http11ConnectionHandler(final Http11AprProtocol proto) {
            this.proto = proto;
        }
        
        @Override
        protected AbstractProtocol getProtocol() {
            return this.proto;
        }
        
        @Override
        protected Log getLog() {
            return Http11AprProtocol.log;
        }
        
        @Override
        public void recycle() {
            this.recycledProcessors.clear();
        }
        
        public void release(final SocketWrapper<Long> socket, final Processor<Long> processor, final boolean isSocketClosing, final boolean addToPoller) {
            processor.recycle(isSocketClosing);
            this.recycledProcessors.offer((Processor<S>)processor);
            if (addToPoller && this.proto.endpoint.isRunning()) {
                ((AprEndpoint)this.proto.endpoint).getPoller().add(socket.getSocket(), this.proto.endpoint.getKeepAliveTimeout(), true, false);
            }
        }
        
        @Override
        protected void initSsl(final SocketWrapper<Long> socket, final Processor<Long> processor) {
        }
        
        @Override
        protected void longPoll(final SocketWrapper<Long> socket, final Processor<Long> processor) {
            if (processor.isAsync()) {
                socket.setAsync(true);
            }
            else if (processor.isComet()) {
                if (this.proto.endpoint.isRunning()) {
                    socket.setComet(true);
                    ((AprEndpoint)this.proto.endpoint).getPoller().add(socket.getSocket(), this.proto.endpoint.getSoTimeout(), true, false);
                }
                else {
                    ((AprEndpoint)this.proto.endpoint).processSocket(socket.getSocket(), SocketStatus.STOP);
                }
            }
            else if (processor.isUpgrade()) {
                final AprEndpoint.Poller p = ((AprEndpoint)this.proto.endpoint).getPoller();
                if (p == null) {
                    this.release(socket, processor, true, false);
                }
                else {
                    p.add(socket.getSocket(), -1, true, false);
                }
            }
            else {
                ((AprEndpoint)this.proto.endpoint).getPoller().add(socket.getSocket(), processor.getUpgradeInbound().getReadTimeout(), true, false);
            }
        }
        
        @Override
        protected Http11AprProcessor createProcessor() {
            final Http11AprProcessor processor = new Http11AprProcessor(this.proto.getMaxHttpHeaderSize(), (AprEndpoint)this.proto.endpoint, this.proto.getMaxTrailerSize(), this.proto.getMaxExtensionSize());
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
            processor.setClientCertProvider(this.proto.getClientCertProvider());
            ((AbstractConnectionHandler<Long, P>)this).register(processor);
            return processor;
        }
        
        @Deprecated
        @Override
        protected Processor<Long> createUpgradeProcessor(final SocketWrapper<Long> socket, final UpgradeInbound inbound) throws IOException {
            return new UpgradeAprProcessor(socket, inbound);
        }
        
        @Override
        protected Processor<Long> createUpgradeProcessor(final SocketWrapper<Long> socket, final HttpUpgradeHandler httpUpgradeProcessor) throws IOException {
            return new AprProcessor(socket, httpUpgradeProcessor, (AprEndpoint)this.proto.endpoint);
        }
    }
}
