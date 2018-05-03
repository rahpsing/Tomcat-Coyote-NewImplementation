// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;

public final class Code extends Attribute
{
    private static final long serialVersionUID = 8936843273318969602L;
    private int max_stack;
    private int max_locals;
    private int code_length;
    private byte[] code;
    private int exception_table_length;
    private CodeException[] exception_table;
    private int attributes_count;
    private Attribute[] attributes;
    
    Code(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, file.readUnsignedShort(), file.readUnsignedShort(), null, null, null, constant_pool);
        this.code_length = file.readInt();
        file.readFully(this.code = new byte[this.code_length]);
        this.exception_table_length = file.readUnsignedShort();
        this.exception_table = new CodeException[this.exception_table_length];
        for (int i = 0; i < this.exception_table_length; ++i) {
            this.exception_table[i] = new CodeException(file);
        }
        this.attributes_count = file.readUnsignedShort();
        this.attributes = new Attribute[this.attributes_count];
        for (int i = 0; i < this.attributes_count; ++i) {
            this.attributes[i] = Attribute.readAttribute(file, constant_pool);
        }
        this.length = length;
    }
    
    public Code(final int name_index, final int length, final int max_stack, final int max_locals, final byte[] code, final CodeException[] exception_table, final Attribute[] attributes, final ConstantPool constant_pool) {
        super((byte)2, name_index, length, constant_pool);
        this.max_stack = max_stack;
        this.max_locals = max_locals;
        this.setCode(code);
        this.setExceptionTable(exception_table);
        this.setAttributes(attributes);
    }
    
    public LocalVariableTable getLocalVariableTable() {
        for (int i = 0; i < this.attributes_count; ++i) {
            if (this.attributes[i] instanceof LocalVariableTable) {
                return (LocalVariableTable)this.attributes[i];
            }
        }
        return null;
    }
    
    private final int getInternalLength() {
        return 8 + this.code_length + 2 + 8 * this.exception_table_length + 2;
    }
    
    private final int calculateLength() {
        int len = 0;
        for (int i = 0; i < this.attributes_count; ++i) {
            len += this.attributes[i].length + 6;
        }
        return len + this.getInternalLength();
    }
    
    public final void setAttributes(final Attribute[] attributes) {
        this.attributes = attributes;
        this.attributes_count = ((attributes == null) ? 0 : attributes.length);
        this.length = this.calculateLength();
    }
    
    public final void setCode(final byte[] code) {
        this.code = code;
        this.code_length = ((code == null) ? 0 : code.length);
        this.length = this.calculateLength();
    }
    
    public final void setExceptionTable(final CodeException[] exception_table) {
        this.exception_table = exception_table;
        this.exception_table_length = ((exception_table == null) ? 0 : exception_table.length);
        this.length = this.calculateLength();
    }
    
    public final String toString(final boolean verbose) {
        final StringBuilder buf = new StringBuilder(100);
        buf.append("Code(max_stack = ").append(this.max_stack).append(", max_locals = ").append(this.max_locals).append(", code_length = ").append(this.code_length).append(")\n").append(Utility.codeToString(this.code, this.constant_pool, 0, -1, verbose));
        if (this.exception_table_length > 0) {
            buf.append("\nException handler(s) = \n").append("From\tTo\tHandler\tType\n");
            for (int i = 0; i < this.exception_table_length; ++i) {
                buf.append(this.exception_table[i].toString(this.constant_pool, verbose)).append("\n");
            }
        }
        if (this.attributes_count > 0) {
            buf.append("\nAttribute(s) = \n");
            for (int i = 0; i < this.attributes_count; ++i) {
                buf.append(this.attributes[i].toString()).append("\n");
            }
        }
        return buf.toString();
    }
    
    @Override
    public final String toString() {
        return this.toString(true);
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final Code c = (Code)this.clone();
        if (this.code != null) {
            c.code = new byte[this.code.length];
            System.arraycopy(this.code, 0, c.code, 0, this.code.length);
        }
        c.constant_pool = _constant_pool;
        c.exception_table = new CodeException[this.exception_table_length];
        for (int i = 0; i < this.exception_table_length; ++i) {
            c.exception_table[i] = this.exception_table[i].copy();
        }
        c.attributes = new Attribute[this.attributes_count];
        for (int i = 0; i < this.attributes_count; ++i) {
            c.attributes[i] = this.attributes[i].copy(_constant_pool);
        }
        return c;
    }
}
