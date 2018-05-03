// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.InputBuffer;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import java.nio.channels.Selector;
import java.io.EOFException;
import org.apache.tomcat.util.net.NioEndpoint;
import java.io.IOException;
import org.apache.coyote.Request;
import org.apache.tomcat.util.net.NioSelectorPool;
import java.nio.charset.Charset;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.NioChannel;

public class InternalNioInputBuffer extends AbstractInputBuffer<NioChannel>
{
    private static final Log log;
    private static final Charset DEFAULT_CHARSET;
    private boolean parsingRequestLine;
    private int parsingRequestLinePhase;
    private boolean parsingRequestLineEol;
    private int parsingRequestLineStart;
    private int parsingRequestLineQPos;
    private HeaderParsePosition headerParsePos;
    private NioChannel socket;
    private NioSelectorPool pool;
    private final int headerBufferSize;
    private int socketReadBufferSize;
    private HeaderParseData headerData;
    
    public InternalNioInputBuffer(final Request request, final int headerBufferSize) {
        this.parsingRequestLinePhase = 0;
        this.parsingRequestLineEol = false;
        this.parsingRequestLineStart = 0;
        this.parsingRequestLineQPos = -1;
        this.headerData = new HeaderParseData();
        this.request = request;
        this.headers = request.getMimeHeaders();
        this.headerBufferSize = headerBufferSize;
        this.inputStreamInputBuffer = new SocketInputBuffer();
        this.filterLibrary = new InputFilter[0];
        this.activeFilters = new InputFilter[0];
        this.lastActiveFilter = -1;
        this.parsingHeader = true;
        this.parsingRequestLine = true;
        this.parsingRequestLinePhase = 0;
        this.parsingRequestLineEol = false;
        this.parsingRequestLineStart = 0;
        this.parsingRequestLineQPos = -1;
        this.headerParsePos = HeaderParsePosition.HEADER_START;
        this.headerData.recycle();
        this.swallowInput = true;
    }
    
    @Override
    public void recycle() {
        super.recycle();
        this.socket = null;
        this.headerParsePos = HeaderParsePosition.HEADER_START;
        this.parsingRequestLine = true;
        this.parsingRequestLinePhase = 0;
        this.parsingRequestLineEol = false;
        this.parsingRequestLineStart = 0;
        this.parsingRequestLineQPos = -1;
        this.headerData.recycle();
    }
    
    @Override
    public void nextRequest() {
        super.nextRequest();
        this.headerParsePos = HeaderParsePosition.HEADER_START;
        this.parsingRequestLine = true;
        this.parsingRequestLinePhase = 0;
        this.parsingRequestLineEol = false;
        this.parsingRequestLineStart = 0;
        this.parsingRequestLineQPos = -1;
        this.headerData.recycle();
    }
    
