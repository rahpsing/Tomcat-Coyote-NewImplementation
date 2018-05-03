// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

import org.apache.tomcat.util.buf.MessageBytes;
import java.util.Enumeration;

class ValuesEnumerator implements Enumeration<String>
{
    int pos;
    int size;
    MessageBytes next;
    MimeHeaders headers;
    String name;
    
    ValuesEnumerator(final MimeHeaders headers, final String name) {
        this.name = name;
        this.headers = headers;
        this.pos = 0;
        this.size = headers.size();
        this.findNext();
    }
    
    private void findNext() {
        this.next = null;
        while (this.pos < this.size) {
            final MessageBytes n1 = this.headers.getName(this.pos);
            if (n1.equalsIgnoreCase(this.name)) {
                this.next = this.headers.getValue(this.pos);
                break;
            }
            ++this.pos;
        }
        ++this.pos;
    }
    
    @Override
    public boolean hasMoreElements() {
        return this.next != null;
    }
    
    @Override
    public String nextElement() {
        final MessageBytes current = this.next;
        this.findNext();
        return current.toString();
    }
}
