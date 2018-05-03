// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

public class Constants
{
    public static final String CATALINA_BASE_PROP = "catalina.base";
    public static final boolean IS_SECURITY_ENABLED;
    
    static {
        IS_SECURITY_ENABLED = (System.getSecurityManager() != null);
    }
}
