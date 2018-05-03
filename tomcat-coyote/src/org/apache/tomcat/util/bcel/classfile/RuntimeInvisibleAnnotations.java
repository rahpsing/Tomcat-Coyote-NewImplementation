// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;

public class RuntimeInvisibleAnnotations extends Annotations
{
    private static final long serialVersionUID = -7962627688723310248L;
    
    RuntimeInvisibleAnnotations(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        super((byte)13, name_index, length, file, constant_pool);
    }
    
    @Override
    public Attribute copy(final ConstantPool constant_pool) {
        final Annotations c = (Annotations)this.clone();
        return c;
    }
}
