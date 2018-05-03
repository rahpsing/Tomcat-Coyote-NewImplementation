// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;

public final class Synthetic extends Attribute
{
    private static final long serialVersionUID = -5129612853226360165L;
    private byte[] bytes;
    
    public Synthetic(final int name_index, final int length, final byte[] bytes, final ConstantPool constant_pool) {
        super((byte)7, name_index, length, constant_pool);
        this.bytes = bytes;
    }
    
    Synthetic(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, (byte[])null, constant_pool);
        if (length > 0) {
            file.readFully(this.bytes = new byte[length]);
            System.err.println("Synthetic attribute with length > 0");
        }
    }
    
    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder("Synthetic");
        if (this.length > 0) {
            buf.append(" ").append(Utility.toHexString(this.bytes));
        }
        return buf.toString();
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final Synthetic c = (Synthetic)this.clone();
        if (this.bytes != null) {
            c.bytes = new byte[this.bytes.length];
            System.arraycopy(this.bytes, 0, c.bytes, 0, this.bytes.length);
        }
        c.constant_pool = _constant_pool;
        return c;
    }
}
