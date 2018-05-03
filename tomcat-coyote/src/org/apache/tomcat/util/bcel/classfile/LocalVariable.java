// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInput;
import java.io.Serializable;
import org.apache.tomcat.util.bcel.Constants;

public final class LocalVariable implements Constants, Cloneable, Serializable
{
    private static final long serialVersionUID = -914189896372081589L;
    private int start_pc;
    private int length;
    private int name_index;
    private int signature_index;
    private int index;
    private ConstantPool constant_pool;
    
    LocalVariable(final DataInput file, final ConstantPool constant_pool) throws IOException {
        this(file.readUnsignedShort(), file.readUnsignedShort(), file.readUnsignedShort(), file.readUnsignedShort(), file.readUnsignedShort(), constant_pool);
    }
    
    public LocalVariable(final int start_pc, final int length, final int name_index, final int signature_index, final int index, final ConstantPool constant_pool) {
        this.start_pc = start_pc;
        this.length = length;
        this.name_index = name_index;
        this.signature_index = signature_index;
        this.index = index;
        this.constant_pool = constant_pool;
    }
    
    public final void dump(final DataOutputStream file) throws IOException {
        file.writeShort(this.start_pc);
        file.writeShort(this.length);
        file.writeShort(this.name_index);
        file.writeShort(this.signature_index);
        file.writeShort(this.index);
    }
    
    public final String getName() {
        final ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.name_index, (byte)1);
        return c.getBytes();
    }
    
    public final String getSignature() {
        final ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.signature_index, (byte)1);
        return c.getBytes();
    }
    
    public final int getIndex() {
        return this.index;
    }
    
    @Override
    public final String toString() {
        final String name = this.getName();
        final String signature = Utility.signatureToString(this.getSignature());
        return "LocalVariable(start_pc = " + this.start_pc + ", length = " + this.length + ", index = " + this.index + ":" + signature + " " + name + ")";
    }
    
    public LocalVariable copy() {
        try {
            return (LocalVariable)this.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
