// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.DataInputStream;

public final class ClassParser
{
    private DataInputStream file;
    private boolean fileOwned;
    private String file_name;
    private String zip_file;
    private int class_name_index;
    private int superclass_name_index;
    private int major;
    private int minor;
    private int access_flags;
    private int[] interfaces;
    private ConstantPool constant_pool;
    private Field[] fields;
    private Method[] methods;
    private Attribute[] attributes;
    private boolean is_zip;
    private static final int BUFSIZE = 8192;
    
    public ClassParser(final InputStream file, final String file_name) {
        this.file_name = file_name;
        this.fileOwned = false;
        final String clazz = file.getClass().getName();
        this.is_zip = (clazz.startsWith("java.util.zip.") || clazz.startsWith("java.util.jar."));
        if (file instanceof DataInputStream) {
            this.file = (DataInputStream)file;
        }
        else {
            this.file = new DataInputStream(new BufferedInputStream(file, 8192));
        }
    }
    
    public JavaClass parse() throws IOException, ClassFormatException {
        ZipFile zip = null;
        try {
            if (this.fileOwned) {
                if (this.is_zip) {
                    zip = new ZipFile(this.zip_file);
                    final ZipEntry entry = zip.getEntry(this.file_name);
                    if (entry == null) {
                        throw new IOException("File " + this.file_name + " not found");
                    }
                    this.file = new DataInputStream(new BufferedInputStream(zip.getInputStream(entry), 8192));
                }
                else {
                    this.file = new DataInputStream(new BufferedInputStream(new FileInputStream(this.file_name), 8192));
                }
            }
            this.readID();
            this.readVersion();
            this.readConstantPool();
            this.readClassInfo();
            this.readInterfaces();
            this.readFields();
            this.readMethods();
            this.readAttributes();
        }
        finally {
            if (this.fileOwned) {
                try {
                    if (this.file != null) {
                        this.file.close();
                    }
                    if (zip != null) {
                        zip.close();
                    }
                }
                catch (IOException ex) {}
            }
        }
        return new JavaClass(this.class_name_index, this.superclass_name_index, this.file_name, this.major, this.minor, this.access_flags, this.constant_pool, this.interfaces, this.fields, this.methods, this.attributes);
    }
    
    private final void readAttributes() throws IOException, ClassFormatException {
        final int attributes_count = this.file.readUnsignedShort();
        this.attributes = new Attribute[attributes_count];
        for (int i = 0; i < attributes_count; ++i) {
            this.attributes[i] = Attribute.readAttribute(this.file, this.constant_pool);
        }
    }
    
    private final void readClassInfo() throws IOException, ClassFormatException {
        this.access_flags = this.file.readUnsignedShort();
        if ((this.access_flags & 0x200) != 0x0) {
            this.access_flags |= 0x400;
        }
        if ((this.access_flags & 0x400) != 0x0 && (this.access_flags & 0x10) != 0x0) {
            throw new ClassFormatException("Class " + this.file_name + " can't be both final and abstract");
        }
        this.class_name_index = this.file.readUnsignedShort();
        this.superclass_name_index = this.file.readUnsignedShort();
    }
    
    private final void readConstantPool() throws IOException, ClassFormatException {
        this.constant_pool = new ConstantPool(this.file);
    }
    
    private final void readFields() throws IOException, ClassFormatException {
        final int fields_count = this.file.readUnsignedShort();
        this.fields = new Field[fields_count];
        for (int i = 0; i < fields_count; ++i) {
            this.fields[i] = new Field(this.file, this.constant_pool);
        }
    }
    
    private final void readID() throws IOException, ClassFormatException {
        final int magic = -889275714;
        if (this.file.readInt() != magic) {
            throw new ClassFormatException(this.file_name + " is not a Java .class file");
        }
    }
    
    private final void readInterfaces() throws IOException, ClassFormatException {
        final int interfaces_count = this.file.readUnsignedShort();
        this.interfaces = new int[interfaces_count];
        for (int i = 0; i < interfaces_count; ++i) {
            this.interfaces[i] = this.file.readUnsignedShort();
        }
    }
    
    private final void readMethods() throws IOException, ClassFormatException {
        final int methods_count = this.file.readUnsignedShort();
        this.methods = new Method[methods_count];
        for (int i = 0; i < methods_count; ++i) {
            this.methods[i] = new Method(this.file, this.constant_pool);
        }
    }
    
    private final void readVersion() throws IOException, ClassFormatException {
        this.minor = this.file.readUnsignedShort();
        this.major = this.file.readUnsignedShort();
    }
}
