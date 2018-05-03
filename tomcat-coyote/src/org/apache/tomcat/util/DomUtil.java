// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util;

import java.io.Reader;
import java.io.StringReader;
import org.xml.sax.InputSource;
import org.apache.juli.logging.LogFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Transformer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.TransformerFactory;
import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.EntityResolver;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import java.io.InputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.apache.juli.logging.Log;

@Deprecated
public class DomUtil
{
    private static final Log log;
    
    public static String getContent(final Node n) {
        if (n == null) {
            return null;
        }
        final Node n2 = getChild(n, 3);
        if (n2 == null) {
            return null;
        }
        final String s1 = n2.getNodeValue();
        return s1.trim();
    }
    
    public static Node getChild(final Node parent, final String name) {
        if (parent == null) {
            return null;
        }
        final Node first = parent.getFirstChild();
        if (first == null) {
            return null;
        }
        for (Node node = first; node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1) {
                if (name != null && name.equals(node.getNodeName())) {
                    return node;
                }
                if (name == null) {
                    return node;
                }
            }
        }
        return null;
    }
    
    public static String getAttribute(final Node element, final String attName) {
        final NamedNodeMap attrs = element.getAttributes();
        if (attrs == null) {
            return null;
        }
        final Node attN = attrs.getNamedItem(attName);
        if (attN == null) {
            return null;
        }
        return attN.getNodeValue();
    }
    
    public static void setAttribute(final Node node, final String attName, final String val) {
        final NamedNodeMap attributes = node.getAttributes();
        final Node attNode = node.getOwnerDocument().createAttribute(attName);
        attNode.setNodeValue(val);
        attributes.setNamedItem(attNode);
    }
    
    public static void removeAttribute(final Node node, final String attName) {
        final NamedNodeMap attributes = node.getAttributes();
        attributes.removeNamedItem(attName);
    }
    
    public static void setText(final Node node, final String val) {
        final Node chld = getChild(node, 3);
        if (chld == null) {
            final Node textN = node.getOwnerDocument().createTextNode(val);
            node.appendChild(textN);
            return;
        }
        chld.setNodeValue(val);
    }
    
    public static Node findChildWithAtt(final Node parent, final String elemName, final String attName, final String attVal) {
        Node child = getChild(parent, 1);
        if (attVal == null) {
            while (child != null && (elemName == null || elemName.equals(child.getNodeName())) && getAttribute(child, attName) != null) {
                child = getNext(child, elemName, 1);
            }
        }
        else {
            while (child != null && (elemName == null || elemName.equals(child.getNodeName())) && !attVal.equals(getAttribute(child, attName))) {
                child = getNext(child, elemName, 1);
            }
        }
        return child;
    }
    
    public static String getChildContent(final Node parent, final String name) {
        final Node first = parent.getFirstChild();
        if (first == null) {
            return null;
        }
        for (Node node = first; node != null; node = node.getNextSibling()) {
            if (name.equals(node.getNodeName())) {
                return getContent(node);
            }
        }
        return null;
    }
    
    public static Node getChild(final Node parent, final int type) {
        Node n;
        for (n = parent.getFirstChild(); n != null && type != n.getNodeType(); n = n.getNextSibling()) {}
        if (n == null) {
            return null;
        }
        return n;
    }
    
    public static Node getNext(final Node current) {
        final String name = current.getNodeName();
        final int type = current.getNodeType();
        return getNext(current, name, type);
    }
    
    public static Node getNext(final Node current, final String name, final int type) {
        final Node first = current.getNextSibling();
        if (first == null) {
            return null;
        }
        for (Node node = first; node != null; node = node.getNextSibling()) {
            if (type < 0 || node.getNodeType() == type) {
                if (name == null) {
                    return node;
                }
                if (name.equals(node.getNodeName())) {
                    return node;
                }
            }
        }
        return null;
    }
    
    public static void setAttributes(final Object o, final Node parent) {
        final NamedNodeMap attrs = parent.getAttributes();
        if (attrs == null) {
            return;
        }
        for (int i = 0; i < attrs.getLength(); ++i) {
            final Node n = attrs.item(i);
            final String name = n.getNodeName();
            final String value = n.getNodeValue();
            if (DomUtil.log.isTraceEnabled()) {
                DomUtil.log.trace((Object)("Attribute " + parent.getNodeName() + " " + name + "=" + value));
            }
            try {
                IntrospectionUtils.setProperty(o, name, value);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static Document readXml(final InputStream is) throws SAXException, IOException, ParserConfigurationException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = null;
        db = dbf.newDocumentBuilder();
        db.setEntityResolver(new NullResolver());
        final Document doc = db.parse(is);
        return doc;
    }
    
    public static void writeXml(final Node n, final OutputStream os) throws TransformerException {
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer t = tf.newTransformer();
        t.setOutputProperty("indent", "yes");
        t.transform(new DOMSource(n), new StreamResult(os));
    }
    
    static {
        log = LogFactory.getLog((Class)DomUtil.class);
    }
    
    public static class NullResolver implements EntityResolver
    {
        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            if (DomUtil.log.isTraceEnabled()) {
                DomUtil.log.trace((Object)("ResolveEntity: " + publicId + " " + systemId));
            }
            return new InputSource(new StringReader(""));
        }
    }
}
