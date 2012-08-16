/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package net.java.btrace.ext.sys;

import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.extensions.runtime.Runtime;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import javax.annotation.Resource;
import net.java.btrace.api.extensions.runtime.MBeans;

/*
 * Wraps the memory related BTrace utility methods
 * @since 1.3
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class Memory {
    @Resource
    private static MBeans mbeans;
    
    @Resource
    private static Runtime rt;
    
    /**
     * Returns the amount of free memory in the Java Virtual Machine.
     * Calling the
     * <code>gc</code> method may result in increasing the value returned
     * by <code>freeMemory.</code>
     *
     * @return  an approximation to the total amount of memory currently
     *          available for future allocated objects, measured in bytes.
     */
    public static long freeMemory() {
        return java.lang.Runtime.getRuntime().freeMemory();
    }

    /**
     * Returns the total amount of memory in the Java virtual machine.
     * The value returned by this method may vary over time, depending on
     * the host environment.
     * <p>
     * Note that the amount of memory required to hold an object of any
     * given type may be implementation-dependent.
     *
     * @return  the total amount of memory currently available for current
     *          and future objects, measured in bytes.
     */
    public static long totalMemory() {
        return java.lang.Runtime.getRuntime().totalMemory();
    }

    /**
     * Returns the maximum amount of memory that the Java virtual machine will
     * attempt to use.  If there is no inherent limit then the value {@link
     * java.lang.Long#MAX_VALUE} will be returned. </p>
     *
     * @return  the maximum amount of memory that the virtual machine will
     *          attempt to use, measured in bytes
     */
    public static long maxMemory() {
        return java.lang.Runtime.getRuntime().maxMemory();
    }

    /**
     * Returns heap memory usage
     */
    public static MemoryUsage heapUsage() {
        return mbeans.getMemoryMBean().getHeapMemoryUsage();
    }

    /**
     * Returns non-heap memory usage
     */
    public static MemoryUsage nonHeapUsage() {
        return mbeans.getMemoryMBean().getNonHeapMemoryUsage();
    }

    /**
     * Returns the amount of memory in bytes that the Java virtual
     * machine initially requests from the operating system for
     * memory management.
     */
    public static long init(MemoryUsage mu) {
        return mu.getInit();
    }

    /**
     * Returns the amount of memory in bytes that is committed for the Java
     * virtual machine to use. This amount of memory is guaranteed for the
     * Java virtual machine to use.
     */
    public static long committed(MemoryUsage mu) {
        return mu.getCommitted();
    }

    /**
     * Returns the maximum amount of memory in bytes that can be used
     * for memory management. This method returns -1 if the maximum memory
     * size is undefined.
     */
    public static long max(MemoryUsage mu) {
        return mu.getMax();
    }

    /**
     * Returns the amount of used memory in bytes.
     */
    public static long used(MemoryUsage mu) {
        return mu.getUsed();
    }

    /**
     * Returns the approximate number of objects for
     * which finalization is pending.
     */
    public static long finalizationCount() {
        return mbeans.getMemoryMBean().getObjectPendingFinalizationCount();
    }

    /**
     * Dump the snapshot of the Java heap to a file in hprof
     * binary format. Only the live objects are dumped.
     * Under the current dir of traced app, ./btrace&lt;pid>/&lt;btrace-class>/
     * directory is created. Under that directory, a file of given
     * fileName is created.
     *
     * @param fileName name of the file to which heap is dumped
     */
    public static void dumpHeap(String fileName) {
        dumpHeap(fileName, true);
    }

    /**
     * Dump the snapshot of the Java heap to a file in hprof
     * binary format.
     * Under the current dir of traced app, ./btrace&lt;pid>/&lt;btrace-class>/
     * directory is created. Under that directory, a file of given
     * fileName is created.
     *
     * @param fileName name of the file to which heap is dumped
     * @param live flag that tells whether only live objects are
     *             to be dumped or all objects are to be dumped.
     */
    public static void dumpHeap(String fileName, boolean live) {
        try {
            String name = rt.getFilePath(fileName);
            mbeans.getHotSpotMBean().dumpHeap(name, live);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    /**
     * Runs the garbage collector.
     * <p>
     * Calling the <code>gc</code> method suggests that the Java Virtual
     * Machine expend effort toward recycling unused objects in order to
     * make the memory they currently occupy available for quick reuse.
     * When control returns from the method call, the Java Virtual
     * Machine has made a best effort to reclaim space from all discarded
     * objects. This method calls Sys.gc() to perform GC.
     * </p>
     */
    public static void gc() {
        java.lang.System.gc();
    }

    /**
     * Returns the total amount of time spent in GarbageCollection up to this point
     * since the application was started.
     * @return Returns the amount of overall time spent in GC
     */
    public static long getTotalGcTime() {
        long totalGcTime = 0;
        for (GarbageCollectorMXBean gcBean : mbeans.getGarbageCollectionMBeans()) {
            totalGcTime += gcBean.getCollectionTime();
        }
        return totalGcTime;
    }

    /**
     * Returns an overview of available memory pools <br>
     * It is possible to provide a text format the overview will use
     * @param poolFormat The text format string to format the overview. <br>
     *                   Exactly 5 arguments are passed to the format function. <br>
     *                   The format defaults to ";%1$s;%2$d;%3$d;%4$d;%5$d;Memory]"
     * @return Returns the formatted value of memory pools overview
     * @since 1.2
     */
    public static String getMemoryPoolUsage(String poolFormat) {
        if (poolFormat == null) {
            poolFormat = "%1$s;%2$d;%3$d;%4$d;%5$d";
        }
        List<MemoryPoolMXBean> memPoolList = mbeans.getMemoryPoolMXBeans();
    	Object[][] poolOutput = new Object[memPoolList.size()][5];

    	StringBuilder membuffer = new StringBuilder();

    	for (int i = 0; i < memPoolList.size(); i++) {
            MemoryPoolMXBean memPool = memPoolList.get(i);
            poolOutput[i][0] = memPool.getName();
            poolOutput[i][1] = new Long(memPool.getUsage().getMax());
            poolOutput[i][2] = new Long(memPool.getUsage().getUsed());
            poolOutput[i][3] = new Long(memPool.getUsage().getCommitted());
            poolOutput[i][4] = new Long(memPool.getUsage().getInit());

    	}
    	for (Object[] memPoolOutput : poolOutput) {
            membuffer.append(String.format(poolFormat, memPoolOutput)).append("\n");
        }

    	return membuffer.toString();
    }

    /**
     * Runs the finalization methods of any objects pending finalization.
     * <p>
     * Calling this method suggests that the Java Virtual Machine expend
     * effort toward running the <code>finalize</code> methods of objects
     * that have been found to be discarded but whose <code>finalize</code>
     * methods have not yet been run. When control returns from the
     * method call, the Java Virtual Machine has made a best effort to
     * complete all outstanding finalizations. This method calls
     * Sys.runFinalization() to run finalization.
     * </p>
     */
    public static void runFinalization() {
        java.lang.System.runFinalization();
    }
}
