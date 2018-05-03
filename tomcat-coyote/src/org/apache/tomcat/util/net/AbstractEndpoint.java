// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.juli.logging.Log;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.SocketAddress;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.tomcat.util.threads.TaskThreadFactory;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.threads.ResizableExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.net.ssl.KeyManagerFactory;
import java.util.HashMap;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import org.apache.tomcat.util.threads.LimitLatch;
import org.apache.tomcat.util.res.StringManager;

public abstract class AbstractEndpoint
{
    protected static final StringManager sm;
    private static final int INITIAL_ERROR_DELAY = 50;
    private static final int MAX_ERROR_DELAY = 1600;
    protected volatile boolean running;
    protected volatile boolean paused;
    protected volatile boolean internalExecutor;
    private volatile LimitLatch connectionLimitLatch;
    protected SocketProperties socketProperties;
    protected Acceptor[] acceptors;
    protected int acceptorThreadCount;
    protected int acceptorThreadPriority;
    private int maxConnections;
    private Executor executor;
    private int port;
    private InetAddress address;
    private int backlog;
    private boolean bindOnInit;
    private BindState bindState;
    private Integer keepAliveTimeout;
    private boolean SSLEnabled;
    private int minSpareThreads;
    private int maxThreads;
    private int maxKeepAliveRequests;
    private int maxHeaderCount;
    private String name;
    private boolean daemon;
    protected int threadPriority;
    protected HashMap<String, Object> attributes;
    private String algorithm;
    private String clientAuth;
    private String keystoreFile;
    private String keystorePass;
    private String keystoreType;
    private String keystoreProvider;
    private String sslProtocol;
    private String ciphers;
    private String[] ciphersarr;
    private String keyAlias;
    private String keyPass;
    private String truststoreFile;
    private String truststorePass;
    private String truststoreType;
    private String truststoreProvider;
    private String truststoreAlgorithm;
    private String trustManagerClassName;
    private String crlFile;
    private String trustMaxCertLength;
    private String sessionCacheSize;
    private String sessionTimeout;
    private String allowUnsafeLegacyRenegotiation;
    private String[] sslEnabledProtocolsarr;
    
    public AbstractEndpoint() {
        this.running = false;
        this.paused = false;
        this.internalExecutor = false;
        this.connectionLimitLatch = null;
        this.socketProperties = new SocketProperties();
        this.acceptorThreadCount = 0;
        this.acceptorThreadPriority = 5;
        this.maxConnections = 10000;
        this.executor = null;
        this.backlog = 100;
        this.bindOnInit = true;
        this.bindState = BindState.UNBOUND;
        this.keepAliveTimeout = null;
        this.SSLEnabled = false;
        this.minSpareThreads = 10;
        this.maxThreads = 200;
        this.maxKeepAliveRequests = 100;
        this.maxHeaderCount = 100;
        this.name = "TP";
        this.daemon = true;
        this.threadPriority = 5;
        this.attributes = new HashMap<String, Object>();
        this.algorithm = KeyManagerFactory.getDefaultAlgorithm();
        this.clientAuth = "false";
        this.keystoreFile = System.getProperty("user.home") + "/.keystore";
        this.keystorePass = null;
        this.keystoreType = "JKS";
        this.keystoreProvider = null;
        this.sslProtocol = "TLS";
        this.ciphers = null;
        this.ciphersarr = new String[0];
        this.keyAlias = null;
        this.keyPass = null;
        this.truststoreFile = System.getProperty("javax.net.ssl.trustStore");
        this.truststorePass = System.getProperty("javax.net.ssl.trustStorePassword");
        this.truststoreType = System.getProperty("javax.net.ssl.trustStoreType");
        this.truststoreProvider = null;
        this.truststoreAlgorithm = null;
        this.trustManagerClassName = null;
        this.crlFile = null;
        this.trustMaxCertLength = null;
        this.sessionCacheSize = null;
        this.sessionTimeout = "86400";
        this.allowUnsafeLegacyRenegotiation = null;
        this.sslEnabledProtocolsarr = new String[0];
    }
    
    public SocketProperties getSocketProperties() {
        return this.socketProperties;
    }
    
    public void setAcceptorThreadCount(final int acceptorThreadCount) {
        this.acceptorThreadCount = acceptorThreadCount;
    }
    
    public int getAcceptorThreadCount() {
        return this.acceptorThreadCount;
    }
    
