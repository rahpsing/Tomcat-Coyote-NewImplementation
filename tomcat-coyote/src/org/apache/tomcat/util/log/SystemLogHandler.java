// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.log;

import java.io.IOException;
import java.util.EmptyStackException;
import java.io.OutputStream;
import java.util.Stack;
import java.io.PrintStream;

public class SystemLogHandler extends PrintStream
{
    protected PrintStream out;
    protected static ThreadLocal<Stack<CaptureLog>> logs;
    protected static Stack<CaptureLog> reuse;
    
    public SystemLogHandler(final PrintStream wrapped) {
        super(wrapped);
        this.out = null;
        this.out = wrapped;
    }
    
    public static void startCapture() {
        CaptureLog log = null;
        if (!SystemLogHandler.reuse.isEmpty()) {
            try {
                log = SystemLogHandler.reuse.pop();
            }
            catch (EmptyStackException e) {
                log = new CaptureLog();
            }
        }
        else {
            log = new CaptureLog();
        }
        Stack<CaptureLog> stack = SystemLogHandler.logs.get();
        if (stack == null) {
            stack = new Stack<CaptureLog>();
            SystemLogHandler.logs.set(stack);
        }
        stack.push(log);
    }
    
    public static String stopCapture() {
        final Stack<CaptureLog> stack = SystemLogHandler.logs.get();
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        final CaptureLog log = stack.pop();
        if (log == null) {
            return null;
        }
        final String capture = log.getCapture();
        log.reset();
        SystemLogHandler.reuse.push(log);
        return capture;
    }
    
    protected PrintStream findStream() {
        final Stack<CaptureLog> stack = SystemLogHandler.logs.get();
        if (stack != null && !stack.isEmpty()) {
            final CaptureLog log = stack.peek();
            if (log != null) {
                final PrintStream ps = log.getStream();
                if (ps != null) {
                    return ps;
                }
            }
        }
        return this.out;
    }
    
    @Override
    public void flush() {
        this.findStream().flush();
    }
    
    @Override
    public void close() {
        this.findStream().close();
    }
    
    @Override
    public boolean checkError() {
        return this.findStream().checkError();
    }
    
    @Override
    protected void setError() {
    }
    
    @Override
    public void write(final int b) {
        this.findStream().write(b);
    }
    
    @Override
    public void write(final byte[] b) throws IOException {
        this.findStream().write(b);
    }
    
    @Override
    public void write(final byte[] buf, final int off, final int len) {
        this.findStream().write(buf, off, len);
    }
    
    @Override
    public void print(final boolean b) {
        this.findStream().print(b);
    }
    
    @Override
    public void print(final char c) {
        this.findStream().print(c);
    }
    
    @Override
    public void print(final int i) {
        this.findStream().print(i);
    }
    
    @Override
    public void print(final long l) {
        this.findStream().print(l);
    }
    
    @Override
    public void print(final float f) {
        this.findStream().print(f);
    }
    
    @Override
    public void print(final double d) {
        this.findStream().print(d);
    }
    
    @Override
    public void print(final char[] s) {
        this.findStream().print(s);
    }
    
    @Override
    public void print(final String s) {
        this.findStream().print(s);
    }
    
    @Override
    public void print(final Object obj) {
        this.findStream().print(obj);
    }
    
    @Override
    public void println() {
        this.findStream().println();
    }
    
    @Override
    public void println(final boolean x) {
        this.findStream().println(x);
    }
    
    @Override
    public void println(final char x) {
        this.findStream().println(x);
    }
    
    @Override
    public void println(final int x) {
        this.findStream().println(x);
    }
    
    @Override
    public void println(final long x) {
        this.findStream().println(x);
    }
    
    @Override
    public void println(final float x) {
        this.findStream().println(x);
    }
    
    @Override
    public void println(final double x) {
        this.findStream().println(x);
    }
    
    @Override
    public void println(final char[] x) {
        this.findStream().println(x);
    }
    
    @Override
    public void println(final String x) {
        this.findStream().println(x);
    }
    
    @Override
    public void println(final Object x) {
        this.findStream().println(x);
    }
    
    static {
        SystemLogHandler.logs = new ThreadLocal<Stack<CaptureLog>>();
        SystemLogHandler.reuse = new Stack<CaptureLog>();
    }
}
