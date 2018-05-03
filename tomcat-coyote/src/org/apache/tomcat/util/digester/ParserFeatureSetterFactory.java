// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.util.Properties;

public class ParserFeatureSetterFactory
{
    private static boolean isXercesUsed;
    
    public static SAXParser newSAXParser(final Properties properties) throws ParserConfigurationException, SAXException, SAXNotRecognizedException, SAXNotSupportedException {
        if (ParserFeatureSetterFactory.isXercesUsed) {
            return XercesParser.newSAXParser(properties);
        }
        return GenericParser.newSAXParser(properties);
    }
    
    static {
        try {
            Class.forName("org.apache.xerces.impl.Version");
            ParserFeatureSetterFactory.isXercesUsed = true;
        }
        catch (Exception ex) {
            ParserFeatureSetterFactory.isXercesUsed = false;
        }
    }
}
