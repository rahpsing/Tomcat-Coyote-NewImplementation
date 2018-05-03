// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.buf;

import java.nio.charset.CoderResult;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;

public class Utf8Encoder extends CharsetEncoder
{
    public Utf8Encoder() {
        super(B2CConverter.UTF_8, 1.1f, 4.0f);
    }
    
    @Override
    protected CoderResult encodeLoop(final CharBuffer in, final ByteBuffer out) {
        if (in.hasArray() && out.hasArray()) {
            return this.encodeHasArray(in, out);
        }
        return this.encodeNotHasArray(in, out);
    }
    
    private CoderResult encodeHasArray(final CharBuffer in, final ByteBuffer out) {
        int outRemaining = out.remaining();
        final int pos = in.position();
        final int limit = in.limit();
        int x = pos;
        final byte[] bArr = out.array();
        final char[] cArr = in.array();
        int outPos = out.position();
        final int rem = in.remaining();
        x = pos;
        while (x < pos + rem) {
            final int jchar = cArr[x] & '\uffff';
            if (jchar <= 127) {
                if (outRemaining < 1) {
                    in.position(x);
                    out.position(outPos);
                    return CoderResult.OVERFLOW;
                }
                bArr[outPos++] = (byte)(jchar & 0xFF);
                --outRemaining;
            }
            else if (jchar <= 2047) {
                if (outRemaining < 2) {
                    in.position(x);
                    out.position(outPos);
                    return CoderResult.OVERFLOW;
                }
                bArr[outPos++] = (byte)(192 + (jchar >> 6 & 0x1F));
                bArr[outPos++] = (byte)(128 + (jchar & 0x3F));
                outRemaining -= 2;
            }
            else if (jchar >= 55296 && jchar <= 57343) {
                if (limit <= x + 1) {
                    in.position(x);
                    out.position(outPos);
                    return CoderResult.UNDERFLOW;
                }
                if (outRemaining < 4) {
                    in.position(x);
                    out.position(outPos);
                    return CoderResult.OVERFLOW;
                }
                if (jchar >= 56320) {
                    in.position(x);
                    out.position(outPos);
                    return CoderResult.malformedForLength(1);
                }
                final int jchar2 = cArr[x + 1] & '\uffff';
                if (jchar2 < 56320) {
                    in.position(x);
                    out.position(outPos);
                    return CoderResult.malformedForLength(1);
                }
                final int n = (jchar << 10) + jchar2 - 56613888;
                bArr[outPos++] = (byte)(240 + (n >> 18 & 0x7));
                bArr[outPos++] = (byte)(128 + (n >> 12 & 0x3F));
                bArr[outPos++] = (byte)(128 + (n >> 6 & 0x3F));
                bArr[outPos++] = (byte)(128 + (n & 0x3F));
                outRemaining -= 4;
                ++x;
            }
            else {
                if (outRemaining < 3) {
                    in.position(x);
                    out.position(outPos);
                    return CoderResult.OVERFLOW;
                }
                bArr[outPos++] = (byte)(224 + (jchar >> 12 & 0xF));
                bArr[outPos++] = (byte)(128 + (jchar >> 6 & 0x3F));
                bArr[outPos++] = (byte)(128 + (jchar & 0x3F));
                outRemaining -= 3;
            }
            if (outRemaining == 0) {
                in.position(x + 1);
                out.position(outPos);
                if (x + 1 == limit) {
                    return CoderResult.UNDERFLOW;
                }
                return CoderResult.OVERFLOW;
            }
            else {
                ++x;
            }
        }
        if (rem != 0) {
            in.position(x);
            out.position(outPos);
        }
        return CoderResult.UNDERFLOW;
    }
    
    private CoderResult encodeNotHasArray(final CharBuffer in, final ByteBuffer out) {
        int outRemaining = out.remaining();
        int pos = in.position();
        final int limit = in.limit();
        try {
            while (pos < limit) {
                if (outRemaining == 0) {
                    return CoderResult.OVERFLOW;
                }
                final int jchar = in.get() & '\uffff';
                if (jchar <= 127) {
                    if (outRemaining < 1) {
                        return CoderResult.OVERFLOW;
                    }
                    out.put((byte)jchar);
                    --outRemaining;
                }
                else if (jchar <= 2047) {
                    if (outRemaining < 2) {
                        return CoderResult.OVERFLOW;
                    }
                    out.put((byte)(192 + (jchar >> 6 & 0x1F)));
                    out.put((byte)(128 + (jchar & 0x3F)));
                    outRemaining -= 2;
                }
                else if (jchar >= 55296 && jchar <= 57343) {
                    if (limit <= pos + 1) {
                        return CoderResult.UNDERFLOW;
                    }
                    if (outRemaining < 4) {
                        return CoderResult.OVERFLOW;
                    }
                    if (jchar >= 56320) {
                        return CoderResult.malformedForLength(1);
                    }
                    final int jchar2 = in.get() & '\uffff';
                    if (jchar2 < 56320) {
                        return CoderResult.malformedForLength(1);
                    }
                    final int n = (jchar << 10) + jchar2 - 56613888;
                    out.put((byte)(240 + (n >> 18 & 0x7)));
                    out.put((byte)(128 + (n >> 12 & 0x3F)));
                    out.put((byte)(128 + (n >> 6 & 0x3F)));
                    out.put((byte)(128 + (n & 0x3F)));
                    outRemaining -= 4;
                    ++pos;
                }
                else {
                    if (outRemaining < 3) {
                        return CoderResult.OVERFLOW;
                    }
                    out.put((byte)(224 + (jchar >> 12 & 0xF)));
                    out.put((byte)(128 + (jchar >> 6 & 0x3F)));
                    out.put((byte)(128 + (jchar & 0x3F)));
                    outRemaining -= 3;
                }
                ++pos;
            }
        }
        finally {
            in.position(pos);
        }
        return CoderResult.UNDERFLOW;
    }
}
