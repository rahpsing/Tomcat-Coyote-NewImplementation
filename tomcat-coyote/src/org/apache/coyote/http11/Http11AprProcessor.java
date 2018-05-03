// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jni.Sockaddr;
import org.apache.coyote.http11.filters.BufferedInputFilter;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.apache.tomcat.jni.SSLSocket;
import org.apache.tomcat.jni.Address;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.jni.Socket;
import java.io.IOException;
import org.apache.coyote.RequestInfo;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.InterruptedIOException;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.InputBuffer;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.net.AprEndpoint;
import org.apache.juli.logging.Log;

public class Http11AprProcessor extends AbstractHttp11Processor<Long>
{
    private static final Log log;
    protected InternalAprInputBuffer inputBuffer;
    protected InternalAprOutputBuffer outputBuffer;
    protected AprEndpoint.SendfileData sendfileData;
    protected SocketWrapper<Long> socket;
    protected String clientCertProvider;
    
    @Override
    protected Log getLog() {
        return Http11AprProcessor.log;
    }
    
    public Http11AprProcessor(final int headerBufferSize, final AprEndpoint endpoint, final int maxTrailerSize, final int maxExtensionSize) {
        super(endpoint);
        this.inputBuffer = null;
        this.outputBuffer = null;
        this.sendfileData = null;
        this.socket = null;
        this.clientCertProvider = null;
        this.inputBuffer = new InternalAprInputBuffer(this.request, headerBufferSize);
        this.request.setInputBuffer(this.inputBuffer);
        this.outputBuffer = new InternalAprOutputBuffer(this.response, headerBufferSize);
        this.response.setOutputBuffer(this.outputBuffer);
        this.initializeFilters(maxTrailerSize, maxExtensionSize);
    }
    
    public String getClientCertProvider() {
        return this.clientCertProvider;
    }
    
