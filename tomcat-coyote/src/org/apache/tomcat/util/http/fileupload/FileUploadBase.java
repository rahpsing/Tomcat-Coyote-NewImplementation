// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

import org.apache.tomcat.util.http.fileupload.util.Closeable;
import java.util.NoSuchElementException;
import java.io.InputStream;
import org.apache.tomcat.util.http.fileupload.util.LimitedInputStream;
import org.apache.tomcat.util.http.fileupload.util.FileItemHeadersImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Locale;
import java.nio.charset.Charset;

public abstract class FileUploadBase
{
    private static final Charset CHARSET_ISO_8859_1;
    public static final String CONTENT_TYPE = "Content-type";
    public static final String CONTENT_DISPOSITION = "Content-disposition";
    public static final String CONTENT_LENGTH = "Content-length";
    public static final String FORM_DATA = "form-data";
    public static final String ATTACHMENT = "attachment";
    public static final String MULTIPART = "multipart/";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String MULTIPART_MIXED = "multipart/mixed";
    private long sizeMax;
    private long fileSizeMax;
    private String headerEncoding;
    private ProgressListener listener;
    
    public FileUploadBase() {
        this.sizeMax = -1L;
        this.fileSizeMax = -1L;
    }
    
    public static final boolean isMultipartContent(final RequestContext ctx) {
        final String contentType = ctx.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/");
    }
    
    public abstract FileItemFactory getFileItemFactory();
    
    public abstract void setFileItemFactory(final FileItemFactory p0);
    
    public long getSizeMax() {
        return this.sizeMax;
    }
    
    public void setSizeMax(final long sizeMax) {
        this.sizeMax = sizeMax;
    }
    
    public long getFileSizeMax() {
        return this.fileSizeMax;
    }
    
    public void setFileSizeMax(final long fileSizeMax) {
        this.fileSizeMax = fileSizeMax;
    }
    
    public String getHeaderEncoding() {
        return this.headerEncoding;
    }
    
    public void setHeaderEncoding(final String encoding) {
        this.headerEncoding = encoding;
    }
    
    public FileItemIterator getItemIterator(final RequestContext ctx) throws FileUploadException, IOException {
        try {
            return new FileItemIteratorImpl(ctx);
        }
        catch (FileUploadIOException e) {
            throw (FileUploadException)e.getCause();
        }
    }
    
