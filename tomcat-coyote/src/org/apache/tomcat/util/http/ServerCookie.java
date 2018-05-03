// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.FieldPosition;
import java.util.Date;
import java.text.DateFormat;
import org.apache.tomcat.util.buf.MessageBytes;
import java.io.Serializable;

public class ServerCookie implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final MessageBytes name;
    private final MessageBytes value;
    private final MessageBytes path;
    private final MessageBytes domain;
    private boolean secure;
    private final MessageBytes comment;
    private int maxAge;
    private int version;
    private static final String OLD_COOKIE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";
    private static final ThreadLocal<DateFormat> OLD_COOKIE_FORMAT;
    private static final String ancientDate;
    
    public ServerCookie() {
        this.name = MessageBytes.newInstance();
        this.value = MessageBytes.newInstance();
        this.path = MessageBytes.newInstance();
        this.domain = MessageBytes.newInstance();
        this.comment = MessageBytes.newInstance();
        this.maxAge = -1;
        this.version = 0;
    }
    
    public void recycle() {
        this.path.recycle();
        this.name.recycle();
        this.value.recycle();
        this.comment.recycle();
        this.maxAge = -1;
        this.path.recycle();
        this.domain.recycle();
        this.version = 0;
        this.secure = false;
    }
    
    public MessageBytes getComment() {
        return this.comment;
    }
    
    public MessageBytes getDomain() {
        return this.domain;
    }
    
    public void setMaxAge(final int expiry) {
        this.maxAge = expiry;
    }
    
    public int getMaxAge() {
        return this.maxAge;
    }
    
    public MessageBytes getPath() {
        return this.path;
    }
    
    public void setSecure(final boolean flag) {
        this.secure = flag;
    }
    
    public boolean getSecure() {
        return this.secure;
    }
    
    public MessageBytes getName() {
        return this.name;
    }
    
    public MessageBytes getValue() {
        return this.value;
    }
    
    public int getVersion() {
        return this.version;
    }
    
    public void setVersion(final int v) {
        this.version = v;
    }
    
    @Override
    public String toString() {
        return "Cookie " + this.getName() + "=" + this.getValue() + " ; " + this.getVersion() + " " + this.getPath() + " " + this.getDomain();
    }
    
    public static void appendCookieValue(final StringBuffer headerBuf, final int version, final String name, final String value, final String path, final String domain, final String comment, final int maxAge, final boolean isSecure, final boolean isHttpOnly) {
        final StringBuffer buf = new StringBuffer();
        buf.append(name);
        buf.append("=");
        int newVersion = version;
        if (newVersion == 0 && ((!CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 && CookieSupport.isHttpToken(value)) || (CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 && CookieSupport.isV0Token(value)))) {
            newVersion = 1;
        }
        if (newVersion == 0 && comment != null) {
            newVersion = 1;
        }
        if (newVersion == 0 && ((!CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 && CookieSupport.isHttpToken(path)) || (CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 && CookieSupport.isV0Token(path)))) {
            newVersion = 1;
        }
        if (newVersion == 0 && ((!CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 && CookieSupport.isHttpToken(domain)) || (CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 && CookieSupport.isV0Token(domain)))) {
            newVersion = 1;
        }
        maybeQuote(buf, value);
        if (newVersion == 1) {
            buf.append("; Version=1");
            if (comment != null) {
                buf.append("; Comment=");
                maybeQuote(buf, comment);
            }
        }
        if (domain != null) {
            buf.append("; Domain=");
            maybeQuote(buf, domain);
        }
        if (maxAge >= 0) {
            if (newVersion > 0) {
                buf.append("; Max-Age=");
                buf.append(maxAge);
            }
            if (newVersion == 0 || CookieSupport.ALWAYS_ADD_EXPIRES) {
                buf.append("; Expires=");
                if (maxAge == 0) {
                    buf.append(ServerCookie.ancientDate);
                }
                else {
                    ServerCookie.OLD_COOKIE_FORMAT.get().format(new Date(System.currentTimeMillis() + maxAge * 1000L), buf, new FieldPosition(0));
                }
            }
        }
        if (path != null) {
            buf.append("; Path=");
            maybeQuote(buf, path);
        }
        if (isSecure) {
            buf.append("; Secure");
        }
        if (isHttpOnly) {
            buf.append("; HttpOnly");
        }
        headerBuf.append(buf);
    }
    
    private static void maybeQuote(final StringBuffer buf, final String value) {
        if (value == null || value.length() == 0) {
            buf.append("\"\"");
        }
        else if (CookieSupport.alreadyQuoted(value)) {
            buf.append('\"');
            buf.append(escapeDoubleQuotes(value, 1, value.length() - 1));
            buf.append('\"');
        }
        else if ((CookieSupport.isHttpToken(value) && !CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0) || (CookieSupport.isV0Token(value) && CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0)) {
            buf.append('\"');
            buf.append(escapeDoubleQuotes(value, 0, value.length()));
            buf.append('\"');
        }
        else {
            buf.append(value);
        }
    }
    
    private static String escapeDoubleQuotes(final String s, final int beginIndex, final int endIndex) {
        if (s == null || s.length() == 0 || s.indexOf(34) == -1) {
            return s;
        }
        final StringBuffer b = new StringBuffer();
        for (int i = beginIndex; i < endIndex; ++i) {
            final char c = s.charAt(i);
            if (c == '\\') {
                b.append(c);
                if (++i >= endIndex) {
                    throw new IllegalArgumentException("Invalid escape character in cookie value.");
                }
                b.append(s.charAt(i));
            }
            else if (c == '\"') {
                b.append('\\').append('\"');
            }
            else {
                b.append(c);
            }
        }
        return b.toString();
    }
    
    static {
        OLD_COOKIE_FORMAT = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue() {
                final DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df;
            }
        };
        ancientDate = ServerCookie.OLD_COOKIE_FORMAT.get().format(new Date(10000L));
    }
}
