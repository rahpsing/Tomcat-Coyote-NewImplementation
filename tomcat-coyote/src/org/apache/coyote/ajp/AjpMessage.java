// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.ajp;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;

public class AjpMessage
{
    private static final Log log;
    protected static final StringManager sm;
    protected byte[] buf;
    protected int pos;
    protected int len;
    
    public AjpMessage(final int packetSize) {
        this.buf = null;
        this.buf = new byte[packetSize];
    }
    
    public void reset() {
        this.len = 4;
        this.pos = 4;
    }
    
    public void end() {
        this.len = this.pos;
        final int dLen = this.len - 4;
        this.buf[0] = 65;
        this.buf[1] = 66;
        this.buf[2] = (byte)(dLen >>> 8 & 0xFF);
        this.buf[3] = (byte)(dLen & 0xFF);
    }
    
    public byte[] getBuffer() {
        return this.buf;
    }
    
    public int getLen() {
        return this.len;
    }
    
    public void appendInt(final int val) {
        this.buf[this.pos++] = (byte)(val >>> 8 & 0xFF);
        this.buf[this.pos++] = (byte)(val & 0xFF);
    }
    
    public void appendByte(final int val) {
        this.buf[this.pos++] = (byte)val;
    }
    
    public void appendBytes(final MessageBytes mb) {
        if (mb == null) {
            AjpMessage.log.error((Object)AjpMessage.sm.getString("ajpmessage.null"), (Throwable)new NullPointerException());
            this.appendInt(0);
            this.appendByte(0);
            return;
        }
        if (mb.getType() == 2) {
            final ByteChunk bc = mb.getByteChunk();
            this.appendByteChunk(bc);
        }
        else if (mb.getType() == 3) {
            final CharChunk cc = mb.getCharChunk();
            this.appendCharChunk(cc);
        }
        else {
            this.appendString(mb.toString());
        }
    }
    
    public void appendByteChunk(final ByteChunk bc) {
        if (bc == null) {
            AjpMessage.log.error((Object)AjpMessage.sm.getString("ajpmessage.null"), (Throwable)new NullPointerException());
            this.appendInt(0);
            this.appendByte(0);
            return;
        }
        this.appendBytes(bc.getBytes(), bc.getStart(), bc.getLength());
    }
    
    public void appendCharChunk(final CharChunk cc) {
        if (cc == null) {
            AjpMessage.log.error((Object)AjpMessage.sm.getString("ajpmessage.null"), (Throwable)new NullPointerException());
            this.appendInt(0);
            this.appendByte(0);
            return;
        }
        final int start = cc.getStart();
        final int end = cc.getEnd();
        this.appendInt(end - start);
        final char[] cbuf = cc.getBuffer();
        for (int i = start; i < end; ++i) {
            char c = cbuf[i];
            if ((c <= '\u001f' && c != '\t') || c == '\u007f' || c > '\u00ff') {
                c = ' ';
            }
            this.appendByte(c);
        }
        this.appendByte(0);
    }
    
