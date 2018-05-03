// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public abstract class ConstantCP extends Constant
{
    private static final long serialVersionUID = 7282382456501145526L;
    protected int class_index;
    protected int name_and_type_index;
    
    ConstantCP(final byte tag, final DataInput file) throws IOException {
        this(tag, file.readUnsignedShort(), file.readUnsignedShort());
    }
    
    protected ConstantCP(final byte tag, final int class_index, final int name_and_type_index) {
        super(tag);
        this.class_index = class_index;
        this.name_and_type_index = name_and_type_index;
    }
    
    public final int getClassIndex() {
        return this.class_index;
    }
    
    public final int getNameAndTypeIndex() {
        return this.name_and_type_index;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(class_index = " + this.class_index + ", name_and_type_index = " + this.name_and_type_index + ")";
    }
}
