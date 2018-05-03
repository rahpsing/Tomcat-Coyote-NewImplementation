// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import org.apache.juli.logging.LogFactory;
import javax.management.DynamicMBean;
import org.apache.tomcat.util.modeler.modules.ModelerSource;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServerFactory;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import java.util.ArrayList;
import java.util.Iterator;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.List;
import java.util.Hashtable;
import java.net.URL;
import javax.management.MBeanServer;
import java.util.HashMap;
import org.apache.juli.logging.Log;
import javax.management.MBeanRegistration;

public class Registry implements RegistryMBean, MBeanRegistration
{
    private static final Log log;
    private static HashMap<Object, Registry> perLoaderRegistries;
    private static Registry registry;
    private MBeanServer server;
    private HashMap<String, ManagedBean> descriptors;
    private HashMap<String, ManagedBean> descriptorsByClass;
    private HashMap<String, URL> searchedPaths;
    private Object guard;
    private Hashtable<String, Hashtable<String, Integer>> idDomains;
    private Hashtable<String, int[]> ids;
    
    public Registry() {
        this.server = null;
        this.descriptors = new HashMap<String, ManagedBean>();
        this.descriptorsByClass = new HashMap<String, ManagedBean>();
        this.searchedPaths = new HashMap<String, URL>();
        this.idDomains = new Hashtable<String, Hashtable<String, Integer>>();
        this.ids = new Hashtable<String, int[]>();
    }
    
    public static synchronized Registry getRegistry(Object key, final Object guard) {
        if (Registry.perLoaderRegistries != null) {
            if (key == null) {
                key = Thread.currentThread().getContextClassLoader();
            }
            if (key != null) {
                Registry localRegistry = Registry.perLoaderRegistries.get(key);
                if (localRegistry == null) {
                    localRegistry = new Registry();
                    localRegistry.guard = guard;
                    Registry.perLoaderRegistries.put(key, localRegistry);
                    return localRegistry;
                }
                if (localRegistry.guard != null && localRegistry.guard != guard) {
                    return null;
                }
                return localRegistry;
            }
        }
        if (Registry.registry == null) {
            Registry.registry = new Registry();
        }
        if (Registry.registry.guard != null && Registry.registry.guard != guard) {
            return null;
        }
        return Registry.registry;
    }
    
    public static void setUseContextClassLoader(final boolean enable) {
        if (enable) {
            Registry.perLoaderRegistries = new HashMap<Object, Registry>();
        }
    }
    
    @Override
    public void stop() {
        this.descriptorsByClass = new HashMap<String, ManagedBean>();
        this.descriptors = new HashMap<String, ManagedBean>();
        this.searchedPaths = new HashMap<String, URL>();
    }
    
    @Override
    public List<ObjectName> loadMBeans(final Object source, final ClassLoader cl) throws Exception {
        return this.load("MbeansSource", source, null);
    }
    
    @Override
    public void loadMetadata(final Object source) throws Exception {
        this.loadDescriptors(null, source, null);
    }
    
    @Override
    public void registerComponent(final Object bean, final String oname, final String type) throws Exception {
        this.registerComponent(bean, new ObjectName(oname), type);
    }
    
    @Override
    public void unregisterComponent(final String oname) {
        try {
            this.unregisterComponent(new ObjectName(oname));
        }
        catch (MalformedObjectNameException e) {
            Registry.log.info((Object)("Error creating object name " + e));
        }
    }
    
    @Override
    public void invoke(final List<ObjectName> mbeans, final String operation, final boolean failFirst) throws Exception {
        if (mbeans == null) {
            return;
        }
        for (final ObjectName current : mbeans) {
            try {
                if (current == null) {
                    continue;
                }
                if (this.getMethodInfo(current, operation) == null) {
                    continue;
                }
                this.getMBeanServer().invoke(current, operation, new Object[0], new String[0]);
            }
            catch (Exception t) {
                if (failFirst) {
                    throw t;
                }
                Registry.log.info((Object)("Error initializing " + current + " " + t.toString()));
            }
        }
    }
    
