// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler.modules;

import org.apache.juli.logging.LogFactory;
import javax.management.Attribute;
import java.io.FileNotFoundException;
import javax.xml.transform.TransformerException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import javax.management.MBeanServer;
import java.io.InputStream;
import org.apache.tomcat.util.modeler.AttributeInfo;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.BaseModelMBean;
import javax.management.loading.MLet;
import java.net.URL;
import org.apache.tomcat.util.DomUtil;
import java.util.ArrayList;
import org.w3c.dom.Node;
import java.util.HashMap;
import org.w3c.dom.Document;
import javax.management.ObjectName;
import java.util.List;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.juli.logging.Log;

@Deprecated
public class MbeansSource extends ModelerSource implements MbeansSourceMBean
{
    private static final Log log;
    Registry registry;
    String type;
    boolean loading;
    List<ObjectName> mbeans;
    static boolean loaderLoaded;
    private Document document;
    private HashMap<ObjectName, Node> object2Node;
    long lastUpdate;
    long updateInterval;
    
    public MbeansSource() {
        this.loading = true;
        this.mbeans = new ArrayList<ObjectName>();
        this.object2Node = new HashMap<ObjectName, Node>();
        this.updateInterval = 10000L;
    }
    
    public void setRegistry(final Registry reg) {
        this.registry = reg;
    }
    
