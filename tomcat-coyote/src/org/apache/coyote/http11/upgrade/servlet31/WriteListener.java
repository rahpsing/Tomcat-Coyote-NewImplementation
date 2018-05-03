// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade.servlet31;

import java.io.IOException;
import java.util.EventListener;

public interface WriteListener extends EventListener
{
    void onWritePossible() throws IOException;
    
    void onError(final Throwable p0);
}
