// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Procattr
{
    public static native long create(final long p0) throws Error;
    
    public static native int ioSet(final long p0, final int p1, final int p2, final int p3);
    
    public static native int childInSet(final long p0, final long p1, final long p2);
    
    public static native int childOutSet(final long p0, final long p1, final long p2);
    
    public static native int childErrSet(final long p0, final long p1, final long p2);
    
    public static native int dirSet(final long p0, final String p1);
    
    public static native int cmdtypeSet(final long p0, final int p1);
    
    public static native int detachSet(final long p0, final int p1);
    
    public static native int errorCheckSet(final long p0, final int p1);
    
    public static native int addrspaceSet(final long p0, final int p1);
    
    public static native void errfnSet(final long p0, final long p1, final Object p2);
    
    public static native int userSet(final long p0, final String p1, final String p2);
    
    public static native int groupSet(final long p0, final String p1);
}
