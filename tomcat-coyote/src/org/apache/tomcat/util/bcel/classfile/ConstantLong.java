// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class ConstantLong extends Constant
{
    private static final long serialVersionUID = -1893131676489003562L;
    private long bytes;
    
    public ConstantLong(final long bytes) {
        super((byte)5);
        this.bytes = bytes;
    }
    
    ConstantLong(final DataInput file) throws IOException {
        this(file.readLong());
    }
    
    public final long getBytes() {
        return this.bytes;
    }
    
    @Override
    public final String toString() {
        return super.toString() + "(bytes = " + this.bytes + ")";
    }
}
