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

package net.java.btrace.api.extensions;

import net.java.btrace.api.core.PerfReader;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.wireio.Command;
import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * A virtual BTrace extension context. 
 * <p>
 * Extensions can use it to get access to the thread specific BTrace runtime.
 * One should not create a new instance of the context as it will be injected
 * by BTrace runtime when needed.
 * </p>
 * @author Jaroslav Bachorik
 * @since 2.0
 */
final public class Runtime {
    private Runtime() {}
    /**
     * This method enables sending arbitrary {@linkplain Command} instances over the BTrace pipeline
     * @param cmd A valid instance of {@linkplain Command}. Custom commands may be used but they might be ignored by the BTrace client part.
     */
    public <T extends AbstractCommand> void send(Class<? extends T> cmdClass, AbstractCommand.Initializer<T> init) { throw new UnsupportedOperationException(); };
    /**
     * Returns identity string of the form class-name@identity-hash
     *
     * @param obj object for which identity string is returned
     * @return identity string
     */
    public String identityStr(Object obj) { throw new UnsupportedOperationException(); };
    /**
     * Returns the same hash code for the given object as
     * would be returned by the default method hashCode(),
     * whether or not the given object's class overrides
     * hashCode(). The hash code for the null reference is zero.
     *
     * @param  obj object for which the hashCode is to be calculated
     * @return the hashCode
     */
    public int identityHashCode(Object obj) { throw new UnsupportedOperationException(); };
    
    /**
     * Returns the shallow size of the given object
     * @param obj object for which the sizeof is to be calculated
     * @return the shallow object size
     */
    public long sizeof(Object obj) { throw new UnsupportedOperationException(); }
    
    /**
     * Returns a hash code value for the object. This method is supported
     * for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>. For bootstrap classes, returns the
     * result of calling Object.hashCode() override. For non-bootstrap classes,
     * the identity hash code is returned.
     *
     * @param obj the Object whose hash code is returned.
     * @return  a hash code value for the given object.
     */
    public int hash(Object obj) { throw new UnsupportedOperationException(); };
    /**
     * Indicates whether two given objects are "equal to" one another.
     * For bootstrap classes, returns the result of calling Object.equals()
     * override. For non-bootstrap classes, the reference identity comparison
     * is done.
     *
     * @param  obj1 first object to compare equality
     * @param  obj2 second object to compare equality
     * @return <code>true</code> if the given objects are equal;
     *         <code>false</code> otherwise.
     */
    public boolean compare(Object obj1, Object obj2) { throw new UnsupportedOperationException(); };
    /**
     * @return Returns an initialized {@linkplain PerfReader} instance
     */
    public PerfReader getPerfReader() { throw new UnsupportedOperationException(); };
    /**
     * Assembles a BTrace specific path for the given file name
     * @param fileName The file name without any directory information
     * @return Returns a full BTrace specific path to the file with a given file name
     */
    public String getFilePath(String fileName) { throw new UnsupportedOperationException(); };
    /**
     * A utility method to wrap a checked exception into a {@linkplain RuntimeException} instance
     * @param exp The exception to wrap
     * @return A new {@linkplain RuntimeException} instance - can be re-thrown and needs not be checked for
     */
    public RuntimeException translate(Exception exp) { throw new UnsupportedOperationException(); };
    /**
     * Marshalls the exception through the BTrace communication channel.
     * The exception details will be available to clients
     * @param e The exception to marshall
     */
    public void throwException(Exception e) { throw new UnsupportedOperationException(); };
    /**
     * BTrace runtime arguments count
     * @return Returns the number of the BTrace runtime arguments
     */
    public int $length() { throw new UnsupportedOperationException(); };
    /**
     * BTrace runtime arguments accessor
     * @param n The position (zero based) of the argument to be obtained
     * @return Returns the runtime argument on the n-th position or NULL
     */
    public String $(int n) { throw new UnsupportedOperationException(); };
    
    public String[] $$() { throw new UnsupportedOperationException(); };
    
    /**
     * Exits the BTrace agent with the given exit code
     * @param exitCode The exit code; will be passed to the BTrace client
     */
    public void exit(int exitCode) { throw new UnsupportedOperationException(); };
    /**
     * 
     * @return Returns the {@linkplain MemoryMXBean} MBean for the running JVM
     */
    public MemoryMXBean getMemoryMBean() { throw new UnsupportedOperationException(); };
    /**
     * 
     * @return Returns the {@linkplain RuntimeMXBean} MBean for the running JVM
     */
    public RuntimeMXBean getRuntimeMBean() { throw new UnsupportedOperationException(); };
    /**
     * 
     * @return Returns the {@linkplain HotSpotDiagnosticMXBean} MBean for the running JVM
     */
    public HotSpotDiagnosticMXBean getHotSpotMBean() { throw new UnsupportedOperationException(); };
    /**
     * 
     * @return Returns a list of {@linkplain GarbageCollectorMXBean} instances registered in the running JVM
     */
    public List<GarbageCollectorMXBean> getGarbageCollectionMBeans() { throw new UnsupportedOperationException(); };
    /**
     * 
     * @return Returns a list {@linkplain MemoryPoolMXBean} instances registered in the running JVM
     */
    public List<MemoryPoolMXBean> getMemoryPoolMXBeans() { throw new UnsupportedOperationException(); };
    /**
     * BTrace to DTrace communication channel.
     * Raise DTrace USDT probe from BTrace.
     *
     * @param s1 first String param to DTrace probe
     * @param s2 second String param to DTrace probe
     * @param i1 first int param to DTrace probe
     * @param i2 second int param to DTrace probe
     */
    public int dtraceProbe(String s1, String s2, int i1, int i2) { throw new UnsupportedOperationException(); };
}
