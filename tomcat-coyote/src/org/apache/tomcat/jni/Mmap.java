// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Mmap
{
    public static final int APR_MMAP_READ = 1;
    public static final int APR_MMAP_WRITE = 2;
    
    public static native long create(final long p0, final long p1, final long p2, final int p3, final long p4) throws Error;
    
    public static native long dup(final long p0, final long p1) throws Error;
    
    public static native int delete(final long p0);
    
    public static native long offset(final long p0, final long p1) throws Error;
}
