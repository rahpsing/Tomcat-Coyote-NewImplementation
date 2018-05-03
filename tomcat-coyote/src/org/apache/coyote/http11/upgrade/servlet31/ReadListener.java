// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11.upgrade.servlet31;

import java.io.IOException;
import java.util.EventListener;

public interface ReadListener extends EventListener
{
    void onDataAvailable() throws IOException;
    
    void onAllDataRead() throws IOException;
    
    void onError(final Throwable p0);
}
