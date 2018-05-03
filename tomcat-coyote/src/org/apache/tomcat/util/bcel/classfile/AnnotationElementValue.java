// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataOutputStream;

public class AnnotationElementValue extends ElementValue
{
    private AnnotationEntry annotationEntry;
    
    public AnnotationElementValue(final int type, final AnnotationEntry annotationEntry, final ConstantPool cpool) {
        super(type, cpool);
        if (type != 64) {
            throw new RuntimeException("Only element values of type annotation can be built with this ctor - type specified: " + type);
        }
        this.annotationEntry = annotationEntry;
    }
    
    @Override
    public void dump(final DataOutputStream dos) throws IOException {
        dos.writeByte(this.type);
        this.annotationEntry.dump(dos);
    }
    
    @Override
    public String stringifyValue() {
        return this.annotationEntry.toString();
    }
    
    @Override
    public String toString() {
        return this.stringifyValue();
    }
    
    public AnnotationEntry getAnnotationEntry() {
        return this.annotationEntry;
    }
}
