// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;
import org.apache.tomcat.util.buf.B2CConverter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.buf.MessageBytes;
import java.util.ArrayList;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.log.UserDataHelper;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.http.hpp.ParameterValue;
import org.apache.tomcat.util.http.hpp.ParameterType;
import org.apache.tomcat.util.http.hpp.exception.HttpParameterPollutionException;

public final class Parameters
{
    private static final Log log;
    private static final UserDataHelper userDataLog;
    private static final UserDataHelper maxParamCountLog;
    protected static final StringManager sm;
    private final Map<String, ArrayList<String>> paramHashValues;
    private boolean didQueryParameters;
    MessageBytes queryMB;
    UDecoder urlDec;
    MessageBytes decodedQuery;
    String encoding;
    String queryStringEncoding;
    private int limit;
    private int parameterCount;
    private boolean parseFailed;
    ByteChunk tmpName;
    ByteChunk tmpValue;
    private final ByteChunk origName;
    private final ByteChunk origValue;
    CharChunk tmpNameC;
    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final Charset DEFAULT_CHARSET;
    
    public Parameters() {
        this.paramHashValues = new LinkedHashMap<String, ArrayList<String>>();
        this.didQueryParameters = false;
        this.decodedQuery = MessageBytes.newInstance();
        this.encoding = null;
        this.queryStringEncoding = null;
        this.limit = -1;
        this.parameterCount = 0;
        this.parseFailed = false;
        this.tmpName = new ByteChunk();
        this.tmpValue = new ByteChunk();
        this.origName = new ByteChunk();
        this.origValue = new ByteChunk();
        this.tmpNameC = new CharChunk(1024);
    }
    
    public void setQuery(final MessageBytes queryMB) {
        this.queryMB = queryMB;
    }
    
    public void setLimit(final int limit) {
        this.limit = limit;
    }
    
    public String getEncoding() {
        return this.encoding;
    }
    
    public void setEncoding(final String s) {
        this.encoding = s;
        if (Parameters.log.isDebugEnabled()) {
            Parameters.log.debug((Object)("Set encoding to " + s));
        }
    }
    
    public void setQueryStringEncoding(final String s) {
        this.queryStringEncoding = s;
        if (Parameters.log.isDebugEnabled()) {
            Parameters.log.debug((Object)("Set query string encoding to " + s));
        }
    }
    
    public boolean isParseFailed() {
        return this.parseFailed;
    }
    
    public void setParseFailed(final boolean parseFailed) {
        this.parseFailed = parseFailed;
    }
    
    public void recycle() {
        this.parameterCount = 0;
        this.paramHashValues.clear();
        this.didQueryParameters = false;
        this.encoding = null;
        this.decodedQuery.recycle();
        this.parseFailed = false;
    }
    
    @Deprecated
    public void addParameterValues(final String key, final String[] newValues) {
        if (key == null) {
            return;
        }
        ArrayList<String> values = this.paramHashValues.get(key);
        if (values == null) {
            values = new ArrayList<String>(newValues.length);
            this.paramHashValues.put(key, values);
        }
        else {
            values.ensureCapacity(values.size() + newValues.length);
        }
        for (final String newValue : newValues) {
            values.add(newValue);
        }
    }
    
    public String[] getParameterValues(final String name) {
        this.handleQueryParameters();
        final ArrayList<String> values = this.paramHashValues.get(name);
        if (values == null) {
            return null;
        }
        return values.toArray(new String[values.size()]);
    }
    
    public Enumeration<String> getParameterNames() {
        this.handleQueryParameters();
        return Collections.enumeration(this.paramHashValues.keySet());
    }
    
    public String getParameter(final String name) {
        this.handleQueryParameters();
        final ArrayList<String> values = this.paramHashValues.get(name);
        if (values == null) {
            return null;
        }
        if (values.size() == 0) {
            return "";
        }
        return values.get(0);
    }
    

