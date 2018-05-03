// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade;

import java.io.IOException;
import java.io.OutputStream;

@Deprecated
public class UpgradeOutbound extends OutputStream
{
    private final UpgradeProcessor<?> processor;
    
    @Override
    public void flush() throws IOException {
        this.processor.flush();
    }
    
    public UpgradeOutbound(final UpgradeProcessor<?> processor) {
        this.processor = processor;
    }
    
    @Override
    public void write(final int b) throws IOException {
        this.processor.write(b);
    }
    
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.processor.write(b, off, len);
    }
}