    public void appendString(final String str) {
        if (str == null) {
            AjpMessage.log.error((Object)AjpMessage.sm.getString("ajpmessage.null"), (Throwable)new NullPointerException());
            this.appendInt(0);
            this.appendByte(0);
            return;
        }
        final int len = str.length();
        this.appendInt(len);
        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            if ((c <= '\u001f' && c != '\t') || c == '\u007f' || c > '\u00ff') {
                c = ' ';
            }
            this.appendByte(c);
        }
        this.appendByte(0);
    }
    
    public void appendBytes(final byte[] b, final int off, final int numBytes) {
        if (this.pos + numBytes + 3 > this.buf.length) {
            AjpMessage.log.error((Object)AjpMessage.sm.getString("ajpmessage.overflow", new Object[] { "" + numBytes, "" + this.pos }), (Throwable)new ArrayIndexOutOfBoundsException());
            if (AjpMessage.log.isDebugEnabled()) {
                this.dump("Overflow/coBytes");
            }
            return;
        }
        this.appendInt(numBytes);
        System.arraycopy(b, off, this.buf, this.pos, numBytes);
        this.pos += numBytes;
        this.appendByte(0);
    }
    
    public int getInt() {
        final int b1 = this.buf[this.pos++] & 0xFF;
        final int b2 = this.buf[this.pos++] & 0xFF;
        this.validatePos(this.pos);
        return (b1 << 8) + b2;
    }
    
    public int peekInt() {
        this.validatePos(this.pos + 2);
        final int b1 = this.buf[this.pos] & 0xFF;
        final int b2 = this.buf[this.pos + 1] & 0xFF;
        return (b1 << 8) + b2;
    }
    
    public byte getByte() {
        final byte res = this.buf[this.pos++];
        this.validatePos(this.pos);
        return res;
    }
    
    public void getBytes(final MessageBytes mb) {
        this.doGetBytes(mb, true);
    }
    
    public void getBodyBytes(final MessageBytes mb) {
        this.doGetBytes(mb, false);
    }
    
    private void doGetBytes(final MessageBytes mb, final boolean terminated) {
        final int length = this.getInt();
        if (length == 65535 || length == -1) {
            mb.recycle();
            return;
        }
        if (terminated) {
            this.validatePos(this.pos + length + 1);
        }
        else {
            this.validatePos(this.pos + length);
        }
        mb.setBytes(this.buf, this.pos, length);
        mb.getCharChunk().recycle();
        this.pos += length;
        if (terminated) {
            ++this.pos;
        }
    }
    
    public int getLongInt() {
        int b1 = this.buf[this.pos++] & 0xFF;
        b1 <<= 8;
        b1 |= (this.buf[this.pos++] & 0xFF);
        b1 <<= 8;
        b1 |= (this.buf[this.pos++] & 0xFF);
        b1 <<= 8;
        b1 |= (this.buf[this.pos++] & 0xFF);
        this.validatePos(this.pos);
        return b1;
    }
    
    public int getHeaderLength() {
        return 4;
    }
    
    public int getPacketSize() {
        return this.buf.length;
    }
    
    @Deprecated
    public int processHeader() {
        return this.processHeader(true);
    }
    
    public int processHeader(final boolean toContainer) {
        this.pos = 0;
        final int mark = this.getInt();
        this.len = this.getInt();
        if ((toContainer && mark != 4660) || (!toContainer && mark != 16706)) {
            AjpMessage.log.error((Object)AjpMessage.sm.getString("ajpmessage.invalid", new Object[] { "" + mark }));
            if (AjpMessage.log.isDebugEnabled()) {
                this.dump("In: ");
            }
            return -1;
        }
        if (AjpMessage.log.isDebugEnabled()) {
            AjpMessage.log.debug((Object)("Received " + this.len + " " + this.buf[0]));
        }
        return this.len;
    }
    
    public void dump(final String msg) {
        if (AjpMessage.log.isDebugEnabled()) {
            AjpMessage.log.debug((Object)(msg + ": " + HexUtils.toHexString(this.buf) + " " + this.pos + "/" + (this.len + 4)));
        }
        int max = this.pos;
        if (this.len + 4 > this.pos) {
            max = this.len + 4;
        }
        if (max > 1000) {
            max = 1000;
        }
        if (AjpMessage.log.isDebugEnabled()) {
            for (int j = 0; j < max; j += 16) {
                AjpMessage.log.debug((Object)hexLine(this.buf, j, this.len));
            }
        }
    }
    
    private void validatePos(final int posToTest) {
        if (posToTest > this.len + 4) {
            throw new ArrayIndexOutOfBoundsException(AjpMessage.sm.getString("ajpMessage.invalidPos", new Object[] { posToTest }));
        }
    }
    
    protected static String hexLine(final byte[] buf, final int start, final int len) {
        final StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + 16; ++i) {
            if (i < len + 4) {
                sb.append(hex(buf[i]) + " ");
            }
            else {
                sb.append("   ");
            }
        }
        sb.append(" | ");
        for (int i = start; i < start + 16 && i < len + 4; ++i) {
            if (!Character.isISOControl((char)buf[i])) {
                sb.append((Object)(char)buf[i]);
            }
            else {
                sb.append(".");
            }
        }
        return sb.toString();
    }
    
    protected static String hex(final int x) {
        String h = Integer.toHexString(x);
        if (h.length() == 1) {
            h = "0" + h;
        }
        return h.substring(h.length() - 2);
    }
    
    static {
        log = LogFactory.getLog((Class)AjpMessage.class);
        sm = StringManager.getManager("org.apache.coyote.ajp");
    }
}
