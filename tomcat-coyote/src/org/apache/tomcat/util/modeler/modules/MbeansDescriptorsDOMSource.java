// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.modeler.modules;

import org.apache.juli.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.apache.tomcat.util.modeler.ParameterInfo;
import org.apache.tomcat.util.modeler.OperationInfo;
import org.apache.tomcat.util.modeler.NotificationInfo;
import org.apache.tomcat.util.modeler.AttributeInfo;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.DomUtil;
import java.io.InputStream;
import java.util.ArrayList;
import javax.management.ObjectName;
import java.util.List;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.juli.logging.Log;

@Deprecated
public class MbeansDescriptorsDOMSource extends ModelerSource
{
    private static final Log log;
    Registry registry;
    String location;
    String type;
    Object source;
    List<ObjectName> mbeans;
    
    public MbeansDescriptorsDOMSource() {
        this.mbeans = new ArrayList<ObjectName>();
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
    
    public void setSource(final Object source) {
        this.source = source;
    }
    
    @Override
    public List<ObjectName> loadDescriptors(final Registry registry, final String type, final Object source) throws Exception {
        this.setRegistry(registry);
        this.setType(type);
        this.setSource(source);
        this.execute();
        return this.mbeans;
    }
    
    public void execute() throws Exception {
        if (this.registry == null) {
            this.registry = Registry.getRegistry(null, null);
        }
        try {
            final InputStream stream = (InputStream)this.source;
            final long t1 = System.currentTimeMillis();
            final Document doc = DomUtil.readXml(stream);
            final Node descriptorsN = doc.getDocumentElement();
            if (descriptorsN == null) {
                MbeansDescriptorsDOMSource.log.error((Object)"No descriptors found");
                return;
            }
            Node firstMbeanN = null;
            if ("mbean".equals(descriptorsN.getNodeName())) {
                firstMbeanN = descriptorsN;
            }
            else {
                firstMbeanN = DomUtil.getChild(descriptorsN, "mbean");
            }
            if (firstMbeanN == null) {
                MbeansDescriptorsDOMSource.log.error((Object)" No mbean tags ");
                return;
            }
            for (Node mbeanN = firstMbeanN; mbeanN != null; mbeanN = DomUtil.getNext(mbeanN)) {
                final ManagedBean managed = new ManagedBean();
                DomUtil.setAttributes(managed, mbeanN);
                Node descN;
                for (Node firstN = descN = DomUtil.getChild(mbeanN, "attribute"); descN != null; descN = DomUtil.getNext(descN)) {
                    final AttributeInfo ai = new AttributeInfo();
                    DomUtil.setAttributes(ai, descN);
                    managed.addAttribute(ai);
                    if (MbeansDescriptorsDOMSource.log.isTraceEnabled()) {
                        MbeansDescriptorsDOMSource.log.trace((Object)("Create attribute " + ai));
                    }
                }
                for (Node firstN = descN = DomUtil.getChild(mbeanN, "notification"); descN != null; descN = DomUtil.getNext(descN)) {
                    final NotificationInfo ni = new NotificationInfo();
                    DomUtil.setAttributes(ni, descN);
                    Node paramN;
                    for (Node firstParamN = paramN = DomUtil.getChild(descN, "notification-type"); paramN != null; paramN = DomUtil.getNext(paramN)) {
                        ni.addNotifType(DomUtil.getContent(paramN));
                    }
                    managed.addNotification(ni);
                    if (MbeansDescriptorsDOMSource.log.isTraceEnabled()) {
                        MbeansDescriptorsDOMSource.log.trace((Object)("Created notification " + ni));
                    }
                }
                for (Node firstN = descN = DomUtil.getChild(mbeanN, "operation"); descN != null; descN = DomUtil.getNext(descN)) {
                    final OperationInfo oi = new OperationInfo();
                    DomUtil.setAttributes(oi, descN);
                    Node paramN;
                    for (Node firstParamN = paramN = DomUtil.getChild(descN, "parameter"); paramN != null; paramN = DomUtil.getNext(paramN)) {
                        final ParameterInfo pi = new ParameterInfo();
                        DomUtil.setAttributes(pi, paramN);
                        if (MbeansDescriptorsDOMSource.log.isTraceEnabled()) {
                            MbeansDescriptorsDOMSource.log.trace((Object)("Add param " + pi.getName()));
                        }
                        oi.addParameter(pi);
                    }
                    managed.addOperation(oi);
                    if (MbeansDescriptorsDOMSource.log.isTraceEnabled()) {
                        MbeansDescriptorsDOMSource.log.trace((Object)("Create operation " + oi));
                    }
                }
                this.registry.addManagedBean(managed);
            }
            final long t2 = System.currentTimeMillis();
            MbeansDescriptorsDOMSource.log.debug((Object)("Reading descriptors ( dom ) " + (t2 - t1)));
        }
        catch (Exception ex) {
            MbeansDescriptorsDOMSource.log.error((Object)"Error reading descriptors ", (Throwable)ex);
        }
    }
    
    static {
        log = LogFactory.getLog((Class)MbeansDescriptorsDOMSource.class);
    }
}
