// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

public class Util
{
    public static boolean objectNameValueNeedsQuote(final String input) {
        for (int i = 0; i < input.length(); ++i) {
            final char ch = input.charAt(i);
            if (ch == ',' || ch == '=' || ch == ':' || ch == '*' || ch == '?') {
                return true;
            }
        }
        return false;
    }
}
