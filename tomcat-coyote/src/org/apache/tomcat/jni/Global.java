// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Global
{
    public static native long create(final String p0, final int p1, final long p2) throws Error;
    
    public static native long childInit(final String p0, final long p1) throws Error;
    
    public static native int lock(final long p0);
    
    public static native int trylock(final long p0);
    
    public static native int unlock(final long p0);
    
    public static native int destroy(final long p0);
}
