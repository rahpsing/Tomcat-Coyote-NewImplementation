// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class Registry
{
    public static final int HKEY_CLASSES_ROOT = 1;
    public static final int HKEY_CURRENT_CONFIG = 2;
    public static final int HKEY_CURRENT_USER = 3;
    public static final int HKEY_LOCAL_MACHINE = 4;
    public static final int HKEY_USERS = 5;
    public static final int KEY_ALL_ACCESS = 1;
    public static final int KEY_CREATE_LINK = 2;
    public static final int KEY_CREATE_SUB_KEY = 4;
    public static final int KEY_ENUMERATE_SUB_KEYS = 8;
    public static final int KEY_EXECUTE = 16;
    public static final int KEY_NOTIFY = 32;
    public static final int KEY_QUERY_VALUE = 64;
    public static final int KEY_READ = 128;
    public static final int KEY_SET_VALUE = 256;
    public static final int KEY_WOW64_64KEY = 512;
    public static final int KEY_WOW64_32KEY = 1024;
    public static final int KEY_WRITE = 2048;
    public static final int REG_BINARY = 1;
    public static final int REG_DWORD = 2;
    public static final int REG_EXPAND_SZ = 3;
    public static final int REG_MULTI_SZ = 4;
    public static final int REG_QWORD = 5;
    public static final int REG_SZ = 6;
    
    public static native long create(final int p0, final String p1, final int p2, final long p3) throws Error;
    
    public static native long open(final int p0, final String p1, final int p2, final long p3) throws Error;
    
    public static native int close(final long p0);
    
    public static native int getType(final long p0, final String p1);
    
    public static native int getValueI(final long p0, final String p1) throws Error;
    
    public static native long getValueJ(final long p0, final String p1) throws Error;
    
    public static native int getSize(final long p0, final String p1);
    
    public static native String getValueS(final long p0, final String p1) throws Error;
    
    public static native String[] getValueA(final long p0, final String p1) throws Error;
    
    public static native byte[] getValueB(final long p0, final String p1) throws Error;
    
    public static native int setValueI(final long p0, final String p1, final int p2);
    
    public static native int setValueJ(final long p0, final String p1, final long p2);
    
    public static native int setValueS(final long p0, final String p1, final String p2);
    
    public static native int setValueE(final long p0, final String p1, final String p2);
    
    public static native int setValueA(final long p0, final String p1, final String[] p2);
    
    public static native int setValueB(final long p0, final String p1, final byte[] p2);
    
    public static native String[] enumKeys(final long p0) throws Error;
    
    public static native String[] enumValues(final long p0) throws Error;
    
    public static native int deleteValue(final long p0, final String p1);
    
    public static native int deleteKey(final int p0, final String p1, final boolean p2);
}
