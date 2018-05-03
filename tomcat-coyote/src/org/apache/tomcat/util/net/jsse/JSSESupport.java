// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net.jsse;

import javax.net.ssl.HandshakeCompletedEvent;
import java.util.WeakHashMap;
import org.apache.juli.logging.LogFactory;
import java.net.SocketException;
import javax.net.ssl.SSLException;
import java.security.cert.Certificate;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.IOException;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.SSLSessionManager;
import org.apache.tomcat.util.net.SSLSupport;

class JSSESupport implements SSLSupport, SSLSessionManager
{
    private static final Log log;
    private static final StringManager sm;
    private static final Map<SSLSession, Integer> keySizeCache;
    protected SSLSocket ssl;
    protected SSLSession session;
    Listener listener;
    
    JSSESupport(final SSLSocket sock) {
        this.listener = new Listener();
        this.ssl = sock;
        this.session = sock.getSession();
        sock.addHandshakeCompletedListener(this.listener);
    }
    
    JSSESupport(final SSLSession session) {
        this.listener = new Listener();
        this.session = session;
    }
    
    @Override
    public String getCipherSuite() throws IOException {
        if (this.session == null) {
            return null;
        }
        return this.session.getCipherSuite();
    }
    
    @Override
    public Object[] getPeerCertificateChain() throws IOException {
        return this.getPeerCertificateChain(false);
    }
    
    protected X509Certificate[] getX509Certificates(final SSLSession session) {
        Certificate[] certs = null;
        try {
            certs = session.getPeerCertificates();
        }
        catch (Throwable t) {
            JSSESupport.log.debug((Object)JSSESupport.sm.getString("jsseSupport.clientCertError"), t);
            return null;
        }
        if (certs == null) {
            return null;
        }
        final X509Certificate[] x509Certs = new X509Certificate[certs.length];
        for (int i = 0; i < certs.length; ++i) {
            if (certs[i] instanceof X509Certificate) {
                x509Certs[i] = (X509Certificate)certs[i];
            }
            else {
                try {
                    final byte[] buffer = certs[i].getEncoded();
                    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    final ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
                    x509Certs[i] = (X509Certificate)cf.generateCertificate(stream);
                }
                catch (Exception ex) {
                    JSSESupport.log.info((Object)JSSESupport.sm.getString("jseeSupport.certTranslationError", new Object[] { certs[i] }), (Throwable)ex);
                    return null;
                }
            }
            if (JSSESupport.log.isTraceEnabled()) {
                JSSESupport.log.trace((Object)("Cert #" + i + " = " + x509Certs[i]));
            }
        }
        if (x509Certs.length < 1) {
            return null;
        }
        return x509Certs;
    }
    
    @Override
    public Object[] getPeerCertificateChain(final boolean force) throws IOException {
        if (this.session == null) {
            return null;
        }
        javax.security.cert.X509Certificate[] jsseCerts = null;
        try {
            jsseCerts = this.session.getPeerCertificateChain();
        }
        catch (Exception ex) {}
        if (jsseCerts == null) {
            jsseCerts = new javax.security.cert.X509Certificate[0];
        }
        if (jsseCerts.length <= 0 && force && this.ssl != null) {
            this.session.invalidate();
            this.handShake();
            this.session = this.ssl.getSession();
        }
        return this.getX509Certificates(this.session);
    }
    
    protected void handShake() throws IOException {
        if (this.ssl.getWantClientAuth()) {
            JSSESupport.log.debug((Object)JSSESupport.sm.getString("jsseSupport.noCertWant"));
        }
        else {
            this.ssl.setNeedClientAuth(true);
        }
        if (this.ssl.getEnabledCipherSuites().length == 0) {
            JSSESupport.log.warn((Object)JSSESupport.sm.getString("jsseSupport.serverRenegDisabled"));
            this.session.invalidate();
            this.ssl.close();
            return;
        }
        final InputStream in = this.ssl.getInputStream();
        final int oldTimeout = this.ssl.getSoTimeout();
        this.ssl.setSoTimeout(1000);
        final byte[] b = { 0 };
        this.listener.reset();
        this.ssl.startHandshake();
        for (int maxTries = 60, i = 0; i < maxTries; ++i) {
            if (JSSESupport.log.isTraceEnabled()) {
                JSSESupport.log.trace((Object)("Reading for try #" + i));
            }
            try {
                final int read = in.read(b);
                if (read > 0) {
                    throw new SSLException(JSSESupport.sm.getString("jsseSupport.unexpectedData"));
                }
            }
            catch (SSLException sslex) {
                JSSESupport.log.info((Object)JSSESupport.sm.getString("jsseSupport.clientCertError"), (Throwable)sslex);
                throw sslex;
            }
            catch (IOException ex) {}
            if (this.listener.completed) {
                break;
            }
        }
        this.ssl.setSoTimeout(oldTimeout);
        if (!this.listener.completed) {
            throw new SocketException("SSL Cert handshake timeout");
        }
    }
    
    @Override
    public Integer getKeySize() throws IOException {
        final CipherData[] c_aux = JSSESupport.ciphers;
        if (this.session == null) {
            return null;
        }
        Integer keySize = null;
        synchronized (JSSESupport.keySizeCache) {
            keySize = JSSESupport.keySizeCache.get(this.session);
        }
        if (keySize == null) {
            int size = 0;
            final String cipherSuite = this.session.getCipherSuite();
            for (int i = 0; i < c_aux.length; ++i) {
                if (cipherSuite.indexOf(c_aux[i].phrase) >= 0) {
                    size = c_aux[i].keySize;
                    break;
                }
            }
            keySize = size;
            synchronized (JSSESupport.keySizeCache) {
                JSSESupport.keySizeCache.put(this.session, keySize);
            }
        }
        return keySize;
    }
    
    @Override
    public String getSessionId() throws IOException {
        if (this.session == null) {
            return null;
        }
        final byte[] ssl_session = this.session.getId();
        if (ssl_session == null) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        for (int x = 0; x < ssl_session.length; ++x) {
            String digit = Integer.toHexString(ssl_session[x]);
            if (digit.length() < 2) {
                buf.append('0');
            }
            if (digit.length() > 2) {
                digit = digit.substring(digit.length() - 2);
            }
            buf.append(digit);
        }
        return buf.toString();
    }
    
    @Override
    public void invalidateSession() {
        this.session.invalidate();
    }
    
    static {
        log = LogFactory.getLog((Class)JSSESupport.class);
        sm = StringManager.getManager("org.apache.tomcat.util.net.jsse.res");
        keySizeCache = new WeakHashMap<SSLSession, Integer>();
    }
    
    private static class Listener implements HandshakeCompletedListener
    {
        volatile boolean completed;
        
        private Listener() {
            this.completed = false;
        }
        
        @Override
        public void handshakeCompleted(final HandshakeCompletedEvent event) {
            this.completed = true;
        }
        
        void reset() {
            this.completed = false;
        }
    }
}
