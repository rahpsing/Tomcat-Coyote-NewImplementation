// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler.modules;

import java.io.File;
import java.math.BigInteger;
import java.math.BigDecimal;
import org.apache.juli.logging.LogFactory;
import java.util.Enumeration;
import org.apache.tomcat.util.modeler.ParameterInfo;
import org.apache.tomcat.util.modeler.OperationInfo;
import org.apache.tomcat.util.modeler.AttributeInfo;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import org.apache.tomcat.util.modeler.ManagedBean;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.management.ObjectName;
import java.util.List;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.juli.logging.Log;

public class MbeansDescriptorsIntrospectionSource extends ModelerSource
{
    private static final Log log;
    Registry registry;
    String type;
    List<ObjectName> mbeans;
    static Hashtable<String, String> specialMethods;
    private static String[] strArray;
    private static ObjectName[] objNameArray;
    private static Class<?>[] supportedTypes;
    
    public MbeansDescriptorsIntrospectionSource() {
        this.mbeans = new ArrayList<ObjectName>();
    }
    
    public void setRegistry(final Registry reg) {
        this.registry = reg;
    }
    
    @Deprecated
    public void setLocation(final String loc) {
        this.location = loc;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    public void setSource(final Object source) {
        this.source = source;
    }
    
    @Override
    public List<ObjectName> loadDescriptors(final Registry registry, final String type, final Object source) throws Exception {
        this.setRegistry(registry);
        this.setType(type);
        this.setSource(source);
        this.execute();
        return this.mbeans;
    }
    
    public void execute() throws Exception {
        if (this.registry == null) {
            this.registry = Registry.getRegistry(null, null);
        }
        try {
            final ManagedBean managed = this.createManagedBean(this.registry, null, (Class<?>)this.source, this.type);
            if (managed == null) {
                return;
            }
            managed.setName(this.type);
            this.registry.addManagedBean(managed);
        }
        catch (Exception ex) {
            MbeansDescriptorsIntrospectionSource.log.error((Object)"Error reading descriptors ", (Throwable)ex);
        }
    }
    
    private boolean supportedType(final Class<?> ret) {
        for (int i = 0; i < MbeansDescriptorsIntrospectionSource.supportedTypes.length; ++i) {
            if (ret == MbeansDescriptorsIntrospectionSource.supportedTypes[i]) {
                return true;
            }
        }
        return this.isBeanCompatible(ret);
    }
    
    protected boolean isBeanCompatible(final Class<?> javaType) {
        if (javaType.isArray() || javaType.isPrimitive()) {
            return false;
        }
        if (javaType.getName().startsWith("java.") || javaType.getName().startsWith("javax.")) {
            return false;
        }
        try {
            javaType.getConstructor((Class<?>[])new Class[0]);
        }
        catch (NoSuchMethodException e) {
            return false;
        }
        final Class<?> superClass = javaType.getSuperclass();
        return superClass == null || superClass == Object.class || superClass == Exception.class || superClass == Throwable.class || this.isBeanCompatible(superClass);
    }
    
    private void initMethods(final Class<?> realClass, final Method[] methods, final Hashtable<String, Method> attMap, final Hashtable<String, Method> getAttMap, final Hashtable<String, Method> setAttMap, final Hashtable<String, Method> invokeAttMap) {
        for (int j = 0; j < methods.length; ++j) {
            String name = methods[j].getName();
            if (!Modifier.isStatic(methods[j].getModifiers())) {
                if (!Modifier.isPublic(methods[j].getModifiers())) {
                    if (MbeansDescriptorsIntrospectionSource.log.isDebugEnabled()) {
                        MbeansDescriptorsIntrospectionSource.log.debug((Object)("Not public " + methods[j]));
                    }
                }
                else if (methods[j].getDeclaringClass() != Object.class) {
                    final Class<?>[] params = methods[j].getParameterTypes();
                    if (name.startsWith("get") && params.length == 0) {
                        final Class<?> ret = methods[j].getReturnType();
                        if (!this.supportedType(ret)) {
                            if (MbeansDescriptorsIntrospectionSource.log.isDebugEnabled()) {
                                MbeansDescriptorsIntrospectionSource.log.debug((Object)("Unsupported type " + methods[j]));
                            }
                        }
                        else {
                            name = unCapitalize(name.substring(3));
                            getAttMap.put(name, methods[j]);
                            attMap.put(name, methods[j]);
                        }
                    }
                    else if (name.startsWith("is") && params.length == 0) {
                        final Class<?> ret = methods[j].getReturnType();
                        if (Boolean.TYPE != ret) {
                            if (MbeansDescriptorsIntrospectionSource.log.isDebugEnabled()) {
                                MbeansDescriptorsIntrospectionSource.log.debug((Object)("Unsupported type " + methods[j] + " " + ret));
                            }
                        }
                        else {
                            name = unCapitalize(name.substring(2));
                            getAttMap.put(name, methods[j]);
                            attMap.put(name, methods[j]);
                        }
                    }
                    else if (name.startsWith("set") && params.length == 1) {
                        if (!this.supportedType(params[0])) {
                            if (MbeansDescriptorsIntrospectionSource.log.isDebugEnabled()) {
                                MbeansDescriptorsIntrospectionSource.log.debug((Object)("Unsupported type " + methods[j] + " " + params[0]));
                            }
                        }
                        else {
                            name = unCapitalize(name.substring(3));
                            setAttMap.put(name, methods[j]);
                            attMap.put(name, methods[j]);
                        }
                    }
                    else if (params.length == 0) {
                        if (MbeansDescriptorsIntrospectionSource.specialMethods.get(methods[j].getName()) == null) {
                            invokeAttMap.put(name, methods[j]);
                        }
                    }
                    else {
                        boolean supported = true;
                        for (int i = 0; i < params.length; ++i) {
                            if (!this.supportedType(params[i])) {
                                supported = false;
                                break;
                            }
                        }
                        if (supported) {
                            invokeAttMap.put(name, methods[j]);
                        }
                    }
                }
            }
        }
    }
    
    public ManagedBean createManagedBean(final Registry registry, final String domain, final Class<?> realClass, final String type) {
        final ManagedBean mbean = new ManagedBean();
        Method[] methods = null;
        final Hashtable<String, Method> attMap = new Hashtable<String, Method>();
        final Hashtable<String, Method> getAttMap = new Hashtable<String, Method>();
        final Hashtable<String, Method> setAttMap = new Hashtable<String, Method>();
        final Hashtable<String, Method> invokeAttMap = new Hashtable<String, Method>();
        methods = realClass.getMethods();
        this.initMethods(realClass, methods, attMap, getAttMap, setAttMap, invokeAttMap);
        try {
            Enumeration<String> en = attMap.keys();
            while (en.hasMoreElements()) {
                final String name = en.nextElement();
                final AttributeInfo ai = new AttributeInfo();
                ai.setName(name);
                final Method gm = getAttMap.get(name);
                if (gm != null) {
                    ai.setGetMethod(gm.getName());
                    final Class<?> t = gm.getReturnType();
                    if (t != null) {
                        ai.setType(t.getName());
                    }
                }
                final Method sm = setAttMap.get(name);
                if (sm != null) {
                    final Class<?> t2 = sm.getParameterTypes()[0];
                    if (t2 != null) {
                        ai.setType(t2.getName());
                    }
                    ai.setSetMethod(sm.getName());
                }
                ai.setDescription("Introspected attribute " + name);
                if (MbeansDescriptorsIntrospectionSource.log.isDebugEnabled()) {
                    MbeansDescriptorsIntrospectionSource.log.debug((Object)("Introspected attribute " + name + " " + gm + " " + sm));
                }
                if (gm == null) {
                    ai.setReadable(false);
                }
                if (sm == null) {
                    ai.setWriteable(false);
                }
                if (sm != null || gm != null) {
                    mbean.addAttribute(ai);
                }
            }
            en = invokeAttMap.keys();
            while (en.hasMoreElements()) {
                final String name = en.nextElement();
                final Method m = invokeAttMap.get(name);
                if (m != null && name != null) {
                    final OperationInfo op = new OperationInfo();
                    op.setName(name);
                    op.setReturnType(m.getReturnType().getName());
                    op.setDescription("Introspected operation " + name);
                    final Class<?>[] parms = m.getParameterTypes();
                    for (int i = 0; i < parms.length; ++i) {
                        final ParameterInfo pi = new ParameterInfo();
                        pi.setType(parms[i].getName());
                        pi.setName("param" + i);
                        pi.setDescription("Introspected parameter param" + i);
                        op.addParameter(pi);
                    }
                    mbean.addOperation(op);
                }
                else {
                    MbeansDescriptorsIntrospectionSource.log.error((Object)("Null arg " + name + " " + m));
                }
            }
            if (MbeansDescriptorsIntrospectionSource.log.isDebugEnabled()) {
                MbeansDescriptorsIntrospectionSource.log.debug((Object)("Setting name: " + type));
            }
            mbean.setName(type);
            return mbean;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    private static String unCapitalize(final String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        final char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
    
    static {
        log = LogFactory.getLog((Class)MbeansDescriptorsIntrospectionSource.class);
        (MbeansDescriptorsIntrospectionSource.specialMethods = new Hashtable<String, String>()).put("preDeregister", "");
        MbeansDescriptorsIntrospectionSource.specialMethods.put("postDeregister", "");
        MbeansDescriptorsIntrospectionSource.strArray = new String[0];
        MbeansDescriptorsIntrospectionSource.objNameArray = new ObjectName[0];
        MbeansDescriptorsIntrospectionSource.supportedTypes = (Class<?>[])new Class[] { Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE, Character.class, Character.TYPE, Short.class, Short.TYPE, Integer.class, Integer.TYPE, Long.class, Long.TYPE, Float.class, Float.TYPE, Double.class, Double.TYPE, String.class, MbeansDescriptorsIntrospectionSource.strArray.getClass(), BigDecimal.class, BigInteger.class, ObjectName.class, MbeansDescriptorsIntrospectionSource.objNameArray.getClass(), File.class };
    }
}
