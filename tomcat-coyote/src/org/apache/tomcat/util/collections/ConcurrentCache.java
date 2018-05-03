// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.collections;

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public final class ConcurrentCache<K, V>
{
    private final int size;
    private final Map<K, V> eden;
    private final Map<K, V> longterm;
    
    public ConcurrentCache(final int size) {
        this.size = size;
        this.eden = new ConcurrentHashMap<K, V>(size);
        this.longterm = new WeakHashMap<K, V>(size);
    }
    
    public V get(final K k) {
        V v = this.eden.get(k);
        if (v == null) {
            synchronized (this.longterm) {
                v = this.longterm.get(k);
            }
            if (v != null) {
                this.eden.put(k, v);
            }
        }
        return v;
    }
    
    public void put(final K k, final V v) {
        if (this.eden.size() >= this.size) {
            synchronized (this.longterm) {
                this.longterm.putAll((Map<? extends K, ? extends V>)this.eden);
            }
            this.eden.clear();
        }
        this.eden.put(k, v);
    }
}
