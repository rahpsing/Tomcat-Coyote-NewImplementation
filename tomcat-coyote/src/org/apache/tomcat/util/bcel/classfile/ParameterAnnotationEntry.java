// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.IOException;
import java.io.DataInputStream;
import org.apache.tomcat.util.bcel.Constants;

public class ParameterAnnotationEntry implements Constants
{
    private int annotation_table_length;
    private AnnotationEntry[] annotation_table;
    
    ParameterAnnotationEntry(final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        this.annotation_table_length = file.readUnsignedShort();
        this.annotation_table = new AnnotationEntry[this.annotation_table_length];
        for (int i = 0; i < this.annotation_table_length; ++i) {
            this.annotation_table[i] = AnnotationEntry.read(file, constant_pool);
        }
    }
}
