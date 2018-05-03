// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;

public final class ConstantFieldref extends ConstantCP
{
    private static final long serialVersionUID = -8062332095934294437L;
    
    ConstantFieldref(final DataInputStream file) throws IOException {
        super((byte)9, file);
    }
}
