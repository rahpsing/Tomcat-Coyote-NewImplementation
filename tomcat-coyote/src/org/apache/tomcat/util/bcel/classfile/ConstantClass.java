// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantClass extends Constant
{
    private static final long serialVersionUID = -6603658849582876642L;
    private int name_index;
    
    ConstantClass(final DataInput file) throws IOException {
        this(file.readUnsignedShort());
    }
    
    public ConstantClass(final int name_index) {
        super((byte)7);
        this.name_index = name_index;
    }
    
    public final int getNameIndex() {
        return this.name_index;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(name_index = " + this.name_index + ")";
    }
}
