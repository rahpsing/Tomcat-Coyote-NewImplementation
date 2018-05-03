// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.bcel;

public interface Constants
{
    public static final short ACC_FINAL = 16;
    public static final short ACC_INTERFACE = 512;
    public static final short ACC_ABSTRACT = 1024;
    public static final short ACC_ANNOTATION = 8192;
    public static final short ACC_ENUM = 16384;
    public static final short ACC_SUPER = 32;
    public static final short MAX_ACC_FLAG = 16384;
    public static final String[] ACCESS_NAMES = { "public", "private", "protected", "static", "final", "synchronized", "volatile", "transient", "native", "interface", "abstract", "strictfp", "synthetic", "annotation", "enum" };
    public static final byte CONSTANT_Utf8 = 1;
    public static final byte CONSTANT_Integer = 3;
    public static final byte CONSTANT_Float = 4;
    public static final byte CONSTANT_Long = 5;
    public static final byte CONSTANT_Double = 6;
    public static final byte CONSTANT_Class = 7;
    public static final byte CONSTANT_Fieldref = 9;
    public static final byte CONSTANT_String = 8;
    public static final byte CONSTANT_Methodref = 10;
    public static final byte CONSTANT_InterfaceMethodref = 11;
    public static final byte CONSTANT_NameAndType = 12;
    public static final byte CONSTANT_MethodHandle = 15;
    public static final byte CONSTANT_MethodType = 16;
    public static final byte CONSTANT_InvokeDynamic = 18;
    public static final String[] CONSTANT_NAMES = { "", "CONSTANT_Utf8", "", "CONSTANT_Integer", "CONSTANT_Float", "CONSTANT_Long", "CONSTANT_Double", "CONSTANT_Class", "CONSTANT_String", "CONSTANT_Fieldref", "CONSTANT_Methodref", "CONSTANT_InterfaceMethodref", "CONSTANT_NameAndType", "CONSTANT_MethodHandle", "CONSTANT_MethodType", "CONSTANT_InvokeDynamic" };
    public static final short LDC = 18;
    public static final short LDC_W = 19;
    public static final short LDC2_W = 20;
    public static final short ILOAD = 21;
    public static final short LLOAD = 22;
    public static final short FLOAD = 23;
    public static final short DLOAD = 24;
    public static final short ALOAD = 25;
    public static final short ISTORE = 54;
    public static final short LSTORE = 55;
    public static final short FSTORE = 56;
    public static final short DSTORE = 57;
    public static final short ASTORE = 58;
    public static final short IINC = 132;
    public static final short IFEQ = 153;
    public static final short IFNE = 154;
    public static final short IFLT = 155;
    public static final short IFGE = 156;
    public static final short IFGT = 157;
    public static final short IFLE = 158;
    public static final short IF_ICMPEQ = 159;
    public static final short IF_ICMPNE = 160;
    public static final short IF_ICMPLT = 161;
    public static final short IF_ICMPGE = 162;
    public static final short IF_ICMPGT = 163;
    public static final short IF_ICMPLE = 164;
    public static final short IF_ACMPEQ = 165;
    public static final short IF_ACMPNE = 166;
    public static final short GOTO = 167;
    public static final short JSR = 168;
    public static final short RET = 169;
    public static final short TABLESWITCH = 170;
    public static final short LOOKUPSWITCH = 171;
    public static final short GETSTATIC = 178;
    public static final short PUTSTATIC = 179;
    public static final short GETFIELD = 180;
    public static final short PUTFIELD = 181;
    public static final short INVOKEVIRTUAL = 182;
    public static final short INVOKESPECIAL = 183;
    public static final short INVOKESTATIC = 184;
    public static final short INVOKEINTERFACE = 185;
    public static final short NEW = 187;
    public static final short NEWARRAY = 188;
    public static final short ANEWARRAY = 189;
    public static final short CHECKCAST = 192;
    public static final short INSTANCEOF = 193;
    public static final short WIDE = 196;
    public static final short MULTIANEWARRAY = 197;
    public static final short IFNULL = 198;
    public static final short IFNONNULL = 199;
    public static final short GOTO_W = 200;
    public static final short JSR_W = 201;
    public static final short UNDEFINED = -1;
    public static final short UNPREDICTABLE = -2;
    public static final short RESERVED = -3;
    public static final String ILLEGAL_OPCODE = "<illegal opcode>";
    public static final String ILLEGAL_TYPE = "<illegal type>";
    public static final byte T_BYTE = 8;
    public static final byte T_SHORT = 9;
    public static final byte T_INT = 10;
    public static final byte T_UNKNOWN = 15;
    public static final String[] TYPE_NAMES = { "<illegal type>", "<illegal type>", "<illegal type>", "<illegal type>", "boolean", "char", "float", "double", "byte", "short", "int", "long", "void", "array", "object", "unknown", "address" };
    public static final short[] NO_OF_OPERANDS = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, -2, -2, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 4, -1, 2, 1, 2, 0, 0, 2, 2, 0, 0, -2, 3, 2, 2, 4, 4, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -3, -3 };
    public static final short[][] TYPE_OF_OPERANDS = { new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], { 8 }, { 9 }, { 8 }, { 9 }, { 9 }, { 8 }, { 8 }, { 8 }, { 8 }, { 8 }, new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], { 8 }, { 8 }, { 8 }, { 8 }, { 8 }, new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], { 8, 8 }, new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 8 }, new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9 }, { 9, 8, 8 }, new short[0], { 9 }, { 8 }, { 9 }, new short[0], new short[0], { 9 }, { 9 }, new short[0], new short[0], { 8 }, { 9, 8 }, { 9 }, { 9 }, { 10 }, { 10 }, new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0], new short[0] };
    public static final String[] OPCODE_NAMES = { "nop", "aconst_null", "iconst_m1", "iconst_0", "iconst_1", "iconst_2", "iconst_3", "iconst_4", "iconst_5", "lconst_0", "lconst_1", "fconst_0", "fconst_1", "fconst_2", "dconst_0", "dconst_1", "bipush", "sipush", "ldc", "ldc_w", "ldc2_w", "iload", "lload", "fload", "dload", "aload", "iload_0", "iload_1", "iload_2", "iload_3", "lload_0", "lload_1", "lload_2", "lload_3", "fload_0", "fload_1", "fload_2", "fload_3", "dload_0", "dload_1", "dload_2", "dload_3", "aload_0", "aload_1", "aload_2", "aload_3", "iaload", "laload", "faload", "daload", "aaload", "baload", "caload", "saload", "istore", "lstore", "fstore", "dstore", "astore", "istore_0", "istore_1", "istore_2", "istore_3", "lstore_0", "lstore_1", "lstore_2", "lstore_3", "fstore_0", "fstore_1", "fstore_2", "fstore_3", "dstore_0", "dstore_1", "dstore_2", "dstore_3", "astore_0", "astore_1", "astore_2", "astore_3", "iastore", "lastore", "fastore", "dastore", "aastore", "bastore", "castore", "sastore", "pop", "pop2", "dup", "dup_x1", "dup_x2", "dup2", "dup2_x1", "dup2_x2", "swap", "iadd", "ladd", "fadd", "dadd", "isub", "lsub", "fsub", "dsub", "imul", "lmul", "fmul", "dmul", "idiv", "ldiv", "fdiv", "ddiv", "irem", "lrem", "frem", "drem", "ineg", "lneg", "fneg", "dneg", "ishl", "lshl", "ishr", "lshr", "iushr", "lushr", "iand", "land", "ior", "lor", "ixor", "lxor", "iinc", "i2l", "i2f", "i2d", "l2i", "l2f", "l2d", "f2i", "f2l", "f2d", "d2i", "d2l", "d2f", "i2b", "i2c", "i2s", "lcmp", "fcmpl", "fcmpg", "dcmpl", "dcmpg", "ifeq", "ifne", "iflt", "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne", "if_icmplt", "if_icmpge", "if_icmpgt", "if_icmple", "if_acmpeq", "if_acmpne", "goto", "jsr", "ret", "tableswitch", "lookupswitch", "ireturn", "lreturn", "freturn", "dreturn", "areturn", "return", "getstatic", "putstatic", "getfield", "putfield", "invokevirtual", "invokespecial", "invokestatic", "invokeinterface", "<illegal opcode>", "new", "newarray", "anewarray", "arraylength", "athrow", "checkcast", "instanceof", "monitorenter", "monitorexit", "wide", "multianewarray", "ifnull", "ifnonnull", "goto_w", "jsr_w", "breakpoint", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "<illegal opcode>", "impdep1", "impdep2" };
    public static final byte ATTR_UNKNOWN = -1;
    public static final byte ATTR_SOURCE_FILE = 0;
    public static final byte ATTR_CONSTANT_VALUE = 1;
    public static final byte ATTR_CODE = 2;
    public static final byte ATTR_EXCEPTIONS = 3;
    public static final byte ATTR_LINE_NUMBER_TABLE = 4;
    public static final byte ATTR_LOCAL_VARIABLE_TABLE = 5;
    public static final byte ATTR_INNER_CLASSES = 6;
    public static final byte ATTR_SYNTHETIC = 7;
    public static final byte ATTR_DEPRECATED = 8;
    public static final byte ATTR_PMG = 9;
    public static final byte ATTR_SIGNATURE = 10;
    public static final byte ATTR_STACK_MAP = 11;
    public static final byte ATTR_RUNTIME_VISIBLE_ANNOTATIONS = 12;
    public static final byte ATTR_RUNTIMEIN_VISIBLE_ANNOTATIONS = 13;
    public static final byte ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = 14;
    public static final byte ATTR_RUNTIMEIN_VISIBLE_PARAMETER_ANNOTATIONS = 15;
    public static final byte ATTR_ANNOTATION_DEFAULT = 16;
    public static final byte ATTR_LOCAL_VARIABLE_TYPE_TABLE = 17;
    public static final byte ATTR_ENCLOSING_METHOD = 18;
    public static final byte ATTR_STACK_MAP_TABLE = 19;
    public static final short KNOWN_ATTRIBUTES = 20;
    public static final String[] ATTRIBUTE_NAMES = { "SourceFile", "ConstantValue", "Code", "Exceptions", "LineNumberTable", "LocalVariableTable", "InnerClasses", "Synthetic", "Deprecated", "PMGClass", "Signature", "StackMap", "RuntimeVisibleAnnotations", "RuntimeInvisibleAnnotations", "RuntimeVisibleParameterAnnotations", "RuntimeInvisibleParameterAnnotations", "AnnotationDefault", "LocalVariableTypeTable", "EnclosingMethod", "StackMapTable" };
    public static final byte ITEM_Bogus = 0;
    public static final byte ITEM_Object = 7;
    public static final byte ITEM_NewObject = 8;
    public static final String[] ITEM_NAMES = { "Bogus", "Integer", "Float", "Double", "Long", "Null", "InitObject", "Object", "NewObject" };
    public static final int SAME_FRAME = 0;
    public static final int SAME_LOCALS_1_STACK_ITEM_FRAME = 64;
    public static final int SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED = 247;
    public static final int CHOP_FRAME = 248;
    public static final int SAME_FRAME_EXTENDED = 251;
    public static final int APPEND_FRAME = 252;
    public static final int FULL_FRAME = 255;
    public static final int SAME_FRAME_MAX = 63;
    public static final int SAME_LOCALS_1_STACK_ITEM_FRAME_MAX = 127;
    public static final int CHOP_FRAME_MAX = 250;
    public static final int APPEND_FRAME_MAX = 254;
}
