// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.log;

import org.apache.juli.logging.Log;

public class UserDataHelper
{
    private final Log log;
    private final Config config;
    private final long suppressionTime;
    private volatile long lastInfoTime;
    
    public UserDataHelper(final Log log) {
        this.lastInfoTime = 0L;
        this.log = log;
        final String configString = System.getProperty("org.apache.juli.logging.UserDataHelper.CONFIG");
        Config tempConfig;
        if (configString == null) {
            tempConfig = Config.INFO_THEN_DEBUG;
        }
        else {
            try {
                tempConfig = Config.valueOf(configString);
            }
            catch (IllegalArgumentException iae) {
                tempConfig = Config.INFO_THEN_DEBUG;
            }
        }
        this.suppressionTime = Integer.getInteger("org.apache.juli.logging.UserDataHelper.SUPPRESSION_TIME", 86400) * 1000L;
        if (this.suppressionTime == 0L) {
            tempConfig = Config.INFO_ALL;
        }
        this.config = tempConfig;
    }
    
    public Mode getNextMode() {
        if (Config.NONE == this.config) {
            return null;
        }
        if (Config.DEBUG_ALL == this.config) {
            return this.log.isDebugEnabled() ? Mode.DEBUG : null;
        }
        if (Config.INFO_THEN_DEBUG == this.config) {
            if (this.logAtInfo()) {
                return this.log.isInfoEnabled() ? Mode.INFO_THEN_DEBUG : null;
            }
            return this.log.isDebugEnabled() ? Mode.DEBUG : null;
        }
        else {
            if (Config.INFO_ALL == this.config) {
                return this.log.isInfoEnabled() ? Mode.INFO : null;
            }
            return null;
        }
    }
    
    private boolean logAtInfo() {
        if (this.suppressionTime < 0L && this.lastInfoTime > 0L) {
            return false;
        }
        final long now = System.currentTimeMillis();
        if (this.lastInfoTime + this.suppressionTime > now) {
            return false;
        }
        this.lastInfoTime = now;
        return true;
    }
    
    private enum Config
    {
        NONE, 
        DEBUG_ALL, 
        INFO_THEN_DEBUG, 
        INFO_ALL;
    }
    
    public enum Mode
    {
        DEBUG(false), 
        INFO_THEN_DEBUG(true), 
        INFO(false);
        
        private final boolean fallToDebug;
        
        private Mode(final boolean fallToDebug) {
            this.fallToDebug = fallToDebug;
        }
        
        @Deprecated
        public boolean fallToDebug() {
            return this.fallToDebug;
        }
    }
}
