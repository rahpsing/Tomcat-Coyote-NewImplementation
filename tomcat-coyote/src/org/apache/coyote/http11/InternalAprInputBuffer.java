// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.coyote.InputBuffer;
import org.apache.juli.logging.LogFactory;
import java.net.SocketTimeoutException;
import org.apache.tomcat.jni.Socket;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.buf.ByteChunk;
import java.nio.charset.Charset;
import org.apache.tomcat.util.buf.MessageBytes;
import java.io.IOException;
import java.io.EOFException;
import org.apache.coyote.Request;
import java.nio.ByteBuffer;
import org.apache.juli.logging.Log;

public class InternalAprInputBuffer extends AbstractInputBuffer<Long>
{
    private static final Log log;
    private ByteBuffer bbuf;
    private long socket;
    
    public InternalAprInputBuffer(final Request request, final int headerBufferSize) {
        this.request = request;
        this.headers = request.getMimeHeaders();
        this.buf = new byte[headerBufferSize];
        if (headerBufferSize < 8192) {
            this.bbuf = ByteBuffer.allocateDirect(9000);
        }
        else {
            this.bbuf = ByteBuffer.allocateDirect((headerBufferSize / 1500 + 1) * 1500);
        }
        this.inputStreamInputBuffer = new SocketInputBuffer();
        this.filterLibrary = new InputFilter[0];
        this.activeFilters = new InputFilter[0];
        this.lastActiveFilter = -1;
        this.parsingHeader = true;
        this.swallowInput = true;
    }
    
    @Override
    public void recycle() {
        this.socket = 0L;
        super.recycle();
    }
    
