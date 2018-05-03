// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.Attributes;

public class SetPropertyRule extends Rule
{
    protected String name;
    protected String value;
    
    public SetPropertyRule(final Digester digester, final String name, final String value) {
        this(name, value);
    }
    
    public SetPropertyRule(final String name, final String value) {
        this.name = null;
        this.value = null;
        this.name = name;
        this.value = value;
    }
    
    @Override
    public void begin(final String namespace, final String theName, final Attributes attributes) throws Exception {
        String actualName = null;
        String actualValue = null;
        for (int i = 0; i < attributes.getLength(); ++i) {
            String name = attributes.getLocalName(i);
            if ("".equals(name)) {
                name = attributes.getQName(i);
            }
            final String value = attributes.getValue(i);
            if (name.equals(this.name)) {
                actualName = value;
            }
            else if (name.equals(this.value)) {
                actualValue = value;
            }
        }
        final Object top = this.digester.peek();
        if (this.digester.log.isDebugEnabled()) {
            this.digester.log.debug((Object)("[SetPropertyRule]{" + this.digester.match + "} Set " + top.getClass().getName() + " property " + actualName + " to " + actualValue));
        }
        if (!this.digester.isFakeAttribute(top, actualName) && !IntrospectionUtils.setProperty(top, actualName, actualValue) && this.digester.getRulesValidation()) {
            this.digester.log.warn((Object)("[SetPropertyRule]{" + this.digester.match + "} Setting property '" + this.name + "' to '" + this.value + "' did not find a matching property."));
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetPropertyRule[");
        sb.append("name=");
        sb.append(this.name);
        sb.append(", value=");
        sb.append(this.value);
        sb.append("]");
        return sb.toString();
    }
}
