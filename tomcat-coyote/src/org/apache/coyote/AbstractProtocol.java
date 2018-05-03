// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.coyote.http11.upgrade.UpgradeInbound;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.IOException;
import java.net.SocketException;
import org.apache.coyote.http11.upgrade.UpgradeProcessor;
import org.apache.coyote.http11.upgrade.servlet31.WebConnection;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.tomcat.util.net.SocketWrapper;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.util.modeler.Registry;
import javax.management.MalformedObjectNameException;
import org.apache.juli.logging.Log;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import javax.management.MBeanServer;
import org.apache.tomcat.util.net.AbstractEndpoint;
import javax.management.ObjectName;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.res.StringManager;
import javax.management.MBeanRegistration;

public abstract class AbstractProtocol implements ProtocolHandler, MBeanRegistration
{
    protected static final StringManager sm;
    private static final AtomicInteger nameCounter;
    protected ObjectName rgOname;
    protected ObjectName tpOname;
    private int nameIndex;
    protected AbstractEndpoint endpoint;
    protected Adapter adapter;
    protected int processorCache;
    protected String clientCertProvider;
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;
    
    public AbstractProtocol() {
        this.rgOname = null;
        this.tpOname = null;
        this.nameIndex = 0;
        this.endpoint = null;
        this.processorCache = 200;
        this.clientCertProvider = null;
    }
    
    public boolean setProperty(final String name, final String value) {
        return this.endpoint.setProperty(name, value);
    }
    
    public String getProperty(final String name) {
        return this.endpoint.getProperty(name);
    }
    
