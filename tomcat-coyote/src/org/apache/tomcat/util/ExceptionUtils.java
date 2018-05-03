// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util;

import java.lang.reflect.InvocationTargetException;

public class ExceptionUtils
{
    public static void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath)t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError)t;
        }
    }
    
    public static Throwable unwrapInvocationTargetException(final Throwable t) {
        if (t instanceof InvocationTargetException && t.getCause() != null) {
            return t.getCause();
        }
        return t;
    }
}
