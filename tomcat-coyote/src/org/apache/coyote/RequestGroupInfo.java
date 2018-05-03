// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import java.util.ArrayList;

public class RequestGroupInfo
{
    ArrayList<RequestInfo> processors;
    private long deadMaxTime;
    private long deadProcessingTime;
    private int deadRequestCount;
    private int deadErrorCount;
    private long deadBytesReceived;
    private long deadBytesSent;
    
    public RequestGroupInfo() {
        this.processors = new ArrayList<RequestInfo>();
        this.deadMaxTime = 0L;
        this.deadProcessingTime = 0L;
        this.deadRequestCount = 0;
        this.deadErrorCount = 0;
        this.deadBytesReceived = 0L;
        this.deadBytesSent = 0L;
    }
    
    public synchronized void addRequestProcessor(final RequestInfo rp) {
        this.processors.add(rp);
    }
    
    public synchronized void removeRequestProcessor(final RequestInfo rp) {
        if (rp != null) {
            if (this.deadMaxTime < rp.getMaxTime()) {
                this.deadMaxTime = rp.getMaxTime();
            }
            this.deadProcessingTime += rp.getProcessingTime();
            this.deadRequestCount += rp.getRequestCount();
            this.deadErrorCount += rp.getErrorCount();
            this.deadBytesReceived += rp.getBytesReceived();
            this.deadBytesSent += rp.getBytesSent();
            this.processors.remove(rp);
        }
    }
    
    public synchronized long getMaxTime() {
        long maxTime = this.deadMaxTime;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            if (maxTime < rp.getMaxTime()) {
                maxTime = rp.getMaxTime();
            }
        }
        return maxTime;
    }
    
    public synchronized void setMaxTime(final long maxTime) {
        this.deadMaxTime = maxTime;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            rp.setMaxTime(maxTime);
        }
    }
    
    public synchronized long getProcessingTime() {
        long time = this.deadProcessingTime;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            time += rp.getProcessingTime();
        }
        return time;
    }
    
    public synchronized void setProcessingTime(final long totalTime) {
        this.deadProcessingTime = totalTime;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            rp.setProcessingTime(totalTime);
        }
    }
    
    public synchronized int getRequestCount() {
        int requestCount = this.deadRequestCount;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            requestCount += rp.getRequestCount();
        }
        return requestCount;
    }
    
    public synchronized void setRequestCount(final int requestCount) {
        this.deadRequestCount = requestCount;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            rp.setRequestCount(requestCount);
        }
    }
    
    public synchronized int getErrorCount() {
        int requestCount = this.deadErrorCount;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            requestCount += rp.getErrorCount();
        }
        return requestCount;
    }
    
    public synchronized void setErrorCount(final int errorCount) {
        this.deadErrorCount = errorCount;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            rp.setErrorCount(errorCount);
        }
    }
    
    public synchronized long getBytesReceived() {
        long bytes = this.deadBytesReceived;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            bytes += rp.getBytesReceived();
        }
        return bytes;
    }
    
    public synchronized void setBytesReceived(final long bytesReceived) {
        this.deadBytesReceived = bytesReceived;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            rp.setBytesReceived(bytesReceived);
        }
    }
    
    public synchronized long getBytesSent() {
        long bytes = this.deadBytesSent;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            bytes += rp.getBytesSent();
        }
        return bytes;
    }
    
    public synchronized void setBytesSent(final long bytesSent) {
        this.deadBytesSent = bytesSent;
        for (int i = 0; i < this.processors.size(); ++i) {
            final RequestInfo rp = this.processors.get(i);
            rp.setBytesSent(bytesSent);
        }
    }
    
    public void resetCounters() {
        this.setBytesReceived(0L);
        this.setBytesSent(0L);
        this.setRequestCount(0);
        this.setProcessingTime(0L);
        this.setMaxTime(0L);
        this.setErrorCount(0);
    }
}
