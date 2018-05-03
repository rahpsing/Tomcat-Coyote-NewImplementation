// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel.classfile;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import org.apache.tomcat.util.bcel.util.ByteSequence;
import org.apache.tomcat.util.bcel.Constants;

public abstract class Utility
{
    private static ThreadLocal<Integer> consumed_chars;
    private static boolean wide;
    private static final int FREE_CHARS = 48;
    static int[] CHAR_MAP;
    static int[] MAP_CHAR;
    
    private static int unwrap(final ThreadLocal<Integer> tl) {
        return tl.get();
    }
    
    private static void wrap(final ThreadLocal<Integer> tl, final int value) {
        tl.set(value);
    }
    
    public static final String accessToString(final int access_flags) {
        return accessToString(access_flags, false);
    }
    
    public static final String accessToString(final int access_flags, final boolean for_class) {
        final StringBuilder buf = new StringBuilder();
        int p = 0;
        int i = 0;
        while (p < 16384) {
            p = pow2(i);
            Label_0068: {
                if ((access_flags & p) != 0x0) {
                    if (for_class) {
                        if (p == 32) {
                            break Label_0068;
                        }
                        if (p == 512) {
                            break Label_0068;
                        }
                    }
                    buf.append(Constants.ACCESS_NAMES[i]).append(" ");
                }
            }
            ++i;
        }
        return buf.toString().trim();
    }
    
    public static final String classOrInterface(final int access_flags) {
        return ((access_flags & 0x200) != 0x0) ? "interface" : "class";
    }
    
    public static final String codeToString(final byte[] code, final ConstantPool constant_pool, final int index, final int length, final boolean verbose) {
        final StringBuilder buf = new StringBuilder(code.length * 20);
        final ByteSequence stream = new ByteSequence(code);
        try {
            for (int i = 0; i < index; ++i) {
                codeToString(stream, constant_pool, verbose);
            }
            int i = 0;
            while (stream.available() > 0) {
                if (length < 0 || i < length) {
                    final String indices = fillup(stream.getIndex() + ":", 6, true, ' ');
                    buf.append(indices).append(codeToString(stream, constant_pool, verbose)).append('\n');
                }
                ++i;
            }
        }
        catch (IOException e) {
            System.out.println(buf.toString());
            e.printStackTrace();
            throw new ClassFormatException("Byte code error: " + e, e);
        }
        return buf.toString();
    }
    
