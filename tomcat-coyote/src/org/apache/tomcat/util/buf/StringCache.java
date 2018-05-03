// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

import java.nio.charset.Charset;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashMap;
import org.apache.juli.logging.Log;

public class StringCache
{
    private static final Log log;
    protected static boolean byteEnabled;
    protected static boolean charEnabled;
    protected static int trainThreshold;
    protected static int cacheSize;
    protected static int maxStringSize;
    protected static HashMap<ByteEntry, int[]> bcStats;
    protected static int bcCount;
    protected static ByteEntry[] bcCache;
    protected static HashMap<CharEntry, int[]> ccStats;
    protected static int ccCount;
    protected static CharEntry[] ccCache;
    protected static int accessCount;
    protected static int hitCount;
    
    public int getCacheSize() {
        return StringCache.cacheSize;
    }
    
    public void setCacheSize(final int cacheSize) {
        StringCache.cacheSize = cacheSize;
    }
    
    public boolean getByteEnabled() {
        return StringCache.byteEnabled;
    }
    
    public void setByteEnabled(final boolean byteEnabled) {
        StringCache.byteEnabled = byteEnabled;
    }
    
    public boolean getCharEnabled() {
        return StringCache.charEnabled;
    }
    
    public void setCharEnabled(final boolean charEnabled) {
        StringCache.charEnabled = charEnabled;
    }
    
    public int getTrainThreshold() {
        return StringCache.trainThreshold;
    }
    
    public void setTrainThreshold(final int trainThreshold) {
        StringCache.trainThreshold = trainThreshold;
    }
    
    public int getAccessCount() {
        return StringCache.accessCount;
    }
    
    public int getHitCount() {
        return StringCache.hitCount;
    }
    
    public void reset() {
        StringCache.hitCount = 0;
        StringCache.accessCount = 0;
        synchronized (StringCache.bcStats) {
            StringCache.bcCache = null;
            StringCache.bcCount = 0;
        }
        synchronized (StringCache.ccStats) {
            StringCache.ccCache = null;
            StringCache.ccCount = 0;
        }
    }
    
    public static String toString(final ByteChunk bc) {
        if (StringCache.bcCache == null) {
            final String value = bc.toStringInternal();
            if (StringCache.byteEnabled && value.length() < StringCache.maxStringSize) {
                synchronized (StringCache.bcStats) {
                    if (StringCache.bcCache != null) {
                        return value;
                    }
                    if (StringCache.bcCount > StringCache.trainThreshold) {
                        final long t1 = System.currentTimeMillis();
                        final TreeMap<Integer, ArrayList<ByteEntry>> tempMap = new TreeMap<Integer, ArrayList<ByteEntry>>();
                        for (final Map.Entry<ByteEntry, int[]> item : StringCache.bcStats.entrySet()) {
                            final ByteEntry entry = item.getKey();
                            final int[] countA = item.getValue();
                            final Integer count = countA[0];
                            ArrayList<ByteEntry> list = tempMap.get(count);
                            if (list == null) {
                                list = new ArrayList<ByteEntry>();
                                tempMap.put(count, list);
                            }
                            list.add(entry);
                        }
                        int size = StringCache.bcStats.size();
                        if (size > StringCache.cacheSize) {
                            size = StringCache.cacheSize;
                        }
                        final ByteEntry[] tempbcCache = new ByteEntry[size];
                        final ByteChunk tempChunk = new ByteChunk();
                        int n = 0;
                        while (n < size) {
                            final Object key = tempMap.lastKey();
                            final ArrayList<ByteEntry> list = tempMap.get(key);
                            for (int i = 0; i < list.size() && n < size; ++n, ++i) {
                                final ByteEntry entry2 = list.get(i);
                                tempChunk.setBytes(entry2.name, 0, entry2.name.length);
                                final int insertPos = findClosest(tempChunk, tempbcCache, n);
                                if (insertPos == n) {
                                    tempbcCache[n + 1] = entry2;
                                }
                                else {
                                    System.arraycopy(tempbcCache, insertPos + 1, tempbcCache, insertPos + 2, n - insertPos - 1);
                                    tempbcCache[insertPos + 1] = entry2;
                                }
                            }
                            tempMap.remove(key);
                        }
                        StringCache.bcCount = 0;
                        StringCache.bcStats.clear();
                        StringCache.bcCache = tempbcCache;
                        if (StringCache.log.isDebugEnabled()) {
                            final long t2 = System.currentTimeMillis();
                            StringCache.log.debug((Object)("ByteCache generation time: " + (t2 - t1) + "ms"));
                        }
                    }
                    else {
                        ++StringCache.bcCount;
                        final ByteEntry entry3 = new ByteEntry();
                        entry3.value = value;
                        int[] count2 = StringCache.bcStats.get(entry3);
                        if (count2 == null) {
                            final int end = bc.getEnd();
                            final int start = bc.getStart();
                            entry3.name = new byte[bc.getLength()];
                            System.arraycopy(bc.getBuffer(), start, entry3.name, 0, end - start);
                            entry3.charset = bc.getCharset();
                            count2 = new int[] { 1 };
                            StringCache.bcStats.put(entry3, count2);
                        }
                        else {
                            ++count2[0];
                        }
                    }
                }
            }
            return value;
        }
        ++StringCache.accessCount;
        final String result = find(bc);
        if (result == null) {
            return bc.toStringInternal();
        }
        ++StringCache.hitCount;
        return result;
    }
    
