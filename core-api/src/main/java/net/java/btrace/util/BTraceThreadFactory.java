/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
final public class BTraceThreadFactory implements ThreadFactory {
    final private static AtomicInteger counter = new AtomicInteger();
    
    final private String prefix;
    
    public BTraceThreadFactory() {
        this("BTrace Pool");
    }
    
    public BTraceThreadFactory(String prefix) {
        this.prefix = prefix;
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, prefix + " - " + counter.incrementAndGet());
        t.setDaemon(true);
        return t;
    }
    
}
