// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

import java.nio.ByteBuffer;

public class Pool
{
    public static native long create(final long p0);
    
    public static native void clear(final long p0);
    
    public static native void destroy(final long p0);
    
    public static native long parentGet(final long p0);
    
    public static native boolean isAncestor(final long p0, final long p1);
    
    public static native long cleanupRegister(final long p0, final Object p1);
    
    public static native void cleanupKill(final long p0, final long p1);
    
    public static native void noteSubprocess(final long p0, final long p1, final int p2);
    
    public static native ByteBuffer alloc(final long p0, final int p1);
    
    public static native ByteBuffer calloc(final long p0, final int p1);
    
    public static native int dataSet(final long p0, final String p1, final Object p2);
    
    public static native Object dataGet(final long p0, final String p1);
    
    public static native void cleanupForExec();
}
