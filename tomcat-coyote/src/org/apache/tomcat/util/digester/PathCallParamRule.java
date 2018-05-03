// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.xml.sax.Attributes;

public class PathCallParamRule extends Rule
{
    protected int paramIndex;
    
    public PathCallParamRule(final int paramIndex) {
        this.paramIndex = 0;
        this.paramIndex = paramIndex;
    }
    
    @Override
    public void begin(final String namespace, final String name, final Attributes attributes) throws Exception {
        final String param = this.getDigester().getMatch();
        if (param != null) {
            final Object[] parameters = (Object[])this.digester.peekParams();
            parameters[this.paramIndex] = param;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PathCallParamRule[");
        sb.append("paramIndex=");
        sb.append(this.paramIndex);
        sb.append("]");
        return sb.toString();
    }
}
