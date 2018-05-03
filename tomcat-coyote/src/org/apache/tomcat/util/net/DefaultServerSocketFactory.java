// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.ServerSocket;

public class DefaultServerSocketFactory implements ServerSocketFactory
{
    public DefaultServerSocketFactory(final AbstractEndpoint endpoint) {
    }
    
    @Override
    public ServerSocket createSocket(final int port) throws IOException {
        return new ServerSocket(port);
    }
    
    @Override
    public ServerSocket createSocket(final int port, final int backlog) throws IOException {
        return new ServerSocket(port, backlog);
    }
    
    @Override
    public ServerSocket createSocket(final int port, final int backlog, final InetAddress ifAddress) throws IOException {
        return new ServerSocket(port, backlog, ifAddress);
    }
    
    @Override
    public Socket acceptSocket(final ServerSocket socket) throws IOException {
        return socket.accept();
    }
    
    @Override
    public void handshake(final Socket sock) throws IOException {
    }
}
