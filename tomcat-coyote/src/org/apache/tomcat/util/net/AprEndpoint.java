// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import org.apache.tomcat.jni.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;
import org.apache.juli.logging.LogFactory;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.apache.tomcat.jni.Status;
import org.apache.tomcat.jni.Poll;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.jni.SSLSocket;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;
import org.apache.tomcat.jni.Error;
import org.apache.tomcat.jni.Socket;
import org.apache.tomcat.jni.OS;
import org.apache.tomcat.jni.Pool;
import org.apache.tomcat.jni.Sockaddr;
import org.apache.tomcat.jni.Address;
import org.apache.tomcat.jni.Library;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.juli.logging.Log;

public class AprEndpoint extends AbstractEndpoint
{
    private static final Log log;
    protected long rootPool;
    protected long serverSock;
    protected long serverSockPool;
    protected long sslContext;
    protected ConcurrentLinkedQueue<SocketWrapper<Long>> waitingRequests;
    private final Map<Long, AprSocketWrapper> connections;
    protected boolean deferAccept;
    protected int sendfileSize;
    protected Handler handler;
    protected int pollTime;
    protected boolean useSendfile;
    protected boolean useComet;
    protected int sendfileThreadCount;
    protected Poller poller;
    protected AsyncTimeout asyncTimeout;
    protected Sendfile sendfile;
    protected String SSLProtocol;
    protected String SSLPassword;
    protected String SSLCipherSuite;
    protected String SSLCertificateFile;
    protected String SSLCertificateKeyFile;
    protected String SSLCertificateChainFile;
    protected String SSLCACertificatePath;
    protected String SSLCACertificateFile;
    protected String SSLCARevocationPath;
    protected String SSLCARevocationFile;
    protected String SSLVerifyClient;
    protected int SSLVerifyDepth;
    protected boolean SSLInsecureRenegotiation;
    protected boolean SSLHonorCipherOrder;
    protected boolean SSLDisableCompression;
    
    public AprEndpoint() {
        this.rootPool = 0L;
        this.serverSock = 0L;
        this.serverSockPool = 0L;
        this.sslContext = 0L;
        this.waitingRequests = new ConcurrentLinkedQueue<SocketWrapper<Long>>();
        this.connections = new ConcurrentHashMap<Long, AprSocketWrapper>();
        this.deferAccept = true;
        this.sendfileSize = 1024;
        this.handler = null;
        this.pollTime = 2000;
        this.useSendfile = Library.APR_HAS_SENDFILE;
        this.useComet = true;
        this.sendfileThreadCount = 0;
        this.poller = null;
        this.asyncTimeout = null;
        this.sendfile = null;
        this.SSLProtocol = "all";
        this.SSLPassword = null;
        this.SSLCipherSuite = "ALL";
        this.SSLCertificateFile = null;
        this.SSLCertificateKeyFile = null;
        this.SSLCertificateChainFile = null;
        this.SSLCACertificatePath = null;
        this.SSLCACertificateFile = null;
        this.SSLCARevocationPath = null;
        this.SSLCARevocationFile = null;
        this.SSLVerifyClient = "none";
        this.SSLVerifyDepth = 10;
        this.SSLInsecureRenegotiation = false;
        this.SSLHonorCipherOrder = false;
        this.SSLDisableCompression = false;
        this.setMaxConnections(8192);
    }
    
    public void setDeferAccept(final boolean deferAccept) {
        this.deferAccept = deferAccept;
    }
    
    public boolean getDeferAccept() {
        return this.deferAccept;
    }
    
    public void setSendfileSize(final int sendfileSize) {
        this.sendfileSize = sendfileSize;
    }
    
    public int getSendfileSize() {
        return this.sendfileSize;
    }
    
    public void setHandler(final Handler handler) {
        this.handler = handler;
    }
    
    public Handler getHandler() {
        return this.handler;
    }
    
    public int getPollTime() {
        return this.pollTime;
    }
    
    public void setPollTime(final int pollTime) {
        if (pollTime > 0) {
            this.pollTime = pollTime;
        }
    }
    
    public void setUseSendfile(final boolean useSendfile) {
        this.useSendfile = useSendfile;
    }
    
    @Override
    public boolean getUseSendfile() {
        return this.useSendfile;
    }
    
    public void setUseComet(final boolean useComet) {
        this.useComet = useComet;
    }
    
    @Override
    public boolean getUseComet() {
        return this.useComet;
    }
    
    @Override
    public boolean getUseCometTimeout() {
        return false;
    }
    
    @Override
    public boolean getUsePolling() {
        return true;
    }
    
    public void setSendfileThreadCount(final int sendfileThreadCount) {
        this.sendfileThreadCount = sendfileThreadCount;
    }
    
    public int getSendfileThreadCount() {
        return this.sendfileThreadCount;
    }
    
    public Poller getPoller() {
        return this.poller;
    }
    
    public AsyncTimeout getAsyncTimeout() {
        return this.asyncTimeout;
    }
    
    public Sendfile getSendfile() {
        return this.sendfile;
    }
    
    public String getSSLProtocol() {
        return this.SSLProtocol;
    }
    
    public void setSSLProtocol(final String SSLProtocol) {
        this.SSLProtocol = SSLProtocol;
    }
    
    public String getSSLPassword() {
        return this.SSLPassword;
    }
    
    public void setSSLPassword(final String SSLPassword) {
        this.SSLPassword = SSLPassword;
    }
    
    public String getSSLCipherSuite() {
        return this.SSLCipherSuite;
    }
    
    public void setSSLCipherSuite(final String SSLCipherSuite) {
        this.SSLCipherSuite = SSLCipherSuite;
    }
    
    public String getSSLCertificateFile() {
        return this.SSLCertificateFile;
    }
    
    public void setSSLCertificateFile(final String SSLCertificateFile) {
        this.SSLCertificateFile = SSLCertificateFile;
    }
    
    public String getSSLCertificateKeyFile() {
        return this.SSLCertificateKeyFile;
    }
    
    public void setSSLCertificateKeyFile(final String SSLCertificateKeyFile) {
        this.SSLCertificateKeyFile = SSLCertificateKeyFile;
    }
    
    public String getSSLCertificateChainFile() {
        return this.SSLCertificateChainFile;
    }
    
    public void setSSLCertificateChainFile(final String SSLCertificateChainFile) {
        this.SSLCertificateChainFile = SSLCertificateChainFile;
    }
    
    public String getSSLCACertificatePath() {
        return this.SSLCACertificatePath;
    }
    
    public void setSSLCACertificatePath(final String SSLCACertificatePath) {
        this.SSLCACertificatePath = SSLCACertificatePath;
    }
    
    public String getSSLCACertificateFile() {
        return this.SSLCACertificateFile;
    }
    
    public void setSSLCACertificateFile(final String SSLCACertificateFile) {
        this.SSLCACertificateFile = SSLCACertificateFile;
    }
    
    public String getSSLCARevocationPath() {
        return this.SSLCARevocationPath;
    }
    
    public void setSSLCARevocationPath(final String SSLCARevocationPath) {
        this.SSLCARevocationPath = SSLCARevocationPath;
    }
    
    public String getSSLCARevocationFile() {
        return this.SSLCARevocationFile;
    }
    
    public void setSSLCARevocationFile(final String SSLCARevocationFile) {
        this.SSLCARevocationFile = SSLCARevocationFile;
    }
    
    public String getSSLVerifyClient() {
        return this.SSLVerifyClient;
    }
    
    public void setSSLVerifyClient(final String SSLVerifyClient) {
        this.SSLVerifyClient = SSLVerifyClient;
    }
    