    @Override
    public boolean parseRequestLine(final boolean useAvailableDataOnly) throws IOException {
        if (!this.parsingRequestLine) {
            return true;
        }
        if (this.parsingRequestLinePhase == 0) {
            byte chr = 0;
            do {
                if (this.pos >= this.lastValid) {
                    if (useAvailableDataOnly) {
                        return false;
                    }
                    if (!this.fill(true, false)) {
                        return false;
                    }
                }
                chr = this.buf[this.pos++];
            } while (chr == 13 || chr == 10);
            --this.pos;
            this.parsingRequestLineStart = this.pos;
            this.parsingRequestLinePhase = 2;
            if (InternalNioInputBuffer.log.isDebugEnabled()) {
                InternalNioInputBuffer.log.debug((Object)("Received [" + new String(this.buf, this.pos, this.lastValid - this.pos, InternalNioInputBuffer.DEFAULT_CHARSET) + "]"));
            }
        }
        if (this.parsingRequestLinePhase == 2) {
            boolean space = false;
            while (!space) {
                if (this.pos >= this.lastValid && !this.fill(true, false)) {
                    return false;
                }
                if (this.buf[this.pos] == 13 || this.buf[this.pos] == 10) {
                    throw new IllegalArgumentException(InternalNioInputBuffer.sm.getString("iib.invalidmethod"));
                }
                if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                    space = true;
                    this.request.method().setBytes(this.buf, this.parsingRequestLineStart, this.pos - this.parsingRequestLineStart);
                }
                ++this.pos;
            }
            this.parsingRequestLinePhase = 3;
        }
        if (this.parsingRequestLinePhase == 3) {
            boolean space = true;
            while (space) {
                if (this.pos >= this.lastValid && !this.fill(true, false)) {
                    return false;
                }
                if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                    ++this.pos;
                }
                else {
                    space = false;
                }
            }
            this.parsingRequestLineStart = this.pos;
            this.parsingRequestLinePhase = 4;
        }
        if (this.parsingRequestLinePhase == 4) {
            int end = 0;
            boolean space2 = false;
            while (!space2) {
                if (this.pos >= this.lastValid && !this.fill(true, false)) {
                    return false;
                }
                if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                    space2 = true;
                    end = this.pos;
                }
                else if (this.buf[this.pos] == 13 || this.buf[this.pos] == 10) {
                    this.parsingRequestLineEol = true;
                    space2 = true;
                    end = this.pos;
                }
                else if (this.buf[this.pos] == 63 && this.parsingRequestLineQPos == -1) {
                    this.parsingRequestLineQPos = this.pos;
                }
                ++this.pos;
            }
            this.request.unparsedURI().setBytes(this.buf, this.parsingRequestLineStart, end - this.parsingRequestLineStart);
            if (this.parsingRequestLineQPos >= 0) {
                this.request.queryString().setBytes(this.buf, this.parsingRequestLineQPos + 1, end - this.parsingRequestLineQPos - 1);
                this.request.requestURI().setBytes(this.buf, this.parsingRequestLineStart, this.parsingRequestLineQPos - this.parsingRequestLineStart);
            }
            else {
                this.request.requestURI().setBytes(this.buf, this.parsingRequestLineStart, end - this.parsingRequestLineStart);
            }
            this.parsingRequestLinePhase = 5;
        }
        if (this.parsingRequestLinePhase == 5) {
            boolean space = true;
            while (space) {
                if (this.pos >= this.lastValid && !this.fill(true, false)) {
                    return false;
                }
                if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                    ++this.pos;
                }
                else {
                    space = false;
                }
            }
            this.parsingRequestLineStart = this.pos;
            this.parsingRequestLinePhase = 6;
            this.end = 0;
        }
        if (this.parsingRequestLinePhase == 6) {
            while (!this.parsingRequestLineEol) {
                if (this.pos >= this.lastValid && !this.fill(true, false)) {
                    return false;
                }
                if (this.buf[this.pos] == 13) {
                    this.end = this.pos;
                }
                else if (this.buf[this.pos] == 10) {
                    if (this.end == 0) {
                        this.end = this.pos;
                    }
                    this.parsingRequestLineEol = true;
                }
                ++this.pos;
            }
            if (this.end - this.parsingRequestLineStart > 0) {
                this.request.protocol().setBytes(this.buf, this.parsingRequestLineStart, this.end - this.parsingRequestLineStart);
            }
            else {
                this.request.protocol().setString("");
            }
            this.parsingRequestLine = false;
            this.parsingRequestLinePhase = 0;
            this.parsingRequestLineEol = false;
            this.parsingRequestLineStart = 0;
            return true;
        }
        throw new IllegalStateException("Invalid request line parse phase:" + this.parsingRequestLinePhase);
    }
    
    private void expand(final int newsize) {
        if (newsize > this.buf.length) {
            if (this.parsingHeader) {
                throw new IllegalArgumentException(InternalNioInputBuffer.sm.getString("iib.requestheadertoolarge.error"));
            }
            InternalNioInputBuffer.log.warn((Object)("Expanding buffer size. Old size: " + this.buf.length + ", new size: " + newsize), (Throwable)new Exception());
            final byte[] tmp = new byte[newsize];
            System.arraycopy(this.buf, 0, tmp, 0, this.buf.length);
            this.buf = tmp;
        }
    }
    
    private int readSocket(final boolean timeout, final boolean block) throws IOException {
        int nRead = 0;
        this.socket.getBufHandler().getReadBuffer().clear();
        if (block) {
            Selector selector = null;
            try {
                selector = this.pool.get();
            }
            catch (IOException ex) {}
            try {
                final NioEndpoint.KeyAttachment att = (NioEndpoint.KeyAttachment)this.socket.getAttachment(false);
                if (att == null) {
                    throw new IOException("Key must be cancelled.");
                }
                nRead = this.pool.read(this.socket.getBufHandler().getReadBuffer(), this.socket, selector, this.socket.getIOChannel().socket().getSoTimeout());
            }
            catch (EOFException eof) {
                nRead = -1;
            }
            finally {
                if (selector != null) {
                    this.pool.put(selector);
                }
            }
        }
        else {
            nRead = this.socket.read(this.socket.getBufHandler().getReadBuffer());
        }
        if (nRead > 0) {
            this.socket.getBufHandler().getReadBuffer().flip();
            this.socket.getBufHandler().getReadBuffer().limit(nRead);
            this.expand(nRead + this.pos);
            this.socket.getBufHandler().getReadBuffer().get(this.buf, this.pos, nRead);
            this.lastValid = this.pos + nRead;
            return nRead;
        }
        if (nRead == -1) {
            throw new EOFException(InternalNioInputBuffer.sm.getString("iib.eof.error"));
        }
        return 0;
    }
    
    @Override
    public boolean parseHeaders() throws IOException {
        if (!this.parsingHeader) {
            throw new IllegalStateException(InternalNioInputBuffer.sm.getString("iib.parseheaders.ise.error"));
        }
        HeaderParseStatus status = HeaderParseStatus.HAVE_MORE_HEADERS;
        do {
            status = this.parseHeader();
            if (this.pos > this.headerBufferSize || this.buf.length - this.pos < this.socketReadBufferSize) {
                throw new IllegalArgumentException(InternalNioInputBuffer.sm.getString("iib.requestheadertoolarge.error"));
            }
        } while (status == HeaderParseStatus.HAVE_MORE_HEADERS);
        if (status == HeaderParseStatus.DONE) {
            this.parsingHeader = false;
            this.end = this.pos;
            return true;
        }
        return false;
    }
    
    private HeaderParseStatus parseHeader() throws IOException {
        byte chr = 0;
        while (this.headerParsePos == HeaderParsePosition.HEADER_START) {
            if (this.pos >= this.lastValid && !this.fill(true, false)) {
                this.headerParsePos = HeaderParsePosition.HEADER_START;
                return HeaderParseStatus.NEED_MORE_DATA;
            }
            chr = this.buf[this.pos];
            if (chr == 13) {
                ++this.pos;
            }
            else {
                if (chr == 10) {
                    ++this.pos;
                    return HeaderParseStatus.DONE;
                }
                break;
            }
        }
        if (this.headerParsePos == HeaderParsePosition.HEADER_START) {
            this.headerData.start = this.pos;
            this.headerParsePos = HeaderParsePosition.HEADER_NAME;
        }
        while (this.headerParsePos == HeaderParsePosition.HEADER_NAME) {
            if (this.pos >= this.lastValid && !this.fill(true, false)) {
                return HeaderParseStatus.NEED_MORE_DATA;
            }
            chr = this.buf[this.pos];
            if (chr == 58) {
                this.headerParsePos = HeaderParsePosition.HEADER_VALUE_START;
                this.headerData.headerValue = this.headers.addValue(this.buf, this.headerData.start, this.pos - this.headerData.start);
                ++this.pos;
                this.headerData.start = this.pos;
                this.headerData.realPos = this.pos;
                this.headerData.lastSignificantChar = this.pos;
                break;
            }
            if (!InternalNioInputBuffer.HTTP_TOKEN_CHAR[chr]) {
                this.headerData.lastSignificantChar = this.pos;
                return this.skipLine();
            }
            if (chr >= 65 && chr <= 90) {
                this.buf[this.pos] = (byte)(chr + 32);
            }
            ++this.pos;
        }
        if (this.headerParsePos == HeaderParsePosition.HEADER_SKIPLINE) {
            return this.skipLine();
        }
        while (this.headerParsePos == HeaderParsePosition.HEADER_VALUE_START || this.headerParsePos == HeaderParsePosition.HEADER_VALUE || this.headerParsePos == HeaderParsePosition.HEADER_MULTI_LINE) {
            Label_0450: {
                if (this.headerParsePos == HeaderParsePosition.HEADER_VALUE_START) {
                    while (this.pos < this.lastValid || this.fill(true, false)) {
                        chr = this.buf[this.pos];
                        if (chr != 32 && chr != 9) {
                            this.headerParsePos = HeaderParsePosition.HEADER_VALUE;
                            break Label_0450;
                        }
                        ++this.pos;
                    }
                    return HeaderParseStatus.NEED_MORE_DATA;
                }
            }
            if (this.headerParsePos == HeaderParsePosition.HEADER_VALUE) {
                boolean eol = false;
                while (!eol) {
                    if (this.pos >= this.lastValid && !this.fill(true, false)) {
                        return HeaderParseStatus.NEED_MORE_DATA;
                    }
                    chr = this.buf[this.pos];
                    if (chr != 13) {
                        if (chr == 10) {
                            eol = true;
                        }
                        else if (chr == 32 || chr == 9) {
                            this.buf[this.headerData.realPos] = chr;
                            final HeaderParseData headerData = this.headerData;
                            ++headerData.realPos;
                        }
                        else {
                            this.buf[this.headerData.realPos] = chr;
                            final HeaderParseData headerData2 = this.headerData;
                            ++headerData2.realPos;
                            this.headerData.lastSignificantChar = this.headerData.realPos;
                        }
                    }
                    ++this.pos;
                }
                this.headerData.realPos = this.headerData.lastSignificantChar;
                this.headerParsePos = HeaderParsePosition.HEADER_MULTI_LINE;
            }
            if (this.pos >= this.lastValid && !this.fill(true, false)) {
                return HeaderParseStatus.NEED_MORE_DATA;
            }
            chr = this.buf[this.pos];
            if (this.headerParsePos != HeaderParsePosition.HEADER_MULTI_LINE) {
                continue;
            }
            if (chr != 32 && chr != 9) {
                this.headerParsePos = HeaderParsePosition.HEADER_START;
                break;
            }
            this.buf[this.headerData.realPos] = chr;
            final HeaderParseData headerData3 = this.headerData;
            ++headerData3.realPos;
            this.headerParsePos = HeaderParsePosition.HEADER_VALUE_START;
        }
        this.headerData.headerValue.setBytes(this.buf, this.headerData.start, this.headerData.lastSignificantChar - this.headerData.start);
        this.headerData.recycle();
        return HeaderParseStatus.HAVE_MORE_HEADERS;
    }
    
    public int getParsingRequestLinePhase() {
        return this.parsingRequestLinePhase;
    }
    
    private HeaderParseStatus skipLine() throws IOException {
        this.headerParsePos = HeaderParsePosition.HEADER_SKIPLINE;
        boolean eol = false;
        while (!eol) {
            if (this.pos >= this.lastValid && !this.fill(true, false)) {
                return HeaderParseStatus.NEED_MORE_DATA;
            }
            if (this.buf[this.pos] != 13) {
                if (this.buf[this.pos] == 10) {
                    eol = true;
                }
                else {
                    this.headerData.lastSignificantChar = this.pos;
                }
            }
            ++this.pos;
        }
        if (InternalNioInputBuffer.log.isDebugEnabled()) {
            InternalNioInputBuffer.log.debug((Object)InternalNioInputBuffer.sm.getString("iib.invalidheader", new Object[] { new String(this.buf, this.headerData.start, this.headerData.lastSignificantChar - this.headerData.start + 1, InternalNioInputBuffer.DEFAULT_CHARSET) }));
        }
        this.headerParsePos = HeaderParsePosition.HEADER_START;
        return HeaderParseStatus.HAVE_MORE_HEADERS;
    }
    
    @Override
    protected void init(final SocketWrapper<NioChannel> socketWrapper, final AbstractEndpoint endpoint) throws IOException {
        this.socket = socketWrapper.getSocket();
        this.socketReadBufferSize = this.socket.getBufHandler().getReadBuffer().capacity();
        final int bufLength = this.headerBufferSize + this.socketReadBufferSize;
        if (this.buf == null || this.buf.length < bufLength) {
            this.buf = new byte[bufLength];
        }
        this.pool = ((NioEndpoint)endpoint).getSelectorPool();
    }
    
    @Override
    protected boolean fill(final boolean block) throws IOException, EOFException {
        return this.fill(true, block);
    }
    
    protected boolean fill(final boolean timeout, final boolean block) throws IOException, EOFException {
        boolean read = false;
        if (this.parsingHeader) {
            if (this.lastValid > this.headerBufferSize) {
                throw new IllegalArgumentException(InternalNioInputBuffer.sm.getString("iib.requestheadertoolarge.error"));
            }
            read = (this.readSocket(timeout, block) > 0);
        }
        else {
            final int end = this.end;
            this.pos = end;
            this.lastValid = end;
            read = (this.readSocket(timeout, block) > 0);
        }
        return read;
    }
    
    static {
        log = LogFactory.getLog((Class)InternalNioInputBuffer.class);
        DEFAULT_CHARSET = Charset.forName("ISO-8859-1");
    }
    
    enum HeaderParseStatus
    {
        DONE, 
        HAVE_MORE_HEADERS, 
        NEED_MORE_DATA;
    }
    
    enum HeaderParsePosition
    {
        HEADER_START, 
        HEADER_NAME, 
        HEADER_VALUE_START, 
        HEADER_VALUE, 
        HEADER_MULTI_LINE, 
        HEADER_SKIPLINE;
    }
    
    public static class HeaderParseData
    {
        int start;
        int realPos;
        int lastSignificantChar;
        MessageBytes headerValue;
        
        public HeaderParseData() {
            this.start = 0;
            this.realPos = 0;
            this.lastSignificantChar = 0;
            this.headerValue = null;
        }
        
        public void recycle() {
            this.start = 0;
            this.realPos = 0;
            this.lastSignificantChar = 0;
            this.headerValue = null;
        }
    }
    
    protected class SocketInputBuffer implements InputBuffer
    {
        @Override
        public int doRead(final ByteChunk chunk, final Request req) throws IOException {
            if (InternalNioInputBuffer.this.pos >= InternalNioInputBuffer.this.lastValid && !InternalNioInputBuffer.this.fill(true, true)) {
                return -1;
            }
            final int length = InternalNioInputBuffer.this.lastValid - InternalNioInputBuffer.this.pos;
            chunk.setBytes(InternalNioInputBuffer.this.buf, InternalNioInputBuffer.this.pos, length);
            InternalNioInputBuffer.this.pos = InternalNioInputBuffer.this.lastValid;
            return length;
        }
    }
}
