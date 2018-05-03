// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote.http11;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.tomcat.util.http.HttpMessages;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;
import org.apache.coyote.http11.filters.GzipOutputFilter;
import java.io.IOException;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
import org.apache.coyote.Response;
import org.apache.coyote.OutputBuffer;

public abstract class AbstractOutputBuffer<S> implements OutputBuffer
{
    protected Response response;
    protected boolean committed;
    protected boolean finished;
    protected byte[] buf;
    protected int pos;
    protected OutputFilter[] filterLibrary;
    protected OutputFilter[] activeFilters;
    protected int lastActiveFilter;
    protected OutputBuffer outputStreamOutputBuffer;
    protected long byteCount;
    protected static final StringManager sm;
    private static final Log log;
    
    public AbstractOutputBuffer() {
        this.byteCount = 0L;
    }
    
    public void addFilter(final OutputFilter filter) {
        final OutputFilter[] newFilterLibrary = new OutputFilter[this.filterLibrary.length + 1];
        for (int i = 0; i < this.filterLibrary.length; ++i) {
            newFilterLibrary[i] = this.filterLibrary[i];
        }
        newFilterLibrary[this.filterLibrary.length] = filter;
        this.filterLibrary = newFilterLibrary;
        this.activeFilters = new OutputFilter[this.filterLibrary.length];
    }
    
    public OutputFilter[] getFilters() {
        return this.filterLibrary;
    }
    
    public void addActiveFilter(final OutputFilter filter) {
        if (this.lastActiveFilter == -1) {
            filter.setBuffer(this.outputStreamOutputBuffer);
        }
        else {
            for (int i = 0; i <= this.lastActiveFilter; ++i) {
                if (this.activeFilters[i] == filter) {
                    return;
                }
            }
            filter.setBuffer(this.activeFilters[this.lastActiveFilter]);
        }
        (this.activeFilters[++this.lastActiveFilter] = filter).setResponse(this.response);
    }
    
    @Override
    public int doWrite(final ByteChunk chunk, final Response res) throws IOException {
        if (!this.committed) {
            this.response.action(ActionCode.COMMIT, null);
        }
        if (this.lastActiveFilter == -1) {
            return this.outputStreamOutputBuffer.doWrite(chunk, res);
        }
        return this.activeFilters[this.lastActiveFilter].doWrite(chunk, res);
    }
    
    @Override
    public long getBytesWritten() {
        if (this.lastActiveFilter == -1) {
            return this.outputStreamOutputBuffer.getBytesWritten();
        }
        return this.activeFilters[this.lastActiveFilter].getBytesWritten();
    }
    
    public void flush() throws IOException {
        if (!this.committed) {
            this.response.action(ActionCode.COMMIT, null);
        }
        for (int i = 0; i <= this.lastActiveFilter; ++i) {
            if (this.activeFilters[i] instanceof GzipOutputFilter) {
                if (AbstractOutputBuffer.log.isDebugEnabled()) {
                    AbstractOutputBuffer.log.debug((Object)("Flushing the gzip filter at position " + i + " of the filter chain..."));
                }
                ((GzipOutputFilter)this.activeFilters[i]).flush();
                break;
            }
        }
    }
    
    public void reset() {
        if (this.committed) {
            throw new IllegalStateException();
        }
        this.response.recycle();
        this.pos = 0;
        this.byteCount = 0L;
    }
    
    public void recycle() {
        this.nextRequest();
    }
    
    public void nextRequest() {
        for (int i = 0; i <= this.lastActiveFilter; ++i) {
            this.activeFilters[i].recycle();
        }
        this.response.recycle();
        this.pos = 0;
        this.lastActiveFilter = -1;
        this.committed = false;
        this.finished = false;
        this.byteCount = 0L;
    }
    
    public void endRequest() throws IOException {
        if (!this.committed) {
            this.response.action(ActionCode.COMMIT, null);
        }
        if (this.finished) {
            return;
        }
        if (this.lastActiveFilter != -1) {
            this.activeFilters[this.lastActiveFilter].end();
        }
        this.finished = true;
    }
    
    public abstract void init(final SocketWrapper<S> p0, final AbstractEndpoint p1) throws IOException;
    
