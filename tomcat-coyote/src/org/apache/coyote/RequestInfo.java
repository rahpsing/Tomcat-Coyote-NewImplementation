// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import javax.management.ObjectName;

public class RequestInfo
{
    RequestGroupInfo global;
    Request req;
    int stage;
    String workerThreadName;
    ObjectName rpName;
    private long bytesSent;
    private long bytesReceived;
    private long processingTime;
    private long maxTime;
    private String maxRequestUri;
    private int requestCount;
    private int errorCount;
    private long lastRequestProcessingTime;
    
    public RequestInfo(final Request req) {
        this.global = null;
        this.stage = 0;
        this.lastRequestProcessingTime = 0L;
        this.req = req;
    }
    
    public RequestGroupInfo getGlobalProcessor() {
        return this.global;
    }
    
    public void setGlobalProcessor(final RequestGroupInfo global) {
        if (global != null) {
            (this.global = global).addRequestProcessor(this);
        }
        else if (this.global != null) {
            this.global.removeRequestProcessor(this);
            this.global = null;
        }
    }
    
    public String getMethod() {
        return this.req.method().toString();
    }
    
    public String getCurrentUri() {
        return this.req.requestURI().toString();
    }
    
    public String getCurrentQueryString() {
        return this.req.queryString().toString();
    }
    
    public String getProtocol() {
        return this.req.protocol().toString();
    }
    
    public String getVirtualHost() {
        return this.req.serverName().toString();
    }
    
    public int getServerPort() {
        return this.req.getServerPort();
    }
    
    public String getRemoteAddr() {
        this.req.action(ActionCode.REQ_HOST_ADDR_ATTRIBUTE, null);
        return this.req.remoteAddr().toString();
    }
    
    public int getContentLength() {
        return this.req.getContentLength();
    }
    
    public long getRequestBytesReceived() {
        return this.req.getBytesRead();
    }
    
    public long getRequestBytesSent() {
        return this.req.getResponse().getContentWritten();
    }
    
    public long getRequestProcessingTime() {
        if (this.getStage() == 7) {
            return 0L;
        }
        return System.currentTimeMillis() - this.req.getStartTime();
    }
    
    void updateCounters() {
        this.bytesReceived += this.req.getBytesRead();
        this.bytesSent += this.req.getResponse().getContentWritten();
        ++this.requestCount;
        if (this.req.getResponse().getStatus() >= 400) {
            ++this.errorCount;
        }
        final long t0 = this.req.getStartTime();
        final long t2 = System.currentTimeMillis();
        final long time = t2 - t0;
        this.lastRequestProcessingTime = time;
        this.processingTime += time;
        if (this.maxTime < time) {
            this.maxTime = time;
            this.maxRequestUri = this.req.requestURI().toString();
        }
    }
    
    public int getStage() {
        return this.stage;
    }
    
    public void setStage(final int stage) {
        this.stage = stage;
    }
    
    public long getBytesSent() {
        return this.bytesSent;
    }
    
    public void setBytesSent(final long bytesSent) {
        this.bytesSent = bytesSent;
    }
    
    public long getBytesReceived() {
        return this.bytesReceived;
    }
    
    public void setBytesReceived(final long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }
    
    public long getProcessingTime() {
        return this.processingTime;
    }
    
    public void setProcessingTime(final long processingTime) {
        this.processingTime = processingTime;
    }
    
    public long getMaxTime() {
        return this.maxTime;
    }
    
    public void setMaxTime(final long maxTime) {
        this.maxTime = maxTime;
    }
    
    public String getMaxRequestUri() {
        return this.maxRequestUri;
    }
    
    public void setMaxRequestUri(final String maxRequestUri) {
        this.maxRequestUri = maxRequestUri;
    }
    
    public int getRequestCount() {
        return this.requestCount;
    }
    
    public void setRequestCount(final int requestCount) {
        this.requestCount = requestCount;
    }
    
    public int getErrorCount() {
        return this.errorCount;
    }
    
    public void setErrorCount(final int errorCount) {
        this.errorCount = errorCount;
    }
    
    public String getWorkerThreadName() {
        return this.workerThreadName;
    }
    
    public ObjectName getRpName() {
        return this.rpName;
    }
    
    public long getLastRequestProcessingTime() {
        return this.lastRequestProcessingTime;
    }
    
    public void setWorkerThreadName(final String workerThreadName) {
        this.workerThreadName = workerThreadName;
    }
    
    public void setRpName(final ObjectName rpName) {
        this.rpName = rpName;
    }
    
    public void setLastRequestProcessingTime(final long lastRequestProcessingTime) {
        this.lastRequestProcessingTime = lastRequestProcessingTime;
    }
}
