// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;

public abstract class FieldOrMethod extends AccessFlags implements Cloneable
{
    private static final long serialVersionUID = -3383525930205542157L;
    protected int name_index;
    protected int signature_index;
    protected int attributes_count;
    protected Attribute[] attributes;
    protected ConstantPool constant_pool;
    
    FieldOrMethod() {
    }
    
    protected FieldOrMethod(final DataInputStream file, final ConstantPool constant_pool) throws IOException, ClassFormatException {
        this(file.readUnsignedShort(), file.readUnsignedShort(), file.readUnsignedShort(), null, constant_pool);
        this.attributes_count = file.readUnsignedShort();
        this.attributes = new Attribute[this.attributes_count];
        for (int i = 0; i < this.attributes_count; ++i) {
            this.attributes[i] = Attribute.readAttribute(file, constant_pool);
        }
    }
    
    protected FieldOrMethod(final int access_flags, final int name_index, final int signature_index, final Attribute[] attributes, final ConstantPool constant_pool) {
        this.access_flags = access_flags;
        this.name_index = name_index;
        this.signature_index = signature_index;
        this.constant_pool = constant_pool;
        this.setAttributes(attributes);
    }
    
    public final void setAttributes(final Attribute[] attributes) {
        this.attributes = attributes;
        this.attributes_count = ((attributes == null) ? 0 : attributes.length);
    }
    
    public final String getName() {
        final ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.name_index, (byte)1);
        return c.getBytes();
    }
    
    public final String getSignature() {
        final ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.signature_index, (byte)1);
        return c.getBytes();
    }
}
