// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInput;
import java.io.Serializable;

public final class LineNumber implements Cloneable, Serializable
{
    private static final long serialVersionUID = 3393830630264494355L;
    private int start_pc;
    private int line_number;
    
    LineNumber(final DataInput file) throws IOException {
        this(file.readUnsignedShort(), file.readUnsignedShort());
    }
    
    public LineNumber(final int start_pc, final int line_number) {
        this.start_pc = start_pc;
        this.line_number = line_number;
    }
    
    public final void dump(final DataOutputStream file) throws IOException {
        file.writeShort(this.start_pc);
        file.writeShort(this.line_number);
    }
    
    @Override
    public final String toString() {
        return "LineNumber(" + this.start_pc + ", " + this.line_number + ")";
    }
    
    public LineNumber copy() {
        try {
            return (LineNumber)this.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
