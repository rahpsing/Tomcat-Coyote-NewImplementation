// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;

public final class StackMapTable extends Attribute
{
    private static final long serialVersionUID = -2931695092763099621L;
    private int map_length;
    private StackMapTableEntry[] map;
    
    public StackMapTable(final int name_index, final int length, final StackMapTableEntry[] map, final ConstantPool constant_pool) {
        super((byte)19, name_index, length, constant_pool);
        this.setStackMapTable(map);
    }
    
    StackMapTable(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, (StackMapTableEntry[])null, constant_pool);
        this.map_length = file.readUnsignedShort();
        this.map = new StackMapTableEntry[this.map_length];
        for (int i = 0; i < this.map_length; ++i) {
            this.map[i] = new StackMapTableEntry(file, constant_pool);
        }
    }
    
    public final void setStackMapTable(final StackMapTableEntry[] map) {
        this.map = map;
        this.map_length = ((map == null) ? 0 : map.length);
    }
    
    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder("StackMapTable(");
        for (int i = 0; i < this.map_length; ++i) {
            buf.append(this.map[i].toString());
            if (i < this.map_length - 1) {
                buf.append(", ");
            }
        }
        buf.append(')');
        return buf.toString();
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final StackMapTable c = (StackMapTable)this.clone();
        c.map = new StackMapTableEntry[this.map_length];
        for (int i = 0; i < this.map_length; ++i) {
            c.map[i] = this.map[i].copy();
        }
        c.constant_pool = _constant_pool;
        return c;
    }
}
