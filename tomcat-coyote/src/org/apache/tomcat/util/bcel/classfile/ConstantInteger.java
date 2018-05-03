// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantInteger extends Constant
{
    private static final long serialVersionUID = -6415476571232528966L;
    private int bytes;
    
    public ConstantInteger(final int bytes) {
        super((byte)3);
        this.bytes = bytes;
    }
    
    ConstantInteger(final DataInput file) throws IOException {
        this(file.readInt());
    }
    
    public final int getBytes() {
        return this.bytes;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(bytes = " + this.bytes + ")";
    }
}
