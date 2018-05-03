// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantMethodHandle extends Constant
{
    private static final long serialVersionUID = -7875124116920198044L;
    private int reference_kind;
    private int reference_index;
    
    public ConstantMethodHandle(final ConstantMethodHandle c) {
        this(c.getReferenceKind(), c.getReferenceIndex());
    }
    
    ConstantMethodHandle(final DataInput file) throws IOException {
        this(file.readUnsignedByte(), file.readUnsignedShort());
    }
    
    public ConstantMethodHandle(final int reference_kind, final int reference_index) {
        super((byte)15);
        this.reference_kind = reference_kind;
        this.reference_index = reference_index;
    }
    
    public int getReferenceKind() {
        return this.reference_kind;
    }
    
    public void setReferenceKind(final int reference_kind) {
        this.reference_kind = reference_kind;
    }
    
    public int getReferenceIndex() {
        return this.reference_index;
    }
    
    public void setReferenceIndex(final int reference_index) {
        this.reference_index = reference_index;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(reference_kind = " + this.reference_kind + ", reference_index = " + this.reference_index + ")";
    }
}
