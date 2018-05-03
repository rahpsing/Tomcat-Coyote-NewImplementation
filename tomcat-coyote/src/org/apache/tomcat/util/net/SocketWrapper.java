// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

public class SocketWrapper<E>
{
    protected volatile E socket;
    protected volatile long lastAccess;
    protected long timeout;
    protected boolean error;
    protected long lastRegistered;
    protected volatile int keepAliveLeft;
    private boolean comet;
    protected boolean async;
    protected boolean keptAlive;
    private boolean upgraded;
    private boolean secure;
    private volatile boolean blockingStatus;
    private final Lock blockingStatusReadLock;
    private final ReentrantReadWriteLock.WriteLock blockingStatusWriteLock;
    private final Object writeThreadLock;
    
    public SocketWrapper(final E socket) {
        this.lastAccess = -1L;
        this.timeout = -1L;
        this.error = false;
        this.lastRegistered = 0L;
        this.keepAliveLeft = 100;
        this.comet = false;
        this.async = false;
        this.keptAlive = false;
        this.upgraded = false;
        this.secure = false;
        this.blockingStatus = true;
        this.writeThreadLock = new Object();
        this.socket = socket;
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.blockingStatusReadLock = lock.readLock();
        this.blockingStatusWriteLock = lock.writeLock();
    }
    
    public E getSocket() {
        return this.socket;
    }
    
    public boolean isComet() {
        return this.comet;
    }
    
    public void setComet(final boolean comet) {
        this.comet = comet;
    }
    
    public boolean isAsync() {
        return this.async;
    }
    
    public void setAsync(final boolean async) {
        this.async = async;
    }
    
    public boolean isUpgraded() {
        return this.upgraded;
    }
    
    public void setUpgraded(final boolean upgraded) {
        this.upgraded = upgraded;
    }
    
    public boolean isSecure() {
        return this.secure;
    }
    
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }
    
    public long getLastAccess() {
        return this.lastAccess;
    }
    
    public void access() {
        this.access(System.currentTimeMillis());
    }
    
    public void access(final long access) {
        this.lastAccess = access;
    }
    
    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }
    
    public long getTimeout() {
        return this.timeout;
    }
    
    public boolean getError() {
        return this.error;
    }
    
    public void setError(final boolean error) {
        this.error = error;
    }
    
    public void setKeepAliveLeft(final int keepAliveLeft) {
        this.keepAliveLeft = keepAliveLeft;
    }
    
    public int decrementKeepAlive() {
        return --this.keepAliveLeft;
    }
    
    public boolean isKeptAlive() {
        return this.keptAlive;
    }
    
    public void setKeptAlive(final boolean keptAlive) {
        this.keptAlive = keptAlive;
    }
    
    public boolean getBlockingStatus() {
        return this.blockingStatus;
    }
    
    public void setBlockingStatus(final boolean blockingStatus) {
        this.blockingStatus = blockingStatus;
    }
    
    public Lock getBlockingStatusReadLock() {
        return this.blockingStatusReadLock;
    }
    
    public ReentrantReadWriteLock.WriteLock getBlockingStatusWriteLock() {
        return this.blockingStatusWriteLock;
    }
    
    public Object getWriteThreadLock() {
        return this.writeThreadLock;
    }
    
    public void reset(final E socket, final long timeout) {
        this.async = false;
        this.blockingStatus = true;
        this.comet = false;
        this.error = false;
        this.keepAliveLeft = 100;
        this.lastAccess = System.currentTimeMillis();
        this.socket = socket;
        this.timeout = timeout;
        this.upgraded = false;
    }
}
