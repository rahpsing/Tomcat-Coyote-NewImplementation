// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.digester;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;

public class NodeCreateRule extends Rule
{
    private DocumentBuilder documentBuilder;
    private int nodeType;
    
    public NodeCreateRule() throws ParserConfigurationException {
        this(1);
    }
    
    public NodeCreateRule(final DocumentBuilder documentBuilder) {
        this(1, documentBuilder);
    }
    
    public NodeCreateRule(final int nodeType) throws ParserConfigurationException {
        this(nodeType, DocumentBuilderFactory.newInstance().newDocumentBuilder());
    }
    
    public NodeCreateRule(final int nodeType, final DocumentBuilder documentBuilder) {
        this.documentBuilder = null;
        this.nodeType = 1;
        if (nodeType != 11 && nodeType != 1) {
            throw new IllegalArgumentException("Can only create nodes of type DocumentFragment and Element");
        }
        this.nodeType = nodeType;
        this.documentBuilder = documentBuilder;
    }
    
    @Override
    public void begin(final String namespaceURI, final String name, final Attributes attributes) throws Exception {
        final XMLReader xmlReader = this.getDigester().getXMLReader();
        final Document doc = this.documentBuilder.newDocument();
        NodeBuilder builder = null;
        if (this.nodeType == 1) {
            Element element = null;
            if (this.getDigester().getNamespaceAware()) {
                element = doc.createElementNS(namespaceURI, name);
                for (int i = 0; i < attributes.getLength(); ++i) {
                    element.setAttributeNS(attributes.getURI(i), attributes.getLocalName(i), attributes.getValue(i));
                }
            }
            else {
                element = doc.createElement(name);
                for (int i = 0; i < attributes.getLength(); ++i) {
                    element.setAttribute(attributes.getQName(i), attributes.getValue(i));
                }
            }
            builder = new NodeBuilder(doc, element);
        }
        else {
            builder = new NodeBuilder(doc, doc.createDocumentFragment());
        }
        xmlReader.setContentHandler(builder);
    }
    
    @Override
    public void end(final String namespace, final String name) throws Exception {
        this.digester.pop();
    }
    
    private class NodeBuilder extends DefaultHandler
    {
        protected ContentHandler oldContentHandler;
        protected int depth;
        protected Document doc;
        protected Node root;
        protected Node top;
        
        public NodeBuilder(final Document doc, final Node root) throws ParserConfigurationException, SAXException {
            this.oldContentHandler = null;
            this.depth = 0;
            this.doc = null;
            this.root = null;
            this.top = null;
            this.doc = doc;
            this.root = root;
            this.top = root;
            this.oldContentHandler = NodeCreateRule.this.digester.getXMLReader().getContentHandler();
        }
        
        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            try {
                final String str = new String(ch, start, length);
                if (str.trim().length() > 0) {
                    this.top.appendChild(this.doc.createTextNode(str));
                }
            }
            catch (DOMException e) {
                throw new SAXException(e.getMessage(), e);
            }
        }
        
        @Override
        public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
            try {
                if (this.depth == 0) {
                    NodeCreateRule.this.getDigester().getXMLReader().setContentHandler(this.oldContentHandler);
                    NodeCreateRule.this.getDigester().push(this.root);
                    NodeCreateRule.this.getDigester().endElement(namespaceURI, localName, qName);
                }
                this.top = this.top.getParentNode();
                --this.depth;
            }
            catch (DOMException e) {
                throw new SAXException(e.getMessage(), e);
            }
        }
        
        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {
            try {
                this.top.appendChild(this.doc.createProcessingInstruction(target, data));
            }
            catch (DOMException e) {
                throw new SAXException(e.getMessage(), e);
            }
        }
        
        @Override
        public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) throws SAXException {
            try {
                final Node previousTop = this.top;
                if (localName == null || localName.length() == 0) {
                    this.top = this.doc.createElement(qName);
                }
                else {
                    this.top = this.doc.createElementNS(namespaceURI, localName);
                }
                for (int i = 0; i < atts.getLength(); ++i) {
                    Attr attr = null;
                    if (atts.getLocalName(i) == null || atts.getLocalName(i).length() == 0) {
                        attr = this.doc.createAttribute(atts.getQName(i));
                        attr.setNodeValue(atts.getValue(i));
                        ((Element)this.top).setAttributeNode(attr);
                    }
                    else {
                        attr = this.doc.createAttributeNS(atts.getURI(i), atts.getLocalName(i));
                        attr.setNodeValue(atts.getValue(i));
                        ((Element)this.top).setAttributeNodeNS(attr);
                    }
                }
                previousTop.appendChild(this.top);
                ++this.depth;
            }
            catch (DOMException e) {
                throw new SAXException(e.getMessage(), e);
            }
        }
    }
}
