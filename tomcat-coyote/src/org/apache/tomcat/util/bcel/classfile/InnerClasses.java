// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataInputStream;

public final class InnerClasses extends Attribute
{
    private static final long serialVersionUID = 54179484605570305L;
    private InnerClass[] inner_classes;
    private int number_of_classes;
    
    public InnerClasses(final int name_index, final int length, final InnerClass[] inner_classes, final ConstantPool constant_pool) {
        super((byte)6, name_index, length, constant_pool);
        this.setInnerClasses(inner_classes);
    }
    
    InnerClasses(final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(name_index, length, (InnerClass[])null, constant_pool);
        this.number_of_classes = file.readUnsignedShort();
        this.inner_classes = new InnerClass[this.number_of_classes];
        for (int i = 0; i < this.number_of_classes; ++i) {
            this.inner_classes[i] = new InnerClass(file);
        }
    }
    
    public final void setInnerClasses(final InnerClass[] inner_classes) {
        this.inner_classes = inner_classes;
        this.number_of_classes = ((inner_classes == null) ? 0 : inner_classes.length);
    }
    
    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < this.number_of_classes; ++i) {
            buf.append(this.inner_classes[i].toString(this.constant_pool)).append("\n");
        }
        return buf.toString();
    }
    
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
        final InnerClasses c = (InnerClasses)this.clone();
        c.inner_classes = new InnerClass[this.number_of_classes];
        for (int i = 0; i < this.number_of_classes; ++i) {
            c.inner_classes[i] = this.inner_classes[i].copy();
        }
        c.constant_pool = _constant_pool;
        return c;
    }
}
