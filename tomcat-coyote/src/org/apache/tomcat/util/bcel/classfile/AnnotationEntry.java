// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.util.List;
import java.io.Serializable;
import org.apache.tomcat.util.bcel.Constants;

public class AnnotationEntry implements Constants, Serializable
{
    private static final long serialVersionUID = 1L;
    private final int type_index;
    private final ConstantPool constant_pool;
    private List<ElementValuePair> element_value_pairs;
    
    public static AnnotationEntry read(final DataInputStream file, final ConstantPool constant_pool) throws IOException {
        final AnnotationEntry annotationEntry = new AnnotationEntry(file.readUnsignedShort(), constant_pool);
        final int num_element_value_pairs = file.readUnsignedShort();
        annotationEntry.element_value_pairs = new ArrayList<ElementValuePair>();
        for (int i = 0; i < num_element_value_pairs; ++i) {
            annotationEntry.element_value_pairs.add(new ElementValuePair(file.readUnsignedShort(), ElementValue.readElementValue(file, constant_pool), constant_pool));
        }
        return annotationEntry;
    }
    
    public AnnotationEntry(final int type_index, final ConstantPool constant_pool) {
        this.type_index = type_index;
        this.constant_pool = constant_pool;
    }
    
    public String getAnnotationType() {
        final ConstantUtf8 c = (ConstantUtf8)this.constant_pool.getConstant(this.type_index, (byte)1);
        return c.getBytes();
    }
    
    public ElementValuePair[] getElementValuePairs() {
        return this.element_value_pairs.toArray(new ElementValuePair[this.element_value_pairs.size()]);
    }
    
    public void dump(final DataOutputStream dos) throws IOException {
        dos.writeShort(this.type_index);
        dos.writeShort(this.element_value_pairs.size());
        for (int i = 0; i < this.element_value_pairs.size(); ++i) {
            final ElementValuePair envp = this.element_value_pairs.get(i);
            envp.dump(dos);
        }
    }
}