    public static String toString(final CharChunk cc) {
        if (StringCache.ccCache == null) {
            final String value = cc.toStringInternal();
            if (StringCache.charEnabled && value.length() < StringCache.maxStringSize) {
                synchronized (StringCache.ccStats) {
                    if (StringCache.ccCache != null) {
                        return value;
                    }
                    if (StringCache.ccCount > StringCache.trainThreshold) {
                        final long t1 = System.currentTimeMillis();
                        final TreeMap<Integer, ArrayList<CharEntry>> tempMap = new TreeMap<Integer, ArrayList<CharEntry>>();
                        for (final Map.Entry<CharEntry, int[]> item : StringCache.ccStats.entrySet()) {
                            final CharEntry entry = item.getKey();
                            final int[] countA = item.getValue();
                            final Integer count = countA[0];
                            ArrayList<CharEntry> list = tempMap.get(count);
                            if (list == null) {
                                list = new ArrayList<CharEntry>();
                                tempMap.put(count, list);
                            }
                            list.add(entry);
                        }
                        int size = StringCache.ccStats.size();
                        if (size > StringCache.cacheSize) {
                            size = StringCache.cacheSize;
                        }
                        final CharEntry[] tempccCache = new CharEntry[size];
                        final CharChunk tempChunk = new CharChunk();
                        int n = 0;
                        while (n < size) {
                            final Object key = tempMap.lastKey();
                            final ArrayList<CharEntry> list = tempMap.get(key);
                            for (int i = 0; i < list.size() && n < size; ++n, ++i) {
                                final CharEntry entry2 = list.get(i);
                                tempChunk.setChars(entry2.name, 0, entry2.name.length);
                                final int insertPos = findClosest(tempChunk, tempccCache, n);
                                if (insertPos == n) {
                                    tempccCache[n + 1] = entry2;
                                }
                                else {
                                    System.arraycopy(tempccCache, insertPos + 1, tempccCache, insertPos + 2, n - insertPos - 1);
                                    tempccCache[insertPos + 1] = entry2;
                                }
                            }
                            tempMap.remove(key);
                        }
                        StringCache.ccCount = 0;
                        StringCache.ccStats.clear();
                        StringCache.ccCache = tempccCache;
                        if (StringCache.log.isDebugEnabled()) {
                            final long t2 = System.currentTimeMillis();
                            StringCache.log.debug((Object)("CharCache generation time: " + (t2 - t1) + "ms"));
                        }
                    }
                    else {
                        ++StringCache.ccCount;
                        final CharEntry entry3 = new CharEntry();
                        entry3.value = value;
                        int[] count2 = StringCache.ccStats.get(entry3);
                        if (count2 == null) {
                            final int end = cc.getEnd();
                            final int start = cc.getStart();
                            entry3.name = new char[cc.getLength()];
                            System.arraycopy(cc.getBuffer(), start, entry3.name, 0, end - start);
                            count2 = new int[] { 1 };
                            StringCache.ccStats.put(entry3, count2);
                        }
                        else {
                            ++count2[0];
                        }
                    }
                }
            }
            return value;
        }
        ++StringCache.accessCount;
        final String result = find(cc);
        if (result == null) {
            return cc.toStringInternal();
        }
        ++StringCache.hitCount;
        return result;
    }
    
    protected static final int compare(final ByteChunk name, final byte[] compareTo) {
        int result = 0;
        final byte[] b = name.getBuffer();
        final int start = name.getStart();
        final int end = name.getEnd();
        int len = compareTo.length;
        if (end - start < len) {
            len = end - start;
        }
        for (int i = 0; i < len && result == 0; ++i) {
            if (b[i + start] > compareTo[i]) {
                result = 1;
            }
            else if (b[i + start] < compareTo[i]) {
                result = -1;
            }
        }
        if (result == 0) {
            if (compareTo.length > end - start) {
                result = -1;
            }
            else if (compareTo.length < end - start) {
                result = 1;
            }
        }
        return result;
    }
    
