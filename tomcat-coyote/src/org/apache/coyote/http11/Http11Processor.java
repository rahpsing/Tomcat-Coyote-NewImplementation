// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.juli.logging.LogFactory;
import java.net.InetAddress;
import org.apache.coyote.http11.filters.BufferedInputFilter;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.net.SocketStatus;
import java.io.IOException;
import java.io.EOFException;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.InputBuffer;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.JIoEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.juli.logging.Log;
import java.net.Socket;

public class Http11Processor extends AbstractHttp11Processor<Socket>
{
    private static final Log log;
    protected InternalInputBuffer inputBuffer;
    protected InternalOutputBuffer outputBuffer;
    protected SSLSupport sslSupport;
    protected SocketWrapper<Socket> socket;
    private int disableKeepAlivePercentage;
    
    @Override
    protected Log getLog() {
        return Http11Processor.log;
    }
    
    public Http11Processor(final int headerBufferSize, final JIoEndpoint endpoint, final int maxTrailerSize, final int maxExtensionSize) {
        super(endpoint);
        this.inputBuffer = null;
        this.outputBuffer = null;
        this.disableKeepAlivePercentage = 75;
        this.inputBuffer = new InternalInputBuffer(this.request, headerBufferSize);
        this.request.setInputBuffer(this.inputBuffer);
        this.outputBuffer = new InternalOutputBuffer(this.response, headerBufferSize);
        this.response.setOutputBuffer(this.outputBuffer);
        this.initializeFilters(maxTrailerSize, maxExtensionSize);
    }
    
    @Override
    public void setSslSupport(final SSLSupport sslSupport) {
        this.sslSupport = sslSupport;
    }
    
    public int getDisableKeepAlivePercentage() {
        return this.disableKeepAlivePercentage;
    }
    
    public void setDisableKeepAlivePercentage(final int disableKeepAlivePercentage) {
        this.disableKeepAlivePercentage = disableKeepAlivePercentage;
    }
    
    @Override
    protected boolean disableKeepAlive() {
        int threadRatio = -1;
        final int maxThreads;
        final int threadsBusy;
        if ((maxThreads = this.endpoint.getMaxThreads()) > 0 && (threadsBusy = this.endpoint.getCurrentThreadsBusy()) > 0) {
            threadRatio = threadsBusy * 100 / maxThreads;
        }
        return threadRatio > this.getDisableKeepAlivePercentage();
    }
    
    @Override
    protected void setRequestLineReadTimeout() throws IOException {
        if (this.inputBuffer.lastValid == 0 && this.socket.getLastAccess() > -1L) {
            int firstReadTimeout;
            if (this.keepAliveTimeout == -1) {
                firstReadTimeout = 0;
            }
            else {
                final long queueTime = System.currentTimeMillis() - this.socket.getLastAccess();
                if (queueTime >= this.keepAliveTimeout) {
                    firstReadTimeout = 1;
                }
                else {
                    firstReadTimeout = this.keepAliveTimeout - (int)queueTime;
                }
            }
            this.socket.getSocket().setSoTimeout(firstReadTimeout);
            if (!this.inputBuffer.fill()) {
                throw new EOFException(Http11Processor.sm.getString("iib.eof.error"));
            }
            if (this.endpoint.getSoTimeout() > 0) {
                this.setSocketTimeout(this.endpoint.getSoTimeout());
            }
            else {
                this.setSocketTimeout(0);
            }
        }
    }
    
    @Override
    protected boolean handleIncompleteRequestLineRead() {
        return false;
    }
    
    @Override
    protected void setSocketTimeout(final int timeout) throws IOException {
        this.socket.getSocket().setSoTimeout(timeout);
    }
    
    @Override
    protected void setCometTimeouts(final SocketWrapper<Socket> socketWrapper) {
    }
    
    @Override
    protected boolean breakKeepAliveLoop(final SocketWrapper<Socket> socketWrapper) {
        this.openSocket = this.keepAlive;
        return this.inputBuffer.lastValid == 0;
    }
    
    @Override
    protected void resetTimeouts() {
    }
    
