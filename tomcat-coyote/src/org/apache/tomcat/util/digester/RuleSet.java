// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

public interface RuleSet
{
    String getNamespaceURI();
    
    void addRuleInstances(final Digester p0);
}
