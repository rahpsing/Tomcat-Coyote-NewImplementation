// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;

public class LocalVariableTypeTable extends Attribute
{
    private static final long serialVersionUID = -5466082154076451597L;
    private int local_variable_type_table_length;
    private LocalVariable[] local_variable_type_table;
    
    public LocalVariableTypeTable(final int name_index, final int length, final LocalVariable[] local_variable_table, final ConstantPool constant_pool) {
        super((byte)17, name_index, length, constant_pool);
        this.setLocalVariableTable(local_variable_table);
    }
    
    LocalVariableTypeTable(final int nameIdx, final int len, final DataInputStream dis, final ConstantPool cpool) throws IOException {
        this(nameIdx, len, (LocalVariable[])null, cpool);
        this.local_variable_type_table_length = dis.readUnsignedShort();
        this.local_variable_type_table = new LocalVariable[this.local_variable_type_table_length];
        for (int i = 0; i < this.local_variable_type_table_length; ++i) {
            this.local_variable_type_table[i] = new LocalVariable(dis, cpool);
        }
    }
    
    public final void setLocalVariableTable(final LocalVariable[] local_variable_table) {
        this.local_variable_type_table = local_variable_table;
        this.local_variable_type_table_length = ((local_variable_table == null) ? 0 : local_variable_table.length);
    }
    
    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < this.local_variable_type_table_length; ++i) {
            buf.append(this.local_variable_type_table[i].toString());
            if (i < this.local_variable_type_table_length - 1) {
                buf.append('\n');
            }
        }
        return buf.toString();
    }
    
    @Override
    public Attribute copy(final ConstantPool constant_pool) {
        final LocalVariableTypeTable c = (LocalVariableTypeTable)this.clone();
        c.local_variable_type_table = new LocalVariable[this.local_variable_type_table_length];
        for (int i = 0; i < this.local_variable_type_table_length; ++i) {
            c.local_variable_type_table[i] = this.local_variable_type_table[i].copy();
        }
        c.constant_pool = constant_pool;
        return c;
    }
}
