// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import java.io.IOException;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.http.ContentType;
import java.util.HashMap;
import org.apache.tomcat.util.http.Parameters;
import org.apache.tomcat.util.http.Cookies;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.buf.MessageBytes;

public final class Request
{
    private int serverPort;
    private MessageBytes serverNameMB;
    private int remotePort;
    private int localPort;
    private MessageBytes schemeMB;
    private MessageBytes methodMB;
    private MessageBytes unparsedURIMB;
    private MessageBytes uriMB;
    private MessageBytes decodedUriMB;
    private MessageBytes queryMB;
    private MessageBytes protoMB;
    private MessageBytes remoteAddrMB;
    private MessageBytes localNameMB;
    private MessageBytes remoteHostMB;
    private MessageBytes localAddrMB;
    private MimeHeaders headers;
    private MessageBytes instanceId;
    private Object[] notes;
    private InputBuffer inputBuffer;
    private UDecoder urlDecoder;
    private long contentLength;
    private MessageBytes contentTypeMB;
    private String charEncoding;
    private Cookies cookies;
    private Parameters parameters;
    private MessageBytes remoteUser;
    private MessageBytes authType;
    private HashMap<String, Object> attributes;
    private Response response;
    private ActionHook hook;
    private int bytesRead;
    private long startTime;
    private int available;
    private RequestInfo reqProcessorMX;
    
    public Request() {
        this.serverPort = -1;
        this.serverNameMB = MessageBytes.newInstance();
        this.schemeMB = MessageBytes.newInstance();
        this.methodMB = MessageBytes.newInstance();
        this.unparsedURIMB = MessageBytes.newInstance();
        this.uriMB = MessageBytes.newInstance();
        this.decodedUriMB = MessageBytes.newInstance();
        this.queryMB = MessageBytes.newInstance();
        this.protoMB = MessageBytes.newInstance();
        this.remoteAddrMB = MessageBytes.newInstance();
        this.localNameMB = MessageBytes.newInstance();
        this.remoteHostMB = MessageBytes.newInstance();
        this.localAddrMB = MessageBytes.newInstance();
        this.headers = new MimeHeaders();
        this.instanceId = MessageBytes.newInstance();
        this.notes = new Object[32];
        this.inputBuffer = null;
        this.urlDecoder = new UDecoder();
        this.contentLength = -1L;
        this.contentTypeMB = null;
        this.charEncoding = null;
        this.cookies = new Cookies(this.headers);
        this.parameters = new Parameters();
        this.remoteUser = MessageBytes.newInstance();
        this.authType = MessageBytes.newInstance();
        this.attributes = new HashMap<String, Object>();
        this.bytesRead = 0;
        this.startTime = -1L;
        this.available = 0;
        this.reqProcessorMX = new RequestInfo(this);
        this.parameters.setQuery(this.queryMB);
        this.parameters.setURLDecoder(this.urlDecoder);
    }
    
    public MessageBytes instanceId() {
        return this.instanceId;
    }
    
    public MimeHeaders getMimeHeaders() {
        return this.headers;
    }
    
    public UDecoder getURLDecoder() {
        return this.urlDecoder;
    }
    
    public MessageBytes scheme() {
        return this.schemeMB;
    }
    
    public MessageBytes method() {
        return this.methodMB;
    }
    
    public MessageBytes unparsedURI() {
        return this.unparsedURIMB;
    }
    
    public MessageBytes requestURI() {
        return this.uriMB;
    }
    
    public MessageBytes decodedURI() {
        return this.decodedUriMB;
    }
    
    public MessageBytes queryString() {
        return this.queryMB;
    }
    
    public MessageBytes protocol() {
        return this.protoMB;
    }
    
    public MessageBytes serverName() {
        return this.serverNameMB;
    }
    
    public int getServerPort() {
        return this.serverPort;
    }
    
    public void setServerPort(final int serverPort) {
        this.serverPort = serverPort;
    }
    
    public MessageBytes remoteAddr() {
        return this.remoteAddrMB;
    }
    
    public MessageBytes remoteHost() {
        return this.remoteHostMB;
    }
    
    public MessageBytes localName() {
        return this.localNameMB;
    }
    
    public MessageBytes localAddr() {
        return this.localAddrMB;
    }
    
    public int getRemotePort() {
        return this.remotePort;
    }
    
    public void setRemotePort(final int port) {
        this.remotePort = port;
    }
    
    public int getLocalPort() {
        return this.localPort;
    }
    
    public void setLocalPort(final int port) {
        this.localPort = port;
    }
    
