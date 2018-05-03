// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.xml.sax.Attributes;

public interface ObjectCreationFactory
{
    Object createObject(final Attributes p0) throws Exception;
    
    Digester getDigester();
    
    void setDigester(final Digester p0);
}
