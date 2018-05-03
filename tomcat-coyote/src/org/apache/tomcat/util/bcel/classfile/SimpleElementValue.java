// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataOutputStream;

public class SimpleElementValue extends ElementValue
{
    private int index;
    
    public SimpleElementValue(final int type, final int index, final ConstantPool cpool) {
        super(type, cpool);
        this.index = index;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    @Override
    public String toString() {
        return this.stringifyValue();
    }
    
    @Override
    public String stringifyValue() {
        switch (this.type) {
            case 73: {
                final ConstantInteger c = (ConstantInteger)this.cpool.getConstant(this.getIndex(), (byte)3);
                return Integer.toString(c.getBytes());
            }
            case 74: {
                final ConstantLong j = (ConstantLong)this.cpool.getConstant(this.getIndex(), (byte)5);
                return Long.toString(j.getBytes());
            }
            case 68: {
                final ConstantDouble d = (ConstantDouble)this.cpool.getConstant(this.getIndex(), (byte)6);
                return Double.toString(d.getBytes());
            }
            case 70: {
                final ConstantFloat f = (ConstantFloat)this.cpool.getConstant(this.getIndex(), (byte)4);
                return Float.toString(f.getBytes());
            }
            case 83: {
                final ConstantInteger s = (ConstantInteger)this.cpool.getConstant(this.getIndex(), (byte)3);
                return Integer.toString(s.getBytes());
            }
            case 66: {
                final ConstantInteger b = (ConstantInteger)this.cpool.getConstant(this.getIndex(), (byte)3);
                return Integer.toString(b.getBytes());
            }
            case 67: {
                final ConstantInteger ch = (ConstantInteger)this.cpool.getConstant(this.getIndex(), (byte)3);
                return String.valueOf((char)ch.getBytes());
            }
            case 90: {
                final ConstantInteger bo = (ConstantInteger)this.cpool.getConstant(this.getIndex(), (byte)3);
                if (bo.getBytes() == 0) {
                    return "false";
                }
                return "true";
            }
            case 115: {
                final ConstantUtf8 cu8 = (ConstantUtf8)this.cpool.getConstant(this.getIndex(), (byte)1);
                return cu8.getBytes();
            }
            default: {
                throw new RuntimeException("SimpleElementValue class does not know how to stringify type " + this.type);
            }
        }
    }
    
    @Override
    public void dump(final DataOutputStream dos) throws IOException {
        dos.writeByte(this.type);
        switch (this.type) {
            case 66:
            case 67:
            case 68:
            case 70:
            case 73:
            case 74:
            case 83:
            case 90:
            case 115: {
                dos.writeShort(this.getIndex());
            }
            default: {
                throw new RuntimeException("SimpleElementValue doesnt know how to write out type " + this.type);
            }
        }
    }
}
