// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.parser;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.io.IOException;
import java.util.Locale;
import java.util.HashMap;
import java.io.StringReader;
import java.util.Map;

public class HttpParser
{
    private static final Integer FIELD_TYPE_TOKEN;
    private static final Integer FIELD_TYPE_QUOTED_STRING;
    private static final Integer FIELD_TYPE_TOKEN_OR_QUOTED_STRING;
    private static final Integer FIELD_TYPE_LHEX;
    private static final Integer FIELD_TYPE_QUOTED_TOKEN;
    private static final Map<String, Integer> fieldTypes;
    private static final boolean[] isToken;
    private static final boolean[] isHex;
    
    public static Map<String, String> parseAuthorizationDigest(final StringReader input) throws IllegalArgumentException, IOException {
        final Map<String, String> result = new HashMap<String, String>();
        if (skipConstant(input, "Digest") != SkipConstantResult.FOUND) {
            return null;
        }
        String field = readToken(input);
        if (field == null) {
            return null;
        }
        while (!field.equals("")) {
            if (skipConstant(input, "=") != SkipConstantResult.FOUND) {
                return null;
            }
            String value = null;
            Integer type = HttpParser.fieldTypes.get(field.toLowerCase(Locale.ENGLISH));
            if (type == null) {
                type = HttpParser.FIELD_TYPE_TOKEN_OR_QUOTED_STRING;
            }
            switch (type) {
                case 0: {
                    value = readToken(input);
                    break;
                }
                case 1: {
                    value = readQuotedString(input, false);
                    break;
                }
                case 2: {
                    value = readTokenOrQuotedString(input, false);
                    break;
                }
                case 3: {
                    value = readLhex(input);
                    break;
                }
                case 4: {
                    value = readQuotedToken(input);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("TODO i18n: Unsupported type");
                }
            }
            if (value == null) {
                return null;
            }
            result.put(field, value);
            if (skipConstant(input, ",") == SkipConstantResult.NOT_FOUND) {
                return null;
            }
            field = readToken(input);
            if (field == null) {
                return null;
            }
        }
        return result;
    }
    
    public static MediaType parseMediaType(final StringReader input) throws IOException {
        final String type = readToken(input);
        if (type == null || type.length() == 0) {
            return null;
        }
        if (skipConstant(input, "/") == SkipConstantResult.NOT_FOUND) {
            return null;
        }
        final String subtype = readToken(input);
        if (subtype == null || subtype.length() == 0) {
            return null;
        }
        final LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
        SkipConstantResult lookForSemiColon = skipConstant(input, ";");
        if (lookForSemiColon == SkipConstantResult.NOT_FOUND) {
            return null;
        }
        while (lookForSemiColon == SkipConstantResult.FOUND) {
            final String attribute = readToken(input);
            String value = "";
            if (skipConstant(input, "=") == SkipConstantResult.FOUND) {
                value = readTokenOrQuotedString(input, true);
            }
            if (attribute != null) {
                parameters.put(attribute.toLowerCase(Locale.ENGLISH), value);
            }
            lookForSemiColon = skipConstant(input, ";");
            if (lookForSemiColon == SkipConstantResult.NOT_FOUND) {
                return null;
            }
        }
        return new MediaType(type, subtype, parameters);
    }
    
