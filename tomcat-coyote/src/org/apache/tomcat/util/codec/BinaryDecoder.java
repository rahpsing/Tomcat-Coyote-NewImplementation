// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.codec;

public interface BinaryDecoder extends Decoder
{
    byte[] decode(final byte[] p0) throws DecoderException;
}