    public int getSSLVerifyDepth() {
        return this.SSLVerifyDepth;
    }
    
    public void setSSLVerifyDepth(final int SSLVerifyDepth) {
        this.SSLVerifyDepth = SSLVerifyDepth;
    }
    
    public void setSSLInsecureRenegotiation(final boolean SSLInsecureRenegotiation) {
        this.SSLInsecureRenegotiation = SSLInsecureRenegotiation;
    }
    
    public boolean getSSLInsecureRenegotiation() {
        return this.SSLInsecureRenegotiation;
    }
    
    public void setSSLHonorCipherOrder(final boolean SSLHonorCipherOrder) {
        this.SSLHonorCipherOrder = SSLHonorCipherOrder;
    }
    
    public boolean getSSLHonorCipherOrder() {
        return this.SSLHonorCipherOrder;
    }
    
    public void setSSLDisableCompression(final boolean SSLDisableCompression) {
        this.SSLDisableCompression = SSLDisableCompression;
    }
    
    public boolean getSSLDisableCompression() {
        return this.SSLDisableCompression;
    }
    
    @Override
    public int getLocalPort() {
        final long s = this.serverSock;
        if (s == 0L) {
            return -1;
        }
        try {
            final long sa = Address.get(0, s);
            final Sockaddr addr = Address.getInfo(sa);
            return addr.port;
        }
        catch (Exception e) {
            return -1;
        }
    }
    
    public int getKeepAliveCount() {
        if (this.poller == null) {
            return 0;
        }
        return this.poller.getConnectionCount();
    }
    
    public int getSendfileCount() {
        if (this.sendfile == null) {
            return 0;
        }
        return this.sendfile.getSendfileCount();
    }
    
    @Override
    public void bind() throws Exception {
        try {
            this.rootPool = Pool.create(0L);
        }
        catch (UnsatisfiedLinkError e) {
            throw new Exception(AprEndpoint.sm.getString("endpoint.init.notavail"));
        }
        this.serverSockPool = Pool.create(this.rootPool);
        String addressStr = null;
        if (this.getAddress() != null) {
            addressStr = this.getAddress().getHostAddress();
        }
        int family = 1;
        if (Library.APR_HAVE_IPV6) {
            if (addressStr == null) {
                if (!OS.IS_BSD && !OS.IS_WIN32 && !OS.IS_WIN64) {
                    family = 0;
                }
            }
            else if (addressStr.indexOf(58) >= 0) {
                family = 0;
            }
        }
        final long inetAddress = Address.info(addressStr, family, this.getPort(), 0, this.rootPool);
        this.serverSock = Socket.create(Address.getInfo(inetAddress).family, 0, 6, this.rootPool);
        if (OS.IS_UNIX) {
            Socket.optSet(this.serverSock, 16, 1);
        }
        Socket.optSet(this.serverSock, 2, 1);
        int ret = Socket.bind(this.serverSock, inetAddress);
        if (ret != 0) {
            throw new Exception(AprEndpoint.sm.getString("endpoint.init.bind", new Object[] { "" + ret, Error.strerror(ret) }));
        }
        ret = Socket.listen(this.serverSock, this.getBacklog());
        if (ret != 0) {
            throw new Exception(AprEndpoint.sm.getString("endpoint.init.listen", new Object[] { "" + ret, Error.strerror(ret) }));
        }
        if (OS.IS_WIN32 || OS.IS_WIN64) {
            Socket.optSet(this.serverSock, 16, 1);
        }
        if (this.useSendfile && !Library.APR_HAS_SENDFILE) {
            this.useSendfile = false;
        }
        if (this.acceptorThreadCount == 0) {
            this.acceptorThreadCount = 1;
        }
        if (this.deferAccept && Socket.optSet(this.serverSock, 32768, 1) == 70023) {
            this.deferAccept = false;
        }
        if (this.isSSLEnabled()) {
            if (this.SSLCertificateFile == null) {
                throw new Exception(AprEndpoint.sm.getString("endpoint.apr.noSslCertFile"));
            }
            int value = 0;
            if (this.SSLProtocol == null || this.SSLProtocol.length() == 0) {
                value = 6;
            }
            else {
                for (String protocol : this.SSLProtocol.split("\\+")) {
                    protocol = protocol.trim();
                    if ("SSLv2".equalsIgnoreCase(protocol)) {
                        value |= 0x1;
                    }
                    else if ("SSLv3".equalsIgnoreCase(protocol)) {
                        value |= 0x2;
                    }
                    else if ("TLSv1".equalsIgnoreCase(protocol)) {
                        value |= 0x4;
                    }
                    else {
                        if (!"all".equalsIgnoreCase(protocol)) {
                            throw new Exception(AprEndpoint.sm.getString("endpoint.apr.invalidSslProtocol", new Object[] { this.SSLProtocol }));
                        }
                        value |= 0x6;
                    }
                }
            }
            this.sslContext = SSLContext.make(this.rootPool, value, 1);
            if (this.SSLInsecureRenegotiation) {
                boolean legacyRenegSupported = false;
                try {
                    legacyRenegSupported = SSL.hasOp(262144);
                    if (legacyRenegSupported) {
                        SSLContext.setOptions(this.sslContext, 262144);
                    }
                }
                catch (UnsatisfiedLinkError unsatisfiedLinkError) {}
                if (!legacyRenegSupported) {
                    AprEndpoint.log.warn((Object)AprEndpoint.sm.getString("endpoint.warn.noInsecureReneg", new Object[] { SSL.versionString() }));
                }
            }
            if (this.SSLHonorCipherOrder) {
                boolean orderCiphersSupported = false;
                try {
                    orderCiphersSupported = SSL.hasOp(4194304);
                    if (orderCiphersSupported) {
                        SSLContext.setOptions(this.sslContext, 4194304);
                    }
                }
                catch (UnsatisfiedLinkError unsatisfiedLinkError2) {}
                if (!orderCiphersSupported) {
                    AprEndpoint.log.warn((Object)AprEndpoint.sm.getString("endpoint.warn.noHonorCipherOrder", new Object[] { SSL.versionString() }));
                }
            }
            if (this.SSLDisableCompression) {
                boolean disableCompressionSupported = false;
                try {
                    disableCompressionSupported = SSL.hasOp(131072);
                    if (disableCompressionSupported) {
                        SSLContext.setOptions(this.sslContext, 131072);
                    }
                }
                catch (UnsatisfiedLinkError unsatisfiedLinkError3) {}
                if (!disableCompressionSupported) {
                    AprEndpoint.log.warn((Object)AprEndpoint.sm.getString("endpoint.warn.noDisableCompression", new Object[] { SSL.versionString() }));
                }
            }
            SSLContext.setCipherSuite(this.sslContext, this.SSLCipherSuite);
            SSLContext.setCertificate(this.sslContext, this.SSLCertificateFile, this.SSLCertificateKeyFile, this.SSLPassword, 0);
            SSLContext.setCertificateChainFile(this.sslContext, this.SSLCertificateChainFile, false);
            SSLContext.setCACertificate(this.sslContext, this.SSLCACertificateFile, this.SSLCACertificatePath);
            SSLContext.setCARevocation(this.sslContext, this.SSLCARevocationFile, this.SSLCARevocationPath);
            value = 0;
            if ("optional".equalsIgnoreCase(this.SSLVerifyClient)) {
                value = 1;
            }
            else if ("require".equalsIgnoreCase(this.SSLVerifyClient)) {
                value = 2;
            }
            else if ("optionalNoCA".equalsIgnoreCase(this.SSLVerifyClient)) {
                value = 3;
            }
            SSLContext.setVerify(this.sslContext, value, this.SSLVerifyDepth);
            this.useSendfile = false;
        }
    }
    
