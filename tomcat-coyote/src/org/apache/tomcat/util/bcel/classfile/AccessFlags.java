// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.Serializable;

public abstract class AccessFlags implements Serializable
{
    private static final long serialVersionUID = 2548932939969293935L;
    protected int access_flags;
    
    public final int getAccessFlags() {
        return this.access_flags;
    }
}
