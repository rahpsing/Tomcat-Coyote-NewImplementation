// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Local
{
    public static native long create(final String p0, final long p1) throws Exception;
    
    public static native int bind(final long p0, final long p1);
    
    public static native int listen(final long p0, final int p1);
    
    public static native long accept(final long p0) throws Exception;
    
    public static native int connect(final long p0, final long p1);
}
