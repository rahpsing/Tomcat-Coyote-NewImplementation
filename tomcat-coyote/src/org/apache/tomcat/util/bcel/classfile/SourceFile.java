// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class SourceFile extends Attribute
{
    private static final long serialVersionUID = 332346699609443704L;
    private int sourcefile_index;
    
    SourceFile(final int name_index, final int length, final DataInput file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, file.readUnsignedShort(), constant_pool);
    }
    
    public SourceFile(final int name_index, final int length, final int sourcefile_index, final ConstantPool constant_pool) {
        super((byte)0, name_index, length, constant_pool);
        this.sourcefile_index = sourcefile_index;
    }
    
    public final String getSourceFileName() {
        final ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.sourcefile_index, (byte)1);
        return c.getBytes();
    }
    
    @Override
    public final String toString() {
        return "SourceFile(" + this.getSourceFileName() + ")";
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        return (SourceFile)this.clone();
    }
}
