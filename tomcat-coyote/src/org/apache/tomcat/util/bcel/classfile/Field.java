// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;
import org.apache.tomcat.util.bcel.util.BCELComparator;

public final class Field extends FieldOrMethod
{
    private static final long serialVersionUID = 2646214544240375238L;
    private static BCELComparator _cmp;
    
    Field(final DataInputStream file, final ConstantPool constant_pool) throws IOException, ClassFormatException {
        super(file, constant_pool);
    }
    
    public final ConstantValue getConstantValue() {
        for (int i = 0; i < this.attributes_count; ++i) {
            if (this.attributes[i].getTag() == 1) {
                return (ConstantValue)this.attributes[i];
            }
        }
        return null;
    }
    
    @Override
    public final String toString() {
        String access = Utility.accessToString(this.access_flags);
        access = (access.equals("") ? "" : (access + " "));
        final String signature = Utility.signatureToString(this.getSignature());
        final String name = this.getName();
        final StringBuilder buf = new StringBuilder(64);
        buf.append(access).append(signature).append(" ").append(name);
        final ConstantValue cv = this.getConstantValue();
        if (cv != null) {
            buf.append(" = ").append(cv);
        }
        for (int i = 0; i < this.attributes_count; ++i) {
            final Attribute a = this.attributes[i];
            if (!(a instanceof ConstantValue)) {
                buf.append(" [").append(a.toString()).append("]");
            }
        }
        return buf.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return Field._cmp.equals(this, obj);
    }
    
    @Override
    public int hashCode() {
        return Field._cmp.hashCode(this);
    }
    
    static {
        Field._cmp = new BCELComparator() {
            @Override
            public boolean equals(final Object o1, final Object o2) {
                final Field THIS = (Field)o1;
                final Field THAT = (Field)o2;
                return THIS.getName().equals(THAT.getName()) && THIS.getSignature().equals(THAT.getSignature());
            }
            
            @Override
            public int hashCode(final Object o) {
                final Field THIS = (Field)o;
                return THIS.getSignature().hashCode() ^ THIS.getName().hashCode();
            }
        };
    }
}
