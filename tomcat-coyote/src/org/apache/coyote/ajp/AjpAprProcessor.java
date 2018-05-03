// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.ajp;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.coyote.ActionCode;
import org.apache.coyote.RequestInfo;
import java.io.InterruptedIOException;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.IOException;
import org.apache.tomcat.jni.Socket;
import org.apache.coyote.OutputBuffer;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AprEndpoint;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.juli.logging.Log;

public class AjpAprProcessor extends AbstractAjpProcessor<Long>
{
    private static final Log log;
    protected SocketWrapper<Long> socket;
    protected ByteBuffer inputBuffer;
    protected ByteBuffer outputBuffer;
    
    @Override
    protected Log getLog() {
        return AjpAprProcessor.log;
    }
    
    public AjpAprProcessor(final int packetSize, final AprEndpoint endpoint) {
        super(packetSize, endpoint);
        this.inputBuffer = null;
        this.outputBuffer = null;
        this.response.setOutputBuffer(new SocketOutputBuffer(this));
        (this.inputBuffer = ByteBuffer.allocateDirect(packetSize * 2)).limit(0);
        this.outputBuffer = ByteBuffer.allocateDirect(packetSize * 2);
    }
    
    @Override
    public AbstractEndpoint.Handler.SocketState process(final SocketWrapper<Long> socket) throws IOException {
        final RequestInfo rp = this.request.getRequestProcessor();
        rp.setStage(1);
        this.socket = socket;
        final long socketRef = socket.getSocket();
        Socket.setrbb(socketRef, this.inputBuffer);
        Socket.setsbb(socketRef, this.outputBuffer);
        boolean cping = false;
        this.error = false;
        boolean keptAlive = false;
        while (!this.error && !this.endpoint.isPaused()) {
            try {
                if (!this.readMessage(this.requestHeaderMessage, true, keptAlive)) {
                    break;
                }
                final int type = this.requestHeaderMessage.getByte();
                if (type == 10) {
                    if (this.endpoint.isPaused()) {
                        this.recycle(true);
                        break;
                    }
                    cping = true;
                    if (Socket.send(socketRef, AjpAprProcessor.pongMessageArray, 0, AjpAprProcessor.pongMessageArray.length) >= 0) {
                        continue;
                    }
                    this.error = true;
                    continue;
                }
                else {
                    if (type != 2) {
                        if (AjpAprProcessor.log.isDebugEnabled()) {
                            AjpAprProcessor.log.debug((Object)("Unexpected message: " + type));
                        }
                        this.error = true;
                        break;
                    }
                    keptAlive = true;
                    this.request.setStartTime(System.currentTimeMillis());
                }
            }
            catch (IOException e) {
                this.error = true;
                break;
            }
            catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                AjpAprProcessor.log.debug((Object)AjpAprProcessor.sm.getString("ajpprocessor.header.error"), t);
                this.response.setStatus(400);
                this.adapter.log(this.request, this.response, 0L);
                this.error = true;
            }
            if (!this.error) {
                rp.setStage(2);
                try {
                    this.prepareRequest();
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    AjpAprProcessor.log.debug((Object)AjpAprProcessor.sm.getString("ajpprocessor.request.prepare"), t);
                    this.response.setStatus(400);
                    this.adapter.log(this.request, this.response, 0L);
                    this.error = true;
                }
            }
            if (!this.error && !cping && this.endpoint.isPaused()) {
                this.response.setStatus(503);
                this.adapter.log(this.request, this.response, 0L);
                this.error = true;
            }
            cping = false;
            if (!this.error) {
                try {
                    rp.setStage(3);
                    this.adapter.service(this.request, this.response);
                }
                catch (InterruptedIOException e2) {
                    this.error = true;
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    AjpAprProcessor.log.error((Object)AjpAprProcessor.sm.getString("ajpprocessor.request.process"), t);
                    this.response.setStatus(500);
                    this.adapter.log(this.request, this.response, 0L);
                    this.error = true;
                }
            }
            if (this.isAsync() && !this.error) {
                break;
            }
            if (!this.finished) {
                try {
                    this.finish();
                }
                catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    this.error = true;
                }
            }
            if (this.error) {
                this.response.setStatus(500);
            }
            this.request.updateCounters();
            rp.setStage(6);
            this.recycle(false);
        }
        rp.setStage(7);
        if (this.error || this.endpoint.isPaused()) {
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        if (this.isAsync()) {
            return AbstractEndpoint.Handler.SocketState.LONG;
        }
        return AbstractEndpoint.Handler.SocketState.OPEN;
    }
    
    @Override
    protected void actionInternal(final ActionCode actionCode, final Object param) {
        if (actionCode == ActionCode.ASYNC_COMPLETE) {
            if (this.asyncStateMachine.asyncComplete()) {
                ((AprEndpoint)this.endpoint).processSocketAsync(this.socket, SocketStatus.OPEN_READ);
            }
        }
        else if (actionCode == ActionCode.ASYNC_SETTIMEOUT) {
            if (param == null) {
                return;
            }
            final long timeout = (long)param;
            this.socket.setTimeout(timeout);
        }
        else if (actionCode == ActionCode.ASYNC_DISPATCH && this.asyncStateMachine.asyncDispatch()) {
            ((AprEndpoint)this.endpoint).processSocketAsync(this.socket, SocketStatus.OPEN_READ);
        }
    }
    
    @Override
    protected void resetTimeouts() {
    }
    
    @Override
    protected void output(final byte[] src, final int offset, final int length) throws IOException {
        this.outputBuffer.put(src, offset, length);
        final long socketRef = this.socket.getSocket();
        if (this.outputBuffer.position() > 0) {
            if (socketRef != 0L && Socket.sendbb(socketRef, 0, this.outputBuffer.position()) < 0) {
                this.outputBuffer.clear();
                throw new IOException(AjpAprProcessor.sm.getString("ajpprocessor.failedsend"));
            }
            this.outputBuffer.clear();
        }
    }
    
    protected boolean read(final int n) throws IOException {
        if (this.inputBuffer.capacity() - this.inputBuffer.limit() <= n - this.inputBuffer.remaining()) {
            this.inputBuffer.compact();
            this.inputBuffer.limit(this.inputBuffer.position());
            this.inputBuffer.position(0);
        }
        while (this.inputBuffer.remaining() < n) {
            final int nRead = Socket.recvbb(this.socket.getSocket(), this.inputBuffer.limit(), this.inputBuffer.capacity() - this.inputBuffer.limit());
            if (nRead <= 0) {
                throw new IOException(AjpAprProcessor.sm.getString("ajpprocessor.failedread"));
            }
            this.inputBuffer.limit(this.inputBuffer.limit() + nRead);
        }
        return true;
    }
    
    protected boolean readt(final int n, final boolean useAvailableData) throws IOException {
        if (useAvailableData && this.inputBuffer.remaining() == 0) {
            return false;
        }
        if (this.inputBuffer.capacity() - this.inputBuffer.limit() <= n - this.inputBuffer.remaining()) {
            this.inputBuffer.compact();
            this.inputBuffer.limit(this.inputBuffer.position());
            this.inputBuffer.position(0);
        }
        while (this.inputBuffer.remaining() < n) {
            final int nRead = Socket.recvbb(this.socket.getSocket(), this.inputBuffer.limit(), this.inputBuffer.capacity() - this.inputBuffer.limit());
            if (nRead > 0) {
                this.inputBuffer.limit(this.inputBuffer.limit() + nRead);
            }
            else {
                if (-nRead == 120005 || -nRead == 120001) {
                    return false;
                }
                throw new IOException(AjpAprProcessor.sm.getString("ajpprocessor.failedread"));
            }
        }
        return true;
    }
    
    public boolean receive() throws IOException {
        this.first = false;
        this.bodyMessage.reset();
        if (!this.readMessage(this.bodyMessage, false, false)) {
            return false;
        }
        if (this.bodyMessage.getLen() == 0) {
            return false;
        }
        final int blen = this.bodyMessage.peekInt();
        if (blen == 0) {
            return false;
        }
        this.bodyMessage.getBodyBytes(this.bodyBytes);
        this.empty = false;
        return true;
    }
    
    protected boolean readMessage(final AjpMessage message, final boolean first, final boolean useAvailableData) throws IOException {
        final int headerLength = message.getHeaderLength();
        if (first) {
            if (!this.readt(headerLength, useAvailableData)) {
                return false;
            }
        }
        else {
            this.read(headerLength);
        }
        this.inputBuffer.get(message.getBuffer(), 0, headerLength);
        final int messageLength = message.processHeader(true);
        if (messageLength < 0) {
            return false;
        }
        if (messageLength == 0) {
            return true;
        }
        if (messageLength > message.getBuffer().length) {
            throw new IllegalArgumentException(AjpAprProcessor.sm.getString("ajpprocessor.header.tooLong", new Object[] { messageLength, message.getBuffer().length }));
        }
        this.read(messageLength);
        this.inputBuffer.get(message.getBuffer(), headerLength, messageLength);
        return true;
    }
    
    @Override
    public void recycle(final boolean socketClosing) {
        super.recycle(socketClosing);
        this.inputBuffer.clear();
        this.inputBuffer.limit(0);
        this.outputBuffer.clear();
    }
    
    static {
        log = LogFactory.getLog((Class)AjpAprProcessor.class);
    }
}