    @Override
    public void startInternal() throws Exception {
        if (!this.running) {
            this.running = true;
            this.paused = false;
            if (this.getExecutor() == null) {
                this.createExecutor();
            }
            this.initializeConnectionLatch();
            (this.poller = new Poller()).init();
            final Thread pollerThread = new Thread(this.poller, this.getName() + "-Poller");
            pollerThread.setPriority(this.threadPriority);
            pollerThread.setDaemon(true);
            pollerThread.start();
            if (this.useSendfile) {
                (this.sendfile = new Sendfile()).init();
                final Thread sendfileThread = new Thread(this.sendfile, this.getName() + "-Sendfile");
                sendfileThread.setPriority(this.threadPriority);
                sendfileThread.setDaemon(true);
                sendfileThread.start();
            }
            this.startAcceptorThreads();
            this.asyncTimeout = new AsyncTimeout();
            final Thread timeoutThread = new Thread(this.asyncTimeout, this.getName() + "-AsyncTimeout");
            timeoutThread.setPriority(this.threadPriority);
            timeoutThread.setDaemon(true);
            timeoutThread.start();
        }
    }
    
    @Override
    public void stopInternal() {
        this.releaseConnectionLatch();
        if (!this.paused) {
            this.pause();
        }
        if (this.running) {
            this.running = false;
            this.poller.stop();
            this.asyncTimeout.stop();
            this.unlockAccept();
            for (final AbstractEndpoint.Acceptor acceptor : this.acceptors) {
                long waitLeft;
                for (waitLeft = 10000L; waitLeft > 0L && acceptor.getState() != AbstractEndpoint.Acceptor.AcceptorState.ENDED && this.serverSock != 0L; waitLeft -= 50L) {
                    try {
                        Thread.sleep(50L);
                    }
                    catch (InterruptedException ex) {}
                }
                if (waitLeft == 0L) {
                    AprEndpoint.log.warn((Object)AprEndpoint.sm.getString("endpoint.warn.unlockAcceptorFailed", new Object[] { acceptor.getThreadName() }));
                    if (this.serverSock != 0L) {
                        Socket.shutdown(this.serverSock, 0);
                        this.serverSock = 0L;
                    }
                }
            }
            try {
                this.poller.destroy();
            }
            catch (Exception ex2) {}
            this.poller = null;
            this.connections.clear();
            if (this.useSendfile) {
                try {
                    this.sendfile.destroy();
                }
                catch (Exception ex3) {}
                this.sendfile = null;
            }
        }
        this.shutdownExecutor();
    }
    
    @Override
    public void unbind() throws Exception {
        if (this.running) {
            this.stop();
        }
        if (this.serverSockPool != 0L) {
            Pool.destroy(this.serverSockPool);
            this.serverSockPool = 0L;
        }
        if (this.serverSock != 0L) {
            Socket.close(this.serverSock);
            this.serverSock = 0L;
        }
        this.sslContext = 0L;
        if (this.rootPool != 0L) {
            Pool.destroy(this.rootPool);
            this.rootPool = 0L;
        }
        this.handler.recycle();
    }
    
    @Override
    protected AbstractEndpoint.Acceptor createAcceptor() {
        return new Acceptor();
    }
    
    protected boolean setSocketOptions(final long socket) {
        int step = 1;
        try {
            if (this.socketProperties.getSoLingerOn() && this.socketProperties.getSoLingerTime() >= 0) {
                Socket.optSet(socket, 1, this.socketProperties.getSoLingerTime());
            }
            if (this.socketProperties.getTcpNoDelay()) {
                Socket.optSet(socket, 512, this.socketProperties.getTcpNoDelay() ? 1 : 0);
            }
            Socket.timeoutSet(socket, this.socketProperties.getSoTimeout() * 1000);
            step = 2;
            if (this.sslContext != 0L) {
                SSLSocket.attach(this.sslContext, socket);
                if (SSLSocket.handshake(socket) != 0) {
                    if (AprEndpoint.log.isDebugEnabled()) {
                        AprEndpoint.log.debug((Object)(AprEndpoint.sm.getString("endpoint.err.handshake") + ": " + SSL.getLastError()));
                    }
                    return false;
                }
            }
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            if (AprEndpoint.log.isDebugEnabled()) {
                if (step == 2) {
                    AprEndpoint.log.debug((Object)AprEndpoint.sm.getString("endpoint.err.handshake"), t);
                }
                else {
                    AprEndpoint.log.debug((Object)AprEndpoint.sm.getString("endpoint.err.unexpected"), t);
                }
            }
            return false;
        }
        return true;
    }
    
    protected long allocatePoller(final int size, final long pool, final int timeout) {
        try {
            return Poll.create(size, pool, 0, timeout * 1000);
        }
        catch (Error e) {
            if (Status.APR_STATUS_IS_EINVAL(e.getError())) {
                AprEndpoint.log.info((Object)AprEndpoint.sm.getString("endpoint.poll.limitedpollsize", new Object[] { "" + size }));
                return 0L;
            }
            AprEndpoint.log.error((Object)AprEndpoint.sm.getString("endpoint.poll.initfail"), (Throwable)e);
            return -1L;
        }
    }
    
