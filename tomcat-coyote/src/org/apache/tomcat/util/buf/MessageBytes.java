// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

import java.io.IOException;
import java.util.Locale;
import java.nio.charset.Charset;
import java.io.Serializable;

public final class MessageBytes implements Cloneable, Serializable
{
    private static final long serialVersionUID = 1L;
    private int type;
    public static final int T_NULL = 0;
    public static final int T_STR = 1;
    public static final int T_BYTES = 2;
    public static final int T_CHARS = 3;
    private int hashCode;
    private boolean hasHashCode;
    private final ByteChunk byteC;
    private final CharChunk charC;
    private String strValue;
    private boolean hasStrValue;
    private int intValue;
    private boolean hasIntValue;
    private long longValue;
    private boolean hasLongValue;
    private static MessageBytesFactory factory;
    
    private MessageBytes() {
        this.type = 0;
        this.hashCode = 0;
        this.hasHashCode = false;
        this.byteC = new ByteChunk();
        this.charC = new CharChunk();
        this.hasStrValue = false;
        this.hasIntValue = false;
        this.hasLongValue = false;
    }
    
    public static MessageBytes newInstance() {
        return MessageBytes.factory.newInstance();
    }
    
    @Deprecated
    public MessageBytes getClone() {
        try {
            return (MessageBytes)this.clone();
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public boolean isNull() {
        return this.byteC.isNull() && this.charC.isNull() && !this.hasStrValue;
    }
    
    public void recycle() {
        this.type = 0;
        this.byteC.recycle();
        this.charC.recycle();
        this.strValue = null;
        this.hasStrValue = false;
        this.hasHashCode = false;
        this.hasIntValue = false;
        this.hasLongValue = false;
    }
    
    public void setBytes(final byte[] b, final int off, final int len) {
        this.byteC.setBytes(b, off, len);
        this.type = 2;
        this.hasStrValue = false;
        this.hasHashCode = false;
        this.hasIntValue = false;
        this.hasLongValue = false;
    }
    
    @Deprecated
    public void setCharset(final Charset charset) {
        if (!this.byteC.isNull()) {
            this.charC.recycle();
            this.hasStrValue = false;
        }
        this.byteC.setCharset(charset);
    }
    
    public void setChars(final char[] c, final int off, final int len) {
        this.charC.setChars(c, off, len);
        this.type = 3;
        this.hasStrValue = false;
        this.hasHashCode = false;
        this.hasIntValue = false;
        this.hasLongValue = false;
    }
    
    public void setString(final String s) {
        this.strValue = s;
        this.hasHashCode = false;
        this.hasIntValue = false;
        this.hasLongValue = false;
        if (s == null) {
            this.hasStrValue = false;
            this.type = 0;
        }
        else {
            this.hasStrValue = true;
            this.type = 1;
        }
    }
    
    @Override
    public String toString() {
        if (this.hasStrValue) {
            return this.strValue;
        }
        switch (this.type) {
            case 3: {
                this.strValue = this.charC.toString();
                this.hasStrValue = true;
                return this.strValue;
            }
            case 2: {
                this.strValue = this.byteC.toString();
                this.hasStrValue = true;
                return this.strValue;
            }
            default: {
                return null;
            }
        }
    }
    
    public int getType() {
        return this.type;
    }
    
    public ByteChunk getByteChunk() {
        return this.byteC;
    }
    
    public CharChunk getCharChunk() {
        return this.charC;
    }
    
    public String getString() {
        return this.strValue;
    }
    
    public void toBytes() {
        if (!this.byteC.isNull()) {
            this.type = 2;
            return;
        }
        this.toString();
        this.type = 2;
        final byte[] bb = this.strValue.getBytes(Charset.defaultCharset());
        this.byteC.setBytes(bb, 0, bb.length);
    }
    
    public void toChars() {
        if (!this.charC.isNull()) {
            this.type = 3;
            return;
        }
        this.toString();
        this.type = 3;
        final char[] cc = this.strValue.toCharArray();
        this.charC.setChars(cc, 0, cc.length);
    }
    
    public int getLength() {
        if (this.type == 2) {
            return this.byteC.getLength();
        }
        if (this.type == 3) {
            return this.charC.getLength();
        }
        if (this.type == 1) {
            return this.strValue.length();
        }
        this.toString();
        if (this.strValue == null) {
            return 0;
        }
        return this.strValue.length();
    }
    
    public boolean equals(final String s) {
        switch (this.type) {
            case 1: {
                if (this.strValue == null) {
                    return s == null;
                }
                return this.strValue.equals(s);
            }
            case 3: {
                return this.charC.equals(s);
            }
            case 2: {
                return this.byteC.equals(s);
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean equalsIgnoreCase(final String s) {
        switch (this.type) {
            case 1: {
                if (this.strValue == null) {
                    return s == null;
                }
                return this.strValue.equalsIgnoreCase(s);
            }
            case 3: {
                return this.charC.equalsIgnoreCase(s);
            }
            case 2: {
                return this.byteC.equalsIgnoreCase(s);
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof MessageBytes && this.equals((MessageBytes)obj);
    }
    
    public boolean equals(final MessageBytes mb) {
        switch (this.type) {
            case 1: {
                return mb.equals(this.strValue);
            }
            default: {
                if (mb.type != 3 && mb.type != 2) {
                    return this.equals(mb.toString());
                }
                if (mb.type == 3 && this.type == 3) {
                    return this.charC.equals(mb.charC);
                }
                if (mb.type == 2 && this.type == 2) {
                    return this.byteC.equals(mb.byteC);
                }
                if (mb.type == 3 && this.type == 2) {
                    return this.byteC.equals(mb.charC);
                }
                return mb.type != 2 || this.type != 3 || mb.byteC.equals(this.charC);
            }
        }
    }
    
    @Deprecated
    public boolean startsWith(final String s) {
        switch (this.type) {
            case 1: {
                return this.strValue.startsWith(s);
            }
            case 3: {
                return this.charC.startsWith(s);
            }
            case 2: {
                return this.byteC.startsWith(s);
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean startsWithIgnoreCase(final String s, final int pos) {
        switch (this.type) {
            case 1: {
                if (this.strValue == null) {
                    return false;
                }
                if (this.strValue.length() < pos + s.length()) {
                    return false;
                }
                for (int i = 0; i < s.length(); ++i) {
                    if (Ascii.toLower(s.charAt(i)) != Ascii.toLower(this.strValue.charAt(pos + i))) {
                        return false;
                    }
                }
                return true;
            }
            case 3: {
                return this.charC.startsWithIgnoreCase(s, pos);
            }
            case 2: {
                return this.byteC.startsWithIgnoreCase(s, pos);
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public int hashCode() {
        if (this.hasHashCode) {
            return this.hashCode;
        }
        int code = 0;
        code = this.hash();
        this.hashCode = code;
        this.hasHashCode = true;
        return code;
    }
    
    private int hash() {
        int code = 0;
        switch (this.type) {
            case 1: {
                for (int i = 0; i < this.strValue.length(); ++i) {
                    code = code * 37 + this.strValue.charAt(i);
                }
                return code;
            }
            case 3: {
                return this.charC.hash();
            }
            case 2: {
                return this.byteC.hash();
            }
            default: {
                return 0;
            }
        }
    }
    
    @Deprecated
    public int indexOf(final char c) {
        return this.indexOf(c, 0);
    }
    
    public int indexOf(final String s, final int starting) {
        this.toString();
        return this.strValue.indexOf(s, starting);
    }
    
    public int indexOf(final String s) {
        return this.indexOf(s, 0);
    }
    
    public int indexOfIgnoreCase(final String s, final int starting) {
        this.toString();
        final String upper = this.strValue.toUpperCase(Locale.ENGLISH);
        final String sU = s.toUpperCase(Locale.ENGLISH);
        return upper.indexOf(sU, starting);
    }
    
    @Deprecated
    public int indexOf(final char c, final int starting) {
        switch (this.type) {
            case 1: {
                return this.strValue.indexOf(c, starting);
            }
            case 3: {
                return this.charC.indexOf(c, starting);
            }
            case 2: {
                return this.byteC.indexOf(c, starting);
            }
            default: {
                return -1;
            }
        }
    }
    
    public void duplicate(final MessageBytes src) throws IOException {
        switch (src.getType()) {
            case 2: {
                this.type = 2;
                final ByteChunk bc = src.getByteChunk();
                this.byteC.allocate(2 * bc.getLength(), -1);
                this.byteC.append(bc);
                break;
            }
            case 3: {
                this.type = 3;
                final CharChunk cc = src.getCharChunk();
                this.charC.allocate(2 * cc.getLength(), -1);
                this.charC.append(cc);
                break;
            }
            case 1: {
                this.type = 1;
                final String sc = src.getString();
                this.setString(sc);
                break;
            }
        }
    }
    
    @Deprecated
    public void setInt(final int i) {
        this.byteC.allocate(16, 32);
        int current = i;
        final byte[] buf = this.byteC.getBuffer();
        int start = 0;
        int end = 0;
        if (i == 0) {
            buf[end++] = 48;
        }
        if (i < 0) {
            current = -i;
            buf[end++] = 45;
        }
        while (current > 0) {
            final int digit = current % 10;
            current /= 10;
            buf[end++] = HexUtils.getHex(digit);
        }
        this.byteC.setOffset(0);
        this.byteC.setEnd(end);
        --end;
        if (i < 0) {
            ++start;
        }
        while (end > start) {
            final byte temp = buf[start];
            buf[start] = buf[end];
            buf[end] = temp;
            ++start;
            --end;
        }
        this.intValue = i;
        this.hasStrValue = false;
        this.hasHashCode = false;
        this.hasIntValue = true;
        this.hasLongValue = false;
        this.type = 2;
    }
    
    public void setLong(final long l) {
        this.byteC.allocate(32, 64);
        long current = l;
        final byte[] buf = this.byteC.getBuffer();
        int start = 0;
        int end = 0;
        if (l == 0L) {
            buf[end++] = 48;
        }
        if (l < 0L) {
            current = -l;
            buf[end++] = 45;
        }
        while (current > 0L) {
            final int digit = (int)(current % 10L);
            current /= 10L;
            buf[end++] = HexUtils.getHex(digit);
        }
        this.byteC.setOffset(0);
        this.byteC.setEnd(end);
        --end;
        if (l < 0L) {
            ++start;
        }
        while (end > start) {
            final byte temp = buf[start];
            buf[start] = buf[end];
            buf[end] = temp;
            ++start;
            --end;
        }
        this.longValue = l;
        this.hasStrValue = false;
        this.hasHashCode = false;
        this.hasIntValue = false;
        this.hasLongValue = true;
        this.type = 2;
    }
    
    @Deprecated
    public int getInt() {
        if (this.hasIntValue) {
            return this.intValue;
        }
        switch (this.type) {
            case 2: {
                this.intValue = this.byteC.getInt();
                break;
            }
            default: {
                this.intValue = Integer.parseInt(this.toString());
                break;
            }
        }
        this.hasIntValue = true;
        return this.intValue;
    }
    
    public long getLong() {
        if (this.hasLongValue) {
            return this.longValue;
        }
        switch (this.type) {
            case 2: {
                this.longValue = this.byteC.getLong();
                break;
            }
            default: {
                this.longValue = Long.parseLong(this.toString());
                break;
            }
        }
        this.hasLongValue = true;
        return this.longValue;
    }
    
    @Deprecated
    public static void setFactory(final MessageBytesFactory mbf) {
        MessageBytes.factory = mbf;
    }
    
    static {
        MessageBytes.factory = new MessageBytesFactory();
    }
    
    public static class MessageBytesFactory
    {
        public MessageBytes newInstance() {
            return new MessageBytes(null);
        }
    }
}
