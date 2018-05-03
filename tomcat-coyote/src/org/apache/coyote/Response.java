// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.coyote;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.http.parser.MediaType;
import java.io.IOException;
import org.apache.tomcat.util.http.parser.HttpParser;
import java.io.StringReader;
import org.apache.tomcat.util.http.MimeHeaders;
import java.util.Locale;

public final class Response
{
    private static Locale DEFAULT_LOCALE;
    protected int status;
    protected String message;
    protected MimeHeaders headers;
    protected OutputBuffer outputBuffer;
    protected Object[] notes;
    protected boolean commited;
    public ActionHook hook;
    protected String contentType;
    protected String contentLanguage;
    protected String characterEncoding;
    protected long contentLength;
    private Locale locale;
    private long contentWritten;
    private long commitTime;
    protected Exception errorException;
    protected boolean charsetSet;
    protected Request req;
    
    public Response() {
        this.status = 200;
        this.message = null;
        this.headers = new MimeHeaders();
        this.notes = new Object[32];
        this.commited = false;
        this.contentType = null;
        this.contentLanguage = null;
        this.characterEncoding = "ISO-8859-1";
        this.contentLength = -1L;
        this.locale = Response.DEFAULT_LOCALE;
        this.contentWritten = 0L;
        this.commitTime = -1L;
        this.errorException = null;
        this.charsetSet = false;
    }
    
    public Request getRequest() {
        return this.req;
    }
    
    public void setRequest(final Request req) {
        this.req = req;
    }
    
    public OutputBuffer getOutputBuffer() {
        return this.outputBuffer;
    }
    
    public void setOutputBuffer(final OutputBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
    }
    
    public MimeHeaders getMimeHeaders() {
        return this.headers;
    }
    
    public ActionHook getHook() {
        return this.hook;
    }
    
    public void setHook(final ActionHook hook) {
        this.hook = hook;
    }
    
    public final void setNote(final int pos, final Object value) {
        this.notes[pos] = value;
    }
    
    public final Object getNote(final int pos) {
        return this.notes[pos];
    }
    
    public void action(final ActionCode actionCode, final Object param) {
        if (this.hook != null) {
            if (param == null) {
                this.hook.action(actionCode, this);
            }
            else {
                this.hook.action(actionCode, param);
            }
        }
    }
    
    public int getStatus() {
        return this.status;
    }
    