    @Override
    protected void recycleInternal() {
        this.socket = null;
        this.sslSupport = null;
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState event(final SocketStatus status) throws IOException {
        throw new IOException(Http11Processor.sm.getString("http11processor.comet.notsupported"));
    }
    
    public void actionInternal(final ActionCode actionCode, final Object param) {
        if (actionCode == ActionCode.REQ_SSL_ATTRIBUTE) {
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
                Http11Processor.log.warn((Object)Http11Processor.sm.getString("http11processor.socket.ssl"), (Throwable)e);
            }
        }
        else if (actionCode == ActionCode.REQ_HOST_ADDR_ATTRIBUTE) {
            if (this.remoteAddr == null && this.socket != null) {
                final InetAddress inetAddr = this.socket.getSocket().getInetAddress();
                if (inetAddr != null) {
                    this.remoteAddr = inetAddr.getHostAddress();
                }
            }
            this.request.remoteAddr().setString(this.remoteAddr);
        }
        else if (actionCode == ActionCode.REQ_LOCAL_NAME_ATTRIBUTE) {
            if (this.localName == null && this.socket != null) {
                final InetAddress inetAddr = this.socket.getSocket().getLocalAddress();
                if (inetAddr != null) {
                    this.localName = inetAddr.getHostName();
                }
            }
            this.request.localName().setString(this.localName);
        }
        else if (actionCode == ActionCode.REQ_HOST_ATTRIBUTE) {
            if (this.remoteHost == null && this.socket != null) {
                final InetAddress inetAddr = this.socket.getSocket().getInetAddress();
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
                this.localAddr = this.socket.getSocket().getLocalAddress().getHostAddress();
            }
            this.request.localAddr().setString(this.localAddr);
        }
        else if (actionCode == ActionCode.REQ_REMOTEPORT_ATTRIBUTE) {
            if (this.remotePort == -1 && this.socket != null) {
                this.remotePort = this.socket.getSocket().getPort();
            }
            this.request.setRemotePort(this.remotePort);
        }
        else if (actionCode == ActionCode.REQ_LOCALPORT_ATTRIBUTE) {
            if (this.localPort == -1 && this.socket != null) {
                this.localPort = this.socket.getSocket().getLocalPort();
            }
            this.request.setLocalPort(this.localPort);
        }
        else if (actionCode == ActionCode.REQ_SSL_CERTIFICATE) {
            if (this.sslSupport != null) {
                final InputFilter[] inputFilters = this.inputBuffer.getFilters();
                ((BufferedInputFilter)inputFilters[3]).setLimit(this.maxSavePostSize);
                this.inputBuffer.addActiveFilter(inputFilters[3]);
                try {
                    final Object sslO2 = this.sslSupport.getPeerCertificateChain(true);
                    if (sslO2 != null) {
                        this.request.setAttribute("javax.servlet.request.X509Certificate", sslO2);
                    }
                }
                catch (Exception e2) {
                    Http11Processor.log.warn((Object)Http11Processor.sm.getString("http11processor.socket.ssl"), (Throwable)e2);
                }
            }
        }
        else if (actionCode == ActionCode.ASYNC_COMPLETE) {
            if (this.asyncStateMachine.asyncComplete()) {
                ((JIoEndpoint)this.endpoint).processSocketAsync(this.socket, SocketStatus.OPEN_READ);
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
            ((JIoEndpoint)this.endpoint).processSocketAsync(this.socket, SocketStatus.OPEN_READ);
        }
    }
    
    @Override
    protected void prepareRequestInternal() {
    }
    
    protected boolean prepareSendfile(final OutputFilter[] outputFilters) {
        final Exception e = new Exception();
        Http11Processor.log.error((Object)Http11Processor.sm.getString("http11processor.neverused"), (Throwable)e);
        return false;
    }
    
    @Override
    protected void setSocketWrapper(final SocketWrapper<Socket> socketWrapper) {
        this.socket = socketWrapper;
    }
    
    @Override
    protected AbstractInputBuffer<Socket> getInputBuffer() {
        return this.inputBuffer;
    }
    
    @Override
    protected AbstractOutputBuffer<Socket> getOutputBuffer() {
        return this.outputBuffer;
    }
    
    @Override
    public void setSocketBuffer(final int socketBuffer) {
        super.setSocketBuffer(socketBuffer);
        this.outputBuffer.setSocketBuffer(socketBuffer);
    }
    
    static {
        log = LogFactory.getLog((Class)Http11Processor.class);
    }
}
