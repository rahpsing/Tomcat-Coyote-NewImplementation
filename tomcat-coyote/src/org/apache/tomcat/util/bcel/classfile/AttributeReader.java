// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.DataInputStream;

public interface AttributeReader
{
    Attribute createAttribute(final int p0, final int p1, final DataInputStream p2, final ConstantPool p3);
}
