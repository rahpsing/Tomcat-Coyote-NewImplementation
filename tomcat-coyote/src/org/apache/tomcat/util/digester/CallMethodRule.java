// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.xml.sax.SAXException;
import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.Attributes;

public class CallMethodRule extends Rule
{
    protected String bodyText;
    protected int targetOffset;
    protected String methodName;
    protected int paramCount;
    protected Class<?>[] paramTypes;
    protected String[] paramClassNames;
    protected boolean useExactMatch;
    
    public CallMethodRule(final String methodName, final int paramCount) {
        this(0, methodName, paramCount);
    }
    
    public CallMethodRule(final int targetOffset, final String methodName, final int paramCount) {
        this.bodyText = null;
        this.targetOffset = 0;
        this.methodName = null;
        this.paramCount = 0;
        this.paramTypes = null;
        this.paramClassNames = null;
        this.useExactMatch = false;
        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;
        if (paramCount == 0) {
            this.paramTypes = (Class<?>[])new Class[] { String.class };
        }
        else {
            this.paramTypes = (Class<?>[])new Class[paramCount];
            for (int i = 0; i < this.paramTypes.length; ++i) {
                this.paramTypes[i] = String.class;
            }
        }
    }
    
    public CallMethodRule(final String methodName) {
        this(0, methodName, 0, (Class<?>[])null);
    }
    
    public CallMethodRule(final int targetOffset, final String methodName) {
        this(targetOffset, methodName, 0, (Class<?>[])null);
    }
    
    public CallMethodRule(final String methodName, final int paramCount, final String[] paramTypes) {
        this(0, methodName, paramCount, paramTypes);
    }
    
    public CallMethodRule(final int targetOffset, final String methodName, final int paramCount, final String[] paramTypes) {
        this.bodyText = null;
        this.targetOffset = 0;
        this.methodName = null;
        this.paramCount = 0;
        this.paramTypes = null;
        this.paramClassNames = null;
        this.useExactMatch = false;
        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;
        if (paramTypes == null) {
            this.paramTypes = (Class<?>[])new Class[paramCount];
            for (int i = 0; i < this.paramTypes.length; ++i) {
                this.paramTypes[i] = "abc".getClass();
            }
        }
        else {
            this.paramClassNames = new String[paramTypes.length];
            for (int i = 0; i < this.paramClassNames.length; ++i) {
                this.paramClassNames[i] = paramTypes[i];
            }
        }
    }
    
    public CallMethodRule(final String methodName, final int paramCount, final Class<?>[] paramTypes) {
        this(0, methodName, paramCount, paramTypes);
    }
    
    public CallMethodRule(final int targetOffset, final String methodName, final int paramCount, final Class<?>[] paramTypes) {
        this.bodyText = null;
        this.targetOffset = 0;
        this.methodName = null;
        this.paramCount = 0;
        this.paramTypes = null;
        this.paramClassNames = null;
        this.useExactMatch = false;
        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;
        if (paramTypes == null) {
            this.paramTypes = (Class<?>[])new Class[paramCount];
            for (int i = 0; i < this.paramTypes.length; ++i) {
                this.paramTypes[i] = "abc".getClass();
            }
        }
        else {
            this.paramTypes = (Class<?>[])new Class[paramTypes.length];
            for (int i = 0; i < this.paramTypes.length; ++i) {
                this.paramTypes[i] = paramTypes[i];
            }
        }
    }
    
    public boolean getUseExactMatch() {
        return this.useExactMatch;
    }
    
    public void setUseExactMatch(final boolean useExactMatch) {
        this.useExactMatch = useExactMatch;
    }
    
    @Override
    public void setDigester(final Digester digester) {
        super.setDigester(digester);
        if (this.paramClassNames != null) {
            this.paramTypes = (Class<?>[])new Class[this.paramClassNames.length];
            for (int i = 0; i < this.paramClassNames.length; ++i) {
                try {
                    this.paramTypes[i] = digester.getClassLoader().loadClass(this.paramClassNames[i]);
                }
                catch (ClassNotFoundException e) {
                    digester.getLogger().error((Object)("(CallMethodRule) Cannot load class " + this.paramClassNames[i]), (Throwable)e);
                    this.paramTypes[i] = null;
                }
            }
        }
    }
    
