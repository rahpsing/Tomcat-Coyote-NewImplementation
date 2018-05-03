// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.apache.tomcat.util.IntrospectionUtils;

public class SetTopRule extends Rule
{
    protected String methodName;
    protected String paramType;
    protected boolean useExactMatch;
    
    public SetTopRule(final Digester digester, final String methodName) {
        this(methodName);
    }
    
    public SetTopRule(final Digester digester, final String methodName, final String paramType) {
        this(methodName, paramType);
    }
    
    public SetTopRule(final String methodName) {
        this(methodName, null);
    }
    
    public SetTopRule(final String methodName, final String paramType) {
        this.methodName = null;
        this.paramType = null;
        this.useExactMatch = false;
        this.methodName = methodName;
        this.paramType = paramType;
    }
    
    public boolean isExactMatch() {
        return this.useExactMatch;
    }
    
    public void setExactMatch(final boolean useExactMatch) {
        this.useExactMatch = useExactMatch;
    }
    
    @Override
    public void end(final String namespace, final String name) throws Exception {
        final Object child = this.digester.peek(0);
        final Object parent = this.digester.peek(1);
        if (this.digester.log.isDebugEnabled()) {
            if (child == null) {
                this.digester.log.debug((Object)("[SetTopRule]{" + this.digester.match + "} Call [NULL CHILD]." + this.methodName + "(" + parent + ")"));
            }
            else {
                this.digester.log.debug((Object)("[SetTopRule]{" + this.digester.match + "} Call " + child.getClass().getName() + "." + this.methodName + "(" + parent + ")"));
            }
        }
        IntrospectionUtils.callMethod1(child, this.methodName, parent, this.paramType, this.digester.getClassLoader());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetTopRule[");
        sb.append("methodName=");
        sb.append(this.methodName);
        sb.append(", paramType=");
        sb.append(this.paramType);
        sb.append("]");
        return sb.toString();
    }
}
