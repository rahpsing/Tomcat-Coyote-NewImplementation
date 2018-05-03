// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Lock
{
    public static final int APR_LOCK_FCNTL = 0;
    public static final int APR_LOCK_FLOCK = 1;
    public static final int APR_LOCK_SYSVSEM = 2;
    public static final int APR_LOCK_PROC_PTHREAD = 3;
    public static final int APR_LOCK_POSIXSEM = 4;
    public static final int APR_LOCK_DEFAULT = 5;
    
    public static native long create(final String p0, final int p1, final long p2) throws Error;
    
    public static native long childInit(final String p0, final long p1) throws Error;
    
    public static native int lock(final long p0);
    
    public static native int trylock(final long p0);
    
    public static native int unlock(final long p0);
    
    public static native int destroy(final long p0);
    
    public static native String lockfile(final long p0);
    
    public static native String name(final long p0);
    
    public static native String defname();
}