    @Override
    public void setAdapter(final Adapter adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public Adapter getAdapter() {
        return this.adapter;
    }
    
    public int getProcessorCache() {
        return this.processorCache;
    }
    
    public void setProcessorCache(final int processorCache) {
        this.processorCache = processorCache;
    }
    
    public String getClientCertProvider() {
        return this.clientCertProvider;
    }
    
    public void setClientCertProvider(final String s) {
        this.clientCertProvider = s;
    }
    
    @Override
    public boolean isAprRequired() {
        return false;
    }
    
    @Override
    public Executor getExecutor() {
        return this.endpoint.getExecutor();
    }
    
    public void setExecutor(final Executor executor) {
        this.endpoint.setExecutor(executor);
    }
    
    public int getMaxThreads() {
        return this.endpoint.getMaxThreads();
    }
    
    public void setMaxThreads(final int maxThreads) {
        this.endpoint.setMaxThreads(maxThreads);
    }
    
    public int getMaxConnections() {
        return this.endpoint.getMaxConnections();
    }
    
    public void setMaxConnections(final int maxConnections) {
        this.endpoint.setMaxConnections(maxConnections);
    }
    
    public int getMinSpareThreads() {
        return this.endpoint.getMinSpareThreads();
    }
    
    public void setMinSpareThreads(final int minSpareThreads) {
        this.endpoint.setMinSpareThreads(minSpareThreads);
    }
    
    public int getThreadPriority() {
        return this.endpoint.getThreadPriority();
    }
    
    public void setThreadPriority(final int threadPriority) {
        this.endpoint.setThreadPriority(threadPriority);
    }
    
    public int getBacklog() {
        return this.endpoint.getBacklog();
    }
    
    public void setBacklog(final int backlog) {
        this.endpoint.setBacklog(backlog);
    }
    
    public boolean getTcpNoDelay() {
        return this.endpoint.getTcpNoDelay();
    }
    
    public void setTcpNoDelay(final boolean tcpNoDelay) {
        this.endpoint.setTcpNoDelay(tcpNoDelay);
    }
    
    public int getSoLinger() {
        return this.endpoint.getSoLinger();
    }
    
    public void setSoLinger(final int soLinger) {
        this.endpoint.setSoLinger(soLinger);
    }
    
    public int getKeepAliveTimeout() {
        return this.endpoint.getKeepAliveTimeout();
    }
    
    public void setKeepAliveTimeout(final int keepAliveTimeout) {
        this.endpoint.setKeepAliveTimeout(keepAliveTimeout);
    }
    
    public InetAddress getAddress() {
        return this.endpoint.getAddress();
    }
    
    public void setAddress(final InetAddress ia) {
        this.endpoint.setAddress(ia);
    }
    
    public int getPort() {
        return this.endpoint.getPort();
    }
    
    public void setPort(final int port) {
        this.endpoint.setPort(port);
    }
    
    public int getLocalPort() {
        return this.endpoint.getLocalPort();
    }
    
    public int getConnectionTimeout() {
        return this.endpoint.getSoTimeout();
    }
    
    public void setConnectionTimeout(final int timeout) {
        this.endpoint.setSoTimeout(timeout);
    }
    
    public int getSoTimeout() {
        return this.getConnectionTimeout();
    }
    
    public void setSoTimeout(final int timeout) {
        this.setConnectionTimeout(timeout);
    }
    
    public int getMaxHeaderCount() {
        return this.endpoint.getMaxHeaderCount();
    }
    
    public void setMaxHeaderCount(final int maxHeaderCount) {
        this.endpoint.setMaxHeaderCount(maxHeaderCount);
    }
    
    public long getConnectionCount() {
        return this.endpoint.getConnectionCount();
    }
    
    public synchronized int getNameIndex() {
        if (this.nameIndex == 0) {
            this.nameIndex = AbstractProtocol.nameCounter.incrementAndGet();
        }
        return this.nameIndex;
    }
    
    public String getName() {
        final StringBuilder name = new StringBuilder(this.getNamePrefix());
        name.append('-');
        if (this.getAddress() != null) {
            name.append(this.getAddress().getHostAddress());
            name.append('-');
        }
        int port = this.getPort();
        if (port == 0) {
            name.append("auto-");
            name.append(this.getNameIndex());
            port = this.getLocalPort();
            if (port != -1) {
                name.append('-');
                name.append(port);
            }
        }
        else {
            name.append(port);
        }
        return ObjectName.quote(name.toString());
    }
    
    protected abstract Log getLog();
    
    protected abstract String getNamePrefix();
    
    protected abstract String getProtocolName();
    
    protected abstract AbstractEndpoint.Handler getHandler();
    
    public ObjectName getObjectName() {
        return this.oname;
    }
    
    public String getDomain() {
        return this.domain;
    }
    
    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception {
        this.oname = name;
        this.mserver = server;
        this.domain = name.getDomain();
        return name;
    }
    
    @Override
    public void postRegister(final Boolean registrationDone) {
    }
    
    @Override
    public void preDeregister() throws Exception {
    }
    
    @Override
    public void postDeregister() {
    }
    
    private ObjectName createObjectName() throws MalformedObjectNameException {
        this.domain = this.adapter.getDomain();
        if (this.domain == null) {
            return null;
        }
        final StringBuilder name = new StringBuilder(this.getDomain());
        name.append(":type=ProtocolHandler,port=");
        final int port = this.getPort();
        if (port > 0) {
            name.append(this.getPort());
        }
        else {
            name.append("auto-");
            name.append(this.getNameIndex());
        }
        final InetAddress address = this.getAddress();
        if (address != null) {
            name.append(",address=");
            name.append(ObjectName.quote(address.getHostAddress()));
        }
        return new ObjectName(name.toString());
    }
    
    @Override
    public void init() throws Exception {
        if (this.getLog().isInfoEnabled()) {
            this.getLog().info((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.init", new Object[] { this.getName() }));
        }
        if (this.oname == null) {
            this.oname = this.createObjectName();
            if (this.oname != null) {
                Registry.getRegistry(null, null).registerComponent(this, this.oname, null);
            }
        }
        if (this.domain != null) {
            try {
                this.tpOname = new ObjectName(this.domain + ":" + "type=ThreadPool,name=" + this.getName());
                Registry.getRegistry(null, null).registerComponent(this.endpoint, this.tpOname, null);
            }
            catch (Exception e) {
                this.getLog().error((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.mbeanRegistrationFailed", new Object[] { this.tpOname, this.getName() }), (Throwable)e);
            }
            this.rgOname = new ObjectName(this.domain + ":type=GlobalRequestProcessor,name=" + this.getName());
            Registry.getRegistry(null, null).registerComponent(this.getHandler().getGlobal(), this.rgOname, null);
        }
        final String endpointName = this.getName();
        this.endpoint.setName(endpointName.substring(1, endpointName.length() - 1));
        try {
            this.endpoint.init();
        }
        catch (Exception ex) {
            this.getLog().error((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.initError", new Object[] { this.getName() }), (Throwable)ex);
            throw ex;
        }
    }
    
    @Override
    public void start() throws Exception {
        if (this.getLog().isInfoEnabled()) {
            this.getLog().info((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.start", new Object[] { this.getName() }));
        }
        try {
            this.endpoint.start();
        }
        catch (Exception ex) {
            this.getLog().error((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.startError", new Object[] { this.getName() }), (Throwable)ex);
            throw ex;
        }
    }
    
    @Override
    public void pause() throws Exception {
        if (this.getLog().isInfoEnabled()) {
            this.getLog().info((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.pause", new Object[] { this.getName() }));
        }
        try {
            this.endpoint.pause();
        }
        catch (Exception ex) {
            this.getLog().error((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.pauseError", new Object[] { this.getName() }), (Throwable)ex);
            throw ex;
        }
    }
    
    @Override
    public void resume() throws Exception {
        if (this.getLog().isInfoEnabled()) {
            this.getLog().info((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.resume", new Object[] { this.getName() }));
        }
        try {
            this.endpoint.resume();
        }
        catch (Exception ex) {
            this.getLog().error((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.resumeError", new Object[] { this.getName() }), (Throwable)ex);
            throw ex;
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (this.getLog().isInfoEnabled()) {
            this.getLog().info((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.stop", new Object[] { this.getName() }));
        }
        try {
            this.endpoint.stop();
        }
        catch (Exception ex) {
            this.getLog().error((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.stopError", new Object[] { this.getName() }), (Throwable)ex);
            throw ex;
        }
    }
    
    @Override
    public void destroy() {
        if (this.getLog().isInfoEnabled()) {
            this.getLog().info((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.destroy", new Object[] { this.getName() }));
        }
        try {
            this.endpoint.destroy();
        }
        catch (Exception e) {
            this.getLog().error((Object)AbstractProtocol.sm.getString("abstractProtocolHandler.destroyError", new Object[] { this.getName() }), (Throwable)e);
        }
        if (this.oname != null) {
            Registry.getRegistry(null, null).unregisterComponent(this.oname);
        }
        if (this.tpOname != null) {
            Registry.getRegistry(null, null).unregisterComponent(this.tpOname);
        }
        if (this.rgOname != null) {
            Registry.getRegistry(null, null).unregisterComponent(this.rgOname);
        }
    }
    
    static {
        sm = StringManager.getManager("org.apache.coyote");
        nameCounter = new AtomicInteger(0);
    }
    
    protected abstract static class AbstractConnectionHandler<S, P extends Processor<S>> implements AbstractEndpoint.Handler
    {
        protected RequestGroupInfo global;
        protected AtomicLong registerCount;
        protected ConcurrentHashMap<S, Processor<S>> connections;
        protected RecycledProcessors<P, S> recycledProcessors;
        
        protected AbstractConnectionHandler() {
            this.global = new RequestGroupInfo();
            this.registerCount = new AtomicLong(0L);
            this.connections = new ConcurrentHashMap<S, Processor<S>>();
            this.recycledProcessors = new RecycledProcessors<P, S>(this);
        }
        
        protected abstract Log getLog();
        
        protected abstract AbstractProtocol getProtocol();
        
        @Override
        public Object getGlobal() {
            return this.global;
        }
        
        @Override
        public void recycle() {
            this.recycledProcessors.clear();
        }
        
        public SocketState process(final SocketWrapper<S> wrapper, final SocketStatus status) {
            final S socket = wrapper.getSocket();
            if (socket == null) {
                return SocketState.CLOSED;
            }
            Processor<S> processor = this.connections.get(socket);
            if (status == SocketStatus.DISCONNECT && processor == null) {
                return SocketState.CLOSED;
            }
            wrapper.setAsync(false);
            try {
                if (processor == null) {
                    processor = this.recycledProcessors.poll();
                }
                if (processor == null) {
                    processor = this.createProcessor();
                }
                this.initSsl(wrapper, processor);
                SocketState state = SocketState.CLOSED;
                do {
                    if (status != SocketStatus.DISCONNECT || processor.isComet()) {
                        if (processor.isAsync() || state == SocketState.ASYNC_END) {
                            state = processor.asyncDispatch(status);
                        }
                        else if (processor.isComet()) {
                            state = processor.event(status);
                        }
                        else if (processor.getUpgradeInbound() != null) {
                            state = processor.upgradeDispatch();
                        }
                        else if (processor.isUpgrade()) {
                            state = processor.upgradeDispatch(status);
                        }
                        else {
                            state = processor.process(wrapper);
                        }
                    }
                    if (state != SocketState.CLOSED && processor.isAsync()) {
                        state = processor.asyncPostProcess();
                    }
                    if (state == SocketState.UPGRADING) {
                        final HttpUpgradeHandler httpUpgradeHandler = processor.getHttpUpgradeHandler();
                        this.release(wrapper, processor, false, false);
                        processor = this.createUpgradeProcessor(wrapper, httpUpgradeHandler);
                        wrapper.setUpgraded(true);
                        this.connections.put(socket, processor);
                        httpUpgradeHandler.init((WebConnection)processor);
                    }
                    else if (state == SocketState.UPGRADING_TOMCAT) {
                        final UpgradeInbound inbound = processor.getUpgradeInbound();
                        this.release(wrapper, processor, false, false);
                        processor = this.createUpgradeProcessor(wrapper, inbound);
                        inbound.onUpgradeComplete();
                    }
                    if (this.getLog().isDebugEnabled()) {
                        this.getLog().debug((Object)("Socket: [" + wrapper + "], Status in: [" + status + "], State out: [" + state + "]"));
                    }
                } while (state == SocketState.ASYNC_END || state == SocketState.UPGRADING || state == SocketState.UPGRADING_TOMCAT);
                if (state == SocketState.LONG) {
                    this.connections.put(socket, processor);
                    this.longPoll(wrapper, processor);
                }
                else if (state == SocketState.OPEN) {
                    this.connections.remove(socket);
                    this.release(wrapper, processor, false, true);
                }
                else if (state == SocketState.SENDFILE) {
                    this.connections.remove(socket);
                    this.release(wrapper, processor, false, false);
                }
                else if (state == SocketState.UPGRADED) {
                    this.connections.put(socket, processor);
                    if (status != SocketStatus.OPEN_WRITE) {
                        this.longPoll(wrapper, processor);
                    }
                }
                else {
                    this.connections.remove(socket);
                    if (processor.isUpgrade()) {
                        processor.getHttpUpgradeHandler().destroy();
                    }
                    else if (!(processor instanceof UpgradeProcessor)) {
                        this.release(wrapper, processor, true, false);
                    }
                }
                return state;
            }
            catch (SocketException e) {
                this.getLog().debug((Object)AbstractProtocol.sm.getString("abstractConnectionHandler.socketexception.debug"), (Throwable)e);
            }
            catch (IOException e2) {
                this.getLog().debug((Object)AbstractProtocol.sm.getString("abstractConnectionHandler.ioexception.debug"), (Throwable)e2);
            }
            catch (Throwable e3) {
                ExceptionUtils.handleThrowable(e3);
                this.getLog().error((Object)AbstractProtocol.sm.getString("abstractConnectionHandler.error"), e3);
            }
            this.connections.remove(socket);
            if (!(processor instanceof UpgradeProcessor) && !processor.isUpgrade()) {
                this.release(wrapper, processor, true, false);
            }
            return SocketState.CLOSED;
        }
        
        protected abstract P createProcessor();
        
        protected abstract void initSsl(final SocketWrapper<S> p0, final Processor<S> p1);
        
        protected abstract void longPoll(final SocketWrapper<S> p0, final Processor<S> p1);
        
        protected abstract void release(final SocketWrapper<S> p0, final Processor<S> p1, final boolean p2, final boolean p3);
        
        @Deprecated
        protected abstract Processor<S> createUpgradeProcessor(final SocketWrapper<S> p0, final UpgradeInbound p1) throws IOException;
        
        protected abstract Processor<S> createUpgradeProcessor(final SocketWrapper<S> p0, final HttpUpgradeHandler p1) throws IOException;
        
        protected void register(final AbstractProcessor<S> processor) {
            if (this.getProtocol().getDomain() != null) {
                synchronized (this) {
                    try {
                        final long count = this.registerCount.incrementAndGet();
                        final RequestInfo rp = processor.getRequest().getRequestProcessor();
                        rp.setGlobalProcessor(this.global);
                        final ObjectName rpName = new ObjectName(this.getProtocol().getDomain() + ":type=RequestProcessor,worker=" + this.getProtocol().getName() + ",name=" + this.getProtocol().getProtocolName() + "Request" + count);
                        if (this.getLog().isDebugEnabled()) {
                            this.getLog().debug((Object)("Register " + rpName));
                        }
                        Registry.getRegistry(null, null).registerComponent(rp, rpName, null);
                        rp.setRpName(rpName);
                    }
                    catch (Exception e) {
                        this.getLog().warn((Object)"Error registering request");
                    }
                }
            }
        }
        
        protected void unregister(final Processor<S> processor) {
            if (this.getProtocol().getDomain() != null) {
                synchronized (this) {
                    try {
                        final Request r = processor.getRequest();
                        if (r == null) {
                            return;
                        }
                        final RequestInfo rp = r.getRequestProcessor();
                        rp.setGlobalProcessor(null);
                        final ObjectName rpName = rp.getRpName();
                        if (this.getLog().isDebugEnabled()) {
                            this.getLog().debug((Object)("Unregister " + rpName));
                        }
                        Registry.getRegistry(null, null).unregisterComponent(rpName);
                        rp.setRpName(null);
                    }
                    catch (Exception e) {
                        this.getLog().warn((Object)"Error unregistering request", (Throwable)e);
                    }
                }
            }
        }
    }
    
    protected static class RecycledProcessors<P extends Processor<S>, S> extends ConcurrentLinkedQueue<Processor<S>>
    {
        private static final long serialVersionUID = 1L;
        private transient AbstractConnectionHandler<S, P> handler;
        protected AtomicInteger size;
        
        public RecycledProcessors(final AbstractConnectionHandler<S, P> handler) {
            this.size = new AtomicInteger(0);
            this.handler = handler;
        }
        
        @Override
        public boolean offer(final Processor<S> processor) {
            final int cacheSize = this.handler.getProtocol().getProcessorCache();
            final boolean offer = cacheSize == -1 || this.size.get() < cacheSize;
            boolean result = false;
            if (offer) {
                result = super.offer(processor);
                if (result) {
                    this.size.incrementAndGet();
                }
            }
            if (!result) {
                this.handler.unregister(processor);
            }
            return result;
        }
        
        @Override
        public Processor<S> poll() {
            final Processor<S> result = super.poll();
            if (result != null) {
                this.size.decrementAndGet();
            }
            return result;
        }
        
        @Override
        public void clear() {
            for (Processor<S> next = this.poll(); next != null; next = this.poll()) {
                this.handler.unregister(next);
            }
            super.clear();
            this.size.set(0);
        }
    }
}
