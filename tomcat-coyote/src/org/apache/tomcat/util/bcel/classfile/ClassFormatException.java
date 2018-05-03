// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

public class ClassFormatException extends RuntimeException
{
    private static final long serialVersionUID = 3243149520175287759L;
    
    public ClassFormatException() {
    }
    
    public ClassFormatException(final String s) {
        super(s);
    }
    
    public ClassFormatException(final String s, final Throwable initCause) {
        super(s, initCause);
    }
}
