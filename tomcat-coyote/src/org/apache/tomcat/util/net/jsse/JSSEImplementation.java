// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net.jsse;

import org.apache.tomcat.util.net.SSLUtil;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.tomcat.util.net.SSLSupport;
import java.net.Socket;
import org.apache.tomcat.util.net.ServerSocketFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLImplementation;

public class JSSEImplementation extends SSLImplementation
{
    @Override
    public String getImplementationName() {
        return "JSSE";
    }
    
    @Override
    public ServerSocketFactory getServerSocketFactory(final AbstractEndpoint endpoint) {
        return new JSSESocketFactory(endpoint);
    }
    
    @Override
    public SSLSupport getSSLSupport(final Socket s) {
        return new JSSESupport((SSLSocket)s);
    }
    
    @Override
    public SSLSupport getSSLSupport(final SSLSession session) {
        return new JSSESupport(session);
    }
    
    @Override
    public SSLUtil getSSLUtil(final AbstractEndpoint endpoint) {
        return new JSSESocketFactory(endpoint);
    }
}
