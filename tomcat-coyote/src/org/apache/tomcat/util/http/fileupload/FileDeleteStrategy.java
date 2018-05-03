// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

import java.io.IOException;
import java.io.File;

public class FileDeleteStrategy
{
    public static final FileDeleteStrategy NORMAL;
    private final String name;
    
    protected FileDeleteStrategy(final String name) {
        this.name = name;
    }
    
    public boolean deleteQuietly(final File fileToDelete) {
        if (fileToDelete == null || !fileToDelete.exists()) {
            return true;
        }
        try {
            return this.doDelete(fileToDelete);
        }
        catch (IOException ex) {
            return false;
        }
    }
    
    protected boolean doDelete(final File fileToDelete) throws IOException {
        return fileToDelete.delete();
    }
    
    @Override
    public String toString() {
        return "FileDeleteStrategy[" + this.name + "]";
    }
    
    static {
        NORMAL = new FileDeleteStrategy("Normal");
    }
    
    static class ForceFileDeleteStrategy extends FileDeleteStrategy
    {
        ForceFileDeleteStrategy() {
            super("Force");
        }
        
        @Override
        protected boolean doDelete(final File fileToDelete) throws IOException {
            FileUtils.forceDelete(fileToDelete);
            return true;
        }
    }
}
