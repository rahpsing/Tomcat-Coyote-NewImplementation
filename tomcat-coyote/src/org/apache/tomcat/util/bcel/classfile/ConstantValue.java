// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantValue extends Attribute
{
    private static final long serialVersionUID = -388222612752527969L;
    private int constantvalue_index;
    
    ConstantValue(final int name_index, final int length, final DataInput file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, file.readUnsignedShort(), constant_pool);
    }
    
    public ConstantValue(final int name_index, final int length, final int constantvalue_index, final ConstantPool constant_pool) {
        super((byte)1, name_index, length, constant_pool);
        this.constantvalue_index = constantvalue_index;
    }
    
    @Override
    public final String toString() {
        Constant c = this.constant_pool.getConstant(this.constantvalue_index);
        String buf = null;
        switch (c.getTag()) {
            case 5: {
                buf = String.valueOf(((ConstantLong)c).getBytes());
                break;
            }
            case 4: {
                buf = String.valueOf(((ConstantFloat)c).getBytes());
                break;
            }
            case 6: {
                buf = String.valueOf(((ConstantDouble)c).getBytes());
                break;
            }
            case 3: {
                buf = String.valueOf(((ConstantInteger)c).getBytes());
                break;
            }
            case 8: {
                final int i = ((ConstantString)c).getStringIndex();
                c = this.constant_pool.getConstant(i, (byte)1);
                buf = "\"" + Utility.convertString(((ConstantUtf8)c).getBytes()) + "\"";
                break;
            }
            default: {
                throw new IllegalStateException("Type of ConstValue invalid: " + c);
            }
        }
        return buf;
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final ConstantValue c = (ConstantValue)this.clone();
        c.constant_pool = _constant_pool;
        return c;
    }
}
