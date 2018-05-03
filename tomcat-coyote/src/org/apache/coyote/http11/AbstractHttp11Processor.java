// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.tomcat.util.net.SocketStatus;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.MimeHeaders;
import java.util.Locale;
import org.apache.coyote.RequestInfo;
import java.io.InterruptedIOException;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.coyote.AsyncContextCallback;
import org.apache.coyote.http11.filters.SavedRequestInputFilter;
import java.io.IOException;
import org.apache.coyote.ActionCode;
import org.apache.coyote.http11.filters.GzipOutputFilter;
import org.apache.coyote.http11.filters.BufferedInputFilter;
import org.apache.coyote.http11.filters.VoidOutputFilter;
import org.apache.coyote.http11.filters.VoidInputFilter;
import org.apache.coyote.http11.filters.ChunkedOutputFilter;
import org.apache.coyote.http11.filters.ChunkedInputFilter;
import org.apache.coyote.http11.filters.IdentityOutputFilter;
import org.apache.coyote.http11.filters.IdentityInputFilter;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.buf.Ascii;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import java.util.StringTokenizer;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.juli.logging.Log;
import org.apache.coyote.http11.upgrade.servlet31.HttpUpgradeHandler;
import org.apache.coyote.http11.upgrade.UpgradeInbound;
import java.util.regex.Pattern;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.log.UserDataHelper;
import org.apache.coyote.AbstractProcessor;

public abstract class AbstractHttp11Processor<S> extends AbstractProcessor<S>
{
    private final UserDataHelper userDataHelper;
    protected static final StringManager sm;
    private int pluggableFilterIndex;
    protected boolean error;
    protected boolean keepAlive;
    protected boolean openSocket;
    protected boolean keptAlive;
    protected boolean sendfileInProgress;
    protected boolean readComplete;
    protected boolean http11;
    protected boolean http09;
    protected boolean contentDelimitation;
    protected boolean expectation;
    protected boolean comet;
    protected Pattern restrictedUserAgents;
    protected int maxKeepAliveRequests;
    protected int keepAliveTimeout;
    protected String remoteAddr;
    protected String remoteHost;
    protected String localName;
    protected int localPort;
    protected int remotePort;
    protected String localAddr;
    protected int connectionUploadTimeout;
    protected boolean disableUploadTimeout;
    protected int compressionLevel;
    protected int compressionMinSize;
    protected int socketBuffer;
    protected int maxSavePostSize;
    protected Pattern noCompressionUserAgents;
    protected String[] compressableMimeTypes;
    protected char[] hostNameC;
    protected String server;
    @Deprecated
    protected UpgradeInbound upgradeInbound;
    protected HttpUpgradeHandler httpUpgradeHandler;
    
    protected abstract Log getLog();
    
    public AbstractHttp11Processor(final AbstractEndpoint endpoint) {
        super(endpoint);
        this.pluggableFilterIndex = Integer.MAX_VALUE;
        this.error = false;
        this.keepAlive = true;
        this.openSocket = false;
        this.sendfileInProgress = false;
        this.readComplete = true;
        this.http11 = true;
        this.http09 = false;
        this.contentDelimitation = true;
        this.expectation = false;
        this.comet = false;
        this.restrictedUserAgents = null;
        this.maxKeepAliveRequests = -1;
        this.keepAliveTimeout = -1;
        this.remoteAddr = null;
        this.remoteHost = null;
        this.localName = null;
        this.localPort = -1;
        this.remotePort = -1;
        this.localAddr = null;
        this.connectionUploadTimeout = 300000;
        this.disableUploadTimeout = false;
        this.compressionLevel = 0;
        this.compressionMinSize = 2048;
        this.socketBuffer = -1;
        this.maxSavePostSize = 4096;
        this.noCompressionUserAgents = null;
        this.compressableMimeTypes = new String[] { "text/html", "text/xml", "text/plain" };
        this.hostNameC = new char[0];
        this.server = null;
        this.upgradeInbound = null;
        this.httpUpgradeHandler = null;
        this.userDataHelper = new UserDataHelper(this.getLog());
    }
    
    public void setCompression(final String compression) {
        if (compression.equals("on")) {
            this.compressionLevel = 1;
        }
        else if (compression.equals("force")) {
            this.compressionLevel = 2;
        }
        else if (compression.equals("off")) {
            this.compressionLevel = 0;
        }
        else {
            try {
                this.compressionMinSize = Integer.parseInt(compression);
                this.compressionLevel = 1;
            }
            catch (Exception e) {
                this.compressionLevel = 0;
            }
        }
    }
    
    public void setCompressionMinSize(final int compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }
    
