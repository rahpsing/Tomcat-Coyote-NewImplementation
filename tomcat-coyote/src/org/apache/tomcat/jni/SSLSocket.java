// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class SSLSocket
{
    public static native int attach(final long p0, final long p1) throws Exception;
    
    public static native int handshake(final long p0);
    
    public static native int renegotiate(final long p0);
    
    public static native void setVerify(final long p0, final int p1, final int p2);
    
    public static native byte[] getInfoB(final long p0, final int p1) throws Exception;
    
    public static native String getInfoS(final long p0, final int p1) throws Exception;
    
    public static native int getInfoI(final long p0, final int p1) throws Exception;
}
