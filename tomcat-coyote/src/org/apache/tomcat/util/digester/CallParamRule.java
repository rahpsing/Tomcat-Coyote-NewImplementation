// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.xml.sax.Attributes;

public class CallParamRule extends Rule
{
    protected String attributeName;
    protected int paramIndex;
    protected boolean fromStack;
    protected int stackIndex;
    protected ArrayStack<String> bodyTextStack;
    
    public CallParamRule(final int paramIndex) {
        this(paramIndex, null);
    }
    
    public CallParamRule(final int paramIndex, final String attributeName) {
        this.attributeName = null;
        this.paramIndex = 0;
        this.fromStack = false;
        this.stackIndex = 0;
        this.paramIndex = paramIndex;
        this.attributeName = attributeName;
    }
    
    public CallParamRule(final int paramIndex, final boolean fromStack) {
        this.attributeName = null;
        this.paramIndex = 0;
        this.fromStack = false;
        this.stackIndex = 0;
        this.paramIndex = paramIndex;
        this.fromStack = fromStack;
    }
    
    public CallParamRule(final int paramIndex, final int stackIndex) {
        this.attributeName = null;
        this.paramIndex = 0;
        this.fromStack = false;
        this.stackIndex = 0;
        this.paramIndex = paramIndex;
        this.fromStack = true;
        this.stackIndex = stackIndex;
    }
    
    @Override
    public void begin(final String namespace, final String name, final Attributes attributes) throws Exception {
        Object param = null;
        if (this.attributeName != null) {
            param = attributes.getValue(this.attributeName);
        }
        else if (this.fromStack) {
            param = this.digester.peek(this.stackIndex);
            if (this.digester.log.isDebugEnabled()) {
                final StringBuilder sb = new StringBuilder("[CallParamRule]{");
                sb.append(this.digester.match);
                sb.append("} Save from stack; from stack?").append(this.fromStack);
                sb.append("; object=").append(param);
                this.digester.log.debug((Object)sb.toString());
            }
        }
        if (param != null) {
            final Object[] parameters = (Object[])this.digester.peekParams();
            parameters[this.paramIndex] = param;
        }
    }
    
    @Override
    public void body(final String namespace, final String name, final String bodyText) throws Exception {
        if (this.attributeName == null && !this.fromStack) {
            if (this.bodyTextStack == null) {
                this.bodyTextStack = new ArrayStack<String>();
            }
            this.bodyTextStack.push(bodyText.trim());
        }
    }
    
    @Override
    public void end(final String namespace, final String name) {
        if (this.bodyTextStack != null && !this.bodyTextStack.empty()) {
            final Object[] parameters = (Object[])this.digester.peekParams();
            parameters[this.paramIndex] = this.bodyTextStack.pop();
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallParamRule[");
        sb.append("paramIndex=");
        sb.append(this.paramIndex);
        sb.append(", attributeName=");
        sb.append(this.attributeName);
        sb.append(", from stack=");
        sb.append(this.fromStack);
        sb.append("]");
        return sb.toString();
    }
}
