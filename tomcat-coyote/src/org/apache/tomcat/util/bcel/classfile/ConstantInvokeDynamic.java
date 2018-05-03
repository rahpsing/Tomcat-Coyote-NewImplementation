// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantInvokeDynamic extends Constant
{
    private static final long serialVersionUID = 4310367359017396174L;
    private int bootstrap_method_attr_index;
    private int name_and_type_index;
    
    public ConstantInvokeDynamic(final ConstantInvokeDynamic c) {
        this(c.getBootstrapMethodAttrIndex(), c.getNameAndTypeIndex());
    }
    
    ConstantInvokeDynamic(final DataInput file) throws IOException {
        this(file.readUnsignedShort(), file.readUnsignedShort());
    }
    
    public ConstantInvokeDynamic(final int bootstrap_method_attr_index, final int name_and_type_index) {
        super((byte)18);
        this.bootstrap_method_attr_index = bootstrap_method_attr_index;
        this.name_and_type_index = name_and_type_index;
    }
    
    public int getBootstrapMethodAttrIndex() {
        return this.bootstrap_method_attr_index;
    }
    
    public void setBootstrapMethodAttrIndex(final int bootstrap_method_attr_index) {
        this.bootstrap_method_attr_index = bootstrap_method_attr_index;
    }
    
    public int getNameAndTypeIndex() {
        return this.name_and_type_index;
    }
    
    public void setNameAndTypeIndex(final int name_and_type_index) {
        this.name_and_type_index = name_and_type_index;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(bootstrap_method_attr_index = " + this.bootstrap_method_attr_index + ", name_and_type_index = " + this.name_and_type_index + ")";
    }
}