    @Override
    public synchronized int getId(String domain, String name) {
        if (domain == null) {
            domain = "";
        }
        Hashtable<String, Integer> domainTable = this.idDomains.get(domain);
        if (domainTable == null) {
            domainTable = new Hashtable<String, Integer>();
            this.idDomains.put(domain, domainTable);
        }
        if (name == null) {
            name = "";
        }
        final Integer i = domainTable.get(name);
        if (i != null) {
            return i;
        }
        int[] id = this.ids.get(domain);
        if (id == null) {
            id = new int[] { 0 };
            this.ids.put(domain, id);
        }
        final int code = id[0]++;
        domainTable.put(name, new Integer(code));
        return code;
    }
    
    public void addManagedBean(final ManagedBean bean) {
        this.descriptors.put(bean.getName(), bean);
        if (bean.getType() != null) {
            this.descriptorsByClass.put(bean.getType(), bean);
        }
    }
    
    public ManagedBean findManagedBean(final String name) {
        ManagedBean mb = this.descriptors.get(name);
        if (mb == null) {
            mb = this.descriptorsByClass.get(name);
        }
        return mb;
    }
    
    public String[] findManagedBeans() {
        return this.descriptors.keySet().toArray(new String[0]);
    }
    
    public String[] findManagedBeans(final String group) {
        final ArrayList<String> results = new ArrayList<String>();
        for (final ManagedBean item : this.descriptors.values()) {
            if (group == null) {
                if (item.getGroup() != null) {
                    continue;
                }
                results.add(item.getName());
            }
            else {
                if (!group.equals(item.getGroup())) {
                    continue;
                }
                results.add(item.getName());
            }
        }
        final String[] values = new String[results.size()];
        return results.toArray(values);
    }
    
    public void removeManagedBean(final ManagedBean bean) {
        this.descriptors.remove(bean.getName());
        this.descriptorsByClass.remove(bean.getType());
    }
    
    public String getType(final ObjectName oname, final String attName) {
        String type = null;
        MBeanInfo info = null;
        try {
            info = this.server.getMBeanInfo(oname);
        }
        catch (Exception e) {
            Registry.log.info((Object)("Can't find metadata for object" + oname));
            return null;
        }
        final MBeanAttributeInfo[] attInfo = info.getAttributes();
        for (int i = 0; i < attInfo.length; ++i) {
            if (attName.equals(attInfo[i].getName())) {
                type = attInfo[i].getType();
                return type;
            }
        }
        return null;
    }
    
    public MBeanOperationInfo getMethodInfo(final ObjectName oname, final String opName) {
        MBeanInfo info = null;
        try {
            info = this.server.getMBeanInfo(oname);
        }
        catch (Exception e) {
            Registry.log.info((Object)("Can't find metadata " + oname));
            return null;
        }
        final MBeanOperationInfo[] attInfo = info.getOperations();
        for (int i = 0; i < attInfo.length; ++i) {
            if (opName.equals(attInfo[i].getName())) {
                return attInfo[i];
            }
        }
        return null;
    }
    
    public void unregisterComponent(final ObjectName oname) {
        try {
            if (this.getMBeanServer().isRegistered(oname)) {
                this.getMBeanServer().unregisterMBean(oname);
            }
        }
        catch (Throwable t) {
            Registry.log.error((Object)"Error unregistering mbean ", t);
        }
    }
    
