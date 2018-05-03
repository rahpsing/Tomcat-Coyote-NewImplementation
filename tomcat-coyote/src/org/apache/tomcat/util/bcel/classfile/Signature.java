// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class Signature extends Attribute
{
    private static final long serialVersionUID = 7493781777025829964L;
    private int signature_index;
    
    Signature(final int name_index, final int length, final DataInput file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, file.readUnsignedShort(), constant_pool);
    }
    
    public Signature(final int name_index, final int length, final int signature_index, final ConstantPool constant_pool) {
        super((byte)10, name_index, length, constant_pool);
        this.signature_index = signature_index;
    }
    
    public final String getSignature() {
        final ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.signature_index, (byte)1);
        return c.getBytes();
    }
    
    @Override
    public final String toString() {
        final String s = this.getSignature();
        return "Signature(" + s + ")";
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        return (Signature)this.clone();
    }
}
