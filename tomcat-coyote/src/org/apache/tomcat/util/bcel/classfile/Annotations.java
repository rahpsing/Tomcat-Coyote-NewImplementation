// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInputStream;

public abstract class Annotations extends Attribute
{
    private static final long serialVersionUID = 1L;
    private AnnotationEntry[] annotation_table;
    
    public Annotations(final byte annotation_type, final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(annotation_type, name_index, length, (AnnotationEntry[])null, constant_pool);
        final int annotation_table_length = file.readUnsignedShort();
        this.annotation_table = new AnnotationEntry[annotation_table_length];
        for (int i = 0; i < annotation_table_length; ++i) {
            this.annotation_table[i] = AnnotationEntry.read(file, constant_pool);
        }
    }
    
    public Annotations(final byte annotation_type, final int name_index, final int length, final AnnotationEntry[] annotation_table, final ConstantPool constant_pool) {
        super(annotation_type, name_index, length, constant_pool);
        this.setAnnotationTable(annotation_table);
    }
    
    public final void setAnnotationTable(final AnnotationEntry[] annotation_table) {
        this.annotation_table = annotation_table;
    }
    
    public AnnotationEntry[] getAnnotationEntries() {
        return this.annotation_table;
    }
    
    protected void writeAnnotations(final DataOutputStream dos) throws IOException {
        if (this.annotation_table == null) {
            return;
        }
        dos.writeShort(this.annotation_table.length);
        for (int i = 0; i < this.annotation_table.length; ++i) {
            this.annotation_table[i].dump(dos);
        }
    }
}