    protected static final String find(final ByteChunk name) {
        final int pos = findClosest(name, StringCache.bcCache, StringCache.bcCache.length);
        if (pos < 0 || compare(name, StringCache.bcCache[pos].name) != 0 || !name.getCharset().equals(StringCache.bcCache[pos].charset)) {
            return null;
        }
        return StringCache.bcCache[pos].value;
    }
    
    protected static final int findClosest(final ByteChunk name, final ByteEntry[] array, final int len) {
        int a = 0;
        int b = len - 1;
        if (b == -1) {
            return -1;
        }
        if (compare(name, array[0].name) < 0) {
            return -1;
        }
        if (b == 0) {
            return 0;
        }
        int i = 0;
        while (true) {
            i = b + a >>> 1;
            final int result = compare(name, array[i].name);
            if (result == 1) {
                a = i;
            }
            else {
                if (result == 0) {
                    return i;
                }
                b = i;
            }
            if (b - a == 1) {
                final int result2 = compare(name, array[b].name);
                if (result2 < 0) {
                    return a;
                }
                return b;
            }
        }
    }
    
    protected static final int compare(final CharChunk name, final char[] compareTo) {
        int result = 0;
        final char[] c = name.getBuffer();
        final int start = name.getStart();
        final int end = name.getEnd();
        int len = compareTo.length;
        if (end - start < len) {
            len = end - start;
        }
        for (int i = 0; i < len && result == 0; ++i) {
            if (c[i + start] > compareTo[i]) {
                result = 1;
            }
            else if (c[i + start] < compareTo[i]) {
                result = -1;
            }
        }
        if (result == 0) {
            if (compareTo.length > end - start) {
                result = -1;
            }
            else if (compareTo.length < end - start) {
                result = 1;
            }
        }
        return result;
    }
    
    protected static final String find(final CharChunk name) {
        final int pos = findClosest(name, StringCache.ccCache, StringCache.ccCache.length);
        if (pos < 0 || compare(name, StringCache.ccCache[pos].name) != 0) {
            return null;
        }
        return StringCache.ccCache[pos].value;
    }
    
    protected static final int findClosest(final CharChunk name, final CharEntry[] array, final int len) {
        int a = 0;
        int b = len - 1;
        if (b == -1) {
            return -1;
        }
        if (compare(name, array[0].name) < 0) {
            return -1;
        }
        if (b == 0) {
            return 0;
        }
        int i = 0;
        while (true) {
            i = b + a >>> 1;
            final int result = compare(name, array[i].name);
            if (result == 1) {
                a = i;
            }
            else {
                if (result == 0) {
                    return i;
                }
                b = i;
            }
            if (b - a == 1) {
                final int result2 = compare(name, array[b].name);
                if (result2 < 0) {
                    return a;
                }
                return b;
            }
        }
    }
    
    static {
        log = LogFactory.getLog((Class)StringCache.class);
        StringCache.byteEnabled = "true".equals(System.getProperty("tomcat.util.buf.StringCache.byte.enabled", "false"));
        StringCache.charEnabled = "true".equals(System.getProperty("tomcat.util.buf.StringCache.char.enabled", "false"));
        StringCache.trainThreshold = Integer.parseInt(System.getProperty("tomcat.util.buf.StringCache.trainThreshold", "20000"));
        StringCache.cacheSize = Integer.parseInt(System.getProperty("tomcat.util.buf.StringCache.cacheSize", "200"));
        StringCache.maxStringSize = Integer.parseInt(System.getProperty("tomcat.util.buf.StringCache.maxStringSize", "128"));
        StringCache.bcStats = new HashMap<ByteEntry, int[]>(StringCache.cacheSize);
        StringCache.bcCount = 0;
        StringCache.bcCache = null;
        StringCache.ccStats = new HashMap<CharEntry, int[]>(StringCache.cacheSize);
        StringCache.ccCount = 0;
        StringCache.ccCache = null;
        StringCache.accessCount = 0;
        StringCache.hitCount = 0;
    }
    
    public static class ByteEntry
    {
        public byte[] name;
        public Charset charset;
        public String value;
        
        public ByteEntry() {
            this.name = null;
            this.charset = null;
            this.value = null;
        }
        
        @Override
        public String toString() {
            return this.value;
        }
        
        @Override
        public int hashCode() {
            return this.value.hashCode();
        }
        
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof ByteEntry && this.value.equals(((ByteEntry)obj).value);
        }
    }
    
    public static class CharEntry
    {
        public char[] name;
        public String value;
        
        public CharEntry() {
            this.name = null;
            this.value = null;
        }
        
        @Override
        public String toString() {
            return this.value;
        }
        
        @Override
        public int hashCode() {
            return this.value.hashCode();
        }
        
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof CharEntry && this.value.equals(((CharEntry)obj).value);
        }
    }
}