    public String getParameter(String name, ParameterType pType) throws HttpParameterPollutionException {
       handleQueryParameters();
       ArrayList<String> values = (ArrayList)this.paramHashValues.get(name);
      if (values != null) {
         if (values.size() == 0) {
           return "";
         } else if(pType.equals(ParameterType.SINGLE_VALUED) && values.size() > 1) {
  	              throw new HttpParameterPollutionException("Multiple Values for Single Valued Parameter "+ name + "found");
         } else
            return (String)values.get(0);
       }
       return null;
    }


    public String getParameter(String name, ParameterType pType, ParameterValue pValue) throws HttpParameterPollutionException {
        handleQueryParameters();
        ArrayList<String> values = (ArrayList)this.paramHashValues.get(name);
       if (values != null) {
          if (values.size() == 0) {
            return "";
          } else if(pType.equals(ParameterType.SINGLE_VALUED) && values.size() > 1) {
   	              throw new HttpParameterPollutionException("Multiple Values for Single Valued Parameter "+ name + "found");
                   } else
            return getPositionalParameter(name,pValue);
       }
     return null;
     }

   		   
   		   
   		   private String getPositionalParameter(String name, ParameterValue pValue) {
   			   
   			   ArrayList<String> values = (ArrayList)this.paramHashValues.get(name);
   			   StringBuilder returnValue = new StringBuilder();
   			   switch(pValue) {
   				   
   				   case FETCH_FIRST:
   				        returnValue.append((String)values.get(0));
   						break;
   						
   				   case FETCH_LAST:
   				        returnValue.append((String)values.get(values.size() - 1));
   				        break;
   						
   				   case CONCATENATE:
   			            for(String value : values) {
   							returnValue.append(value);
   						}
   			   }
   			   
   			   return returnValue.toString();
   		   }
    
    
    public void handleQueryParameters() {
        if (this.didQueryParameters) {
            return;
        }
        this.didQueryParameters = true;
        if (this.queryMB == null || this.queryMB.isNull()) {
            return;
        }
        if (Parameters.log.isDebugEnabled()) {
            Parameters.log.debug((Object)("Decoding query " + this.decodedQuery + " " + this.queryStringEncoding));
        }
        try {
            this.decodedQuery.duplicate(this.queryMB);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.processParameters(this.decodedQuery, this.queryStringEncoding);
    }
    
    public void addParameter(final String key, final String value) throws IllegalStateException {
        if (key == null) {
            return;
        }
        ++this.parameterCount;
        if (this.limit > -1 && this.parameterCount > this.limit) {
            this.parseFailed = true;
            throw new IllegalStateException(Parameters.sm.getString("parameters.maxCountFail", new Object[] { this.limit }));
        }
        ArrayList<String> values = this.paramHashValues.get(key);
        if (values == null) {
            values = new ArrayList<String>(1);
            this.paramHashValues.put(key, values);
        }
        values.add(value);
    }
    
    public void setURLDecoder(final UDecoder u) {
        this.urlDec = u;
    }
    
    public void processParameters(final byte[] bytes, final int start, final int len) {
        this.processParameters(bytes, start, len, this.getCharset(this.encoding));
    }
    
    private void processParameters(final byte[] bytes, final int start, final int len, final Charset charset) {
        if (Parameters.log.isDebugEnabled()) {
            Parameters.log.debug((Object)Parameters.sm.getString("parameters.bytes", new Object[] { new String(bytes, start, len, Parameters.DEFAULT_CHARSET) }));
        }
        int decodeFailCount = 0;
        int pos = start;
        final int end = start + len;
        while (pos < end) {
            final int nameStart = pos;
            int nameEnd = -1;
            int valueStart = -1;
            int valueEnd = -1;
            boolean parsingName = true;
            boolean decodeName = false;
            boolean decodeValue = false;
            boolean parameterComplete = false;
            do {
                switch (bytes[pos]) {
                    case 61: {
                        if (parsingName) {
                            nameEnd = pos;
                            parsingName = false;
                            valueStart = ++pos;
                            continue;
                        }
                        ++pos;
                        continue;
                    }
                    case 38: {
                        if (parsingName) {
                            nameEnd = pos;
                        }
                        else {
                            valueEnd = pos;
                        }
                        parameterComplete = true;
                        ++pos;
                        continue;
                    }
                    case 37:
                    case 43: {
                        if (parsingName) {
                            decodeName = true;
                        }
                        else {
                            decodeValue = true;
                        }
                        ++pos;
                        continue;
                    }
                    default: {
                        ++pos;
                        continue;
                    }
                }
            } while (!parameterComplete && pos < end);
            if (pos == end) {
                if (nameEnd == -1) {
                    nameEnd = pos;
                }
                else if (valueStart > -1 && valueEnd == -1) {
                    valueEnd = pos;
                }
            }
            if (Parameters.log.isDebugEnabled() && valueStart == -1) {
                Parameters.log.debug((Object)Parameters.sm.getString("parameters.noequal", new Object[] { nameStart, nameEnd, new String(bytes, nameStart, nameEnd - nameStart, Parameters.DEFAULT_CHARSET) }));
            }
            if (nameEnd <= nameStart) {
                if (valueStart == -1) {
                    if (!Parameters.log.isDebugEnabled()) {
                        continue;
                    }
                    Parameters.log.debug((Object)Parameters.sm.getString("parameters.emptyChunk"));
                }
                else {
                    final UserDataHelper.Mode logMode = Parameters.userDataLog.getNextMode();
                    if (logMode != null) {
                        String extract;
                        if (valueEnd > nameStart) {
                            extract = new String(bytes, nameStart, valueEnd - nameStart, Parameters.DEFAULT_CHARSET);
                        }
                        else {
                            extract = "";
                        }
                        String message = Parameters.sm.getString("parameters.invalidChunk", new Object[] { nameStart, valueEnd, extract });
                        switch (logMode) {
                            case INFO_THEN_DEBUG: {
                                message += Parameters.sm.getString("parameters.fallToDebug");
                            }
                            case INFO: {
                                Parameters.log.info((Object)message);
                                break;
                            }
                            case DEBUG: {
                                Parameters.log.debug((Object)message);
                                break;
                            }
                        }
                    }
                    this.parseFailed = true;
                }
            }
            else {
                this.tmpName.setBytes(bytes, nameStart, nameEnd - nameStart);
                if (valueStart >= 0) {
                    this.tmpValue.setBytes(bytes, valueStart, valueEnd - valueStart);
                }
                else {
                    this.tmpValue.setBytes(bytes, 0, 0);
                }
                if (Parameters.log.isDebugEnabled()) {
                    try {
                        this.origName.append(bytes, nameStart, nameEnd - nameStart);
                        if (valueStart >= 0) {
                            this.origValue.append(bytes, valueStart, valueEnd - valueStart);
                        }
                        else {
                            this.origValue.append(bytes, 0, 0);
                        }
                    }
                    catch (IOException ioe) {
                        Parameters.log.error((Object)Parameters.sm.getString("parameters.copyFail"), (Throwable)ioe);
                    }
                }
                try {
                    if (decodeName) {
                        this.urlDecode(this.tmpName);
                    }
                    this.tmpName.setCharset(charset);
                    final String name = this.tmpName.toString();
                    String value;
                    if (valueStart >= 0) {
                        if (decodeValue) {
                            this.urlDecode(this.tmpValue);
                        }
                        this.tmpValue.setCharset(charset);
                        value = this.tmpValue.toString();
                    }
                    else {
                        value = "";
                    }
                    try {
                        this.addParameter(name, value);
                    }
                    catch (IllegalStateException ise) {
                        this.parseFailed = true;
                        final UserDataHelper.Mode logMode2 = Parameters.maxParamCountLog.getNextMode();
                        if (logMode2 != null) {
                            String message2 = ise.getMessage();
                            switch (logMode2) {
                                case INFO_THEN_DEBUG: {
                                    message2 += Parameters.sm.getString("parameters.maxCountFail.fallToDebug");
                                }
                                case INFO: {
                                    Parameters.log.info((Object)message2);
                                    break;
                                }
                                case DEBUG: {
                                    Parameters.log.debug((Object)message2);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                catch (IOException e) {
                    this.parseFailed = true;
                    if (++decodeFailCount == 1 || Parameters.log.isDebugEnabled()) {
                        if (Parameters.log.isDebugEnabled()) {
                            Parameters.log.debug((Object)Parameters.sm.getString("parameters.decodeFail.debug", new Object[] { this.origName.toString(), this.origValue.toString() }), (Throwable)e);
                        }
                        else if (Parameters.log.isInfoEnabled()) {
                            final UserDataHelper.Mode logMode3 = Parameters.userDataLog.getNextMode();
                            if (logMode3 != null) {
                                String message = Parameters.sm.getString("parameters.decodeFail.info", new Object[] { this.tmpName.toString(), this.tmpValue.toString() });
                                switch (logMode3) {
                                    case DEBUG: {
                                        Parameters.log.debug((Object)message);
                                        break;
                                    }
                                    case INFO_THEN_DEBUG: {
                                        message += Parameters.sm.getString("parameters.fallToDebug");
                                    }
                                    case INFO: {
                                        Parameters.log.info((Object)message);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                this.tmpName.recycle();
                this.tmpValue.recycle();
                if (!Parameters.log.isDebugEnabled()) {
                    continue;
                }
                this.origName.recycle();
                this.origValue.recycle();
            }
        }
        if (decodeFailCount > 1 && !Parameters.log.isDebugEnabled()) {
            final UserDataHelper.Mode logMode4 = Parameters.userDataLog.getNextMode();
            if (logMode4 != null) {
                String message3 = Parameters.sm.getString("parameters.multipleDecodingFail", new Object[] { decodeFailCount });
                switch (logMode4) {
                    case INFO_THEN_DEBUG: {
                        message3 += Parameters.sm.getString("parameters.fallToDebug");
                    }
                    case INFO: {
                        Parameters.log.info((Object)message3);
                        break;
                    }
                    case DEBUG: {
                        Parameters.log.debug((Object)message3);
                        break;
                    }
                }
            }
        }
    }
    
    private void urlDecode(final ByteChunk bc) throws IOException {
        if (this.urlDec == null) {
            this.urlDec = new UDecoder();
        }
        this.urlDec.convert(bc, true);
    }
    
    public void processParameters(final MessageBytes data, final String encoding) {
        if (data == null || data.isNull() || data.getLength() <= 0) {
            return;
        }
        if (data.getType() != 2) {
            data.toBytes();
        }
        final ByteChunk bc = data.getByteChunk();
        this.processParameters(bc.getBytes(), bc.getOffset(), bc.getLength(), this.getCharset(encoding));
    }
    
    private Charset getCharset(final String encoding) {
        if (encoding == null) {
            return Parameters.DEFAULT_CHARSET;
        }
        try {
            return B2CConverter.getCharset(encoding);
        }
        catch (UnsupportedEncodingException e) {
            return Parameters.DEFAULT_CHARSET;
        }
    }
    
    public String paramsAsString() {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, ArrayList<String>> e : this.paramHashValues.entrySet()) {
            sb.append(e.getKey()).append('=');
            final ArrayList<String> values = e.getValue();
            for (final String value : values) {
                sb.append(value).append(',');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    
    static {
        log = LogFactory.getLog((Class)Parameters.class);
        userDataLog = new UserDataHelper(Parameters.log);
        maxParamCountLog = new UserDataHelper(Parameters.log);
        sm = StringManager.getManager("org.apache.tomcat.util.http");
        DEFAULT_CHARSET = Charset.forName("ISO-8859-1");
    }
}
