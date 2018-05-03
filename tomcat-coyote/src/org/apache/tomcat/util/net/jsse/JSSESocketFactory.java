// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net.jsse;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.apache.juli.logging.LogFactory;
import javax.net.ssl.SSLServerSocket;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertStoreParameters;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.CertSelector;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import javax.net.ssl.ManagerFactoryParameters;
import java.security.cert.CertPathParameters;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.X509KeyManager;
import java.util.Locale;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSessionContext;
import java.security.SecureRandom;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.security.UnrecoverableKeyException;
import java.security.KeyStore;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLException;
import java.net.SocketException;
import javax.net.ssl.SSLSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.ServerSocketFactory;

public class JSSESocketFactory implements ServerSocketFactory, SSLUtil
{
    private static final Log log;
    private static final StringManager sm;
    private static final boolean RFC_5746_SUPPORTED;
    private static final String[] DEFAULT_SERVER_PROTOCOLS;
    private static final String[] DEAFULT_SERVER_CIPHER_SUITES;
    private static final String defaultProtocol = "TLS";
    private static final String defaultKeystoreType = "JKS";
    private static final String defaultKeystoreFile;
    private static final int defaultSessionCacheSize = 0;
    private static final int defaultSessionTimeout = 86400;
    private static final String ALLOW_ALL_SUPPORTED_CIPHERS = "ALL";
    public static final String DEFAULT_KEY_PASS = "changeit";
    private AbstractEndpoint endpoint;
    protected SSLServerSocketFactory sslProxy;
    protected String[] enabledCiphers;
    protected String[] enabledProtocols;
    protected boolean allowUnsafeLegacyRenegotiation;
    protected boolean requireClientAuth;
    protected boolean wantClientAuth;
    
    public JSSESocketFactory(final AbstractEndpoint endpoint) {
        this.sslProxy = null;
        this.allowUnsafeLegacyRenegotiation = false;
        this.requireClientAuth = false;
        this.wantClientAuth = false;
        this.endpoint = endpoint;
    }
    
    @Override
    public ServerSocket createSocket(final int port) throws IOException {
        this.init();
        final ServerSocket socket = this.sslProxy.createServerSocket(port);
        this.initServerSocket(socket);
        return socket;
    }
    
    @Override
    public ServerSocket createSocket(final int port, final int backlog) throws IOException {
        this.init();
        final ServerSocket socket = this.sslProxy.createServerSocket(port, backlog);
        this.initServerSocket(socket);
        return socket;
    }
    
    @Override
    public ServerSocket createSocket(final int port, final int backlog, final InetAddress ifAddress) throws IOException {
        this.init();
        final ServerSocket socket = this.sslProxy.createServerSocket(port, backlog, ifAddress);
        this.initServerSocket(socket);
        return socket;
    }
    
    @Override
    public Socket acceptSocket(final ServerSocket socket) throws IOException {
        SSLSocket asock = null;
        try {
            asock = (SSLSocket)socket.accept();
        }
        catch (SSLException e) {
            throw new SocketException("SSL handshake error" + e.toString());
        }
        return asock;
    }
    
    @Override
    public void handshake(final Socket sock) throws IOException {
        final SSLSession session = ((SSLSocket)sock).getSession();
        if (session.getCipherSuite().equals("SSL_NULL_WITH_NULL_NULL")) {
            throw new IOException("SSL handshake failed. Ciper suite in SSL Session is SSL_NULL_WITH_NULL_NULL");
        }
        if (!this.allowUnsafeLegacyRenegotiation && !JSSESocketFactory.RFC_5746_SUPPORTED) {
            ((SSLSocket)sock).setEnabledCipherSuites(new String[0]);
        }
    }
    
