// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInput;
import java.io.Serializable;
import org.apache.tomcat.util.bcel.Constants;

public final class CodeException implements Cloneable, Constants, Serializable
{
    private static final long serialVersionUID = -6351674720658890686L;
    private int start_pc;
    private int end_pc;
    private int handler_pc;
    private int catch_type;
    
    CodeException(final DataInput file) throws IOException {
        this(file.readUnsignedShort(), file.readUnsignedShort(), file.readUnsignedShort(), file.readUnsignedShort());
    }
    
    public CodeException(final int start_pc, final int end_pc, final int handler_pc, final int catch_type) {
        this.start_pc = start_pc;
        this.end_pc = end_pc;
        this.handler_pc = handler_pc;
        this.catch_type = catch_type;
    }
    
    public final void dump(final DataOutputStream file) throws IOException {
        file.writeShort(this.start_pc);
        file.writeShort(this.end_pc);
        file.writeShort(this.handler_pc);
        file.writeShort(this.catch_type);
    }
    
    @Override
    public final String toString() {
        return "CodeException(start_pc = " + this.start_pc + ", end_pc = " + this.end_pc + ", handler_pc = " + this.handler_pc + ", catch_type = " + this.catch_type + ")";
    }
    
    public final String toString(final ConstantPool cp, final boolean verbose) {
        String str;
        if (this.catch_type == 0) {
            str = "<Any exception>(0)";
        }
        else {
            str = Utility.compactClassName(cp.getConstantString(this.catch_type, (byte)7), false) + (verbose ? ("(" + this.catch_type + ")") : "");
        }
        return this.start_pc + "\t" + this.end_pc + "\t" + this.handler_pc + "\t" + str;
    }
    
    public CodeException copy() {
        try {
            return (CodeException)this.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
