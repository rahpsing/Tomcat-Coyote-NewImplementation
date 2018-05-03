// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.util.Iterator;
import org.apache.juli.logging.LogFactory;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.concurrent.RejectedExecutionException;
import org.apache.tomcat.util.ExceptionUtils;
import java.net.SocketException;
import java.net.BindException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.net.ServerSocket;
import org.apache.juli.logging.Log;

public class JIoEndpoint extends AbstractEndpoint
{
    private static final Log log;
    protected ServerSocket serverSocket;
    protected Handler handler;
    protected ServerSocketFactory serverSocketFactory;
    protected ConcurrentLinkedQueue<SocketWrapper<Socket>> waitingRequests;
    
    public JIoEndpoint() {
        this.serverSocket = null;
        this.handler = null;
        this.serverSocketFactory = null;
        this.waitingRequests = new ConcurrentLinkedQueue<SocketWrapper<Socket>>();
        this.setMaxConnections(0);
    }
    
    public void setHandler(final Handler handler) {
        this.handler = handler;
    }
    
    public Handler getHandler() {
        return this.handler;
    }
    
    public void setServerSocketFactory(final ServerSocketFactory factory) {
        this.serverSocketFactory = factory;
    }
    
    public ServerSocketFactory getServerSocketFactory() {
        return this.serverSocketFactory;
    }
    
    @Override
    public int getLocalPort() {
        final ServerSocket s = this.serverSocket;
        if (s == null) {
            return -1;
        }
        return s.getLocalPort();
    }
    
    @Override
    public boolean getUseSendfile() {
        return false;
    }
    
    @Override
    public boolean getUseComet() {
        return false;
    }
    
    @Override
    public boolean getUseCometTimeout() {
        return false;
    }
    
    public boolean getDeferAccept() {
        return false;
    }
    
    @Override
    public boolean getUsePolling() {
        return false;
    }
    
    private void closeSocket(final Socket socket) {
        try {
            socket.close();
        }
        catch (IOException ex) {}
    }
    
    @Override
    public void bind() throws Exception {
        if (this.acceptorThreadCount == 0) {
            this.acceptorThreadCount = 1;
        }
        if (this.getMaxConnections() == 0) {
            this.setMaxConnections(this.getMaxThreadsExecutor(true));
        }
        if (this.serverSocketFactory == null) {
            if (this.isSSLEnabled()) {
                this.serverSocketFactory = this.handler.getSslImplementation().getServerSocketFactory(this);
            }
            else {
                this.serverSocketFactory = new DefaultServerSocketFactory(this);
            }
        }
        if (this.serverSocket == null) {
            try {
                if (this.getAddress() == null) {
                    this.serverSocket = this.serverSocketFactory.createSocket(this.getPort(), this.getBacklog());
                }
                else {
                    this.serverSocket = this.serverSocketFactory.createSocket(this.getPort(), this.getBacklog(), this.getAddress());
                }
            }
            catch (BindException orig) {
                String msg;
                if (this.getAddress() == null) {
                    msg = orig.getMessage() + " <null>:" + this.getPort();
                }
                else {
                    msg = orig.getMessage() + " " + this.getAddress().toString() + ":" + this.getPort();
                }
                final BindException be = new BindException(msg);
                be.initCause(orig);
                throw be;
            }
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
            this.startAcceptorThreads();
            final Thread timeoutThread = new Thread(new AsyncTimeout(), this.getName() + "-AsyncTimeout");
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
            this.unlockAccept();
        }
        this.shutdownExecutor();
    }
    
    @Override
    public void unbind() throws Exception {
        if (this.running) {
            this.stop();
        }
        if (this.serverSocket != null) {
            try {
                if (this.serverSocket != null) {
                    this.serverSocket.close();
                }
            }
            catch (Exception e) {
                JIoEndpoint.log.error((Object)JIoEndpoint.sm.getString("endpoint.err.close"), (Throwable)e);
            }
            this.serverSocket = null;
        }
        this.handler.recycle();
    }
    
    @Override
    protected AbstractEndpoint.Acceptor createAcceptor() {
        return new Acceptor();
    }
    
