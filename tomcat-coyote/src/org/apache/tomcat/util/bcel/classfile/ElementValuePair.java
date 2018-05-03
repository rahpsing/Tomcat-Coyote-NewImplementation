// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataOutputStream;

public class ElementValuePair
{
    private ElementValue elementValue;
    private ConstantPool constantPool;
    private int elementNameIndex;
    
    public ElementValuePair(final int elementNameIndex, final ElementValue elementValue, final ConstantPool constantPool) {
        this.elementValue = elementValue;
        this.elementNameIndex = elementNameIndex;
        this.constantPool = constantPool;
    }
    
    public String getNameString() {
        final ConstantUtf8 c = (ConstantUtf8)this.constantPool.getConstant(this.elementNameIndex, (byte)1);
        return c.getBytes();
    }
    
    public final ElementValue getValue() {
        return this.elementValue;
    }
    
    protected void dump(final DataOutputStream dos) throws IOException {
        dos.writeShort(this.elementNameIndex);
        this.elementValue.dump(dos);
    }
}