    public static final String codeToString(final ByteSequence bytes, final ConstantPool constant_pool, final boolean verbose) throws IOException {
        final short opcode = (short)bytes.readUnsignedByte();
        int default_offset = 0;
        int no_pad_bytes = 0;
        final StringBuilder buf = new StringBuilder(Constants.OPCODE_NAMES[opcode]);
        if (opcode == 170 || opcode == 171) {
            final int remainder = bytes.getIndex() % 4;
            no_pad_bytes = ((remainder == 0) ? 0 : (4 - remainder));
            for (int i = 0; i < no_pad_bytes; ++i) {
                final byte b;
                if ((b = bytes.readByte()) != 0) {
                    System.err.println("Warning: Padding byte != 0 in " + Constants.OPCODE_NAMES[opcode] + ":" + b);
                }
            }
            default_offset = bytes.readInt();
        }
        switch (opcode) {
            case 170: {
                final int low = bytes.readInt();
                final int high = bytes.readInt();
                final int offset = bytes.getIndex() - 12 - no_pad_bytes - 1;
                default_offset += offset;
                buf.append("\tdefault = ").append(default_offset).append(", low = ").append(low).append(", high = ").append(high).append("(");
                final int[] jump_table = new int[high - low + 1];
                for (int j = 0; j < jump_table.length; ++j) {
                    buf.append(jump_table[j] = offset + bytes.readInt());
                    if (j < jump_table.length - 1) {
                        buf.append(", ");
                    }
                }
                buf.append(")");
                break;
            }
            case 171: {
                final int npairs = bytes.readInt();
                final int offset = bytes.getIndex() - 8 - no_pad_bytes - 1;
                final int[] match = new int[npairs];
                final int[] jump_table = new int[npairs];
                default_offset += offset;
                buf.append("\tdefault = ").append(default_offset).append(", npairs = ").append(npairs).append(" (");
                for (int j = 0; j < npairs; ++j) {
                    match[j] = bytes.readInt();
                    jump_table[j] = offset + bytes.readInt();
                    buf.append("(").append(match[j]).append(", ").append(jump_table[j]).append(")");
                    if (j < npairs - 1) {
                        buf.append(", ");
                    }
                }
                buf.append(")");
                break;
            }
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166:
            case 167:
            case 168:
            case 198:
            case 199: {
                buf.append("\t\t#").append(bytes.getIndex() - 1 + bytes.readShort());
                break;
            }
            case 200:
            case 201: {
                buf.append("\t\t#").append(bytes.getIndex() - 1 + bytes.readInt());
                break;
            }
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 169: {
                int vindex;
                if (Utility.wide) {
                    vindex = bytes.readUnsignedShort();
                    Utility.wide = false;
                }
                else {
                    vindex = bytes.readUnsignedByte();
                }
                buf.append("\t\t%").append(vindex);
                break;
            }
            case 196: {
                Utility.wide = true;
                buf.append("\t(wide)");
                break;
            }
            case 188: {
                buf.append("\t\t<").append(Constants.TYPE_NAMES[bytes.readByte()]).append(">");
                break;
            }
            case 178:
            case 179:
            case 180:
            case 181: {
                final int index = bytes.readUnsignedShort();
                buf.append("\t\t").append(constant_pool.constantToString(index, (byte)9)).append(verbose ? (" (" + index + ")") : "");
                break;
            }
            case 187:
            case 192: {
                buf.append("\t");
            }
            case 193: {
                final int index = bytes.readUnsignedShort();
                buf.append("\t<").append(constant_pool.constantToString(index, (byte)7)).append(">").append(verbose ? (" (" + index + ")") : "");
                break;
            }
            case 182:
            case 183:
            case 184: {
                final int index = bytes.readUnsignedShort();
                buf.append("\t").append(constant_pool.constantToString(index, (byte)10)).append(verbose ? (" (" + index + ")") : "");
                break;
            }
            case 185: {
                final int index = bytes.readUnsignedShort();
                final int nargs = bytes.readUnsignedByte();
                buf.append("\t").append(constant_pool.constantToString(index, (byte)11)).append(verbose ? (" (" + index + ")\t") : "").append(nargs).append("\t").append(bytes.readUnsignedByte());
                break;
            }
            case 19:
            case 20: {
                final int index = bytes.readUnsignedShort();
                buf.append("\t\t").append(constant_pool.constantToString(index, constant_pool.getConstant(index).getTag())).append(verbose ? (" (" + index + ")") : "");
                break;
            }
            case 18: {
                final int index = bytes.readUnsignedByte();
                buf.append("\t\t").append(constant_pool.constantToString(index, constant_pool.getConstant(index).getTag())).append(verbose ? (" (" + index + ")") : "");
                break;
            }
            case 189: {
                final int index = bytes.readUnsignedShort();
                buf.append("\t\t<").append(compactClassName(constant_pool.getConstantString(index, (byte)7), false)).append(">").append(verbose ? (" (" + index + ")") : "");
                break;
            }
            case 197: {
                final int index = bytes.readUnsignedShort();
                final int dimensions = bytes.readUnsignedByte();
                buf.append("\t<").append(compactClassName(constant_pool.getConstantString(index, (byte)7), false)).append(">\t").append(dimensions).append(verbose ? (" (" + index + ")") : "");
                break;
            }
            case 132: {
                int vindex;
                int constant;
                if (Utility.wide) {
                    vindex = bytes.readUnsignedShort();
                    constant = bytes.readShort();
                    Utility.wide = false;
                }
                else {
                    vindex = bytes.readUnsignedByte();
                    constant = bytes.readByte();
                }
                buf.append("\t\t%").append(vindex).append("\t").append(constant);
                break;
            }
            default: {
                if (Constants.NO_OF_OPERANDS[opcode] > 0) {
                    for (int i = 0; i < Constants.TYPE_OF_OPERANDS[opcode].length; ++i) {
                        buf.append("\t\t");
                        switch (Constants.TYPE_OF_OPERANDS[opcode][i]) {
                            case 8: {
                                buf.append(bytes.readByte());
                                break;
                            }
                            case 9: {
                                buf.append(bytes.readShort());
                                break;
                            }
                            case 10: {
                                buf.append(bytes.readInt());
                                break;
                            }
                            default: {
                                System.err.println("Unreachable default case reached!");
                                System.exit(-1);
                                break;
                            }
                        }
                    }
                    break;
                }
                break;
            }
        }
        return buf.toString();
    }
    
    public static final String compactClassName(final String str) {
        return compactClassName(str, true);
    }
    
    public static final String compactClassName(String str, final String prefix, final boolean chopit) {
        final int len = prefix.length();
        str = str.replace('/', '.');
        if (chopit && str.startsWith(prefix) && str.substring(len).indexOf(46) == -1) {
            str = str.substring(len);
        }
        return str;
    }
    
    public static final String compactClassName(final String str, final boolean chopit) {
        return compactClassName(str, "java.lang.", chopit);
    }
    
    public static final String methodSignatureToString(final String signature, final String name, final String access, final boolean chopit, final LocalVariableTable vars) throws ClassFormatException {
        final StringBuilder buf = new StringBuilder("(");
        int var_index = (access.indexOf("static") < 0) ? 1 : 0;
        String type;
        try {
            if (signature.charAt(0) != '(') {
                throw new ClassFormatException("Invalid method signature: " + signature);
            }
            int index;
            for (index = 1; signature.charAt(index) != ')'; index += unwrap(Utility.consumed_chars)) {
                final String param_type = signatureToString(signature.substring(index), chopit);
                buf.append(param_type);
                if (vars != null) {
                    final LocalVariable l = vars.getLocalVariable(var_index);
                    if (l != null) {
                        buf.append(" ").append(l.getName());
                    }
                }
                else {
                    buf.append(" arg").append(var_index);
                }
                if ("double".equals(param_type) || "long".equals(param_type)) {
                    var_index += 2;
                }
                else {
                    ++var_index;
                }
                buf.append(", ");
            }
            ++index;
            type = signatureToString(signature.substring(index), chopit);
        }
        catch (StringIndexOutOfBoundsException e) {
            throw new ClassFormatException("Invalid method signature: " + signature, e);
        }
        if (buf.length() > 1) {
            buf.setLength(buf.length() - 2);
        }
        buf.append(")");
        return access + ((access.length() > 0) ? " " : "") + type + " " + name + buf.toString();
    }
    
