// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

import java.io.File;

public final class Library
{
    private static String[] NAMES;
    private static Library _instance;
    public static int TCN_MAJOR_VERSION;
    public static int TCN_MINOR_VERSION;
    public static int TCN_PATCH_VERSION;
    public static int TCN_IS_DEV_VERSION;
    public static int APR_MAJOR_VERSION;
    public static int APR_MINOR_VERSION;
    public static int APR_PATCH_VERSION;
    public static int APR_IS_DEV_VERSION;
    public static boolean APR_HAVE_IPV6;
    public static boolean APR_HAS_SHARED_MEMORY;
    public static boolean APR_HAS_THREADS;
    public static boolean APR_HAS_SENDFILE;
    public static boolean APR_HAS_MMAP;
    public static boolean APR_HAS_FORK;
    public static boolean APR_HAS_RANDOM;
    public static boolean APR_HAS_OTHER_CHILD;
    public static boolean APR_HAS_DSO;
    public static boolean APR_HAS_SO_ACCEPTFILTER;
    public static boolean APR_HAS_UNICODE_FS;
    public static boolean APR_HAS_PROC_INVOKED;
    public static boolean APR_HAS_USER;
    public static boolean APR_HAS_LARGE_FILES;
    public static boolean APR_HAS_XTHREAD_FILES;
    public static boolean APR_HAS_OS_UUID;
    public static boolean APR_IS_BIGENDIAN;
    public static boolean APR_FILES_AS_SOCKETS;
    public static boolean APR_CHARSET_EBCDIC;
    public static boolean APR_TCP_NODELAY_INHERITED;
    public static boolean APR_O_NONBLOCK_INHERITED;
    public static int APR_SIZEOF_VOIDP;
    public static int APR_PATH_MAX;
    public static int APRMAXHOSTLEN;
    public static int APR_MAX_IOVEC_SIZE;
    public static int APR_MAX_SECS_TO_LINGER;
    public static int APR_MMAP_THRESHOLD;
    public static int APR_MMAP_LIMIT;
    