    public void setNoCompressionUserAgents(final String noCompressionUserAgents) {
        if (noCompressionUserAgents == null || noCompressionUserAgents.length() == 0) {
            this.noCompressionUserAgents = null;
        }
        else {
            this.noCompressionUserAgents = Pattern.compile(noCompressionUserAgents);
        }
    }
    
    public void addCompressableMimeType(final String mimeType) {
        this.compressableMimeTypes = this.addStringArray(this.compressableMimeTypes, mimeType);
    }
    
    public void setCompressableMimeTypes(final String[] compressableMimeTypes) {
        this.compressableMimeTypes = compressableMimeTypes;
    }
    
    public void setCompressableMimeTypes(final String compressableMimeTypes) {
        if (compressableMimeTypes != null) {
            this.compressableMimeTypes = null;
            final StringTokenizer st = new StringTokenizer(compressableMimeTypes, ",");
            while (st.hasMoreTokens()) {
                this.addCompressableMimeType(st.nextToken().trim());
            }
        }
    }
    
    public String getCompression() {
        switch (this.compressionLevel) {
            case 0: {
                return "off";
            }
            case 1: {
                return "on";
            }
            case 2: {
                return "force";
            }
            default: {
                return "off";
            }
        }
    }
    
    private String[] addStringArray(final String[] sArray, final String value) {
        String[] result = null;
        if (sArray == null) {
            result = new String[] { value };
        }
        else {
            result = new String[sArray.length + 1];
            for (int i = 0; i < sArray.length; ++i) {
                result[i] = sArray[i];
            }
            result[sArray.length] = value;
        }
        return result;
    }
    