    @Override
    public String[] getEnableableCiphers(final SSLContext context) {
        final String requestedCiphersStr = this.endpoint.getCiphers();
        if ("ALL".equals(requestedCiphersStr)) {
            return context.getSupportedSSLParameters().getCipherSuites();
        }
        if (requestedCiphersStr == null || requestedCiphersStr.trim().length() == 0) {
            return JSSESocketFactory.DEAFULT_SERVER_CIPHER_SUITES;
        }
        final List<String> requestedCiphers = new ArrayList<String>();
        for (final String rc : requestedCiphersStr.split(",")) {
            final String cipher = rc.trim();
            if (cipher.length() > 0) {
                requestedCiphers.add(cipher);
            }
        }
        if (requestedCiphers.isEmpty()) {
            return JSSESocketFactory.DEAFULT_SERVER_CIPHER_SUITES;
        }
        final List<String> ciphers = new ArrayList<String>(requestedCiphers);
        ciphers.retainAll(Arrays.asList(context.getSupportedSSLParameters().getCipherSuites()));
        if (ciphers.isEmpty()) {
            JSSESocketFactory.log.warn((Object)JSSESocketFactory.sm.getString("jsse.requested_ciphers_not_supported", new Object[] { requestedCiphersStr }));
        }
        if (JSSESocketFactory.log.isDebugEnabled()) {
            JSSESocketFactory.log.debug((Object)JSSESocketFactory.sm.getString("jsse.enableable_ciphers", new Object[] { ciphers }));
            if (ciphers.size() != requestedCiphers.size()) {
                final List<String> skipped = new ArrayList<String>(requestedCiphers);
                skipped.removeAll(ciphers);
                JSSESocketFactory.log.debug((Object)JSSESocketFactory.sm.getString("jsse.unsupported_ciphers", new Object[] { skipped }));
            }
        }
        return ciphers.toArray(new String[ciphers.size()]);
    }
    
    protected String getKeystorePassword() {
        String keystorePass = this.endpoint.getKeystorePass();
        if (keystorePass == null) {
            keystorePass = this.endpoint.getKeyPass();
        }
        if (keystorePass == null) {
            keystorePass = "changeit";
        }
        return keystorePass;
    }
    
    protected KeyStore getKeystore(final String type, final String provider, final String pass) throws IOException {
        String keystoreFile = this.endpoint.getKeystoreFile();
        if (keystoreFile == null) {
            keystoreFile = JSSESocketFactory.defaultKeystoreFile;
        }
        return this.getStore(type, provider, keystoreFile, pass);
    }
    
    protected KeyStore getTrustStore(final String keystoreType, final String keystoreProvider) throws IOException {
        KeyStore trustStore = null;
        String truststoreFile = this.endpoint.getTruststoreFile();
        if (truststoreFile == null) {
            truststoreFile = System.getProperty("javax.net.ssl.trustStore");
        }
        if (JSSESocketFactory.log.isDebugEnabled()) {
            JSSESocketFactory.log.debug((Object)("Truststore = " + truststoreFile));
        }
        String truststorePassword = this.endpoint.getTruststorePass();
        if (truststorePassword == null) {
            truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
        }
        if (JSSESocketFactory.log.isDebugEnabled()) {
            JSSESocketFactory.log.debug((Object)("TrustPass = " + truststorePassword));
        }
        String truststoreType = this.endpoint.getTruststoreType();
        if (truststoreType == null) {
            truststoreType = System.getProperty("javax.net.ssl.trustStoreType");
        }
        if (truststoreType == null) {
            truststoreType = keystoreType;
        }
        if (JSSESocketFactory.log.isDebugEnabled()) {
            JSSESocketFactory.log.debug((Object)("trustType = " + truststoreType));
        }
        String truststoreProvider = this.endpoint.getTruststoreProvider();
        if (truststoreProvider == null) {
            truststoreProvider = System.getProperty("javax.net.ssl.trustStoreProvider");
        }
        if (truststoreProvider == null) {
            truststoreProvider = keystoreProvider;
        }
        if (JSSESocketFactory.log.isDebugEnabled()) {
            JSSESocketFactory.log.debug((Object)("trustProvider = " + truststoreProvider));
        }
        if (truststoreFile != null) {
            try {
                trustStore = this.getStore(truststoreType, truststoreProvider, truststoreFile, truststorePassword);
            }
            catch (IOException ioe) {
                final Throwable cause = ioe.getCause();
                if (!(cause instanceof UnrecoverableKeyException)) {
                    throw ioe;
                }
                JSSESocketFactory.log.warn((Object)JSSESocketFactory.sm.getString("jsse.invalid_truststore_password"), cause);
                trustStore = this.getStore(truststoreType, truststoreProvider, truststoreFile, null);
            }
        }
        return trustStore;
    }
    
