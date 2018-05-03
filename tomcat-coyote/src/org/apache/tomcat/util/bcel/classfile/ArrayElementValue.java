// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataOutputStream;

public class ArrayElementValue extends ElementValue
{
    private ElementValue[] evalues;
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < this.evalues.length; ++i) {
            sb.append(this.evalues[i].toString());
            if (i + 1 < this.evalues.length) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    public ArrayElementValue(final int type, final ElementValue[] datums, final ConstantPool cpool) {
        super(type, cpool);
        if (type != 91) {
            throw new RuntimeException("Only element values of type array can be built with this ctor - type specified: " + type);
        }
        this.evalues = datums;
    }
    
    @Override
    public void dump(final DataOutputStream dos) throws IOException {
        dos.writeByte(this.type);
        dos.writeShort(this.evalues.length);
        for (int i = 0; i < this.evalues.length; ++i) {
            this.evalues[i].dump(dos);
        }
    }
    
    @Override
    public String stringifyValue() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < this.evalues.length; ++i) {
            sb.append(this.evalues[i].stringifyValue());
            if (i + 1 < this.evalues.length) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    public ElementValue[] getElementValuesArray() {
        return this.evalues;
    }
}
