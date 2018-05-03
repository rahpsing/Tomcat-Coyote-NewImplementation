// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

public final class HexUtils
{
    private static final int[] DEC;
    private static final byte[] HEX;
    private static final char[] hex;
    
    @Deprecated
    public static void load() {
    }
    
    public static int getDec(final int index) {
        try {
            return HexUtils.DEC[index - 48];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            return -1;
        }
    }
    
    public static byte getHex(final int index) {
        return HexUtils.HEX[index];
    }
    
    public static String toHexString(final byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(bytes.length << 1);
        for (int i = 0; i < bytes.length; ++i) {
            sb.append(HexUtils.hex[(bytes[i] & 0xF0) >> 4]).append(HexUtils.hex[bytes[i] & 0xF]);
        }
        return sb.toString();
    }
    
    static {
        DEC = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15 };
        HEX = new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102 };
        hex = "0123456789abcdef".toCharArray();
    }
}
