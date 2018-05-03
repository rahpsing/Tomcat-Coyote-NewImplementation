// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

import java.util.Enumeration;

class NamesEnumerator implements Enumeration<String>
{
    int pos;
    int size;
    String next;
    MimeHeaders headers;
    
    public NamesEnumerator(final MimeHeaders headers) {
        this.headers = headers;
        this.pos = 0;
        this.size = headers.size();
        this.findNext();
    }
    
    private void findNext() {
        this.next = null;
        while (this.pos < this.size) {
            this.next = this.headers.getName(this.pos).toString();
            for (int j = 0; j < this.pos; ++j) {
                if (this.headers.getName(j).equalsIgnoreCase(this.next)) {
                    this.next = null;
                    break;
                }
            }
            if (this.next != null) {
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
        final String current = this.next;
        this.findNext();
        return current;
    }
}