    public static String unquote(final String input) {
        if (input == null || input.length() < 2 || input.charAt(0) != '\"') {
            return input;
        }
        final StringBuilder result = new StringBuilder();
        for (int i = 1; i < input.length() - 1; ++i) {
            final char c = input.charAt(i);
            if (input.charAt(i) == '\\') {
                ++i;
                result.append(input.charAt(i));
            }
            else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    private static boolean isToken(final int c) {
        try {
            return HttpParser.isToken[c];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }
    
    private static boolean isHex(final int c) {
        try {
            return HttpParser.isHex[c];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }
    
    private static int skipLws(final StringReader input, final boolean withReset) throws IOException {
        if (withReset) {
            input.mark(1);
        }
        int c;
        for (c = input.read(); c == 32 || c == 9 || c == 10 || c == 13; c = input.read()) {
            if (withReset) {
                input.mark(1);
            }
        }
        if (withReset) {
            input.reset();
        }
        return c;
    }
    
    private static SkipConstantResult skipConstant(final StringReader input, final String constant) throws IOException {
        final int len = constant.length();
        int c = skipLws(input, false);
        for (int i = 0; i < len; ++i) {
            if (i == 0 && c == -1) {
                return SkipConstantResult.EOF;
            }
            if (c != constant.charAt(i)) {
                input.skip(-(i + 1));
                return SkipConstantResult.NOT_FOUND;
            }
            if (i != len - 1) {
                c = input.read();
            }
        }
        return SkipConstantResult.FOUND;
    }
    
    private static String readToken(final StringReader input) throws IOException {
        final StringBuilder result = new StringBuilder();
        int c;
        for (c = skipLws(input, false); c != -1 && isToken(c); c = input.read()) {
            result.append((char)c);
        }
        input.skip(-1L);
        if (c != -1 && result.length() == 0) {
            return null;
        }
        return result.toString();
    }
    
    private static String readQuotedString(final StringReader input, final boolean returnQuoted) throws IOException {
        int c = skipLws(input, false);
        if (c != 34) {
            return null;
        }
        final StringBuilder result = new StringBuilder();
        if (returnQuoted) {
            result.append('\"');
        }
        for (c = input.read(); c != 34; c = input.read()) {
            if (c == -1) {
                return null;
            }
            if (c == 92) {
                c = input.read();
                if (returnQuoted) {
                    result.append('\\');
                }
                result.append(c);
            }
            else {
                result.append((char)c);
            }
        }
        if (returnQuoted) {
            result.append('\"');
        }
        return result.toString();
    }
    
    private static String readTokenOrQuotedString(final StringReader input, final boolean returnQuoted) throws IOException {
        final int c = skipLws(input, true);
        if (c == 34) {
            return readQuotedString(input, returnQuoted);
        }
        return readToken(input);
    }
    
    private static String readQuotedToken(final StringReader input) throws IOException {
        final StringBuilder result = new StringBuilder();
        boolean quoted = false;
        int c = skipLws(input, false);
        if (c == 34) {
            quoted = true;
        }
        else {
            if (c == -1 || !isToken(c)) {
                return null;
            }
            result.append((char)c);
        }
        for (c = input.read(); c != -1 && isToken(c); c = input.read()) {
            result.append((char)c);
        }
        if (quoted) {
            if (c != 34) {
                return null;
            }
        }
        else {
            input.skip(-1L);
        }
        if (c != -1 && result.length() == 0) {
            return null;
        }
        return result.toString();
    }
    
    private static String readLhex(final StringReader input) throws IOException {
        final StringBuilder result = new StringBuilder();
        boolean quoted = false;
        int c = skipLws(input, false);
        if (c == 34) {
            quoted = true;
        }
        else {
            if (c == -1 || !isHex(c)) {
                return null;
            }
            if (65 <= c && c <= 70) {
                c += 32;
            }
            result.append((char)c);
        }
        for (c = input.read(); c != -1 && isHex(c); c = input.read()) {
            if (65 <= c && c <= 70) {
                c += 32;
            }
            result.append((char)c);
        }
        if (quoted) {
            if (c != 34) {
                return null;
            }
        }
        else {
            input.skip(-1L);
        }
        if (c != -1 && result.length() == 0) {
            return null;
        }
        return result.toString();
    }
    
    static {
        FIELD_TYPE_TOKEN = 0;
        FIELD_TYPE_QUOTED_STRING = 1;
        FIELD_TYPE_TOKEN_OR_QUOTED_STRING = 2;
        FIELD_TYPE_LHEX = 3;
        FIELD_TYPE_QUOTED_TOKEN = 4;
        fieldTypes = new HashMap<String, Integer>();
        isToken = new boolean[128];
        isHex = new boolean[128];
        HttpParser.fieldTypes.put("username", HttpParser.FIELD_TYPE_QUOTED_STRING);
        HttpParser.fieldTypes.put("realm", HttpParser.FIELD_TYPE_QUOTED_STRING);
        HttpParser.fieldTypes.put("nonce", HttpParser.FIELD_TYPE_QUOTED_STRING);
        HttpParser.fieldTypes.put("digest-uri", HttpParser.FIELD_TYPE_QUOTED_STRING);
        HttpParser.fieldTypes.put("response", HttpParser.FIELD_TYPE_LHEX);
        HttpParser.fieldTypes.put("algorithm", HttpParser.FIELD_TYPE_QUOTED_TOKEN);
        HttpParser.fieldTypes.put("cnonce", HttpParser.FIELD_TYPE_QUOTED_STRING);
        HttpParser.fieldTypes.put("opaque", HttpParser.FIELD_TYPE_QUOTED_STRING);
        HttpParser.fieldTypes.put("qop", HttpParser.FIELD_TYPE_QUOTED_TOKEN);
        HttpParser.fieldTypes.put("nc", HttpParser.FIELD_TYPE_LHEX);
        for (int i = 0; i < 128; ++i) {
            if (i < 32) {
                HttpParser.isToken[i] = false;
            }
            else if (i == 40 || i == 41 || i == 60 || i == 62 || i == 64 || i == 44 || i == 59 || i == 58 || i == 92 || i == 34 || i == 47 || i == 91 || i == 93 || i == 63 || i == 61 || i == 123 || i == 125 || i == 32 || i == 9) {
                HttpParser.isToken[i] = false;
            }
            else {
                HttpParser.isToken[i] = true;
            }
            if ((i >= 48 && i <= 57) || (i >= 65 && i <= 70) || (i >= 97 && i <= 102)) {
                HttpParser.isHex[i] = true;
            }
            else {
                HttpParser.isHex[i] = false;
            }
        }
    }
    
    private enum SkipConstantResult
    {
        FOUND, 
        NOT_FOUND, 
        EOF;
    }
}
