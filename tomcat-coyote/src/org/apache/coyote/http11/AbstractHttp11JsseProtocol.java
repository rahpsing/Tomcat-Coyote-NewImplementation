// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.tomcat.util.net.SSLImplementation;

public abstract class AbstractHttp11JsseProtocol extends AbstractHttp11Protocol
{
    protected SSLImplementation sslImplementation;
    private String sslImplementationName;
    
    public AbstractHttp11JsseProtocol() {
        this.sslImplementation = null;
        this.sslImplementationName = null;
    }
    
    public String getAlgorithm() {
        return this.endpoint.getAlgorithm();
    }
    
    public void setAlgorithm(final String s) {
        this.endpoint.setAlgorithm(s);
    }
    
    public String getClientAuth() {
        return this.endpoint.getClientAuth();
    }
    
    public void setClientAuth(final String s) {
        this.endpoint.setClientAuth(s);
    }
    
    public String getKeystoreFile() {
        return this.endpoint.getKeystoreFile();
    }
    
    public void setKeystoreFile(final String s) {
        this.endpoint.setKeystoreFile(s);
    }
    
    public String getKeystorePass() {
        return this.endpoint.getKeystorePass();
    }
    
    public void setKeystorePass(final String s) {
        this.endpoint.setKeystorePass(s);
    }
    
    public String getKeystoreType() {
        return this.endpoint.getKeystoreType();
    }
    
    public void setKeystoreType(final String s) {
        this.endpoint.setKeystoreType(s);
    }
    
    public String getKeystoreProvider() {
        return this.endpoint.getKeystoreProvider();
    }
    
    public void setKeystoreProvider(final String s) {
        this.endpoint.setKeystoreProvider(s);
    }
    
    public String getSslProtocol() {
        return this.endpoint.getSslProtocol();
    }
    
    public void setSslProtocol(final String s) {
        this.endpoint.setSslProtocol(s);
    }
    
    public String getCiphers() {
        return this.endpoint.getCiphers();
    }
    
    public void setCiphers(final String s) {
        this.endpoint.setCiphers(s);
    }
    
    public String getKeyAlias() {
        return this.endpoint.getKeyAlias();
    }
    
    public void setKeyAlias(final String s) {
        this.endpoint.setKeyAlias(s);
    }
    
    public String getKeyPass() {
        return this.endpoint.getKeyPass();
    }
    
    public void setKeyPass(final String s) {
        this.endpoint.setKeyPass(s);
    }
    
    public void setTruststoreFile(final String f) {
        this.endpoint.setTruststoreFile(f);
    }
    
    public String getTruststoreFile() {
        return this.endpoint.getTruststoreFile();
    }
    
    public void setTruststorePass(final String p) {
        this.endpoint.setTruststorePass(p);
    }
    
    public String getTruststorePass() {
        return this.endpoint.getTruststorePass();
    }
    
    public void setTruststoreType(final String t) {
        this.endpoint.setTruststoreType(t);
    }
    
    public String getTruststoreType() {
        return this.endpoint.getTruststoreType();
    }
    
    public void setTruststoreProvider(final String t) {
        this.endpoint.setTruststoreProvider(t);
    }
    
    public String getTruststoreProvider() {
        return this.endpoint.getTruststoreProvider();
    }
    
    public void setTruststoreAlgorithm(final String a) {
        this.endpoint.setTruststoreAlgorithm(a);
    }
    
    public String getTruststoreAlgorithm() {
        return this.endpoint.getTruststoreAlgorithm();
    }
    
    public void setTrustMaxCertLength(final String s) {
        this.endpoint.setTrustMaxCertLength(s);
    }
    
    public String getTrustMaxCertLength() {
        return this.endpoint.getTrustMaxCertLength();
    }
    
    public void setCrlFile(final String s) {
        this.endpoint.setCrlFile(s);
    }
    
    public String getCrlFile() {
        return this.endpoint.getCrlFile();
    }
    
    public void setSessionCacheSize(final String s) {
        this.endpoint.setSessionCacheSize(s);
    }
    
    public String getSessionCacheSize() {
        return this.endpoint.getSessionCacheSize();
    }
    
    public void setSessionTimeout(final String s) {
        this.endpoint.setSessionTimeout(s);
    }
    
    public String getSessionTimeout() {
        return this.endpoint.getSessionTimeout();
    }
    
    public void setAllowUnsafeLegacyRenegotiation(final String s) {
        this.endpoint.setAllowUnsafeLegacyRenegotiation(s);
    }
    
    public String getAllowUnsafeLegacyRenegotiation() {
        return this.endpoint.getAllowUnsafeLegacyRenegotiation();
    }
    
    public String getSslImplementationName() {
        return this.sslImplementationName;
    }
    
    public void setSslImplementationName(final String s) {
        this.sslImplementationName = s;
    }
    
    @Override
    public void init() throws Exception {
        this.sslImplementation = SSLImplementation.getInstance(this.sslImplementationName);
        super.init();
    }
}
