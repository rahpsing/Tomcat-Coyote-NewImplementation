// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

import java.io.IOException;
import java.util.BitSet;

public final class UEncoder
{
    private BitSet safeChars;
    private C2BConverter c2b;
    private ByteChunk bb;
    private CharChunk cb;
    private CharChunk output;
    private String encoding;
    
    public UEncoder() {
        this.safeChars = null;
        this.c2b = null;
        this.bb = null;
        this.cb = null;
        this.output = null;
        this.encoding = "UTF8";
        this.initSafeChars();
    }
    
    @Deprecated
    public void setEncoding(final String s) {
        this.encoding = s;
    }
    
    public void addSafeCharacter(final char c) {
        this.safeChars.set(c);
    }
    
    public CharChunk encodeURL(final String s, final int start, final int end) throws IOException {
        if (this.c2b == null) {
            this.bb = new ByteChunk(8);
            this.cb = new CharChunk(2);
            this.output = new CharChunk(64);
            this.c2b = new C2BConverter(this.encoding);
        }
        else {
            this.bb.recycle();
            this.cb.recycle();
            this.output.recycle();
        }
        for (int i = start; i < end; ++i) {
            final char c = s.charAt(i);
            if (this.safeChars.get(c)) {
                this.output.append(c);
            }
            else {
                this.cb.append(c);
                this.c2b.convert(this.cb, this.bb);
                if (c >= '\ud800' && c <= '\udbff' && i + 1 < end) {
                    final char d = s.charAt(i + 1);
                    if (d >= '\udc00' && d <= '\udfff') {
                        this.cb.append(d);
                        this.c2b.convert(this.cb, this.bb);
                        ++i;
                    }
                }
                this.urlEncode(this.output, this.bb);
                this.cb.recycle();
                this.bb.recycle();
            }
        }
        return this.output;
    }
    
    protected void urlEncode(final CharChunk out, final ByteChunk bb) throws IOException {
        final byte[] bytes = bb.getBuffer();
        for (int j = bb.getStart(); j < bb.getEnd(); ++j) {
            out.append('%');
            char ch = Character.forDigit(bytes[j] >> 4 & 0xF, 16);
            out.append(ch);
            ch = Character.forDigit(bytes[j] & 0xF, 16);
            out.append(ch);
        }
    }
    
    private void initSafeChars() {
        this.safeChars = new BitSet(128);
        for (int i = 97; i <= 122; ++i) {
            this.safeChars.set(i);
        }
        for (int i = 65; i <= 90; ++i) {
            this.safeChars.set(i);
        }
        for (int i = 48; i <= 57; ++i) {
            this.safeChars.set(i);
        }
        this.safeChars.set(36);
        this.safeChars.set(45);
        this.safeChars.set(95);
        this.safeChars.set(46);
        this.safeChars.set(33);
        this.safeChars.set(42);
        this.safeChars.set(39);
        this.safeChars.set(40);
        this.safeChars.set(41);
        this.safeChars.set(44);
    }
}
