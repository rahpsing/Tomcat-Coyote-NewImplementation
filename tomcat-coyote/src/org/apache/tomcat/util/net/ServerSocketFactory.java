// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.net;

import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.ServerSocket;

public interface ServerSocketFactory
{
    ServerSocket createSocket(final int p0) throws IOException, InstantiationException;
    
    ServerSocket createSocket(final int p0, final int p1) throws IOException, InstantiationException;
    
    ServerSocket createSocket(final int p0, final int p1, final InetAddress p2) throws IOException, InstantiationException;
    
    Socket acceptSocket(final ServerSocket p0) throws IOException;
    
    void handshake(final Socket p0) throws IOException;
}
