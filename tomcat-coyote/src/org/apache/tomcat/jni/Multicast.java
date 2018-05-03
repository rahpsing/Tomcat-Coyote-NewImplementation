// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Multicast
{
    public static native int join(final long p0, final long p1, final long p2, final long p3);
    
    public static native int leave(final long p0, final long p1, final long p2, final long p3);
    
    public static native int hops(final long p0, final int p1);
    
    public static native int loopback(final long p0, final boolean p1);
    
    public static native int ointerface(final long p0, final long p1);
}
