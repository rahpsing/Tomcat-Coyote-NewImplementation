// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;

public final class ConstantMethodref extends ConstantCP
{
    private static final long serialVersionUID = -7857009620954576086L;
    
    ConstantMethodref(final DataInputStream file) throws IOException {
        super((byte)10, file);
    }
}
