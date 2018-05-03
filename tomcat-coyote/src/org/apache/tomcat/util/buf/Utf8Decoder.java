// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

import java.nio.charset.CoderResult;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;

public class Utf8Decoder extends CharsetDecoder
{
    private static final int[] remainingBytes;
    private static final int[] remainingNumbers;
    private static final int[] lowerEncodingLimit;
    
    public Utf8Decoder() {
        super(B2CConverter.UTF_8, 1.0f, 1.0f);
    }
    
    @Override
    protected CoderResult decodeLoop(final ByteBuffer in, final CharBuffer out) {
        if (in.hasArray() && out.hasArray()) {
            return this.decodeHasArray(in, out);
        }
        return this.decodeNotHasArray(in, out);
    }
    
    private CoderResult decodeNotHasArray(final ByteBuffer in, final CharBuffer out) {
        int outRemaining = out.remaining();
        int pos = in.position();
        final int limit = in.limit();
        try {
            while (pos < limit) {
                if (outRemaining == 0) {
                    return CoderResult.OVERFLOW;
                }
                int jchar = in.get();
                if (jchar < 0) {
                    jchar &= 0x7F;
                    final int tail = Utf8Decoder.remainingBytes[jchar];
                    if (tail == -1) {
                        return CoderResult.malformedForLength(1);
                    }
                    if (limit - pos < 1 + tail) {
                        return CoderResult.UNDERFLOW;
                    }
                    for (int i = 0; i < tail; ++i) {
                        final int nextByte = in.get() & 0xFF;
                        if ((nextByte & 0xC0) != 0x80) {
                            return CoderResult.malformedForLength(1 + i);
                        }
                        jchar = (jchar << 6) + nextByte;
                    }
                    jchar -= Utf8Decoder.remainingNumbers[tail];
                    if (jchar < Utf8Decoder.lowerEncodingLimit[tail]) {
                        return CoderResult.malformedForLength(1);
                    }
                    pos += tail;
                }
                if (jchar >= 55296 && jchar <= 57343) {
                    return CoderResult.unmappableForLength(3);
                }
                if (jchar > 1114111) {
                    return CoderResult.unmappableForLength(4);
                }
                if (jchar <= 65535) {
                    out.put((char)jchar);
                    --outRemaining;
                }
                else {
                    if (outRemaining < 2) {
                        return CoderResult.OVERFLOW;
                    }
                    out.put((char)((jchar >> 10) + 55232));
                    out.put((char)((jchar & 0x3FF) + 56320));
                    outRemaining -= 2;
                }
                ++pos;
            }
            return CoderResult.UNDERFLOW;
        }
        finally {
            in.position(pos);
        }
    }
    