    public void setAcceptorThreadPriority(final int acceptorThreadPriority) {
        this.acceptorThreadPriority = acceptorThreadPriority;
    }
    
    public int getAcceptorThreadPriority() {
        return this.acceptorThreadPriority;
    }
    
    public void setMaxConnections(final int maxCon) {
        this.maxConnections = maxCon;
        final LimitLatch latch = this.connectionLimitLatch;
        if (latch != null) {
            if (maxCon == -1) {
                this.releaseConnectionLatch();
            }
            else {
                latch.setLimit(maxCon);
            }
        }
        else if (maxCon > 0) {
            this.initializeConnectionLatch();
        }
    }
    
    public int getMaxConnections() {
        return this.maxConnections;
    }
    
    public long getConnectionCount() {
        final LimitLatch latch = this.connectionLimitLatch;
        if (latch != null) {
            return latch.getCount();
        }
        return -1L;
    }
    
    public void setExecutor(final Executor executor) {
        this.executor = executor;
        this.internalExecutor = (executor == null);
    }
    
    public Executor getExecutor() {
        return this.executor;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public void setPort(final int port) {
        this.port = port;
    }
    
    public abstract int getLocalPort();
    
    public InetAddress getAddress() {
        return this.address;
    }
    
    public void setAddress(final InetAddress address) {
        this.address = address;
    }
    
    public void setBacklog(final int backlog) {
        if (backlog > 0) {
            this.backlog = backlog;
        }
    }
    
    public int getBacklog() {
        return this.backlog;
    }
    
    public boolean getBindOnInit() {
        return this.bindOnInit;
    }
    
    public void setBindOnInit(final boolean b) {
        this.bindOnInit = b;
    }
    
    public int getKeepAliveTimeout() {
        if (this.keepAliveTimeout == null) {
            return this.getSoTimeout();
        }
        return this.keepAliveTimeout;
    }
    
    public void setKeepAliveTimeout(final int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }
    
    public boolean getTcpNoDelay() {
        return this.socketProperties.getTcpNoDelay();
    }
    
    public void setTcpNoDelay(final boolean tcpNoDelay) {
        this.socketProperties.setTcpNoDelay(tcpNoDelay);
    }
    
    public int getSoLinger() {
        return this.socketProperties.getSoLingerTime();
    }
    
    public void setSoLinger(final int soLinger) {
        this.socketProperties.setSoLingerTime(soLinger);
        this.socketProperties.setSoLingerOn(soLinger >= 0);
    }
    
    public int getSoTimeout() {
        return this.socketProperties.getSoTimeout();
    }
    
    public void setSoTimeout(final int soTimeout) {
        this.socketProperties.setSoTimeout(soTimeout);
    }
    
    public boolean isSSLEnabled() {
        return this.SSLEnabled;
    }
    
    public void setSSLEnabled(final boolean SSLEnabled) {
        this.SSLEnabled = SSLEnabled;
    }
    
    public int getMinSpareThreads() {
        return Math.min(this.minSpareThreads, this.getMaxThreads());
    }
    
    public void setMinSpareThreads(final int minSpareThreads) {
        this.minSpareThreads = minSpareThreads;
        if (this.running && this.executor != null) {
            if (this.executor instanceof ThreadPoolExecutor) {
                ((ThreadPoolExecutor)this.executor).setCorePoolSize(minSpareThreads);
            }
            else if (this.executor instanceof ResizableExecutor) {
                ((ResizableExecutor)this.executor).resizePool(minSpareThreads, this.maxThreads);
            }
        }
    }
    
    public void setMaxThreads(final int maxThreads) {
        this.maxThreads = maxThreads;
        if (this.running && this.executor != null) {
            if (this.executor instanceof ThreadPoolExecutor) {
                ((ThreadPoolExecutor)this.executor).setMaximumPoolSize(maxThreads);
            }
            else if (this.executor instanceof ResizableExecutor) {
                ((ResizableExecutor)this.executor).resizePool(this.minSpareThreads, maxThreads);
            }
        }
    }
    
    public int getMaxThreads() {
        return this.getMaxThreadsExecutor(this.running);
    }
    
    protected int getMaxThreadsExecutor(final boolean useExecutor) {
        if (!useExecutor || this.executor == null) {
            return this.maxThreads;
        }
        if (this.executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor)this.executor).getMaximumPoolSize();
        }
        if (this.executor instanceof ResizableExecutor) {
            return ((ResizableExecutor)this.executor).getMaxThreads();
        }
        return -1;
    }
    
    public int getMaxKeepAliveRequests() {
        return this.maxKeepAliveRequests;
    }
    
    public void setMaxKeepAliveRequests(final int maxKeepAliveRequests) {
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }
    
    public int getMaxHeaderCount() {
        return this.maxHeaderCount;
    }
    
    public void setMaxHeaderCount(final int maxHeaderCount) {
        this.maxHeaderCount = maxHeaderCount;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setDaemon(final boolean b) {
        this.daemon = b;
    }
    
    public boolean getDaemon() {
        return this.daemon;
    }
    
    public void setThreadPriority(final int threadPriority) {
        this.threadPriority = threadPriority;
    }
    
    public int getThreadPriority() {
        return this.threadPriority;
    }
    
    protected abstract boolean getDeferAccept();
    
    public void setAttribute(final String name, final Object value) {
        if (this.getLog().isTraceEnabled()) {
            this.getLog().trace((Object)AbstractEndpoint.sm.getString("abstractProtocolHandler.setAttribute", new Object[] { name, value }));
        }
        this.attributes.put(name, value);
    }
    
    public Object getAttribute(final String key) {
        final Object value = this.attributes.get(key);
        if (this.getLog().isTraceEnabled()) {
            this.getLog().trace((Object)AbstractEndpoint.sm.getString("abstractProtocolHandler.getAttribute", new Object[] { key, value }));
        }
        return value;
    }
    
    public boolean setProperty(final String name, final String value) {
        this.setAttribute(name, value);
        final String socketName = "socket.";
        try {
            if (name.startsWith("socket.")) {
                return IntrospectionUtils.setProperty(this.socketProperties, name.substring("socket.".length()), value);
            }
            return IntrospectionUtils.setProperty(this, name, value, false);
        }
        catch (Exception x) {
            this.getLog().error((Object)("Unable to set attribute \"" + name + "\" to \"" + value + "\""), (Throwable)x);
            return false;
        }
    }
    
    public String getProperty(final String name) {
        return (String)this.getAttribute(name);
    }
    
    public int getCurrentThreadCount() {
        if (this.executor == null) {
            return -2;
        }
        if (this.executor instanceof org.apache.tomcat.util.threads.ThreadPoolExecutor) {
            return ((org.apache.tomcat.util.threads.ThreadPoolExecutor)this.executor).getPoolSize();
        }
        if (this.executor instanceof ResizableExecutor) {
            return ((ResizableExecutor)this.executor).getPoolSize();
        }
        return -1;
    }
    
    public int getCurrentThreadsBusy() {
        if (this.executor == null) {
            return -2;
        }
        if (this.executor instanceof org.apache.tomcat.util.threads.ThreadPoolExecutor) {
            return ((org.apache.tomcat.util.threads.ThreadPoolExecutor)this.executor).getActiveCount();
        }
        if (this.executor instanceof ResizableExecutor) {
            return ((ResizableExecutor)this.executor).getActiveCount();
        }
        return -1;
    }
    
    public boolean isRunning() {
        return this.running;
    }
    
    public boolean isPaused() {
        return this.paused;
    }
    
    public void createExecutor() {
        this.internalExecutor = true;
        final TaskQueue taskqueue = new TaskQueue();
        final TaskThreadFactory tf = new TaskThreadFactory(this.getName() + "-exec-", this.daemon, this.getThreadPriority());
        this.executor = new org.apache.tomcat.util.threads.ThreadPoolExecutor(this.getMinSpareThreads(), this.getMaxThreads(), 60L, TimeUnit.SECONDS, taskqueue, tf);
        taskqueue.setParent((org.apache.tomcat.util.threads.ThreadPoolExecutor)this.executor);
    }
    
    public void shutdownExecutor() {
        if (this.executor != null && this.internalExecutor) {
            if (this.executor instanceof org.apache.tomcat.util.threads.ThreadPoolExecutor) {
                final org.apache.tomcat.util.threads.ThreadPoolExecutor tpe = (org.apache.tomcat.util.threads.ThreadPoolExecutor)this.executor;
                tpe.shutdownNow();
                int count = 0;
                while (count < 50 && tpe.isTerminating()) {
                    try {
                        Thread.sleep(100L);
                        ++count;
                    }
                    catch (InterruptedException e) {}
                }
                if (tpe.isTerminating()) {
                    this.getLog().warn((Object)AbstractEndpoint.sm.getString("endpoint.warn.executorShutdown", new Object[] { this.getName() }));
                }
                final TaskQueue queue = (TaskQueue)tpe.getQueue();
                queue.setParent(null);
            }
            this.executor = null;
        }
    }
    
    protected void unlockAccept() {
        boolean unlockRequired = false;
        for (final Acceptor acceptor : this.acceptors) {
            if (acceptor.getState() == Acceptor.AcceptorState.RUNNING) {
                unlockRequired = true;
                break;
            }
        }
        if (!unlockRequired) {
            return;
        }
        Socket s = null;
        InetSocketAddress saddr = null;
        try {
            if (this.address == null) {
                saddr = new InetSocketAddress("localhost", this.getLocalPort());
            }
            else {
                saddr = new InetSocketAddress(this.address, this.getLocalPort());
            }
            s = new Socket();
            int stmo = 2000;
            int utmo = 2000;
            if (this.getSocketProperties().getSoTimeout() > stmo) {
                stmo = this.getSocketProperties().getSoTimeout();
            }
            if (this.getSocketProperties().getUnlockTimeout() > utmo) {
                utmo = this.getSocketProperties().getUnlockTimeout();
            }
            s.setSoTimeout(stmo);
            s.setSoLinger(this.getSocketProperties().getSoLingerOn(), this.getSocketProperties().getSoLingerTime());
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug((Object)("About to unlock socket for:" + saddr));
            }
            s.connect(saddr, utmo);
            if (this.getDeferAccept()) {
                final OutputStreamWriter sw = new OutputStreamWriter(s.getOutputStream(), "ISO-8859-1");
                sw.write("OPTIONS * HTTP/1.0\r\nUser-Agent: Tomcat wakeup connection\r\n\r\n");
                sw.flush();
            }
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug((Object)("Socket unlock completed for:" + saddr));
            }
            long waitLeft = 1000L;
            for (final Acceptor acceptor2 : this.acceptors) {
                while (waitLeft > 0L && acceptor2.getState() == Acceptor.AcceptorState.RUNNING) {
                    Thread.sleep(50L);
                    waitLeft -= 50L;
                }
            }
        }
        catch (Exception e) {
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug((Object)AbstractEndpoint.sm.getString("endpoint.debug.unlock", new Object[] { "" + this.getPort() }), (Throwable)e);
            }
        }
        finally {
            if (s != null) {
                try {
                    s.close();
                }
                catch (Exception ex) {}
            }
        }
    }
    
    public abstract void bind() throws Exception;
    
    public abstract void unbind() throws Exception;
    
    public abstract void startInternal() throws Exception;
    
    public abstract void stopInternal() throws Exception;
    
    public final void init() throws Exception {
        if (this.bindOnInit) {
            this.bind();
            this.bindState = BindState.BOUND_ON_INIT;
        }
    }
    
    public final void start() throws Exception {
        if (this.bindState == BindState.UNBOUND) {
            this.bind();
            this.bindState = BindState.BOUND_ON_START;
        }
        this.startInternal();
    }
    
    protected final void startAcceptorThreads() {
        final int count = this.getAcceptorThreadCount();
        this.acceptors = new Acceptor[count];
        for (int i = 0; i < count; ++i) {
            this.acceptors[i] = this.createAcceptor();
            final String threadName = this.getName() + "-Acceptor-" + i;
            this.acceptors[i].setThreadName(threadName);
            final Thread t = new Thread(this.acceptors[i], threadName);
            t.setPriority(this.getAcceptorThreadPriority());
            t.setDaemon(this.getDaemon());
            t.start();
        }
    }
    
    protected abstract Acceptor createAcceptor();
    
    public void pause() {
        if (this.running && !this.paused) {
            this.paused = true;
            this.unlockAccept();
        }
    }
    
    public void resume() {
        if (this.running) {
            this.paused = false;
        }
    }
    
    public final void stop() throws Exception {
        this.stopInternal();
        if (this.bindState == BindState.BOUND_ON_START) {
            this.unbind();
            this.bindState = BindState.UNBOUND;
        }
    }
    
    public final void destroy() throws Exception {
        if (this.bindState == BindState.BOUND_ON_INIT) {
            this.unbind();
            this.bindState = BindState.UNBOUND;
        }
    }
    
    public String adjustRelativePath(final String path, final String relativeTo) {
        String newPath = path;
        File f = new File(newPath);
        if (!f.isAbsolute()) {
            newPath = relativeTo + File.separator + newPath;
            f = new File(newPath);
        }
        if (!f.exists()) {
            this.getLog().warn((Object)("configured file:[" + newPath + "] does not exist."));
        }
        return newPath;
    }
    
    protected abstract Log getLog();
    
    public abstract boolean getUseSendfile();
    
    public abstract boolean getUseComet();
    
    public abstract boolean getUseCometTimeout();
    
    public abstract boolean getUsePolling();
    
    protected LimitLatch initializeConnectionLatch() {
        if (this.maxConnections == -1) {
            return null;
        }
        if (this.connectionLimitLatch == null) {
            this.connectionLimitLatch = new LimitLatch(this.getMaxConnections());
        }
        return this.connectionLimitLatch;
    }
    
    protected void releaseConnectionLatch() {
        final LimitLatch latch = this.connectionLimitLatch;
        if (latch != null) {
            latch.releaseAll();
        }
        this.connectionLimitLatch = null;
    }
    
    protected void countUpOrAwaitConnection() throws InterruptedException {
        if (this.maxConnections == -1) {
            return;
        }
        final LimitLatch latch = this.connectionLimitLatch;
        if (latch != null) {
            latch.countUpOrAwait();
        }
    }
    
    protected long countDownConnection() {
        if (this.maxConnections == -1) {
            return -1L;
        }
        final LimitLatch latch = this.connectionLimitLatch;
        if (latch != null) {
            final long result = latch.countDown();
            if (result < 0L) {
                this.getLog().warn((Object)"Incorrect connection count, multiple socket.close called on the same socket.");
            }
            return result;
        }
        return -1L;
    }
    
    protected int handleExceptionWithDelay(final int currentErrorDelay) {
        if (currentErrorDelay > 0) {
            try {
                Thread.sleep(currentErrorDelay);
            }
            catch (InterruptedException ex) {}
        }
        if (currentErrorDelay == 0) {
            return 50;
        }
        if (currentErrorDelay < 1600) {
            return currentErrorDelay * 2;
        }
        return 1600;
    }
    
    public String getAlgorithm() {
        return this.algorithm;
    }
    
    public void setAlgorithm(final String s) {
        this.algorithm = s;
    }
    
    public String getClientAuth() {
        return this.clientAuth;
    }
    
    public void setClientAuth(final String s) {
        this.clientAuth = s;
    }
    
    public String getKeystoreFile() {
        return this.keystoreFile;
    }
    
    public void setKeystoreFile(final String s) {
        final String file = this.adjustRelativePath(s, System.getProperty("catalina.base"));
        this.keystoreFile = file;
    }
    
    public String getKeystorePass() {
        return this.keystorePass;
    }
    
    public void setKeystorePass(final String s) {
        this.keystorePass = s;
    }
    
    public String getKeystoreType() {
        return this.keystoreType;
    }
    
    public void setKeystoreType(final String s) {
        this.keystoreType = s;
    }
    
    public String getKeystoreProvider() {
        return this.keystoreProvider;
    }
    
    public void setKeystoreProvider(final String s) {
        this.keystoreProvider = s;
    }
    
    public String getSslProtocol() {
        return this.sslProtocol;
    }
    
    public void setSslProtocol(final String s) {
        this.sslProtocol = s;
    }
    
    public String[] getCiphersArray() {
        return this.ciphersarr;
    }
    
    public String getCiphers() {
        return this.ciphers;
    }
    
    public void setCiphers(final String s) {
        this.ciphers = s;
        if (s == null) {
            this.ciphersarr = new String[0];
        }
        else {
            final StringTokenizer t = new StringTokenizer(s, ",");
            this.ciphersarr = new String[t.countTokens()];
            for (int i = 0; i < this.ciphersarr.length; ++i) {
                this.ciphersarr[i] = t.nextToken();
            }
        }
    }
    
    public String getKeyAlias() {
        return this.keyAlias;
    }
    
    public void setKeyAlias(final String s) {
        this.keyAlias = s;
    }
    
    public String getKeyPass() {
        return this.keyPass;
    }
    
    public void setKeyPass(final String s) {
        this.keyPass = s;
    }
    
    public String getTruststoreFile() {
        return this.truststoreFile;
    }
    
    public void setTruststoreFile(final String s) {
        if (s == null) {
            this.truststoreFile = null;
        }
        else {
            final String file = this.adjustRelativePath(s, System.getProperty("catalina.base"));
            this.truststoreFile = file;
        }
    }
    
    public String getTruststorePass() {
        return this.truststorePass;
    }
    
    public void setTruststorePass(final String truststorePass) {
        this.truststorePass = truststorePass;
    }
    
    public String getTruststoreType() {
        return this.truststoreType;
    }
    
    public void setTruststoreType(final String truststoreType) {
        this.truststoreType = truststoreType;
    }
    
    public String getTruststoreProvider() {
        return this.truststoreProvider;
    }
    
    public void setTruststoreProvider(final String truststoreProvider) {
        this.truststoreProvider = truststoreProvider;
    }
    
    public String getTruststoreAlgorithm() {
        return this.truststoreAlgorithm;
    }
    
    public void setTruststoreAlgorithm(final String truststoreAlgorithm) {
        this.truststoreAlgorithm = truststoreAlgorithm;
    }
    
    public String getTrustManagerClassName() {
        return this.trustManagerClassName;
    }
    
    public void setTrustManagerClassName(final String trustManagerClassName) {
        this.trustManagerClassName = trustManagerClassName;
    }
    
    public String getCrlFile() {
        return this.crlFile;
    }
    
    public void setCrlFile(final String crlFile) {
        this.crlFile = crlFile;
    }
    
    public String getTrustMaxCertLength() {
        return this.trustMaxCertLength;
    }
    
    public void setTrustMaxCertLength(final String trustMaxCertLength) {
        this.trustMaxCertLength = trustMaxCertLength;
    }
    
    public String getSessionCacheSize() {
        return this.sessionCacheSize;
    }
    
    public void setSessionCacheSize(final String s) {
        this.sessionCacheSize = s;
    }
    
    public String getSessionTimeout() {
        return this.sessionTimeout;
    }
    
    public void setSessionTimeout(final String s) {
        this.sessionTimeout = s;
    }
    
    public String getAllowUnsafeLegacyRenegotiation() {
        return this.allowUnsafeLegacyRenegotiation;
    }
    
    public void setAllowUnsafeLegacyRenegotiation(final String s) {
        this.allowUnsafeLegacyRenegotiation = s;
    }
    
    public String[] getSslEnabledProtocolsArray() {
        return this.sslEnabledProtocolsarr;
    }
    
    public void setSslEnabledProtocols(final String s) {
        if (s == null) {
            this.sslEnabledProtocolsarr = new String[0];
        }
        else {
            final ArrayList<String> sslEnabledProtocols = new ArrayList<String>();
            final StringTokenizer t = new StringTokenizer(s, ",");
            while (t.hasMoreTokens()) {
                final String p = t.nextToken().trim();
                if (p.length() > 0) {
                    sslEnabledProtocols.add(p);
                }
            }
            this.sslEnabledProtocolsarr = sslEnabledProtocols.toArray(new String[sslEnabledProtocols.size()]);
        }
    }
    
    static {
        sm = StringManager.getManager("org.apache.tomcat.util.net.res");
    }
    
    protected enum BindState
    {
        UNBOUND, 
        BOUND_ON_INIT, 
        BOUND_ON_START;
    }
    
    public abstract static class Acceptor implements Runnable
    {
        protected volatile AcceptorState state;
        private String threadName;
        
        public Acceptor() {
            this.state = AcceptorState.NEW;
        }
        
        public final AcceptorState getState() {
            return this.state;
        }
        
        protected final void setThreadName(final String threadName) {
            this.threadName = threadName;
        }
        
        protected final String getThreadName() {
            return this.threadName;
        }
        
        public enum AcceptorState
        {
            NEW, 
            RUNNING, 
            PAUSED, 
            ENDED;
        }
    }
    
    public interface Handler
    {
        Object getGlobal();
        
        void recycle();
        
        public enum SocketState
        {
            OPEN, 
            CLOSED, 
            LONG, 
            ASYNC_END, 
            SENDFILE, 
            UPGRADING_TOMCAT, 
            UPGRADING, 
            UPGRADED;
        }
    }
}
