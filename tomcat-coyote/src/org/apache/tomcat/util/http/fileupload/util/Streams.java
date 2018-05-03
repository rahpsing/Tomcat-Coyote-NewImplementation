// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload.util;

import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

public final class Streams
{
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    
    public static long copy(final InputStream pInputStream, final OutputStream pOutputStream, final boolean pClose) throws IOException {
        return copy(pInputStream, pOutputStream, pClose, new byte[8192]);
    }
    
    public static long copy(final InputStream pIn, final OutputStream pOut, final boolean pClose, final byte[] pBuffer) throws IOException {
        OutputStream out = pOut;
        InputStream in = pIn;
        try {
            long total = 0L;
            while (true) {
                final int res = in.read(pBuffer);
                if (res == -1) {
                    break;
                }
                if (res <= 0) {
                    continue;
                }
                total += res;
                if (out == null) {
                    continue;
                }
                out.write(pBuffer, 0, res);
            }
            if (out != null) {
                if (pClose) {
                    out.close();
                }
                else {
                    out.flush();
                }
                out = null;
            }
            in.close();
            in = null;
            return total;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex) {}
            }
            if (pClose && out != null) {
                try {
                    out.close();
                }
                catch (IOException ex2) {}
            }
        }
    }
    
    public static String asString(final InputStream pStream) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(pStream, baos, true);
        return baos.toString();
    }
    
    public static String asString(final InputStream pStream, final String pEncoding) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(pStream, baos, true);
        return baos.toString(pEncoding);
    }
    
    public static String checkFileName(final String pFileName) {
        if (pFileName != null && pFileName.indexOf(0) != -1) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pFileName.length(); ++i) {
                final char c = pFileName.charAt(i);
                switch (c) {
                    case '\0': {
                        sb.append("\\0");
                        break;
                    }
                    default: {
                        sb.append(c);
                        break;
                    }
                }
            }
            throw new InvalidFileNameException(pFileName, "Invalid file name: " + (Object)sb);
        }
        return pFileName;
    }
}
