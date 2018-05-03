// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import org.apache.tomcat.util.bcel.Constants;
import java.io.IOException;
import java.io.DataInputStream;

public final class Deprecated extends Attribute
{
    private static final long serialVersionUID = 8499975360019639912L;
    private byte[] bytes;
    
    public Deprecated(final int name_index, final int length, final byte[] bytes, final ConstantPool constant_pool) {
        super((byte)8, name_index, length, constant_pool);
        this.bytes = bytes;
    }
    
    Deprecated(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, (byte[])null, constant_pool);
        if (length > 0) {
            file.readFully(this.bytes = new byte[length]);
            System.err.println("Deprecated attribute with length > 0");
        }
    }
    
    @Override
    public final String toString() {
        return Constants.ATTRIBUTE_NAMES[8];
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final Deprecated c = (Deprecated)this.clone();
        if (this.bytes != null) {
            c.bytes = new byte[this.bytes.length];
            System.arraycopy(this.bytes, 0, c.bytes, 0, this.bytes.length);
        }
        c.constant_pool = _constant_pool;
        return c;
    }
}
