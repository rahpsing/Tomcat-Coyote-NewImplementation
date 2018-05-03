// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Poll
{
    public static final int APR_POLLIN = 1;
    public static final int APR_POLLPRI = 2;
    public static final int APR_POLLOUT = 4;
    public static final int APR_POLLERR = 16;
    public static final int APR_POLLHUP = 32;
    public static final int APR_POLLNVAL = 64;
    public static final int APR_POLLSET_THREADSAFE = 1;
    public static final int APR_NO_DESC = 0;
    public static final int APR_POLL_SOCKET = 1;
    public static final int APR_POLL_FILE = 2;
    public static final int APR_POLL_LASTDESC = 3;
    
    public static native long create(final int p0, final long p1, final int p2, final long p3) throws Error;
    
    public static native int destroy(final long p0);
    
    public static native int add(final long p0, final long p1, final int p2);
    
    public static native int addWithTimeout(final long p0, final long p1, final int p2, final long p3);
    
    public static native int remove(final long p0, final long p1);
    
    public static native int poll(final long p0, final long p1, final long[] p2, final boolean p3);
    
    public static native int maintain(final long p0, final long[] p1, final boolean p2);
    
    public static native void setTtl(final long p0, final long p1);
    
    public static native long getTtl(final long p0);
    
    public static native int pollset(final long p0, final long[] p1);
}
