// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import java.util.List;

public interface Rules
{
    Digester getDigester();
    
    void setDigester(final Digester p0);
    
    String getNamespaceURI();
    
    void setNamespaceURI(final String p0);
    
    void add(final String p0, final Rule p1);
    
    void clear();
    
    List<Rule> match(final String p0, final String p1);
    
    List<Rule> rules();
}