    @Override
    public void begin(final String namespace, final String name, final Attributes attributes) throws Exception {
        if (this.paramCount > 0) {
            final Object[] parameters = new Object[this.paramCount];
            for (int i = 0; i < parameters.length; ++i) {
                parameters[i] = null;
            }
            this.digester.pushParams(parameters);
        }
    }
    
    @Override
    public void body(final String namespace, final String name, final String bodyText) throws Exception {
        if (this.paramCount == 0) {
            this.bodyText = bodyText.trim();
        }
    }
    
    @Override
    public void end(final String namespace, final String name) throws Exception {
        Object[] parameters = null;
        if (this.paramCount > 0) {
            parameters = (Object[])this.digester.popParams();
            if (this.digester.log.isTraceEnabled()) {
                for (int i = 0, size = parameters.length; i < size; ++i) {
                    this.digester.log.trace((Object)("[CallMethodRule](" + i + ")" + parameters[i]));
                }
            }
            if (this.paramCount == 1 && parameters[0] == null) {
                return;
            }
        }
        else if (this.paramTypes != null && this.paramTypes.length != 0) {
            if (this.bodyText == null) {
                return;
            }
            parameters = new Object[] { this.bodyText };
            if (this.paramTypes.length == 0) {
                (this.paramTypes = (Class<?>[])new Class[1])[0] = "abc".getClass();
            }
        }
        final Object[] paramValues = new Object[this.paramTypes.length];
        for (int j = 0; j < this.paramTypes.length; ++j) {
            if (parameters[j] == null || (parameters[j] instanceof String && !String.class.isAssignableFrom(this.paramTypes[j]))) {
                paramValues[j] = IntrospectionUtils.convert((String)parameters[j], this.paramTypes[j]);
            }
            else {
                paramValues[j] = parameters[j];
            }
        }
        Object target;
        if (this.targetOffset >= 0) {
            target = this.digester.peek(this.targetOffset);
        }
        else {
            target = this.digester.peek(this.digester.getCount() + this.targetOffset);
        }
        if (target == null) {
            final StringBuilder sb = new StringBuilder();
            sb.append("[CallMethodRule]{");
            sb.append(this.digester.match);
            sb.append("} Call target is null (");
            sb.append("targetOffset=");
            sb.append(this.targetOffset);
            sb.append(",stackdepth=");
            sb.append(this.digester.getCount());
            sb.append(")");
            throw new SAXException(sb.toString());
        }
        if (this.digester.log.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder("[CallMethodRule]{");
            sb.append(this.digester.match);
            sb.append("} Call ");
            sb.append(target.getClass().getName());
            sb.append(".");
            sb.append(this.methodName);
            sb.append("(");
            for (int k = 0; k < paramValues.length; ++k) {
                if (k > 0) {
                    sb.append(",");
                }
                if (paramValues[k] == null) {
                    sb.append("null");
                }
                else {
                    sb.append(paramValues[k].toString());
                }
                sb.append("/");
                if (this.paramTypes[k] == null) {
                    sb.append("null");
                }
                else {
                    sb.append(this.paramTypes[k].getName());
                }
            }
            sb.append(")");
            this.digester.log.debug((Object)sb.toString());
        }
        final Object result = IntrospectionUtils.callMethodN(target, this.methodName, paramValues, this.paramTypes);
        this.processMethodCallResult(result);
    }
    
    @Override
    public void finish() throws Exception {
        this.bodyText = null;
    }
    
    protected void processMethodCallResult(final Object result) {
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallMethodRule[");
        sb.append("methodName=");
        sb.append(this.methodName);
        sb.append(", paramCount=");
        sb.append(this.paramCount);
        sb.append(", paramTypes={");
        if (this.paramTypes != null) {
            for (int i = 0; i < this.paramTypes.length; ++i) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(this.paramTypes[i].getName());
            }
        }
        sb.append("}");
        sb.append("]");
        return sb.toString();
    }
}
