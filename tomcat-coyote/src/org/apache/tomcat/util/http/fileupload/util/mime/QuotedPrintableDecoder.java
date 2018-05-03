// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload.util.mime;

import java.io.IOException;
import java.io.OutputStream;

final class QuotedPrintableDecoder
{
    private static final int UPPER_NIBBLE_SHIFT = 4;
    
    public static int decode(final byte[] data, final OutputStream out) throws IOException {
        int off = 0;
        final int length = data.length;
        final int endOffset = off + length;
        int bytesWritten = 0;
        while (off < endOffset) {
            final byte ch = data[off++];
            if (ch == 95) {
                out.write(32);
            }
            else if (ch == 61) {
                if (off + 1 >= endOffset) {
                    throw new IOException("Invalid quoted printable encoding; truncated escape sequence");
                }
                final byte b1 = data[off++];
                final byte b2 = data[off++];
                if (b1 == 13) {
                    if (b2 != 10) {
                        throw new IOException("Invalid quoted printable encoding; CR must be followed by LF");
                    }
                    continue;
                }
                else {
                    final int c1 = hexToBinary(b1);
                    final int c2 = hexToBinary(b2);
                    out.write(c1 << 4 | c2);
                    ++bytesWritten;
                }
            }
            else {
                out.write(ch);
                ++bytesWritten;
            }
        }
        return bytesWritten;
    }
    
    private static int hexToBinary(final byte b) throws IOException {
        final int i = Character.digit((char)b, 16);
        if (i == -1) {
            throw new IOException("Invalid quoted printable encoding: not a valid hex digit: " + b);
        }
        return i;
    }
}
