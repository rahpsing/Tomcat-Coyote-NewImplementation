// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;

public class AnnotationDefault extends Attribute
{
    private static final long serialVersionUID = 6715933396664171543L;
    ElementValue default_value;
    
    public AnnotationDefault(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, (ElementValue)null, constant_pool);
        this.default_value = ElementValue.readElementValue(file, constant_pool);
    }
    
    public AnnotationDefault(final int name_index, final int length, final ElementValue defaultValue, final ConstantPool constant_pool) {
        super((byte)16, name_index, length, constant_pool);
        this.setDefaultValue(defaultValue);
    }
    
    public final void setDefaultValue(final ElementValue defaultValue) {
        this.default_value = defaultValue;
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        throw new RuntimeException("Not implemented yet!");
    }
}
