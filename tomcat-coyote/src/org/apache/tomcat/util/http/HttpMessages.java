// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.tomcat.util.res.StringManager;
import java.util.Locale;
import java.util.Map;

public class HttpMessages
{
    private static final Map<Locale, HttpMessages> instances;
    private static final HttpMessages DEFAULT;
    private final StringManager sm;
    private String st_200;
    private String st_302;
    private String st_400;
    private String st_404;
    
    private HttpMessages(final StringManager sm) {
        this.st_200 = null;
        this.st_302 = null;
        this.st_400 = null;
        this.st_404 = null;
        this.sm = sm;
    }
    
    public String getMessage(final int status) {
        switch (status) {
            case 200: {
                if (this.st_200 == null) {
                    this.st_200 = this.sm.getString("sc.200");
                }
                return this.st_200;
            }
            case 302: {
                if (this.st_302 == null) {
                    this.st_302 = this.sm.getString("sc.302");
                }
                return this.st_302;
            }
            case 400: {
                if (this.st_400 == null) {
                    this.st_400 = this.sm.getString("sc.400");
                }
                return this.st_400;
            }
            case 404: {
                if (this.st_404 == null) {
                    this.st_404 = this.sm.getString("sc.404");
                }
                return this.st_404;
            }
            default: {
                return this.sm.getString("sc." + status);
            }
        }
    }
    
    public static HttpMessages getInstance(final Locale locale) {
        HttpMessages result = HttpMessages.instances.get(locale);
        if (result == null) {
            final StringManager sm = StringManager.getManager("org.apache.tomcat.util.http.res", locale);
            if (Locale.getDefault().equals(sm.getLocale())) {
                result = HttpMessages.DEFAULT;
            }
            else {
                result = new HttpMessages(sm);
            }
            HttpMessages.instances.put(locale, result);
        }
        return result;
    }
    
    public static String filter(final String message) {
        if (message == null) {
            return null;
        }
        final char[] content = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        final StringBuilder result = new StringBuilder(content.length + 50);
        for (int i = 0; i < content.length; ++i) {
            switch (content[i]) {
                case '<': {
                    result.append("&lt;");
                    break;
                }
                case '>': {
                    result.append("&gt;");
                    break;
                }
                case '&': {
                    result.append("&amp;");
                    break;
                }
                case '\"': {
                    result.append("&quot;");
                    break;
                }
                default: {
                    result.append(content[i]);
                    break;
                }
            }
        }
        return result.toString();
    }
    
    public static boolean isSafeInHttpHeader(final String msg) {
        if (msg == null) {
            return true;
        }
        for (int len = msg.length(), i = 0; i < len; ++i) {
            final char c = msg.charAt(i);
            if ((' ' > c || c > '~') && ('\u0080' > c || c > '\u00ff') && c != '\t') {
                return false;
            }
        }
        return true;
    }
    
    static {
        instances = new ConcurrentHashMap<Locale, HttpMessages>();
        DEFAULT = new HttpMessages(StringManager.getManager("org.apache.tomcat.util.http.res", Locale.getDefault()));
    }
}