    public List<FileItem> parseRequest(final RequestContext ctx) throws FileUploadException {
        final List<FileItem> items = new ArrayList<FileItem>();
        boolean successful = false;
        try {
            final FileItemIterator iter = this.getItemIterator(ctx);
            final FileItemFactory fac = this.getFileItemFactory();
            if (fac == null) {
                throw new NullPointerException("No FileItemFactory has been set.");
            }
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl)item).name;
                final FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), item.isFormField(), fileName);
                items.add(fileItem);
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true);
                }
                catch (FileUploadIOException e) {
                    throw (FileUploadException)e.getCause();
                }
                catch (IOException e2) {
                    throw new IOFileUploadException(String.format("Processing of %s request failed. %s", "multipart/form-data", e2.getMessage()), e2);
                }
                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders(fih);
            }
            successful = true;
            return items;
        }
        catch (FileUploadIOException e3) {
            throw (FileUploadException)e3.getCause();
        }
        catch (IOException e4) {
            throw new FileUploadException(e4.getMessage(), e4);
        }
        finally {
            if (!successful) {
                for (final FileItem fileItem2 : items) {
                    try {
                        fileItem2.delete();
                    }
                    catch (Exception ex) {}
                }
            }
        }
    }
    
    public Map<String, List<FileItem>> parseParameterMap(final RequestContext ctx) throws FileUploadException {
        final List<FileItem> items = this.parseRequest(ctx);
        final Map<String, List<FileItem>> itemsMap = new HashMap<String, List<FileItem>>(items.size());
        for (final FileItem fileItem : items) {
            final String fieldName = fileItem.getFieldName();
            List<FileItem> mappedItems = itemsMap.get(fieldName);
            if (mappedItems == null) {
                mappedItems = new ArrayList<FileItem>();
                itemsMap.put(fieldName, mappedItems);
            }
            mappedItems.add(fileItem);
        }
        return itemsMap;
    }
    
    protected byte[] getBoundary(final String contentType) {
        final ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        final Map<String, String> params = parser.parse(contentType, new char[] { ';', ',' });
        final String boundaryStr = params.get("boundary");
        if (boundaryStr == null) {
            return null;
        }
        final byte[] boundary = boundaryStr.getBytes(FileUploadBase.CHARSET_ISO_8859_1);
        return boundary;
    }
    
    protected String getFileName(final FileItemHeaders headers) {
        return this.getFileName(headers.getHeader("Content-disposition"));
    }
    
    private String getFileName(final String pContentDisposition) {
        String fileName = null;
        if (pContentDisposition != null) {
            final String cdl = pContentDisposition.toLowerCase(Locale.ENGLISH);
            if (cdl.startsWith("form-data") || cdl.startsWith("attachment")) {
                final ParameterParser parser = new ParameterParser();
                parser.setLowerCaseNames(true);
                final Map<String, String> params = parser.parse(pContentDisposition, ';');
                if (params.containsKey("filename")) {
                    fileName = params.get("filename");
                    if (fileName != null) {
                        fileName = fileName.trim();
                    }
                    else {
                        fileName = "";
                    }
                }
            }
        }
        return fileName;
    }
    
    protected String getFieldName(final FileItemHeaders headers) {
        return this.getFieldName(headers.getHeader("Content-disposition"));
    }
    
    private String getFieldName(final String pContentDisposition) {
        String fieldName = null;
        if (pContentDisposition != null && pContentDisposition.toLowerCase(Locale.ENGLISH).startsWith("form-data")) {
            final ParameterParser parser = new ParameterParser();
            parser.setLowerCaseNames(true);
            final Map<String, String> params = parser.parse(pContentDisposition, ';');
            fieldName = params.get("name");
            if (fieldName != null) {
                fieldName = fieldName.trim();
            }
        }
        return fieldName;
    }
    
    protected FileItemHeaders getParsedHeaders(final String headerPart) {
        final int len = headerPart.length();
        final FileItemHeadersImpl headers = this.newFileItemHeaders();
        int start = 0;
        while (true) {
            int end = this.parseEndOfLine(headerPart, start);
            if (start == end) {
                break;
            }
            final StringBuilder header = new StringBuilder(headerPart.substring(start, end));
            for (start = end + 2; start < len; start = end + 2) {
                int nonWs;
                for (nonWs = start; nonWs < len; ++nonWs) {
                    final char c = headerPart.charAt(nonWs);
                    if (c != ' ' && c != '\t') {
                        break;
                    }
                }
                if (nonWs == start) {
                    break;
                }
                end = this.parseEndOfLine(headerPart, nonWs);
                header.append(" ").append(headerPart.substring(nonWs, end));
            }
            this.parseHeaderLine(headers, header.toString());
        }
        return headers;
    }
    
    protected FileItemHeadersImpl newFileItemHeaders() {
        return new FileItemHeadersImpl();
    }
    
    private int parseEndOfLine(final String headerPart, final int end) {
        int index = end;
        while (true) {
            final int offset = headerPart.indexOf(13, index);
            if (offset == -1 || offset + 1 >= headerPart.length()) {
                throw new IllegalStateException("Expected headers to be terminated by an empty line.");
            }
            if (headerPart.charAt(offset + 1) == '\n') {
                return offset;
            }
            index = offset + 1;
        }
    }
    
    private void parseHeaderLine(final FileItemHeadersImpl headers, final String header) {
        final int colonOffset = header.indexOf(58);
        if (colonOffset == -1) {
            return;
        }
        final String headerName = header.substring(0, colonOffset).trim();
        final String headerValue = header.substring(header.indexOf(58) + 1).trim();
        headers.addHeader(headerName, headerValue);
    }
    
    public ProgressListener getProgressListener() {
        return this.listener;
    }
    
    public void setProgressListener(final ProgressListener pListener) {
        this.listener = pListener;
    }
    
    static {
        CHARSET_ISO_8859_1 = Charset.forName("ISO-8859-1");
    }
    
    private class FileItemIteratorImpl implements FileItemIterator
    {
        private final MultipartStream multi;
        private final MultipartStream.ProgressNotifier notifier;
        private final byte[] boundary;
        private FileItemStreamImpl currentItem;
        private String currentFieldName;
        private boolean skipPreamble;
        private boolean itemValid;
        private boolean eof;
        final /* synthetic */ FileUploadBase this$0;
        
        FileItemIteratorImpl(final RequestContext ctx) throws FileUploadException, IOException {
            if (ctx == null) {
                throw new NullPointerException("ctx parameter");
            }
            final String contentType = ctx.getContentType();
            if (null == contentType || !contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/")) {
                throw new InvalidContentTypeException(String.format("the request doesn't contain a %s or %s stream, content type header is %s", "multipart/form-data", "multipart/form-data", contentType));
            }
            InputStream input = ctx.getInputStream();
            final long requestSize = ((UploadContext)ctx).contentLength();
            if (FileUploadBase.this.sizeMax >= 0L) {
                if (requestSize != -1L && requestSize > FileUploadBase.this.sizeMax) {
                    throw new SizeLimitExceededException(String.format("the request was rejected because its size (%s) exceeds the configured maximum (%s)", requestSize, FileUploadBase.this.sizeMax), requestSize, FileUploadBase.this.sizeMax);
                }
                input = new LimitedInputStream(input, FileUploadBase.this.sizeMax) {
                    @Override
                    protected void raiseError(final long pSizeMax, final long pCount) throws IOException {
                        final FileUploadException ex = new SizeLimitExceededException(String.format("the request was rejected because its size (%s) exceeds the configured maximum (%s)", pCount, pSizeMax), pCount, pSizeMax);
                        throw new FileUploadIOException(ex);
                    }
                };
            }
            String charEncoding = FileUploadBase.this.headerEncoding;
            if (charEncoding == null) {
                charEncoding = ctx.getCharacterEncoding();
            }
            this.boundary = FileUploadBase.this.getBoundary(contentType);
            if (this.boundary == null) {
                throw new FileUploadException("the request was rejected because no multipart boundary was found");
            }
            this.notifier = new MultipartStream.ProgressNotifier(FileUploadBase.this.listener, requestSize);
            (this.multi = new MultipartStream(input, this.boundary, this.notifier)).setHeaderEncoding(charEncoding);
            this.skipPreamble = true;
            this.findNextItem();
        }
        
        private boolean findNextItem() throws IOException {
            if (this.eof) {
                return false;
            }
            if (this.currentItem != null) {
                this.currentItem.close();
                this.currentItem = null;
            }
            while (true) {
                boolean nextPart;
                if (this.skipPreamble) {
                    nextPart = this.multi.skipPreamble();
                }
                else {
                    nextPart = this.multi.readBoundary();
                }
                if (!nextPart) {
                    if (this.currentFieldName == null) {
                        this.eof = true;
                        return false;
                    }
                    this.multi.setBoundary(this.boundary);
                    this.currentFieldName = null;
                }
                else {
                    final FileItemHeaders headers = FileUploadBase.this.getParsedHeaders(this.multi.readHeaders());
                    if (this.currentFieldName == null) {
                        final String fieldName = FileUploadBase.this.getFieldName(headers);
                        if (fieldName != null) {
                            final String subContentType = headers.getHeader("Content-type");
                            if (subContentType != null && subContentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/mixed")) {
                                this.currentFieldName = fieldName;
                                final byte[] subBoundary = FileUploadBase.this.getBoundary(subContentType);
                                this.multi.setBoundary(subBoundary);
                                this.skipPreamble = true;
                                continue;
                            }
                            final String fileName = FileUploadBase.this.getFileName(headers);
                            (this.currentItem = new FileItemStreamImpl(fileName, fieldName, headers.getHeader("Content-type"), fileName == null, this.getContentLength(headers))).setHeaders(headers);
                            this.notifier.noteItem();
                            return this.itemValid = true;
                        }
                    }
                    else {
                        final String fileName2 = FileUploadBase.this.getFileName(headers);
                        if (fileName2 != null) {
                            (this.currentItem = new FileItemStreamImpl(fileName2, this.currentFieldName, headers.getHeader("Content-type"), false, this.getContentLength(headers))).setHeaders(headers);
                            this.notifier.noteItem();
                            return this.itemValid = true;
                        }
                    }
                    this.multi.discardBodyData();
                }
            }
        }
        
        private long getContentLength(final FileItemHeaders pHeaders) {
            try {
                return Long.parseLong(pHeaders.getHeader("Content-length"));
            }
            catch (Exception e) {
                return -1L;
            }
        }
        
        @Override
        public boolean hasNext() throws FileUploadException, IOException {
            if (this.eof) {
                return false;
            }
            if (this.itemValid) {
                return true;
            }
            try {
                return this.findNextItem();
            }
            catch (FileUploadIOException e) {
                throw (FileUploadException)e.getCause();
            }
        }
        
        @Override
        public FileItemStream next() throws FileUploadException, IOException {
            if (this.eof || (!this.itemValid && !this.hasNext())) {
                throw new NoSuchElementException();
            }
            this.itemValid = false;
            return this.currentItem;
        }
        
        class FileItemStreamImpl implements FileItemStream
        {
            private final String contentType;
            private final String fieldName;
            private final String name;
            private final boolean formField;
            private final InputStream stream;
            private boolean opened;
            private FileItemHeaders headers;
            
            FileItemStreamImpl(final String pName, final String pFieldName, final String pContentType, final boolean pFormField, final long pContentLength) throws IOException {
                this.name = pName;
                this.fieldName = pFieldName;
                this.contentType = pContentType;
                this.formField = pFormField;
                InputStream istream;
                final MultipartStream.ItemInputStream itemStream = (MultipartStream.ItemInputStream)(istream = FileItemIteratorImpl.this.multi.newInputStream());
                if (FileItemIteratorImpl.this.this$0.fileSizeMax != -1L) {
                    if (pContentLength != -1L && pContentLength > FileItemIteratorImpl.this.this$0.fileSizeMax) {
                        final FileSizeLimitExceededException e = new FileSizeLimitExceededException(String.format("The field %s exceeds its maximum permitted size of %s bytes.", this.fieldName, FileItemIteratorImpl.this.this$0.fileSizeMax), pContentLength, FileItemIteratorImpl.this.this$0.fileSizeMax);
                        e.setFileName(pName);
                        e.setFieldName(pFieldName);
                        throw new FileUploadIOException(e);
                    }
                    istream = new LimitedInputStream(istream, FileItemIteratorImpl.this.this$0.fileSizeMax) {
                        @Override
                        protected void raiseError(final long pSizeMax, final long pCount) throws IOException {
                            itemStream.close(true);
                            final FileSizeLimitExceededException e = new FileSizeLimitExceededException(String.format("The field %s exceeds its maximum permitted size of %s bytes.", FileItemStreamImpl.this.fieldName, pSizeMax), pCount, pSizeMax);
                            e.setFieldName(FileItemStreamImpl.this.fieldName);
                            e.setFileName(FileItemStreamImpl.this.name);
                            throw new FileUploadIOException(e);
                        }
                    };
                }
                this.stream = istream;
            }
            
            @Override
            public String getContentType() {
                return this.contentType;
            }
            
            @Override
            public String getFieldName() {
                return this.fieldName;
            }
            
            @Override
            public String getName() {
                return Streams.checkFileName(this.name);
            }
            
            @Override
            public boolean isFormField() {
                return this.formField;
            }
            
            @Override
            public InputStream openStream() throws IOException {
                if (this.opened) {
                    throw new IllegalStateException("The stream was already opened.");
                }
                if (((Closeable)this.stream).isClosed()) {
                    throw new ItemSkippedException();
                }
                return this.stream;
            }
            
            void close() throws IOException {
                this.stream.close();
            }
            
            @Override
            public FileItemHeaders getHeaders() {
                return this.headers;
            }
            
            @Override
            public void setHeaders(final FileItemHeaders pHeaders) {
                this.headers = pHeaders;
            }
        }
    }
    
    public static class FileUploadIOException extends IOException
    {
        private static final long serialVersionUID = -3082868232248803474L;
        
        public FileUploadIOException() {
        }
        
        public FileUploadIOException(final String message, final Throwable cause) {
            super(message, cause);
        }
        
        public FileUploadIOException(final String message) {
            super(message);
        }
        
        public FileUploadIOException(final Throwable cause) {
            super(cause);
        }
    }
    
    public static class InvalidContentTypeException extends FileUploadException
    {
        private static final long serialVersionUID = -9073026332015646668L;
        
        public InvalidContentTypeException() {
        }
        
        public InvalidContentTypeException(final String message) {
            super(message);
        }
    }
    
    public static class IOFileUploadException extends FileUploadException
    {
        private static final long serialVersionUID = -5858565745868986701L;
        
        public IOFileUploadException() {
        }
        
        public IOFileUploadException(final String message, final Throwable cause) {
            super(message, cause);
        }
        
        public IOFileUploadException(final String message) {
            super(message);
        }
        
        public IOFileUploadException(final Throwable cause) {
            super(cause);
        }
    }
    
    public abstract static class SizeException extends FileUploadException
    {
        private static final long serialVersionUID = -8776225574705254126L;
        private final long actual;
        private final long permitted;
        
        protected SizeException(final String message, final long actual, final long permitted) {
            super(message);
            this.actual = actual;
            this.permitted = permitted;
        }
        
        public long getActualSize() {
            return this.actual;
        }
        
        public long getPermittedSize() {
            return this.permitted;
        }
    }
    
    public static class SizeLimitExceededException extends SizeException
    {
        private static final long serialVersionUID = -2474893167098052828L;
        
        public SizeLimitExceededException(final String message, final long actual, final long permitted) {
            super(message, actual, permitted);
        }
    }
    
    public static class FileSizeLimitExceededException extends SizeException
    {
        private static final long serialVersionUID = 8150776562029630058L;
        private String fileName;
        private String fieldName;
        
        public FileSizeLimitExceededException(final String message, final long actual, final long permitted) {
            super(message, actual, permitted);
        }
        
        public String getFileName() {
            return this.fileName;
        }
        
        public void setFileName(final String pFileName) {
            this.fileName = pFileName;
        }
        
        public String getFieldName() {
            return this.fieldName;
        }
        
        public void setFieldName(final String pFieldName) {
            this.fieldName = pFieldName;
        }
    }
}
