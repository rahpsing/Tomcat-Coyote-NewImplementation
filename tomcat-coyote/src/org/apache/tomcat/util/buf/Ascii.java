// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

public final class Ascii
{
    private static final byte[] toUpper;
    private static final byte[] toLower;
    private static final boolean[] isAlpha;
    private static final boolean[] isUpper;
    private static final boolean[] isLower;
    private static final boolean[] isWhite;
    private static final boolean[] isDigit;
    
    @Deprecated
    public static int toUpper(final int c) {
        return Ascii.toUpper[c & 0xFF] & 0xFF;
    }
    
    public static int toLower(final int c) {
        return Ascii.toLower[c & 0xFF] & 0xFF;
    }
    
    @Deprecated
    public static boolean isAlpha(final int c) {
        return Ascii.isAlpha[c & 0xFF];
    }
    
    @Deprecated
    public static boolean isUpper(final int c) {
        return Ascii.isUpper[c & 0xFF];
    }
    
    @Deprecated
    public static boolean isLower(final int c) {
        return Ascii.isLower[c & 0xFF];
    }
    
    @Deprecated
    public static boolean isWhite(final int c) {
        return Ascii.isWhite[c & 0xFF];
    }
    
    public static boolean isDigit(final int c) {
        return Ascii.isDigit[c & 0xFF];
    }
    
    @Deprecated
    public static int parseInt(final byte[] b, int off, int len) throws NumberFormatException {
        int c;
        if (b == null || len <= 0 || !isDigit(c = b[off++])) {
            throw new NumberFormatException();
        }
        int n = c - 48;
        while (--len > 0) {
            if (!isDigit(c = b[off++])) {
                throw new NumberFormatException();
            }
            n = n * 10 + c - 48;
        }
        return n;
    }
    
    @Deprecated
    public static int parseInt(final char[] b, int off, int len) throws NumberFormatException {
        int c;
        if (b == null || len <= 0 || !isDigit(c = b[off++])) {
            throw new NumberFormatException();
        }
        int n = c - 48;
        while (--len > 0) {
            if (!isDigit(c = b[off++])) {
                throw new NumberFormatException();
            }
            n = n * 10 + c - 48;
        }
        return n;
    }
    
    public static long parseLong(final byte[] b, int off, int len) throws NumberFormatException {
        int c;
        if (b == null || len <= 0 || !isDigit(c = b[off++])) {
            throw new NumberFormatException();
        }
        long n = c - 48;
        while (--len > 0) {
            if (!isDigit(c = b[off++])) {
                throw new NumberFormatException();
            }
            final long m = n * 10L + c - 48L;
            if (m < n) {
                throw new NumberFormatException();
            }
            n = m;
        }
        return n;
    }
    
    @Deprecated
    public static long parseLong(final char[] b, int off, int len) throws NumberFormatException {
        int c;
        if (b == null || len <= 0 || !isDigit(c = b[off++])) {
            throw new NumberFormatException();
        }
        long n = c - 48;
        while (--len > 0) {
            if (!isDigit(c = b[off++])) {
                throw new NumberFormatException();
            }
            final long m = n * 10L + c - 48L;
            if (m < n) {
                throw new NumberFormatException();
            }
            n = m;
        }
        return n;
    }
    
    static {
        toUpper = new byte[256];
        toLower = new byte[256];
        isAlpha = new boolean[256];
        isUpper = new boolean[256];
        isLower = new boolean[256];
        isWhite = new boolean[256];
        isDigit = new boolean[256];
        for (int i = 0; i < 256; ++i) {
            Ascii.toUpper[i] = (byte)i;
            Ascii.toLower[i] = (byte)i;
        }
        for (int lc = 97; lc <= 122; ++lc) {
            final int uc = lc + 65 - 97;
            Ascii.toUpper[lc] = (byte)uc;
            Ascii.toLower[uc] = (byte)lc;
            Ascii.isAlpha[lc] = true;
            Ascii.isAlpha[uc] = true;
            Ascii.isLower[lc] = true;
            Ascii.isUpper[uc] = true;
        }
        Ascii.isWhite[32] = true;
        Ascii.isWhite[9] = true;
        Ascii.isWhite[13] = true;
        Ascii.isWhite[10] = true;
        Ascii.isWhite[12] = true;
        Ascii.isWhite[8] = true;
        for (int d = 48; d <= 57; ++d) {
            Ascii.isDigit[d] = true;
        }
    }
}
