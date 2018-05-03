// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;

public final class PMGClass extends Attribute
{
    private static final long serialVersionUID = -1876065562391587509L;
    private int pmg_class_index;
    private int pmg_index;
    
    PMGClass(final int name_index, final int length, final DataInput file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, file.readUnsignedShort(), file.readUnsignedShort(), constant_pool);
    }
    
    public PMGClass(final int name_index, final int length, final int pmg_index, final int pmg_class_index, final ConstantPool constant_pool) {
        super((byte)9, name_index, length, constant_pool);
        this.pmg_index = pmg_index;
        this.pmg_class_index = pmg_class_index;
    }
    
    public final String getPMGName() {
        final ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.pmg_index, (byte)1);
        return c.getBytes();
    }
    
    public final String getPMGClassName() {
        final ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.pmg_class_index, (byte)1);
        return c.getBytes();
    }
    
    @Override
    public final String toString() {
        return "PMGClass(" + this.getPMGName() + ", " + this.getPMGClassName() + ")";
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        return (PMGClass)this.clone();
    }
}
