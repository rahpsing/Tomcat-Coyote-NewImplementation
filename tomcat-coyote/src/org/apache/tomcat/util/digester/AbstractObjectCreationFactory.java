// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.xml.sax.Attributes;

public abstract class AbstractObjectCreationFactory implements ObjectCreationFactory
{
    private Digester digester;
    
    public AbstractObjectCreationFactory() {
        this.digester = null;
    }
    
    @Override
    public abstract Object createObject(final Attributes p0) throws Exception;
    
    @Override
    public Digester getDigester() {
        return this.digester;
    }
    
    @Override
    public void setDigester(final Digester digester) {
        this.digester = digester;
    }
}
