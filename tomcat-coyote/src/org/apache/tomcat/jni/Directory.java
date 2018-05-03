// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Directory
{
    public static native int make(final String p0, final int p1, final long p2);
    
    public static native int makeRecursive(final String p0, final int p1, final long p2);
    
    public static native int remove(final String p0, final long p1);
    
    public static native String tempGet(final long p0);
    
    public static native long open(final String p0, final long p1) throws Error;
    
    public static native int close(final long p0);
    
    public static native int rewind(final long p0);
    
    public static native int read(final FileInfo p0, final int p1, final long p2);
}
