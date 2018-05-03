// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.mapper;

import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.Ascii;
import javax.naming.directory.DirContext;
import javax.naming.NamingException;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import javax.naming.Context;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;

public final class Mapper
{
    private static final Log log;
    protected static final StringManager sm;
    protected Host[] hosts;
    protected String defaultHostName;
    protected ContextVersion context;
    
    public Mapper() {
        this.hosts = new Host[0];
        this.defaultHostName = null;
        this.context = new ContextVersion();
    }
    
    public void setDefaultHostName(final String defaultHostName) {
        this.defaultHostName = defaultHostName;
    }
    
    public synchronized void addHost(final String name, final String[] aliases, final Object host) {
        Host[] newHosts = new Host[this.hosts.length + 1];
        Host newHost = new Host();
        final ContextList contextList = new ContextList();
        newHost.name = name;
        newHost.contextList = contextList;
        newHost.object = host;
        if (insertMap(this.hosts, newHosts, newHost)) {
            this.hosts = newHosts;
        }
        for (int i = 0; i < aliases.length; ++i) {
            newHosts = new Host[this.hosts.length + 1];
            newHost = new Host();
            newHost.name = aliases[i];
            newHost.contextList = contextList;
            newHost.object = host;
            if (insertMap(this.hosts, newHosts, newHost)) {
                this.hosts = newHosts;
            }
        }
    }
    
    public synchronized void removeHost(final String name) {
        final int pos = find(this.hosts, name);
        if (pos < 0) {
            return;
        }
        final Object host = this.hosts[pos].object;
        final Host[] newHosts = new Host[this.hosts.length - 1];
        if (removeMap(this.hosts, newHosts, name)) {
            this.hosts = newHosts;
        }
        for (int i = 0; i < newHosts.length; ++i) {
            if (newHosts[i].object == host) {
                final Host[] newHosts2 = new Host[this.hosts.length - 1];
                if (removeMap(this.hosts, newHosts2, newHosts[i].name)) {
                    this.hosts = newHosts2;
                }
            }
        }
    }
    
    public synchronized void addHostAlias(final String name, final String alias) {
        final int pos = find(this.hosts, name);
        if (pos < 0) {
            return;
        }
        final Host realHost = this.hosts[pos];
        final Host[] newHosts = new Host[this.hosts.length + 1];
        final Host newHost = new Host();
        newHost.name = alias;
        newHost.contextList = realHost.contextList;
        newHost.object = realHost.object;
        if (insertMap(this.hosts, newHosts, newHost)) {
            this.hosts = newHosts;
        }
    }
    
    public synchronized void removeHostAlias(final String alias) {
        final int pos = find(this.hosts, alias);
        if (pos < 0) {
            return;
        }
        final Host[] newHosts = new Host[this.hosts.length - 1];
        if (removeMap(this.hosts, newHosts, alias)) {
            this.hosts = newHosts;
        }
    }
    
    public void setContext(final String path, final String[] welcomeResources, final javax.naming.Context resources) {
        this.context.path = path;
        this.context.welcomeResources = welcomeResources;
        this.context.resources = resources;
    }
    
    public void addContextVersion(final String hostName, final Object host, final String path, final String version, final Object context, final String[] welcomeResources, final javax.naming.Context resources) {
        Host[] hosts = this.hosts;
        int pos = find(hosts, hostName);
        if (pos < 0) {
            this.addHost(hostName, new String[0], host);
            hosts = this.hosts;
            pos = find(hosts, hostName);
        }
        if (pos < 0) {
            Mapper.log.error((Object)("No host found: " + hostName));
        }
        final Host mappedHost = hosts[pos];
        if (mappedHost.name.equals(hostName)) {
            final int slashCount = slashCount(path);
            synchronized (mappedHost) {
                final Context[] contexts = mappedHost.contextList.contexts;
                if (slashCount > mappedHost.contextList.nesting) {
                    mappedHost.contextList.nesting = slashCount;
                }
                int pos2 = find(contexts, path);
                if (pos2 < 0 || !path.equals(contexts[pos2].name)) {
                    final Context newContext = new Context();
                    newContext.name = path;
                    final Context[] newContexts = new Context[contexts.length + 1];
                    if (insertMap(contexts, newContexts, newContext)) {
                        mappedHost.contextList.contexts = newContexts;
                    }
                    pos2 = find(newContexts, path);
                }
                final Context mappedContext = mappedHost.contextList.contexts[pos2];
                final ContextVersion[] contextVersions = mappedContext.versions;
                final ContextVersion[] newContextVersions = new ContextVersion[contextVersions.length + 1];
                final ContextVersion newContextVersion = new ContextVersion();
                newContextVersion.path = path;
                newContextVersion.name = version;
                newContextVersion.object = context;
                newContextVersion.welcomeResources = welcomeResources;
                newContextVersion.resources = resources;
                if (insertMap(contextVersions, newContextVersions, newContextVersion)) {
                    mappedContext.versions = newContextVersions;
                }
            }
        }
    }
    