    private boolean startsWithStringArray(final String[] sArray, final String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < sArray.length; ++i) {
            if (value.startsWith(sArray[i])) {
                return true;
            }
        }
        return false;
    }
    
    public void setRestrictedUserAgents(final String restrictedUserAgents) {
        if (restrictedUserAgents == null || restrictedUserAgents.length() == 0) {
            this.restrictedUserAgents = null;
        }
        else {
            this.restrictedUserAgents = Pattern.compile(restrictedUserAgents);
        }
    }
    
    public void setMaxKeepAliveRequests(final int mkar) {
        this.maxKeepAliveRequests = mkar;
    }
    
    public int getMaxKeepAliveRequests() {
        return this.maxKeepAliveRequests;
    }
    
    public void setKeepAliveTimeout(final int timeout) {
        this.keepAliveTimeout = timeout;
    }
    
    public int getKeepAliveTimeout() {
        return this.keepAliveTimeout;
    }
    
    public void setMaxSavePostSize(final int msps) {
        this.maxSavePostSize = msps;
    }
    
    public int getMaxSavePostSize() {
        return this.maxSavePostSize;
    }
    
    public void setDisableUploadTimeout(final boolean isDisabled) {
        this.disableUploadTimeout = isDisabled;
    }
    
    public boolean getDisableUploadTimeout() {
        return this.disableUploadTimeout;
    }
    
    public void setSocketBuffer(final int socketBuffer) {
        this.socketBuffer = socketBuffer;
    }
    
    public int getSocketBuffer() {
        return this.socketBuffer;
    }
    
    public void setConnectionUploadTimeout(final int timeout) {
        this.connectionUploadTimeout = timeout;
    }
    
    public int getConnectionUploadTimeout() {
        return this.connectionUploadTimeout;
    }
    
    public void setServer(final String server) {
        if (server == null || server.equals("")) {
            this.server = null;
        }
        else {
            this.server = server;
        }
    }
    
    public String getServer() {
        return this.server;
    }
    
    private boolean isCompressable() {
        final MessageBytes contentEncodingMB = this.response.getMimeHeaders().getValue("Content-Encoding");
        if (contentEncodingMB != null && contentEncodingMB.indexOf("gzip") != -1) {
            return false;
        }
        if (this.compressionLevel == 2) {
            return true;
        }
        final long contentLength = this.response.getContentLengthLong();
        return (contentLength == -1L || contentLength > this.compressionMinSize) && this.compressableMimeTypes != null && this.startsWithStringArray(this.compressableMimeTypes, this.response.getContentType());
    }
    
    private boolean useCompression() {
        final MessageBytes acceptEncodingMB = this.request.getMimeHeaders().getValue("accept-encoding");
        if (acceptEncodingMB == null || acceptEncodingMB.indexOf("gzip") == -1) {
            return false;
        }
        if (this.compressionLevel == 2) {
            return true;
        }
        if (this.noCompressionUserAgents != null) {
            final MessageBytes userAgentValueMB = this.request.getMimeHeaders().getValue("user-agent");
            if (userAgentValueMB != null) {
                final String userAgentValue = userAgentValueMB.toString();
                if (this.noCompressionUserAgents != null && this.noCompressionUserAgents.matcher(userAgentValue).matches()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    protected int findBytes(final ByteChunk bc, final byte[] b) {
        final byte first = b[0];
        final byte[] buff = bc.getBuffer();
        final int start = bc.getStart();
        for (int end = bc.getEnd(), srcEnd = b.length, i = start; i <= end - srcEnd; ++i) {
            if (Ascii.toLower(buff[i]) == first) {
                int myPos = i + 1;
                int srcPos = 1;
                while (srcPos < srcEnd) {
                    if (Ascii.toLower(buff[myPos++]) != b[srcPos++]) {
                        break;
                    }
                    if (srcPos == srcEnd) {
                        return i - start;
                    }
                }
            }
        }
        return -1;
    }
    
    protected boolean statusDropsConnection(final int status) {
        return status == 400 || status == 408 || status == 411 || status == 413 || status == 414 || status == 500 || status == 503 || status == 501;
    }
    
    protected abstract void setSocketWrapper(final SocketWrapper<S> p0);
    
    protected abstract AbstractInputBuffer<S> getInputBuffer();
    
    protected abstract AbstractOutputBuffer<S> getOutputBuffer();
    
    protected void initializeFilters(final int maxTrailerSize, final int maxExtensionSize) {
        this.getInputBuffer().addFilter(new IdentityInputFilter());
        this.getOutputBuffer().addFilter(new IdentityOutputFilter());
        this.getInputBuffer().addFilter(new ChunkedInputFilter(maxTrailerSize, maxExtensionSize));
        this.getOutputBuffer().addFilter(new ChunkedOutputFilter());
        this.getInputBuffer().addFilter(new VoidInputFilter());
        this.getOutputBuffer().addFilter(new VoidOutputFilter());
        this.getInputBuffer().addFilter(new BufferedInputFilter());
        this.getOutputBuffer().addFilter(new GzipOutputFilter());
        this.pluggableFilterIndex = this.getInputBuffer().getFilters().length;
    }
    
    protected boolean addInputFilter(final InputFilter[] inputFilters, final String encodingName) {
        if (!encodingName.equals("identity")) {
            if (!encodingName.equals("chunked")) {
                for (int i = this.pluggableFilterIndex; i < inputFilters.length; ++i) {
                    if (inputFilters[i].getEncodingName().toString().equals(encodingName)) {
                        this.getInputBuffer().addActiveFilter(inputFilters[i]);
                        return true;
                    }
                }
                return false;
            }
            this.getInputBuffer().addActiveFilter(inputFilters[1]);
            this.contentDelimitation = true;
        }
        return true;
    }
    
    @Override
    public final void action(final ActionCode actionCode, final Object param) {
        if (actionCode == ActionCode.CLOSE) {
            try {
                this.getOutputBuffer().endRequest();
            }
            catch (IOException e) {
                this.error = true;
            }
        }
        else if (actionCode == ActionCode.COMMIT) {
            if (this.response.isCommitted()) {
                return;
            }
            try {
                this.prepareResponse();
                this.getOutputBuffer().commit();
            }
            catch (IOException e) {
                this.error = true;
            }
        }
        else if (actionCode == ActionCode.ACK) {
            if (this.response.isCommitted() || !this.expectation) {
                return;
            }
            this.getInputBuffer().setSwallowInput(true);
            try {
                this.getOutputBuffer().sendAck();
            }
            catch (IOException e) {
                this.error = true;
            }
        }
        else if (actionCode == ActionCode.CLIENT_FLUSH) {
            try {
                this.getOutputBuffer().flush();
            }
            catch (IOException e) {
                this.error = true;
                this.response.setErrorException(e);
            }
        }
        else if (actionCode == ActionCode.DISABLE_SWALLOW_INPUT) {
            this.error = true;
            this.getInputBuffer().setSwallowInput(false);
        }
        else if (actionCode == ActionCode.RESET) {
            this.getOutputBuffer().reset();
        }
        else if (actionCode != ActionCode.CUSTOM) {
            if (actionCode == ActionCode.REQ_SET_BODY_REPLAY) {
                final ByteChunk body = (ByteChunk)param;
                final InputFilter savedBody = new SavedRequestInputFilter(body);
                savedBody.setRequest(this.request);
                final AbstractInputBuffer<S> internalBuffer = (AbstractInputBuffer<S>)this.request.getInputBuffer();
                internalBuffer.addActiveFilter(savedBody);
            }
            else if (actionCode == ActionCode.ASYNC_START) {
                this.asyncStateMachine.asyncStart((AsyncContextCallback)param);
            }
            else if (actionCode == ActionCode.ASYNC_DISPATCHED) {
                this.asyncStateMachine.asyncDispatched();
            }
            else if (actionCode == ActionCode.ASYNC_TIMEOUT) {
                final AtomicBoolean result = (AtomicBoolean)param;
                result.set(this.asyncStateMachine.asyncTimeout());
            }
            else if (actionCode == ActionCode.ASYNC_RUN) {
                this.asyncStateMachine.asyncRun((Runnable)param);
            }
            else if (actionCode == ActionCode.ASYNC_ERROR) {
                this.asyncStateMachine.asyncError();
            }
            else if (actionCode == ActionCode.ASYNC_IS_STARTED) {
                ((AtomicBoolean)param).set(this.asyncStateMachine.isAsyncStarted());
            }
            else if (actionCode == ActionCode.ASYNC_IS_DISPATCHING) {
                ((AtomicBoolean)param).set(this.asyncStateMachine.isAsyncDispatching());
            }
            else if (actionCode == ActionCode.ASYNC_IS_ASYNC) {
                ((AtomicBoolean)param).set(this.asyncStateMachine.isAsync());
            }
            else if (actionCode == ActionCode.ASYNC_IS_TIMINGOUT) {
                ((AtomicBoolean)param).set(this.asyncStateMachine.isAsyncTimingOut());
            }
            else if (actionCode == ActionCode.ASYNC_IS_ERROR) {
                ((AtomicBoolean)param).set(this.asyncStateMachine.isAsyncError());
            }
            else if (actionCode == ActionCode.UPGRADE_TOMCAT) {
                this.upgradeInbound = (UpgradeInbound)param;
                this.getOutputBuffer().finished = true;
            }
            else if (actionCode == ActionCode.UPGRADE) {
                this.httpUpgradeHandler = (HttpUpgradeHandler)param;
                this.getOutputBuffer().finished = true;
            }
            else {
                this.actionInternal(actionCode, param);
            }
        }
    }
    
    abstract void actionInternal(final ActionCode p0, final Object p1);
    
    protected abstract boolean disableKeepAlive();
    
    protected abstract void setRequestLineReadTimeout() throws IOException;
    
    protected abstract boolean handleIncompleteRequestLineRead();
    
    protected abstract void setSocketTimeout(final int p0) throws IOException;
    
    @Override
    public AbstractEndpoint.Handler.SocketState process(final SocketWrapper<S> socketWrapper) throws IOException {
        final RequestInfo rp = this.request.getRequestProcessor();
        rp.setStage(1);
        this.setSocketWrapper(socketWrapper);
        this.getInputBuffer().init(socketWrapper, this.endpoint);
        this.getOutputBuffer().init(socketWrapper, this.endpoint);
        this.error = false;
        this.keepAlive = true;
        this.comet = false;
        this.openSocket = false;
        this.sendfileInProgress = false;
        this.readComplete = true;
        if (this.endpoint.getUsePolling()) {
            this.keptAlive = false;
        }
        else {
            this.keptAlive = socketWrapper.isKeptAlive();
        }
        if (this.disableKeepAlive()) {
            socketWrapper.setKeepAliveLeft(0);
        }
        while (!this.error && this.keepAlive && !this.comet && !this.isAsync() && this.upgradeInbound == null && this.httpUpgradeHandler == null && !this.endpoint.isPaused()) {
            try {
                this.setRequestLineReadTimeout();
                if (!this.getInputBuffer().parseRequestLine(this.keptAlive) && this.handleIncompleteRequestLineRead()) {
                    break;
                }
                if (this.endpoint.isPaused()) {
                    this.response.setStatus(503);
                    this.error = true;
                }
                else {
                    if (this.request.getStartTime() < 0L) {
                        this.request.setStartTime(System.currentTimeMillis());
                    }
                    this.keptAlive = true;
                    this.request.getMimeHeaders().setLimit(this.endpoint.getMaxHeaderCount());
                    if (!this.getInputBuffer().parseHeaders()) {
                        this.openSocket = true;
                        this.readComplete = false;
                        break;
                    }
                    if (!this.disableUploadTimeout) {
                        this.setSocketTimeout(this.connectionUploadTimeout);
                    }
                }
            }
            catch (IOException e) {
                if (this.getLog().isDebugEnabled()) {
                    this.getLog().debug((Object)AbstractHttp11Processor.sm.getString("http11processor.header.parse"), (Throwable)e);
                }
                this.error = true;
                break;
            }
            catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                final UserDataHelper.Mode logMode = this.userDataHelper.getNextMode();
                if (logMode != null) {
                    String message = AbstractHttp11Processor.sm.getString("http11processor.header.parse");
                    switch (logMode) {
                        case INFO_THEN_DEBUG: {
                            message += AbstractHttp11Processor.sm.getString("http11processor.fallToDebug");
                        }
                        case INFO: {
                            this.getLog().info((Object)message);
                            break;
                        }
                        case DEBUG: {
                            this.getLog().debug((Object)message);
                            break;
                        }
                    }
                }
                this.response.setStatus(400);
                this.adapter.log(this.request, this.response, 0L);
                this.error = true;
            }
            if (!this.error) {
                rp.setStage(2);
                try {
                    this.prepareRequest();
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    if (this.getLog().isDebugEnabled()) {
                        this.getLog().debug((Object)AbstractHttp11Processor.sm.getString("http11processor.request.prepare"), t);
                    }
                    this.response.setStatus(400);
                    this.adapter.log(this.request, this.response, 0L);
                    this.error = true;
                }
            }
            if (this.maxKeepAliveRequests == 1) {
                this.keepAlive = false;
            }
            else if (this.maxKeepAliveRequests > 0 && socketWrapper.decrementKeepAlive() <= 0) {
                this.keepAlive = false;
            }
            if (!this.error) {
                try {
                    rp.setStage(3);
                    this.adapter.service(this.request, this.response);
                    if (this.keepAlive && !this.error) {
                        this.error = (this.response.getErrorException() != null || (!this.isAsync() && this.statusDropsConnection(this.response.getStatus())));
                    }
                    this.setCometTimeouts(socketWrapper);
                }
                catch (InterruptedIOException e2) {
                    this.error = true;
                }
                catch (HeadersTooLargeException e3) {
                    this.error = true;
                    if (!this.response.isCommitted()) {
                        this.response.reset();
                        this.response.setStatus(500);
                        this.response.setHeader("Connection", "close");
                    }
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    this.getLog().error((Object)AbstractHttp11Processor.sm.getString("http11processor.request.process"), t);
                    this.response.setStatus(500);
                    this.adapter.log(this.request, this.response, 0L);
                    this.error = true;
                }
            }
            rp.setStage(4);
            if (!this.isAsync() && !this.comet) {
                if (this.error) {
                    this.getInputBuffer().setSwallowInput(false);
                }
                this.endRequest();
            }
            rp.setStage(5);
            if (this.error) {
                this.response.setStatus(500);
            }
            this.request.updateCounters();
            if ((!this.isAsync() && !this.comet) || this.error) {
                this.getInputBuffer().nextRequest();
                this.getOutputBuffer().nextRequest();
            }
            if (!this.disableUploadTimeout) {
                if (this.endpoint.getSoTimeout() > 0) {
                    this.setSocketTimeout(this.endpoint.getSoTimeout());
                }
                else {
                    this.setSocketTimeout(0);
                }
            }
            rp.setStage(6);
            if (this.breakKeepAliveLoop(socketWrapper)) {
                break;
            }
        }
        rp.setStage(7);
        if (this.error || this.endpoint.isPaused()) {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        if (this.isAsync() || this.comet) {
            return AbstractEndpoint.Handler.SocketState.LONG;
        }
        if (this.isUpgrade()) {
            return AbstractEndpoint.Handler.SocketState.UPGRADING;
        }
        if (this.getUpgradeInbound() != null) {
            return AbstractEndpoint.Handler.SocketState.UPGRADING_TOMCAT;
        }
        if (this.sendfileInProgress) {
            return AbstractEndpoint.Handler.SocketState.SENDFILE;
        }
        if (!this.openSocket) {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        if (this.readComplete) {
            return AbstractEndpoint.Handler.SocketState.OPEN;
        }
        return AbstractEndpoint.Handler.SocketState.LONG;
    }
    
    protected void prepareRequest() {
        this.http11 = true;
        this.http09 = false;
        this.contentDelimitation = false;
        this.expectation = false;
        this.prepareRequestInternal();
        if (this.endpoint.isSSLEnabled()) {
            this.request.scheme().setString("https");
        }
        final MessageBytes protocolMB = this.request.protocol();
        if (protocolMB.equals("HTTP/1.1")) {
            this.http11 = true;
            protocolMB.setString("HTTP/1.1");
        }
        else if (protocolMB.equals("HTTP/1.0")) {
            this.http11 = false;
            this.keepAlive = false;
            protocolMB.setString("HTTP/1.0");
        }
        else if (protocolMB.equals("")) {
            this.http09 = true;
            this.http11 = false;
            this.keepAlive = false;
        }
        else {
            this.http11 = false;
            this.error = true;
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug((Object)(AbstractHttp11Processor.sm.getString("http11processor.request.prepare") + " Unsupported HTTP version \"" + protocolMB + "\""));
            }
            this.response.setStatus(505);
        }
        final MessageBytes methodMB = this.request.method();
        if (methodMB.equals("GET")) {
            methodMB.setString("GET");
        }
        else if (methodMB.equals("POST")) {
            methodMB.setString("POST");
        }
        final MimeHeaders headers = this.request.getMimeHeaders();
        final MessageBytes connectionValueMB = headers.getValue("Connection");
        if (connectionValueMB != null) {
            final ByteChunk connectionValueBC = connectionValueMB.getByteChunk();
            if (this.findBytes(connectionValueBC, Constants.CLOSE_BYTES) != -1) {
                this.keepAlive = false;
            }
            else if (this.findBytes(connectionValueBC, Constants.KEEPALIVE_BYTES) != -1) {
                this.keepAlive = true;
            }
        }
        MessageBytes expectMB = null;
        if (this.http11) {
            expectMB = headers.getValue("expect");
        }
        if (expectMB != null && expectMB.indexOfIgnoreCase("100-continue", 0) != -1) {
            this.getInputBuffer().setSwallowInput(false);
            this.expectation = true;
        }
        if (this.restrictedUserAgents != null && (this.http11 || this.keepAlive)) {
            final MessageBytes userAgentValueMB = headers.getValue("user-agent");
            if (userAgentValueMB != null) {
                final String userAgentValue = userAgentValueMB.toString();
                if (this.restrictedUserAgents != null && this.restrictedUserAgents.matcher(userAgentValue).matches()) {
                    this.http11 = false;
                    this.keepAlive = false;
                }
            }
        }
        final ByteChunk uriBC = this.request.requestURI().getByteChunk();
        if (uriBC.startsWithIgnoreCase("http", 0)) {
            final int pos = uriBC.indexOf("://", 0, 3, 4);
            final int uriBCStart = uriBC.getStart();
            int slashPos = -1;
            if (pos != -1) {
                final byte[] uriB = uriBC.getBytes();
                slashPos = uriBC.indexOf('/', pos + 3);
                if (slashPos == -1) {
                    slashPos = uriBC.getLength();
                    this.request.requestURI().setBytes(uriB, uriBCStart + pos + 1, 1);
                }
                else {
                    this.request.requestURI().setBytes(uriB, uriBCStart + slashPos, uriBC.getLength() - slashPos);
                }
                final MessageBytes hostMB = headers.setValue("host");
                hostMB.setBytes(uriB, uriBCStart + pos + 3, slashPos - pos - 3);
            }
        }
        final InputFilter[] inputFilters = this.getInputBuffer().getFilters();
        MessageBytes transferEncodingValueMB = null;
        if (this.http11) {
            transferEncodingValueMB = headers.getValue("transfer-encoding");
        }
        if (transferEncodingValueMB != null) {
            final String transferEncodingValue = transferEncodingValueMB.toString();
            int startPos = 0;
            int commaPos = transferEncodingValue.indexOf(44);
            String encodingName = null;
            while (commaPos != -1) {
                encodingName = transferEncodingValue.substring(startPos, commaPos).toLowerCase(Locale.ENGLISH).trim();
                if (!this.addInputFilter(inputFilters, encodingName)) {
                    this.error = true;
                    this.response.setStatus(501);
                }
                startPos = commaPos + 1;
                commaPos = transferEncodingValue.indexOf(44, startPos);
            }
            encodingName = transferEncodingValue.substring(startPos).toLowerCase(Locale.ENGLISH).trim();
            if (!this.addInputFilter(inputFilters, encodingName)) {
                this.error = true;
                if (this.getLog().isDebugEnabled()) {
                    this.getLog().debug((Object)(AbstractHttp11Processor.sm.getString("http11processor.request.prepare") + " Unsupported transfer encoding \"" + encodingName + "\""));
                }
                this.response.setStatus(501);
            }
        }
        final long contentLength = this.request.getContentLengthLong();
        if (contentLength >= 0L) {
            if (this.contentDelimitation) {
                headers.removeHeader("content-length");
                this.request.setContentLength(-1L);
            }
            else {
                this.getInputBuffer().addActiveFilter(inputFilters[0]);
                this.contentDelimitation = true;
            }
        }
        final MessageBytes valueMB = headers.getValue("host");
        if (this.http11 && valueMB == null) {
            this.error = true;
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug((Object)(AbstractHttp11Processor.sm.getString("http11processor.request.prepare") + " host header missing"));
            }
            this.response.setStatus(400);
        }
        this.parseHost(valueMB);
        if (!this.contentDelimitation) {
            this.getInputBuffer().addActiveFilter(inputFilters[2]);
            this.contentDelimitation = true;
        }
        if (this.endpoint.getUseSendfile()) {
            this.request.setAttribute("org.apache.tomcat.sendfile.support", Boolean.TRUE);
        }
        if (this.endpoint.getUseComet()) {
            this.request.setAttribute("org.apache.tomcat.comet.support", Boolean.TRUE);
        }
        if (this.endpoint.getUseCometTimeout()) {
            this.request.setAttribute("org.apache.tomcat.comet.timeout.support", Boolean.TRUE);
        }
        if (this.error) {
            this.adapter.log(this.request, this.response, 0L);
        }
    }
    
    protected abstract void prepareRequestInternal();
    
    private void prepareResponse() {
        boolean entityBody = true;
        this.contentDelimitation = false;
        final OutputFilter[] outputFilters = this.getOutputBuffer().getFilters();
        if (this.http09) {
            this.getOutputBuffer().addActiveFilter(outputFilters[0]);
            return;
        }
        final int statusCode = this.response.getStatus();
        if (statusCode < 200 || statusCode == 204 || statusCode == 205 || statusCode == 304) {
            this.getOutputBuffer().addActiveFilter(outputFilters[2]);
            entityBody = false;
            this.contentDelimitation = true;
        }
        final MessageBytes methodMB = this.request.method();
        if (methodMB.equals("HEAD")) {
            this.getOutputBuffer().addActiveFilter(outputFilters[2]);
            this.contentDelimitation = true;
        }
        boolean sendingWithSendfile = false;
        if (this.getEndpoint().getUseSendfile()) {
            sendingWithSendfile = this.prepareSendfile(outputFilters);
        }
        boolean isCompressable = false;
        boolean useCompression = false;
        if (entityBody && this.compressionLevel > 0 && !sendingWithSendfile) {
            isCompressable = this.isCompressable();
            if (isCompressable) {
                useCompression = this.useCompression();
            }
            if (useCompression) {
                this.response.setContentLength(-1);
            }
        }
        final MimeHeaders headers = this.response.getMimeHeaders();
        if (!entityBody) {
            this.response.setContentLength(-1);
        }
        if (entityBody || statusCode == 204) {
            final String contentType = this.response.getContentType();
            if (contentType != null) {
                headers.setValue("Content-Type").setString(contentType);
            }
            final String contentLanguage = this.response.getContentLanguage();
            if (contentLanguage != null) {
                headers.setValue("Content-Language").setString(contentLanguage);
            }
        }
        final long contentLength = this.response.getContentLengthLong();
        boolean connectionClosePresent = false;
        if (contentLength != -1L) {
            headers.setValue("Content-Length").setLong(contentLength);
            this.getOutputBuffer().addActiveFilter(outputFilters[0]);
            this.contentDelimitation = true;
        }
        else {
            connectionClosePresent = this.isConnectionClose(headers);
            if (entityBody && this.http11 && !connectionClosePresent) {
                this.getOutputBuffer().addActiveFilter(outputFilters[1]);
                this.contentDelimitation = true;
                headers.addValue("Transfer-Encoding").setString("chunked");
            }
            else {
                this.getOutputBuffer().addActiveFilter(outputFilters[0]);
            }
        }
        if (useCompression) {
            this.getOutputBuffer().addActiveFilter(outputFilters[3]);
            headers.setValue("Content-Encoding").setString("gzip");
        }
        if (isCompressable) {
            final MessageBytes vary = headers.getValue("Vary");
            if (vary == null) {
                headers.setValue("Vary").setString("Accept-Encoding");
            }
            else if (!vary.equals("*")) {
                headers.setValue("Vary").setString(vary.getString() + ",Accept-Encoding");
            }
        }
        if (headers.getValue("Date") == null) {
            headers.setValue("Date").setString(FastHttpDateFormat.getCurrentDate());
        }
        if (entityBody && !this.contentDelimitation) {
            this.keepAlive = false;
        }
        if (!(this.keepAlive = (this.keepAlive && !this.statusDropsConnection(statusCode)))) {
            if (!connectionClosePresent) {
                headers.addValue("Connection").setString("close");
            }
        }
        else if (!this.http11 && !this.error) {
            headers.addValue("Connection").setString("keep-alive");
        }
        this.getOutputBuffer().sendStatus();
        if (this.server != null) {
            headers.setValue("Server").setString(this.server);
        }
        else if (headers.getValue("Server") == null) {
            this.getOutputBuffer().write(Constants.SERVER_BYTES);
        }
        for (int size = headers.size(), i = 0; i < size; ++i) {
            this.getOutputBuffer().sendHeader(headers.getName(i), headers.getValue(i));
        }
        this.getOutputBuffer().endHeaders();
    }
    
    private boolean isConnectionClose(final MimeHeaders headers) {
        final MessageBytes connection = headers.getValue("Connection");
        return connection != null && connection.equals("close");
    }
    
    abstract boolean prepareSendfile(final OutputFilter[] p0);
    
    protected void parseHost(final MessageBytes valueMB) {
        if (valueMB == null || valueMB.isNull()) {
            this.request.setServerPort(this.endpoint.getPort());
            return;
        }
        final ByteChunk valueBC = valueMB.getByteChunk();
        final byte[] valueB = valueBC.getBytes();
        final int valueL = valueBC.getLength();
        final int valueS = valueBC.getStart();
        int colonPos = -1;
        if (this.hostNameC.length < valueL) {
            this.hostNameC = new char[valueL];
        }
        final boolean ipv6 = valueB[valueS] == 91;
        boolean bracketClosed = false;
        for (int i = 0; i < valueL; ++i) {
            final char b = (char)valueB[i + valueS];
            if ((this.hostNameC[i] = b) == ']') {
                bracketClosed = true;
            }
            else if (b == ':' && (!ipv6 || bracketClosed)) {
                colonPos = i;
                break;
            }
        }
        if (colonPos < 0) {
            if (!this.endpoint.isSSLEnabled()) {
                this.request.setServerPort(80);
            }
            else {
                this.request.setServerPort(443);
            }
            this.request.serverName().setChars(this.hostNameC, 0, valueL);
        }
        else {
            this.request.serverName().setChars(this.hostNameC, 0, colonPos);
            int port = 0;
            int mult = 1;
            for (int j = valueL - 1; j > colonPos; --j) {
                final int charValue = HexUtils.getDec(valueB[j + valueS]);
                if (charValue == -1 || charValue > 9) {
                    this.error = true;
                    this.response.setStatus(400);
                    break;
                }
                port += charValue * mult;
                mult *= 10;
            }
            this.request.setServerPort(port);
        }
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState asyncDispatch(final SocketStatus status) {
        final RequestInfo rp = this.request.getRequestProcessor();
        try {
            rp.setStage(3);
            this.error = !this.adapter.asyncDispatch(this.request, this.response, status);
            this.resetTimeouts();
        }
        catch (InterruptedIOException e) {
            this.error = true;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            this.getLog().error((Object)AbstractHttp11Processor.sm.getString("http11processor.request.process"), t);
            this.error = true;
        }
        finally {
            if (this.error) {
                this.response.setStatus(500);
                this.adapter.log(this.request, this.response, 0L);
            }
        }
        rp.setStage(7);
        if (this.error) {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        if (this.isAsync()) {
            return AbstractEndpoint.Handler.SocketState.LONG;
        }
        if (!this.keepAlive) {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        this.getInputBuffer().nextRequest();
        this.getOutputBuffer().nextRequest();
        return AbstractEndpoint.Handler.SocketState.OPEN;
    }
    
    @Override
    public boolean isComet() {
        return this.comet;
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState upgradeDispatch() throws IOException {
        throw new IOException(AbstractHttp11Processor.sm.getString("TODO"));
    }
    
    @Deprecated
    @Override
    public UpgradeInbound getUpgradeInbound() {
        return this.upgradeInbound;
    }
    
    @Override
    public boolean isUpgrade() {
        return this.httpUpgradeHandler != null;
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState upgradeDispatch(final SocketStatus status) throws IOException {
        throw new IOException(AbstractHttp11Processor.sm.getString("ajpprocessor.httpupgrade.notsupported"));
    }
    
    @Override
    public HttpUpgradeHandler getHttpUpgradeHandler() {
        return this.httpUpgradeHandler;
    }
    
    protected abstract void resetTimeouts();
    
    protected abstract void setCometTimeouts(final SocketWrapper<S> p0);
    
    public void endRequest() {
        try {
            this.getInputBuffer().endRequest();
        }
        catch (IOException e) {
            this.error = true;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            this.getLog().error((Object)AbstractHttp11Processor.sm.getString("http11processor.request.finish"), t);
            this.response.setStatus(500);
            this.error = true;
        }
        try {
            this.getOutputBuffer().endRequest();
        }
        catch (IOException e) {
            this.error = true;
        }
        catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            this.getLog().error((Object)AbstractHttp11Processor.sm.getString("http11processor.response.finish"), t);
            this.error = true;
        }
    }
    
    protected abstract boolean breakKeepAliveLoop(final SocketWrapper<S> p0);
    
    @Override
    public final void recycle(final boolean isSocketClosing) {
        if (this.getInputBuffer() != null) {
            this.getInputBuffer().recycle();
        }
        if (this.getOutputBuffer() != null) {
            this.getOutputBuffer().recycle();
        }
        if (this.asyncStateMachine != null) {
            this.asyncStateMachine.recycle();
        }
        this.upgradeInbound = null;
        this.httpUpgradeHandler = null;
        this.remoteAddr = null;
        this.remoteHost = null;
        this.localAddr = null;
        this.localName = null;
        this.remotePort = -1;
        this.localPort = -1;
        this.comet = false;
        this.recycleInternal();
    }
    
    protected abstract void recycleInternal();
    
    static {
        sm = StringManager.getManager("org.apache.coyote.http11");
    }
}
