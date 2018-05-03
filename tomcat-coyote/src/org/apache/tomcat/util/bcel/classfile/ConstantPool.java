// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import org.apache.tomcat.util.bcel.Constants;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.Serializable;

public class ConstantPool implements Cloneable, Serializable
{
    private static final long serialVersionUID = -6765503791185687014L;
    private int constant_pool_count;
    private Constant[] constant_pool;
    
    ConstantPool(final DataInputStream file) throws IOException, ClassFormatException {
        this.constant_pool_count = file.readUnsignedShort();
        this.constant_pool = new Constant[this.constant_pool_count];
        for (int i = 1; i < this.constant_pool_count; ++i) {
            this.constant_pool[i] = Constant.readConstant(file);
            final byte tag = this.constant_pool[i].getTag();
            if (tag == 6 || tag == 5) {
                ++i;
            }
        }
    }
    
    public String constantToString(Constant c) throws ClassFormatException {
        final byte tag = c.getTag();
        String str = null;
        switch (tag) {
            case 7: {
                final int i = ((ConstantClass)c).getNameIndex();
                c = this.getConstant(i, (byte)1);
                str = Utility.compactClassName(((ConstantUtf8)c).getBytes(), false);
                break;
            }
            case 8: {
                final int i = ((ConstantString)c).getStringIndex();
                c = this.getConstant(i, (byte)1);
                str = "\"" + escape(((ConstantUtf8)c).getBytes()) + "\"";
                break;
            }
            case 1: {
                str = ((ConstantUtf8)c).getBytes();
                break;
            }
            case 6: {
                str = String.valueOf(((ConstantDouble)c).getBytes());
                break;
            }
            case 4: {
                str = String.valueOf(((ConstantFloat)c).getBytes());
                break;
            }
            case 5: {
                str = String.valueOf(((ConstantLong)c).getBytes());
                break;
            }
            case 3: {
                str = String.valueOf(((ConstantInteger)c).getBytes());
                break;
            }
            case 12: {
                str = this.constantToString(((ConstantNameAndType)c).getNameIndex(), (byte)1) + " " + this.constantToString(((ConstantNameAndType)c).getSignatureIndex(), (byte)1);
                break;
            }
            case 9:
            case 10:
            case 11: {
                str = this.constantToString(((ConstantCP)c).getClassIndex(), (byte)7) + "." + this.constantToString(((ConstantCP)c).getNameAndTypeIndex(), (byte)12);
                break;
            }
            default: {
                throw new RuntimeException("Unknown constant type " + tag);
            }
        }
        return str;
    }
    
    private static final String escape(final String str) {
        final int len = str.length();
        final StringBuilder buf = new StringBuilder(len + 5);
        final char[] ch = str.toCharArray();
        for (int i = 0; i < len; ++i) {
            switch (ch[i]) {
                case '\n': {
                    buf.append("\\n");
                    break;
                }
                case '\r': {
                    buf.append("\\r");
                    break;
                }
                case '\t': {
                    buf.append("\\t");
                    break;
                }
                case '\b': {
                    buf.append("\\b");
                    break;
                }
                case '\"': {
                    buf.append("\\\"");
                    break;
                }
                default: {
                    buf.append(ch[i]);
                    break;
                }
            }
        }
        return buf.toString();
    }
    
    public String constantToString(final int index, final byte tag) throws ClassFormatException {
        final Constant c = this.getConstant(index, tag);
        return this.constantToString(c);
    }
    
    public Constant getConstant(final int index) {
        if (index >= this.constant_pool.length || index < 0) {
            throw new ClassFormatException("Invalid constant pool reference: " + index + ". Constant pool size is: " + this.constant_pool.length);
        }
        return this.constant_pool[index];
    }
    
    public Constant getConstant(final int index, final byte tag) throws ClassFormatException {
        final Constant c = this.getConstant(index);
        if (c == null) {
            throw new ClassFormatException("Constant pool at index " + index + " is null.");
        }
        if (c.getTag() != tag) {
            throw new ClassFormatException("Expected class `" + Constants.CONSTANT_NAMES[tag] + "' at index " + index + " and got " + c);
        }
        return c;
    }
    
    public String getConstantString(final int index, final byte tag) throws ClassFormatException {
        Constant c = this.getConstant(index, tag);
        int i = 0;
        switch (tag) {
            case 7: {
                i = ((ConstantClass)c).getNameIndex();
                break;
            }
            case 8: {
                i = ((ConstantString)c).getStringIndex();
                break;
            }
            default: {
                throw new RuntimeException("getConstantString called with illegal tag " + tag);
            }
        }
        c = this.getConstant(i, (byte)1);
        return ((ConstantUtf8)c).getBytes();
    }
    
    public int getLength() {
        return this.constant_pool_count;
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        for (int i = 1; i < this.constant_pool_count; ++i) {
            buf.append(i).append(")").append(this.constant_pool[i]).append("\n");
        }
        return buf.toString();
    }
}