    public void removeContextVersion(final String hostName, final String path, final String version) {
        final Host[] hosts = this.hosts;
        final int pos = find(hosts, hostName);
        if (pos < 0) {
            return;
        }
        final Host host = hosts[pos];
        if (host.name.equals(hostName)) {
            synchronized (host) {
                final Context[] contexts = host.contextList.contexts;
                if (contexts.length == 0) {
                    return;
                }
                final int pos2 = find(contexts, path);
                if (pos2 < 0 || !path.equals(contexts[pos2].name)) {
                    return;
                }
                final Context context = contexts[pos2];
                final ContextVersion[] contextVersions = context.versions;
                final ContextVersion[] newContextVersions = new ContextVersion[contextVersions.length - 1];
                if (removeMap(contextVersions, newContextVersions, version)) {
                    context.versions = newContextVersions;
                    if (context.versions.length == 0) {
                        final Context[] newContexts = new Context[contexts.length - 1];
                        if (removeMap(contexts, newContexts, path)) {
                            host.contextList.contexts = newContexts;
                            host.contextList.nesting = 0;
                            for (int i = 0; i < newContexts.length; ++i) {
                                final int slashCount = slashCount(newContexts[i].name);
                                if (slashCount > host.contextList.nesting) {
                                    host.contextList.nesting = slashCount;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void addWrapper(final String hostName, final String contextPath, final String version, final String path, final Object wrapper, final boolean jspWildCard, final boolean resourceOnly) {
        final Host[] hosts = this.hosts;
        final int pos = find(hosts, hostName);
        if (pos < 0) {
            return;
        }
        final Host host = hosts[pos];
        if (host.name.equals(hostName)) {
            final Context[] contexts = host.contextList.contexts;
            final int pos2 = find(contexts, contextPath);
            if (pos2 < 0) {
                Mapper.log.error((Object)("No context found: " + contextPath));
                return;
            }
            final Context context = contexts[pos2];
            if (context.name.equals(contextPath)) {
                final ContextVersion[] contextVersions = context.versions;
                final int pos3 = find(contextVersions, version);
                if (pos3 < 0) {
                    Mapper.log.error((Object)("No context version found: " + contextPath + " " + version));
                    return;
                }
                final ContextVersion contextVersion = contextVersions[pos3];
                if (contextVersion.name.equals(version)) {
                    this.addWrapper(contextVersion, path, wrapper, jspWildCard, resourceOnly);
                }
            }
        }
    }
    
    public void addWrapper(final String path, final Object wrapper, final boolean jspWildCard, final boolean resourceOnly) {
        this.addWrapper(this.context, path, wrapper, jspWildCard, resourceOnly);
    }
    
    protected void addWrapper(final ContextVersion context, final String path, final Object wrapper, final boolean jspWildCard, final boolean resourceOnly) {
        synchronized (context) {
            final Wrapper newWrapper = new Wrapper();
            newWrapper.object = wrapper;
            newWrapper.jspWildCard = jspWildCard;
            newWrapper.resourceOnly = resourceOnly;
            if (path.endsWith("/*")) {
                newWrapper.name = path.substring(0, path.length() - 2);
                final Wrapper[] oldWrappers = context.wildcardWrappers;
                final Wrapper[] newWrappers = new Wrapper[oldWrappers.length + 1];
                if (insertMap(oldWrappers, newWrappers, newWrapper)) {
                    context.wildcardWrappers = newWrappers;
                    final int slashCount = slashCount(newWrapper.name);
                    if (slashCount > context.nesting) {
                        context.nesting = slashCount;
                    }
                }
            }
            else if (path.startsWith("*.")) {
                newWrapper.name = path.substring(2);
                final Wrapper[] oldWrappers = context.extensionWrappers;
                final Wrapper[] newWrappers = new Wrapper[oldWrappers.length + 1];
                if (insertMap(oldWrappers, newWrappers, newWrapper)) {
                    context.extensionWrappers = newWrappers;
                }
            }
            else if (path.equals("/")) {
                newWrapper.name = "";
                context.defaultWrapper = newWrapper;
            }
            else {
                if (path.length() == 0) {
                    newWrapper.name = "/";
                }
                else {
                    newWrapper.name = path;
                }
                final Wrapper[] oldWrappers = context.exactWrappers;
                final Wrapper[] newWrappers = new Wrapper[oldWrappers.length + 1];
                if (insertMap(oldWrappers, newWrappers, newWrapper)) {
                    context.exactWrappers = newWrappers;
                }
            }
        }
    }
    
    public void removeWrapper(final String path) {
        this.removeWrapper(this.context, path);
    }
    
    public void removeWrapper(final String hostName, final String contextPath, final String version, final String path) {
        final Host[] hosts = this.hosts;
        final int pos = find(hosts, hostName);
        if (pos < 0) {
            return;
        }
        final Host host = hosts[pos];
        if (host.name.equals(hostName)) {
            final Context[] contexts = host.contextList.contexts;
            final int pos2 = find(contexts, contextPath);
            if (pos2 < 0) {
                return;
            }
            final Context context = contexts[pos2];
            if (context.name.equals(contextPath)) {
                final ContextVersion[] contextVersions = context.versions;
                final int pos3 = find(contextVersions, version);
                if (pos3 < 0) {
                    return;
                }
                final ContextVersion contextVersion = contextVersions[pos3];
                if (contextVersion.name.equals(version)) {
                    this.removeWrapper(contextVersion, path);
                }
            }
        }
    }
    
    protected void removeWrapper(final ContextVersion context, final String path) {
        if (Mapper.log.isDebugEnabled()) {
            Mapper.log.debug((Object)Mapper.sm.getString("mapper.removeWrapper", new Object[] { context.name, path }));
        }
        synchronized (context) {
            if (path.endsWith("/*")) {
                final String name = path.substring(0, path.length() - 2);
                final Wrapper[] oldWrappers = context.wildcardWrappers;
                if (oldWrappers.length == 0) {
                    return;
                }
                final Wrapper[] newWrappers = new Wrapper[oldWrappers.length - 1];
                if (removeMap(oldWrappers, newWrappers, name)) {
                    context.nesting = 0;
                    for (int i = 0; i < newWrappers.length; ++i) {
                        final int slashCount = slashCount(newWrappers[i].name);
                        if (slashCount > context.nesting) {
                            context.nesting = slashCount;
                        }
                    }
                    context.wildcardWrappers = newWrappers;
                }
            }
            else if (path.startsWith("*.")) {
                final String name = path.substring(2);
                final Wrapper[] oldWrappers = context.extensionWrappers;
                if (oldWrappers.length == 0) {
                    return;
                }
                final Wrapper[] newWrappers = new Wrapper[oldWrappers.length - 1];
                if (removeMap(oldWrappers, newWrappers, name)) {
                    context.extensionWrappers = newWrappers;
                }
            }
            else if (path.equals("/")) {
                context.defaultWrapper = null;
            }
            else {
                String name;
                if (path.length() == 0) {
                    name = "/";
                }
                else {
                    name = path;
                }
                final Wrapper[] oldWrappers = context.exactWrappers;
                if (oldWrappers.length == 0) {
                    return;
                }
                final Wrapper[] newWrappers = new Wrapper[oldWrappers.length - 1];
                if (removeMap(oldWrappers, newWrappers, name)) {
                    context.exactWrappers = newWrappers;
                }
            }
        }
    }
    
    public void addWelcomeFile(final String hostName, final String contextPath, final String version, final String welcomeFile) {
        final Host[] hosts = this.hosts;
        final int pos = find(hosts, hostName);
        if (pos < 0) {
            return;
        }
        final Host host = hosts[pos];
        if (host.name.equals(hostName)) {
            final Context[] contexts = host.contextList.contexts;
            final int pos2 = find(contexts, contextPath);
            if (pos2 < 0) {
                Mapper.log.error((Object)("No context found: " + contextPath));
                return;
            }
            final Context context = contexts[pos2];
            if (context.name.equals(contextPath)) {
                final ContextVersion[] contextVersions = context.versions;
                final int pos3 = find(contextVersions, version);
                if (pos3 < 0) {
                    Mapper.log.error((Object)("No context version found: " + contextPath + " " + version));
                    return;
                }
                final ContextVersion contextVersion = contextVersions[pos3];
                if (contextVersion.name.equals(version)) {
                    final int len = contextVersion.welcomeResources.length + 1;
                    final String[] newWelcomeResources = new String[len];
                    System.arraycopy(contextVersion.welcomeResources, 0, newWelcomeResources, 0, len - 1);
                    newWelcomeResources[len - 1] = welcomeFile;
                    contextVersion.welcomeResources = newWelcomeResources;
                }
            }
        }
    }
    
    public void removeWelcomeFile(final String hostName, final String contextPath, final String version, final String welcomeFile) {
        final Host[] hosts = this.hosts;
        final int pos = find(hosts, hostName);
        if (pos < 0) {
            return;
        }
        final Host host = hosts[pos];
        if (host.name.equals(hostName)) {
            final Context[] contexts = host.contextList.contexts;
            final int pos2 = find(contexts, contextPath);
            if (pos2 < 0) {
                Mapper.log.error((Object)("No context found: " + contextPath));
                return;
            }
            final Context context = contexts[pos2];
            if (context.name.equals(contextPath)) {
                final ContextVersion[] contextVersions = context.versions;
                final int pos3 = find(contextVersions, version);
                if (pos3 < 0) {
                    Mapper.log.error((Object)("No context version found: " + contextPath + " " + version));
                    return;
                }
                final ContextVersion contextVersion = contextVersions[pos3];
                if (contextVersion.name.equals(version)) {
                    int match = -1;
                    for (int i = 0; i < contextVersion.welcomeResources.length; ++i) {
                        if (welcomeFile.equals(contextVersion.welcomeResources[i])) {
                            match = i;
                            break;
                        }
                    }
                    if (match > -1) {
                        final int len = contextVersion.welcomeResources.length - 1;
                        final String[] newWelcomeResources = new String[len];
                        System.arraycopy(contextVersion.welcomeResources, 0, newWelcomeResources, 0, match);
                        if (match < len) {
                            System.arraycopy(contextVersion.welcomeResources, match + 1, newWelcomeResources, match, len - match);
                        }
                        contextVersion.welcomeResources = newWelcomeResources;
                    }
                }
            }
        }
    }
    
    public void clearWelcomeFiles(final String hostName, final String contextPath, final String version) {
        final Host[] hosts = this.hosts;
        final int pos = find(hosts, hostName);
        if (pos < 0) {
            return;
        }
        final Host host = hosts[pos];
        if (host.name.equals(hostName)) {
            final Context[] contexts = host.contextList.contexts;
            final int pos2 = find(contexts, contextPath);
            if (pos2 < 0) {
                Mapper.log.error((Object)("No context found: " + contextPath));
                return;
            }
            final Context context = contexts[pos2];
            if (context.name.equals(contextPath)) {
                final ContextVersion[] contextVersions = context.versions;
                final int pos3 = find(contextVersions, version);
                if (pos3 < 0) {
                    Mapper.log.error((Object)("No context version found: " + contextPath + " " + version));
                    return;
                }
                final ContextVersion contextVersion = contextVersions[pos3];
                if (contextVersion.name.equals(version)) {
                    contextVersion.welcomeResources = new String[0];
                }
            }
        }
    }
    
    public void map(final MessageBytes host, final MessageBytes uri, final String version, final MappingData mappingData) throws Exception {
        if (host.isNull()) {
            host.getCharChunk().append(this.defaultHostName);
        }
        host.toChars();
        uri.toChars();
        this.internalMap(host.getCharChunk(), uri.getCharChunk(), version, mappingData);
    }
    
    public void map(final MessageBytes uri, final MappingData mappingData) throws Exception {
        uri.toChars();
        final CharChunk uricc = uri.getCharChunk();
        uricc.setLimit(-1);
        this.internalMapWrapper(this.context, uricc, mappingData);
    }
    
    private final void internalMap(final CharChunk host, final CharChunk uri, final String version, final MappingData mappingData) throws Exception {
        uri.setLimit(-1);
        Context[] contexts = null;
        Context context = null;
        ContextVersion contextVersion = null;
        int nesting = 0;
        if (mappingData.host == null) {
            final Host[] hosts = this.hosts;
            int pos = findIgnoreCase(hosts, host);
            if (pos != -1 && host.equalsIgnoreCase(hosts[pos].name)) {
                mappingData.host = hosts[pos].object;
                contexts = hosts[pos].contextList.contexts;
                nesting = hosts[pos].contextList.nesting;
            }
            else {
                if (this.defaultHostName == null) {
                    return;
                }
                pos = find(hosts, this.defaultHostName);
                if (pos == -1 || !this.defaultHostName.equals(hosts[pos].name)) {
                    return;
                }
                mappingData.host = hosts[pos].object;
                contexts = hosts[pos].contextList.contexts;
                nesting = hosts[pos].contextList.nesting;
            }
        }
        if (mappingData.context == null) {
            int pos2 = find(contexts, uri);
            if (pos2 == -1) {
                return;
            }
            int lastSlash = -1;
            final int uriEnd = uri.getEnd();
            int length = -1;
            boolean found = false;
            while (pos2 >= 0) {
                if (uri.startsWith(contexts[pos2].name)) {
                    length = contexts[pos2].name.length();
                    if (uri.getLength() == length) {
                        found = true;
                        break;
                    }
                    if (uri.startsWithIgnoreCase("/", length)) {
                        found = true;
                        break;
                    }
                }
                if (lastSlash == -1) {
                    lastSlash = nthSlash(uri, nesting + 1);
                }
                else {
                    lastSlash = lastSlash(uri);
                }
                uri.setEnd(lastSlash);
                pos2 = find(contexts, uri);
            }
            uri.setEnd(uriEnd);
            if (!found) {
                if (contexts[0].name.equals("")) {
                    context = contexts[0];
                }
            }
            else {
                context = contexts[pos2];
            }
            if (context != null) {
                mappingData.contextPath.setString(context.name);
            }
        }
        if (context != null) {
            final ContextVersion[] contextVersions = context.versions;
            final int versionCount = contextVersions.length;
            if (versionCount > 1) {
                final Object[] contextObjects = new Object[contextVersions.length];
                for (int i = 0; i < contextObjects.length; ++i) {
                    contextObjects[i] = contextVersions[i].object;
                }
                mappingData.contexts = contextObjects;
            }
            if (version == null) {
                contextVersion = contextVersions[versionCount - 1];
            }
            else {
                final int pos3 = find(contextVersions, version);
                if (pos3 < 0 || !contextVersions[pos3].name.equals(version)) {
                    contextVersion = contextVersions[versionCount - 1];
                }
                else {
                    contextVersion = contextVersions[pos3];
                }
            }
            mappingData.context = contextVersion.object;
        }
        if (contextVersion != null && mappingData.wrapper == null) {
            this.internalMapWrapper(contextVersion, uri, mappingData);
        }
    }
    
    private final void internalMapWrapper(final ContextVersion contextVersion, final CharChunk path, final MappingData mappingData) throws Exception {
        int pathOffset = path.getOffset();
        int pathEnd = path.getEnd();
        int servletPath = pathOffset;
        boolean noServletPath = false;
        final int length = contextVersion.path.length();
        if (length != pathEnd - pathOffset) {
            servletPath = pathOffset + length;
        }
        else {
            noServletPath = true;
            path.append('/');
            pathOffset = path.getOffset();
            pathEnd = path.getEnd();
            servletPath = pathOffset + length;
        }
        path.setOffset(servletPath);
        final Wrapper[] exactWrappers = contextVersion.exactWrappers;
        this.internalMapExactWrapper(exactWrappers, path, mappingData);
        boolean checkJspWelcomeFiles = false;
        final Wrapper[] wildcardWrappers = contextVersion.wildcardWrappers;
        if (mappingData.wrapper == null) {
            this.internalMapWildcardWrapper(wildcardWrappers, contextVersion.nesting, path, mappingData);
            if (mappingData.wrapper != null && mappingData.jspWildCard) {
                final char[] buf = path.getBuffer();
                if (buf[pathEnd - 1] == '/') {
                    mappingData.wrapper = null;
                    checkJspWelcomeFiles = true;
                }
                else {
                    mappingData.wrapperPath.setChars(buf, path.getStart(), path.getLength());
                    mappingData.pathInfo.recycle();
                }
            }
        }
        if (mappingData.wrapper == null && noServletPath) {
            mappingData.redirectPath.setChars(path.getBuffer(), pathOffset, pathEnd - pathOffset);
            path.setEnd(pathEnd - 1);
            return;
        }
        final Wrapper[] extensionWrappers = contextVersion.extensionWrappers;
        if (mappingData.wrapper == null && !checkJspWelcomeFiles) {
            this.internalMapExtensionWrapper(extensionWrappers, path, mappingData, true);
        }
        if (mappingData.wrapper == null) {
            boolean checkWelcomeFiles = checkJspWelcomeFiles;
            if (!checkWelcomeFiles) {
                final char[] buf2 = path.getBuffer();
                checkWelcomeFiles = (buf2[pathEnd - 1] == '/');
            }
            if (checkWelcomeFiles) {
                for (int i = 0; i < contextVersion.welcomeResources.length && mappingData.wrapper == null; ++i) {
                    path.setOffset(pathOffset);
                    path.setEnd(pathEnd);
                    path.append(contextVersion.welcomeResources[i], 0, contextVersion.welcomeResources[i].length());
                    path.setOffset(servletPath);
                    this.internalMapExactWrapper(exactWrappers, path, mappingData);
                    if (mappingData.wrapper == null) {
                        this.internalMapWildcardWrapper(wildcardWrappers, contextVersion.nesting, path, mappingData);
                    }
                    if (mappingData.wrapper == null && contextVersion.resources != null) {
                        Object file = null;
                        final String pathStr = path.toString();
                        try {
                            file = contextVersion.resources.lookup(pathStr);
                        }
                        catch (NamingException ex) {}
                        if (file != null && !(file instanceof DirContext)) {
                            this.internalMapExtensionWrapper(extensionWrappers, path, mappingData, true);
                            if (mappingData.wrapper == null && contextVersion.defaultWrapper != null) {
                                mappingData.wrapper = contextVersion.defaultWrapper.object;
                                mappingData.requestPath.setChars(path.getBuffer(), path.getStart(), path.getLength());
                                mappingData.wrapperPath.setChars(path.getBuffer(), path.getStart(), path.getLength());
                                mappingData.requestPath.setString(pathStr);
                                mappingData.wrapperPath.setString(pathStr);
                            }
                        }
                    }
                }
                path.setOffset(servletPath);
                path.setEnd(pathEnd);
            }
        }
        if (mappingData.wrapper == null) {
            boolean checkWelcomeFiles = checkJspWelcomeFiles;
            if (!checkWelcomeFiles) {
                final char[] buf2 = path.getBuffer();
                checkWelcomeFiles = (buf2[pathEnd - 1] == '/');
            }
            if (checkWelcomeFiles) {
                for (int i = 0; i < contextVersion.welcomeResources.length && mappingData.wrapper == null; ++i) {
                    path.setOffset(pathOffset);
                    path.setEnd(pathEnd);
                    path.append(contextVersion.welcomeResources[i], 0, contextVersion.welcomeResources[i].length());
                    path.setOffset(servletPath);
                    this.internalMapExtensionWrapper(extensionWrappers, path, mappingData, false);
                }
                path.setOffset(servletPath);
                path.setEnd(pathEnd);
            }
        }
        if (mappingData.wrapper == null && !checkJspWelcomeFiles) {
            if (contextVersion.defaultWrapper != null) {
                mappingData.wrapper = contextVersion.defaultWrapper.object;
                mappingData.requestPath.setChars(path.getBuffer(), path.getStart(), path.getLength());
                mappingData.wrapperPath.setChars(path.getBuffer(), path.getStart(), path.getLength());
            }
            final char[] buf3 = path.getBuffer();
            if (contextVersion.resources != null && buf3[pathEnd - 1] != '/') {
                Object file2 = null;
                final String pathStr2 = path.toString();
                try {
                    file2 = contextVersion.resources.lookup(pathStr2);
                }
                catch (NamingException ex2) {}
                if (file2 != null && file2 instanceof DirContext) {
                    path.setOffset(pathOffset);
                    path.append('/');
                    mappingData.redirectPath.setChars(path.getBuffer(), path.getStart(), path.getLength());
                }
                else {
                    mappingData.requestPath.setString(pathStr2);
                    mappingData.wrapperPath.setString(pathStr2);
                }
            }
        }
        path.setOffset(pathOffset);
        path.setEnd(pathEnd);
    }
    
    private final void internalMapExactWrapper(final Wrapper[] wrappers, final CharChunk path, final MappingData mappingData) {
        final int pos = find(wrappers, path);
        if (pos != -1 && path.equals(wrappers[pos].name)) {
            mappingData.requestPath.setString(wrappers[pos].name);
            mappingData.wrapper = wrappers[pos].object;
            if (path.equals("/")) {
                mappingData.pathInfo.setString("/");
                mappingData.wrapperPath.setString("");
                mappingData.contextPath.setString("");
            }
            else {
                mappingData.wrapperPath.setString(wrappers[pos].name);
            }
        }
    }
    
    private final void internalMapWildcardWrapper(final Wrapper[] wrappers, final int nesting, final CharChunk path, final MappingData mappingData) {
        final int pathEnd = path.getEnd();
        int lastSlash = -1;
        int length = -1;
        int pos = find(wrappers, path);
        if (pos != -1) {
            boolean found = false;
            while (pos >= 0) {
                if (path.startsWith(wrappers[pos].name)) {
                    length = wrappers[pos].name.length();
                    if (path.getLength() == length) {
                        found = true;
                        break;
                    }
                    if (path.startsWithIgnoreCase("/", length)) {
                        found = true;
                        break;
                    }
                }
                if (lastSlash == -1) {
                    lastSlash = nthSlash(path, nesting + 1);
                }
                else {
                    lastSlash = lastSlash(path);
                }
                path.setEnd(lastSlash);
                pos = find(wrappers, path);
            }
            path.setEnd(pathEnd);
            if (found) {
                mappingData.wrapperPath.setString(wrappers[pos].name);
                if (path.getLength() > length) {
                    mappingData.pathInfo.setChars(path.getBuffer(), path.getOffset() + length, path.getLength() - length);
                }
                mappingData.requestPath.setChars(path.getBuffer(), path.getOffset(), path.getLength());
                mappingData.wrapper = wrappers[pos].object;
                mappingData.jspWildCard = wrappers[pos].jspWildCard;
            }
        }
    }
    
    private final void internalMapExtensionWrapper(final Wrapper[] wrappers, final CharChunk path, final MappingData mappingData, final boolean resourceExpected) {
        final char[] buf = path.getBuffer();
        final int pathEnd = path.getEnd();
        final int servletPath = path.getOffset();
        int slash = -1;
        for (int i = pathEnd - 1; i >= servletPath; --i) {
            if (buf[i] == '/') {
                slash = i;
                break;
            }
        }
        if (slash >= 0) {
            int period = -1;
            for (int j = pathEnd - 1; j > slash; --j) {
                if (buf[j] == '.') {
                    period = j;
                    break;
                }
            }
            if (period >= 0) {
                path.setOffset(period + 1);
                path.setEnd(pathEnd);
                final int pos = find(wrappers, path);
                if (pos != -1 && path.equals(wrappers[pos].name) && (resourceExpected || !wrappers[pos].resourceOnly)) {
                    mappingData.wrapperPath.setChars(buf, servletPath, pathEnd - servletPath);
                    mappingData.requestPath.setChars(buf, servletPath, pathEnd - servletPath);
                    mappingData.wrapper = wrappers[pos].object;
                }
                path.setOffset(servletPath);
                path.setEnd(pathEnd);
            }
        }
    }
    
    private static final int find(final MapElement[] map, final CharChunk name) {
        return find(map, name, name.getStart(), name.getEnd());
    }
    
    private static final int find(final MapElement[] map, final CharChunk name, final int start, final int end) {
        int a = 0;
        int b = map.length - 1;
        if (b == -1) {
            return -1;
        }
        if (compare(name, start, end, map[0].name) < 0) {
            return -1;
        }
        if (b == 0) {
            return 0;
        }
        int i = 0;
        while (true) {
            i = (b + a) / 2;
            final int result = compare(name, start, end, map[i].name);
            if (result == 1) {
                a = i;
            }
            else {
                if (result == 0) {
                    return i;
                }
                b = i;
            }
            if (b - a == 1) {
                final int result2 = compare(name, start, end, map[b].name);
                if (result2 < 0) {
                    return a;
                }
                return b;
            }
        }
    }
    
    private static final int findIgnoreCase(final MapElement[] map, final CharChunk name) {
        return findIgnoreCase(map, name, name.getStart(), name.getEnd());
    }
    
    private static final int findIgnoreCase(final MapElement[] map, final CharChunk name, final int start, final int end) {
        int a = 0;
        int b = map.length - 1;
        if (b == -1) {
            return -1;
        }
        if (compareIgnoreCase(name, start, end, map[0].name) < 0) {
            return -1;
        }
        if (b == 0) {
            return 0;
        }
        int i = 0;
        while (true) {
            i = (b + a) / 2;
            final int result = compareIgnoreCase(name, start, end, map[i].name);
            if (result == 1) {
                a = i;
            }
            else {
                if (result == 0) {
                    return i;
                }
                b = i;
            }
            if (b - a == 1) {
                final int result2 = compareIgnoreCase(name, start, end, map[b].name);
                if (result2 < 0) {
                    return a;
                }
                return b;
            }
        }
    }
    
    private static final int find(final MapElement[] map, final String name) {
        int a = 0;
        int b = map.length - 1;
        if (b == -1) {
            return -1;
        }
        if (name.compareTo(map[0].name) < 0) {
            return -1;
        }
        if (b == 0) {
            return 0;
        }
        int i = 0;
        while (true) {
            i = (b + a) / 2;
            final int result = name.compareTo(map[i].name);
            if (result > 0) {
                a = i;
            }
            else {
                if (result == 0) {
                    return i;
                }
                b = i;
            }
            if (b - a == 1) {
                final int result2 = name.compareTo(map[b].name);
                if (result2 < 0) {
                    return a;
                }
                return b;
            }
        }
    }
    
    private static final int compare(final CharChunk name, final int start, final int end, final String compareTo) {
        int result = 0;
        final char[] c = name.getBuffer();
        int len = compareTo.length();
        if (end - start < len) {
            len = end - start;
        }
        for (int i = 0; i < len && result == 0; ++i) {
            if (c[i + start] > compareTo.charAt(i)) {
                result = 1;
            }
            else if (c[i + start] < compareTo.charAt(i)) {
                result = -1;
            }
        }
        if (result == 0) {
            if (compareTo.length() > end - start) {
                result = -1;
            }
            else if (compareTo.length() < end - start) {
                result = 1;
            }
        }
        return result;
    }
    
    private static final int compareIgnoreCase(final CharChunk name, final int start, final int end, final String compareTo) {
        int result = 0;
        final char[] c = name.getBuffer();
        int len = compareTo.length();
        if (end - start < len) {
            len = end - start;
        }
        for (int i = 0; i < len && result == 0; ++i) {
            if (Ascii.toLower(c[i + start]) > Ascii.toLower(compareTo.charAt(i))) {
                result = 1;
            }
            else if (Ascii.toLower(c[i + start]) < Ascii.toLower(compareTo.charAt(i))) {
                result = -1;
            }
        }
        if (result == 0) {
            if (compareTo.length() > end - start) {
                result = -1;
            }
            else if (compareTo.length() < end - start) {
                result = 1;
            }
        }
        return result;
    }
    
    private static final int lastSlash(final CharChunk name) {
        final char[] c = name.getBuffer();
        final int end = name.getEnd();
        final int start = name.getStart();
        int pos = end;
        while (pos > start && c[--pos] != '/') {}
        return pos;
    }
    
    private static final int nthSlash(final CharChunk name, final int n) {
        final char[] c = name.getBuffer();
        final int end = name.getEnd();
        int pos;
        final int start = pos = name.getStart();
        int count = 0;
        while (pos < end) {
            if (c[pos++] == '/' && ++count == n) {
                --pos;
                break;
            }
        }
        return pos;
    }
    
    private static final int slashCount(final String name) {
        int pos = -1;
        int count = 0;
        while ((pos = name.indexOf(47, pos + 1)) != -1) {
            ++count;
        }
        return count;
    }
    
    private static final boolean insertMap(final MapElement[] oldMap, final MapElement[] newMap, final MapElement newElement) {
        final int pos = find(oldMap, newElement.name);
        if (pos != -1 && newElement.name.equals(oldMap[pos].name)) {
            return false;
        }
        System.arraycopy(oldMap, 0, newMap, 0, pos + 1);
        newMap[pos + 1] = newElement;
        System.arraycopy(oldMap, pos + 1, newMap, pos + 2, oldMap.length - pos - 1);
        return true;
    }
    
    private static final boolean removeMap(final MapElement[] oldMap, final MapElement[] newMap, final String name) {
        final int pos = find(oldMap, name);
        if (pos != -1 && name.equals(oldMap[pos].name)) {
            System.arraycopy(oldMap, 0, newMap, 0, pos);
            System.arraycopy(oldMap, pos + 1, newMap, pos, oldMap.length - pos - 1);
            return true;
        }
        return false;
    }
    
    static {
        log = LogFactory.getLog((Class)Mapper.class);
        sm = StringManager.getManager(Mapper.class.getPackage().getName());
    }
    
    protected abstract static class MapElement
    {
        public String name;
        public Object object;
        
        protected MapElement() {
            this.name = null;
            this.object = null;
        }
    }
    
    protected static final class Host extends MapElement
    {
        public ContextList contextList;
        
        protected Host() {
            this.contextList = null;
        }
    }
    
    protected static final class ContextList
    {
        public Context[] contexts;
        public int nesting;
        
        protected ContextList() {
            this.contexts = new Context[0];
            this.nesting = 0;
        }
    }
    
    protected static final class Context extends MapElement
    {
        public ContextVersion[] versions;
        
        protected Context() {
            this.versions = new ContextVersion[0];
        }
    }
    
    protected static final class ContextVersion extends MapElement
    {
        public String path;
        public String[] welcomeResources;
        public javax.naming.Context resources;
        public Wrapper defaultWrapper;
        public Wrapper[] exactWrappers;
        public Wrapper[] wildcardWrappers;
        public Wrapper[] extensionWrappers;
        public int nesting;
        
        protected ContextVersion() {
            this.path = null;
            this.welcomeResources = new String[0];
            this.resources = null;
            this.defaultWrapper = null;
            this.exactWrappers = new Wrapper[0];
            this.wildcardWrappers = new Wrapper[0];
            this.extensionWrappers = new Wrapper[0];
            this.nesting = 0;
        }
    }
    
    protected static class Wrapper extends MapElement
    {
        public boolean jspWildCard;
        public boolean resourceOnly;
        
        protected Wrapper() {
            this.jspWildCard = false;
            this.resourceOnly = false;
        }
    }
}