    public abstract void sendAck() throws IOException;
    
    protected abstract void commit() throws IOException;
    
    public void sendStatus() {
        this.write(Constants.HTTP_11_BYTES);
        this.buf[this.pos++] = 32;
        final int status = this.response.getStatus();
        switch (status) {
            case 200: {
                this.write(Constants._200_BYTES);
                break;
            }
            case 400: {
                this.write(Constants._400_BYTES);
                break;
            }
            case 404: {
                this.write(Constants._404_BYTES);
                break;
            }
            default: {
                this.write(status);
                break;
            }
        }
        this.buf[this.pos++] = 32;
        String message = null;
        if (org.apache.coyote.Constants.USE_CUSTOM_STATUS_MSG_IN_HEADER && HttpMessages.isSafeInHttpHeader(this.response.getMessage())) {
            message = this.response.getMessage();
        }
        if (message == null) {
            this.write(HttpMessages.getInstance(this.response.getLocale()).getMessage(status));
        }
        else {
            this.write(message);
        }
        if (org.apache.coyote.Constants.IS_SECURITY_ENABLED) {
            AccessController.doPrivileged((PrivilegedAction<Object>)new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    AbstractOutputBuffer.this.buf[AbstractOutputBuffer.this.pos++] = 13;
                    AbstractOutputBuffer.this.buf[AbstractOutputBuffer.this.pos++] = 10;
                    return null;
                }
            });
        }
        else {
            this.buf[this.pos++] = 13;
            this.buf[this.pos++] = 10;
        }
    }
    
    public void sendHeader(final MessageBytes name, final MessageBytes value) {
        this.write(name);
        this.buf[this.pos++] = 58;
        this.buf[this.pos++] = 32;
        this.write(value);
        this.buf[this.pos++] = 13;
        this.buf[this.pos++] = 10;
    }
    
    public void endHeaders() {
        this.buf[this.pos++] = 13;
        this.buf[this.pos++] = 10;
    }
    
    protected void write(final MessageBytes mb) {
        if (mb.getType() == 2) {
            final ByteChunk bc = mb.getByteChunk();
            this.write(bc);
        }
        else if (mb.getType() == 3) {
            final CharChunk cc = mb.getCharChunk();
            this.write(cc);
        }
        else {
            this.write(mb.toString());
        }
    }
    
    protected void write(final ByteChunk bc) {
        final int length = bc.getLength();
        this.checkLengthBeforeWrite(length);
        System.arraycopy(bc.getBytes(), bc.getStart(), this.buf, this.pos, length);
        this.pos += length;
    }
    
    protected void write(final CharChunk cc) {
        final int start = cc.getStart();
        final int end = cc.getEnd();
        this.checkLengthBeforeWrite(end - start);
        final char[] cbuf = cc.getBuffer();
        for (int i = start; i < end; ++i) {
            char c = cbuf[i];
            if ((c <= '\u001f' && c != '\t') || c == '\u007f' || c > '\u00ff') {
                c = ' ';
            }
            this.buf[this.pos++] = (byte)c;
        }
    }
    
    public void write(final byte[] b) {
        this.checkLengthBeforeWrite(b.length);
        System.arraycopy(b, 0, this.buf, this.pos, b.length);
        this.pos += b.length;
    }
    
    protected void write(final String s) {
        if (s == null) {
            return;
        }
        final int len = s.length();
        this.checkLengthBeforeWrite(len);
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if ((c <= '\u001f' && c != '\t') || c == '\u007f' || c > '\u00ff') {
                c = ' ';
            }
            this.buf[this.pos++] = (byte)c;
        }
    }
    
    protected void write(final int i) {
        this.write(String.valueOf(i));
    }
    
    private void checkLengthBeforeWrite(final int length) {
        if (this.pos + length > this.buf.length) {
            throw new HeadersTooLargeException(AbstractOutputBuffer.sm.getString("iob.responseheadertoolarge.error"));
        }
    }
    
    static {
        sm = StringManager.getManager("org.apache.coyote.http11");
        log = LogFactory.getLog((Class)AbstractOutputBuffer.class);
    }
}