    private KeyStore getStore(final String type, final String provider, final String path, final String pass) throws IOException {
        KeyStore ks = null;
        InputStream istream = null;
        try {
            if (provider == null) {
                ks = KeyStore.getInstance(type);
            }
            else {
                ks = KeyStore.getInstance(type, provider);
            }
            if (!"PKCS11".equalsIgnoreCase(type) && !"".equalsIgnoreCase(path)) {
                File keyStoreFile = new File(path);
                if (!keyStoreFile.isAbsolute()) {
                    keyStoreFile = new File(System.getProperty("catalina.base"), path);
                }
                istream = new FileInputStream(keyStoreFile);
            }
            char[] storePass = null;
            if (pass != null && !"".equals(pass)) {
                storePass = pass.toCharArray();
            }
            ks.load(istream, storePass);
        }
        catch (FileNotFoundException fnfe) {
            JSSESocketFactory.log.error((Object)JSSESocketFactory.sm.getString("jsse.keystore_load_failed", new Object[] { type, path, fnfe.getMessage() }), (Throwable)fnfe);
            throw fnfe;
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Exception ex) {
            final String msg = JSSESocketFactory.sm.getString("jsse.keystore_load_failed", new Object[] { type, path, ex.getMessage() });
            JSSESocketFactory.log.error((Object)msg, (Throwable)ex);
            throw new IOException(msg);
        }
        finally {
            if (istream != null) {
                try {
                    istream.close();
                }
                catch (IOException ex2) {}
            }
        }
        return ks;
    }
    
