// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.mapper;

import org.apache.tomcat.util.buf.MessageBytes;

public class MappingData
{
    public Object host;
    public Object context;
    public Object[] contexts;
    public Object wrapper;
    public boolean jspWildCard;
    public MessageBytes contextPath;
    public MessageBytes requestPath;
    public MessageBytes wrapperPath;
    public MessageBytes pathInfo;
    public MessageBytes redirectPath;
    
    public MappingData() {
        this.host = null;
        this.context = null;
        this.contexts = null;
        this.wrapper = null;
        this.jspWildCard = false;
        this.contextPath = MessageBytes.newInstance();
        this.requestPath = MessageBytes.newInstance();
        this.wrapperPath = MessageBytes.newInstance();
        this.pathInfo = MessageBytes.newInstance();
        this.redirectPath = MessageBytes.newInstance();
    }
    
    public void recycle() {
        this.host = null;
        this.context = null;
        this.contexts = null;
        this.wrapper = null;
        this.jspWildCard = false;
        this.contextPath.recycle();
        this.requestPath.recycle();
        this.wrapperPath.recycle();
        this.pathInfo.recycle();
        this.redirectPath.recycle();
    }
}