    private static final int pow2(final int n) {
        return 1 << n;
    }
    
    public static final String replace(String str, final String old, final String new_) {
        try {
            if (str.indexOf(old) != -1) {
                final StringBuffer buf = new StringBuffer();
                int old_index;
                int index;
                for (old_index = 0; (index = str.indexOf(old, old_index)) != -1; old_index = index + old.length()) {
                    buf.append(str.substring(old_index, index));
                    buf.append(new_);
                }
                buf.append(str.substring(old_index));
                str = buf.toString();
            }
        }
        catch (StringIndexOutOfBoundsException e) {
            System.err.println(e);
        }
        return str;
    }
    
    public static final String signatureToString(final String signature) {
        return signatureToString(signature, true);
    }
    
    public static final String signatureToString(final String signature, final boolean chopit) {
        wrap(Utility.consumed_chars, 1);
        try {
            switch (signature.charAt(0)) {
                case 'B': {
                    return "byte";
                }
                case 'C': {
                    return "char";
                }
                case 'D': {
                    return "double";
                }
                case 'F': {
                    return "float";
                }
                case 'I': {
                    return "int";
                }
                case 'J': {
                    return "long";
                }
                case 'L': {
                    final int index = signature.indexOf(59);
                    if (index < 0) {
                        throw new ClassFormatException("Invalid signature: " + signature);
                    }
                    wrap(Utility.consumed_chars, index + 1);
                    return compactClassName(signature.substring(1, index), chopit);
                }
                case 'S': {
                    return "short";
                }
                case 'Z': {
                    return "boolean";
                }
                case '[': {
                    final StringBuilder brackets = new StringBuilder();
                    int n;
                    for (n = 0; signature.charAt(n) == '['; ++n) {
                        brackets.append("[]");
                    }
                    final int consumed_chars = n;
                    final String type = signatureToString(signature.substring(n), chopit);
                    final int _temp = unwrap(Utility.consumed_chars) + consumed_chars;
                    wrap(Utility.consumed_chars, _temp);
                    return type + brackets.toString();
                }
                case 'V': {
                    return "void";
                }
                default: {
                    throw new ClassFormatException("Invalid signature: `" + signature + "'");
                }
            }
        }
        catch (StringIndexOutOfBoundsException e) {
            throw new ClassFormatException("Invalid signature: " + signature, e);
        }
    }
    
    private static final short byteToShort(final byte b) {
        return (b < 0) ? ((short)(256 + b)) : b;
    }
    
    public static final String toHexString(final byte[] bytes) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            final short b = byteToShort(bytes[i]);
            final String hex = Integer.toString(b, 16);
            if (b < 16) {
                buf.append('0');
            }
            buf.append(hex);
            if (i < bytes.length - 1) {
                buf.append(' ');
            }
        }
        return buf.toString();
    }
    
    public static final String fillup(final String str, final int length, final boolean left_justify, final char fill) {
        final int len = length - str.length();
        final char[] buf = new char[(len < 0) ? 0 : len];
        for (int j = 0; j < buf.length; ++j) {
            buf[j] = fill;
        }
        if (left_justify) {
            return str + new String(buf);
        }
        return new String(buf) + str;
    }
    
    public static final String convertString(final String label) {
        final char[] ch = label.toCharArray();
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < ch.length; ++i) {
            switch (ch[i]) {
                case '\n': {
                    buf.append("\\n");
                    break;
                }
                case '\r': {
                    buf.append("\\r");
                    break;
                }
                case '\"': {
                    buf.append("\\\"");
                    break;
                }
                case '\'': {
                    buf.append("\\'");
                    break;
                }
                case '\\': {
                    buf.append("\\\\");
                    break;
                }
                default: {
                    buf.append(ch[i]);
                    break;
                }
            }
        }
        return buf.toString();
    }
    
    static {
        Utility.consumed_chars = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
        Utility.wide = false;
        Utility.CHAR_MAP = new int[48];
        Utility.MAP_CHAR = new int[256];
        int j = 0;
        for (int i = 65; i <= 90; ++i) {
            Utility.CHAR_MAP[j] = i;
            Utility.MAP_CHAR[i] = j;
            ++j;
        }
        for (int i = 103; i <= 122; ++i) {
            Utility.CHAR_MAP[j] = i;
            Utility.MAP_CHAR[i] = j;
            ++j;
        }
        Utility.CHAR_MAP[j] = 36;
        Utility.MAP_CHAR[36] = j;
        ++j;
        Utility.CHAR_MAP[j] = 95;
        Utility.MAP_CHAR[95] = j;
    }
}
