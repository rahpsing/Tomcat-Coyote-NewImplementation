// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

public class HeadersTooLargeException extends IllegalStateException
{
    private static final long serialVersionUID = 1L;
    
    public HeadersTooLargeException() {
    }
    
    public HeadersTooLargeException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public HeadersTooLargeException(final String s) {
        super(s);
    }
    
    public HeadersTooLargeException(final Throwable cause) {
        super(cause);
    }
}
