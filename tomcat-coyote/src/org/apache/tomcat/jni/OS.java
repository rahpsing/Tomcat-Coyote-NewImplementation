// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class OS
{
    private static final int UNIX = 1;
    private static final int NETWARE = 2;
    private static final int WIN32 = 3;
    private static final int WIN64 = 4;
    private static final int LINUX = 5;
    private static final int SOLARIS = 6;
    private static final int BSD = 7;
    private static final int MACOSX = 8;
    public static final int LOG_EMERG = 1;
    public static final int LOG_ERROR = 2;
    public static final int LOG_NOTICE = 3;
    public static final int LOG_WARN = 4;
    public static final int LOG_INFO = 5;
    public static final int LOG_DEBUG = 6;
    public static final boolean IS_UNIX;
    public static final boolean IS_NETWARE;
    public static final boolean IS_WIN32;
    public static final boolean IS_WIN64;
    public static final boolean IS_LINUX;
    public static final boolean IS_SOLARIS;
    public static final boolean IS_BSD;
    public static final boolean IS_MACOSX;
    
    private static native boolean is(final int p0);
    
    public static native String defaultEncoding(final long p0);
    
    public static native String localeEncoding(final long p0);
    
    public static native int random(final byte[] p0, final int p1);
    
    public static native int info(final long[] p0);
    
    public static native String expand(final String p0);
    
    public static native void sysloginit(final String p0);
    
    public static native void syslog(final int p0, final String p1);
    
    static {
        IS_UNIX = is(1);
        IS_NETWARE = is(2);
        IS_WIN32 = is(3);
        IS_WIN64 = is(4);
        IS_LINUX = is(5);
        IS_SOLARIS = is(6);
        IS_BSD = is(7);
        IS_MACOSX = is(8);
    }
}
