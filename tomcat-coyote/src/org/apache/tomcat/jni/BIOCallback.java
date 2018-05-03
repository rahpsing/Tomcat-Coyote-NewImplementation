// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.jni;

public interface BIOCallback
{
    int write(final byte[] p0);
    
    int read(final byte[] p0);
    
    int puts(final String p0);
    
    String gets(final int p0);
}
