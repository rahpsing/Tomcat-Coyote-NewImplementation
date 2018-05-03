// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import java.util.concurrent.Executor;

public interface ProtocolHandler
{
    void setAdapter(final Adapter p0);
    
    Adapter getAdapter();
    
    Executor getExecutor();
    
    void init() throws Exception;
    
    void start() throws Exception;
    
    void pause() throws Exception;
    
    void resume() throws Exception;
    
    void stop() throws Exception;
    
    void destroy() throws Exception;
    
    boolean isAprRequired();
}
