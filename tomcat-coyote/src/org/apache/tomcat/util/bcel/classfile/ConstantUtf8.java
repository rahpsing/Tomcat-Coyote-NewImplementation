// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.DataInput;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;

public final class ConstantUtf8 extends Constant
{
    private static final long serialVersionUID = 8119001312020421976L;
    private final String bytes;
    private static final int MAX_CACHE_ENTRIES = 20000;
    private static final int INITIAL_CACHE_CAPACITY = 26666;
    private static HashMap<String, ConstantUtf8> cache;
    
    private static synchronized ConstantUtf8 getCachedInstance(final String s) {
        if (s.length() > 200) {
            return new ConstantUtf8(s);
        }
        if (ConstantUtf8.cache == null) {
            ConstantUtf8.cache = new LinkedHashMap<String, ConstantUtf8>(26666, 0.75f, true) {
                private static final long serialVersionUID = 1L;
                
                @Override
                protected boolean removeEldestEntry(final Map.Entry<String, ConstantUtf8> eldest) {
                    return this.size() > 20000;
                }
            };
        }
        ConstantUtf8 result = ConstantUtf8.cache.get(s);
        if (result != null) {
            return result;
        }
        result = new ConstantUtf8(s);
        ConstantUtf8.cache.put(s, result);
        return result;
    }
    
    private static ConstantUtf8 getInstance(final String s) {
        return getCachedInstance(s);
    }
    
    static ConstantUtf8 getInstance(final DataInputStream file) throws IOException {
        return getInstance(file.readUTF());
    }
    
    ConstantUtf8(final DataInput file) throws IOException {
        super((byte)1);
        this.bytes = file.readUTF();
    }
    
    private ConstantUtf8(final String bytes) {
        super((byte)1);
        if (bytes == null) {
            throw new IllegalArgumentException("bytes must not be null!");
        }
        this.bytes = bytes;
    }
    
    public final String getBytes() {
        return this.bytes;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(\"" + Utility.replace(this.bytes, "\n", "\\n") + "\")";
    }
}
