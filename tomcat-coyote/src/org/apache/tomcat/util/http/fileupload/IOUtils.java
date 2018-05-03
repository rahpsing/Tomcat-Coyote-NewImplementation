// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

public class IOUtils
{
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    
    public static int copy(final InputStream input, final OutputStream output) throws IOException {
        final long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int)count;
    }
    
    public static long copyLarge(final InputStream input, final OutputStream output) throws IOException {
        final byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