    private CoderResult decodeHasArray(final ByteBuffer in, final CharBuffer out) {
        int outRemaining = out.remaining();
        final int pos = in.position();
        final int limit = in.limit();
        final byte[] bArr = in.array();
        final char[] cArr = out.array();
        final int inIndexLimit = limit + in.arrayOffset();
        int inIndex = pos + in.arrayOffset();
        int outIndex = out.position() + out.arrayOffset();
        while (inIndex < inIndexLimit && outRemaining > 0) {
            int jchar = bArr[inIndex];
            if (jchar < 0) {
                jchar &= 0x7F;
                final int tail = Utf8Decoder.remainingBytes[jchar];
                if (tail == -1) {
                    in.position(inIndex - in.arrayOffset());
                    out.position(outIndex - out.arrayOffset());
                    return CoderResult.malformedForLength(1);
                }
                final int tailAvailable = inIndexLimit - inIndex - 1;
                if (tailAvailable > 0) {
                    if (jchar > 65 && jchar < 96 && (bArr[inIndex + 1] & 0xC0) != 0x80) {
                        in.position(inIndex - in.arrayOffset());
                        out.position(outIndex - out.arrayOffset());
                        return CoderResult.malformedForLength(1);
                    }
                    if (jchar == 96 && (bArr[inIndex + 1] & 0xE0) != 0xA0) {
                        in.position(inIndex - in.arrayOffset());
                        out.position(outIndex - out.arrayOffset());
                        return CoderResult.malformedForLength(1);
                    }
                    if (jchar > 96 && jchar < 109 && (bArr[inIndex + 1] & 0xC0) != 0x80) {
                        in.position(inIndex - in.arrayOffset());
                        out.position(outIndex - out.arrayOffset());
                        return CoderResult.malformedForLength(1);
                    }
                    if (jchar == 109 && (bArr[inIndex + 1] & 0xE0) != 0x80) {
                        in.position(inIndex - in.arrayOffset());
                        out.position(outIndex - out.arrayOffset());
                        return CoderResult.malformedForLength(1);
                    }
                    if (jchar > 109 && jchar < 112 && (bArr[inIndex + 1] & 0xC0) != 0x80) {
                        in.position(inIndex - in.arrayOffset());
                        out.position(outIndex - out.arrayOffset());
                        return CoderResult.malformedForLength(1);
                    }
                    if (jchar == 112 && ((bArr[inIndex + 1] & 0xFF) < 144 || (bArr[inIndex + 1] & 0xFF) > 191)) {
                        in.position(inIndex - in.arrayOffset());
                        out.position(outIndex - out.arrayOffset());
                        return CoderResult.malformedForLength(1);
                    }
                    if (jchar > 112 && jchar < 116 && (bArr[inIndex + 1] & 0xC0) != 0x80) {
                        in.position(inIndex - in.arrayOffset());
                        out.position(outIndex - out.arrayOffset());
                        return CoderResult.malformedForLength(1);
                    }
                    if (jchar == 116 && (bArr[inIndex + 1] & 0xF0) != 0x80) {
                        in.position(inIndex - in.arrayOffset());
                        out.position(outIndex - out.arrayOffset());
                        return CoderResult.malformedForLength(1);
                    }
                }
                if (tailAvailable > 1 && tail > 1 && (bArr[inIndex + 2] & 0xC0) != 0x80) {
                    in.position(inIndex - in.arrayOffset());
                    out.position(outIndex - out.arrayOffset());
                    return CoderResult.malformedForLength(2);
                }
                if (tailAvailable > 2 && tail > 2 && (bArr[inIndex + 3] & 0xC0) != 0x80) {
                    in.position(inIndex - in.arrayOffset());
                    out.position(outIndex - out.arrayOffset());
                    return CoderResult.malformedForLength(3);
                }
                if (tailAvailable < tail) {
                    break;
                }
                for (int i = 0; i < tail; ++i) {
                    final int nextByte = bArr[inIndex + i + 1] & 0xFF;
                    if ((nextByte & 0xC0) != 0x80) {
                        in.position(inIndex - in.arrayOffset());
                        out.position(outIndex - out.arrayOffset());
                        return CoderResult.malformedForLength(1 + i);
                    }
                    jchar = (jchar << 6) + nextByte;
                }
                jchar -= Utf8Decoder.remainingNumbers[tail];
                if (jchar < Utf8Decoder.lowerEncodingLimit[tail]) {
                    in.position(inIndex - in.arrayOffset());
                    out.position(outIndex - out.arrayOffset());
                    return CoderResult.malformedForLength(1);
                }
                inIndex += tail;
            }
            if (jchar >= 55296 && jchar <= 57343) {
                return CoderResult.unmappableForLength(3);
            }
            if (jchar > 1114111) {
                return CoderResult.unmappableForLength(4);
            }
            if (jchar <= 65535) {
                cArr[outIndex++] = (char)jchar;
                --outRemaining;
            }
            else {
                if (outRemaining < 2) {
                    return CoderResult.OVERFLOW;
                }
                cArr[outIndex++] = (char)((jchar >> 10) + 55232);
                cArr[outIndex++] = (char)((jchar & 0x3FF) + 56320);
                outRemaining -= 2;
            }
            ++inIndex;
        }
        in.position(inIndex - in.arrayOffset());
        out.position(outIndex - out.arrayOffset());
        return (outRemaining == 0 && inIndex < inIndexLimit) ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
    }
    
    static {
        remainingBytes = new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
        remainingNumbers = new int[] { 0, 4224, 401536, 29892736 };
        lowerEncodingLimit = new int[] { -1, 128, 2048, 65536 };
    }
}
