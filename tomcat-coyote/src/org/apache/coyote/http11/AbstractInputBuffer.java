// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import java.io.IOException;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.coyote.Request;
import org.apache.tomcat.util.res.StringManager;
import org.apache.coyote.InputBuffer;

public abstract class AbstractInputBuffer<S> implements InputBuffer
{
    protected static final boolean[] HTTP_TOKEN_CHAR;
    protected static final StringManager sm;
    protected Request request;
    protected MimeHeaders headers;
    protected boolean parsingHeader;
    protected boolean swallowInput;
    protected byte[] buf;
    protected int lastValid;
    protected int pos;
    protected int end;
    protected InputBuffer inputStreamInputBuffer;
    protected InputFilter[] filterLibrary;
    protected InputFilter[] activeFilters;
    protected int lastActiveFilter;
    
    public void addFilter(final InputFilter filter) {
        final InputFilter[] newFilterLibrary = new InputFilter[this.filterLibrary.length + 1];
        for (int i = 0; i < this.filterLibrary.length; ++i) {
            newFilterLibrary[i] = this.filterLibrary[i];
        }
        newFilterLibrary[this.filterLibrary.length] = filter;
        this.filterLibrary = newFilterLibrary;
        this.activeFilters = new InputFilter[this.filterLibrary.length];
    }
    
    public InputFilter[] getFilters() {
        return this.filterLibrary;
    }
    
    public void addActiveFilter(final InputFilter filter) {
        if (this.lastActiveFilter == -1) {
            filter.setBuffer(this.inputStreamInputBuffer);
        }
        else {
            for (int i = 0; i <= this.lastActiveFilter; ++i) {
                if (this.activeFilters[i] == filter) {
                    return;
                }
            }
            filter.setBuffer(this.activeFilters[this.lastActiveFilter]);
        }
        (this.activeFilters[++this.lastActiveFilter] = filter).setRequest(this.request);
    }
    
    public void setSwallowInput(final boolean swallowInput) {
        this.swallowInput = swallowInput;
    }
    
    public abstract boolean parseRequestLine(final boolean p0) throws IOException;
    
    public abstract boolean parseHeaders() throws IOException;
    
    protected abstract boolean fill(final boolean p0) throws IOException;
    
    protected abstract void init(final SocketWrapper<S> p0, final AbstractEndpoint p1) throws IOException;
    
    public void recycle() {
        this.request.recycle();
        for (int i = 0; i <= this.lastActiveFilter; ++i) {
            this.activeFilters[i].recycle();
        }
        this.lastValid = 0;
        this.pos = 0;
        this.lastActiveFilter = -1;
        this.parsingHeader = true;
        this.swallowInput = true;
    }
    
    public void nextRequest() {
        this.request.recycle();
        if (this.lastValid - this.pos > 0) {
            int npos;
            int opos;
            for (npos = 0, opos = this.pos; this.lastValid - opos > opos - npos; npos += this.pos, opos += this.pos) {
                System.arraycopy(this.buf, opos, this.buf, npos, opos - npos);
            }
            System.arraycopy(this.buf, opos, this.buf, npos, this.lastValid - opos);
        }
        for (int i = 0; i <= this.lastActiveFilter; ++i) {
            this.activeFilters[i].recycle();
        }
        this.lastValid -= this.pos;
        this.pos = 0;
        this.lastActiveFilter = -1;
        this.parsingHeader = true;
        this.swallowInput = true;
    }
    
    public void endRequest() throws IOException {
        if (this.swallowInput && this.lastActiveFilter != -1) {
            final int extraBytes = (int)this.activeFilters[this.lastActiveFilter].end();
            this.pos -= extraBytes;
        }
    }
    
    public int available() {
        int result = this.lastValid - this.pos;
        if (result == 0 && this.lastActiveFilter >= 0) {
            for (int i = 0; result == 0 && i <= this.lastActiveFilter; result = this.activeFilters[i].available(), ++i) {}
        }
        return result;
    }
    
    @Override
    public int doRead(final ByteChunk chunk, final Request req) throws IOException {
        if (this.lastActiveFilter == -1) {
            return this.inputStreamInputBuffer.doRead(chunk, req);
        }
        return this.activeFilters[this.lastActiveFilter].doRead(chunk, req);
    }
    
    static {
        HTTP_TOKEN_CHAR = new boolean[128];
        sm = StringManager.getManager("org.apache.coyote.http11");
        for (int i = 0; i < 128; ++i) {
            if (i < 32) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 127) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 40) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 41) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 60) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 62) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 64) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 44) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 59) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 58) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 92) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 34) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 47) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 91) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 93) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 63) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 61) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 123) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 125) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 32) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else if (i == 9) {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = false;
            }
            else {
                AbstractInputBuffer.HTTP_TOKEN_CHAR[i] = true;
            }
        }
    }
}