    void init() throws IOException {
        try {
            final String clientAuthStr = this.endpoint.getClientAuth();
            if ("true".equalsIgnoreCase(clientAuthStr) || "yes".equalsIgnoreCase(clientAuthStr)) {
                this.requireClientAuth = true;
            }
            else if ("want".equalsIgnoreCase(clientAuthStr)) {
                this.wantClientAuth = true;
            }
            final SSLContext context = this.createSSLContext();
            context.init(this.getKeyManagers(), this.getTrustManagers(), null);
            final SSLSessionContext sessionContext = context.getServerSessionContext();
            if (sessionContext != null) {
                this.configureSessionContext(sessionContext);
            }
            this.sslProxy = context.getServerSocketFactory();
            this.enabledCiphers = this.getEnableableCiphers(context);
            this.enabledProtocols = this.getEnableableProtocols(context);
            this.allowUnsafeLegacyRenegotiation = "true".equals(this.endpoint.getAllowUnsafeLegacyRenegotiation());
            this.checkConfig();
        }
        catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException)e;
            }
            throw new IOException(e.getMessage(), e);
        }
    }
    
    @Override
    public SSLContext createSSLContext() throws Exception {
        String protocol = this.endpoint.getSslProtocol();
        if (protocol == null) {
            protocol = "TLS";
        }
        final SSLContext context = SSLContext.getInstance(protocol);
        return context;
    }
    
    @Override
    public KeyManager[] getKeyManagers() throws Exception {
        String keystoreType = this.endpoint.getKeystoreType();
        if (keystoreType == null) {
            keystoreType = "JKS";
        }
        String algorithm = this.endpoint.getAlgorithm();
        if (algorithm == null) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }
        return this.getKeyManagers(keystoreType, this.endpoint.getKeystoreProvider(), algorithm, this.endpoint.getKeyAlias());
    }
    
    @Override
    public TrustManager[] getTrustManagers() throws Exception {
        String truststoreType = this.endpoint.getTruststoreType();
        if (truststoreType == null) {
            truststoreType = System.getProperty("javax.net.ssl.trustStoreType");
        }
        if (truststoreType == null) {
            truststoreType = this.endpoint.getKeystoreType();
        }
        if (truststoreType == null) {
            truststoreType = "JKS";
        }
        String algorithm = this.endpoint.getTruststoreAlgorithm();
        if (algorithm == null) {
            algorithm = TrustManagerFactory.getDefaultAlgorithm();
        }
        return this.getTrustManagers(truststoreType, this.endpoint.getKeystoreProvider(), algorithm);
    }
    
    @Override
    public void configureSessionContext(final SSLSessionContext sslSessionContext) {
        int sessionCacheSize;
        if (this.endpoint.getSessionCacheSize() != null) {
            sessionCacheSize = Integer.parseInt(this.endpoint.getSessionCacheSize());
        }
        else {
            sessionCacheSize = 0;
        }
        int sessionTimeout;
        if (this.endpoint.getSessionTimeout() != null) {
            sessionTimeout = Integer.parseInt(this.endpoint.getSessionTimeout());
        }
        else {
            sessionTimeout = 86400;
        }
        sslSessionContext.setSessionCacheSize(sessionCacheSize);
        sslSessionContext.setSessionTimeout(sessionTimeout);
    }
    
    protected KeyManager[] getKeyManagers(final String keystoreType, final String keystoreProvider, final String algorithm, final String keyAlias) throws Exception {
        KeyManager[] kms = null;
        final String keystorePass = this.getKeystorePassword();
        final KeyStore ks = this.getKeystore(keystoreType, keystoreProvider, keystorePass);
        if (keyAlias != null && !ks.isKeyEntry(keyAlias)) {
            throw new IOException(JSSESocketFactory.sm.getString("jsse.alias_no_key_entry", new Object[] { keyAlias }));
        }
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        String keyPass = this.endpoint.getKeyPass();
        if (keyPass == null) {
            keyPass = keystorePass;
        }
        kmf.init(ks, keyPass.toCharArray());
        kms = kmf.getKeyManagers();
        if (keyAlias != null) {
            String alias = keyAlias;
            if ("JKS".equals(keystoreType)) {
                alias = alias.toLowerCase(Locale.ENGLISH);
            }
            for (int i = 0; i < kms.length; ++i) {
                kms[i] = new JSSEKeyManager((X509KeyManager)kms[i], alias);
            }
        }
        return kms;
    }
    
    protected TrustManager[] getTrustManagers(final String keystoreType, final String keystoreProvider, final String algorithm) throws Exception {
        final String crlf = this.endpoint.getCrlFile();
        final String className = this.endpoint.getTrustManagerClassName();
        if (className == null || className.length() <= 0) {
            TrustManager[] tms = null;
            final KeyStore trustStore = this.getTrustStore(keystoreType, keystoreProvider);
            if (trustStore != null || this.endpoint.getTrustManagerClassName() != null) {
                if (crlf == null) {
                    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                    tmf.init(trustStore);
                    tms = tmf.getTrustManagers();
                }
                else {
                    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                    final CertPathParameters params = this.getParameters(algorithm, crlf, trustStore);
                    final ManagerFactoryParameters mfp = new CertPathTrustManagerParameters(params);
                    tmf.init(mfp);
                    tms = tmf.getTrustManagers();
                }
            }
            return tms;
        }
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final Class<?> clazz = classLoader.loadClass(className);
        if (!TrustManager.class.isAssignableFrom(clazz)) {
            throw new InstantiationException(JSSESocketFactory.sm.getString("jsse.invalidTrustManagerClassName", new Object[] { className }));
        }
        final Object trustManagerObject = clazz.newInstance();
        final TrustManager trustManager = (TrustManager)trustManagerObject;
        return new TrustManager[] { trustManager };
    }
    
    protected CertPathParameters getParameters(final String algorithm, final String crlf, final KeyStore trustStore) throws Exception {
        CertPathParameters params = null;
        if ("PKIX".equalsIgnoreCase(algorithm)) {
            final PKIXBuilderParameters xparams = new PKIXBuilderParameters(trustStore, new X509CertSelector());
            final Collection<? extends CRL> crls = this.getCRLs(crlf);
            final CertStoreParameters csp = new CollectionCertStoreParameters(crls);
            final CertStore store = CertStore.getInstance("Collection", csp);
            xparams.addCertStore(store);
            xparams.setRevocationEnabled(true);
            final String trustLength = this.endpoint.getTrustMaxCertLength();
            if (trustLength != null) {
                try {
                    xparams.setMaxPathLength(Integer.parseInt(trustLength));
                }
                catch (Exception ex) {
                    JSSESocketFactory.log.warn((Object)("Bad maxCertLength: " + trustLength));
                }
            }
            params = xparams;
            return params;
        }
        throw new CRLException("CRLs not supported for type: " + algorithm);
    }
    
    protected Collection<? extends CRL> getCRLs(final String crlf) throws IOException, CRLException, CertificateException {
        File crlFile = new File(crlf);
        if (!crlFile.isAbsolute()) {
            crlFile = new File(System.getProperty("catalina.base"), crlf);
        }
        Collection<? extends CRL> crls = null;
        InputStream is = null;
        try {
            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            is = new FileInputStream(crlFile);
            crls = cf.generateCRLs(is);
        }
        catch (IOException iex) {
            throw iex;
        }
        catch (CRLException crle) {
            throw crle;
        }
        catch (CertificateException ce) {
            throw ce;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception ex) {}
            }
        }
        return crls;
    }
    
    @Override
    public String[] getEnableableProtocols(final SSLContext context) {
        final String[] requestedProtocols = this.endpoint.getSslEnabledProtocolsArray();
        if (requestedProtocols == null || requestedProtocols.length == 0) {
            return JSSESocketFactory.DEFAULT_SERVER_PROTOCOLS;
        }
        final List<String> protocols = new ArrayList<String>(Arrays.asList(requestedProtocols));
        protocols.retainAll(Arrays.asList(context.getSupportedSSLParameters().getProtocols()));
        if (protocols.isEmpty()) {
            JSSESocketFactory.log.warn((Object)JSSESocketFactory.sm.getString("jsse.requested_protocols_not_supported", new Object[] { Arrays.asList(requestedProtocols) }));
        }
        if (JSSESocketFactory.log.isDebugEnabled()) {
            JSSESocketFactory.log.debug((Object)JSSESocketFactory.sm.getString("jsse.enableable_protocols", new Object[] { protocols }));
            if (protocols.size() != requestedProtocols.length) {
                final List<String> skipped = new ArrayList<String>(Arrays.asList(requestedProtocols));
                skipped.removeAll(protocols);
                JSSESocketFactory.log.debug((Object)JSSESocketFactory.sm.getString("jsse.unsupported_protocols", new Object[] { skipped }));
            }
        }
        return protocols.toArray(new String[protocols.size()]);
    }
    
    protected void configureClientAuth(final SSLServerSocket socket) {
        if (this.wantClientAuth) {
            socket.setWantClientAuth(this.wantClientAuth);
        }
        else {
            socket.setNeedClientAuth(this.requireClientAuth);
        }
    }
    
    private void initServerSocket(final ServerSocket ssocket) {
        final SSLServerSocket socket = (SSLServerSocket)ssocket;
        socket.setEnabledCipherSuites(this.enabledCiphers);
        socket.setEnabledProtocols(this.enabledProtocols);
        this.configureClientAuth(socket);
    }
    
    private void checkConfig() throws IOException {
        final ServerSocket socket = this.sslProxy.createServerSocket();
        this.initServerSocket(socket);
        try {
            socket.setSoTimeout(1);
            socket.accept();
        }
        catch (SSLException ssle) {
            final IOException ioe = new IOException(JSSESocketFactory.sm.getString("jsse.invalid_ssl_conf", new Object[] { ssle.getMessage() }));
            ioe.initCause(ssle);
            throw ioe;
        }
        catch (Exception e) {}
        finally {
            if (!socket.isClosed()) {
                socket.close();
            }
        }
    }
    
    static {
        log = LogFactory.getLog((Class)JSSESocketFactory.class);
        sm = StringManager.getManager("org.apache.tomcat.util.net.jsse.res");
        defaultKeystoreFile = System.getProperty("user.home") + "/.keystore";
        boolean result = false;
        String[] ciphers = null;
        String[] protocols = null;
        try {
            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
            final SSLServerSocketFactory ssf = context.getServerSocketFactory();
            final String[] arr$;
            final String[] supportedCiphers = arr$ = ssf.getSupportedCipherSuites();
            for (final String cipher : arr$) {
                if ("TLS_EMPTY_RENEGOTIATION_INFO_SCSV".equals(cipher)) {
                    result = true;
                    break;
                }
            }
            final SSLServerSocket socket = (SSLServerSocket)ssf.createServerSocket();
            ciphers = socket.getEnabledCipherSuites();
            protocols = socket.getEnabledProtocols();
        }
        catch (NoSuchAlgorithmException e) {}
        catch (KeyManagementException e2) {}
        catch (IOException ex) {}
        RFC_5746_SUPPORTED = result;
        DEAFULT_SERVER_CIPHER_SUITES = ciphers;
        DEFAULT_SERVER_PROTOCOLS = protocols;
    }
}
