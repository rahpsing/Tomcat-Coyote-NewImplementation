// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantFloat extends Constant
{
    private static final long serialVersionUID = 8301269629885378651L;
    private float bytes;
    
    public ConstantFloat(final float bytes) {
        super((byte)4);
        this.bytes = bytes;
    }
    
    ConstantFloat(final DataInput file) throws IOException {
        this(file.readFloat());
    }
    
    public final float getBytes() {
        return this.bytes;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(bytes = " + this.bytes + ")";
    }
}
