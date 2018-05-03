// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.CharConversionException;
import java.io.IOException;

public final class UDecoder
{
    protected static final boolean ALLOW_ENCODED_SLASH;
    private static final IOException EXCEPTION_EOF;
    private static final IOException EXCEPTION_NOT_HEX_DIGIT;
    private static final IOException EXCEPTION_SLASH;
    
    @Deprecated
    public void convert(final ByteChunk mb) throws IOException {
        this.convert(mb, true);
    }
    
    public void convert(final ByteChunk mb, final boolean query) throws IOException {
        final int start = mb.getOffset();
        final byte[] buff = mb.getBytes();
        final int end = mb.getEnd();
        int idx = ByteChunk.findByte(buff, start, end, (byte)37);
        int idx2 = -1;
        if (query) {
            idx2 = ByteChunk.findByte(buff, start, (idx >= 0) ? idx : end, (byte)43);
        }
        if (idx < 0 && idx2 < 0) {
            return;
        }
        if ((idx2 >= 0 && idx2 < idx) || idx < 0) {
            idx = idx2;
        }
        final boolean noSlash = !UDecoder.ALLOW_ENCODED_SLASH && !query;
        for (int j = idx; j < end; ++j, ++idx) {
            if (buff[j] == 43 && query) {
                buff[idx] = 32;
            }
            else if (buff[j] != 37) {
                buff[idx] = buff[j];
            }
            else {
                if (j + 2 >= end) {
                    throw UDecoder.EXCEPTION_EOF;
                }
                final byte b1 = buff[j + 1];
                final byte b2 = buff[j + 2];
                if (!isHexDigit(b1) || !isHexDigit(b2)) {
                    throw UDecoder.EXCEPTION_NOT_HEX_DIGIT;
                }
                j += 2;
                final int res = x2c(b1, b2);
                if (noSlash && res == 47) {
                    throw UDecoder.EXCEPTION_SLASH;
                }
                buff[idx] = (byte)res;
            }
        }
        mb.setEnd(idx);
    }
    
    @Deprecated
    public void convert(final CharChunk mb) throws IOException {
        this.convert(mb, true);
    }
    
    public void convert(final CharChunk mb, final boolean query) throws IOException {
        final int start = mb.getOffset();
        final char[] buff = mb.getBuffer();
        final int cend = mb.getEnd();
        int idx = CharChunk.indexOf(buff, start, cend, '%');
        int idx2 = -1;
        if (query) {
            idx2 = CharChunk.indexOf(buff, start, (idx >= 0) ? idx : cend, '+');
        }
        if (idx < 0 && idx2 < 0) {
            return;
        }
        if ((idx2 >= 0 && idx2 < idx) || idx < 0) {
            idx = idx2;
        }
        final boolean noSlash = !UDecoder.ALLOW_ENCODED_SLASH && !query;
        for (int j = idx; j < cend; ++j, ++idx) {
            if (buff[j] == '+' && query) {
                buff[idx] = ' ';
            }
            else if (buff[j] != '%') {
                buff[idx] = buff[j];
            }
            else {
                if (j + 2 >= cend) {
                    throw UDecoder.EXCEPTION_EOF;
                }
                final char b1 = buff[j + 1];
                final char b2 = buff[j + 2];
                if (!isHexDigit(b1) || !isHexDigit(b2)) {
                    throw UDecoder.EXCEPTION_NOT_HEX_DIGIT;
                }
                j += 2;
                final int res = x2c(b1, b2);
                if (noSlash && res == 47) {
                    throw UDecoder.EXCEPTION_SLASH;
                }
                buff[idx] = (char)res;
            }
        }
        mb.setEnd(idx);
    }
    
    @Deprecated
    public void convert(final MessageBytes mb) throws IOException {
        this.convert(mb, true);
    }
    
    public void convert(final MessageBytes mb, final boolean query) throws IOException {
        switch (mb.getType()) {
            case 1: {
                final String strValue = mb.toString();
                if (strValue == null) {
                    return;
                }
                try {
                    mb.setString(this.convert(strValue, query));
                    break;
                }
                catch (RuntimeException ex) {
                    throw new DecodeException(ex.getMessage());
                }
            }
            case 3: {
                final CharChunk charC = mb.getCharChunk();
                this.convert(charC, query);
                break;
            }
            case 2: {
                final ByteChunk bytesC = mb.getByteChunk();
                this.convert(bytesC, query);
                break;
            }
        }
    }
    
    @Deprecated
    public final String convert(final String str) {
        return this.convert(str, true);
    }
    
    public final String convert(final String str, final boolean query) {
        if (str == null) {
            return null;
        }
        if ((!query || str.indexOf(43) < 0) && str.indexOf(37) < 0) {
            return str;
        }
        final boolean noSlash = !UDecoder.ALLOW_ENCODED_SLASH && !query;
        final StringBuilder dec = new StringBuilder();
        int strPos = 0;
        final int strLen = str.length();
        dec.ensureCapacity(str.length());
        while (strPos < strLen) {
            int laPos;
            for (laPos = strPos; laPos < strLen; ++laPos) {
                final char laChar = str.charAt(laPos);
                if (laChar == '+' && query) {
                    break;
                }
                if (laChar == '%') {
                    break;
                }
            }
            if (laPos > strPos) {
                dec.append(str.substring(strPos, laPos));
                strPos = laPos;
            }
            if (strPos >= strLen) {
                break;
            }
            final char metaChar = str.charAt(strPos);
            if (metaChar == '+') {
                dec.append(' ');
                ++strPos;
            }
            else {
                if (metaChar != '%') {
                    continue;
                }
                final char res = (char)Integer.parseInt(str.substring(strPos + 1, strPos + 3), 16);
                if (noSlash && res == '/') {
                    throw new IllegalArgumentException("noSlash");
                }
                dec.append(res);
                strPos += 3;
            }
        }
        return dec.toString();
    }
    
    private static boolean isHexDigit(final int c) {
        return (c >= 48 && c <= 57) || (c >= 97 && c <= 102) || (c >= 65 && c <= 70);
    }
    
    private static int x2c(final byte b1, final byte b2) {
        int digit = (b1 >= 65) ? ((b1 & 0xDF) - 65 + 10) : (b1 - 48);
        digit *= 16;
        digit += ((b2 >= 65) ? ((b2 & 0xDF) - 65 + 10) : (b2 - 48));
        return digit;
    }
    
    private static int x2c(final char b1, final char b2) {
        int digit = (b1 >= 'A') ? ((b1 & '\u00df') - 'A' + '\n') : (b1 - '0');
        digit *= 16;
        digit += ((b2 >= 'A') ? ((b2 & '\u00df') - 'A' + '\n') : (b2 - '0'));
        return digit;
    }
    
    static {
        ALLOW_ENCODED_SLASH = Boolean.valueOf(System.getProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "false"));
        EXCEPTION_EOF = new DecodeException("EOF");
        EXCEPTION_NOT_HEX_DIGIT = new DecodeException("isHexDigit");
        EXCEPTION_SLASH = new DecodeException("noSlash");
    }
    
    private static class DecodeException extends CharConversionException
    {
        private static final long serialVersionUID = 1L;
        
        public DecodeException(final String s) {
            super(s);
        }
        
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
