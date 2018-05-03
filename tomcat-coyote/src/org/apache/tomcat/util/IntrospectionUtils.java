// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util;

import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.Vector;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.File;
import java.util.StringTokenizer;
import java.net.URL;
import java.lang.reflect.Method;
import java.util.Hashtable;
import org.apache.juli.logging.Log;

public final class IntrospectionUtils
{
    private static final Log log;
    @Deprecated
    public static final String PATH_SEPARATOR;
    static Hashtable<Class<?>, Method[]> objectMethods;
    @Deprecated
    private static final Object[] emptyArray;
    
    @Deprecated
    public static void execute(final Object proxy, final String method) throws Exception {
        Method executeM = null;
        final Class<?> c = proxy.getClass();
        final Class<?>[] params = (Class<?>[])new Class[0];
        executeM = findMethod(c, method, params);
        if (executeM == null) {
            throw new RuntimeException("No execute in " + proxy.getClass());
        }
        executeM.invoke(proxy, (Object[])null);
    }
    
    @Deprecated
    public static void setAttribute(final Object proxy, final String n, final Object v) throws Exception {
        if (proxy instanceof AttributeHolder) {
            ((AttributeHolder)proxy).setAttribute(n, v);
            return;
        }
        Method executeM = null;
        final Class<?> c = proxy.getClass();
        final Class<?>[] params = (Class<?>[])new Class[] { String.class, Object.class };
        executeM = findMethod(c, "setAttribute", params);
        if (executeM == null) {
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("No setAttribute in " + proxy.getClass()));
            }
            return;
        }
        if (IntrospectionUtils.log.isDebugEnabled()) {
            IntrospectionUtils.log.debug((Object)("Setting " + n + "=" + v + "  in " + proxy));
        }
        executeM.invoke(proxy, n, v);
    }
    
    @Deprecated
    public static Object getAttribute(final Object proxy, final String n) throws Exception {
        Method executeM = null;
        final Class<?> c = proxy.getClass();
        final Class<?>[] params = (Class<?>[])new Class[] { String.class };
        executeM = findMethod(c, "getAttribute", params);
        if (executeM == null) {
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("No getAttribute in " + proxy.getClass()));
            }
            return null;
        }
        return executeM.invoke(proxy, n);
    }
    
    @Deprecated
    public static ClassLoader getURLClassLoader(final URL[] urls, final ClassLoader parent) {
        try {
            final Class<?> urlCL = Class.forName("java.net.URLClassLoader");
            final Class<?>[] paramT = (Class<?>[])new Class[] { urls.getClass(), ClassLoader.class };
            final Method m = findMethod(urlCL, "newInstance", paramT);
            if (m == null) {
                return null;
            }
            final ClassLoader cl = (ClassLoader)m.invoke(urlCL, urls, parent);
            return cl;
        }
        catch (ClassNotFoundException ex2) {
            return null;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    @Deprecated
    public static String guessInstall(final String installSysProp, final String homeSysProp, final String jarName) {
        return guessInstall(installSysProp, homeSysProp, jarName, null);
    }
    
    @Deprecated
    public static String guessInstall(final String installSysProp, final String homeSysProp, final String jarName, final String classFile) {
        String install = null;
        String home = null;
        if (installSysProp != null) {
            install = System.getProperty(installSysProp);
        }
        if (homeSysProp != null) {
            home = System.getProperty(homeSysProp);
        }
        if (install != null) {
            if (home == null) {
                ((Hashtable<String, String>)System.getProperties()).put(homeSysProp, install);
            }
            return install;
        }
        final String cpath = System.getProperty("java.class.path");
        final String pathSep = System.getProperty("path.separator");
        final StringTokenizer st = new StringTokenizer(cpath, pathSep);
        while (st.hasMoreTokens()) {
            final String path = st.nextToken();
            if (path.endsWith(jarName)) {
                home = path.substring(0, path.length() - jarName.length());
                try {
                    if ("".equals(home)) {
                        home = new File("./").getCanonicalPath();
                    }
                    else if (home.endsWith(File.separator)) {
                        home = home.substring(0, home.length() - 1);
                    }
                    final File f = new File(home);
                    String parentDir = f.getParent();
                    if (parentDir == null) {
                        parentDir = home;
                    }
                    final File f2 = new File(parentDir);
                    install = f2.getCanonicalPath();
                    if (installSysProp != null) {
                        ((Hashtable<String, String>)System.getProperties()).put(installSysProp, install);
                    }
                    if (home == null && homeSysProp != null) {
                        ((Hashtable<String, String>)System.getProperties()).put(homeSysProp, install);
                    }
                    return install;
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }
            }
            final String fname = path + (path.endsWith("/") ? "" : "/") + classFile;
            if (new File(fname).exists()) {
                try {
                    final File f3 = new File(path);
                    String parentDir2 = f3.getParent();
                    if (parentDir2 == null) {
                        parentDir2 = path;
                    }
                    final File f4 = new File(parentDir2);
                    install = f4.getCanonicalPath();
                    if (installSysProp != null) {
                        ((Hashtable<String, String>)System.getProperties()).put(installSysProp, install);
                    }
                    if (home == null && homeSysProp != null) {
                        ((Hashtable<String, String>)System.getProperties()).put(homeSysProp, install);
                    }
                    return install;
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        }
        if (home != null) {
            ((Hashtable<String, String>)System.getProperties()).put(installSysProp, home);
            return home;
        }
        return null;
    }
    
    @Deprecated
    public static void displayClassPath(final String msg, final URL[] cp) {
        if (IntrospectionUtils.log.isDebugEnabled()) {
            IntrospectionUtils.log.debug((Object)msg);
            for (int i = 0; i < cp.length; ++i) {
                IntrospectionUtils.log.debug((Object)cp[i].getFile());
            }
        }
    }
    
    @Deprecated
    public static String classPathAdd(final URL[] urls, String cp) {
        if (urls == null) {
            return cp;
        }
        for (int i = 0; i < urls.length; ++i) {
            if (cp != null) {
                cp = cp + IntrospectionUtils.PATH_SEPARATOR + urls[i].getFile();
            }
            else {
                cp = urls[i].getFile();
            }
        }
        return cp;
    }
    
    public static boolean setProperty(final Object o, final String name, final String value) {
        return setProperty(o, name, value, true);
    }
    
    public static boolean setProperty(final Object o, final String name, final String value, final boolean invokeSetProperty) {
        if (IntrospectionUtils.log.isDebugEnabled()) {
            IntrospectionUtils.log.debug((Object)("IntrospectionUtils: setProperty(" + o.getClass() + " " + name + "=" + value + ")"));
        }
        final String setter = "set" + capitalize(name);
        try {
            final Method[] methods = findMethods(o.getClass());
            Method setPropertyMethodVoid = null;
            Method setPropertyMethodBool = null;
            for (int i = 0; i < methods.length; ++i) {
                final Class<?>[] paramT = methods[i].getParameterTypes();
                if (setter.equals(methods[i].getName()) && paramT.length == 1 && "java.lang.String".equals(paramT[0].getName())) {
                    methods[i].invoke(o, value);
                    return true;
                }
            }
            for (int i = 0; i < methods.length; ++i) {
                boolean ok = true;
                if (setter.equals(methods[i].getName()) && methods[i].getParameterTypes().length == 1) {
                    final Class<?> paramType = methods[i].getParameterTypes()[0];
                    final Object[] params = { null };
                    Label_0496: {
                        if (!"java.lang.Integer".equals(paramType.getName())) {
                            if (!"int".equals(paramType.getName())) {
                                if (!"java.lang.Long".equals(paramType.getName())) {
                                    if (!"long".equals(paramType.getName())) {
                                        if ("java.lang.Boolean".equals(paramType.getName()) || "boolean".equals(paramType.getName())) {
                                            params[0] = Boolean.valueOf(value);
                                            break Label_0496;
                                        }
                                        if ("java.net.InetAddress".equals(paramType.getName())) {
                                            try {
                                                params[0] = InetAddress.getByName(value);
                                            }
                                            catch (UnknownHostException exc) {
                                                if (IntrospectionUtils.log.isDebugEnabled()) {
                                                    IntrospectionUtils.log.debug((Object)("IntrospectionUtils: Unable to resolve host name:" + value));
                                                }
                                                ok = false;
                                            }
                                            break Label_0496;
                                        }
                                        if (IntrospectionUtils.log.isDebugEnabled()) {
                                            IntrospectionUtils.log.debug((Object)("IntrospectionUtils: Unknown type " + paramType.getName()));
                                        }
                                        break Label_0496;
                                    }
                                }
                                try {
                                    params[0] = new Long(value);
                                }
                                catch (NumberFormatException ex4) {
                                    ok = false;
                                }
                                break Label_0496;
                            }
                        }
                        try {
                            params[0] = new Integer(value);
                        }
                        catch (NumberFormatException ex4) {
                            ok = false;
                        }
                    }
                    if (ok) {
                        methods[i].invoke(o, params);
                        return true;
                    }
                }
                if ("setProperty".equals(methods[i].getName())) {
                    if (methods[i].getReturnType() == Boolean.TYPE) {
                        setPropertyMethodBool = methods[i];
                    }
                    else {
                        setPropertyMethodVoid = methods[i];
                    }
                }
            }
            if (invokeSetProperty && (setPropertyMethodBool != null || setPropertyMethodVoid != null)) {
                final Object[] params2 = { name, value };
                if (setPropertyMethodBool != null) {
                    try {
                        return (boolean)setPropertyMethodBool.invoke(o, params2);
                    }
                    catch (IllegalArgumentException biae) {
                        if (setPropertyMethodVoid != null) {
                            setPropertyMethodVoid.invoke(o, params2);
                            return true;
                        }
                        throw biae;
                    }
                }
                setPropertyMethodVoid.invoke(o, params2);
                return true;
            }
        }
        catch (IllegalArgumentException ex2) {
            IntrospectionUtils.log.warn((Object)("IAE " + o + " " + name + " " + value), (Throwable)ex2);
        }
        catch (SecurityException ex3) {
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils: SecurityException for " + o.getClass() + " " + name + "=" + value + ")"), (Throwable)ex3);
            }
        }
        catch (IllegalAccessException iae) {
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils: IllegalAccessException for " + o.getClass() + " " + name + "=" + value + ")"), (Throwable)iae);
            }
        }
        catch (InvocationTargetException ie) {
            ExceptionUtils.handleThrowable(ie.getCause());
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils: InvocationTargetException for " + o.getClass() + " " + name + "=" + value + ")"), (Throwable)ie);
            }
        }
        return false;
    }
    
    public static Object getProperty(final Object o, final String name) {
        final String getter = "get" + capitalize(name);
        final String isGetter = "is" + capitalize(name);
        try {
            final Method[] methods = findMethods(o.getClass());
            Method getPropertyMethod = null;
            for (int i = 0; i < methods.length; ++i) {
                final Class<?>[] paramT = methods[i].getParameterTypes();
                if (getter.equals(methods[i].getName()) && paramT.length == 0) {
                    return methods[i].invoke(o, (Object[])null);
                }
                if (isGetter.equals(methods[i].getName()) && paramT.length == 0) {
                    return methods[i].invoke(o, (Object[])null);
                }
                if ("getProperty".equals(methods[i].getName())) {
                    getPropertyMethod = methods[i];
                }
            }
            if (getPropertyMethod != null) {
                final Object[] params = { name };
                return getPropertyMethod.invoke(o, params);
            }
        }
        catch (IllegalArgumentException ex2) {
            IntrospectionUtils.log.warn((Object)("IAE " + o + " " + name), (Throwable)ex2);
        }
        catch (SecurityException ex3) {
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils: SecurityException for " + o.getClass() + " " + name + ")"), (Throwable)ex3);
            }
        }
        catch (IllegalAccessException iae) {
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils: IllegalAccessException for " + o.getClass() + " " + name + ")"), (Throwable)iae);
            }
        }
        catch (InvocationTargetException ie) {
            ExceptionUtils.handleThrowable(ie.getCause());
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils: InvocationTargetException for " + o.getClass() + " " + name + ")"));
            }
        }
        return null;
    }
    
    @Deprecated
    public static void setProperty(final Object o, final String name) {
        final String setter = "set" + capitalize(name);
        try {
            final Method[] methods = findMethods(o.getClass());
            for (int i = 0; i < methods.length; ++i) {
                final Class<?>[] paramT = methods[i].getParameterTypes();
                if (setter.equals(methods[i].getName()) && paramT.length == 0) {
                    methods[i].invoke(o, new Object[0]);
                    return;
                }
            }
        }
        catch (Exception ex1) {
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils: Exception for " + o.getClass() + " " + name), (Throwable)ex1);
            }
        }
    }
    
    public static String replaceProperties(final String value, final Hashtable<Object, Object> staticProp, final PropertySource[] dynamicProp) {
        if (value.indexOf("$") < 0) {
            return value;
        }
        final StringBuilder sb = new StringBuilder();
        int prev = 0;
        int pos;
        while ((pos = value.indexOf("$", prev)) >= 0) {
            if (pos > 0) {
                sb.append(value.substring(prev, pos));
            }
            if (pos == value.length() - 1) {
                sb.append('$');
                prev = pos + 1;
            }
            else if (value.charAt(pos + 1) != '{') {
                sb.append('$');
                prev = pos + 1;
            }
            else {
                final int endName = value.indexOf(125, pos);
                if (endName < 0) {
                    sb.append(value.substring(pos));
                    prev = value.length();
                }
                else {
                    final String n = value.substring(pos + 2, endName);
                    String v = null;
                    if (staticProp != null) {
                        v = staticProp.get(n);
                    }
                    if (v == null && dynamicProp != null) {
                        for (int i = 0; i < dynamicProp.length; ++i) {
                            v = dynamicProp[i].getProperty(n);
                            if (v != null) {
                                break;
                            }
                        }
                    }
                    if (v == null) {
                        v = "${" + n + "}";
                    }
                    sb.append(v);
                    prev = endName + 1;
                }
            }
        }
        if (prev < value.length()) {
            sb.append(value.substring(prev));
        }
        return sb.toString();
    }
    
    public static String capitalize(final String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        final char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    
    @Deprecated
    public static String unCapitalize(final String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        final char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
    
    @Deprecated
    public static void addToClassPath(final Vector<URL> cpV, final String dir) {
        try {
            final String[] cpComp = getFilesByExt(dir, ".jar");
            if (cpComp != null) {
                for (int jarCount = cpComp.length, i = 0; i < jarCount; ++i) {
                    final URL url = getURL(dir, cpComp[i]);
                    if (url != null) {
                        cpV.addElement(url);
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Deprecated
    public static void addToolsJar(final Vector<URL> v) {
        try {
            File f = new File(System.getProperty("java.home") + "/../lib/tools.jar");
            if (!f.exists()) {
                f = new File(System.getProperty("java.home") + "/lib/tools.jar");
                if (f.exists() && IntrospectionUtils.log.isDebugEnabled()) {
                    IntrospectionUtils.log.debug((Object)("Detected strange java.home value " + System.getProperty("java.home") + ", it should point to jre"));
                }
            }
            final URL url = new URL("file", "", f.getAbsolutePath());
            v.addElement(url);
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    
    @Deprecated
    public static String[] getFilesByExt(final String ld, final String ext) {
        final File dir = new File(ld);
        String[] names = null;
        if (dir.isDirectory()) {
            names = dir.list(new FilenameFilter() {
                @Override
                public boolean accept(final File d, final String name) {
                    return name.endsWith(ext);
                }
            });
        }
        return names;
    }
    
    @Deprecated
    public static URL getURL(final String base, final String file) {
        try {
            final File baseF = new File(base);
            final File f = new File(baseF, file);
            String path = f.getCanonicalPath();
            if (f.isDirectory()) {
                path += "/";
            }
            if (!f.exists()) {
                return null;
            }
            return new URL("file", "", path);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    @Deprecated
    public static void addJarsFromClassPath(final Vector<URL> jars, final String cp) throws IOException, MalformedURLException {
        final String sep = System.getProperty("path.separator");
        if (cp != null) {
            final StringTokenizer st = new StringTokenizer(cp, sep);
            while (st.hasMoreTokens()) {
                final File f = new File(st.nextToken());
                String path = f.getCanonicalPath();
                if (f.isDirectory()) {
                    path += "/";
                }
                final URL url = new URL("file", "", path);
                if (!jars.contains(url)) {
                    jars.addElement(url);
                }
            }
        }
    }
    
    @Deprecated
    public static URL[] getClassPath(final Vector<URL> v) {
        final URL[] urls = new URL[v.size()];
        for (int i = 0; i < v.size(); ++i) {
            urls[i] = v.elementAt(i);
        }
        return urls;
    }
    
    @Deprecated
    public static URL[] getClassPath(final String dir, final String cpath, final String cpathProp, final boolean addTools) throws IOException, MalformedURLException {
        final Vector<URL> jarsV = new Vector<URL>();
        if (dir != null) {
            final URL url = getURL(dir, "classes");
            if (url != null) {
                jarsV.addElement(url);
            }
            addToClassPath(jarsV, dir);
        }
        if (cpath != null) {
            addJarsFromClassPath(jarsV, cpath);
        }
        if (cpathProp != null) {
            final String cpath2 = System.getProperty(cpathProp);
            addJarsFromClassPath(jarsV, cpath2);
        }
        if (addTools) {
            addToolsJar(jarsV);
        }
        return getClassPath(jarsV);
    }
    
    public static void clear() {
        IntrospectionUtils.objectMethods.clear();
    }
    
    public static Method[] findMethods(final Class<?> c) {
        Method[] methods = IntrospectionUtils.objectMethods.get(c);
        if (methods != null) {
            return methods;
        }
        methods = c.getMethods();
        IntrospectionUtils.objectMethods.put(c, methods);
        return methods;
    }
    
    public static Method findMethod(final Class<?> c, final String name, final Class<?>[] params) {
        final Method[] methods = findMethods(c);
        if (methods == null) {
            return null;
        }
        for (int i = 0; i < methods.length; ++i) {
            if (methods[i].getName().equals(name)) {
                final Class<?>[] methodParams = methods[i].getParameterTypes();
                if (methodParams == null && (params == null || params.length == 0)) {
                    return methods[i];
                }
                if (params == null && (methodParams == null || methodParams.length == 0)) {
                    return methods[i];
                }
                if (params.length == methodParams.length) {
                    boolean found = true;
                    for (int j = 0; j < params.length; ++j) {
                        if (params[j] != methodParams[j]) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        return methods[i];
                    }
                }
            }
        }
        return null;
    }
    
    @Deprecated
    public static boolean hasHook(final Object obj, final String methodN) {
        try {
            final Method[] myMethods = findMethods(obj.getClass());
            for (int i = 0; i < myMethods.length; ++i) {
                if (methodN.equals(myMethods[i].getName())) {
                    final Class<?> declaring = myMethods[i].getDeclaringClass();
                    final Class<?> parentOfDeclaring = declaring.getSuperclass();
                    if (!"java.lang.Object".equals(parentOfDeclaring.getName())) {
                        return true;
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    @Deprecated
    public static void callMain(final Class<?> c, final String[] args) throws Exception {
        final Class<?>[] p = (Class<?>[])new Class[] { args.getClass() };
        final Method m = c.getMethod("main", p);
        m.invoke(c, args);
    }
    
    public static Object callMethod1(final Object target, final String methodN, final Object param1, final String typeParam1, final ClassLoader cl) throws Exception {
        if ((target == null || param1 == null) && IntrospectionUtils.log.isDebugEnabled()) {
            IntrospectionUtils.log.debug((Object)("IntrospectionUtils: Assert: Illegal params " + target + " " + param1));
        }
        if (IntrospectionUtils.log.isDebugEnabled()) {
            IntrospectionUtils.log.debug((Object)("IntrospectionUtils: callMethod1 " + target.getClass().getName() + " " + param1.getClass().getName() + " " + typeParam1));
        }
        final Class<?>[] params = (Class<?>[])new Class[] { null };
        if (typeParam1 == null) {
            params[0] = param1.getClass();
        }
        else {
            params[0] = cl.loadClass(typeParam1);
        }
        final Method m = findMethod(target.getClass(), methodN, params);
        if (m == null) {
            throw new NoSuchMethodException(target.getClass().getName() + " " + methodN);
        }
        try {
            return m.invoke(target, param1);
        }
        catch (InvocationTargetException ie) {
            ExceptionUtils.handleThrowable(ie.getCause());
            throw ie;
        }
    }
    
    @Deprecated
    public static Object callMethod0(final Object target, final String methodN) throws Exception {
        if (target == null) {
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils: Assert: Illegal params " + target));
            }
            return null;
        }
        if (IntrospectionUtils.log.isDebugEnabled()) {
            IntrospectionUtils.log.debug((Object)("IntrospectionUtils: callMethod0 " + target.getClass().getName() + "." + methodN));
        }
        final Class<?>[] params = (Class<?>[])new Class[0];
        final Method m = findMethod(target.getClass(), methodN, params);
        if (m == null) {
            throw new NoSuchMethodException(target.getClass().getName() + " " + methodN);
        }
        try {
            return m.invoke(target, IntrospectionUtils.emptyArray);
        }
        catch (InvocationTargetException ie) {
            ExceptionUtils.handleThrowable(ie.getCause());
            throw ie;
        }
    }
    
    public static Object callMethodN(final Object target, final String methodN, final Object[] params, final Class<?>[] typeParams) throws Exception {
        Method m = null;
        m = findMethod(target.getClass(), methodN, typeParams);
        if (m == null) {
            if (IntrospectionUtils.log.isDebugEnabled()) {
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils: Can't find method " + methodN + " in " + target + " CLASS " + target.getClass()));
            }
            return null;
        }
        try {
            final Object o = m.invoke(target, params);
            if (IntrospectionUtils.log.isDebugEnabled()) {
                final StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getName()).append('.').append(methodN).append("( ");
                for (int i = 0; i < params.length; ++i) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(params[i]);
                }
                sb.append(")");
                IntrospectionUtils.log.debug((Object)("IntrospectionUtils:" + sb.toString()));
            }
            return o;
        }
        catch (InvocationTargetException ie) {
            ExceptionUtils.handleThrowable(ie.getCause());
            throw ie;
        }
    }
    
    public static Object convert(final String object, final Class<?> paramType) {
        Object result = null;
        Label_0194: {
            if ("java.lang.String".equals(paramType.getName())) {
                result = object;
            }
            else {
                if (!"java.lang.Integer".equals(paramType.getName())) {
                    if (!"int".equals(paramType.getName())) {
                        if ("java.lang.Boolean".equals(paramType.getName()) || "boolean".equals(paramType.getName())) {
                            result = Boolean.valueOf(object);
                            break Label_0194;
                        }
                        if ("java.net.InetAddress".equals(paramType.getName())) {
                            try {
                                result = InetAddress.getByName(object);
                            }
                            catch (UnknownHostException exc) {
                                if (IntrospectionUtils.log.isDebugEnabled()) {
                                    IntrospectionUtils.log.debug((Object)("IntrospectionUtils: Unable to resolve host name:" + object));
                                }
                            }
                            break Label_0194;
                        }
                        if (IntrospectionUtils.log.isDebugEnabled()) {
                            IntrospectionUtils.log.debug((Object)("IntrospectionUtils: Unknown type " + paramType.getName()));
                        }
                        break Label_0194;
                    }
                }
                try {
                    result = new Integer(object);
                }
                catch (NumberFormatException ex) {}
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("Can't convert argument: " + object);
        }
        return result;
    }
    
    static {
        log = LogFactory.getLog((Class)IntrospectionUtils.class);
        PATH_SEPARATOR = System.getProperty("path.separator");
        IntrospectionUtils.objectMethods = new Hashtable<Class<?>, Method[]>();
        emptyArray = new Object[0];
    }
    
    @Deprecated
    public interface AttributeHolder
    {
        void setAttribute(final String p0, final Object p1);
    }
    
    public interface PropertySource
    {
        String getProperty(final String p0);
    }
}
