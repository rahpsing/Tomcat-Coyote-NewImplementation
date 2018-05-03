// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantMethodType extends Constant
{
    private static final long serialVersionUID = 6750768220616618881L;
    private int descriptor_index;
    
    public ConstantMethodType(final ConstantMethodType c) {
        this(c.getDescriptorIndex());
    }
    
    ConstantMethodType(final DataInput file) throws IOException {
        this(file.readUnsignedShort());
    }
    
    public ConstantMethodType(final int descriptor_index) {
        super((byte)16);
        this.descriptor_index = descriptor_index;
    }
    
    public int getDescriptorIndex() {
        return this.descriptor_index;
    }
    
    public void setDescriptorIndex(final int descriptor_index) {
        this.descriptor_index = descriptor_index;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(descriptor_index = " + this.descriptor_index + ")";
    }
}
