// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import org.apache.juli.logging.LogFactory;
import javax.management.MBeanServer;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.AttributeChangeNotification;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.InstanceNotFoundException;
import java.util.Iterator;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanInfo;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import org.apache.juli.logging.Log;
import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;
import javax.management.MBeanRegistration;
import javax.management.DynamicMBean;

public class BaseModelMBean implements DynamicMBean, MBeanRegistration, ModelMBeanNotificationBroadcaster
{
    private static final Log log;
    protected ObjectName oname;
    protected BaseNotificationBroadcaster attributeBroadcaster;
    protected BaseNotificationBroadcaster generalBroadcaster;
    protected ManagedBean managedBean;
    protected Object resource;
    static final Object[] NO_ARGS_PARAM;
    static final Class<?>[] NO_ARGS_PARAM_SIG;
    protected String resourceType;
    
    protected BaseModelMBean() throws MBeanException, RuntimeOperationsException {
        this.oname = null;
        this.attributeBroadcaster = null;
        this.generalBroadcaster = null;
        this.managedBean = null;
        this.resource = null;
        this.resourceType = null;
    }
    
    @Override
    public Object getAttribute(final String name) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (name == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name is null"), "Attribute name is null");
        }
        if (this.resource instanceof DynamicMBean && !(this.resource instanceof BaseModelMBean)) {
            return ((DynamicMBean)this.resource).getAttribute(name);
        }
        final Method m = this.managedBean.getGetter(name, this, this.resource);
        Object result = null;
        try {
            final Class<?> declaring = m.getDeclaringClass();
            if (declaring.isAssignableFrom(this.getClass())) {
                result = m.invoke(this, BaseModelMBean.NO_ARGS_PARAM);
            }
            else {
                result = m.invoke(this.resource, BaseModelMBean.NO_ARGS_PARAM);
            }
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t == null) {
                t = e;
            }
            if (t instanceof RuntimeException) {
                throw new RuntimeOperationsException((RuntimeException)t, "Exception invoking method " + name);
            }
            if (t instanceof Error) {
                throw new RuntimeErrorException((Error)t, "Error invoking method " + name);
            }
            throw new MBeanException(e, "Exception invoking method " + name);
        }
        catch (Exception e2) {
            throw new MBeanException(e2, "Exception invoking method " + name);
        }
        return result;
    }
    
    @Override
    public AttributeList getAttributes(final String[] names) {
        if (names == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute names list is null"), "Attribute names list is null");
        }
        final AttributeList response = new AttributeList();
        for (int i = 0; i < names.length; ++i) {
            try {
                response.add(new Attribute(names[i], this.getAttribute(names[i])));
            }
            catch (Exception ex) {}
        }
        return response;
    }
    
    public void setManagedBean(final ManagedBean managedBean) {
        this.managedBean = managedBean;
    }
    
    @Override
    public MBeanInfo getMBeanInfo() {
        return this.managedBean.getMBeanInfo();
    }
    
    @Override
    public Object invoke(final String name, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        if (this.resource instanceof DynamicMBean && !(this.resource instanceof BaseModelMBean)) {
            return ((DynamicMBean)this.resource).invoke(name, params, signature);
        }
        if (name == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Method name is null"), "Method name is null");
        }
        if (BaseModelMBean.log.isDebugEnabled()) {
            BaseModelMBean.log.debug((Object)("Invoke " + name));
        }
        final Method method = this.managedBean.getInvoke(name, params, signature, this, this.resource);
        Object result = null;
        try {
            if (method.getDeclaringClass().isAssignableFrom(this.getClass())) {
                result = method.invoke(this, params);
            }
            else {
                result = method.invoke(this.resource, params);
            }
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            BaseModelMBean.log.error((Object)("Exception invoking method " + name), t);
            if (t == null) {
                t = e;
            }
            if (t instanceof RuntimeException) {
                throw new RuntimeOperationsException((RuntimeException)t, "Exception invoking method " + name);
            }
            if (t instanceof Error) {
                throw new RuntimeErrorException((Error)t, "Error invoking method " + name);
            }
            throw new MBeanException((Exception)t, "Exception invoking method " + name);
        }
        catch (Exception e2) {
            BaseModelMBean.log.error((Object)("Exception invoking method " + name), (Throwable)e2);
            throw new MBeanException(e2, "Exception invoking method " + name);
        }
        return result;
    }
    
    static Class<?> getAttributeClass(final String signature) throws ReflectionException {
        if (signature.equals(Boolean.TYPE.getName())) {
            return Boolean.TYPE;
        }
        if (signature.equals(Byte.TYPE.getName())) {
            return Byte.TYPE;
        }
        if (signature.equals(Character.TYPE.getName())) {
            return Character.TYPE;
        }
        if (signature.equals(Double.TYPE.getName())) {
            return Double.TYPE;
        }
        if (signature.equals(Float.TYPE.getName())) {
            return Float.TYPE;
        }
        if (signature.equals(Integer.TYPE.getName())) {
            return Integer.TYPE;
        }
        if (signature.equals(Long.TYPE.getName())) {
            return Long.TYPE;
        }
        if (signature.equals(Short.TYPE.getName())) {
            return Short.TYPE;
        }
        try {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                return cl.loadClass(signature);
            }
        }
        catch (ClassNotFoundException ex) {}
        try {
            return Class.forName(signature);
        }
        catch (ClassNotFoundException e) {
            throw new ReflectionException(e, "Cannot find Class for " + signature);
        }
    }
    
    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (BaseModelMBean.log.isDebugEnabled()) {
            BaseModelMBean.log.debug((Object)("Setting attribute " + this + " " + attribute));
        }
        if (this.resource instanceof DynamicMBean && !(this.resource instanceof BaseModelMBean)) {
            try {
                ((DynamicMBean)this.resource).setAttribute(attribute);
            }
            catch (InvalidAttributeValueException e) {
                throw new MBeanException(e);
            }
            return;
        }
        if (attribute == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute is null"), "Attribute is null");
        }
        final String name = attribute.getName();
        final Object value = attribute.getValue();
        if (name == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name is null"), "Attribute name is null");
        }
        final Object oldValue = null;
        final Method m = this.managedBean.getSetter(name, this, this.resource);
        try {
            if (m.getDeclaringClass().isAssignableFrom(this.getClass())) {
                m.invoke(this, value);
            }
            else {
                m.invoke(this.resource, value);
            }
        }
        catch (InvocationTargetException e2) {
            Throwable t = e2.getTargetException();
            if (t == null) {
                t = e2;
            }
            if (t instanceof RuntimeException) {
                throw new RuntimeOperationsException((RuntimeException)t, "Exception invoking method " + name);
            }
            if (t instanceof Error) {
                throw new RuntimeErrorException((Error)t, "Error invoking method " + name);
            }
            throw new MBeanException(e2, "Exception invoking method " + name);
        }
        catch (Exception e3) {
            BaseModelMBean.log.error((Object)("Exception invoking method " + name), (Throwable)e3);
            throw new MBeanException(e3, "Exception invoking method " + name);
        }
        try {
            this.sendAttributeChangeNotification(new Attribute(name, oldValue), attribute);
        }
        catch (Exception ex) {
            BaseModelMBean.log.error((Object)("Error sending notification " + name), (Throwable)ex);
        }
    }
    
    @Override
    public String toString() {
        if (this.resource == null) {
            return "BaseModelMbean[" + this.resourceType + "]";
        }
        return this.resource.toString();
    }
    
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        final AttributeList response = new AttributeList();
        if (attributes == null) {
            return response;
        }
        final String[] names = new String[attributes.size()];
        int n = 0;
        for (final Attribute item : attributes) {
            names[n++] = item.getName();
            try {
                this.setAttribute(item);
            }
            catch (Exception ex) {}
        }
        return this.getAttributes(names);
    }
    
    public Object getManagedResource() throws InstanceNotFoundException, InvalidTargetObjectTypeException, MBeanException, RuntimeOperationsException {
        if (this.resource == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Managed resource is null"), "Managed resource is null");
        }
        return this.resource;
    }
    
    public void setManagedResource(final Object resource, final String type) throws InstanceNotFoundException, MBeanException, RuntimeOperationsException {
        if (resource == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Managed resource is null"), "Managed resource is null");
        }
        this.resource = resource;
        this.resourceType = resource.getClass().getName();
    }
    
    @Override
    public void addAttributeChangeNotificationListener(final NotificationListener listener, final String name, final Object handback) throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        if (this.attributeBroadcaster == null) {
            this.attributeBroadcaster = new BaseNotificationBroadcaster();
        }
        if (BaseModelMBean.log.isDebugEnabled()) {
            BaseModelMBean.log.debug((Object)("addAttributeNotificationListener " + listener));
        }
        final BaseAttributeFilter filter = new BaseAttributeFilter(name);
        this.attributeBroadcaster.addNotificationListener(listener, filter, handback);
    }
    
    @Override
    public void removeAttributeChangeNotificationListener(final NotificationListener listener, final String name) throws ListenerNotFoundException {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        if (this.attributeBroadcaster == null) {
            this.attributeBroadcaster = new BaseNotificationBroadcaster();
        }
        this.attributeBroadcaster.removeNotificationListener(listener);
    }
    
    public void removeAttributeChangeNotificationListener(final NotificationListener listener, final String attributeName, final Object handback) throws ListenerNotFoundException {
        this.removeAttributeChangeNotificationListener(listener, attributeName);
    }
    
    @Override
    public void sendAttributeChangeNotification(final AttributeChangeNotification notification) throws MBeanException, RuntimeOperationsException {
        if (notification == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Notification is null"), "Notification is null");
        }
        if (this.attributeBroadcaster == null) {
            return;
        }
        if (BaseModelMBean.log.isDebugEnabled()) {
            BaseModelMBean.log.debug((Object)("AttributeChangeNotification " + notification));
        }
        this.attributeBroadcaster.sendNotification(notification);
    }
    
    @Override
    public void sendAttributeChangeNotification(final Attribute oldValue, final Attribute newValue) throws MBeanException, RuntimeOperationsException {
        String type = null;
        if (newValue.getValue() != null) {
            type = newValue.getValue().getClass().getName();
        }
        else {
            if (oldValue.getValue() == null) {
                return;
            }
            type = oldValue.getValue().getClass().getName();
        }
        final AttributeChangeNotification notification = new AttributeChangeNotification(this, 1L, System.currentTimeMillis(), "Attribute value has changed", oldValue.getName(), type, oldValue.getValue(), newValue.getValue());
        this.sendAttributeChangeNotification(notification);
    }
    
    @Override
    public void sendNotification(final Notification notification) throws MBeanException, RuntimeOperationsException {
        if (notification == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Notification is null"), "Notification is null");
        }
        if (this.generalBroadcaster == null) {
            return;
        }
        this.generalBroadcaster.sendNotification(notification);
    }
    
    @Override
    public void sendNotification(final String message) throws MBeanException, RuntimeOperationsException {
        if (message == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Message is null"), "Message is null");
        }
        final Notification notification = new Notification("jmx.modelmbean.generic", this, 1L, message);
        this.sendNotification(notification);
    }
    
    @Override
    public void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        if (BaseModelMBean.log.isDebugEnabled()) {
            BaseModelMBean.log.debug((Object)("addNotificationListener " + listener));
        }
        if (this.generalBroadcaster == null) {
            this.generalBroadcaster = new BaseNotificationBroadcaster();
        }
        this.generalBroadcaster.addNotificationListener(listener, filter, handback);
        if (this.attributeBroadcaster == null) {
            this.attributeBroadcaster = new BaseNotificationBroadcaster();
        }
        if (BaseModelMBean.log.isDebugEnabled()) {
            BaseModelMBean.log.debug((Object)("addAttributeNotificationListener " + listener));
        }
        this.attributeBroadcaster.addNotificationListener(listener, filter, handback);
    }
    
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        MBeanNotificationInfo[] current = this.getMBeanInfo().getNotifications();
        if (current == null) {
            current = new MBeanNotificationInfo[0];
        }
        final MBeanNotificationInfo[] response = new MBeanNotificationInfo[current.length + 2];
        response[0] = new MBeanNotificationInfo(new String[] { "jmx.modelmbean.generic" }, "GENERIC", "Text message notification from the managed resource");
        response[1] = new MBeanNotificationInfo(new String[] { "jmx.attribute.change" }, "ATTRIBUTE_CHANGE", "Observed MBean attribute value has changed");
        System.arraycopy(current, 0, response, 2, current.length);
        return response;
    }
    
    @Override
    public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        if (this.generalBroadcaster == null) {
            this.generalBroadcaster = new BaseNotificationBroadcaster();
        }
        this.generalBroadcaster.removeNotificationListener(listener);
    }
    
    public void removeNotificationListener(final NotificationListener listener, final Object handback) throws ListenerNotFoundException {
        this.removeNotificationListener(listener);
    }
    
    public void removeNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws ListenerNotFoundException {
        this.removeNotificationListener(listener);
    }
    
    public String getModelerType() {
        return this.resourceType;
    }
    
    public String getClassName() {
        return this.getModelerType();
    }
    
    public ObjectName getJmxName() {
        return this.oname;
    }
    
    public String getObjectName() {
        if (this.oname != null) {
            return this.oname.toString();
        }
        return null;
    }
    
    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception {
        if (BaseModelMBean.log.isDebugEnabled()) {
            BaseModelMBean.log.debug((Object)("preRegister " + this.resource + " " + name));
        }
        this.oname = name;
        if (this.resource instanceof MBeanRegistration) {
            this.oname = ((MBeanRegistration)this.resource).preRegister(server, name);
        }
        return this.oname;
    }
    
    @Override
    public void postRegister(final Boolean registrationDone) {
        if (this.resource instanceof MBeanRegistration) {
            ((MBeanRegistration)this.resource).postRegister(registrationDone);
        }
    }
    
    @Override
    public void preDeregister() throws Exception {
        if (this.resource instanceof MBeanRegistration) {
            ((MBeanRegistration)this.resource).preDeregister();
        }
    }
    
    @Override
    public void postDeregister() {
        if (this.resource instanceof MBeanRegistration) {
            ((MBeanRegistration)this.resource).postDeregister();
        }
    }
    
    static {
        log = LogFactory.getLog((Class)BaseModelMBean.class);
        NO_ARGS_PARAM = new Object[0];
        NO_ARGS_PARAM_SIG = new Class[0];
    }
    
    static class MethodKey
    {
        private String name;
        private String[] signature;
        
        MethodKey(final String name, String[] signature) {
            this.name = name;
            if (signature == null) {
                signature = new String[0];
            }
            this.signature = signature;
        }
        
        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof MethodKey)) {
                return false;
            }
            final MethodKey omk = (MethodKey)other;
            if (!this.name.equals(omk.name)) {
                return false;
            }
            if (this.signature.length != omk.signature.length) {
                return false;
            }
            for (int i = 0; i < this.signature.length; ++i) {
                if (!this.signature[i].equals(omk.signature[i])) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
    }
}
