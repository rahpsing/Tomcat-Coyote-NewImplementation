// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

import java.nio.ByteBuffer;

public class Socket
{
    public static final int SOCK_STREAM = 0;
    public static final int SOCK_DGRAM = 1;
    public static final int APR_SO_LINGER = 1;
    public static final int APR_SO_KEEPALIVE = 2;
    public static final int APR_SO_DEBUG = 4;
    public static final int APR_SO_NONBLOCK = 8;
    public static final int APR_SO_REUSEADDR = 16;
    public static final int APR_SO_SNDBUF = 64;
    public static final int APR_SO_RCVBUF = 128;
    public static final int APR_SO_DISCONNECTED = 256;
    public static final int APR_TCP_NODELAY = 512;
    public static final int APR_TCP_NOPUSH = 1024;
    public static final int APR_RESET_NODELAY = 2048;
    public static final int APR_INCOMPLETE_READ = 4096;
    public static final int APR_INCOMPLETE_WRITE = 8192;
    public static final int APR_IPV6_V6ONLY = 16384;
    public static final int APR_TCP_DEFER_ACCEPT = 32768;
    public static final int APR_SHUTDOWN_READ = 0;
    public static final int APR_SHUTDOWN_WRITE = 1;
    public static final int APR_SHUTDOWN_READWRITE = 2;
    public static final int APR_IPV4_ADDR_OK = 1;
    public static final int APR_IPV6_ADDR_OK = 2;
    public static final int APR_UNSPEC = 0;
    public static final int APR_INET = 1;
    public static final int APR_INET6 = 2;
    public static final int APR_PROTO_TCP = 6;
    public static final int APR_PROTO_UDP = 17;
    public static final int APR_PROTO_SCTP = 132;
    public static final int APR_LOCAL = 0;
    public static final int APR_REMOTE = 1;
    public static final int SOCKET_GET_POOL = 0;
    public static final int SOCKET_GET_IMPL = 1;
    public static final int SOCKET_GET_APRS = 2;
    public static final int SOCKET_GET_TYPE = 3;
    
    public static native long create(final int p0, final int p1, final int p2, final long p3) throws Exception;
    
    public static native int shutdown(final long p0, final int p1);
    
    public static native int close(final long p0);
    
    public static native void destroy(final long p0);
    
    public static native int bind(final long p0, final long p1);
    
    public static native int listen(final long p0, final int p1);
    
    public static native long acceptx(final long p0, final long p1) throws Exception;
    
    public static native long accept(final long p0) throws Exception;
    
    public static native int acceptfilter(final long p0, final String p1, final String p2);
    
    public static native boolean atmark(final long p0);
    
    public static native int connect(final long p0, final long p1);
    
    public static native int send(final long p0, final byte[] p1, final int p2, final int p3);
    
    public static native int sendb(final long p0, final ByteBuffer p1, final int p2, final int p3);
    
    public static native int sendib(final long p0, final ByteBuffer p1, final int p2, final int p3);
    
    public static native int sendbb(final long p0, final int p1, final int p2);
    
    public static native int sendibb(final long p0, final int p1, final int p2);
    
    public static native int sendv(final long p0, final byte[][] p1);
    
    public static native int sendto(final long p0, final long p1, final int p2, final byte[] p3, final int p4, final int p5);
    
    public static native int recv(final long p0, final byte[] p1, final int p2, final int p3);
    
    public static native int recvt(final long p0, final byte[] p1, final int p2, final int p3, final long p4);
    
    public static native int recvb(final long p0, final ByteBuffer p1, final int p2, final int p3);
    
    public static native int recvbb(final long p0, final int p1, final int p2);
    
    public static native int recvbt(final long p0, final ByteBuffer p1, final int p2, final int p3, final long p4);
    
    public static native int recvbbt(final long p0, final int p1, final int p2, final long p3);
    
    public static native int recvfrom(final long p0, final long p1, final int p2, final byte[] p3, final int p4, final int p5);
    
    public static native int optSet(final long p0, final int p1, final int p2);
    
    public static native int optGet(final long p0, final int p1) throws Exception;
    
    public static native int timeoutSet(final long p0, final long p1);
    
    public static native long timeoutGet(final long p0) throws Exception;
    
    public static native long sendfile(final long p0, final long p1, final byte[][] p2, final byte[][] p3, final long p4, final long p5, final int p6);
    
    public static native long sendfilen(final long p0, final long p1, final long p2, final long p3, final int p4);
    
    public static native long pool(final long p0) throws Exception;
    
    private static native long get(final long p0, final int p1);
    
    public static native void setsbb(final long p0, final ByteBuffer p1);
    
    public static native void setrbb(final long p0, final ByteBuffer p1);
    
    public static native int dataSet(final long p0, final String p1, final Object p2);
    
    public static native Object dataGet(final long p0, final String p1);
}
