// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

public final class Constants
{
    public static final String Package = "org.apache.coyote";
    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";
    public static final int MAX_NOTES = 32;
    public static final int STAGE_NEW = 0;
    public static final int STAGE_PARSE = 1;
    public static final int STAGE_PREPARE = 2;
    public static final int STAGE_SERVICE = 3;
    public static final int STAGE_ENDINPUT = 4;
    public static final int STAGE_ENDOUTPUT = 5;
    public static final int STAGE_KEEPALIVE = 6;
    public static final int STAGE_ENDED = 7;
    public static final boolean IS_SECURITY_ENABLED;
    public static final boolean USE_CUSTOM_STATUS_MSG_IN_HEADER;
    public static final String COMET_SUPPORTED_ATTR = "org.apache.tomcat.comet.support";
    public static final String COMET_TIMEOUT_SUPPORTED_ATTR = "org.apache.tomcat.comet.timeout.support";
    public static final String COMET_TIMEOUT_ATTR = "org.apache.tomcat.comet.timeout";
    public static final String SENDFILE_SUPPORTED_ATTR = "org.apache.tomcat.sendfile.support";
    public static final String SENDFILE_FILENAME_ATTR = "org.apache.tomcat.sendfile.filename";
    public static final String SENDFILE_FILE_START_ATTR = "org.apache.tomcat.sendfile.start";
    public static final String SENDFILE_FILE_END_ATTR = "org.apache.tomcat.sendfile.end";
    
    static {
        IS_SECURITY_ENABLED = (System.getSecurityManager() != null);
        USE_CUSTOM_STATUS_MSG_IN_HEADER = Boolean.valueOf(System.getProperty("org.apache.coyote.USE_CUSTOM_STATUS_MSG_IN_HEADER", "false"));
    }
}
