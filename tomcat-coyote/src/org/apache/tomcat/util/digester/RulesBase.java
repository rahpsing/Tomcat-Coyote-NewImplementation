// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class RulesBase implements Rules
{
    protected HashMap<String, List<Rule>> cache;
    protected Digester digester;
    protected String namespaceURI;
    protected ArrayList<Rule> rules;
    
    public RulesBase() {
        this.cache = new HashMap<String, List<Rule>>();
        this.digester = null;
        this.namespaceURI = null;
        this.rules = new ArrayList<Rule>();
    }
    
    @Override
    public Digester getDigester() {
        return this.digester;
    }
    
    @Override
    public void setDigester(final Digester digester) {
        this.digester = digester;
        for (final Rule item : this.rules) {
            item.setDigester(digester);
        }
    }
    
    @Override
    public String getNamespaceURI() {
        return this.namespaceURI;
    }
    
    @Override
    public void setNamespaceURI(final String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }
    
    @Override
    public void add(String pattern, final Rule rule) {
        final int patternLength = pattern.length();
        if (patternLength > 1 && pattern.endsWith("/")) {
            pattern = pattern.substring(0, patternLength - 1);
        }
        List<Rule> list = this.cache.get(pattern);
        if (list == null) {
            list = new ArrayList<Rule>();
            this.cache.put(pattern, list);
        }
        list.add(rule);
        this.rules.add(rule);
        if (this.digester != null) {
            rule.setDigester(this.digester);
        }
        if (this.namespaceURI != null) {
            rule.setNamespaceURI(this.namespaceURI);
        }
    }
    
    @Override
    public void clear() {
        this.cache.clear();
        this.rules.clear();
    }
    
    @Override
    public List<Rule> match(final String namespaceURI, final String pattern) {
        List<Rule> rulesList = this.lookup(namespaceURI, pattern);
        if (rulesList == null || rulesList.size() < 1) {
            String longKey = "";
            for (final String key : this.cache.keySet()) {
                if (key.startsWith("*/") && (pattern.equals(key.substring(2)) || pattern.endsWith(key.substring(1))) && key.length() > longKey.length()) {
                    rulesList = this.lookup(namespaceURI, key);
                    longKey = key;
                }
            }
        }
        if (rulesList == null) {
            rulesList = new ArrayList<Rule>();
        }
        return rulesList;
    }
    
    @Override
    public List<Rule> rules() {
        return this.rules;
    }
    
    protected List<Rule> lookup(final String namespaceURI, final String pattern) {
        final List<Rule> list = this.cache.get(pattern);
        if (list == null) {
            return null;
        }
        if (namespaceURI == null || namespaceURI.length() == 0) {
            return list;
        }
        final ArrayList<Rule> results = new ArrayList<Rule>();
        for (final Rule item : list) {
            if (namespaceURI.equals(item.getNamespaceURI()) || item.getNamespaceURI() == null) {
                results.add(item);
            }
        }
        return results;
    }
}
