// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.util.Locale;
import java.net.MalformedURLException;
import java.io.Serializable;

public final class URL implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String authority;
    private String file;
    private String host;
    private String path;
    private int port;
    private String protocol;
    private String query;
    private String ref;
    private String userInfo;
    
    public URL(final String spec) throws MalformedURLException {
        this(null, spec);
    }
    
    public URL(final URL context, final String spec) throws MalformedURLException {
        this.authority = null;
        this.file = null;
        this.host = null;
        this.path = null;
        this.port = -1;
        this.protocol = null;
        this.query = null;
        this.ref = null;
        this.userInfo = null;
        int start = 0;
        String newProtocol = null;
        boolean aRef = false;
        try {
            int limit;
            for (limit = spec.length(); limit > 0 && spec.charAt(limit - 1) <= ' '; --limit) {}
            while (start < limit && spec.charAt(start) <= ' ') {
                ++start;
            }
            if (spec.regionMatches(true, start, "url:", 0, 4)) {
                start += 4;
            }
            if (start < spec.length() && spec.charAt(start) == '#') {
                aRef = true;
            }
            for (int i = start; !aRef && i < limit; ++i) {
                final int c = spec.charAt(i);
                if (c == 58) {
                    final String s = newProtocol = spec.substring(start, i).toLowerCase(Locale.ENGLISH);
                    start = i + 1;
                    break;
                }
                if (c == 35) {
                    aRef = true;
                }
                else if (!isSchemeChar((char)c)) {
                    break;
                }
            }
            this.protocol = newProtocol;
            if (context != null && (newProtocol == null || newProtocol.equalsIgnoreCase(context.getProtocol()))) {
                if (context.getPath() != null && context.getPath().startsWith("/")) {
                    newProtocol = null;
                }
                if (newProtocol == null) {
                    this.protocol = context.getProtocol();
                    this.authority = context.getAuthority();
                    this.userInfo = context.getUserInfo();
                    this.host = context.getHost();
                    this.port = context.getPort();
                    this.file = context.getFile();
                    final int question = this.file.lastIndexOf("?");
                    if (question < 0) {
                        this.path = this.file;
                    }
                    else {
                        this.path = this.file.substring(0, question);
                    }
                }
            }
            if (this.protocol == null) {
                throw new MalformedURLException("no protocol: " + spec);
            }
            int i = spec.indexOf(35, start);
            if (i >= 0) {
                this.ref = spec.substring(i + 1, limit);
                limit = i;
            }
            this.parse(spec, start, limit);
            if (context != null) {
                this.normalize();
            }
        }
        catch (MalformedURLException e) {
            throw e;
        }
        catch (Exception e2) {
            throw new MalformedURLException(e2.toString());
        }
    }
    
    public URL(final String protocol, final String host, final String file) throws MalformedURLException {
        this(protocol, host, -1, file);
    }
    
    public URL(final String protocol, final String host, final int port, final String file) throws MalformedURLException {
        this.authority = null;
        this.file = null;
        this.host = null;
        this.path = null;
        this.port = -1;
        this.protocol = null;
        this.query = null;
        this.ref = null;
        this.userInfo = null;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        final int hash = file.indexOf(35);
        this.file = ((hash < 0) ? file : file.substring(0, hash));
        this.ref = ((hash < 0) ? null : file.substring(hash + 1));
        final int question = file.lastIndexOf(63);
        if (question >= 0) {
            this.query = file.substring(question + 1);
            this.path = file.substring(0, question);
        }
        else {
            this.path = file;
        }
        if (host != null && host.length() > 0) {
            this.authority = ((port == -1) ? host : (host + ":" + port));
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof URL)) {
            return false;
        }
        final URL other = (URL)obj;
        return this.sameFile(other) && this.compare(this.ref, other.getRef());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.file == null) ? 0 : this.file.hashCode());
        result = 31 * result + ((this.host == null) ? 0 : this.host.hashCode());
        result = 31 * result + this.port;
        result = 31 * result + ((this.protocol == null) ? 0 : this.protocol.hashCode());
        result = 31 * result + ((this.ref == null) ? 0 : this.ref.hashCode());
        return result;
    }
    
    public String getAuthority() {
        return this.authority;
    }
    
    public String getFile() {
        if (this.file == null) {
            return "";
        }
        return this.file;
    }
    
    public String getHost() {
        return this.host;
    }
    
    public String getPath() {
        if (this.path == null) {
            return "";
        }
        return this.path;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public String getProtocol() {
        return this.protocol;
    }
    
    public String getQuery() {
        return this.query;
    }
    
    public String getRef() {
        return this.ref;
    }
    
    public String getUserInfo() {
        return this.userInfo;
    }
    
    public void normalize() throws MalformedURLException {
        if (this.path == null) {
            if (this.query != null) {
                this.file = "?" + this.query;
            }
            else {
                this.file = "";
            }
            return;
        }
        String normalized = this.path;
        if (normalized.equals("/.")) {
            this.path = "/";
            if (this.query != null) {
                this.file = this.path + "?" + this.query;
            }
            else {
                this.file = this.path;
            }
            return;
        }
        if (normalized.indexOf(92) >= 0) {
            normalized = normalized.replace('\\', '/');
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (true) {
            final int index = normalized.indexOf("//");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) + normalized.substring(index + 1);
        }
        while (true) {
            final int index = normalized.indexOf("/./");
            if (index < 0) {
                break;
            }
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0) {
                if (normalized.endsWith("/.")) {
                    normalized = normalized.substring(0, normalized.length() - 1);
                }
                if (normalized.endsWith("/..")) {
                    index = normalized.length() - 3;
                    final int index2 = normalized.lastIndexOf(47, index - 1);
                    if (index2 < 0) {
                        throw new MalformedURLException("Invalid relative URL reference");
                    }
                    normalized = normalized.substring(0, index2 + 1);
                }
                this.path = normalized;
                if (this.query != null) {
                    this.file = this.path + "?" + this.query;
                }
                else {
                    this.file = this.path;
                }
                return;
            }
            if (index == 0) {
                throw new MalformedURLException("Invalid relative URL reference");
            }
            final int index2 = normalized.lastIndexOf(47, index - 1);
            normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
        }
    }
    
    public boolean sameFile(final URL other) {
        return this.compare(this.protocol, other.getProtocol()) && this.compare(this.host, other.getHost()) && this.port == other.getPort() && this.compare(this.file, other.getFile());
    }
    
    @Deprecated
    public String toExternalForm() {
        final StringBuilder sb = new StringBuilder();
        if (this.protocol != null) {
            sb.append(this.protocol);
            sb.append(":");
        }
        if (this.authority != null) {
            sb.append("//");
            sb.append(this.authority);
        }
        if (this.path != null) {
            sb.append(this.path);
        }
        if (this.query != null) {
            sb.append('?');
            sb.append(this.query);
        }
        if (this.ref != null) {
            sb.append('#');
            sb.append(this.ref);
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("URL[");
        sb.append("authority=");
        sb.append(this.authority);
        sb.append(", file=");
        sb.append(this.file);
        sb.append(", host=");
        sb.append(this.host);
        sb.append(", port=");
        sb.append(this.port);
        sb.append(", protocol=");
        sb.append(this.protocol);
        sb.append(", query=");
        sb.append(this.query);
        sb.append(", ref=");
        sb.append(this.ref);
        sb.append(", userInfo=");
        sb.append(this.userInfo);
        sb.append("]");
        return sb.toString();
    }
    
    private boolean compare(final String first, final String second) {
        if (first == null) {
            return second == null;
        }
        return second != null && first.equals(second);
    }
    
    private void parse(final String spec, int start, int limit) throws MalformedURLException {
        final int question = spec.lastIndexOf(63, limit - 1);
        if (question >= 0 && question < limit) {
            this.query = spec.substring(question + 1, limit);
            limit = question;
        }
        else {
            this.query = null;
        }
        if (spec.indexOf("//", start) == start) {
            final int pathStart = spec.indexOf("/", start + 2);
            if (pathStart >= 0 && pathStart < limit) {
                this.authority = spec.substring(start + 2, pathStart);
                start = pathStart;
            }
            else {
                this.authority = spec.substring(start + 2, limit);
                start = limit;
            }
            if (this.authority.length() > 0) {
                int at = this.authority.indexOf(64);
                if (at >= 0) {
                    this.userInfo = this.authority.substring(0, at);
                }
                int ipv6 = this.authority.indexOf(91, at + 1);
                int hStart = at + 1;
                if (ipv6 >= 0) {
                    hStart = ipv6;
                    ipv6 = this.authority.indexOf(93, ipv6);
                    if (ipv6 < 0) {
                        throw new MalformedURLException("Closing ']' not found in IPV6 address: " + this.authority);
                    }
                    at = ipv6 - 1;
                }
                final int colon = this.authority.indexOf(58, at + 1);
                if (colon >= 0) {
                    try {
                        this.port = Integer.parseInt(this.authority.substring(colon + 1));
                    }
                    catch (NumberFormatException e) {
                        throw new MalformedURLException(e.toString());
                    }
                    this.host = this.authority.substring(hStart, colon);
                }
                else {
                    this.host = this.authority.substring(hStart);
                    this.port = -1;
                }
            }
        }
        if (spec.indexOf("/", start) == start) {
            this.path = spec.substring(start, limit);
            if (this.query != null) {
                this.file = this.path + "?" + this.query;
            }
            else {
                this.file = this.path;
            }
            return;
        }
        if (this.path == null) {
            if (this.query != null) {
                this.file = "?" + this.query;
            }
            else {
                this.file = null;
            }
            return;
        }
        if (!this.path.startsWith("/")) {
            throw new MalformedURLException("Base path does not start with '/'");
        }
        if (!this.path.endsWith("/")) {
            this.path += "/../";
        }
        this.path += spec.substring(start, limit);
        if (this.query != null) {
            this.file = this.path + "?" + this.query;
        }
        else {
            this.file = this.path;
        }
    }
    
    public static boolean isSchemeChar(final char c) {
        return Character.isLetterOrDigit(c) || c == '+' || c == '-' || c == '.';
    }
}
