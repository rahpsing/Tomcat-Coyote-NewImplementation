// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataOutputStream;

public class EnumElementValue extends ElementValue
{
    private int typeIdx;
    private int valueIdx;
    
    public EnumElementValue(final int type, final int typeIdx, final int valueIdx, final ConstantPool cpool) {
        super(type, cpool);
        if (type != 101) {
            throw new RuntimeException("Only element values of type enum can be built with this ctor - type specified: " + type);
        }
        this.typeIdx = typeIdx;
        this.valueIdx = valueIdx;
    }
    
    @Override
    public void dump(final DataOutputStream dos) throws IOException {
        dos.writeByte(this.type);
        dos.writeShort(this.typeIdx);
        dos.writeShort(this.valueIdx);
    }
    
    @Override
    public String stringifyValue() {
        final ConstantUtf8 cu8 = (ConstantUtf8)this.cpool.getConstant(this.valueIdx, (byte)1);
        return cu8.getBytes();
    }
}
