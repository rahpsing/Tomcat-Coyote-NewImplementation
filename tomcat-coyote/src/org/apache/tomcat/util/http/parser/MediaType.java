// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.parser;

import java.util.Iterator;
import java.util.Map;
import java.util.Locale;
import java.util.LinkedHashMap;

public class MediaType
{
    private final String type;
    private final String subtype;
    private final LinkedHashMap<String, String> parameters;
    private final String charset;
    private volatile String noCharset;
    private volatile String withCharset;
    
    protected MediaType(final String type, final String subtype, final LinkedHashMap<String, String> parameters) {
        this.type = type;
        this.subtype = subtype;
        this.parameters = parameters;
        String cs = parameters.get("charset");
        if (cs != null && cs.length() > 0 && cs.charAt(0) == '\"') {
            cs = HttpParser.unquote(cs);
        }
        this.charset = cs;
    }
    
    public String getType() {
        return this.type;
    }
    
    public String getSubtype() {
        return this.subtype;
    }
    
    public String getCharset() {
        return this.charset;
    }
    
    public int getParameterCount() {
        return this.parameters.size();
    }
    
    public String getParameterValue(final String parameter) {
        return this.parameters.get(parameter.toLowerCase(Locale.ENGLISH));
    }
    
    @Override
    public String toString() {
        if (this.withCharset == null) {
            synchronized (this) {
                if (this.withCharset == null) {
                    final StringBuilder result = new StringBuilder();
                    result.append(this.type);
                    result.append('/');
                    result.append(this.subtype);
                    for (final Map.Entry<String, String> entry : this.parameters.entrySet()) {
                        final String value = entry.getValue();
                        if (value != null) {
                            if (value.length() == 0) {
                                continue;
                            }
                            result.append(';');
                            result.append(' ');
                            result.append(entry.getKey());
                            result.append('=');
                            result.append(value);
                        }
                    }
                    this.withCharset = result.toString();
                }
            }
        }
        return this.withCharset;
    }
    
    public String toStringNoCharset() {
        if (this.noCharset == null) {
            synchronized (this) {
                if (this.noCharset == null) {
                    final StringBuilder result = new StringBuilder();
                    result.append(this.type);
                    result.append('/');
                    result.append(this.subtype);
                    for (final Map.Entry<String, String> entry : this.parameters.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase("charset")) {
                            continue;
                        }
                        result.append(';');
                        result.append(' ');
                        result.append(entry.getKey());
                        result.append('=');
                        result.append(entry.getValue());
                    }
                    this.noCharset = result.toString();
                }
            }
        }
        return this.noCharset;
    }
}
