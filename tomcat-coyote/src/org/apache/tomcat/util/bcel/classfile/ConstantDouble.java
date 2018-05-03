// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantDouble extends Constant
{
    private static final long serialVersionUID = 3450743772468544760L;
    private double bytes;
    
    public ConstantDouble(final double bytes) {
        super((byte)6);
        this.bytes = bytes;
    }
    
    ConstantDouble(final DataInput file) throws IOException {
        this(file.readDouble());
    }
    
    public final double getBytes() {
        return this.bytes;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(bytes = " + this.bytes + ")";
    }
}
