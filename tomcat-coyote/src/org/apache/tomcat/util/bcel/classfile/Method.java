// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;
import org.apache.tomcat.util.bcel.util.BCELComparator;

public final class Method extends FieldOrMethod
{
    private static final long serialVersionUID = -7447828891136739513L;
    private static BCELComparator _cmp;
    
    public Method() {
    }
    
    Method(final DataInputStream file, final ConstantPool constant_pool) throws IOException, ClassFormatException {
        super(file, constant_pool);
    }
    
    public final Code getCode() {
        for (int i = 0; i < this.attributes_count; ++i) {
            if (this.attributes[i] instanceof Code) {
                return (Code)this.attributes[i];
            }
        }
        return null;
    }
    
    public final ExceptionTable getExceptionTable() {
        for (int i = 0; i < this.attributes_count; ++i) {
            if (this.attributes[i] instanceof ExceptionTable) {
                return (ExceptionTable)this.attributes[i];
            }
        }
        return null;
    }
    
    public final LocalVariableTable getLocalVariableTable() {
        final Code code = this.getCode();
        if (code == null) {
            return null;
        }
        return code.getLocalVariableTable();
    }
    
    @Override
    public final String toString() {
        final String access = Utility.accessToString(this.access_flags);
        ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.signature_index, (byte)1);
        String signature = c.getBytes();
        c = (ConstantUtf8)this.constant_pool.getConstant(this.name_index, (byte)1);
        final String name = c.getBytes();
        signature = Utility.methodSignatureToString(signature, name, access, true, this.getLocalVariableTable());
        final StringBuilder buf = new StringBuilder(signature);
        for (int i = 0; i < this.attributes_count; ++i) {
            final Attribute a = this.attributes[i];
            if (!(a instanceof Code) && !(a instanceof ExceptionTable)) {
                buf.append(" [").append(a.toString()).append("]");
            }
        }
        final ExceptionTable e = this.getExceptionTable();
        if (e != null) {
            final String str = e.toString();
            if (!str.equals("")) {
                buf.append("\n\t\tthrows ").append(str);
            }
        }
        return buf.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return Method._cmp.equals(this, obj);
    }
    
    @Override
    public int hashCode() {
        return Method._cmp.hashCode(this);
    }
    
    static {
        Method._cmp = new BCELComparator() {
            @Override
            public boolean equals(final Object o1, final Object o2) {
                final Method THIS = (Method)o1;
                final Method THAT = (Method)o2;
                return THIS.getName().equals(THAT.getName()) && THIS.getSignature().equals(THAT.getSignature());
            }
            
            @Override
            public int hashCode(final Object o) {
                final Method THIS = (Method)o;
                return THIS.getSignature().hashCode() ^ THIS.getName().hashCode();
            }
        };
    }
}
