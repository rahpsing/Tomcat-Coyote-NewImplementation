// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.log.UserDataHelper;
import org.apache.juli.logging.Log;

public final class Cookies
{
    private static final Log log;
    private static final UserDataHelper userDataLog;
    protected static final StringManager sm;
    public static final int INITIAL_SIZE = 4;
    ServerCookie[] scookies;
    int cookieCount;
    boolean unprocessed;
    MimeHeaders headers;
    
    public Cookies(final MimeHeaders headers) {
        this.scookies = new ServerCookie[4];
        this.cookieCount = 0;
        this.unprocessed = true;
        this.headers = headers;
    }
    
    public void recycle() {
        for (int i = 0; i < this.cookieCount; ++i) {
            if (this.scookies[i] != null) {
                this.scookies[i].recycle();
            }
        }
        this.cookieCount = 0;
        this.unprocessed = true;
    }
    
    @Override
    public String toString() {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("=== Cookies ===");
        for (int count = this.getCookieCount(), i = 0; i < count; ++i) {
            pw.println(this.getCookie(i).toString());
        }
        return sw.toString();
    }
    
    public ServerCookie getCookie(final int idx) {
        if (this.unprocessed) {
            this.getCookieCount();
        }
        return this.scookies[idx];
    }
    
    public int getCookieCount() {
        if (this.unprocessed) {
            this.unprocessed = false;
            this.processCookies(this.headers);
        }
        return this.cookieCount;
    }
    
    private ServerCookie addCookie() {
        if (this.cookieCount >= this.scookies.length) {
            final ServerCookie[] scookiesTmp = new ServerCookie[2 * this.cookieCount];
            System.arraycopy(this.scookies, 0, scookiesTmp, 0, this.cookieCount);
            this.scookies = scookiesTmp;
        }
        ServerCookie c = this.scookies[this.cookieCount];
        if (c == null) {
            c = new ServerCookie();
            this.scookies[this.cookieCount] = c;
        }
        ++this.cookieCount;
        return c;
    }
    
    public void processCookies(final MimeHeaders headers) {
        if (headers == null) {
            return;
        }
        for (int pos = 0; pos >= 0; ++pos) {
            pos = headers.findHeader("Cookie", pos);
            if (pos < 0) {
                break;
            }
            final MessageBytes cookieValue = headers.getValue(pos);
            if (cookieValue != null && !cookieValue.isNull()) {
                if (cookieValue.getType() != 2) {
                    final Exception e = new Exception();
                    Cookies.log.warn((Object)"Cookies: Parsing cookie as String. Expected bytes.", (Throwable)e);
                    cookieValue.toBytes();
                }
                if (Cookies.log.isDebugEnabled()) {
                    Cookies.log.debug((Object)("Cookies: Parsing b[]: " + cookieValue.toString()));
                }
                final ByteChunk bc = cookieValue.getByteChunk();
                this.processCookieHeader(bc.getBytes(), bc.getOffset(), bc.getLength());
            }
        }
    }
    