    public void setStatus(final int status) {
        this.status = status;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public void setMessage(final String message) {
        this.message = message;
    }
    
    public boolean isCommitted() {
        return this.commited;
    }
    
    public void setCommitted(final boolean v) {
        if (v && !this.commited) {
            this.commitTime = System.currentTimeMillis();
        }
        this.commited = v;
    }
    
    public long getCommitTime() {
        return this.commitTime;
    }
    
    public void setErrorException(final Exception ex) {
        this.errorException = ex;
    }
    
    public Exception getErrorException() {
        return this.errorException;
    }
    
    public boolean isExceptionPresent() {
        return this.errorException != null;
    }
    
    public void reset() throws IllegalStateException {
        this.contentType = null;
        this.locale = Response.DEFAULT_LOCALE;
        this.contentLanguage = null;
        this.characterEncoding = "ISO-8859-1";
        this.contentLength = -1L;
        this.charsetSet = false;
        this.status = 200;
        this.message = null;
        this.headers.clear();
        if (this.commited) {
            throw new IllegalStateException();
        }
        this.action(ActionCode.RESET, this);
    }
    
    public void finish() {
        this.action(ActionCode.CLOSE, this);
    }
    
    public void acknowledge() {
        this.action(ActionCode.ACK, this);
    }
    
    public boolean containsHeader(final String name) {
        return this.headers.getHeader(name) != null;
    }
    
    public void setHeader(final String name, final String value) {
        final char cc = name.charAt(0);
        if ((cc == 'C' || cc == 'c') && this.checkSpecialHeader(name, value)) {
            return;
        }
        this.headers.setValue(name).setString(value);
    }
    
    public void addHeader(final String name, final String value) {
        final char cc = name.charAt(0);
        if ((cc == 'C' || cc == 'c') && this.checkSpecialHeader(name, value)) {
            return;
        }
        this.headers.addValue(name).setString(value);
    }
    
    private boolean checkSpecialHeader(final String name, final String value) {
        if (name.equalsIgnoreCase("Content-Type")) {
            this.setContentType(value);
            return true;
        }
        if (name.equalsIgnoreCase("Content-Length")) {
            try {
                final long cL = Long.parseLong(value);
                this.setContentLength(cL);
                return true;
            }
            catch (NumberFormatException ex) {
                return false;
            }
        }
        if (name.equalsIgnoreCase("Content-Language")) {}
        return false;
    }
    
    public void sendHeaders() {
        this.action(ActionCode.COMMIT, this);
        this.setCommitted(true);
    }
    
    public Locale getLocale() {
        return this.locale;
    }
    
    public void setLocale(final Locale locale) {
        if (locale == null) {
            return;
        }
        this.locale = locale;
        this.contentLanguage = locale.getLanguage();
        if (this.contentLanguage != null && this.contentLanguage.length() > 0) {
            final String country = locale.getCountry();
            final StringBuilder value = new StringBuilder(this.contentLanguage);
            if (country != null && country.length() > 0) {
                value.append('-');
                value.append(country);
            }
            this.contentLanguage = value.toString();
        }
    }
    
    public String getContentLanguage() {
        return this.contentLanguage;
    }
    
    public void setCharacterEncoding(final String charset) {
        if (this.isCommitted()) {
            return;
        }
        if (charset == null) {
            return;
        }
        this.characterEncoding = charset;
        this.charsetSet = true;
    }
    
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }
    
    public void setContentType(final String type) {
        if (type == null) {
            this.contentType = null;
            return;
        }
        MediaType m = null;
        try {
            m = HttpParser.parseMediaType(new StringReader(type));
        }
        catch (IOException ex) {}
        if (m == null) {
            this.contentType = type;
            return;
        }
        this.contentType = m.toStringNoCharset();
        final String charsetValue = m.getCharset().trim();
        if (charsetValue != null && charsetValue.length() > 0) {
            this.charsetSet = true;
            this.characterEncoding = charsetValue;
        }
    }
    
    public void setContentTypeNoCharset(final String type) {
        this.contentType = type;
    }
    
    public String getContentType() {
        String ret = this.contentType;
        if (ret != null && this.characterEncoding != null && this.charsetSet) {
            ret = ret + ";charset=" + this.characterEncoding;
        }
        return ret;
    }
    
    public void setContentLength(final int contentLength) {
        this.contentLength = contentLength;
    }
    
    public void setContentLength(final long contentLength) {
        this.contentLength = contentLength;
    }
    
    public int getContentLength() {
        final long length = this.getContentLengthLong();
        if (length < 2147483647L) {
            return (int)length;
        }
        return -1;
    }
    
    public long getContentLengthLong() {
        return this.contentLength;
    }
    
    public void doWrite(final ByteChunk chunk) throws IOException {
        this.outputBuffer.doWrite(chunk, this);
        this.contentWritten += chunk.getLength();
    }
    
    public void recycle() {
        this.contentType = null;
        this.contentLanguage = null;
        this.locale = Response.DEFAULT_LOCALE;
        this.characterEncoding = "ISO-8859-1";
        this.charsetSet = false;
        this.contentLength = -1L;
        this.status = 200;
        this.message = null;
        this.commited = false;
        this.commitTime = -1L;
        this.errorException = null;
        this.headers.clear();
        this.contentWritten = 0L;
    }
    
    public long getContentWritten() {
        return this.contentWritten;
    }
    
    public long getBytesWritten(final boolean flush) {
        if (flush) {
            this.action(ActionCode.CLIENT_FLUSH, this);
        }
        return this.outputBuffer.getBytesWritten();
    }
    
    static {
        Response.DEFAULT_LOCALE = Locale.getDefault();
    }
}
