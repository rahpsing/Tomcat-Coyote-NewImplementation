// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Time
{
    public static final long APR_USEC_PER_SEC = 1000000L;
    public static final long APR_MSEC_PER_USEC = 1000L;
    
    public static long sec(final long t) {
        return t / 1000000L;
    }
    
    public static long msec(final long t) {
        return t / 1000L;
    }
    
    public static native long now();
    
    public static native String rfc822(final long p0);
    
    public static native String ctime(final long p0);
    
    public static native void sleep(final long p0);
}
