// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;

public final class ConstantInterfaceMethodref extends ConstantCP
{
    private static final long serialVersionUID = -8587605570227841891L;
    
    ConstantInterfaceMethodref(final DataInputStream file) throws IOException {
        super((byte)11, file);
    }
}
