// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.tomcat.util.res.StringManager;
import org.apache.coyote.AbstractProtocol;

public abstract class AbstractHttp11Protocol extends AbstractProtocol
{
    protected static final StringManager sm;
    private int socketBuffer;
    private int maxSavePostSize;
    private int maxHttpHeaderSize;
    private int connectionUploadTimeout;
    private boolean disableUploadTimeout;
    private String compression;
    private String noCompressionUserAgents;
    private String compressableMimeTypes;
    private int compressionMinSize;
    private String restrictedUserAgents;
    private String server;
    private int maxTrailerSize;
    private int maxExtensionSize;
    private boolean secure;
    
    public AbstractHttp11Protocol() {
        this.socketBuffer = 9000;
        this.maxSavePostSize = 4096;
        this.maxHttpHeaderSize = 8192;
        this.connectionUploadTimeout = 300000;
        this.disableUploadTimeout = true;
        this.compression = "off";
        this.noCompressionUserAgents = null;
        this.compressableMimeTypes = "text/html,text/xml,text/plain";
        this.compressionMinSize = 2048;
        this.restrictedUserAgents = null;
        this.maxTrailerSize = 8192;
        this.maxExtensionSize = 8192;
    }
    
    @Override
    protected String getProtocolName() {
        return "Http";
    }
    
    public int getSocketBuffer() {
        return this.socketBuffer;
    }
    
    public void setSocketBuffer(final int socketBuffer) {
        this.socketBuffer = socketBuffer;
    }
    
    public int getMaxSavePostSize() {
        return this.maxSavePostSize;
    }
    
    public void setMaxSavePostSize(final int valueI) {
        this.maxSavePostSize = valueI;
    }
    
    public int getMaxHttpHeaderSize() {
        return this.maxHttpHeaderSize;
    }
    
    public void setMaxHttpHeaderSize(final int valueI) {
        this.maxHttpHeaderSize = valueI;
    }
    
    public int getConnectionUploadTimeout() {
        return this.connectionUploadTimeout;
    }
    
    public void setConnectionUploadTimeout(final int i) {
        this.connectionUploadTimeout = i;
    }
    
    public boolean getDisableUploadTimeout() {
        return this.disableUploadTimeout;
    }
    
    public void setDisableUploadTimeout(final boolean isDisabled) {
        this.disableUploadTimeout = isDisabled;
    }
    
    public String getCompression() {
        return this.compression;
    }
    
    public void setCompression(final String valueS) {
        this.compression = valueS;
    }
    
    public String getNoCompressionUserAgents() {
        return this.noCompressionUserAgents;
    }
    
    public void setNoCompressionUserAgents(final String valueS) {
        this.noCompressionUserAgents = valueS;
    }
    
    public String getCompressableMimeType() {
        return this.compressableMimeTypes;
    }
    
    public void setCompressableMimeType(final String valueS) {
        this.compressableMimeTypes = valueS;
    }
    
    public String getCompressableMimeTypes() {
        return this.getCompressableMimeType();
    }
    
    public void setCompressableMimeTypes(final String valueS) {
        this.setCompressableMimeType(valueS);
    }
    
    public int getCompressionMinSize() {
        return this.compressionMinSize;
    }
    
    public void setCompressionMinSize(final int valueI) {
        this.compressionMinSize = valueI;
    }
    
    public String getRestrictedUserAgents() {
        return this.restrictedUserAgents;
    }
    
    public void setRestrictedUserAgents(final String valueS) {
        this.restrictedUserAgents = valueS;
    }
    
    public String getServer() {
        return this.server;
    }
    
    public void setServer(final String server) {
        this.server = server;
    }
    
    public int getMaxTrailerSize() {
        return this.maxTrailerSize;
    }
    
    public void setMaxTrailerSize(final int maxTrailerSize) {
        this.maxTrailerSize = maxTrailerSize;
    }
    
    public int getMaxExtensionSize() {
        return this.maxExtensionSize;
    }
    
    public void setMaxExtensionSize(final int maxExtensionSize) {
        this.maxExtensionSize = maxExtensionSize;
    }
    
    public boolean getSecure() {
        return this.secure;
    }
    
    public void setSecure(final boolean b) {
        this.secure = b;
    }
    
    public boolean isSSLEnabled() {
        return this.endpoint.isSSLEnabled();
    }
    
    public void setSSLEnabled(final boolean SSLEnabled) {
        this.endpoint.setSSLEnabled(SSLEnabled);
    }
    
    public int getMaxKeepAliveRequests() {
        return this.endpoint.getMaxKeepAliveRequests();
    }
    
    public void setMaxKeepAliveRequests(final int mkar) {
        this.endpoint.setMaxKeepAliveRequests(mkar);
    }
    
    static {
        sm = StringManager.getManager("org.apache.coyote.http11");
    }
}
