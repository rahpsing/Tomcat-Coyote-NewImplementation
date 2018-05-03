// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;

public final class ExceptionTable extends Attribute
{
    private static final long serialVersionUID = -5109672682663772900L;
    private int number_of_exceptions;
    private int[] exception_index_table;
    
    public ExceptionTable(final int name_index, final int length, final int[] exception_index_table, final ConstantPool constant_pool) {
        super((byte)3, name_index, length, constant_pool);
        this.setExceptionIndexTable(exception_index_table);
    }
    
    ExceptionTable(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, (int[])null, constant_pool);
        this.number_of_exceptions = file.readUnsignedShort();
        this.exception_index_table = new int[this.number_of_exceptions];
        for (int i = 0; i < this.number_of_exceptions; ++i) {
            this.exception_index_table[i] = file.readUnsignedShort();
        }
    }
    
    public final void setExceptionIndexTable(final int[] exception_index_table) {
        this.exception_index_table = exception_index_table;
        this.number_of_exceptions = ((exception_index_table == null) ? 0 : exception_index_table.length);
    }
    
    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < this.number_of_exceptions; ++i) {
            final String str = this.constant_pool.getConstantString(this.exception_index_table[i], (byte)7);
            buf.append(Utility.compactClassName(str, false));
            if (i < this.number_of_exceptions - 1) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final ExceptionTable c = (ExceptionTable)this.clone();
        if (this.exception_index_table != null) {
            c.exception_index_table = new int[this.exception_index_table.length];
            System.arraycopy(this.exception_index_table, 0, c.exception_index_table, 0, this.exception_index_table.length);
        }
        c.constant_pool = _constant_pool;
        return c;
    }
}