    protected boolean setSocketOptions(final Socket socket) {
        try {
            this.socketProperties.setProperties(socket);
        }
        catch (SocketException s) {
            if (JIoEndpoint.log.isDebugEnabled()) {
                JIoEndpoint.log.debug((Object)JIoEndpoint.sm.getString("endpoint.err.unexpected"), (Throwable)s);
            }
            return false;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            JIoEndpoint.log.error((Object)JIoEndpoint.sm.getString("endpoint.err.unexpected"), t);
            return false;
        }
        return true;
    }
    
    protected boolean processSocket(final Socket socket) {
        try {
            final SocketWrapper<Socket> wrapper = new SocketWrapper<Socket>(socket);
            wrapper.setKeepAliveLeft(this.getMaxKeepAliveRequests());
            wrapper.setSecure(this.isSSLEnabled());
            if (!this.running) {
                return false;
            }
            this.getExecutor().execute(new SocketProcessor(wrapper));
        }
        catch (RejectedExecutionException x) {
            JIoEndpoint.log.warn((Object)("Socket processing request was rejected for:" + socket), (Throwable)x);
            return false;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            JIoEndpoint.log.error((Object)JIoEndpoint.sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }
    
    public boolean processSocketAsync(final SocketWrapper<Socket> socket, final SocketStatus status) {
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
                        if (!this.running) {
                            return false;
                        }
                        this.getExecutor().execute(proc);
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
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            JIoEndpoint.log.error((Object)JIoEndpoint.sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }
    
    @Override
    protected Log getLog() {
        return JIoEndpoint.log;
    }
    
    static {
        log = LogFactory.getLog((Class)JIoEndpoint.class);
    }
    
    protected class AsyncTimeout implements Runnable
    {
        @Override
        public void run() {
            while (JIoEndpoint.this.running) {
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException ex) {}
                final long now = System.currentTimeMillis();
                for (final SocketWrapper<Socket> socket : JIoEndpoint.this.waitingRequests) {
                    final long access = socket.getLastAccess();
                    if (socket.getTimeout() > 0L && now - access > socket.getTimeout()) {
                        JIoEndpoint.this.processSocketAsync(socket, SocketStatus.TIMEOUT);
                    }
                }
                while (JIoEndpoint.this.paused && JIoEndpoint.this.running) {
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e) {}
                }
            }
        }
    }
    
    protected class Acceptor extends AbstractEndpoint.Acceptor
    {
        @Override
        public void run() {
            int errorDelay = 0;
            while (JIoEndpoint.this.running) {
                while (JIoEndpoint.this.paused && JIoEndpoint.this.running) {
                    this.state = AcceptorState.PAUSED;
                    try {
                        Thread.sleep(50L);
                    }
                    catch (InterruptedException e) {}
                }
                if (!JIoEndpoint.this.running) {
                    break;
                }
                this.state = AcceptorState.RUNNING;
                try {
                    JIoEndpoint.this.countUpOrAwaitConnection();
                    Socket socket = null;
                    try {
                        socket = JIoEndpoint.this.serverSocketFactory.acceptSocket(JIoEndpoint.this.serverSocket);
                    }
                    catch (IOException ioe) {
                        JIoEndpoint.this.countDownConnection();
                        errorDelay = JIoEndpoint.this.handleExceptionWithDelay(errorDelay);
                        throw ioe;
                    }
                    errorDelay = 0;
                    if (JIoEndpoint.this.running && !JIoEndpoint.this.paused && JIoEndpoint.this.setSocketOptions(socket)) {
                        if (JIoEndpoint.this.processSocket(socket)) {
                            continue;
                        }
                        JIoEndpoint.this.countDownConnection();
                        JIoEndpoint.this.closeSocket(socket);
                    }
                    else {
                        JIoEndpoint.this.countDownConnection();
                        JIoEndpoint.this.closeSocket(socket);
                    }
                }
                catch (IOException x) {
                    if (!JIoEndpoint.this.running) {
                        continue;
                    }
                    JIoEndpoint.log.error((Object)AbstractEndpoint.sm.getString("endpoint.accept.fail"), (Throwable)x);
                }
                catch (NullPointerException npe) {
                    if (!JIoEndpoint.this.running) {
                        continue;
                    }
                    JIoEndpoint.log.error((Object)AbstractEndpoint.sm.getString("endpoint.accept.fail"), (Throwable)npe);
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    JIoEndpoint.log.error((Object)AbstractEndpoint.sm.getString("endpoint.accept.fail"), t);
                }
            }
            this.state = AcceptorState.ENDED;
        }
    }
    
    protected class SocketProcessor implements Runnable
    {
        protected SocketWrapper<Socket> socket;
        protected SocketStatus status;
        
        public SocketProcessor(final SocketWrapper<Socket> socket) {
            this.socket = null;
            this.status = null;
            if (socket == null) {
                throw new NullPointerException();
            }
            this.socket = socket;
        }
        
        public SocketProcessor(final JIoEndpoint ioEndpoint, final SocketWrapper<Socket> socket, final SocketStatus status) {
            this(socket);
            this.status = status;
        }
        
        @Override
        public void run() {
            boolean launch = false;
            synchronized (this.socket) {
                try {
                    AbstractEndpoint.Handler.SocketState state = AbstractEndpoint.Handler.SocketState.OPEN;
                    try {
                        JIoEndpoint.this.serverSocketFactory.handshake(this.socket.getSocket());
                    }
                    catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        if (JIoEndpoint.log.isDebugEnabled()) {
                            JIoEndpoint.log.debug((Object)AbstractEndpoint.sm.getString("endpoint.err.handshake"), t);
                        }
                        state = AbstractEndpoint.Handler.SocketState.CLOSED;
                    }
                    if (state != AbstractEndpoint.Handler.SocketState.CLOSED) {
                        if (this.status == null) {
                            state = JIoEndpoint.this.handler.process(this.socket, SocketStatus.OPEN_READ);
                        }
                        else {
                            state = JIoEndpoint.this.handler.process(this.socket, this.status);
                        }
                    }
                    if (state == AbstractEndpoint.Handler.SocketState.CLOSED) {
                        if (JIoEndpoint.log.isTraceEnabled()) {
                            JIoEndpoint.log.trace((Object)("Closing socket:" + this.socket));
                        }
                        JIoEndpoint.this.countDownConnection();
                        try {
                            this.socket.getSocket().close();
                        }
                        catch (IOException e) {}
                    }
                    else if (state == AbstractEndpoint.Handler.SocketState.OPEN || state == AbstractEndpoint.Handler.SocketState.UPGRADING || state == AbstractEndpoint.Handler.SocketState.UPGRADING_TOMCAT || state == AbstractEndpoint.Handler.SocketState.UPGRADED) {
                        this.socket.setKeptAlive(true);
                        this.socket.access();
                        launch = true;
                    }
                    else if (state == AbstractEndpoint.Handler.SocketState.LONG) {
                        this.socket.access();
                        JIoEndpoint.this.waitingRequests.add(this.socket);
                    }
                }
                finally {
                    if (launch) {
                        try {
                            JIoEndpoint.this.getExecutor().execute(new SocketProcessor(this.socket, SocketStatus.OPEN_READ));
                        }
                        catch (RejectedExecutionException x) {
                            JIoEndpoint.log.warn((Object)("Socket reprocessing request was rejected for:" + this.socket), (Throwable)x);
                            try {
                                JIoEndpoint.this.handler.process(this.socket, SocketStatus.DISCONNECT);
                            }
                            finally {
                                JIoEndpoint.this.countDownConnection();
                            }
                        }
                        catch (NullPointerException npe) {
                            if (JIoEndpoint.this.running) {
                                JIoEndpoint.log.error((Object)AbstractEndpoint.sm.getString("endpoint.launch.fail"), (Throwable)npe);
                            }
                        }
                    }
                }
            }
            this.socket = null;
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
        SocketState process(final SocketWrapper<Socket> p0, final SocketStatus p1);
        
        SSLImplementation getSslImplementation();
    }
}
