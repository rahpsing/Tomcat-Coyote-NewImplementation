// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

public class InvalidFileNameException extends RuntimeException
{
    private static final long serialVersionUID = 7922042602454350470L;
    private final String name;
    
    public InvalidFileNameException(final String pName, final String pMessage) {
        super(pMessage);
        this.name = pName;
    }
    
    public String getName() {
        return this.name;
    }
}
