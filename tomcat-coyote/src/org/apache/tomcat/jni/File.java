// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

import java.nio.ByteBuffer;

public class File
{
    public static final int APR_FOPEN_READ = 1;
    public static final int APR_FOPEN_WRITE = 2;
    public static final int APR_FOPEN_CREATE = 4;
    public static final int APR_FOPEN_APPEND = 8;
    public static final int APR_FOPEN_TRUNCATE = 16;
    public static final int APR_FOPEN_BINARY = 32;
    public static final int APR_FOPEN_EXCL = 64;
    public static final int APR_FOPEN_BUFFERED = 128;
    public static final int APR_FOPEN_DELONCLOSE = 256;
    public static final int APR_FOPEN_XTHREAD = 512;
    public static final int APR_FOPEN_SHARELOCK = 1024;
    public static final int APR_FOPEN_NOCLEANUP = 2048;
    public static final int APR_FOPEN_SENDFILE_ENABLED = 4096;
    public static final int APR_FOPEN_LARGEFILE = 16384;
    public static final int APR_SET = 0;
    public static final int APR_CUR = 1;
    public static final int APR_END = 2;
    public static final int APR_FILE_ATTR_READONLY = 1;
    public static final int APR_FILE_ATTR_EXECUTABLE = 2;
    public static final int APR_FILE_ATTR_HIDDEN = 4;
    public static final int APR_FLOCK_SHARED = 1;
    public static final int APR_FLOCK_EXCLUSIVE = 2;
    public static final int APR_FLOCK_TYPEMASK = 15;
    public static final int APR_FLOCK_NONBLOCK = 16;
    public static final int APR_NOFILE = 0;
    public static final int APR_REG = 1;
    public static final int APR_DIR = 2;
    public static final int APR_CHR = 3;
    public static final int APR_BLK = 4;
    public static final int APR_PIPE = 5;
    public static final int APR_LNK = 6;
    public static final int APR_SOCK = 7;
    public static final int APR_UNKFILE = 127;
    public static final int APR_FPROT_USETID = 32768;
    public static final int APR_FPROT_UREAD = 1024;
    public static final int APR_FPROT_UWRITE = 512;
    public static final int APR_FPROT_UEXECUTE = 256;
    public static final int APR_FPROT_GSETID = 16384;
    public static final int APR_FPROT_GREAD = 64;
    public static final int APR_FPROT_GWRITE = 32;
    public static final int APR_FPROT_GEXECUTE = 16;
    public static final int APR_FPROT_WSTICKY = 8192;
    public static final int APR_FPROT_WREAD = 4;
    public static final int APR_FPROT_WWRITE = 2;
    public static final int APR_FPROT_WEXECUTE = 1;
    public static final int APR_FPROT_OS_DEFAULT = 4095;
    public static final int APR_FINFO_LINK = 1;
    public static final int APR_FINFO_MTIME = 16;
    public static final int APR_FINFO_CTIME = 32;
    public static final int APR_FINFO_ATIME = 64;
    public static final int APR_FINFO_SIZE = 256;
    public static final int APR_FINFO_CSIZE = 512;
    public static final int APR_FINFO_DEV = 4096;
    public static final int APR_FINFO_INODE = 8192;
    public static final int APR_FINFO_NLINK = 16384;
    public static final int APR_FINFO_TYPE = 32768;
    public static final int APR_FINFO_USER = 65536;
    public static final int APR_FINFO_GROUP = 131072;
    public static final int APR_FINFO_UPROT = 1048576;
    public static final int APR_FINFO_GPROT = 2097152;
    public static final int APR_FINFO_WPROT = 4194304;
    public static final int APR_FINFO_ICASE = 16777216;
    public static final int APR_FINFO_NAME = 33554432;
    public static final int APR_FINFO_MIN = 33136;
    public static final int APR_FINFO_IDENT = 12288;
    public static final int APR_FINFO_OWNER = 196608;
    public static final int APR_FINFO_PROT = 7340032;
    public static final int APR_FINFO_NORM = 7582064;
    public static final int APR_FINFO_DIRENT = 33554432;
    
    public static native long open(final String p0, final int p1, final int p2, final long p3) throws Error;
    
    public static native int close(final long p0);
    
    public static native int flush(final long p0);
    
    public static native long mktemp(final String p0, final int p1, final long p2) throws Error;
    
    public static native int remove(final String p0, final long p1);
    
    public static native int rename(final String p0, final String p1, final long p2);
    
    public static native int copy(final String p0, final String p1, final int p2, final long p3);
    
    public static native int append(final String p0, final String p1, final int p2, final long p3);
    
    public static native int puts(final byte[] p0, final long p1);
    
    public static native long seek(final long p0, final int p1, final long p2) throws Error;
    
    public static native int putc(final byte p0, final long p1);
    
    public static native int ungetc(final byte p0, final long p1);
    
    public static native int write(final long p0, final byte[] p1, final int p2, final int p3);
    
    public static native int writeb(final long p0, final ByteBuffer p1, final int p2, final int p3);
    
    public static native int writeFull(final long p0, final byte[] p1, final int p2, final int p3);
    
    public static native int writeFullb(final long p0, final ByteBuffer p1, final int p2, final int p3);
    
    public static native int writev(final long p0, final byte[][] p1);
    
    public static native int writevFull(final long p0, final byte[][] p1);
    
    public static native int read(final long p0, final byte[] p1, final int p2, final int p3);
    
    public static native int readb(final long p0, final ByteBuffer p1, final int p2, final int p3);
    
    public static native int readFull(final long p0, final byte[] p1, final int p2, final int p3);
    
    public static native int readFullb(final long p0, final ByteBuffer p1, final int p2, final int p3);
    
    public static native int gets(final byte[] p0, final int p1, final long p2);
    
    public static native int getc(final long p0) throws Error;
    
    public static native int eof(final long p0);
    
    public static native String nameGet(final long p0);
    
    public static native int permsSet(final String p0, final int p1);
    
    public static native int attrsSet(final String p0, final int p1, final int p2, final long p3);
    
    public static native int mtimeSet(final String p0, final long p1, final long p2);
    
    public static native int lock(final long p0, final int p1);
    
    public static native int unlock(final long p0);
    
    public static native int flagsGet(final long p0);
    
    public static native int trunc(final long p0, final long p1);
    
    public static native int pipeCreate(final long[] p0, final long p1);
    
    public static native long pipeTimeoutGet(final long p0) throws Error;
    
    public static native int pipeTimeoutSet(final long p0, final long p1);
    
    public static native long dup(final long p0, final long p1, final long p2) throws Error;
    
    public static native int dup2(final long p0, final long p1, final long p2);
    
    public static native int stat(final FileInfo p0, final String p1, final int p2, final long p3);
    
    public static native FileInfo getStat(final String p0, final int p1, final long p2);
    
    public static native int infoGet(final FileInfo p0, final int p1, final long p2);
    
    public static native FileInfo getInfo(final int p0, final long p1);
}
