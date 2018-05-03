// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantNameAndType extends Constant
{
    private static final long serialVersionUID = 1010506730811368756L;
    private int name_index;
    private int signature_index;
    
    ConstantNameAndType(final DataInput file) throws IOException {
        this(file.readUnsignedShort(), file.readUnsignedShort());
    }
    
    public ConstantNameAndType(final int name_index, final int signature_index) {
        super((byte)12);
        this.name_index = name_index;
        this.signature_index = signature_index;
    }
    
    public final int getNameIndex() {
        return this.name_index;
    }
    
    public final int getSignatureIndex() {
        return this.signature_index;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(name_index = " + this.name_index + ", signature_index = " + this.signature_index + ")";
    }
}
