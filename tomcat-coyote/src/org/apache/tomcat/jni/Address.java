// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Address
{
    public static final String APR_ANYADDR = "0.0.0.0";
    
    public static native boolean fill(final Sockaddr p0, final long p1);
    
    public static native Sockaddr getInfo(final long p0);
    
    public static native long info(final String p0, final int p1, final int p2, final int p3, final long p4) throws Exception;
    
    public static native String getnameinfo(final long p0, final int p1);
    
    public static native String getip(final long p0);
    
    public static native int getservbyname(final long p0, final String p1);
    
    public static native long get(final int p0, final long p1) throws Exception;
    
    public static native boolean equal(final long p0, final long p1);
}
