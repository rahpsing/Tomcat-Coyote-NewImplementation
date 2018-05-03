// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;

public final class LineNumberTable extends Attribute
{
    private static final long serialVersionUID = 6585122636118666124L;
    private int line_number_table_length;
    private LineNumber[] line_number_table;
    
    public LineNumberTable(final int name_index, final int length, final LineNumber[] line_number_table, final ConstantPool constant_pool) {
        super((byte)4, name_index, length, constant_pool);
        this.setLineNumberTable(line_number_table);
    }
    
    LineNumberTable(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, (LineNumber[])null, constant_pool);
        this.line_number_table_length = file.readUnsignedShort();
        this.line_number_table = new LineNumber[this.line_number_table_length];
        for (int i = 0; i < this.line_number_table_length; ++i) {
            this.line_number_table[i] = new LineNumber(file);
        }
    }
    
    public final void setLineNumberTable(final LineNumber[] line_number_table) {
        this.line_number_table = line_number_table;
        this.line_number_table_length = ((line_number_table == null) ? 0 : line_number_table.length);
    }
    
    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder();
        final StringBuilder line = new StringBuilder();
        final String newLine = System.getProperty("line.separator", "\n");
        for (int i = 0; i < this.line_number_table_length; ++i) {
            line.append(this.line_number_table[i].toString());
            if (i < this.line_number_table_length - 1) {
                line.append(", ");
            }
            if (line.length() > 72) {
                line.append(newLine);
                buf.append(line.toString());
                line.setLength(0);
            }
        }
        buf.append((CharSequence)line);
        return buf.toString();
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final LineNumberTable c = (LineNumberTable)this.clone();
        c.line_number_table = new LineNumber[this.line_number_table_length];
        for (int i = 0; i < this.line_number_table_length; ++i) {
            c.line_number_table[i] = this.line_number_table[i].copy();
        }
        c.constant_pool = _constant_pool;
        return c;
    }
}
