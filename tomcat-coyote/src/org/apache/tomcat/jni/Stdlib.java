// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Stdlib
{
    public static native boolean memread(final byte[] p0, final long p1, final int p2);
    
    public static native boolean memwrite(final long p0, final byte[] p1, final int p2);
    
    public static native boolean memset(final long p0, final int p1, final int p2);
    
    public static native long malloc(final int p0);
    
    public static native long realloc(final long p0, final int p1);
    
    public static native long calloc(final int p0, final int p1);
    
    public static native void free(final long p0);
    
    public static native int getpid();
    
    public static native int getppid();
}
