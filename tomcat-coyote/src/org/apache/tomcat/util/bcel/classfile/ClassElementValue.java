// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataOutputStream;

public class ClassElementValue extends ElementValue
{
    private int idx;
    
    public ClassElementValue(final int type, final int idx, final ConstantPool cpool) {
        super(type, cpool);
        this.idx = idx;
    }
    
    @Override
    public String stringifyValue() {
        final ConstantUtf8 cu8 = (ConstantUtf8)this.cpool.getConstant(this.idx, (byte)1);
        return cu8.getBytes();
    }
    
    @Override
    public void dump(final DataOutputStream dos) throws IOException {
        dos.writeByte(this.type);
        dos.writeShort(this.idx);
    }
}
