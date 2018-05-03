// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.Serializable;

public final class StackMapTableEntry implements Cloneable, Serializable
{
    private static final long serialVersionUID = 1L;
    private int frame_type;
    private int byte_code_offset_delta;
    private int number_of_locals;
    private StackMapType[] types_of_locals;
    private int number_of_stack_items;
    private StackMapType[] types_of_stack_items;
    
    StackMapTableEntry(final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(file.read(), -1, -1, null, -1, null);
        if (this.frame_type >= 0 && this.frame_type <= 63) {
            this.byte_code_offset_delta = this.frame_type - 0;
        }
        else if (this.frame_type >= 64 && this.frame_type <= 127) {
            this.byte_code_offset_delta = this.frame_type - 64;
            this.number_of_stack_items = 1;
            (this.types_of_stack_items = new StackMapType[1])[0] = new StackMapType(file, constant_pool);
        }
        else if (this.frame_type == 247) {
            this.byte_code_offset_delta = file.readShort();
            this.number_of_stack_items = 1;
            (this.types_of_stack_items = new StackMapType[1])[0] = new StackMapType(file, constant_pool);
        }
        else if (this.frame_type >= 248 && this.frame_type <= 250) {
            this.byte_code_offset_delta = file.readShort();
        }
        else if (this.frame_type == 251) {
            this.byte_code_offset_delta = file.readShort();
        }
        else if (this.frame_type >= 252 && this.frame_type <= 254) {
            this.byte_code_offset_delta = file.readShort();
            this.number_of_locals = this.frame_type - 251;
            this.types_of_locals = new StackMapType[this.number_of_locals];
            for (int i = 0; i < this.number_of_locals; ++i) {
                this.types_of_locals[i] = new StackMapType(file, constant_pool);
            }
        }
        else {
            if (this.frame_type != 255) {
                throw new ClassFormatException("Invalid frame type found while parsing stack map table: " + this.frame_type);
            }
            this.byte_code_offset_delta = file.readShort();
            this.number_of_locals = file.readShort();
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
    }
    
    public StackMapTableEntry(final int tag, final int byte_code_offset_delta, final int number_of_locals, final StackMapType[] types_of_locals, final int number_of_stack_items, final StackMapType[] types_of_stack_items) {
        this.frame_type = tag;
        this.byte_code_offset_delta = byte_code_offset_delta;
        this.number_of_locals = number_of_locals;
        this.types_of_locals = types_of_locals;
        this.number_of_stack_items = number_of_stack_items;
        this.types_of_stack_items = types_of_stack_items;
    }
    
    public final void dump(final DataOutputStream file) throws IOException {
        file.write(this.frame_type);
        if (this.frame_type < 0 || this.frame_type > 63) {
            if (this.frame_type >= 64 && this.frame_type <= 127) {
                this.types_of_stack_items[0].dump(file);
            }
            else if (this.frame_type == 247) {
                file.writeShort(this.byte_code_offset_delta);
                this.types_of_stack_items[0].dump(file);
            }
            else if (this.frame_type >= 248 && this.frame_type <= 250) {
                file.writeShort(this.byte_code_offset_delta);
            }
            else if (this.frame_type == 251) {
                file.writeShort(this.byte_code_offset_delta);
            }
            else if (this.frame_type >= 252 && this.frame_type <= 254) {
                file.writeShort(this.byte_code_offset_delta);
                for (int i = 0; i < this.number_of_locals; ++i) {
                    this.types_of_locals[i].dump(file);
                }
            }
            else {
                if (this.frame_type != 255) {
                    throw new ClassFormatException("Invalid Stack map table tag: " + this.frame_type);
                }
                file.writeShort(this.byte_code_offset_delta);
                file.writeShort(this.number_of_locals);
                for (int i = 0; i < this.number_of_locals; ++i) {
                    this.types_of_locals[i].dump(file);
                }
                file.writeShort(this.number_of_stack_items);
                for (int i = 0; i < this.number_of_stack_items; ++i) {
                    this.types_of_stack_items[i].dump(file);
                }
            }
        }
    }
    
    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder(64);
        buf.append("(");
        if (this.frame_type >= 0 && this.frame_type <= 63) {
            buf.append("SAME");
        }
        else if (this.frame_type >= 64 && this.frame_type <= 127) {
            buf.append("SAME_LOCALS_1_STACK");
        }
        else if (this.frame_type == 247) {
            buf.append("SAME_LOCALS_1_STACK_EXTENDED");
        }
        else if (this.frame_type >= 248 && this.frame_type <= 250) {
            buf.append("CHOP " + (251 - this.frame_type));
        }
        else if (this.frame_type == 251) {
            buf.append("SAME_EXTENDED");
        }
        else if (this.frame_type >= 252 && this.frame_type <= 254) {
            buf.append("APPEND " + (this.frame_type - 251));
        }
        else if (this.frame_type == 255) {
            buf.append("FULL");
        }
        else {
            buf.append("UNKNOWN");
        }
        buf.append(", offset delta=").append(this.byte_code_offset_delta);
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
    
    public StackMapTableEntry copy() {
        try {
            return (StackMapTableEntry)this.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