    public String getCharacterEncoding() {
        if (this.charEncoding != null) {
            return this.charEncoding;
        }
        return this.charEncoding = ContentType.getCharsetFromContentType(this.getContentType());
    }
    
    public void setCharacterEncoding(final String enc) {
        this.charEncoding = enc;
    }
    
    public void setContentLength(final long len) {
        this.contentLength = len;
    }
    
    public int getContentLength() {
        final long length = this.getContentLengthLong();
        if (length < 2147483647L) {
            return (int)length;
        }
        return -1;
    }
    
    public long getContentLengthLong() {
        if (this.contentLength > -1L) {
            return this.contentLength;
        }
        final MessageBytes clB = this.headers.getUniqueValue("content-length");
        return this.contentLength = ((clB == null || clB.isNull()) ? -1L : clB.getLong());
    }
    
    public String getContentType() {
        this.contentType();
        if (this.contentTypeMB == null || this.contentTypeMB.isNull()) {
            return null;
        }
        return this.contentTypeMB.toString();
    }
    
    public void setContentType(final String type) {
        this.contentTypeMB.setString(type);
    }
    
    public MessageBytes contentType() {
        if (this.contentTypeMB == null) {
            this.contentTypeMB = this.headers.getValue("content-type");
        }
        return this.contentTypeMB;
    }
    
    public void setContentType(final MessageBytes mb) {
        this.contentTypeMB = mb;
    }
    
    public String getHeader(final String name) {
        return this.headers.getHeader(name);
    }
    
    public Response getResponse() {
        return this.response;
    }
    
    public void setResponse(final Response response) {
        (this.response = response).setRequest(this);
    }
    
    public void action(final ActionCode actionCode, final Object param) {
        if (this.hook == null && this.response != null) {
            this.hook = this.response.getHook();
        }
        if (this.hook != null) {
            if (param == null) {
                this.hook.action(actionCode, this);
            }
            else {
                this.hook.action(actionCode, param);
            }
        }
    }
    
    public Cookies getCookies() {
        return this.cookies;
    }
    
    public Parameters getParameters() {
        return this.parameters;
    }
    
    public void setAttribute(final String name, final Object o) {
        this.attributes.put(name, o);
    }
    
    public HashMap<String, Object> getAttributes() {
        return this.attributes;
    }
    
    public Object getAttribute(final String name) {
        return this.attributes.get(name);
    }
    
    public MessageBytes getRemoteUser() {
        return this.remoteUser;
    }
    
    public MessageBytes getAuthType() {
        return this.authType;
    }
    
    public int getAvailable() {
        return this.available;
    }
    
    public void setAvailable(final int available) {
        this.available = available;
    }
    
    public InputBuffer getInputBuffer() {
        return this.inputBuffer;
    }
    
    public void setInputBuffer(final InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }
    
    public int doRead(final ByteChunk chunk) throws IOException {
        final int n = this.inputBuffer.doRead(chunk, this);
        if (n > 0) {
            this.bytesRead += n;
        }
        return n;
    }
    
    @Override
    public String toString() {
        return "R( " + this.requestURI().toString() + ")";
    }
    
    public long getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }
    
    public final void setNote(final int pos, final Object value) {
        this.notes[pos] = value;
    }
    
    public final Object getNote(final int pos) {
        return this.notes[pos];
    }
    
    public void recycle() {
        this.bytesRead = 0;
        this.contentLength = -1L;
        this.contentTypeMB = null;
        this.charEncoding = null;
        this.headers.recycle();
        this.serverNameMB.recycle();
        this.serverPort = -1;
        this.localNameMB.recycle();
        this.localPort = -1;
        this.remotePort = -1;
        this.available = 0;
        this.cookies.recycle();
        this.parameters.recycle();
        this.unparsedURIMB.recycle();
        this.uriMB.recycle();
        this.decodedUriMB.recycle();
        this.queryMB.recycle();
        this.methodMB.recycle();
        this.protoMB.recycle();
        this.schemeMB.recycle();
        this.instanceId.recycle();
        this.remoteUser.recycle();
        this.authType.recycle();
        this.attributes.clear();
        this.startTime = -1L;
    }
    
    public void updateCounters() {
        this.reqProcessorMX.updateCounters();
    }
    
    public RequestInfo getRequestProcessor() {
        return this.reqProcessorMX;
    }
    
    public int getBytesRead() {
        return this.bytesRead;
    }
    
    public boolean isProcessing() {
        return this.reqProcessorMX.getStage() == 3;
    }
}
