// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.juli.logging.LogFactory;
import javax.net.ssl.SSLEngine;
import java.net.InetAddress;
import org.apache.tomcat.util.net.SecureNioChannel;
import org.apache.coyote.http11.filters.BufferedInputFilter;
import org.apache.coyote.ActionCode;
import java.nio.channels.SelectionKey;
import java.io.IOException;
import org.apache.coyote.RequestInfo;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.InterruptedIOException;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.InputBuffer;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.NioChannel;

public class Http11NioProcessor extends AbstractHttp11Processor<NioChannel>
{
    private static final Log log;
    protected SSLSupport sslSupport;
    protected InternalNioInputBuffer inputBuffer;
    protected InternalNioOutputBuffer outputBuffer;
    protected NioEndpoint.SendfileData sendfileData;
    protected SocketWrapper<NioChannel> socket;
    
    @Override
    protected Log getLog() {
        return Http11NioProcessor.log;
    }
    
    public Http11NioProcessor(final int maxHttpHeaderSize, final NioEndpoint endpoint, final int maxTrailerSize, final int maxExtensionSize) {
        super(endpoint);
        this.inputBuffer = null;
        this.outputBuffer = null;
        this.sendfileData = null;
        this.socket = null;
        this.inputBuffer = new InternalNioInputBuffer(this.request, maxHttpHeaderSize);
        this.request.setInputBuffer(this.inputBuffer);
        this.outputBuffer = new InternalNioOutputBuffer(this.response, maxHttpHeaderSize);
        this.response.setOutputBuffer(this.outputBuffer);
        this.initializeFilters(maxTrailerSize, maxExtensionSize);
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState event(final SocketStatus status) throws IOException {
        final long soTimeout = this.endpoint.getSoTimeout();
        final RequestInfo rp = this.request.getRequestProcessor();
        final NioEndpoint.KeyAttachment attach = (NioEndpoint.KeyAttachment)this.socket.getSocket().getAttachment(false);
        try {
            rp.setStage(3);
            this.error = !this.adapter.event(this.request, this.response, status);
            if (!this.error && attach != null) {
                attach.setComet(this.comet);
                if (this.comet) {
                    final Integer comettimeout = (Integer)this.request.getAttribute("org.apache.tomcat.comet.timeout");
                    if (comettimeout != null) {
                        attach.setTimeout(comettimeout);
                    }
                }
                else if (this.keepAlive) {
                    attach.setTimeout(this.keepAliveTimeout);
                }
                else {
                    attach.setTimeout(soTimeout);
                }
            }
        }
        catch (InterruptedIOException e) {
            this.error = true;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            Http11NioProcessor.log.error((Object)Http11NioProcessor.sm.getString("http11processor.request.process"), t);
            this.response.setStatus(500);
            this.adapter.log(this.request, this.response, 0L);
            this.error = true;
        }
        rp.setStage(7);
        if (this.error || status == SocketStatus.STOP) {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        if (this.comet) {
            return AbstractEndpoint.Handler.SocketState.LONG;
        }
        if (this.keepAlive) {
            this.inputBuffer.nextRequest();
            this.outputBuffer.nextRequest();
            return AbstractEndpoint.Handler.SocketState.OPEN;
        }
        return AbstractEndpoint.Handler.SocketState.CLOSED;
    }
    
    @Override
    protected void resetTimeouts() {
        final NioEndpoint.KeyAttachment attach = (NioEndpoint.KeyAttachment)this.socket.getSocket().getAttachment(false);
        if (!this.error && attach != null && this.asyncStateMachine.isAsyncDispatching()) {
            final long soTimeout = this.endpoint.getSoTimeout();
            if (this.keepAlive) {
                attach.setTimeout(this.keepAliveTimeout);
            }
            else {
                attach.setTimeout(soTimeout);
            }
        }
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
        if (this.inputBuffer.getParsingRequestLinePhase() < 2) {
            if (this.socket.getLastAccess() > -1L || this.keptAlive) {
                this.socket.setTimeout(this.endpoint.getKeepAliveTimeout());
            }
        }
        else {
            this.readComplete = false;
            this.socket.setTimeout(this.endpoint.getSoTimeout());
        }
        if (this.endpoint.isPaused()) {
            this.response.setStatus(503);
            this.adapter.log(this.request, this.response, 0L);
            this.error = true;
            return false;
        }
        return true;
    }
    
    @Override
    protected void setSocketTimeout(final int timeout) throws IOException {
        this.socket.getSocket().getIOChannel().socket().setSoTimeout(timeout);
    }
    
    @Override
    protected void setCometTimeouts(final SocketWrapper<NioChannel> socketWrapper) {
        final SelectionKey key = socketWrapper.getSocket().getIOChannel().keyFor(socketWrapper.getSocket().getPoller().getSelector());
        if (key != null) {
            final NioEndpoint.KeyAttachment attach = (NioEndpoint.KeyAttachment)key.attachment();
            if (attach != null) {
                attach.setComet(this.comet);
                if (this.comet) {
                    final Integer comettimeout = (Integer)this.request.getAttribute("org.apache.tomcat.comet.timeout");
                    if (comettimeout != null) {
                        attach.setTimeout(comettimeout);
                    }
                }
            }
        }
    }
    
    @Override
    protected boolean breakKeepAliveLoop(final SocketWrapper<NioChannel> socketWrapper) {
        if (this.sendfileData != null && !this.error) {
            ((NioEndpoint.KeyAttachment)socketWrapper).setSendfileData(this.sendfileData);
            this.sendfileData.keepAlive = this.keepAlive;
            final SelectionKey key = socketWrapper.getSocket().getIOChannel().keyFor(socketWrapper.getSocket().getPoller().getSelector());
            this.openSocket = socketWrapper.getSocket().getPoller().processSendfile(key, (NioEndpoint.KeyAttachment)socketWrapper, true);
            return true;
        }
        return false;
    }
    
    public void recycleInternal() {
        this.socket = null;
        this.sendfileData = null;
    }
    
    public void actionInternal(final ActionCode actionCode, final Object param) {
        if (actionCode == ActionCode.REQ_HOST_ADDR_ATTRIBUTE) {
            if (this.remoteAddr == null && this.socket != null) {
                final InetAddress inetAddr = this.socket.getSocket().getIOChannel().socket().getInetAddress();
                if (inetAddr != null) {
                    this.remoteAddr = inetAddr.getHostAddress();
                }
            }
            this.request.remoteAddr().setString(this.remoteAddr);
        }
        else if (actionCode == ActionCode.REQ_LOCAL_NAME_ATTRIBUTE) {
            if (this.localName == null && this.socket != null) {
                final InetAddress inetAddr = this.socket.getSocket().getIOChannel().socket().getLocalAddress();
                if (inetAddr != null) {
                    this.localName = inetAddr.getHostName();
                }
            }
            this.request.localName().setString(this.localName);
        }
        else if (actionCode == ActionCode.REQ_HOST_ATTRIBUTE) {
            if (this.remoteHost == null && this.socket != null) {
                final InetAddress inetAddr = this.socket.getSocket().getIOChannel().socket().getInetAddress();
                if (inetAddr != null) {
                    this.remoteHost = inetAddr.getHostName();
                }
                if (this.remoteHost == null) {
                    if (this.remoteAddr != null) {
                        this.remoteHost = this.remoteAddr;
                    }
                    else {
                        this.request.remoteHost().recycle();
                    }
                }
            }
            this.request.remoteHost().setString(this.remoteHost);
        }
        else if (actionCode == ActionCode.REQ_LOCAL_ADDR_ATTRIBUTE) {
            if (this.localAddr == null) {
                this.localAddr = this.socket.getSocket().getIOChannel().socket().getLocalAddress().getHostAddress();
            }
            this.request.localAddr().setString(this.localAddr);
        }
        else if (actionCode == ActionCode.REQ_REMOTEPORT_ATTRIBUTE) {
            if (this.remotePort == -1 && this.socket != null) {
                this.remotePort = this.socket.getSocket().getIOChannel().socket().getPort();
            }
            this.request.setRemotePort(this.remotePort);
        }
        else if (actionCode == ActionCode.REQ_LOCALPORT_ATTRIBUTE) {
            if (this.localPort == -1 && this.socket != null) {
                this.localPort = this.socket.getSocket().getIOChannel().socket().getLocalPort();
            }
            this.request.setLocalPort(this.localPort);
        }
        else if (actionCode == ActionCode.REQ_SSL_ATTRIBUTE) {
            try {
                if (this.sslSupport != null) {
                    Object sslO = this.sslSupport.getCipherSuite();
                    if (sslO != null) {
                        this.request.setAttribute("javax.servlet.request.cipher_suite", sslO);
                    }
                    sslO = this.sslSupport.getPeerCertificateChain(false);
                    if (sslO != null) {
                        this.request.setAttribute("javax.servlet.request.X509Certificate", sslO);
                    }
                    sslO = this.sslSupport.getKeySize();
                    if (sslO != null) {
                        this.request.setAttribute("javax.servlet.request.key_size", sslO);
                    }
                    sslO = this.sslSupport.getSessionId();
                    if (sslO != null) {
                        this.request.setAttribute("javax.servlet.request.ssl_session_id", sslO);
                    }
                    this.request.setAttribute("javax.servlet.request.ssl_session_mgr", this.sslSupport);
                }
            }
            catch (Exception e) {
                Http11NioProcessor.log.warn((Object)Http11NioProcessor.sm.getString("http11processor.socket.ssl"), (Throwable)e);
            }
        }
        else if (actionCode == ActionCode.REQ_SSL_CERTIFICATE) {
            if (this.sslSupport != null) {
                final InputFilter[] inputFilters = this.inputBuffer.getFilters();
                ((BufferedInputFilter)inputFilters[3]).setLimit(this.maxSavePostSize);
                this.inputBuffer.addActiveFilter(inputFilters[3]);
                final SecureNioChannel sslChannel = this.socket.getSocket();
                final SSLEngine engine = sslChannel.getSslEngine();
                if (!engine.getNeedClientAuth()) {
                    engine.setNeedClientAuth(true);
                    try {
                        sslChannel.rehandshake(this.endpoint.getSoTimeout());
                        this.sslSupport = ((NioEndpoint)this.endpoint).getHandler().getSslImplementation().getSSLSupport(engine.getSession());
                    }
                    catch (IOException ioe) {
                        Http11NioProcessor.log.warn((Object)Http11NioProcessor.sm.getString("http11processor.socket.sslreneg", new Object[] { ioe }));
                    }
                }
                try {
                    final Object sslO2 = this.sslSupport.getPeerCertificateChain(false);
                    if (sslO2 != null) {
                        this.request.setAttribute("javax.servlet.request.X509Certificate", sslO2);
                    }
                }
                catch (Exception e2) {
                    Http11NioProcessor.log.warn((Object)Http11NioProcessor.sm.getString("http11processor.socket.ssl"), (Throwable)e2);
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
            if (this.socket == null || this.socket.getSocket().getAttachment(false) == null) {
                return;
            }
            final NioEndpoint.KeyAttachment attach = (NioEndpoint.KeyAttachment)this.socket.getSocket().getAttachment(false);
            attach.setCometOps(512);
            final RequestInfo rp = this.request.getRequestProcessor();
            if (rp.getStage() != 3) {
                this.socket.getSocket().getPoller().add(this.socket.getSocket());
            }
        }
        else if (actionCode == ActionCode.COMET_SETTIMEOUT) {
            if (param == null) {
                return;
            }
            if (this.socket == null || this.socket.getSocket().getAttachment(false) == null) {
                return;
            }
            final NioEndpoint.KeyAttachment attach = (NioEndpoint.KeyAttachment)this.socket.getSocket().getAttachment(false);
            final long timeout = (long)param;
            final RequestInfo rp2 = this.request.getRequestProcessor();
            if (rp2.getStage() != 3) {
                attach.setTimeout(timeout);
            }
        }
        else if (actionCode == ActionCode.ASYNC_COMPLETE) {
            if (this.asyncStateMachine.asyncComplete()) {
                ((NioEndpoint)this.endpoint).processSocket(this.socket.getSocket(), SocketStatus.OPEN_READ, true);
            }
        }
        else if (actionCode == ActionCode.ASYNC_SETTIMEOUT) {
            if (param == null) {
                return;
            }
            if (this.socket == null || this.socket.getSocket().getAttachment(false) == null) {
                return;
            }
            final NioEndpoint.KeyAttachment attach = (NioEndpoint.KeyAttachment)this.socket.getSocket().getAttachment(false);
            final long timeout = (long)param;
            attach.setTimeout(timeout);
        }
        else if (actionCode == ActionCode.ASYNC_DISPATCH && this.asyncStateMachine.asyncDispatch()) {
            ((NioEndpoint)this.endpoint).processSocket(this.socket.getSocket(), SocketStatus.OPEN_READ, true);
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
            this.sendfileData = new NioEndpoint.SendfileData();
            this.sendfileData.fileName = fileName;
            this.sendfileData.pos = (long)this.request.getAttribute("org.apache.tomcat.sendfile.start");
            this.sendfileData.length = (long)this.request.getAttribute("org.apache.tomcat.sendfile.end") - this.sendfileData.pos;
            return true;
        }
        return false;
    }
    
    @Override
    protected void setSocketWrapper(final SocketWrapper<NioChannel> socketWrapper) {
        this.socket = socketWrapper;
    }
    
    @Override
    protected AbstractInputBuffer<NioChannel> getInputBuffer() {
        return this.inputBuffer;
    }
    
    @Override
    protected AbstractOutputBuffer<NioChannel> getOutputBuffer() {
        return this.outputBuffer;
    }
    
    @Override
    public void setSslSupport(final SSLSupport sslSupport) {
        this.sslSupport = sslSupport;
    }
    
    static {
        log = LogFactory.getLog((Class)Http11NioProcessor.class);
    }
}
