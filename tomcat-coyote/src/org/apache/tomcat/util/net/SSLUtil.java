// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;

public interface SSLUtil
{
    SSLContext createSSLContext() throws Exception;
    
    KeyManager[] getKeyManagers() throws Exception;
    
    TrustManager[] getTrustManagers() throws Exception;
    
    void configureSessionContext(final SSLSessionContext p0);
    
    String[] getEnableableCiphers(final SSLContext p0);
    
    String[] getEnableableProtocols(final SSLContext p0);
}