    @Override
    public boolean parseRequestLine(final boolean useAvailableData) throws IOException {
        int start = 0;
        byte chr = 0;
        do {
            if (this.pos >= this.lastValid) {
                if (useAvailableData) {
                    return false;
                }
                if (!this.fill()) {
                    throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
                }
            }
            chr = this.buf[this.pos++];
        } while (chr == 13 || chr == 10);
        --this.pos;
        start = this.pos;
        if (this.pos >= this.lastValid) {
            if (useAvailableData) {
                return false;
            }
            if (!this.fill()) {
                throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
            }
        }
        boolean space = false;
        while (!space) {
            if (this.pos >= this.lastValid && !this.fill()) {
                throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
            }
            if (this.buf[this.pos] == 13 || this.buf[this.pos] == 10) {
                throw new IllegalArgumentException(InternalAprInputBuffer.sm.getString("iib.invalidmethod"));
            }
            if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                space = true;
                this.request.method().setBytes(this.buf, start, this.pos - start);
            }
            ++this.pos;
        }
        while (space) {
            if (this.pos >= this.lastValid && !this.fill()) {
                throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
            }
            if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                ++this.pos;
            }
            else {
                space = false;
            }
        }
        start = this.pos;
        int end = 0;
        int questionPos = -1;
        boolean eol = false;
        while (!space) {
            if (this.pos >= this.lastValid && !this.fill()) {
                throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
            }
            if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                space = true;
                end = this.pos;
            }
            else if (this.buf[this.pos] == 13 || this.buf[this.pos] == 10) {
                eol = true;
                space = true;
                end = this.pos;
            }
            else if (this.buf[this.pos] == 63 && questionPos == -1) {
                questionPos = this.pos;
            }
            ++this.pos;
        }
        this.request.unparsedURI().setBytes(this.buf, start, end - start);
        if (questionPos >= 0) {
            this.request.queryString().setBytes(this.buf, questionPos + 1, end - questionPos - 1);
            this.request.requestURI().setBytes(this.buf, start, questionPos - start);
        }
        else {
            this.request.requestURI().setBytes(this.buf, start, end - start);
        }
        while (space) {
            if (this.pos >= this.lastValid && !this.fill()) {
                throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
            }
            if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                ++this.pos;
            }
            else {
                space = false;
            }
        }
        start = this.pos;
        end = 0;
        while (!eol) {
            if (this.pos >= this.lastValid && !this.fill()) {
                throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
            }
            if (this.buf[this.pos] == 13) {
                end = this.pos;
            }
            else if (this.buf[this.pos] == 10) {
                if (end == 0) {
                    end = this.pos;
                }
                eol = true;
            }
            ++this.pos;
        }
        if (end - start > 0) {
            this.request.protocol().setBytes(this.buf, start, end - start);
        }
        else {
            this.request.protocol().setString("");
        }
        return true;
    }
    
    @Override
    public boolean parseHeaders() throws IOException {
        if (!this.parsingHeader) {
            throw new IllegalStateException(InternalAprInputBuffer.sm.getString("iib.parseheaders.ise.error"));
        }
        while (this.parseHeader()) {}
        this.parsingHeader = false;
        this.end = this.pos;
        return true;
    }
    
    private boolean parseHeader() throws IOException {
        byte chr = 0;
        while (this.pos < this.lastValid || this.fill()) {
            chr = this.buf[this.pos];
            if (chr == 13) {
                ++this.pos;
            }
            else {
                if (chr == 10) {
                    ++this.pos;
                    return false;
                }
                int start = this.pos;
                boolean colon = false;
                MessageBytes headerValue = null;
                while (!colon) {
                    if (this.pos >= this.lastValid && !this.fill()) {
                        throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
                    }
                    if (this.buf[this.pos] == 58) {
                        colon = true;
                        headerValue = this.headers.addValue(this.buf, start, this.pos - start);
                    }
                    else if (!InternalAprInputBuffer.HTTP_TOKEN_CHAR[this.buf[this.pos]]) {
                        this.skipLine(start);
                        return true;
                    }
                    chr = this.buf[this.pos];
                    if (chr >= 65 && chr <= 90) {
                        this.buf[this.pos] = (byte)(chr + 32);
                    }
                    ++this.pos;
                }
                start = this.pos;
                int realPos = this.pos;
                boolean eol = false;
                boolean validLine = true;
                while (validLine) {
                    boolean space = true;
                    while (space) {
                        if (this.pos >= this.lastValid && !this.fill()) {
                            throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
                        }
                        if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                            ++this.pos;
                        }
                        else {
                            space = false;
                        }
                    }
                    int lastSignificantChar = realPos;
                    while (!eol) {
                        if (this.pos >= this.lastValid && !this.fill()) {
                            throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
                        }
                        if (this.buf[this.pos] != 13) {
                            if (this.buf[this.pos] == 10) {
                                eol = true;
                            }
                            else if (this.buf[this.pos] == 32) {
                                this.buf[realPos] = this.buf[this.pos];
                                ++realPos;
                            }
                            else {
                                this.buf[realPos] = this.buf[this.pos];
                                lastSignificantChar = ++realPos;
                            }
                        }
                        ++this.pos;
                    }
                    realPos = lastSignificantChar;
                    if (this.pos >= this.lastValid && !this.fill()) {
                        throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
                    }
                    chr = this.buf[this.pos];
                    if (chr != 32 && chr != 9) {
                        validLine = false;
                    }
                    else {
                        eol = false;
                        this.buf[realPos] = chr;
                        ++realPos;
                    }
                }
                headerValue.setBytes(this.buf, start, realPos - start);
                return true;
            }
        }
        throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
    }
    
    private void skipLine(final int start) throws IOException {
        boolean eol = false;
        int lastRealByte = start;
        if (this.pos - 1 > start) {
            lastRealByte = this.pos - 1;
        }
        while (!eol) {
            if (this.pos >= this.lastValid && !this.fill()) {
                throw new EOFException(InternalAprInputBuffer.sm.getString("iib.eof.error"));
            }
            if (this.buf[this.pos] != 13) {
                if (this.buf[this.pos] == 10) {
                    eol = true;
                }
                else {
                    lastRealByte = this.pos;
                }
            }
            ++this.pos;
        }
        if (InternalAprInputBuffer.log.isDebugEnabled()) {
            InternalAprInputBuffer.log.debug((Object)InternalAprInputBuffer.sm.getString("iib.invalidheader", new Object[] { new String(this.buf, start, lastRealByte - start + 1, Charset.forName("ISO-8859-1")) }));
        }
    }
    
    @Override
    public int doRead(final ByteChunk chunk, final Request req) throws IOException {
        if (this.lastActiveFilter == -1) {
            return this.inputStreamInputBuffer.doRead(chunk, req);
        }
        return this.activeFilters[this.lastActiveFilter].doRead(chunk, req);
    }
    
    @Override
    protected void init(final SocketWrapper<Long> socketWrapper, final AbstractEndpoint endpoint) throws IOException {
        Socket.setrbb(this.socket = socketWrapper.getSocket(), this.bbuf);
    }
    
    @Override
    protected boolean fill(final boolean block) throws IOException {
        return this.fill();
    }
    
    protected boolean fill() throws IOException {
        int nRead = 0;
        if (this.parsingHeader) {
            if (this.lastValid == this.buf.length) {
                throw new IllegalArgumentException(InternalAprInputBuffer.sm.getString("iib.requestheadertoolarge.error"));
            }
            this.bbuf.clear();
            nRead = Socket.recvbb(this.socket, 0, this.buf.length - this.lastValid);
            if (nRead > 0) {
                this.bbuf.limit(nRead);
                this.bbuf.get(this.buf, this.pos, nRead);
                this.lastValid = this.pos + nRead;
            }
            else {
                if (-nRead == 120002) {
                    return false;
                }
                throw new IOException(InternalAprInputBuffer.sm.getString("iib.failedread"));
            }
        }
        else {
            if (this.buf.length - this.end < 4500) {
                this.buf = new byte[this.buf.length];
                this.end = 0;
            }
            this.pos = this.end;
            this.lastValid = this.pos;
            this.bbuf.clear();
            nRead = Socket.recvbb(this.socket, 0, this.buf.length - this.lastValid);
            if (nRead > 0) {
                this.bbuf.limit(nRead);
                this.bbuf.get(this.buf, this.pos, nRead);
                this.lastValid = this.pos + nRead;
            }
            else {
                if (-nRead == 120005 || -nRead == 120001) {
                    throw new SocketTimeoutException(InternalAprInputBuffer.sm.getString("iib.failedread"));
                }
                if (nRead == 0) {
                    return false;
                }
                throw new IOException(InternalAprInputBuffer.sm.getString("iib.failedread"));
            }
        }
        return nRead > 0;
    }
    
    static {
        log = LogFactory.getLog((Class)InternalAprInputBuffer.class);
    }
    
    protected class SocketInputBuffer implements InputBuffer
    {
        @Override
        public int doRead(final ByteChunk chunk, final Request req) throws IOException {
            if (InternalAprInputBuffer.this.pos >= InternalAprInputBuffer.this.lastValid && !InternalAprInputBuffer.this.fill()) {
                return -1;
            }
            final int length = InternalAprInputBuffer.this.lastValid - InternalAprInputBuffer.this.pos;
            chunk.setBytes(InternalAprInputBuffer.this.buf, InternalAprInputBuffer.this.pos, length);
            InternalAprInputBuffer.this.pos = InternalAprInputBuffer.this.lastValid;
            return length;
        }
    }
}
