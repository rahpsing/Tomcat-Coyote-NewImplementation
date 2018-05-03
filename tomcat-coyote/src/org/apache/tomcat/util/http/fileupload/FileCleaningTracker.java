// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http.fileupload;

import java.lang.ref.PhantomReference;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;
import java.lang.ref.ReferenceQueue;

public class FileCleaningTracker
{
    private final ReferenceQueue<Object> q;
    private final Collection<Tracker> trackers;
    private final List<String> deleteFailures;
    private volatile boolean exitWhenFinished;
    private Thread reaper;
    
    public FileCleaningTracker() {
        this.q = new ReferenceQueue<Object>();
        this.trackers = (Collection<Tracker>)Collections.synchronizedSet(new HashSet<Object>());
        this.deleteFailures = Collections.synchronizedList(new ArrayList<String>());
        this.exitWhenFinished = false;
    }
    
    public void track(final File file, final Object marker) {
        this.track(file, marker, null);
    }
    
    public void track(final File file, final Object marker, final FileDeleteStrategy deleteStrategy) {
        if (file == null) {
            throw new NullPointerException("The file must not be null");
        }
        this.addTracker(file.getPath(), marker, deleteStrategy);
    }
    
    private synchronized void addTracker(final String path, final Object marker, final FileDeleteStrategy deleteStrategy) {
        if (this.exitWhenFinished) {
            throw new IllegalStateException("No new trackers can be added once exitWhenFinished() is called");
        }
        if (this.reaper == null) {
            (this.reaper = new Reaper()).start();
        }
        this.trackers.add(new Tracker(path, deleteStrategy, marker, this.q));
    }
    
    private final class Reaper extends Thread
    {
        Reaper() {
            super("File Reaper");
            this.setPriority(10);
            this.setDaemon(true);
        }
        
        @Override
        public void run() {
            while (true) {
                if (FileCleaningTracker.this.exitWhenFinished) {
                    if (FileCleaningTracker.this.trackers.size() <= 0) {
                        break;
                    }
                }
                try {
                    final Tracker tracker = (Tracker)FileCleaningTracker.this.q.remove();
                    FileCleaningTracker.this.trackers.remove(tracker);
                    if (!tracker.delete()) {
                        FileCleaningTracker.this.deleteFailures.add(tracker.getPath());
                    }
                    tracker.clear();
                }
                catch (InterruptedException e) {}
            }
        }
    }
    
    private static final class Tracker extends PhantomReference<Object>
    {
        private final String path;
        private final FileDeleteStrategy deleteStrategy;
        
        Tracker(final String path, final FileDeleteStrategy deleteStrategy, final Object marker, final ReferenceQueue<? super Object> queue) {
            super(marker, queue);
            this.path = path;
            this.deleteStrategy = ((deleteStrategy == null) ? FileDeleteStrategy.NORMAL : deleteStrategy);
        }
        
        public String getPath() {
            return this.path;
        }
        
        public boolean delete() {
            return this.deleteStrategy.deleteQuietly(new File(this.path));
        }
    }
}
