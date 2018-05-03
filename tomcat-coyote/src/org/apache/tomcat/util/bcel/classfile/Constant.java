// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;
import org.apache.tomcat.util.bcel.Constants;
import org.apache.tomcat.util.bcel.util.BCELComparator;
import java.io.Serializable;

public abstract class Constant implements Cloneable, Serializable
{
    private static final long serialVersionUID = 2827409182154809454L;
    private static BCELComparator _cmp;
    protected byte tag;
    
    Constant(final byte tag) {
        this.tag = tag;
    }
    
    public final byte getTag() {
        return this.tag;
    }
    
    @Override
    public String toString() {
        return Constants.CONSTANT_NAMES[this.tag] + "[" + this.tag + "]";
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    static final Constant readConstant(final DataInputStream file) throws IOException, ClassFormatException {
        final byte b = file.readByte();
        switch (b) {
            case 7: {
                return new ConstantClass(file);
            }
            case 9: {
                return new ConstantFieldref(file);
            }
            case 10: {
                return new ConstantMethodref(file);
            }
            case 11: {
                return new ConstantInterfaceMethodref(file);
            }
            case 8: {
                return new ConstantString(file);
            }
            case 3: {
                return new ConstantInteger(file);
            }
            case 4: {
                return new ConstantFloat(file);
            }
            case 5: {
                return new ConstantLong(file);
            }
            case 6: {
                return new ConstantDouble(file);
            }
            case 12: {
                return new ConstantNameAndType(file);
            }
            case 1: {
                return ConstantUtf8.getInstance(file);
            }
            case 15: {
                return new ConstantMethodHandle(file);
            }
            case 16: {
                return new ConstantMethodType(file);
            }
            case 18: {
                return new ConstantInvokeDynamic(file);
            }
            default: {
                throw new ClassFormatException("Invalid byte tag in constant pool: " + b);
            }
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        return Constant._cmp.equals(this, obj);
    }
    
    @Override
    public int hashCode() {
        return Constant._cmp.hashCode(this);
    }
    
    static {
        Constant._cmp = new BCELComparator() {
            @Override
            public boolean equals(final Object o1, final Object o2) {
                final Constant THIS = (Constant)o1;
                final Constant THAT = (Constant)o2;
                return THIS.toString().equals(THAT.toString());
            }
            
            @Override
            public int hashCode(final Object o) {
                final Constant THIS = (Constant)o;
                return THIS.toString().hashCode();
            }
        };
    }
}
