// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import org.apache.tomcat.util.bcel.util.BCELComparator;

public class JavaClass extends AccessFlags implements Cloneable, Comparable<JavaClass>
{
    private static final long serialVersionUID = 7029227708237523236L;
    private String file_name;
    private String source_file_name;
    private String class_name;
    private String superclass_name;
    private int major;
    private int minor;
    private ConstantPool constant_pool;
    private int[] interfaces;
    private String[] interface_names;
    private Field[] fields;
    private Method[] methods;
    private Attribute[] attributes;
    private AnnotationEntry[] annotations;
    private boolean annotationsOutOfDate;
    private static BCELComparator _cmp;
    
    public JavaClass(final int class_name_index, final int superclass_name_index, final String file_name, final int major, final int minor, final int access_flags, final ConstantPool constant_pool, int[] interfaces, Field[] fields, Method[] methods, Attribute[] attributes) {
        this.source_file_name = "<Unknown>";
        this.annotationsOutOfDate = true;
        if (interfaces == null) {
            interfaces = new int[0];
        }
        if (attributes == null) {
            attributes = new Attribute[0];
        }
        if (fields == null) {
            fields = new Field[0];
        }
        if (methods == null) {
            methods = new Method[0];
        }
        this.file_name = file_name;
        this.major = major;
        this.minor = minor;
        this.access_flags = access_flags;
        this.constant_pool = constant_pool;
        this.interfaces = interfaces;
        this.fields = fields;
        this.methods = methods;
        this.attributes = attributes;
        this.annotationsOutOfDate = true;
        for (int i = 0; i < attributes.length; ++i) {
            if (attributes[i] instanceof SourceFile) {
                this.source_file_name = ((SourceFile)attributes[i]).getSourceFileName();
                break;
            }
        }
        this.class_name = constant_pool.getConstantString(class_name_index, (byte)7);
        this.class_name = Utility.compactClassName(this.class_name, false);
        if (superclass_name_index > 0) {
            this.superclass_name = constant_pool.getConstantString(superclass_name_index, (byte)7);
            this.superclass_name = Utility.compactClassName(this.superclass_name, false);
        }
        else {
            this.superclass_name = "java.lang.Object";
        }
        this.interface_names = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; ++i) {
            final String str = constant_pool.getConstantString(interfaces[i], (byte)7);
            this.interface_names[i] = Utility.compactClassName(str, false);
        }
    }
    
    public Attribute[] getAttributes() {
        return this.attributes;
    }
    
    public AnnotationEntry[] getAnnotationEntries() {
        if (this.annotationsOutOfDate) {
            final Attribute[] attrs = this.getAttributes();
            final List<AnnotationEntry> accumulatedAnnotations = new ArrayList<AnnotationEntry>();
            for (int i = 0; i < attrs.length; ++i) {
                final Attribute attribute = attrs[i];
                if (attribute instanceof Annotations) {
                    final Annotations runtimeAnnotations = (Annotations)attribute;
                    for (int j = 0; j < runtimeAnnotations.getAnnotationEntries().length; ++j) {
                        accumulatedAnnotations.add(runtimeAnnotations.getAnnotationEntries()[j]);
                    }
                }
            }
            this.annotations = accumulatedAnnotations.toArray(new AnnotationEntry[accumulatedAnnotations.size()]);
            this.annotationsOutOfDate = false;
        }
        return this.annotations;
    }
    
    public String getClassName() {
        return this.class_name;
    }
    
    public String[] getInterfaceNames() {
        return this.interface_names;
    }
    
    public String getSuperclassName() {
        return this.superclass_name;
    }
    
    @Override
    public String toString() {
        String access = Utility.accessToString(this.access_flags, true);
        access = (access.equals("") ? "" : (access + " "));
        final StringBuilder buf = new StringBuilder(128);
        buf.append(access).append(Utility.classOrInterface(this.access_flags)).append(" ").append(this.class_name).append(" extends ").append(Utility.compactClassName(this.superclass_name, false)).append('\n');
        final int size = this.interfaces.length;
        if (size > 0) {
            buf.append("implements\t\t");
            for (int i = 0; i < size; ++i) {
                buf.append(this.interface_names[i]);
                if (i < size - 1) {
                    buf.append(", ");
                }
            }
            buf.append('\n');
        }
        buf.append("filename\t\t").append(this.file_name).append('\n');
        buf.append("compiled from\t\t").append(this.source_file_name).append('\n');
        buf.append("compiler version\t").append(this.major).append(".").append(this.minor).append('\n');
        buf.append("access flags\t\t").append(this.access_flags).append('\n');
        buf.append("constant pool\t\t").append(this.constant_pool.getLength()).append(" entries\n");
        buf.append("ACC_SUPER flag\t\t").append(this.isSuper()).append("\n");
        if (this.attributes.length > 0) {
            buf.append("\nAttribute(s):\n");
            for (int i = 0; i < this.attributes.length; ++i) {
                buf.append(indent(this.attributes[i]));
            }
        }
        final AnnotationEntry[] annotations = this.getAnnotationEntries();
        if (annotations != null && annotations.length > 0) {
            buf.append("\nAnnotation(s):\n");
            for (int j = 0; j < annotations.length; ++j) {
                buf.append(indent(annotations[j]));
            }
        }
        if (this.fields.length > 0) {
            buf.append("\n").append(this.fields.length).append(" fields:\n");
            for (int j = 0; j < this.fields.length; ++j) {
                buf.append("\t").append(this.fields[j]).append('\n');
            }
        }
        if (this.methods.length > 0) {
            buf.append("\n").append(this.methods.length).append(" methods:\n");
            for (int j = 0; j < this.methods.length; ++j) {
                buf.append("\t").append(this.methods[j]).append('\n');
            }
        }
        return buf.toString();
    }
    
    private static final String indent(final Object obj) {
        final StringTokenizer tok = new StringTokenizer(obj.toString(), "\n");
        final StringBuilder buf = new StringBuilder();
        while (tok.hasMoreTokens()) {
            buf.append("\t").append(tok.nextToken()).append("\n");
        }
        return buf.toString();
    }
    
    public final boolean isSuper() {
        return (this.access_flags & 0x20) != 0x0;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return JavaClass._cmp.equals(this, obj);
    }
    
    @Override
    public int compareTo(final JavaClass obj) {
        return this.getClassName().compareTo(obj.getClassName());
    }
    
    @Override
    public int hashCode() {
        return JavaClass._cmp.hashCode(this);
    }
    
    static {
        JavaClass._cmp = new BCELComparator() {
            @Override
            public boolean equals(final Object o1, final Object o2) {
                final JavaClass THIS = (JavaClass)o1;
                final JavaClass THAT = (JavaClass)o2;
                return THIS.getClassName().equals(THAT.getClassName());
            }
            
            @Override
            public int hashCode(final Object o) {
                final JavaClass THIS = (JavaClass)o;
                return THIS.getClassName().hashCode();
            }
        };
    }
}
