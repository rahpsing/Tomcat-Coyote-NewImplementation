// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util;

@Deprecated
public class MutableInteger
{
    protected int value;
    
    public MutableInteger() {
        this.value = 0;
    }
    
    public MutableInteger(final int val) {
        this.value = 0;
        this.value = val;
    }
    
    public int get() {
        return this.value;
    }
    
    public void set(final int val) {
        this.value = val;
    }
}
