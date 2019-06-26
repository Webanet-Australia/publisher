/*
 * Watch code adopted from: https://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved. 
*/


package preset.publisher;

import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class Watch {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private boolean trace = false;
    
    public static String WATCH_PATH; 
    
 
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    Watch(Path dir) throws  IOException {
  	
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey,Path>();	      
		
		if (Publisher.recursive) {

			Publisher.logMessage(Level.INFO, "Scanning " + dir + ".");
		    registerAll(dir);

		} else {
		    register(dir);
		}
		
		// enable trace after initial registration
		this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     * 
     */
    void processEvents()  {
        
    	for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                Publisher.logMessage(Level.SEVERE, "WatchKey not recognized.");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                Kind<?> kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }
                       
                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
              
                //System.out.format("%s: %s\n", event.kind().name(), child);
             
                // if file created or modified and is a publish.xml file...ENTRY_MODIFY
                if ( child.getFileName().toString().equals(Publisher.getConfigName()) && (kind == ENTRY_CREATE )   ) {
                	
                	//wait to allow some time for an ftp to finish uploading...hack hack got a better idea?
                	try {
                		TimeUnit.SECONDS.sleep(Publisher.getConfigWait());
                	} catch (Exception e) {
                		Publisher.logMessage(Level.SEVERE, e.getMessage());
                	}
                	
            		Transform trans = new Transform();
            	
            		trans.setPublishFile(child.toString());
            		trans.publish();
            
            		trans = null;
            	
                }
                
                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (Publisher.recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
   
}

