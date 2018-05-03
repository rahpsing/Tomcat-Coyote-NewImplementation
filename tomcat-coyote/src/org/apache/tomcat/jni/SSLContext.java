// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public final class SSLContext
{
    public static native long make(final long p0, final int p1, final int p2) throws Exception;
    
    public static native int free(final long p0);
    
    public static native void setContextId(final long p0, final String p1);
    
    public static native void setBIO(final long p0, final long p1, final int p2);
    
    public static native void setOptions(final long p0, final int p1);
    
    public static native void setQuietShutdown(final long p0, final boolean p1);
    
    public static native boolean setCipherSuite(final long p0, final String p1) throws Exception;
    
    public static native boolean setCARevocation(final long p0, final String p1, final String p2) throws Exception;
    
    public static native boolean setCertificateChainFile(final long p0, final String p1, final boolean p2);
    
    public static native boolean setCertificate(final long p0, final String p1, final String p2, final String p3, final int p4) throws Exception;
    
    public static native boolean setCACertificate(final long p0, final String p1, final String p2) throws Exception;
    
    public static native void setRandom(final long p0, final String p1);
    
    public static native void setShutdownType(final long p0, final int p1);
    
    public static native void setVerify(final long p0, final int p1, final int p2);
}