    protected boolean processSocketWithOptions(final long socket) {
        try {
            if (this.running) {
                if (AprEndpoint.log.isDebugEnabled()) {
                    AprEndpoint.log.debug((Object)AprEndpoint.sm.getString("endpoint.debug.socket", new Object[] { socket }));
                }
                final AprSocketWrapper wrapper = new AprSocketWrapper(Long.valueOf(socket));
                wrapper.setKeepAliveLeft(this.getMaxKeepAliveRequests());
                wrapper.setSecure(this.isSSLEnabled());
                this.connections.put(socket, wrapper);
                this.getExecutor().execute(new SocketWithOptionsProcessor(wrapper));
            }
        }
        catch (RejectedExecutionException x) {
            AprEndpoint.log.warn((Object)("Socket processing request was rejected for:" + socket), (Throwable)x);
            return false;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            AprEndpoint.log.error((Object)AprEndpoint.sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }
    
    public boolean processSocket(final long socket, final SocketStatus status) {
        try {
            final Executor executor = this.getExecutor();
            if (executor == null) {
                AprEndpoint.log.warn((Object)AprEndpoint.sm.getString("endpoint.warn.noExector", new Object[] { socket, null }));
            }
            else {
                final SocketWrapper<Long> wrapper = this.connections.get(socket);
                if (wrapper != null) {
                    executor.execute(new SocketProcessor(wrapper, status));
                }
            }
        }
        catch (RejectedExecutionException x) {
            AprEndpoint.log.warn((Object)("Socket processing request was rejected for:" + socket), (Throwable)x);
            return false;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            AprEndpoint.log.error((Object)AprEndpoint.sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }
    
    public boolean processSocketAsync(final SocketWrapper<Long> socket, final SocketStatus status) {
        try {
            synchronized (socket) {
                if (this.waitingRequests.remove(socket)) {
                    final SocketProcessor proc = new SocketProcessor(socket, status);
                    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    try {
                        if (Constants.IS_SECURITY_ENABLED) {
                            final PrivilegedAction<Void> pa = new PrivilegedSetTccl(this.getClass().getClassLoader());
                            AccessController.doPrivileged(pa);
                        }
                        else {
                            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                        }
                        final Executor executor = this.getExecutor();
                        if (executor == null) {
                            AprEndpoint.log.warn((Object)AprEndpoint.sm.getString("endpoint.warn.noExector", new Object[] { socket, status }));
                            return false;
                        }
                        executor.execute(proc);
                    }
                    finally {
                        if (Constants.IS_SECURITY_ENABLED) {
                            final PrivilegedAction<Void> pa2 = new PrivilegedSetTccl(loader);
                            AccessController.doPrivileged(pa2);
                        }
                        else {
                            Thread.currentThread().setContextClassLoader(loader);
                        }
                    }
                }
            }
        }
        catch (RejectedExecutionException x) {
            AprEndpoint.log.warn((Object)("Socket processing request was rejected for: " + socket), (Throwable)x);
            return false;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            AprEndpoint.log.error((Object)AprEndpoint.sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }
    
    private void closeSocket(final long socket) {
        final Poller poller = this.poller;
        if (poller != null && !poller.close(socket)) {
            this.destroySocket(socket);
        }
    }
    
    private void destroySocket(final long socket) {
        this.connections.remove(socket);
        if (AprEndpoint.log.isDebugEnabled()) {
            final String msg = AprEndpoint.sm.getString("endpoint.debug.destroySocket", new Object[] { socket });
            if (AprEndpoint.log.isTraceEnabled()) {
                AprEndpoint.log.trace((Object)msg, (Throwable)new Exception());
            }
            else {
                AprEndpoint.log.debug((Object)msg);
            }
        }
        if (socket != 0L) {
            Socket.destroy(socket);
            this.countDownConnection();
        }
    }
    
    @Override
    protected Log getLog() {
        return AprEndpoint.log;
    }
    
    static {
        log = LogFactory.getLog((Class)AprEndpoint.class);
    }
    
    protected class Acceptor extends AbstractEndpoint.Acceptor
    {
        private final Log log;
        
        protected Acceptor() {
            this.log = LogFactory.getLog((Class)Acceptor.class);
        }
        
        @Override
        public void run() {
            int errorDelay = 0;
            while (AprEndpoint.this.running) {
                while (AprEndpoint.this.paused && AprEndpoint.this.running) {
                    this.state = AcceptorState.PAUSED;
                    try {
                        Thread.sleep(50L);
                    }
                    catch (InterruptedException e3) {}
                }
                if (!AprEndpoint.this.running) {
                    break;
                }
                this.state = AcceptorState.RUNNING;
                try {
                    AprEndpoint.this.countUpOrAwaitConnection();
                    long socket = 0L;
                    try {
                        socket = Socket.accept(AprEndpoint.this.serverSock);
                        if (this.log.isDebugEnabled()) {
                            final long sa = Address.get(1, socket);
                            final Sockaddr addr = Address.getInfo(sa);
                            this.log.debug((Object)AbstractEndpoint.sm.getString("endpoint.apr.remoteport", new Object[] { socket, addr.port }));
                        }
                    }
                    catch (Exception e) {
                        AprEndpoint.this.countDownConnection();
                        errorDelay = AprEndpoint.this.handleExceptionWithDelay(errorDelay);
                        throw e;
                    }
                    errorDelay = 0;
                    if (AprEndpoint.this.running && !AprEndpoint.this.paused) {
                        if (AprEndpoint.this.processSocketWithOptions(socket)) {
                            continue;
                        }
                        AprEndpoint.this.closeSocket(socket);
                    }
                    else {
                        AprEndpoint.this.destroySocket(socket);
                    }
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    if (!AprEndpoint.this.running) {
                        continue;
                    }
                    final String msg = AbstractEndpoint.sm.getString("endpoint.accept.fail");
                    if (t instanceof Error) {
                        final Error e2 = (Error)t;
                        if (e2.getError() == 233) {
                            this.log.warn((Object)msg, t);
                        }
                        else {
                            this.log.error((Object)msg, t);
                        }
                    }
                    else {
                        this.log.error((Object)msg, t);
                    }
                }
            }
            this.state = AcceptorState.ENDED;
        }
    }
    
    protected class AsyncTimeout implements Runnable
    {
        private volatile boolean asyncTimeoutRunning;
        
        protected AsyncTimeout() {
            this.asyncTimeoutRunning = true;
        }
        
        @Override
        public void run() {
            while (this.asyncTimeoutRunning) {
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException ex) {}
                final long now = System.currentTimeMillis();
                for (final SocketWrapper<Long> socket : AprEndpoint.this.waitingRequests) {
                    if (socket.async) {
                        final long access = socket.getLastAccess();
                        if (socket.getTimeout() <= 0L || now - access <= socket.getTimeout()) {
                            continue;
                        }
                        AprEndpoint.this.processSocketAsync(socket, SocketStatus.TIMEOUT);
                    }
                }
                while (AprEndpoint.this.paused && this.asyncTimeoutRunning) {
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e) {}
                }
            }
        }
        
        protected void stop() {
            this.asyncTimeoutRunning = false;
        }
    }
    
    public static class SocketInfo
    {
        public long socket;
        public int timeout;
        public int flags;
        
        public boolean read() {
            return (this.flags & 0x1) == 0x1;
        }
        
        public boolean write() {
            return (this.flags & 0x4) == 0x4;
        }
        
        public static int merge(final int flag1, final int flag2) {
            return (flag1 & 0x1) | (flag2 & 0x1) | ((flag1 & 0x4) | (flag2 & 0x4));
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Socket: [");
            sb.append(this.socket);
            sb.append("], timeout: [");
            sb.append(this.timeout);
            sb.append("], flags: [");
            sb.append(this.flags);
            return sb.toString();
        }
    }
    
    public class SocketTimeouts
    {
        protected int size;
        protected long[] sockets;
        protected long[] timeouts;
        protected int pos;
        
        public SocketTimeouts(final int size) {
            this.pos = 0;
            this.size = 0;
            this.sockets = new long[size];
            this.timeouts = new long[size];
        }
        
        public void add(final long socket, final long timeout) {
            this.sockets[this.size] = socket;
            this.timeouts[this.size] = timeout;
            ++this.size;
        }
        
        public long remove(final long socket) {
            long result = 0L;
            for (int i = 0; i < this.size; ++i) {
                if (this.sockets[i] == socket) {
                    result = this.timeouts[i];
                    this.sockets[i] = this.sockets[this.size - 1];
                    this.timeouts[i] = this.timeouts[this.size - 1];
                    --this.size;
                    break;
                }
            }
            return result;
        }
        
        public long check(final long date) {
            while (this.pos < this.size) {
                if (date >= this.timeouts[this.pos]) {
                    final long result = this.sockets[this.pos];
                    this.sockets[this.pos] = this.sockets[this.size - 1];
                    this.timeouts[this.pos] = this.timeouts[this.size - 1];
                    --this.size;
                    return result;
                }
                ++this.pos;
            }
            this.pos = 0;
            return 0L;
        }
    }
    
    public class SocketList
    {
        protected int size;
        protected int pos;
        protected long[] sockets;
        protected int[] timeouts;
        protected int[] flags;
        protected SocketInfo info;
        
        public SocketList(final int size) {
            this.info = new SocketInfo();
            this.size = 0;
            this.pos = 0;
            this.sockets = new long[size];
            this.timeouts = new int[size];
            this.flags = new int[size];
        }
        
        public int size() {
            return this.size;
        }
        
        public SocketInfo get() {
            if (this.pos == this.size) {
                return null;
            }
            this.info.socket = this.sockets[this.pos];
            this.info.timeout = this.timeouts[this.pos];
            this.info.flags = this.flags[this.pos];
            ++this.pos;
            return this.info;
        }
        
        public void clear() {
            this.size = 0;
            this.pos = 0;
        }
        
        public boolean add(final long socket, final int timeout, final int flag) {
            if (this.size == this.sockets.length) {
                return false;
            }
            for (int i = 0; i < this.size; ++i) {
                if (this.sockets[i] == socket) {
                    this.flags[i] = SocketInfo.merge(this.flags[i], flag);
                    return true;
                }
            }
            this.sockets[this.size] = socket;
            this.timeouts[this.size] = timeout;
            this.flags[this.size] = flag;
            ++this.size;
            return true;
        }
        
        public boolean remove(final long socket) {
            for (int i = 0; i < this.size; ++i) {
                if (this.sockets[i] == socket) {
                    this.sockets[i] = this.sockets[this.size - 1];
                    this.timeouts[i] = this.timeouts[this.size - 1];
                    this.flags[this.size] = this.flags[this.size - 1];
                    --this.size;
                    return true;
                }
            }
            return false;
        }
        
        public void duplicate(final SocketList copy) {
            copy.size = this.size;
            copy.pos = this.pos;
            System.arraycopy(this.sockets, 0, copy.sockets, 0, this.size);
            System.arraycopy(this.timeouts, 0, copy.timeouts, 0, this.size);
            System.arraycopy(this.flags, 0, copy.flags, 0, this.size);
        }
    }
    
    public class Poller implements Runnable
    {
        protected long[] pollers;
        protected int actualPollerSize;
        protected int[] pollerSpace;
        protected int pollerCount;
        protected int pollerTime;
        private int nextPollerTime;
        protected long pool;
        protected long[] desc;
        protected SocketList addList;
        private SocketList closeList;
        protected SocketTimeouts timeouts;
        protected long lastMaintain;
        private AtomicInteger connectionCount;
        private volatile boolean pollerRunning;
        
        public Poller() {
            this.pollers = null;
            this.actualPollerSize = 0;
            this.pollerSpace = null;
            this.pool = 0L;
            this.addList = null;
            this.closeList = null;
            this.timeouts = null;
            this.lastMaintain = System.currentTimeMillis();
            this.connectionCount = new AtomicInteger(0);
            this.pollerRunning = true;
        }
        
        public int getConnectionCount() {
            return this.connectionCount.get();
        }
        
        protected void init() {
            this.pool = Pool.create(AprEndpoint.this.serverSockPool);
            final int defaultPollerSize = AprEndpoint.this.getMaxConnections();
            if ((OS.IS_WIN32 || OS.IS_WIN64) && defaultPollerSize > 1024) {
                this.actualPollerSize = 1024;
            }
            else {
                this.actualPollerSize = defaultPollerSize;
            }
            this.timeouts = new SocketTimeouts(defaultPollerSize);
            long pollset = AprEndpoint.this.allocatePoller(this.actualPollerSize, this.pool, -1);
            if (pollset == 0L && this.actualPollerSize > 1024) {
                this.actualPollerSize = 1024;
                pollset = AprEndpoint.this.allocatePoller(this.actualPollerSize, this.pool, -1);
            }
            if (pollset == 0L) {
                this.actualPollerSize = 62;
                pollset = AprEndpoint.this.allocatePoller(this.actualPollerSize, this.pool, -1);
            }
            this.pollerCount = defaultPollerSize / this.actualPollerSize;
            this.pollerTime = AprEndpoint.this.pollTime / this.pollerCount;
            this.nextPollerTime = this.pollerTime;
            (this.pollers = new long[this.pollerCount])[0] = pollset;
            for (int i = 1; i < this.pollerCount; ++i) {
                this.pollers[i] = AprEndpoint.this.allocatePoller(this.actualPollerSize, this.pool, -1);
            }
            this.pollerSpace = new int[this.pollerCount];
            for (int i = 0; i < this.pollerCount; ++i) {
                this.pollerSpace[i] = this.actualPollerSize;
            }
            this.desc = new long[this.actualPollerSize * 2];
            this.connectionCount.set(0);
            this.addList = new SocketList(defaultPollerSize);
            this.closeList = new SocketList(defaultPollerSize);
        }
        
        protected synchronized void stop() {
            this.pollerRunning = false;
        }
        
        protected void destroy() {
            try {
                synchronized (this) {
                    this.notify();
                    this.wait(AprEndpoint.this.pollTime / 1000);
                }
            }
            catch (InterruptedException ex) {}
            for (SocketInfo info = this.addList.get(); info != null; info = this.addList.get()) {
                final boolean comet = AprEndpoint.this.connections.get(info.socket).isComet();
                if (!comet || (comet && !AprEndpoint.this.processSocket(info.socket, SocketStatus.STOP))) {
                    AprEndpoint.this.destroySocket(info.socket);
                }
            }
            this.addList.clear();
            for (int i = 0; i < this.pollerCount; ++i) {
                final int rv = Poll.pollset(this.pollers[i], this.desc);
                if (rv > 0) {
                    for (int n = 0; n < rv; ++n) {
                        final boolean comet2 = AprEndpoint.this.connections.get(this.desc[n * 2 + 1]).isComet();
                        if (!comet2 || (comet2 && !AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.STOP))) {
                            AprEndpoint.this.destroySocket(this.desc[n * 2 + 1]);
                        }
                    }
                }
            }
            Pool.destroy(this.pool);
            this.connectionCount.set(0);
        }
        
        public void add(final long socket, final int timeout, final boolean read, final boolean write) {
            this.add(socket, timeout, (read ? 1 : 0) | (write ? 4 : 0));
        }
        
        private void add(final long socket, int timeout, final int flags) {
            if (AprEndpoint.log.isDebugEnabled()) {
                final String msg = AbstractEndpoint.sm.getString("endpoint.debug.pollerAdd", new Object[] { socket, timeout, flags });
                if (AprEndpoint.log.isTraceEnabled()) {
                    AprEndpoint.log.trace((Object)msg, (Throwable)new Exception());
                }
                else {
                    AprEndpoint.log.debug((Object)msg);
                }
            }
            if (timeout <= 0) {
                timeout = Integer.MAX_VALUE;
            }
            boolean ok = false;
            synchronized (this) {
                if (this.pollerRunning && this.addList.add(socket, timeout, flags)) {
                    ok = true;
                    this.notify();
                }
            }
            if (!ok) {
                final boolean comet = AprEndpoint.this.connections.get(socket).isComet();
                if (!comet || (comet && !AprEndpoint.this.processSocket(socket, SocketStatus.ERROR))) {
                    AprEndpoint.this.closeSocket(socket);
                }
            }
        }
        
        protected boolean addToPoller(final long socket, final int events) {
            int rv = -1;
            for (int i = 0; i < this.pollers.length; ++i) {
                if (this.pollerSpace[i] > 0) {
                    rv = Poll.add(this.pollers[i], socket, events);
                    if (rv == 0) {
                        final int[] pollerSpace = this.pollerSpace;
                        final int n = i;
                        --pollerSpace[n];
                        this.connectionCount.incrementAndGet();
                        return true;
                    }
                }
            }
            return false;
        }
        
        protected boolean close(final long socket) {
            if (!this.pollerRunning) {
                return false;
            }
            synchronized (this) {
                if (!this.pollerRunning) {
                    return false;
                }
                this.closeList.add(socket, 0, 0);
                this.notify();
                return true;
            }
        }
        
        private boolean removeFromPoller(final long socket) {
            if (AprEndpoint.log.isDebugEnabled()) {
                AprEndpoint.log.debug((Object)AbstractEndpoint.sm.getString("endpoint.debug.pollerRemove", new Object[] { socket }));
            }
            int rv = -1;
            for (int i = 0; i < this.pollers.length; ++i) {
                if (this.pollerSpace[i] < this.actualPollerSize) {
                    rv = Poll.remove(this.pollers[i], socket);
                    if (rv != 70015) {
                        final int[] pollerSpace = this.pollerSpace;
                        final int n = i;
                        ++pollerSpace[n];
                        this.connectionCount.decrementAndGet();
                        if (AprEndpoint.log.isDebugEnabled()) {
                            AprEndpoint.log.debug((Object)AbstractEndpoint.sm.getString("endpoint.debug.pollerRemoved", new Object[] { socket }));
                            break;
                        }
                        break;
                    }
                }
            }
            this.timeouts.remove(socket);
            return rv == 0;
        }
        
        protected void maintain() {
            final long date = System.currentTimeMillis();
            if (date - this.lastMaintain < 5000L) {
                return;
            }
            this.lastMaintain = date;
            for (long socket = this.timeouts.check(date); socket != 0L; socket = this.timeouts.check(date)) {
                if (AprEndpoint.log.isDebugEnabled()) {
                    AprEndpoint.log.debug((Object)AbstractEndpoint.sm.getString("endpoint.debug.socketTimeout", new Object[] { socket }));
                }
                this.removeFromPoller(socket);
                final boolean comet = AprEndpoint.this.connections.get(socket).isComet();
                if (!comet || (comet && !AprEndpoint.this.processSocket(socket, SocketStatus.TIMEOUT))) {
                    AprEndpoint.this.destroySocket(socket);
                }
            }
        }
        
        @Override
        public String toString() {
            final StringBuffer buf = new StringBuffer();
            buf.append("Poller");
            final long[] res = new long[this.actualPollerSize * 2];
            for (int i = 0; i < this.pollers.length; ++i) {
                final int count = Poll.pollset(this.pollers[i], res);
                buf.append(" [ ");
                for (int j = 0; j < count; ++j) {
                    buf.append(this.desc[2 * j + 1]).append(" ");
                }
                buf.append("]");
            }
            return buf.toString();
        }
        
        @Override
        public void run() {
            int maintain = 0;
            final SocketList localAddList = new SocketList(AprEndpoint.this.getMaxConnections());
            final SocketList localCloseList = new SocketList(AprEndpoint.this.getMaxConnections());
            while (this.pollerRunning) {
                while (this.pollerRunning && AprEndpoint.this.paused) {
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e) {}
                }
                while (this.pollerRunning && this.connectionCount.get() < 1 && this.addList.size() < 1 && this.closeList.size() < 1) {
                    try {
                        if (AprEndpoint.this.getSoTimeout() > 0 && this.pollerRunning) {
                            this.maintain();
                        }
                        synchronized (this) {
                            this.wait(10000L);
                        }
                    }
                    catch (InterruptedException e) {}
                    catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        AprEndpoint.this.getLog().warn((Object)AbstractEndpoint.sm.getString("endpoint.timeout.err"));
                    }
                }
                if (!this.pollerRunning) {
                    break;
                }
                try {
                    if (this.closeList.size() > 0) {
                        synchronized (this) {
                            this.closeList.duplicate(localCloseList);
                            this.closeList.clear();
                        }
                    }
                    else {
                        localCloseList.clear();
                    }
                    if (this.addList.size() > 0) {
                        synchronized (this) {
                            this.addList.duplicate(localAddList);
                            this.addList.clear();
                        }
                    }
                    else {
                        localAddList.clear();
                    }
                    if (localCloseList.size() > 0) {
                        for (SocketInfo info = localCloseList.get(); info != null; info = localCloseList.get()) {
                            localAddList.remove(info.socket);
                            this.removeFromPoller(info.socket);
                            AprEndpoint.this.destroySocket(info.socket);
                        }
                    }
                    if (localAddList.size() > 0) {
                        SocketInfo info = localAddList.get();
                        while (info != null) {
                            if (AprEndpoint.log.isDebugEnabled()) {
                                AprEndpoint.log.debug((Object)AbstractEndpoint.sm.getString("endpoint.debug.pollerAddDo", new Object[] { info.socket }));
                            }
                            this.timeouts.remove(info.socket);
                            final AprSocketWrapper wrapper = AprEndpoint.this.connections.get(info.socket);
                            if (wrapper == null) {
                                continue;
                            }
                            if (info.read() || info.write()) {
                                final boolean comet = wrapper.isComet();
                                if (comet || wrapper.pollerFlags != 0) {
                                    this.removeFromPoller(info.socket);
                                }
                                wrapper.pollerFlags = (wrapper.pollerFlags | (info.read() ? 1 : 0) | (info.write() ? 4 : 0));
                                if (!this.addToPoller(info.socket, wrapper.pollerFlags)) {
                                    if (!comet || (comet && !AprEndpoint.this.processSocket(info.socket, SocketStatus.ERROR))) {
                                        AprEndpoint.this.closeSocket(info.socket);
                                    }
                                }
                                else {
                                    this.timeouts.add(info.socket, System.currentTimeMillis() + info.timeout);
                                }
                            }
                            else {
                                AprEndpoint.this.closeSocket(info.socket);
                                AprEndpoint.this.getLog().warn((Object)AbstractEndpoint.sm.getString("endpoint.apr.pollAddInvalid", new Object[] { info }));
                            }
                            info = localAddList.get();
                        }
                    }
                    for (int i = 0; i < this.pollers.length; ++i) {
                        boolean reset = false;
                        int rv = 0;
                        if (this.pollerSpace[i] < this.actualPollerSize) {
                            rv = Poll.poll(this.pollers[i], this.nextPollerTime, this.desc, true);
                            this.nextPollerTime = this.pollerTime;
                        }
                        else {
                            this.nextPollerTime += this.pollerTime;
                        }
                        if (rv > 0) {
                            final int[] pollerSpace = this.pollerSpace;
                            final int n2 = i;
                            pollerSpace[n2] += rv;
                            this.connectionCount.addAndGet(-rv);
                            for (int n = 0; n < rv; ++n) {
                                long timeout = this.timeouts.remove(this.desc[n * 2 + 1]);
                                final AprSocketWrapper wrapper2 = AprEndpoint.this.connections.get(this.desc[n * 2 + 1]);
                                if (AprEndpoint.this.getLog().isDebugEnabled()) {
                                    AprEndpoint.log.debug((Object)AbstractEndpoint.sm.getString("endpoint.debug.pollerProcess", new Object[] { this.desc[n * 2 + 1], this.desc[n * 2] }));
                                }
                                wrapper2.pollerFlags &= ~(int)this.desc[n * 2];
                                if (wrapper2.isComet()) {
                                    if ((this.desc[n * 2] & 0x20L) == 0x20L || (this.desc[n * 2] & 0x10L) == 0x10L || (this.desc[n * 2] & 0x40L) == 0x40L) {
                                        if (!AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.ERROR)) {
                                            AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                        }
                                    }
                                    else if ((this.desc[n * 2] & 0x1L) == 0x1L) {
                                        if (wrapper2.pollerFlags != 0) {
                                            this.add(this.desc[n * 2 + 1], 1, wrapper2.pollerFlags);
                                        }
                                        if (!AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.OPEN_READ)) {
                                            AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                        }
                                    }
                                    else if ((this.desc[n * 2] & 0x4L) == 0x4L) {
                                        if (wrapper2.pollerFlags != 0) {
                                            this.add(this.desc[n * 2 + 1], 1, wrapper2.pollerFlags);
                                        }
                                        if (!AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.OPEN_WRITE)) {
                                            AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                        }
                                    }
                                    else {
                                        AprEndpoint.this.getLog().warn((Object)AbstractEndpoint.sm.getString("endpoint.apr.pollUnknownEvent", new Object[] { this.desc[n * 2] }));
                                        if (!AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.ERROR)) {
                                            AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                        }
                                    }
                                }
                                else if ((this.desc[n * 2] & 0x20L) == 0x20L || (this.desc[n * 2] & 0x10L) == 0x10L || (this.desc[n * 2] & 0x40L) == 0x40L) {
                                    if (wrapper2.isUpgraded()) {
                                        if ((this.desc[n * 2] & 0x1L) == 0x1L) {
                                            if (!AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.OPEN_READ)) {
                                                AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                            }
                                        }
                                        else if ((this.desc[n * 2] & 0x4L) == 0x4L) {
                                            if (!AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.OPEN_WRITE)) {
                                                AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                            }
                                        }
                                        else if ((wrapper2.pollerFlags & 0x1) == 0x1) {
                                            if (!AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.OPEN_READ)) {
                                                AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                            }
                                        }
                                        else if ((wrapper2.pollerFlags & 0x4) == 0x4) {
                                            if (!AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.OPEN_WRITE)) {
                                                AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                            }
                                        }
                                        else {
                                            AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                        }
                                    }
                                    else {
                                        AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                    }
                                }
                                else if ((this.desc[n * 2] & 0x1L) == 0x1L || (this.desc[n * 2] & 0x4L) == 0x4L) {
                                    boolean error = false;
                                    if ((this.desc[n * 2] & 0x1L) == 0x1L && !AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.OPEN_READ)) {
                                        error = true;
                                        AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                    }
                                    if (!error && (this.desc[n * 2] & 0x4L) == 0x4L && !AprEndpoint.this.processSocket(this.desc[n * 2 + 1], SocketStatus.OPEN_WRITE)) {
                                        error = true;
                                        AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                    }
                                    if (!error && wrapper2.pollerFlags != 0) {
                                        if (timeout > 0L) {
                                            timeout -= System.currentTimeMillis();
                                        }
                                        if (timeout <= 0L) {
                                            timeout = 1L;
                                        }
                                        if (timeout > 2147483647L) {
                                            timeout = 2147483647L;
                                        }
                                        this.add(this.desc[n * 2 + 1], (int)timeout, wrapper2.pollerFlags);
                                    }
                                }
                                else {
                                    AprEndpoint.this.getLog().warn((Object)AbstractEndpoint.sm.getString("endpoint.apr.pollUnknownEvent", new Object[] { this.desc[n * 2] }));
                                    AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                                }
                            }
                        }
                        else if (rv < 0) {
                            int errn = -rv;
                            if (errn != 120001 && errn != 120003) {
                                if (errn > 120000) {
                                    errn -= 120000;
                                }
                                AprEndpoint.this.getLog().error((Object)AbstractEndpoint.sm.getString("endpoint.apr.pollError", new Object[] { errn, Error.strerror(errn) }));
                                reset = true;
                            }
                        }
                        if (reset) {
                            final int count = Poll.pollset(this.pollers[i], this.desc);
                            final long newPoller = AprEndpoint.this.allocatePoller(this.actualPollerSize, this.pool, -1);
                            this.pollerSpace[i] = this.actualPollerSize;
                            this.connectionCount.addAndGet(-count);
                            Poll.destroy(this.pollers[i]);
                            this.pollers[i] = newPoller;
                        }
                    }
                    if (AprEndpoint.this.getSoTimeout() <= 0 || maintain++ <= 1000 || !this.pollerRunning) {
                        continue;
                    }
                    maintain = 0;
                    this.maintain();
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    if (maintain == 0) {
                        AprEndpoint.this.getLog().warn((Object)AbstractEndpoint.sm.getString("endpoint.timeout.error"), t);
                    }
                    else {
                        AprEndpoint.this.getLog().warn((Object)AbstractEndpoint.sm.getString("endpoint.poll.error"), t);
                    }
                }
            }
            synchronized (this) {
                this.notifyAll();
            }
        }
    }
    
    public static class SendfileData
    {
        public String fileName;
        public long fd;
        public long fdpool;
        public long start;
        public long end;
        public long socket;
        public long pos;
        public boolean keepAlive;
    }
    
    public class Sendfile implements Runnable
    {
        protected long sendfilePollset;
        protected long pool;
        protected long[] desc;
        protected HashMap<Long, SendfileData> sendfileData;
        protected int sendfileCount;
        protected ArrayList<SendfileData> addS;
        private volatile boolean sendfileRunning;
        
        public Sendfile() {
            this.sendfilePollset = 0L;
            this.pool = 0L;
            this.sendfileRunning = true;
        }
        
        public int getSendfileCount() {
            return this.sendfileCount;
        }
        
        protected void init() {
            this.pool = Pool.create(AprEndpoint.this.serverSockPool);
            int size = AprEndpoint.this.sendfileSize;
            if (size <= 0) {
                size = ((OS.IS_WIN32 || OS.IS_WIN64) ? 1024 : 16384);
            }
            this.sendfilePollset = AprEndpoint.this.allocatePoller(size, this.pool, AprEndpoint.this.getSoTimeout());
            if (this.sendfilePollset == 0L && size > 1024) {
                size = 1024;
                this.sendfilePollset = AprEndpoint.this.allocatePoller(size, this.pool, AprEndpoint.this.getSoTimeout());
            }
            if (this.sendfilePollset == 0L) {
                size = 62;
                this.sendfilePollset = AprEndpoint.this.allocatePoller(size, this.pool, AprEndpoint.this.getSoTimeout());
            }
            this.desc = new long[size * 2];
            this.sendfileData = new HashMap<Long, SendfileData>(size);
            this.addS = new ArrayList<SendfileData>();
        }
        
        protected void destroy() {
            this.sendfileRunning = false;
            try {
                synchronized (this) {
                    this.notify();
                    this.wait(AprEndpoint.this.pollTime / 1000);
                }
            }
            catch (InterruptedException ex) {}
            for (int i = this.addS.size() - 1; i >= 0; --i) {
                final SendfileData data = this.addS.get(i);
                AprEndpoint.this.closeSocket(data.socket);
            }
            final int rv = Poll.pollset(this.sendfilePollset, this.desc);
            if (rv > 0) {
                for (int n = 0; n < rv; ++n) {
                    AprEndpoint.this.closeSocket(this.desc[n * 2 + 1]);
                }
            }
            Pool.destroy(this.pool);
            this.sendfileData.clear();
        }
        
        public boolean add(final SendfileData data) {
            try {
                data.fdpool = Socket.pool(data.socket);
                data.fd = File.open(data.fileName, 4129, 0, data.fdpool);
                data.pos = data.start;
                Socket.timeoutSet(data.socket, 0L);
                while (true) {
                    final long nw = Socket.sendfilen(data.socket, data.fd, data.pos, data.end - data.pos, 0);
                    if (nw < 0L) {
                        if (-nw != 120002L) {
                            Pool.destroy(data.fdpool);
                            data.socket = 0L;
                            return false;
                        }
                        break;
                    }
                    else {
                        data.pos += nw;
                        if (data.pos >= data.end) {
                            Pool.destroy(data.fdpool);
                            Socket.timeoutSet(data.socket, AprEndpoint.this.getSoTimeout() * 1000);
                            return true;
                        }
                        continue;
                    }
                }
            }
            catch (Exception e) {
                AprEndpoint.log.warn((Object)AbstractEndpoint.sm.getString("endpoint.sendfile.error"), (Throwable)e);
                return false;
            }
            synchronized (this) {
                this.addS.add(data);
                this.notify();
            }
            return false;
        }
        
        protected void remove(final SendfileData data) {
            final int rv = Poll.remove(this.sendfilePollset, data.socket);
            if (rv == 0) {
                --this.sendfileCount;
            }
            this.sendfileData.remove(new Long(data.socket));
        }
        
        @Override
        public void run() {
            long maintainTime = 0L;
            while (this.sendfileRunning) {
                while (this.sendfileRunning && AprEndpoint.this.paused) {
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e) {}
                }
                while (this.sendfileRunning && this.sendfileCount < 1 && this.addS.size() < 1) {
                    maintainTime = 0L;
                    try {
                        synchronized (this) {
                            this.wait();
                        }
                    }
                    catch (InterruptedException e) {}
                }
                if (!this.sendfileRunning) {
                    break;
                }
                try {
                    if (this.addS.size() > 0) {
                        synchronized (this) {
                            for (int i = this.addS.size() - 1; i >= 0; --i) {
                                final SendfileData data = this.addS.get(i);
                                final int rv = Poll.add(this.sendfilePollset, data.socket, 4);
                                if (rv == 0) {
                                    this.sendfileData.put(new Long(data.socket), data);
                                    ++this.sendfileCount;
                                }
                                else {
                                    AprEndpoint.this.getLog().warn((Object)AbstractEndpoint.sm.getString("endpoint.sendfile.addfail", new Object[] { rv, Error.strerror(rv) }));
                                    AprEndpoint.this.closeSocket(data.socket);
                                }
                            }
                            this.addS.clear();
                        }
                    }
                    maintainTime += AprEndpoint.this.pollTime;
                    int rv2 = Poll.poll(this.sendfilePollset, AprEndpoint.this.pollTime, this.desc, false);
                    if (rv2 > 0) {
                        for (int n = 0; n < rv2; ++n) {
                            final SendfileData state = this.sendfileData.get(new Long(this.desc[n * 2 + 1]));
                            if ((this.desc[n * 2] & 0x20L) == 0x20L || (this.desc[n * 2] & 0x10L) == 0x10L) {
                                this.remove(state);
                                AprEndpoint.this.closeSocket(state.socket);
                            }
                            else {
                                final long nw = Socket.sendfilen(state.socket, state.fd, state.pos, state.end - state.pos, 0);
                                if (nw < 0L) {
                                    this.remove(state);
                                    AprEndpoint.this.closeSocket(state.socket);
                                }
                                else {
                                    state.pos += nw;
                                    if (state.pos >= state.end) {
                                        this.remove(state);
                                        if (state.keepAlive) {
                                            Pool.destroy(state.fdpool);
                                            Socket.timeoutSet(state.socket, AprEndpoint.this.getSoTimeout() * 1000);
                                            AprEndpoint.this.getPoller().add(state.socket, AprEndpoint.this.getKeepAliveTimeout(), true, false);
                                        }
                                        else {
                                            AprEndpoint.this.closeSocket(state.socket);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else if (rv2 < 0) {
                        int errn = -rv2;
                        if (errn != 120001 && errn != 120003) {
                            if (errn > 120000) {
                                errn -= 120000;
                            }
                            AprEndpoint.this.getLog().error((Object)AbstractEndpoint.sm.getString("Unexpected poller error", new Object[] { errn, Error.strerror(errn) }));
                            synchronized (this) {
                                this.destroy();
                                this.init();
                            }
                            continue;
                        }
                    }
                    if (AprEndpoint.this.getSoTimeout() <= 0 || maintainTime <= 1000000L || !this.sendfileRunning) {
                        continue;
                    }
                    rv2 = Poll.maintain(this.sendfilePollset, this.desc, false);
                    maintainTime = 0L;
                    if (rv2 <= 0) {
                        continue;
                    }
                    for (int n = 0; n < rv2; ++n) {
                        final SendfileData state = this.sendfileData.get(new Long(this.desc[n]));
                        this.remove(state);
                        AprEndpoint.this.closeSocket(state.socket);
                    }
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    AprEndpoint.this.getLog().error((Object)AbstractEndpoint.sm.getString("endpoint.poll.error"), t);
                }
            }
            synchronized (this) {
                this.notifyAll();
            }
        }
    }
    
    protected class SocketWithOptionsProcessor implements Runnable
    {
        protected SocketWrapper<Long> socket;
        
        public SocketWithOptionsProcessor(final SocketWrapper<Long> socket) {
            this.socket = null;
            this.socket = socket;
        }
        
        @Override
        public void run() {
            synchronized (this.socket) {
                if (!AprEndpoint.this.deferAccept) {
                    if (AprEndpoint.this.setSocketOptions(this.socket.getSocket())) {
                        AprEndpoint.this.getPoller().add(this.socket.getSocket(), AprEndpoint.this.getSoTimeout(), true, false);
                    }
                    else {
                        AprEndpoint.this.closeSocket(this.socket.getSocket());
                        this.socket = null;
                    }
                }
                else {
                    if (!AprEndpoint.this.setSocketOptions(this.socket.getSocket())) {
                        AprEndpoint.this.closeSocket(this.socket.getSocket());
                        this.socket = null;
                        return;
                    }
                    final AbstractEndpoint.Handler.SocketState state = AprEndpoint.this.handler.process(this.socket, SocketStatus.OPEN_READ);
                    if (state == AbstractEndpoint.Handler.SocketState.CLOSED) {
                        AprEndpoint.this.closeSocket(this.socket.getSocket());
                        this.socket = null;
                    }
                    else if (state == AbstractEndpoint.Handler.SocketState.LONG) {
                        this.socket.access();
                        if (this.socket.async) {
                            AprEndpoint.this.waitingRequests.add(this.socket);
                        }
                    }
                }
            }
        }
    }
    
    protected class SocketProcessor implements Runnable
    {
        private final SocketWrapper<Long> socket;
        private final SocketStatus status;
        
        public SocketProcessor(final SocketWrapper<Long> socket, final SocketStatus status) {
            this.socket = socket;
            if (status == null) {
                throw new NullPointerException();
            }
            this.status = status;
        }
        
        @Override
        public void run() {
            if (this.socket.isUpgraded() && SocketStatus.OPEN_WRITE == this.status) {
                synchronized (this.socket.getWriteThreadLock()) {
                    this.doRun();
                }
            }
            else {
                synchronized (this.socket) {
                    this.doRun();
                }
            }
        }
        
        private void doRun() {
            if (this.socket.getSocket() == null) {
                return;
            }
            final AbstractEndpoint.Handler.SocketState state = AprEndpoint.this.handler.process(this.socket, this.status);
            if (state == AbstractEndpoint.Handler.SocketState.CLOSED) {
                AprEndpoint.this.closeSocket(this.socket.getSocket());
                this.socket.socket = null;
            }
            else if (state == AbstractEndpoint.Handler.SocketState.LONG) {
                this.socket.access();
                if (this.socket.async) {
                    AprEndpoint.this.waitingRequests.add(this.socket);
                }
            }
            else if (state == AbstractEndpoint.Handler.SocketState.ASYNC_END) {
                this.socket.access();
                final SocketProcessor proc = new SocketProcessor(this.socket, SocketStatus.OPEN_READ);
                AprEndpoint.this.getExecutor().execute(proc);
            }
        }
    }
    
    private static class AprSocketWrapper extends SocketWrapper<Long>
    {
        private int pollerFlags;
        
        public AprSocketWrapper(final Long socket) {
            super(socket);
            this.pollerFlags = 0;
        }
    }
    
    private static class PrivilegedSetTccl implements PrivilegedAction<Void>
    {
        private ClassLoader cl;
        
        PrivilegedSetTccl(final ClassLoader cl) {
            this.cl = cl;
        }
        
        @Override
        public Void run() {
            Thread.currentThread().setContextClassLoader(this.cl);
            return null;
        }
    }
    
    public interface Handler extends AbstractEndpoint.Handler
    {
        SocketState process(final SocketWrapper<Long> p0, final SocketStatus p1);
    }
}
