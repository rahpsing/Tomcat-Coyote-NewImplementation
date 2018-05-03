// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class WithDefaultsRulesWrapper implements Rules
{
    private Rules wrappedRules;
    private List<Rule> defaultRules;
    private List<Rule> allRules;
    
    public WithDefaultsRulesWrapper(final Rules wrappedRules) {
        this.defaultRules = new ArrayList<Rule>();
        this.allRules = new ArrayList<Rule>();
        if (wrappedRules == null) {
            throw new IllegalArgumentException("Wrapped rules must not be null");
        }
        this.wrappedRules = wrappedRules;
    }
    
    @Override
    public Digester getDigester() {
        return this.wrappedRules.getDigester();
    }
    
    @Override
    public void setDigester(final Digester digester) {
        this.wrappedRules.setDigester(digester);
        for (final Rule rule : this.defaultRules) {
            rule.setDigester(digester);
        }
    }
    
    @Override
    public String getNamespaceURI() {
        return this.wrappedRules.getNamespaceURI();
    }
    
    @Override
    public void setNamespaceURI(final String namespaceURI) {
        this.wrappedRules.setNamespaceURI(namespaceURI);
    }
    
    public List<Rule> getDefaults() {
        return this.defaultRules;
    }
    
    @Override
    public List<Rule> match(final String namespaceURI, final String pattern) {
        final List<Rule> matches = this.wrappedRules.match(namespaceURI, pattern);
        if (matches == null || matches.isEmpty()) {
            return new ArrayList<Rule>(this.defaultRules);
        }
        return matches;
    }
    
    public void addDefault(final Rule rule) {
        if (this.wrappedRules.getDigester() != null) {
            rule.setDigester(this.wrappedRules.getDigester());
        }
        if (this.wrappedRules.getNamespaceURI() != null) {
            rule.setNamespaceURI(this.wrappedRules.getNamespaceURI());
        }
        this.defaultRules.add(rule);
        this.allRules.add(rule);
    }
    
    @Override
    public List<Rule> rules() {
        return this.allRules;
    }
    
    @Override
    public void clear() {
        this.wrappedRules.clear();
        this.allRules.clear();
        this.defaultRules.clear();
    }
    
    @Override
    public void add(final String pattern, final Rule rule) {
        this.wrappedRules.add(pattern, rule);
        this.allRules.add(rule);
    }
}
