// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.codec;

public interface BinaryEncoder extends Encoder
{
    byte[] encode(final byte[] p0) throws EncoderException;
}
