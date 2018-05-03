// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.ServiceNotFoundException;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import java.lang.reflect.Method;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;
import javax.management.DynamicMBean;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanInfo;
import java.io.Serializable;

public class ManagedBean implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final String BASE_MBEAN = "org.apache.tomcat.util.modeler.BaseModelMBean";
    static final Object[] NO_ARGS_PARAM;
    static final Class<?>[] NO_ARGS_PARAM_SIG;
    transient MBeanInfo info;
    private Map<String, AttributeInfo> attributes;
    private Map<String, OperationInfo> operations;
    protected String className;
    protected String description;
    protected String domain;
    protected String group;
    protected String name;
    protected NotificationInfo[] notifications;
    protected String type;
    
    public ManagedBean() {
        this.info = null;
        this.attributes = new HashMap<String, AttributeInfo>();
        this.operations = new HashMap<String, OperationInfo>();
        this.className = "org.apache.tomcat.util.modeler.BaseModelMBean";
        this.description = null;
        this.domain = null;
        this.group = null;
        this.name = null;
        this.notifications = new NotificationInfo[0];
        this.type = null;
        final AttributeInfo ai = new AttributeInfo();
        ai.setName("modelerType");
        ai.setDescription("Type of the modeled resource. Can be set only once");
        ai.setType("java.lang.String");
        ai.setWriteable(false);
        this.addAttribute(ai);
    }
    
    public AttributeInfo[] getAttributes() {
        final AttributeInfo[] result = new AttributeInfo[this.attributes.size()];
        this.attributes.values().toArray(result);
        return result;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public void setClassName(final String className) {
        this.className = className;
        this.info = null;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
        this.info = null;
    }
    
    public String getDomain() {
        return this.domain;
    }
    
    public void setDomain(final String domain) {
        this.domain = domain;
    }
    
    public String getGroup() {
        return this.group;
    }
    
    public void setGroup(final String group) {
        this.group = group;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
        this.info = null;
    }
    
    public NotificationInfo[] getNotifications() {
        return this.notifications;
    }
    
    public OperationInfo[] getOperations() {
        final OperationInfo[] result = new OperationInfo[this.operations.size()];
        this.operations.values().toArray(result);
        return result;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setType(final String type) {
        this.type = type;
        this.info = null;
    }
    
    public void addAttribute(final AttributeInfo attribute) {
        this.attributes.put(attribute.getName(), attribute);
    }
    
    public void addNotification(final NotificationInfo notification) {
        synchronized (this.notifications) {
            final NotificationInfo[] results = new NotificationInfo[this.notifications.length + 1];
            System.arraycopy(this.notifications, 0, results, 0, this.notifications.length);
            results[this.notifications.length] = notification;
            this.notifications = results;
            this.info = null;
        }
    }
    
    public void addOperation(final OperationInfo operation) {
        this.operations.put(this.createOperationKey(operation), operation);
    }
    
    public DynamicMBean createMBean() throws InstanceNotFoundException, MBeanException, RuntimeOperationsException {
        return this.createMBean(null);
    }
    
    public DynamicMBean createMBean(final Object instance) throws InstanceNotFoundException, MBeanException, RuntimeOperationsException {
        BaseModelMBean mbean = null;
        if (this.getClassName().equals("org.apache.tomcat.util.modeler.BaseModelMBean")) {
            mbean = new BaseModelMBean();
        }
        else {
            Class<?> clazz = null;
            Exception ex = null;
            try {
                clazz = Class.forName(this.getClassName());
            }
            catch (Exception ex2) {}
            if (clazz == null) {
                try {
                    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    if (cl != null) {
                        clazz = cl.loadClass(this.getClassName());
                    }
                }
                catch (Exception e) {
                    ex = e;
                }
            }
            if (clazz == null) {
                throw new MBeanException(ex, "Cannot load ModelMBean class " + this.getClassName());
            }
            try {
                mbean = (BaseModelMBean)clazz.newInstance();
            }
            catch (RuntimeOperationsException e2) {
                throw e2;
            }
            catch (Exception e) {
                throw new MBeanException(e, "Cannot instantiate ModelMBean of class " + this.getClassName());
            }
        }
        mbean.setManagedBean(this);
        try {
            if (instance != null) {
                mbean.setManagedResource(instance, "ObjectReference");
            }
        }
        catch (InstanceNotFoundException e3) {
            throw e3;
        }
        return mbean;
    }
    
    MBeanInfo getMBeanInfo() {
        if (this.info != null) {
            return this.info;
        }
        final AttributeInfo[] attrs = this.getAttributes();
        final MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[attrs.length];
        for (int i = 0; i < attrs.length; ++i) {
            attributes[i] = attrs[i].createAttributeInfo();
        }
        final OperationInfo[] opers = this.getOperations();
        final MBeanOperationInfo[] operations = new MBeanOperationInfo[opers.length];
        for (int j = 0; j < opers.length; ++j) {
            operations[j] = opers[j].createOperationInfo();
        }
        final NotificationInfo[] notifs = this.getNotifications();
        final MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[notifs.length];
        for (int k = 0; k < notifs.length; ++k) {
            notifications[k] = notifs[k].createNotificationInfo();
        }
        return this.info = new MBeanInfo(this.getClassName(), this.getDescription(), attributes, new MBeanConstructorInfo[0], operations, notifications);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ManagedBean[");
        sb.append("name=");
        sb.append(this.name);
        sb.append(", className=");
        sb.append(this.className);
        sb.append(", description=");
        sb.append(this.description);
        if (this.group != null) {
            sb.append(", group=");
            sb.append(this.group);
        }
        sb.append(", type=");
        sb.append(this.type);
        sb.append("]");
        return sb.toString();
    }
    
    Method getGetter(final String aname, final BaseModelMBean mbean, final Object resource) throws AttributeNotFoundException, ReflectionException {
        Method m = null;
        final AttributeInfo attrInfo = this.attributes.get(aname);
        if (attrInfo == null) {
            throw new AttributeNotFoundException(" Cannot find attribute " + aname + " for " + resource);
        }
        final String getMethod = attrInfo.getGetMethod();
        if (getMethod == null) {
            throw new AttributeNotFoundException("Cannot find attribute " + aname + " get method name");
        }
        final Object object = null;
        NoSuchMethodException exception = null;
        try {
            m = mbean.getClass().getMethod(getMethod, ManagedBean.NO_ARGS_PARAM_SIG);
        }
        catch (NoSuchMethodException e) {
            exception = e;
        }
        if (m == null && resource != null) {
            try {
                m = resource.getClass().getMethod(getMethod, ManagedBean.NO_ARGS_PARAM_SIG);
                exception = null;
            }
            catch (NoSuchMethodException e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw new ReflectionException(exception, "Cannot find getter method " + getMethod);
        }
        return m;
    }
    
    public Method getSetter(final String aname, final BaseModelMBean bean, final Object resource) throws AttributeNotFoundException, ReflectionException {
        Method m = null;
        final AttributeInfo attrInfo = this.attributes.get(aname);
        if (attrInfo == null) {
            throw new AttributeNotFoundException(" Cannot find attribute " + aname);
        }
        final String setMethod = attrInfo.getSetMethod();
        if (setMethod == null) {
            throw new AttributeNotFoundException("Cannot find attribute " + aname + " set method name");
        }
        final String argType = attrInfo.getType();
        final Class<?>[] signature = (Class<?>[])new Class[] { BaseModelMBean.getAttributeClass(argType) };
        final Object object = null;
        NoSuchMethodException exception = null;
        try {
            m = bean.getClass().getMethod(setMethod, signature);
        }
        catch (NoSuchMethodException e) {
            exception = e;
        }
        if (m == null && resource != null) {
            try {
                m = resource.getClass().getMethod(setMethod, signature);
                exception = null;
            }
            catch (NoSuchMethodException e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw new ReflectionException(exception, "Cannot find setter method " + setMethod + " " + resource);
        }
        return m;
    }
    
    public Method getInvoke(final String aname, Object[] params, String[] signature, final BaseModelMBean bean, final Object resource) throws MBeanException, ReflectionException {
        Method method = null;
        if (params == null) {
            params = new Object[0];
        }
        if (signature == null) {
            signature = new String[0];
        }
        if (params.length != signature.length) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Inconsistent arguments and signature"), "Inconsistent arguments and signature");
        }
        final OperationInfo opInfo = this.operations.get(this.createOperationKey(aname, signature));
        if (opInfo == null) {
            throw new MBeanException(new ServiceNotFoundException("Cannot find operation " + aname), "Cannot find operation " + aname);
        }
        final Class<?>[] types = (Class<?>[])new Class[signature.length];
        for (int i = 0; i < signature.length; ++i) {
            types[i] = BaseModelMBean.getAttributeClass(signature[i]);
        }
        final Object object = null;
        Exception exception = null;
        try {
            method = bean.getClass().getMethod(aname, types);
        }
        catch (NoSuchMethodException e) {
            exception = e;
        }
        try {
            if (method == null && resource != null) {
                method = resource.getClass().getMethod(aname, types);
            }
        }
        catch (NoSuchMethodException e) {
            exception = e;
        }
        if (method == null) {
            throw new ReflectionException(exception, "Cannot find method " + aname + " with this signature");
        }
        return method;
    }
    
    private String createOperationKey(final OperationInfo operation) {
        final StringBuilder key = new StringBuilder(operation.getName());
        key.append('(');
        for (final ParameterInfo parameterInfo : operation.getSignature()) {
            key.append(parameterInfo.getType());
            key.append(',');
        }
        key.append(')');
        return key.toString();
    }
    
    private String createOperationKey(final String methodName, final String[] parameterTypes) {
        final StringBuilder key = new StringBuilder(methodName);
        key.append('(');
        for (final String parameter : parameterTypes) {
            key.append(parameter);
            key.append(',');
        }
        key.append(')');
        return key.toString();
    }
    
    static {
        NO_ARGS_PARAM = new Object[0];
        NO_ARGS_PARAM_SIG = new Class[0];
    }
}
