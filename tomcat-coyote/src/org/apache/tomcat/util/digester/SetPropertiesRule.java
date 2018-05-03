// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.Attributes;

public class SetPropertiesRule extends Rule
{
    private String[] attributeNames;
    private String[] propertyNames;
    
    public SetPropertiesRule(final Digester digester) {
        this();
    }
    
    public SetPropertiesRule() {
    }
    
    public SetPropertiesRule(final String attributeName, final String propertyName) {
        (this.attributeNames = new String[1])[0] = attributeName;
        (this.propertyNames = new String[1])[0] = propertyName;
    }
    
    public SetPropertiesRule(final String[] attributeNames, final String[] propertyNames) {
        this.attributeNames = new String[attributeNames.length];
        for (int i = 0, size = attributeNames.length; i < size; ++i) {
            this.attributeNames[i] = attributeNames[i];
        }
        this.propertyNames = new String[propertyNames.length];
        for (int i = 0, size = propertyNames.length; i < size; ++i) {
            this.propertyNames[i] = propertyNames[i];
        }
    }
    
    @Override
    public void begin(final String namespace, final String theName, final Attributes attributes) throws Exception {
        final Object top = this.digester.peek();
        if (this.digester.log.isDebugEnabled()) {
            if (top != null) {
                this.digester.log.debug((Object)("[SetPropertiesRule]{" + this.digester.match + "} Set " + top.getClass().getName() + " properties"));
            }
            else {
                this.digester.log.debug((Object)("[SetPropertiesRule]{" + this.digester.match + "} Set NULL properties"));
            }
        }
        int attNamesLength = 0;
        if (this.attributeNames != null) {
            attNamesLength = this.attributeNames.length;
        }
        int propNamesLength = 0;
        if (this.propertyNames != null) {
            propNamesLength = this.propertyNames.length;
        }
        for (int i = 0; i < attributes.getLength(); ++i) {
            String name = attributes.getLocalName(i);
            if ("".equals(name)) {
                name = attributes.getQName(i);
            }
            final String value = attributes.getValue(i);
            int n = 0;
            while (n < attNamesLength) {
                if (name.equals(this.attributeNames[n])) {
                    if (n < propNamesLength) {
                        name = this.propertyNames[n];
                        break;
                    }
                    name = null;
                    break;
                }
                else {
                    ++n;
                }
            }
            if (this.digester.log.isDebugEnabled()) {
                this.digester.log.debug((Object)("[SetPropertiesRule]{" + this.digester.match + "} Setting property '" + name + "' to '" + value + "'"));
            }
            if (!this.digester.isFakeAttribute(top, name) && !IntrospectionUtils.setProperty(top, name, value) && this.digester.getRulesValidation()) {
                this.digester.log.warn((Object)("[SetPropertiesRule]{" + this.digester.match + "} Setting property '" + name + "' to '" + value + "' did not find a matching property."));
            }
        }
    }
    
    public void addAlias(final String attributeName, final String propertyName) {
        if (this.attributeNames == null) {
            (this.attributeNames = new String[1])[0] = attributeName;
            (this.propertyNames = new String[1])[0] = propertyName;
        }
        else {
            final int length = this.attributeNames.length;
            final String[] tempAttributes = new String[length + 1];
            for (int i = 0; i < length; ++i) {
                tempAttributes[i] = this.attributeNames[i];
            }
            tempAttributes[length] = attributeName;
            final String[] tempProperties = new String[length + 1];
            for (int j = 0; j < length && j < this.propertyNames.length; ++j) {
                tempProperties[j] = this.propertyNames[j];
            }
            tempProperties[length] = propertyName;
            this.propertyNames = tempProperties;
            this.attributeNames = tempAttributes;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetPropertiesRule[");
        sb.append("]");
        return sb.toString();
    }
}
