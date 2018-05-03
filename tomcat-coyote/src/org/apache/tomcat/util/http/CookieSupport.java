// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

public final class CookieSupport
{
    public static final boolean STRICT_SERVLET_COMPLIANCE;
    public static final boolean ALLOW_EQUALS_IN_VALUE;
    public static final boolean ALLOW_HTTP_SEPARATORS_IN_V0;
    public static final boolean ALWAYS_ADD_EXPIRES;
    public static final boolean FWD_SLASH_IS_SEPARATOR;
    public static final boolean ALLOW_NAME_ONLY;
    private static final char[] V0_SEPARATORS;
    private static final boolean[] V0_SEPARATOR_FLAGS;
    private static final char[] HTTP_SEPARATORS;
    private static final boolean[] HTTP_SEPARATOR_FLAGS;
    
    public static final boolean isV0Separator(final char c) {
        if ((c < ' ' || c >= '\u007f') && c != '\t') {
            throw new IllegalArgumentException("Control character in cookie value or attribute.");
        }
        return CookieSupport.V0_SEPARATOR_FLAGS[c];
    }
    
    public static boolean isV0Token(final String value) {
        if (value == null) {
            return false;
        }
        int i = 0;
        int len = value.length();
        if (alreadyQuoted(value)) {
            ++i;
            --len;
        }
        while (i < len) {
            final char c = value.charAt(i);
            if (isV0Separator(c)) {
                return true;
            }
            ++i;
        }
        return false;
    }
    
    public static final boolean isHttpSeparator(final char c) {
        if ((c < ' ' || c >= '\u007f') && c != '\t') {
            throw new IllegalArgumentException("Control character in cookie value or attribute.");
        }
        return CookieSupport.HTTP_SEPARATOR_FLAGS[c];
    }
    
    public static boolean isHttpToken(final String value) {
        if (value == null) {
            return false;
        }
        int i = 0;
        int len = value.length();
        if (alreadyQuoted(value)) {
            ++i;
            --len;
        }
        while (i < len) {
            final char c = value.charAt(i);
            if (isHttpSeparator(c)) {
                return true;
            }
            ++i;
        }
        return false;
    }
    
    public static boolean alreadyQuoted(final String value) {
        return value != null && value.length() >= 2 && value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"';
    }
    
    static {
        V0_SEPARATORS = new char[] { ',', ';', ' ', '\t' };
        V0_SEPARATOR_FLAGS = new boolean[128];
        HTTP_SEPARATOR_FLAGS = new boolean[128];
        STRICT_SERVLET_COMPLIANCE = Boolean.valueOf(System.getProperty("org.apache.catalina.STRICT_SERVLET_COMPLIANCE", "false"));
        ALLOW_EQUALS_IN_VALUE = Boolean.valueOf(System.getProperty("org.apache.tomcat.util.http.ServerCookie.ALLOW_EQUALS_IN_VALUE", "false"));
        ALLOW_HTTP_SEPARATORS_IN_V0 = Boolean.valueOf(System.getProperty("org.apache.tomcat.util.http.ServerCookie.ALLOW_HTTP_SEPARATORS_IN_V0", "false"));
        final String alwaysAddExpires = System.getProperty("org.apache.tomcat.util.http.ServerCookie.ALWAYS_ADD_EXPIRES");
        if (alwaysAddExpires == null) {
            ALWAYS_ADD_EXPIRES = !CookieSupport.STRICT_SERVLET_COMPLIANCE;
        }
        else {
            ALWAYS_ADD_EXPIRES = Boolean.valueOf(alwaysAddExpires);
        }
        final String fwdSlashIsSeparator = System.getProperty("org.apache.tomcat.util.http.ServerCookie.FWD_SLASH_IS_SEPARATOR");
        if (fwdSlashIsSeparator == null) {
            FWD_SLASH_IS_SEPARATOR = CookieSupport.STRICT_SERVLET_COMPLIANCE;
        }
        else {
            FWD_SLASH_IS_SEPARATOR = Boolean.valueOf(fwdSlashIsSeparator);
        }
        ALLOW_NAME_ONLY = Boolean.valueOf(System.getProperty("org.apache.tomcat.util.http.ServerCookie.ALLOW_NAME_ONLY", "false"));
        if (CookieSupport.FWD_SLASH_IS_SEPARATOR) {
            HTTP_SEPARATORS = new char[] { '\t', ' ', '\"', '(', ')', ',', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '{', '}' };
        }
        else {
            HTTP_SEPARATORS = new char[] { '\t', ' ', '\"', '(', ')', ',', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '{', '}' };
        }
        for (int i = 0; i < 128; ++i) {
            CookieSupport.V0_SEPARATOR_FLAGS[i] = false;
            CookieSupport.HTTP_SEPARATOR_FLAGS[i] = false;
        }
        for (int i = 0; i < CookieSupport.V0_SEPARATORS.length; ++i) {
            CookieSupport.V0_SEPARATOR_FLAGS[CookieSupport.V0_SEPARATORS[i]] = true;
        }
        for (int i = 0; i < CookieSupport.HTTP_SEPARATORS.length; ++i) {
            CookieSupport.HTTP_SEPARATOR_FLAGS[CookieSupport.HTTP_SEPARATORS[i]] = true;
        }
    }
}
