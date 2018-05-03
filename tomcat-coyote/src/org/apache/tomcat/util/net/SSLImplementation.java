// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import org.apache.juli.logging.LogFactory;
import javax.net.ssl.SSLSession;
import java.net.Socket;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.apache.juli.logging.Log;

public abstract class SSLImplementation
{
    private static final Log logger;
    private static final String JSSEImplementationClass = "org.apache.tomcat.util.net.jsse.JSSEImplementation";
    private static final String[] implementations;
    
    public static SSLImplementation getInstance() throws ClassNotFoundException {
        int i = 0;
        while (i < SSLImplementation.implementations.length) {
            try {
                final SSLImplementation impl = getInstance(SSLImplementation.implementations[i]);
                return impl;
            }
            catch (Exception e) {
                if (SSLImplementation.logger.isTraceEnabled()) {
                    SSLImplementation.logger.trace((Object)("Error creating " + SSLImplementation.implementations[i]), (Throwable)e);
                }
                ++i;
                continue;
            }
            break;
        }
        throw new ClassNotFoundException("Can't find any SSL implementation");
    }
    
    public static SSLImplementation getInstance(final String className) throws ClassNotFoundException {
        if (className == null) {
            return getInstance();
        }
        try {
            if ("org.apache.tomcat.util.net.jsse.JSSEImplementation".equals(className)) {
                return new JSSEImplementation();
            }
            final Class<?> clazz = Class.forName(className);
            return (SSLImplementation)clazz.newInstance();
        }
        catch (Exception e) {
            if (SSLImplementation.logger.isDebugEnabled()) {
                SSLImplementation.logger.debug((Object)("Error loading SSL Implementation " + className), (Throwable)e);
            }
            throw new ClassNotFoundException("Error loading SSL Implementation " + className + " :" + e.toString());
        }
    }
    
    public abstract String getImplementationName();
    
    public abstract ServerSocketFactory getServerSocketFactory(final AbstractEndpoint p0);
    
    public abstract SSLSupport getSSLSupport(final Socket p0);
    
    public abstract SSLSupport getSSLSupport(final SSLSession p0);
    
    public abstract SSLUtil getSSLUtil(final AbstractEndpoint p0);
    
    static {
        logger = LogFactory.getLog((Class)SSLImplementation.class);
        implementations = new String[] { "org.apache.tomcat.util.net.jsse.JSSEImplementation" };
    }
}
