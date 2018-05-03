// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.xml.sax.Attributes;

public class ObjectCreateRule extends Rule
{
    protected String attributeName;
    protected String className;
    
    public ObjectCreateRule(final String className) {
        this(className, (String)null);
    }
    
    public ObjectCreateRule(final Class<?> clazz) {
        this(clazz.getName(), (String)null);
    }
    
    public ObjectCreateRule(final String className, final String attributeName) {
        this.attributeName = null;
        this.className = null;
        this.className = className;
        this.attributeName = attributeName;
    }
    
    public ObjectCreateRule(final String attributeName, final Class<?> clazz) {
        this(clazz.getName(), attributeName);
    }
    
    @Override
    public void begin(final String namespace, final String name, final Attributes attributes) throws Exception {
        String realClassName = this.className;
        if (this.attributeName != null) {
            final String value = attributes.getValue(this.attributeName);
            if (value != null) {
                realClassName = value;
            }
        }
        if (this.digester.log.isDebugEnabled()) {
            this.digester.log.debug((Object)("[ObjectCreateRule]{" + this.digester.match + "}New " + realClassName));
        }
        if (realClassName == null) {
            throw new NullPointerException("No class name specified for " + namespace + " " + name);
        }
        final Class<?> clazz = this.digester.getClassLoader().loadClass(realClassName);
        final Object instance = clazz.newInstance();
        this.digester.push(instance);
    }
    
    @Override
    public void end(final String namespace, final String name) throws Exception {
        final Object top = this.digester.pop();
        if (this.digester.log.isDebugEnabled()) {
            this.digester.log.debug((Object)("[ObjectCreateRule]{" + this.digester.match + "} Pop " + top.getClass().getName()));
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ObjectCreateRule[");
        sb.append("className=");
        sb.append(this.className);
        sb.append(", attributeName=");
        sb.append(this.attributeName);
        sb.append("]");
        return sb.toString();
    }
}
