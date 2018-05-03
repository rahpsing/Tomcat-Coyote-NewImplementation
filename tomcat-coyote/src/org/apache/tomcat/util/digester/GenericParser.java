// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import java.util.Hashtable;
import org.apache.juli.logging.LogFactory;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXNotRecognizedException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.util.Properties;
import org.apache.juli.logging.Log;

public class GenericParser
{
    private static final Log log;
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    protected static String JAXP_SCHEMA_LANGUAGE;
    
    public static SAXParser newSAXParser(final Properties properties) throws ParserConfigurationException, SAXException, SAXNotRecognizedException {
        final SAXParserFactory factory = ((Hashtable<K, SAXParserFactory>)properties).get("SAXParserFactory");
        final SAXParser parser = factory.newSAXParser();
        final String schemaLocation = ((Hashtable<K, String>)properties).get("schemaLocation");
        final String schemaLanguage = ((Hashtable<K, String>)properties).get("schemaLanguage");
        try {
            if (schemaLocation != null) {
                parser.setProperty(GenericParser.JAXP_SCHEMA_LANGUAGE, schemaLanguage);
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", schemaLocation);
            }
        }
        catch (SAXNotRecognizedException e) {
            GenericParser.log.info((Object)(parser.getClass().getName() + ": " + e.getMessage() + " not supported."));
        }
        return parser;
    }
    
    static {
        log = LogFactory.getLog("org.apache.tomcat.util.digester.Digester.sax");
        GenericParser.JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    }
}
