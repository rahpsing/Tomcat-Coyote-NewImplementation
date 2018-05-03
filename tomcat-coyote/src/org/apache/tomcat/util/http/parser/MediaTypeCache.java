// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.parser;

import java.io.IOException;
import java.io.StringReader;
import org.apache.tomcat.util.collections.ConcurrentCache;

public class MediaTypeCache
{
    private final ConcurrentCache<String, String[]> cache;
    
    public MediaTypeCache(final int size) {
        this.cache = new ConcurrentCache<String, String[]>(size);
    }
    
    public String[] parse(final String input) {
        String[] result = this.cache.get(input);
        if (result != null) {
            return result;
        }
        MediaType m = null;
        try {
            m = HttpParser.parseMediaType(new StringReader(input));
        }
        catch (IOException ex) {}
        if (m != null) {
            result = new String[] { m.toStringNoCharset(), m.getCharset() };
            this.cache.put(input, result);
        }
        return result;
    }
}