    public void setClientCertProvider(final String s) {
        this.clientCertProvider = s;
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState event(final SocketStatus status) throws IOException {
        final RequestInfo rp = this.request.getRequestProcessor();
        try {
            rp.setStage(3);
            this.error = !this.adapter.event(this.request, this.response, status);
        }
        catch (InterruptedIOException e) {
            this.error = true;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            Http11AprProcessor.log.error((Object)Http11AprProcessor.sm.getString("http11processor.request.process"), t);
            this.response.setStatus(500);
            this.adapter.log(this.request, this.response, 0L);
            this.error = true;
        }
        rp.setStage(7);
        if (this.error || status == SocketStatus.STOP) {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        if (!this.comet) {
            this.inputBuffer.nextRequest();
            this.outputBuffer.nextRequest();
            return AbstractEndpoint.Handler.SocketState.OPEN;
        }
        return AbstractEndpoint.Handler.SocketState.LONG;
    }
    
    @Override
    protected boolean disableKeepAlive() {
        return false;
    }
    
    @Override
    protected void setRequestLineReadTimeout() throws IOException {
    }
    
    @Override
    protected boolean handleIncompleteRequestLineRead() {
        this.openSocket = true;
        if (this.endpoint.isPaused()) {
            this.response.setStatus(503);
            this.adapter.log(this.request, this.response, 0L);
            this.error = true;
            return false;
        }
        return true;
    }
    
    @Override
    protected void setSocketTimeout(final int timeout) {
        Socket.timeoutSet(this.socket.getSocket(), timeout * 1000);
    }
    
    @Override
    protected void setCometTimeouts(final SocketWrapper<Long> socketWrapper) {
    }
    
    @Override
    protected boolean breakKeepAliveLoop(final SocketWrapper<Long> socketWrapper) {
        this.openSocket = this.keepAlive;
        if (this.sendfileData != null && !this.error) {
            this.sendfileData.socket = socketWrapper.getSocket();
            this.sendfileData.keepAlive = this.keepAlive;
            if (!((AprEndpoint)this.endpoint).getSendfile().add(this.sendfileData)) {
                if (this.sendfileData.socket == 0L) {
                    if (Http11AprProcessor.log.isDebugEnabled()) {
                        Http11AprProcessor.log.debug((Object)Http11AprProcessor.sm.getString("http11processor.sendfile.error"));
                    }
                    this.error = true;
                }
                else {
                    this.sendfileInProgress = true;
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void resetTimeouts() {
    }
    
    public void recycleInternal() {
        this.socket = null;
        this.sendfileData = null;
    }
    
    @Override
    public void setSslSupport(final SSLSupport sslSupport) {
    }
    
    public void actionInternal(final ActionCode actionCode, final Object param) {
        final long socketRef = this.socket.getSocket();
        if (actionCode == ActionCode.REQ_HOST_ADDR_ATTRIBUTE) {
            if (this.remoteAddr == null && socketRef != 0L) {
                try {
                    final long sa = Address.get(1, socketRef);
                    this.remoteAddr = Address.getip(sa);
                }
                catch (Exception e) {
                    Http11AprProcessor.log.warn((Object)Http11AprProcessor.sm.getString("http11processor.socket.info"), (Throwable)e);
                }
            }
            this.request.remoteAddr().setString(this.remoteAddr);
        }
        else if (actionCode == ActionCode.REQ_LOCAL_NAME_ATTRIBUTE) {
            if (this.localName == null && socketRef != 0L) {
                try {
                    final long sa = Address.get(0, socketRef);
                    this.localName = Address.getnameinfo(sa, 0);
                }
                catch (Exception e) {
                    Http11AprProcessor.log.warn((Object)Http11AprProcessor.sm.getString("http11processor.socket.info"), (Throwable)e);
                }
            }
            this.request.localName().setString(this.localName);
        }
        else if (actionCode == ActionCode.REQ_HOST_ATTRIBUTE) {
            if (this.remoteHost == null && socketRef != 0L) {
                try {
                    final long sa = Address.get(1, socketRef);
                    this.remoteHost = Address.getnameinfo(sa, 0);
                    if (this.remoteHost == null) {
                        this.remoteHost = Address.getip(sa);
                    }
                }
                catch (Exception e) {
                    Http11AprProcessor.log.warn((Object)Http11AprProcessor.sm.getString("http11processor.socket.info"), (Throwable)e);
                }
            }
            this.request.remoteHost().setString(this.remoteHost);
        }
        else if (actionCode == ActionCode.REQ_LOCAL_ADDR_ATTRIBUTE) {
            if (this.localAddr == null && socketRef != 0L) {
                try {
                    final long sa = Address.get(0, socketRef);
                    this.localAddr = Address.getip(sa);
                }
                catch (Exception e) {
                    Http11AprProcessor.log.warn((Object)Http11AprProcessor.sm.getString("http11processor.socket.info"), (Throwable)e);
                }
            }
            this.request.localAddr().setString(this.localAddr);
        }
        else if (actionCode == ActionCode.REQ_REMOTEPORT_ATTRIBUTE) {
            if (this.remotePort == -1 && socketRef != 0L) {
                try {
                    final long sa = Address.get(1, socketRef);
                    final Sockaddr addr = Address.getInfo(sa);
                    this.remotePort = addr.port;
                }
                catch (Exception e) {
                    Http11AprProcessor.log.warn((Object)Http11AprProcessor.sm.getString("http11processor.socket.info"), (Throwable)e);
                }
            }
            this.request.setRemotePort(this.remotePort);
        }
        else if (actionCode == ActionCode.REQ_LOCALPORT_ATTRIBUTE) {
            if (this.localPort == -1 && socketRef != 0L) {
                try {
                    final long sa = Address.get(0, socketRef);
                    final Sockaddr addr = Address.getInfo(sa);
                    this.localPort = addr.port;
                }
                catch (Exception e) {
                    Http11AprProcessor.log.warn((Object)Http11AprProcessor.sm.getString("http11processor.socket.info"), (Throwable)e);
                }
            }
            this.request.setLocalPort(this.localPort);
        }
        else if (actionCode == ActionCode.REQ_SSL_ATTRIBUTE) {
            if (this.endpoint.isSSLEnabled() && socketRef != 0L) {
                try {
                    Object sslO = SSLSocket.getInfoS(socketRef, 2);
                    if (sslO != null) {
                        this.request.setAttribute("javax.servlet.request.cipher_suite", sslO);
                    }
                    final int certLength = SSLSocket.getInfoI(socketRef, 1024);
                    final byte[] clientCert = SSLSocket.getInfoB(socketRef, 263);
                    X509Certificate[] certs = null;
                    if (clientCert != null && certLength > -1) {
                        certs = new X509Certificate[certLength + 1];
                        CertificateFactory cf;
                        if (this.clientCertProvider == null) {
                            cf = CertificateFactory.getInstance("X.509");
                        }
                        else {
                            cf = CertificateFactory.getInstance("X.509", this.clientCertProvider);
                        }
                        certs[0] = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(clientCert));
                        for (int i = 0; i < certLength; ++i) {
                            final byte[] data = SSLSocket.getInfoB(socketRef, 1024 + i);
                            certs[i + 1] = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(data));
                        }
                    }
                    if (certs != null) {
                        this.request.setAttribute("javax.servlet.request.X509Certificate", certs);
                    }
                    sslO = SSLSocket.getInfoI(socketRef, 3);
                    this.request.setAttribute("javax.servlet.request.key_size", sslO);
                    sslO = SSLSocket.getInfoS(socketRef, 1);
                    if (sslO != null) {
                        this.request.setAttribute("javax.servlet.request.ssl_session_id", sslO);
                    }
                }
                catch (Exception e) {
                    Http11AprProcessor.log.warn((Object)Http11AprProcessor.sm.getString("http11processor.socket.ssl"), (Throwable)e);
                }
            }
        }
        else if (actionCode == ActionCode.REQ_SSL_CERTIFICATE) {
            if (this.endpoint.isSSLEnabled() && socketRef != 0L) {
                final InputFilter[] inputFilters = this.inputBuffer.getFilters();
                ((BufferedInputFilter)inputFilters[3]).setLimit(this.maxSavePostSize);
                this.inputBuffer.addActiveFilter(inputFilters[3]);
                try {
                    SSLSocket.setVerify(socketRef, 2, ((AprEndpoint)this.endpoint).getSSLVerifyDepth());
                    if (SSLSocket.renegotiate(socketRef) == 0) {
                        final int certLength = SSLSocket.getInfoI(socketRef, 1024);
                        final byte[] clientCert = SSLSocket.getInfoB(socketRef, 263);
                        X509Certificate[] certs = null;
                        if (clientCert != null && certLength > -1) {
                            certs = new X509Certificate[certLength + 1];
                            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                            certs[0] = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(clientCert));
                            for (int i = 0; i < certLength; ++i) {
                                final byte[] data = SSLSocket.getInfoB(socketRef, 1024 + i);
                                certs[i + 1] = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(data));
                            }
                        }
                        if (certs != null) {
                            this.request.setAttribute("javax.servlet.request.X509Certificate", certs);
                        }
                    }
                }
                catch (Exception e2) {
                    Http11AprProcessor.log.warn((Object)Http11AprProcessor.sm.getString("http11processor.socket.ssl"), (Throwable)e2);
                }
            }
        }
        else if (actionCode == ActionCode.AVAILABLE) {
            this.request.setAvailable(this.inputBuffer.available());
        }
        else if (actionCode == ActionCode.COMET_BEGIN) {
            this.comet = true;
        }
        else if (actionCode == ActionCode.COMET_END) {
            this.comet = false;
        }
        else if (actionCode == ActionCode.COMET_CLOSE) {
            ((AprEndpoint)this.endpoint).processSocketAsync(this.socket, SocketStatus.OPEN_READ);
        }
        else if (actionCode != ActionCode.COMET_SETTIMEOUT) {
            if (actionCode == ActionCode.ASYNC_COMPLETE) {
                if (this.asyncStateMachine.asyncComplete()) {
                    ((AprEndpoint)this.endpoint).processSocketAsync(this.socket, SocketStatus.OPEN_READ);
                }
            }
            else if (actionCode == ActionCode.ASYNC_SETTIMEOUT) {
                if (param == null) {
                    return;
                }
                final long timeout = (long)param;
                this.socket.setTimeout(timeout);
            }
            else if (actionCode == ActionCode.ASYNC_DISPATCH && this.asyncStateMachine.asyncDispatch()) {
                ((AprEndpoint)this.endpoint).processSocketAsync(this.socket, SocketStatus.OPEN_READ);
            }
        }
    }
    
    @Override
    protected void prepareRequestInternal() {
        this.sendfileData = null;
    }
    
    protected boolean prepareSendfile(final OutputFilter[] outputFilters) {
        final String fileName = (String)this.request.getAttribute("org.apache.tomcat.sendfile.filename");
        if (fileName != null) {
            this.outputBuffer.addActiveFilter(outputFilters[2]);
            this.contentDelimitation = true;
            this.sendfileData = new AprEndpoint.SendfileData();
            this.sendfileData.fileName = fileName;
            this.sendfileData.start = (long)this.request.getAttribute("org.apache.tomcat.sendfile.start");
            this.sendfileData.end = (long)this.request.getAttribute("org.apache.tomcat.sendfile.end");
            return true;
        }
        return false;
    }
    
    @Override
    protected void setSocketWrapper(final SocketWrapper<Long> socketWrapper) {
        this.socket = socketWrapper;
    }
    
    @Override
    protected AbstractInputBuffer<Long> getInputBuffer() {
        return this.inputBuffer;
    }
    
    @Override
    protected AbstractOutputBuffer<Long> getOutputBuffer() {
        return this.outputBuffer;
    }
    
    static {
        log = LogFactory.getLog((Class)Http11AprProcessor.class);
    }
}