    private Library() throws Exception {
        boolean loaded = false;
        final StringBuilder err = new StringBuilder();
        for (int i = 0; i < Library.NAMES.length; ++i) {
            try {
                System.loadLibrary(Library.NAMES[i]);
                loaded = true;
            }
            catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath)t;
                }
                if (t instanceof VirtualMachineError) {
                    throw (VirtualMachineError)t;
                }
                final String name = System.mapLibraryName(Library.NAMES[i]);
                final String path = System.getProperty("java.library.path");
                final String sep = System.getProperty("path.separator");
                final String[] paths = path.split(sep);
                for (int j = 0; j < paths.length; ++j) {
                    final File fd = new File(paths[j], name);
                    if (fd.exists()) {
                        t.printStackTrace();
                    }
                }
                if (i > 0) {
                    err.append(", ");
                }
                err.append(t.getMessage());
            }
            if (loaded) {
                break;
            }
        }
        if (!loaded) {
            err.append('(');
            err.append(System.getProperty("java.library.path"));
            err.append(')');
            throw new UnsatisfiedLinkError(err.toString());
        }
    }
    
    private Library(final String libraryName) {
        System.loadLibrary(libraryName);
    }
    
    private static native boolean initialize();
    
    public static native void terminate();
    
    private static native boolean has(final int p0);
    
    private static native int version(final int p0);
    
    private static native int size(final int p0);
    
    public static native String versionString();
    
    public static native String aprVersionString();
    
    public static native long globalPool();
    
    public static boolean initialize(final String libraryName) throws Exception {
        if (Library._instance == null) {
            if (libraryName == null) {
                Library._instance = new Library();
            }
            else {
                Library._instance = new Library(libraryName);
            }
            Library.TCN_MAJOR_VERSION = version(1);
            Library.TCN_MINOR_VERSION = version(2);
            Library.TCN_PATCH_VERSION = version(3);
            Library.TCN_IS_DEV_VERSION = version(4);
            Library.APR_MAJOR_VERSION = version(17);
            Library.APR_MINOR_VERSION = version(18);
            Library.APR_PATCH_VERSION = version(19);
            Library.APR_IS_DEV_VERSION = version(20);
            Library.APR_SIZEOF_VOIDP = size(1);
            Library.APR_PATH_MAX = size(2);
            Library.APRMAXHOSTLEN = size(3);
            Library.APR_MAX_IOVEC_SIZE = size(4);
            Library.APR_MAX_SECS_TO_LINGER = size(5);
            Library.APR_MMAP_THRESHOLD = size(6);
            Library.APR_MMAP_LIMIT = size(7);
            Library.APR_HAVE_IPV6 = has(0);
            Library.APR_HAS_SHARED_MEMORY = has(1);
            Library.APR_HAS_THREADS = has(2);
            Library.APR_HAS_SENDFILE = has(3);
            Library.APR_HAS_MMAP = has(4);
            Library.APR_HAS_FORK = has(5);
            Library.APR_HAS_RANDOM = has(6);
            Library.APR_HAS_OTHER_CHILD = has(7);
            Library.APR_HAS_DSO = has(8);
            Library.APR_HAS_SO_ACCEPTFILTER = has(9);
            Library.APR_HAS_UNICODE_FS = has(10);
            Library.APR_HAS_PROC_INVOKED = has(11);
            Library.APR_HAS_USER = has(12);
            Library.APR_HAS_LARGE_FILES = has(13);
            Library.APR_HAS_XTHREAD_FILES = has(14);
            Library.APR_HAS_OS_UUID = has(15);
            Library.APR_IS_BIGENDIAN = has(16);
            Library.APR_FILES_AS_SOCKETS = has(17);
            Library.APR_CHARSET_EBCDIC = has(18);
            Library.APR_TCP_NODELAY_INHERITED = has(19);
            Library.APR_O_NONBLOCK_INHERITED = has(20);
            if (Library.APR_MAJOR_VERSION < 1) {
                throw new UnsatisfiedLinkError("Unsupported APR Version (" + aprVersionString() + ")");
            }
            if (!Library.APR_HAS_THREADS) {
                throw new UnsatisfiedLinkError("Missing APR_HAS_THREADS");
            }
        }
        return initialize();
    }
    
    static {
        Library.NAMES = new String[] { "tcnative-1", "libtcnative-1" };
        Library._instance = null;
        Library.TCN_MAJOR_VERSION = 0;
        Library.TCN_MINOR_VERSION = 0;
        Library.TCN_PATCH_VERSION = 0;
        Library.TCN_IS_DEV_VERSION = 0;
        Library.APR_MAJOR_VERSION = 0;
        Library.APR_MINOR_VERSION = 0;
        Library.APR_PATCH_VERSION = 0;
        Library.APR_IS_DEV_VERSION = 0;
        Library.APR_HAVE_IPV6 = false;
        Library.APR_HAS_SHARED_MEMORY = false;
        Library.APR_HAS_THREADS = false;
        Library.APR_HAS_SENDFILE = false;
        Library.APR_HAS_MMAP = false;
        Library.APR_HAS_FORK = false;
        Library.APR_HAS_RANDOM = false;
        Library.APR_HAS_OTHER_CHILD = false;
        Library.APR_HAS_DSO = false;
        Library.APR_HAS_SO_ACCEPTFILTER = false;
        Library.APR_HAS_UNICODE_FS = false;
        Library.APR_HAS_PROC_INVOKED = false;
        Library.APR_HAS_USER = false;
        Library.APR_HAS_LARGE_FILES = false;
        Library.APR_HAS_XTHREAD_FILES = false;
        Library.APR_HAS_OS_UUID = false;
        Library.APR_IS_BIGENDIAN = false;
        Library.APR_FILES_AS_SOCKETS = false;
        Library.APR_CHARSET_EBCDIC = false;
        Library.APR_TCP_NODELAY_INHERITED = false;
        Library.APR_O_NONBLOCK_INHERITED = false;
    }
}
