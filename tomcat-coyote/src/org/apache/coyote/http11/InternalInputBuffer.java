// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.InputBuffer;
import org.apache.juli.logging.LogFactory;
import java.nio.charset.Charset;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.tomcat.util.buf.MessageBytes;
import java.io.IOException;
import java.io.EOFException;
import org.apache.coyote.Request;
import java.io.InputStream;
import org.apache.juli.logging.Log;
import java.net.Socket;

public class InternalInputBuffer extends AbstractInputBuffer<Socket>
{
    private static final Log log;
    private InputStream inputStream;
    
    public InternalInputBuffer(final Request request, final int headerBufferSize) {
        this.request = request;
        this.headers = request.getMimeHeaders();
        this.buf = new byte[headerBufferSize];
        this.inputStreamInputBuffer = new InputStreamInputBuffer();
        this.filterLibrary = new InputFilter[0];
        this.activeFilters = new InputFilter[0];
        this.lastActiveFilter = -1;
        this.parsingHeader = true;
        this.swallowInput = true;
    }
    
    @Override
    public boolean parseRequestLine(final boolean useAvailableDataOnly) throws IOException {
        int start = 0;
        byte chr = 0;
        while (this.pos < this.lastValid || this.fill()) {
            chr = this.buf[this.pos++];
            if (chr != 13 && chr != 10) {
                --this.pos;
                start = this.pos;
                boolean space = false;
                while (!space) {
                    if (this.pos >= this.lastValid && !this.fill()) {
                        throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
                    }
                    if (this.buf[this.pos] == 13 || this.buf[this.pos] == 10) {
                        throw new IllegalArgumentException(InternalInputBuffer.sm.getString("iib.invalidmethod"));
                    }
                    if (this.buf[this.pos] == 32 || this.buf[this.pos] == 9) {
                        space = true;
                        this.request.method().setBytes(this.buf, start, this.pos - start);
                    }
                    ++this.pos;
                }
                while (space) {
                    if (this.pos >= this.lastValid && !this.fill()) {
                        throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
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
                        throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
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
                        throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
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
                        throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
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
        }
        throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
    }
    
    @Override
    public boolean parseHeaders() throws IOException {
        if (!this.parsingHeader) {
            throw new IllegalStateException(InternalInputBuffer.sm.getString("iib.parseheaders.ise.error"));
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
                        throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
                    }
                    if (this.buf[this.pos] == 58) {
                        colon = true;
                        headerValue = this.headers.addValue(this.buf, start, this.pos - start);
                    }
                    else if (!InternalInputBuffer.HTTP_TOKEN_CHAR[this.buf[this.pos]]) {
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
                            throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
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
                            throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
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
                        throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
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
        throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
    }
    
    @Override
    public void recycle() {
        super.recycle();
        this.inputStream = null;
    }
    
    @Override
    protected void init(final SocketWrapper<Socket> socketWrapper, final AbstractEndpoint endpoint) throws IOException {
        this.inputStream = socketWrapper.getSocket().getInputStream();
    }
    
    private void skipLine(final int start) throws IOException {
        boolean eol = false;
        int lastRealByte = start;
        if (this.pos - 1 > start) {
            lastRealByte = this.pos - 1;
        }
        while (!eol) {
            if (this.pos >= this.lastValid && !this.fill()) {
                throw new EOFException(InternalInputBuffer.sm.getString("iib.eof.error"));
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
        if (InternalInputBuffer.log.isDebugEnabled()) {
            InternalInputBuffer.log.debug((Object)InternalInputBuffer.sm.getString("iib.invalidheader", new Object[] { new String(this.buf, start, lastRealByte - start + 1, Charset.forName("ISO-8859-1")) }));
        }
    }
    
    protected boolean fill() throws IOException {
        return this.fill(true);
    }
    
    @Override
    protected boolean fill(final boolean block) throws IOException {
        int nRead = 0;
        if (this.parsingHeader) {
            if (this.lastValid == this.buf.length) {
                throw new IllegalArgumentException(InternalInputBuffer.sm.getString("iib.requestheadertoolarge.error"));
            }
            nRead = this.inputStream.read(this.buf, this.pos, this.buf.length - this.lastValid);
            if (nRead > 0) {
                this.lastValid = this.pos + nRead;
            }
        }
        else {
            if (this.buf.length - this.end < 4500) {
                this.buf = new byte[this.buf.length];
                this.end = 0;
            }
            this.pos = this.end;
            this.lastValid = this.pos;
            nRead = this.inputStream.read(this.buf, this.pos, this.buf.length - this.lastValid);
            if (nRead > 0) {
                this.lastValid = this.pos + nRead;
            }
        }
        return nRead > 0;
    }
    
    static {
        log = LogFactory.getLog((Class)InternalInputBuffer.class);
    }
    
    protected class InputStreamInputBuffer implements InputBuffer
    {
        @Override
        public int doRead(final ByteChunk chunk, final Request req) throws IOException {
            if (InternalInputBuffer.this.pos >= InternalInputBuffer.this.lastValid && !InternalInputBuffer.this.fill()) {
                return -1;
            }
            final int length = InternalInputBuffer.this.lastValid - InternalInputBuffer.this.pos;
            chunk.setBytes(InternalInputBuffer.this.buf, InternalInputBuffer.this.pos, length);
            InternalInputBuffer.this.pos = InternalInputBuffer.this.lastValid;
            return length;
        }
    }
}