    public void setLocation(final String loc) {
        this.location = loc;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    @Override
    public void setSource(final Object source) {
        this.source = source;
    }
    
    @Override
    public Object getSource() {
        return this.source;
    }
    
    public String getLocation() {
        return this.location;
    }
    
    @Override
    public List<ObjectName> getMBeans() {
        return this.mbeans;
    }
    
    @Override
    public List<ObjectName> loadDescriptors(final Registry registry, final String type, final Object source) throws Exception {
        this.setRegistry(registry);
        this.setType(type);
        this.setSource(source);
        this.execute();
        return this.mbeans;
    }
    
    public void start() throws Exception {
        this.registry.invoke(this.mbeans, "start", false);
    }
    
    public void stop() throws Exception {
        this.registry.invoke(this.mbeans, "stop", false);
    }
    
    @Override
    public void init() throws Exception {
        if (this.mbeans == null) {
            this.execute();
        }
        if (this.registry == null) {
            this.registry = Registry.getRegistry(null, null);
        }
        this.registry.invoke(this.mbeans, "init", false);
    }
    
    public void destroy() throws Exception {
        this.registry.invoke(this.mbeans, "destroy", false);
    }
    
    @Override
    public void load() throws Exception {
        this.execute();
    }
    
    public void execute() throws Exception {
        if (this.registry == null) {
            this.registry = Registry.getRegistry(null, null);
        }
        try {
            final InputStream stream = this.getInputStream();
            final long t1 = System.currentTimeMillis();
            this.document = DomUtil.readXml(stream);
            final Node descriptorsN = this.document.getDocumentElement();
            if (descriptorsN == null) {
                MbeansSource.log.error((Object)"No descriptors found");
                return;
            }
            Node firstMbeanN = DomUtil.getChild(descriptorsN, null);
            if (firstMbeanN == null) {
                if (MbeansSource.log.isDebugEnabled()) {
                    MbeansSource.log.debug((Object)("No child " + descriptorsN));
                }
                firstMbeanN = descriptorsN;
            }
            final MBeanServer server = Registry.getRegistry(null, null).getMBeanServer();
            if (!MbeansSource.loaderLoaded) {
                final ObjectName defaultLoader = new ObjectName("modeler", "loader", "modeler");
                final MLet mlet = new MLet(new URL[0], this.getClass().getClassLoader());
                server.registerMBean(mlet, defaultLoader);
                MbeansSource.loaderLoaded = true;
            }
            for (Node mbeanN = firstMbeanN; mbeanN != null; mbeanN = DomUtil.getNext(mbeanN, null, 1)) {
                final String nodeName = mbeanN.getNodeName();
                if ("mbean".equals(nodeName) || "MLET".equals(nodeName)) {
                    final String code = DomUtil.getAttribute(mbeanN, "code");
                    String objectName = DomUtil.getAttribute(mbeanN, "objectName");
                    if (objectName == null) {
                        objectName = DomUtil.getAttribute(mbeanN, "name");
                    }
                    if (MbeansSource.log.isDebugEnabled()) {
                        MbeansSource.log.debug((Object)("Processing mbean objectName=" + objectName + " code=" + code));
                    }
                    Node constructorN = DomUtil.getChild(mbeanN, "constructor");
                    if (constructorN == null) {
                        constructorN = mbeanN;
                    }
                    this.processArg(constructorN);
                    try {
                        final ObjectName oname = new ObjectName(objectName);
                        if (!server.isRegistered(oname)) {
                            final String modelMBean = BaseModelMBean.class.getName();
                            server.createMBean(modelMBean, oname, new Object[] { code, this }, new String[] { String.class.getName(), ModelerSource.class.getName() });
                            this.mbeans.add(oname);
                        }
                        this.object2Node.put(oname, mbeanN);
                    }
                    catch (Exception ex) {
                        MbeansSource.log.error((Object)("Error creating mbean " + objectName), (Throwable)ex);
                    }
                    Node descN;
                    for (Node firstAttN = descN = DomUtil.getChild(mbeanN, "attribute"); descN != null; descN = DomUtil.getNext(descN)) {
                        this.processAttribute(server, descN, objectName);
                    }
                }
                else if ("jmx-operation".equals(nodeName)) {
                    String name = DomUtil.getAttribute(mbeanN, "objectName");
                    if (name == null) {
                        name = DomUtil.getAttribute(mbeanN, "name");
                    }
                    final String operation = DomUtil.getAttribute(mbeanN, "operation");
                    if (MbeansSource.log.isDebugEnabled()) {
                        MbeansSource.log.debug((Object)("Processing invoke objectName=" + name + " code=" + operation));
                    }
                    try {
                        final ObjectName oname2 = new ObjectName(name);
                        this.processArg(mbeanN);
                        server.invoke(oname2, operation, null, null);
                    }
                    catch (Exception e) {
                        MbeansSource.log.error((Object)("Error in invoke " + name + " " + operation));
                    }
                }
                final ManagedBean managed = new ManagedBean();
                DomUtil.setAttributes(managed, mbeanN);
                Node descN2;
                for (Node firstN = descN2 = DomUtil.getChild(mbeanN, "attribute"); descN2 != null; descN2 = DomUtil.getNext(descN2)) {
                    final AttributeInfo ci = new AttributeInfo();
                    DomUtil.setAttributes(ci, descN2);
                    managed.addAttribute(ci);
                }
            }
            final long t2 = System.currentTimeMillis();
            MbeansSource.log.info((Object)("Reading mbeans  " + (t2 - t1)));
            this.loading = false;
        }
        catch (Exception ex2) {
            MbeansSource.log.error((Object)"Error reading mbeans ", (Throwable)ex2);
        }
    }
    
    @Override
    public void updateField(final ObjectName oname, final String name, final Object value) {
        if (this.loading) {
            return;
        }
        final Node n = this.object2Node.get(oname);
        if (n == null) {
            MbeansSource.log.info((Object)("Node not found " + oname));
            return;
        }
        Node attNode = DomUtil.findChildWithAtt(n, "attribute", "name", name);
        if (attNode == null) {
            attNode = n.getOwnerDocument().createElement("attribute");
            DomUtil.setAttribute(attNode, "name", name);
            n.appendChild(attNode);
        }
        final String oldValue = DomUtil.getAttribute(attNode, "value");
        if (oldValue != null) {
            DomUtil.removeAttribute(attNode, "value");
        }
        DomUtil.setText(attNode, value.toString());
    }
    
    @Override
    public void save() {
        final long time = System.currentTimeMillis();
        if (this.location != null && time - this.lastUpdate > this.updateInterval) {
            this.lastUpdate = time;
            try {
                final FileOutputStream fos = new FileOutputStream(this.location);
                DomUtil.writeXml(this.document, fos);
            }
            catch (TransformerException e2) {
                MbeansSource.log.error((Object)"Error writing");
            }
            catch (FileNotFoundException e) {
                MbeansSource.log.error((Object)"Error writing", (Throwable)e);
            }
        }
    }
    
    private void processAttribute(final MBeanServer server, final Node descN, final String objectName) {
        final String attName = DomUtil.getAttribute(descN, "name");
        String value = DomUtil.getAttribute(descN, "value");
        String type = null;
        if (value == null) {
            value = DomUtil.getContent(descN);
        }
        try {
            if (MbeansSource.log.isDebugEnabled()) {
                MbeansSource.log.debug((Object)("Set attribute " + objectName + " " + attName + " " + value));
            }
            final ObjectName oname = new ObjectName(objectName);
            type = this.registry.getType(oname, attName);
            if (type == null) {
                MbeansSource.log.info((Object)("Can't find attribute " + objectName + " " + attName));
            }
            else {
                final Object valueO = this.registry.convertValue(type, value);
                server.setAttribute(oname, new Attribute(attName, valueO));
            }
        }
        catch (Exception ex) {
            MbeansSource.log.error((Object)("Error processing attribute " + objectName + " " + attName + " " + value), (Throwable)ex);
        }
    }
    
    private void processArg(final Node mbeanN) {
        Node argN;
        for (Node firstArgN = argN = DomUtil.getChild(mbeanN, "arg"); argN != null; argN = DomUtil.getNext(argN)) {
            DomUtil.getAttribute(argN, "type");
            String value = DomUtil.getAttribute(argN, "value");
            if (value == null) {
                value = DomUtil.getContent(argN);
            }
        }
    }
    
    static {
        log = LogFactory.getLog((Class)MbeansSource.class);
        MbeansSource.loaderLoaded = false;
    }
}
