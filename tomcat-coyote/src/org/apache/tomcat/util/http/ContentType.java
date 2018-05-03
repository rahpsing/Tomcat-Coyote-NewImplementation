// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

public class ContentType
{
    public static String getCharsetFromContentType(final String contentType) {
        if (contentType == null) {
            return null;
        }
        final int start = contentType.indexOf("charset=");
        if (start < 0) {
            return null;
        }
        String encoding = contentType.substring(start + 8);
        final int end = encoding.indexOf(59);
        if (end >= 0) {
            encoding = encoding.substring(0, end);
        }
        encoding = encoding.trim();
        if (encoding.length() > 2 && encoding.startsWith("\"") && encoding.endsWith("\"")) {
            encoding = encoding.substring(1, encoding.length() - 1);
        }
        return encoding.trim();
    }
    
    public static boolean hasCharset(final String type) {
        boolean hasCharset = false;
        final int len = type.length();
        for (int index = type.indexOf(59); index != -1; index = type.indexOf(59, index)) {
            ++index;
            while (index < len && Character.isSpace(type.charAt(index))) {
                ++index;
            }
            if (index + 8 < len && type.charAt(index) == 'c' && type.charAt(index + 1) == 'h' && type.charAt(index + 2) == 'a' && type.charAt(index + 3) == 'r' && type.charAt(index + 4) == 's' && type.charAt(index + 5) == 'e' && type.charAt(index + 6) == 't' && type.charAt(index + 7) == '=') {
                hasCharset = true;
                break;
            }
        }
        return hasCharset;
    }
}
