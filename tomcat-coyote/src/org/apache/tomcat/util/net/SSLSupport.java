// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.io.IOException;

public interface SSLSupport
{
    public static final String CIPHER_SUITE_KEY = "javax.servlet.request.cipher_suite";
    public static final String KEY_SIZE_KEY = "javax.servlet.request.key_size";
    public static final String CERTIFICATE_KEY = "javax.servlet.request.X509Certificate";
    public static final String SESSION_ID_KEY = "javax.servlet.request.ssl_session_id";
    public static final String SESSION_MGR = "javax.servlet.request.ssl_session_mgr";
    public static final CipherData[] ciphers = { new CipherData("_WITH_NULL_", 0), new CipherData("_WITH_IDEA_CBC_", 128), new CipherData("_WITH_RC2_CBC_40_", 40), new CipherData("_WITH_RC4_40_", 40), new CipherData("_WITH_RC4_128_", 128), new CipherData("_WITH_DES40_CBC_", 40), new CipherData("_WITH_DES_CBC_", 56), new CipherData("_WITH_3DES_EDE_CBC_", 168), new CipherData("_WITH_AES_128_CBC_", 128), new CipherData("_WITH_AES_256_CBC_", 256) };
    
    String getCipherSuite() throws IOException;
    
    Object[] getPeerCertificateChain() throws IOException;
    
    Object[] getPeerCertificateChain(final boolean p0) throws IOException;
    
    Integer getKeySize() throws IOException;
    
    String getSessionId() throws IOException;
    
    public static final class CipherData
    {
        public String phrase;
        public int keySize;
        
        public CipherData(final String phrase, final int keySize) {
            this.phrase = null;
            this.keySize = 0;
            this.phrase = phrase;
            this.keySize = keySize;
        }
    }
}
