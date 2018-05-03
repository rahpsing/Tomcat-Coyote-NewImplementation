// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

import java.util.Iterator;
import java.util.HashMap;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;

public class B2CConverter
{
    private static final StringManager sm;
    private static final Map<String, Charset> encodingToCharsetCache;
    public static final Charset ISO_8859_1;
    public static final Charset UTF_8;
    protected static final int LEFTOVER_SIZE = 9;
    private final CharsetDecoder decoder;
    private ByteBuffer bb;
    private CharBuffer cb;
    private final ByteBuffer leftovers;
    
    public static Charset getCharset(final String enc) throws UnsupportedEncodingException {
        final String lowerCaseEnc = enc.toLowerCase(Locale.ENGLISH);
        return getCharsetLower(lowerCaseEnc);
    }
    
    public static Charset getCharsetLower(final String lowerCaseEnc) throws UnsupportedEncodingException {
        final Charset charset = B2CConverter.encodingToCharsetCache.get(lowerCaseEnc);
        if (charset == null) {
            throw new UnsupportedEncodingException(B2CConverter.sm.getString("b2cConverter.unknownEncoding", new Object[] { lowerCaseEnc }));
        }
        return charset;
    }
    
    public B2CConverter(final String encoding) throws IOException {
        this(encoding, false);
    }
    
    public B2CConverter(final String encoding, final boolean replaceOnError) throws IOException {
        this.bb = null;
        this.cb = null;
        final byte[] left = new byte[9];
        this.leftovers = ByteBuffer.wrap(left);
        CodingErrorAction action;
        if (replaceOnError) {
            action = CodingErrorAction.REPLACE;
        }
        else {
            action = CodingErrorAction.REPORT;
        }
        final Charset charset = getCharset(encoding);
        if (charset.equals(B2CConverter.UTF_8)) {
            this.decoder = new Utf8Decoder();
        }
        else {
            this.decoder = charset.newDecoder();
        }
        this.decoder.onMalformedInput(action);
        this.decoder.onUnmappableCharacter(action);
    }
    
    public void recycle() {
        this.decoder.reset();
        this.leftovers.position(0);
    }
    
    public void convert(final ByteChunk bc, final CharChunk cc, final boolean endOfInput) throws IOException {
        if (this.bb == null || this.bb.array() != bc.getBuffer()) {
            this.bb = ByteBuffer.wrap(bc.getBuffer(), bc.getStart(), bc.getLength());
        }
        else {
            this.bb.limit(bc.getEnd());
            this.bb.position(bc.getStart());
        }
        if (this.cb == null || this.cb.array() != cc.getBuffer()) {
            this.cb = CharBuffer.wrap(cc.getBuffer(), cc.getEnd(), cc.getBuffer().length - cc.getEnd());
        }
        else {
            this.cb.limit(cc.getBuffer().length);
            this.cb.position(cc.getEnd());
        }
        CoderResult result = null;
        if (this.leftovers.position() > 0) {
            final int pos = this.cb.position();
            do {
                this.leftovers.put(bc.substractB());
                this.leftovers.flip();
                result = this.decoder.decode(this.leftovers, this.cb, endOfInput);
                this.leftovers.position(this.leftovers.limit());
                this.leftovers.limit(this.leftovers.array().length);
            } while (result.isUnderflow() && this.cb.position() == pos);
            if (result.isError() || result.isMalformed()) {
                result.throwException();
            }
            this.bb.position(bc.getStart());
            this.leftovers.position(0);
        }
        result = this.decoder.decode(this.bb, this.cb, endOfInput);
        if (result.isError() || result.isMalformed()) {
            result.throwException();
        }
        else if (result.isOverflow()) {
            bc.setOffset(this.bb.position());
            cc.setEnd(this.cb.position());
        }
        else if (result.isUnderflow()) {
            bc.setOffset(this.bb.position());
            cc.setEnd(this.cb.position());
            if (bc.getLength() > 0) {
                this.leftovers.limit(this.leftovers.array().length);
                this.leftovers.position(bc.getLength());
                bc.substract(this.leftovers.array(), 0, bc.getLength());
            }
        }
    }
    
    static {
        sm = StringManager.getManager("org.apache.tomcat.util.buf");
        encodingToCharsetCache = new HashMap<String, Charset>();
        for (final Charset charset : Charset.availableCharsets().values()) {
            B2CConverter.encodingToCharsetCache.put(charset.name().toLowerCase(Locale.ENGLISH), charset);
            for (final String alias : charset.aliases()) {
                B2CConverter.encodingToCharsetCache.put(alias.toLowerCase(Locale.ENGLISH), charset);
            }
        }
        Charset iso88591 = null;
        Charset utf8 = null;
        try {
            iso88591 = getCharset("ISO-8859-1");
            utf8 = getCharset("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ISO_8859_1 = iso88591;
        UTF_8 = utf8;
    }
}
