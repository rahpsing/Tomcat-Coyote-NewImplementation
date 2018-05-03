// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

import java.nio.ByteBuffer;

public class Shm
{
    public static native long create(final long p0, final String p1, final long p2) throws Error;
    
    public static native int remove(final String p0, final long p1);
    
    public static native int destroy(final long p0);
    
    public static native long attach(final String p0, final long p1) throws Error;
    
    public static native int detach(final long p0);
    
    public static native long baseaddr(final long p0);
    
    public static native long size(final long p0);
    
    public static native ByteBuffer buffer(final long p0);
}
