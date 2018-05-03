// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.util.HashMap;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.Map;

public final class Unknown extends Attribute
{
    private static final long serialVersionUID = -4152422704743201314L;
    private byte[] bytes;
    private String name;
    private static final Map<String, Unknown> unknown_attributes;
    
    public Unknown(final int name_index, final int length, final byte[] bytes, final ConstantPool constant_pool) {
        super((byte)(-1), name_index, length, constant_pool);
        this.bytes = bytes;
        this.name = ((ConstantUtf8)constant_pool.getConstant(name_index, (byte)1)).getBytes();
        Unknown.unknown_attributes.put(this.name, this);
    }
    
    Unknown(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, (byte[])null, constant_pool);
        if (length > 0) {
            file.readFully(this.bytes = new byte[length]);
        }
    }
    
    @Override
    public final String getName() {
        return this.name;
    }
    
    @Override
    public final String toString() {
        if (this.length == 0 || this.bytes == null) {
            return "(Unknown attribute " + this.name + ")";
        }
        String hex;
        if (this.length > 10) {
            final byte[] tmp = new byte[10];
            System.arraycopy(this.bytes, 0, tmp, 0, 10);
            hex = Utility.toHexString(tmp) + "... (truncated)";
        }
        else {
            hex = Utility.toHexString(this.bytes);
        }
        return "(Unknown attribute " + this.name + ": " + hex + ")";
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final Unknown c = (Unknown)this.clone();
        if (this.bytes != null) {
            c.bytes = new byte[this.bytes.length];
            System.arraycopy(this.bytes, 0, c.bytes, 0, this.bytes.length);
        }
        c.constant_pool = _constant_pool;
        return c;
    }
    
    static {
        unknown_attributes = new HashMap<String, Unknown>();
    }
}
