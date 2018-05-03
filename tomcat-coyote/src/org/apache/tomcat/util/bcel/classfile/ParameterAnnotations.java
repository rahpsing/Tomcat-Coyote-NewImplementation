// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;

public abstract class ParameterAnnotations extends Attribute
{
    private static final long serialVersionUID = -8831779739803248091L;
    private int num_parameters;
    private ParameterAnnotationEntry[] parameter_annotation_table;
    
    ParameterAnnotations(final byte parameter_annotation_type, final int name_index, final int length, final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this(parameter_annotation_type, name_index, length, (ParameterAnnotationEntry[])null, constant_pool);
        this.num_parameters = file.readUnsignedByte();
        this.parameter_annotation_table = new ParameterAnnotationEntry[this.num_parameters];
        for (int i = 0; i < this.num_parameters; ++i) {
            this.parameter_annotation_table[i] = new ParameterAnnotationEntry(file, constant_pool);
        }
    }
    
    public ParameterAnnotations(final byte parameter_annotation_type, final int name_index, final int length, final ParameterAnnotationEntry[] parameter_annotation_table, final ConstantPool constant_pool) {
        super(parameter_annotation_type, name_index, length, constant_pool);
        this.setParameterAnnotationTable(parameter_annotation_table);
    }
    
    public final void setParameterAnnotationTable(final ParameterAnnotationEntry[] parameter_annotation_table) {
        this.parameter_annotation_table = parameter_annotation_table;
        this.num_parameters = ((parameter_annotation_table == null) ? 0 : parameter_annotation_table.length);
    }
}
