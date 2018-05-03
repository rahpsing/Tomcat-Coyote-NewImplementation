// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;

public class LocalVariableTable extends Attribute
{
    private static final long serialVersionUID = -3904314258294133920L;
    private int local_variable_table_length;
    private LocalVariable[] local_variable_table;
    
    public LocalVariableTable(final int name_index, final int length, final LocalVariable[] local_variable_table, final ConstantPool constant_pool) {
        super((byte)5, name_index, length, constant_pool);
        this.setLocalVariableTable(local_variable_table);
    }
    
    LocalVariableTable(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, (LocalVariable[])null, constant_pool);
        this.local_variable_table_length = file.readUnsignedShort();
        this.local_variable_table = new LocalVariable[this.local_variable_table_length];
        for (int i = 0; i < this.local_variable_table_length; ++i) {
            this.local_variable_table[i] = new LocalVariable(file, constant_pool);
        }
    }
    
    @Deprecated
    public final LocalVariable getLocalVariable(final int index) {
        for (int i = 0; i < this.local_variable_table_length; ++i) {
            if (this.local_variable_table[i].getIndex() == index) {
                return this.local_variable_table[i];
            }
        }
        return null;
    }
    
    public final void setLocalVariableTable(final LocalVariable[] local_variable_table) {
        this.local_variable_table = local_variable_table;
        this.local_variable_table_length = ((local_variable_table == null) ? 0 : local_variable_table.length);
    }
    
    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < this.local_variable_table_length; ++i) {
            buf.append(this.local_variable_table[i].toString());
            if (i < this.local_variable_table_length - 1) {
                buf.append('\n');
            }
        }
        return buf.toString();
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final LocalVariableTable c = (LocalVariableTable)this.clone();
        c.local_variable_table = new LocalVariable[this.local_variable_table_length];
        for (int i = 0; i < this.local_variable_table_length; ++i) {
            c.local_variable_table[i] = this.local_variable_table[i].copy();
        }
        c.constant_pool = _constant_pool;
        return c;
    }
}