    public synchronized MBeanServer getMBeanServer() {
        final long t1 = System.currentTimeMillis();
        if (this.server == null) {
            if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
                this.server = MBeanServerFactory.findMBeanServer(null).get(0);
                if (Registry.log.isDebugEnabled()) {
                    Registry.log.debug((Object)("Using existing MBeanServer " + (System.currentTimeMillis() - t1)));
                }
            }
            else {
                this.server = ManagementFactory.getPlatformMBeanServer();
                if (Registry.log.isDebugEnabled()) {
                    Registry.log.debug((Object)("Creating MBeanServer" + (System.currentTimeMillis() - t1)));
                }
            }
        }
        return this.server;
    }
    
    public ManagedBean findManagedBean(final Object bean, Class<?> beanClass, String type) throws Exception {
        if (bean != null && beanClass == null) {
            beanClass = bean.getClass();
        }
        if (type == null) {
            type = beanClass.getName();
        }
        ManagedBean managed = this.findManagedBean(type);
        if (managed == null) {
            if (Registry.log.isDebugEnabled()) {
                Registry.log.debug((Object)"Looking for descriptor ");
            }
            this.findDescriptor(beanClass, type);
            managed = this.findManagedBean(type);
        }
        if (managed == null) {
            if (Registry.log.isDebugEnabled()) {
                Registry.log.debug((Object)"Introspecting ");
            }
            this.loadDescriptors("MbeansDescriptorsIntrospectionSource", beanClass, type);
            managed = this.findManagedBean(type);
            if (managed == null) {
                Registry.log.warn((Object)("No metadata found for " + type));
                return null;
            }
            managed.setName(type);
            this.addManagedBean(managed);
        }
        return managed;
    }
    
    public Object convertValue(final String type, final String value) {
        Object objValue = value;
        if (type == null || "java.lang.String".equals(type)) {
            objValue = value;
        }
        else {
            Label_0054: {
                if (!"javax.management.ObjectName".equals(type)) {
                    if (!"ObjectName".equals(type)) {
                        break Label_0054;
                    }
                }
                try {
                    objValue = new ObjectName(value);
                    return objValue;
                }
                catch (MalformedObjectNameException e) {
                    return null;
                }
            }
            if ("java.lang.Integer".equals(type) || "int".equals(type)) {
                objValue = new Integer(value);
            }
            else if ("java.lang.Long".equals(type) || "long".equals(type)) {
                objValue = new Long(value);
            }
            else if ("java.lang.Boolean".equals(type) || "boolean".equals(type)) {
                objValue = Boolean.valueOf(value);
            }
        }
        return objValue;
    }
    
    public List<ObjectName> load(String sourceType, final Object source, final String param) throws Exception {
        if (Registry.log.isTraceEnabled()) {
            Registry.log.trace((Object)("load " + source));
        }
        String location = null;
        String type = null;
        Object inputsource = null;
        if (source instanceof URL) {
            final URL url = (URL)source;
            location = url.toString();
            type = param;
            inputsource = url.openStream();
            if (sourceType == null) {
                sourceType = this.sourceTypeFromExt(location);
            }
        }
        else if (source instanceof File) {
            location = ((File)source).getAbsolutePath();
            inputsource = new FileInputStream((File)source);
            type = param;
            if (sourceType == null) {
                sourceType = this.sourceTypeFromExt(location);
            }
        }
        else if (source instanceof InputStream) {
            type = param;
            inputsource = source;
        }
        else if (source instanceof Class) {
            location = ((Class)source).getName();
            type = param;
            inputsource = source;
            if (sourceType == null) {
                sourceType = "MbeansDescriptorsIntrospectionSource";
            }
        }
        if (sourceType == null) {
            sourceType = "MbeansDescriptorsDigesterSource";
        }
        final ModelerSource ds = this.getModelerSource(sourceType);
        final List<ObjectName> mbeans = ds.loadDescriptors(this, type, inputsource);
        return mbeans;
    }
    
    private String sourceTypeFromExt(final String s) {
        if (s.endsWith(".ser")) {
            return "MbeansDescriptorsSerSource";
        }
        if (s.endsWith(".xml")) {
            return "MbeansDescriptorsDigesterSource";
        }
        return null;
    }
    
    public void registerComponent(final Object bean, final ObjectName oname, String type) throws Exception {
        if (Registry.log.isDebugEnabled()) {
            Registry.log.debug((Object)("Managed= " + oname));
        }
        if (bean == null) {
            Registry.log.error((Object)("Null component " + oname));
            return;
        }
        try {
            if (type == null) {
                type = bean.getClass().getName();
            }
            final ManagedBean managed = this.findManagedBean(bean.getClass(), type);
            final DynamicMBean mbean = managed.createMBean(bean);
            if (this.getMBeanServer().isRegistered(oname)) {
                if (Registry.log.isDebugEnabled()) {
                    Registry.log.debug((Object)("Unregistering existing component " + oname));
                }
                this.getMBeanServer().unregisterMBean(oname);
            }
            this.getMBeanServer().registerMBean(mbean, oname);
        }
        catch (Exception ex) {
            Registry.log.error((Object)("Error registering " + oname), (Throwable)ex);
            throw ex;
        }
    }
    
    public void loadDescriptors(final String packageName, final ClassLoader classLoader) {
        final String res = packageName.replace('.', '/');
        if (Registry.log.isTraceEnabled()) {
            Registry.log.trace((Object)("Finding descriptor " + res));
        }
        if (this.searchedPaths.get(packageName) != null) {
            return;
        }
        String descriptors = res + "/mbeans-descriptors.ser";
        URL dURL = classLoader.getResource(descriptors);
        if (dURL == null) {
            descriptors = res + "/mbeans-descriptors.xml";
            dURL = classLoader.getResource(descriptors);
        }
        if (dURL == null) {
            return;
        }
        Registry.log.debug((Object)("Found " + dURL));
        this.searchedPaths.put(packageName, dURL);
        try {
            if (descriptors.endsWith(".xml")) {
                this.loadDescriptors("MbeansDescriptorsDigesterSource", dURL, null);
            }
            else {
                this.loadDescriptors("MbeansDescriptorsSerSource", dURL, null);
            }
        }
        catch (Exception ex) {
            Registry.log.error((Object)("Error loading " + dURL));
        }
    }
    
    private void loadDescriptors(final String sourceType, final Object source, final String param) throws Exception {
        this.load(sourceType, source, param);
    }
    
    private void findDescriptor(final Class<?> beanClass, String type) {
        if (type == null) {
            type = beanClass.getName();
        }
        ClassLoader classLoader = null;
        if (beanClass != null) {
            classLoader = beanClass.getClassLoader();
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }
        String pkg;
        final String className = pkg = type;
        while (pkg.indexOf(".") > 0) {
            final int lastComp = pkg.lastIndexOf(".");
            if (lastComp <= 0) {
                return;
            }
            pkg = pkg.substring(0, lastComp);
            if (this.searchedPaths.get(pkg) != null) {
                return;
            }
            this.loadDescriptors(pkg, classLoader);
        }
    }
    
    private ModelerSource getModelerSource(String type) throws Exception {
        if (type == null) {
            type = "MbeansDescriptorsDigesterSource";
        }
        if (type.indexOf(".") < 0) {
            type = "org.apache.tomcat.util.modeler.modules." + type;
        }
        final Class<?> c = Class.forName(type);
        final ModelerSource ds = (ModelerSource)c.newInstance();
        return ds;
    }
    
    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception {
        this.server = server;
        return name;
    }
    
    @Override
    public void postRegister(final Boolean registrationDone) {
    }
    
    @Override
    public void preDeregister() throws Exception {
    }
    
    @Override
    public void postDeregister() {
    }
    
    @Deprecated
    public ManagedBean findManagedBean(final Class<?> beanClass, final String type) throws Exception {
        return this.findManagedBean(null, beanClass, type);
    }
    
    public void setMBeanServer(final MBeanServer server) {
        this.server = server;
    }
    
    public void resetMetadata() {
        this.stop();
    }
    
    static {
        log = LogFactory.getLog((Class)Registry.class);
        Registry.perLoaderRegistries = null;
        Registry.registry = null;
    }
}
