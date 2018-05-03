// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

import org.apache.tomcat.util.buf.MessageBytes;

class MimeHeaderField
{
    MimeHeaderField next;
    MimeHeaderField prev;
    protected final MessageBytes nameB;
    protected final MessageBytes valueB;
    
    public MimeHeaderField() {
        this.nameB = MessageBytes.newInstance();
        this.valueB = MessageBytes.newInstance();
    }
    
    public void recycle() {
        this.nameB.recycle();
        this.valueB.recycle();
        this.next = null;
    }
    
    public MessageBytes getName() {
        return this.nameB;
    }
    
    public MessageBytes getValue() {
        return this.valueB;
    }
}
