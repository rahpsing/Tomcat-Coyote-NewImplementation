// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantString extends Constant
{
    private static final long serialVersionUID = 2809338612858801341L;
    private int string_index;
    
    ConstantString(final DataInput file) throws IOException {
        this(file.readUnsignedShort());
    }
    
    public ConstantString(final int string_index) {
        super((byte)8);
        this.string_index = string_index;
    }
    
    public final int getStringIndex() {
        return this.string_index;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(string_index = " + this.string_index + ")";
    }
}
