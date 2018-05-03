// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

import org.apache.tomcat.util.buf.MessageBytes;
import java.util.Enumeration;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.tomcat.util.res.StringManager;

public class MimeHeaders
{
    public static final int DEFAULT_HEADER_SIZE = 8;
    private static final StringManager sm;
    private MimeHeaderField[] headers;
    private int count;
    private int limit;
    
    public MimeHeaders() {
        this.headers = new MimeHeaderField[8];
        this.limit = -1;
    }
    
    public void setLimit(final int limit) {
        this.limit = limit;
        if (limit > 0 && this.headers.length > limit && this.count < limit) {
            final MimeHeaderField[] tmp = new MimeHeaderField[limit];
            System.arraycopy(this.headers, 0, tmp, 0, this.count);
            this.headers = tmp;
        }
    }
    
    public void recycle() {
        this.clear();
    }
    
    public void clear() {
        for (int i = 0; i < this.count; ++i) {
            this.headers[i].recycle();
        }
        this.count = 0;
    }
    
    @Override
    public String toString() {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("=== MimeHeaders ===");
        final Enumeration<String> e = this.names();
        while (e.hasMoreElements()) {
            final String n = e.nextElement();
            final Enumeration<String> ev = this.values(n);
            while (ev.hasMoreElements()) {
                pw.print(n);
                pw.print(" = ");
                pw.println(ev.nextElement());
            }
        }
        return sw.toString();
    }
    
    public int size() {
        return this.count;
    }
    
    public MessageBytes getName(final int n) {
        return (n >= 0 && n < this.count) ? this.headers[n].getName() : null;
    }
    
    public MessageBytes getValue(final int n) {
        return (n >= 0 && n < this.count) ? this.headers[n].getValue() : null;
    }
    
    public int findHeader(final String name, final int starting) {
        for (int i = starting; i < this.count; ++i) {
            if (this.headers[i].getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }
    
    public Enumeration<String> names() {
        return new NamesEnumerator(this);
    }
    
    public Enumeration<String> values(final String name) {
        return new ValuesEnumerator(this, name);
    }
    
    private MimeHeaderField createHeader() {
        if (this.limit > -1 && this.count >= this.limit) {
            throw new IllegalStateException(MimeHeaders.sm.getString("headers.maxCountFail", new Object[] { this.limit }));
        }
        final int len = this.headers.length;
        if (this.count >= len) {
            int newLength = this.count * 2;
            if (this.limit > 0 && newLength > this.limit) {
                newLength = this.limit;
            }
            final MimeHeaderField[] tmp = new MimeHeaderField[newLength];
            System.arraycopy(this.headers, 0, tmp, 0, len);
            this.headers = tmp;
        }
        MimeHeaderField mh;
        if ((mh = this.headers[this.count]) == null) {
            mh = (this.headers[this.count] = new MimeHeaderField());
        }
        ++this.count;
        return mh;
    }
    
    public MessageBytes addValue(final String name) {
        final MimeHeaderField mh = this.createHeader();
        mh.getName().setString(name);
        return mh.getValue();
    }
    
    public MessageBytes addValue(final byte[] b, final int startN, final int len) {
        final MimeHeaderField mhf = this.createHeader();
        mhf.getName().setBytes(b, startN, len);
        return mhf.getValue();
    }
    
    public MessageBytes addValue(final char[] c, final int startN, final int len) {
        final MimeHeaderField mhf = this.createHeader();
        mhf.getName().setChars(c, startN, len);
        return mhf.getValue();
    }
    
    public MessageBytes setValue(final String name) {
        for (int i = 0; i < this.count; ++i) {
            if (this.headers[i].getName().equalsIgnoreCase(name)) {
                for (int j = i + 1; j < this.count; ++j) {
                    if (this.headers[j].getName().equalsIgnoreCase(name)) {
                        this.removeHeader(j--);
                    }
                }
                return this.headers[i].getValue();
            }
        }
        final MimeHeaderField mh = this.createHeader();
        mh.getName().setString(name);
        return mh.getValue();
    }
    
    public MessageBytes getValue(final String name) {
        for (int i = 0; i < this.count; ++i) {
            if (this.headers[i].getName().equalsIgnoreCase(name)) {
                return this.headers[i].getValue();
            }
        }
        return null;
    }
    
    public MessageBytes getUniqueValue(final String name) {
        MessageBytes result = null;
        for (int i = 0; i < this.count; ++i) {
            if (this.headers[i].getName().equalsIgnoreCase(name)) {
                if (result != null) {
                    throw new IllegalArgumentException();
                }
                result = this.headers[i].getValue();
            }
        }
        return result;
    }
    
    public String getHeader(final String name) {
        final MessageBytes mh = this.getValue(name);
        return (mh != null) ? mh.toString() : null;
    }
    
    public void removeHeader(final String name) {
        for (int i = 0; i < this.count; ++i) {
            if (this.headers[i].getName().equalsIgnoreCase(name)) {
                this.removeHeader(i--);
            }
        }
    }
    
    private void removeHeader(final int idx) {
        final MimeHeaderField mh = this.headers[idx];
        mh.recycle();
        this.headers[idx] = this.headers[this.count - 1];
        this.headers[this.count - 1] = mh;
        --this.count;
    }
    
    static {
        sm = StringManager.getManager("org.apache.tomcat.util.http");
    }
}
