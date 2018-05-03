// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.Serializable;

public final class StackMapEntry implements Cloneable, Serializable
{
    private static final long serialVersionUID = 1L;
    private int byte_code_offset;
    private int number_of_locals;
    private StackMapType[] types_of_locals;
    private int number_of_stack_items;
    private StackMapType[] types_of_stack_items;
    
    StackMapEntry(final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(file.readShort(), file.readShort(), null, -1, null);
        this.types_of_locals = new StackMapType[this.number_of_locals];
        for (int i = 0; i < this.number_of_locals; ++i) {
            this.types_of_locals[i] = new StackMapType(file, constant_pool);
        }
        this.number_of_stack_items = file.readShort();
        this.types_of_stack_items = new StackMapType[this.number_of_stack_items];
        for (int i = 0; i < this.number_of_stack_items; ++i) {
            this.types_of_stack_items[i] = new StackMapType(file, constant_pool);
        }
    }
    
    public StackMapEntry(final int byte_code_offset, final int number_of_locals, final StackMapType[] types_of_locals, final int number_of_stack_items, final StackMapType[] types_of_stack_items) {
        this.byte_code_offset = byte_code_offset;
        this.number_of_locals = number_of_locals;
        this.types_of_locals = types_of_locals;
        this.number_of_stack_items = number_of_stack_items;
        this.types_of_stack_items = types_of_stack_items;
    }
    
    public final void dump(final DataOutputStream file) throws IOException {
        file.writeShort(this.byte_code_offset);
        file.writeShort(this.number_of_locals);
        for (int i = 0; i < this.number_of_locals; ++i) {
            this.types_of_locals[i].dump(file);
        }
        file.writeShort(this.number_of_stack_items);
        for (int i = 0; i < this.number_of_stack_items; ++i) {
            this.types_of_stack_items[i].dump(file);
        }
    }
    
    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder(64);
        buf.append("(offset=").append(this.byte_code_offset);
        if (this.number_of_locals > 0) {
            buf.append(", locals={");
            for (int i = 0; i < this.number_of_locals; ++i) {
                buf.append(this.types_of_locals[i]);
                if (i < this.number_of_locals - 1) {
                    buf.append(", ");
                }
            }
            buf.append("}");
        }
        if (this.number_of_stack_items > 0) {
            buf.append(", stack items={");
            for (int i = 0; i < this.number_of_stack_items; ++i) {
                buf.append(this.types_of_stack_items[i]);
                if (i < this.number_of_stack_items - 1) {
                    buf.append(", ");
                }
            }
            buf.append("}");
        }
        buf.append(")");
        return buf.toString();
    }
    
    public StackMapEntry copy() {
        try {
            return (StackMapEntry)this.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
