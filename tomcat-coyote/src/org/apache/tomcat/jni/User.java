// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public class User
{
    public static native long uidCurrent(final long p0) throws Error;
    
    public static native long gidCurrent(final long p0) throws Error;
    
    public static native long uid(final String p0, final long p1) throws Error;
    
    public static native long usergid(final String p0, final long p1) throws Error;
    
    public static native long gid(final String p0, final long p1) throws Error;
    
    public static native String username(final long p0, final long p1) throws Error;
    
    public static native String groupname(final long p0, final long p1) throws Error;
    
    public static native int uidcompare(final long p0, final long p1);
    
    public static native int gidcompare(final long p0, final long p1);
    
    public static native String homepath(final String p0, final long p1) throws Error;
}
