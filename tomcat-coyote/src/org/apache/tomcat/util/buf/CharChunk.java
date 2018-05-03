// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;

public final class CharChunk implements Cloneable, Serializable, CharSequence
{
    private static final long serialVersionUID = 1L;
    private char[] buff;
    private int start;
    private int end;
    private boolean isSet;
    private int limit;
    private CharInputChannel in;
    private CharOutputChannel out;
    private boolean optimizedWrite;
    
    public CharChunk() {
        this.isSet = false;
        this.limit = -1;
        this.in = null;
        this.out = null;
        this.optimizedWrite = true;
    }
    
    public CharChunk(final int size) {
        this.isSet = false;
        this.limit = -1;
        this.in = null;
        this.out = null;
        this.optimizedWrite = true;
        this.allocate(size, -1);
    }
    
    @Deprecated
    public CharChunk getClone() {
        try {
            return (CharChunk)this.clone();
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public boolean isNull() {
        return this.end <= 0 && !this.isSet;
    }
    
    public void recycle() {
        this.isSet = false;
        this.start = 0;
        this.end = 0;
    }
    
    @Deprecated
    public void reset() {
        this.buff = null;
    }
    
    public void allocate(final int initial, final int limit) {
        if (this.buff == null || this.buff.length < initial) {
            this.buff = new char[initial];
        }
        this.limit = limit;
        this.start = 0;
        this.end = 0;
        this.isSet = true;
    }
    
    public void setOptimizedWrite(final boolean optimizedWrite) {
        this.optimizedWrite = optimizedWrite;
    }
    
    public void setChars(final char[] c, final int off, final int len) {
        this.buff = c;
        this.start = off;
        this.end = this.start + len;
        this.isSet = true;
    }
    
    public void setLimit(final int limit) {
        this.limit = limit;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public void setCharInputChannel(final CharInputChannel in) {
        this.in = in;
    }
    
    public void setCharOutputChannel(final CharOutputChannel out) {
        this.out = out;
    }
    
    public char[] getChars() {
        return this.getBuffer();
    }
    
    public char[] getBuffer() {
        return this.buff;
    }
    
    public int getStart() {
        return this.start;
    }
    
    public int getOffset() {
        return this.start;
    }
    
    public void setOffset(final int off) {
        this.start = off;
    }
    
    public int getLength() {
        return this.end - this.start;
    }
    
    public int getEnd() {
        return this.end;
    }
    
    public void setEnd(final int i) {
        this.end = i;
    }
    
    public void append(final char b) throws IOException {
        this.makeSpace(1);
        if (this.limit > 0 && this.end >= this.limit) {
            this.flushBuffer();
        }
        this.buff[this.end++] = b;
    }
    
    public void append(final CharChunk src) throws IOException {
        this.append(src.getBuffer(), src.getOffset(), src.getLength());
    }
    
    public void append(final char[] src, final int off, final int len) throws IOException {
        this.makeSpace(len);
        if (this.limit < 0) {
            System.arraycopy(src, off, this.buff, this.end, len);
            this.end += len;
            return;
        }
        if (this.optimizedWrite && len == this.limit && this.end == this.start && this.out != null) {
            this.out.realWriteChars(src, off, len);
            return;
        }
        if (len <= this.limit - this.end) {
            System.arraycopy(src, off, this.buff, this.end, len);
            this.end += len;
            return;
        }
        if (len + this.end < 2 * this.limit) {
            final int avail = this.limit - this.end;
            System.arraycopy(src, off, this.buff, this.end, avail);
            this.end += avail;
            this.flushBuffer();
            System.arraycopy(src, off + avail, this.buff, this.end, len - avail);
            this.end += len - avail;
        }
        else {
            this.flushBuffer();
            this.out.realWriteChars(src, off, len);
        }
    }
    
    @Deprecated
    public void append(final StringBuilder sb) throws IOException {
        final int len = sb.length();
        this.makeSpace(len);
        if (this.limit < 0) {
            sb.getChars(0, len, this.buff, this.end);
            this.end += len;
            return;
        }
        int sbOff;
        final int off = sbOff = 0;
        final int sbEnd = off + len;
        while (sbOff < sbEnd) {
            final int d = this.min(this.limit - this.end, sbEnd - sbOff);
            sb.getChars(sbOff, sbOff + d, this.buff, this.end);
            sbOff += d;
            this.end += d;
            if (this.end >= this.limit) {
                this.flushBuffer();
            }
        }
    }
    
    public void append(final String s) throws IOException {
        this.append(s, 0, s.length());
    }
    
    public void append(final String s, final int off, final int len) throws IOException {
        if (s == null) {
            return;
        }
        this.makeSpace(len);
        if (this.limit < 0) {
            s.getChars(off, off + len, this.buff, this.end);
            this.end += len;
            return;
        }
        int sOff = off;
        final int sEnd = off + len;
        while (sOff < sEnd) {
            final int d = this.min(this.limit - this.end, sEnd - sOff);
            s.getChars(sOff, sOff + d, this.buff, this.end);
            sOff += d;
            this.end += d;
            if (this.end >= this.limit) {
                this.flushBuffer();
            }
        }
    }
    
    public int substract() throws IOException {
        if (this.end - this.start == 0) {
            if (this.in == null) {
                return -1;
            }
            final int n = this.in.realReadChars(this.buff, this.end, this.buff.length - this.end);
            if (n < 0) {
                return -1;
            }
        }
        return this.buff[this.start++];
    }
    
    @Deprecated
    public int substract(final CharChunk src) throws IOException {
        if (this.end - this.start == 0) {
            if (this.in == null) {
                return -1;
            }
            final int n = this.in.realReadChars(this.buff, this.end, this.buff.length - this.end);
            if (n < 0) {
                return -1;
            }
        }
        final int len = this.getLength();
        src.append(this.buff, this.start, len);
        this.start = this.end;
        return len;
    }
    
    public int substract(final char[] src, final int off, final int len) throws IOException {
        if (this.end - this.start == 0) {
            if (this.in == null) {
                return -1;
            }
            final int n = this.in.realReadChars(this.buff, this.end, this.buff.length - this.end);
            if (n < 0) {
                return -1;
            }
        }
        int n;
        if ((n = len) > this.getLength()) {
            n = this.getLength();
        }
        System.arraycopy(this.buff, this.start, src, off, n);
        this.start += n;
        return n;
    }
    
    public void flushBuffer() throws IOException {
        if (this.out == null) {
            throw new IOException("Buffer overflow, no sink " + this.limit + " " + this.buff.length);
        }
        this.out.realWriteChars(this.buff, this.start, this.end - this.start);
        this.end = this.start;
    }
    
    public void makeSpace(final int count) {
        char[] tmp = null;
        int desiredSize = this.end + count;
        if (this.limit > 0 && desiredSize > this.limit) {
            desiredSize = this.limit;
        }
        if (this.buff == null) {
            if (desiredSize < 256) {
                desiredSize = 256;
            }
            this.buff = new char[desiredSize];
        }
        if (desiredSize <= this.buff.length) {
            return;
        }
        if (desiredSize < 2 * this.buff.length) {
            int newSize = this.buff.length * 2;
            if (this.limit > 0 && newSize > this.limit) {
                newSize = this.limit;
            }
            tmp = new char[newSize];
        }
        else {
            int newSize = this.buff.length * 2 + count;
            if (this.limit > 0 && newSize > this.limit) {
                newSize = this.limit;
            }
            tmp = new char[newSize];
        }
        System.arraycopy(this.buff, 0, tmp, 0, this.end);
        this.buff = tmp;
        tmp = null;
    }
    
    @Override
    public String toString() {
        if (null == this.buff) {
            return null;
        }
        if (this.end - this.start == 0) {
            return "";
        }
        return StringCache.toString(this);
    }
    
    public String toStringInternal() {
        return new String(this.buff, this.start, this.end - this.start);
    }
    
    @Deprecated
    public int getInt() {
        return Ascii.parseInt(this.buff, this.start, this.end - this.start);
    }
    
    public boolean equals(final String s) {
        final char[] c = this.buff;
        final int len = this.end - this.start;
        if (c == null || len != s.length()) {
            return false;
        }
        int off = this.start;
        for (int i = 0; i < len; ++i) {
            if (c[off++] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean equalsIgnoreCase(final String s) {
        final char[] c = this.buff;
        final int len = this.end - this.start;
        if (c == null || len != s.length()) {
            return false;
        }
        int off = this.start;
        for (int i = 0; i < len; ++i) {
            if (Ascii.toLower(c[off++]) != Ascii.toLower(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public boolean equals(final CharChunk cc) {
        return this.equals(cc.getChars(), cc.getOffset(), cc.getLength());
    }
    
    public boolean equals(final char[] b2, int off2, final int len2) {
        final char[] b3 = this.buff;
        if (b3 == null && b2 == null) {
            return true;
        }
        if (b3 == null || b2 == null || this.end - this.start != len2) {
            return false;
        }
        int off3 = this.start;
        int len3 = this.end - this.start;
        while (len3-- > 0) {
            if (b3[off3++] != b2[off2++]) {
                return false;
            }
        }
        return true;
    }
    
    @Deprecated
    public boolean equals(final byte[] b2, int off2, final int len2) {
        final char[] b3 = this.buff;
        if (b2 == null && b3 == null) {
            return true;
        }
        if (b3 == null || b2 == null || this.end - this.start != len2) {
            return false;
        }
        int off3 = this.start;
        int len3 = this.end - this.start;
        while (len3-- > 0) {
            if (b3[off3++] != (char)b2[off2++]) {
                return false;
            }
        }
        return true;
    }
    
    public boolean startsWith(final String s) {
        final char[] c = this.buff;
        final int len = s.length();
        if (c == null || len > this.end - this.start) {
            return false;
        }
        int off = this.start;
        for (int i = 0; i < len; ++i) {
            if (c[off++] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean startsWithIgnoreCase(final String s, final int pos) {
        final char[] c = this.buff;
        final int len = s.length();
        if (c == null || len + pos > this.end - this.start) {
            return false;
        }
        int off = this.start + pos;
        for (int i = 0; i < len; ++i) {
            if (Ascii.toLower(c[off++]) != Ascii.toLower(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public boolean endsWith(final String s) {
        final char[] c = this.buff;
        final int len = s.length();
        if (c == null || len > this.end - this.start) {
            return false;
        }
        int off = this.end - len;
        for (int i = 0; i < len; ++i) {
            if (c[off++] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    public int hash() {
        int code = 0;
        for (int i = this.start; i < this.start + this.end - this.start; ++i) {
            code = code * 37 + this.buff[i];
        }
        return code;
    }
    
    @Deprecated
    public int hashIgnoreCase() {
        int code = 0;
        for (int i = this.start; i < this.end; ++i) {
            code = code * 37 + Ascii.toLower(this.buff[i]);
        }
        return code;
    }
    
    public int indexOf(final char c) {
        return this.indexOf(c, this.start);
    }
    
    public int indexOf(final char c, final int starting) {
        final int ret = indexOf(this.buff, this.start + starting, this.end, c);
        return (ret >= this.start) ? (ret - this.start) : -1;
    }
    
    public static int indexOf(final char[] chars, int off, final int cend, final char qq) {
        while (off < cend) {
            final char b = chars[off];
            if (b == qq) {
                return off;
            }
            ++off;
        }
        return -1;
    }
    
    public int indexOf(final String src, final int srcOff, final int srcLen, final int myOff) {
        final char first = src.charAt(srcOff);
        final int srcEnd = srcOff + srcLen;
        for (int i = myOff + this.start; i <= this.end - srcLen; ++i) {
            if (this.buff[i] == first) {
                int myPos = i + 1;
                int srcPos = srcOff + 1;
                while (srcPos < srcEnd) {
                    if (this.buff[myPos++] != src.charAt(srcPos++)) {
                        break;
                    }
                    if (srcPos == srcEnd) {
                        return i - this.start;
                    }
                }
            }
        }
        return -1;
    }
    
    private int min(final int a, final int b) {
        if (a < b) {
            return a;
        }
        return b;
    }
    
    @Override
    public char charAt(final int index) {
        return this.buff[index + this.start];
    }
    
    @Override
    public CharSequence subSequence(final int start, final int end) {
        try {
            final CharChunk result = (CharChunk)this.clone();
            result.setOffset(this.start + start);
            result.setEnd(this.start + end);
            return result;
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
    
    @Override
    public int length() {
        return this.end - this.start;
    }
    
    public interface CharOutputChannel
    {
        void realWriteChars(final char[] p0, final int p1, final int p2) throws IOException;
    }
    
    public interface CharInputChannel
    {
        int realReadChars(final char[] p0, final int p1, final int p2) throws IOException;
    }
}
