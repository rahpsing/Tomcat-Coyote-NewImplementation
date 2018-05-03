// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;

public class EnclosingMethod extends Attribute
{
    private static final long serialVersionUID = 6755214228300933233L;
    
    public EnclosingMethod(final int nameIndex, final int len, final DataInputStream dis, final ConstantPool cpool) throws IOException {
        super((byte)18, nameIndex, len, cpool);
        dis.readUnsignedShort();
        dis.readUnsignedShort();
    }
    
    @Override
    public Attribute copy(final ConstantPool constant_pool) {
        throw new RuntimeException("Not implemented yet!");
    }
}
