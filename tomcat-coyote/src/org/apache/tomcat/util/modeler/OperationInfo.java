// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler;

import javax.management.MBeanParameterInfo;
import javax.management.MBeanOperationInfo;
import java.util.Locale;

public class OperationInfo extends FeatureInfo
{
    static final long serialVersionUID = 4418342922072614875L;
    protected String impact;
    protected String role;
    protected ParameterInfo[] parameters;
    
    public OperationInfo() {
        this.impact = "UNKNOWN";
        this.role = "operation";
        this.parameters = new ParameterInfo[0];
    }
    
    public String getImpact() {
        return this.impact;
    }
    
    public void setImpact(final String impact) {
        if (impact == null) {
            this.impact = null;
        }
        else {
            this.impact = impact.toUpperCase(Locale.ENGLISH);
        }
    }
    
    public String getRole() {
        return this.role;
    }
    
    public void setRole(final String role) {
        this.role = role;
    }
    
    public String getReturnType() {
        if (this.type == null) {
            this.type = "void";
        }
        return this.type;
    }
    
    public void setReturnType(final String returnType) {
        this.type = returnType;
    }
    
    public ParameterInfo[] getSignature() {
        return this.parameters;
    }
    
    public void addParameter(final ParameterInfo parameter) {
        synchronized (this.parameters) {
            final ParameterInfo[] results = new ParameterInfo[this.parameters.length + 1];
            System.arraycopy(this.parameters, 0, results, 0, this.parameters.length);
            results[this.parameters.length] = parameter;
            this.parameters = results;
            this.info = null;
        }
    }
    
    MBeanOperationInfo createOperationInfo() {
        if (this.info == null) {
            int impact = 3;
            if ("ACTION".equals(this.getImpact())) {
                impact = 1;
            }
            else if ("ACTION_INFO".equals(this.getImpact())) {
                impact = 2;
            }
            else if ("INFO".equals(this.getImpact())) {
                impact = 0;
            }
            this.info = new MBeanOperationInfo(this.getName(), this.getDescription(), this.getMBeanParameterInfo(), this.getReturnType(), impact);
        }
        return (MBeanOperationInfo)this.info;
    }
    
    protected MBeanParameterInfo[] getMBeanParameterInfo() {
        final ParameterInfo[] params = this.getSignature();
        final MBeanParameterInfo[] parameters = new MBeanParameterInfo[params.length];
        for (int i = 0; i < params.length; ++i) {
            parameters[i] = params[i].createParameterInfo();
        }
        return parameters;
    }
}