    private static boolean equals(final String s, final byte[] b, final int start, final int end) {
        final int blen = end - start;
        if (b == null || blen != s.length()) {
            return false;
        }
        int boff = start;
        for (int i = 0; i < blen; ++i) {
            if (b[boff++] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    private static final boolean isWhiteSpace(final byte c) {
        return c == 32 || c == 9 || c == 10 || c == 13 || c == 12;
    }
    
    private static void unescapeDoubleQuotes(final ByteChunk bc) {
        if (bc == null || bc.getLength() == 0 || bc.indexOf('\"', 0) == -1) {
            return;
        }
        int src = bc.getStart();
        final int end = bc.getEnd();
        int dest = src;
        final byte[] buffer = bc.getBuffer();
        while (src < end) {
            if (buffer[src] == 92 && src < end && buffer[src + 1] == 34) {
                ++src;
            }
            buffer[dest] = buffer[src];
            ++dest;
            ++src;
        }
        bc.setEnd(dest);
    }
    
    protected final void processCookieHeader(final byte[] bytes, final int off, final int len) {
        if (len <= 0 || bytes == null) {
            return;
        }
        final int end = off + len;
        int pos = off;
        int nameStart = 0;
        int nameEnd = 0;
        int valueStart = 0;
        int valueEnd = 0;
        int version = 0;
        ServerCookie sc = null;
        while (pos < end) {
            boolean isSpecial = false;
            boolean isQuoted = false;
            while (pos < end && ((CookieSupport.isHttpSeparator((char)bytes[pos]) && !CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0) || CookieSupport.isV0Separator((char)bytes[pos]) || isWhiteSpace(bytes[pos]))) {
                ++pos;
            }
            if (pos >= end) {
                return;
            }
            if (bytes[pos] == 36) {
                isSpecial = true;
                ++pos;
            }
            valueStart = (valueEnd = (nameStart = pos));
            for (nameEnd = (pos = getTokenEndPosition(bytes, pos, end, version, true)); pos < end && isWhiteSpace(bytes[pos]); ++pos) {}
            if (pos < end - 1 && bytes[pos] == 61) {
                while (++pos < end && isWhiteSpace(bytes[pos])) {}
                if (pos >= end) {
                    return;
                }
                switch (bytes[pos]) {
                    case 34: {
                        isQuoted = true;
                        valueStart = pos + 1;
                        valueEnd = (pos = getQuotedValueEndPosition(bytes, valueStart, end));
                        if (pos >= end) {
                            return;
                        }
                        break;
                    }
                    case 44:
                    case 59: {
                        valueEnd = (valueStart = -1);
                        break;
                    }
                    default: {
                        if ((version == 0 && !CookieSupport.isV0Separator((char)bytes[pos]) && CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0) || !CookieSupport.isHttpSeparator((char)bytes[pos]) || (bytes[pos] == 61 && CookieSupport.ALLOW_EQUALS_IN_VALUE)) {
                            valueStart = pos;
                            valueEnd = (pos = getTokenEndPosition(bytes, valueStart, end, version, false));
                            break;
                        }
                        final UserDataHelper.Mode logMode = Cookies.userDataLog.getNextMode();
                        if (logMode != null) {
                            String message = Cookies.sm.getString("cookies.invalidCookieToken");
                            switch (logMode) {
                                case INFO_THEN_DEBUG: {
                                    message += Cookies.sm.getString("cookies.fallToDebug");
                                }
                                case INFO: {
                                    Cookies.log.info((Object)message);
                                    break;
                                }
                                case DEBUG: {
                                    Cookies.log.debug((Object)message);
                                    break;
                                }
                            }
                        }
                        while (pos < end && bytes[pos] != 59 && bytes[pos] != 44) {
                            ++pos;
                        }
                        ++pos;
                        sc = null;
                        continue;
                    }
                }
            }
            else {
                valueEnd = (valueStart = -1);
                pos = nameEnd;
            }
            while (pos < end && isWhiteSpace(bytes[pos])) {
                ++pos;
            }
            while (pos < end && bytes[pos] != 59 && bytes[pos] != 44) {
                ++pos;
            }
            ++pos;
            if (isSpecial) {
                isSpecial = false;
                if (equals("Version", bytes, nameStart, nameEnd) && sc == null) {
                    if (bytes[valueStart] != 49 || valueEnd != valueStart + 1) {
                        continue;
                    }
                    version = 1;
                }
                else {
                    if (sc == null) {
                        continue;
                    }
                    if (equals("Domain", bytes, nameStart, nameEnd)) {
                        sc.getDomain().setBytes(bytes, valueStart, valueEnd - valueStart);
                    }
                    else if (equals("Path", bytes, nameStart, nameEnd)) {
                        sc.getPath().setBytes(bytes, valueStart, valueEnd - valueStart);
                    }
                    else {
                        if (equals("Port", bytes, nameStart, nameEnd)) {
                            continue;
                        }
                        if (equals("CommentURL", bytes, nameStart, nameEnd)) {
                            continue;
                        }
                        final UserDataHelper.Mode logMode = Cookies.userDataLog.getNextMode();
                        if (logMode == null) {
                            continue;
                        }
                        String message = Cookies.sm.getString("cookies.invalidSpecial");
                        switch (logMode) {
                            case INFO_THEN_DEBUG: {
                                message += Cookies.sm.getString("cookies.fallToDebug");
                            }
                            case INFO: {
                                Cookies.log.info((Object)message);
                                continue;
                            }
                            case DEBUG: {
                                Cookies.log.debug((Object)message);
                                continue;
                            }
                        }
                    }
                }
            }
            else {
                if (valueStart == -1 && !CookieSupport.ALLOW_NAME_ONLY) {
                    continue;
                }
                sc = this.addCookie();
                sc.setVersion(version);
                sc.getName().setBytes(bytes, nameStart, nameEnd - nameStart);
                if (valueStart != -1) {
                    sc.getValue().setBytes(bytes, valueStart, valueEnd - valueStart);
                    if (!isQuoted) {
                        continue;
                    }
                    unescapeDoubleQuotes(sc.getValue().getByteChunk());
                }
                else {
                    sc.getValue().setString("");
                }
            }
        }
    }
    
    private static final int getTokenEndPosition(final byte[] bytes, final int off, final int end, final int version, final boolean isName) {
        int pos;
        for (pos = off; pos < end && (!CookieSupport.isHttpSeparator((char)bytes[pos]) || (version == 0 && CookieSupport.ALLOW_HTTP_SEPARATORS_IN_V0 && bytes[pos] != 61 && !CookieSupport.isV0Separator((char)bytes[pos])) || (!isName && bytes[pos] == 61 && CookieSupport.ALLOW_EQUALS_IN_VALUE)); ++pos) {}
        if (pos > end) {
            return end;
        }
        return pos;
    }
    
    private static final int getQuotedValueEndPosition(final byte[] bytes, final int off, final int end) {
        int pos = off;
        while (pos < end) {
            if (bytes[pos] == 34) {
                return pos;
            }
            if (bytes[pos] == 92 && pos < end - 1) {
                pos += 2;
            }
            else {
                ++pos;
            }
        }
        return end;
    }
    
    static {
        log = LogFactory.getLog((Class)Cookies.class);
        userDataLog = new UserDataHelper(Cookies.log);
        sm = StringManager.getManager("org.apache.tomcat.util.http");
    }
}
