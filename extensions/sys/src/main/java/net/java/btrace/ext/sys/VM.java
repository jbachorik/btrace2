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

import net.java.btrace.ext.Printer;
import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.extensions.runtime.Runtime;
import java.util.List;
import javax.annotation.Resource;
import net.java.btrace.api.extensions.runtime.MBeans;
import net.java.btrace.api.extensions.runtime.Objects;

/*
 * Wraps the VM related BTrace utility methods
 * @since 1.3
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class VM {
    @Resource
    private static MBeans mbeans;
    @Resource
    private static Runtime rt;
    @Resource
    private static Objects objects;
    
    /**
     * Returns the input arguments passed to the Java virtual machine
     * which does not include the arguments to the <tt>main</tt> method.
     * This method returns an empty list if there is no input argument
     * to the Java virtual machine.
     * <p>
     * Some Java virtual machine implementations may take input arguments
     * from multiple different sources: for examples, arguments passed from
     * the application that launches the Java virtual machine such as
     * the 'java' command, environment variables, configuration files, etc.
     * <p>
     * Typically, not all command-line options to the 'java' command
     * are passed to the Java virtual machine.
     * Thus, the returned input arguments may not
     * include all command-line options.
     *
     * @return a list of <tt>String</tt> objects; each element
     * is an argument passed to the Java virtual machine.
     */
    public static List<String> vmArguments() {
        return mbeans.getRuntimeMBean().getInputArguments();
    }

    /**
     * Prints VM input arguments list.
     *
     * @see #vmArguments
     */
    public static void printVmArguments() {
        Printer.println(vmArguments());
    }

    /**
     * Returns the Java virtual machine implementation version.
     * This method is equivalent to <b>Sys.getProperty("java.vm.version")}</b>.
     *
     * @return the Java virtual machine implementation version.
     */
    public static String vmVersion() {
        return mbeans.getRuntimeMBean().getVmVersion();
    }

    /**
     * Tests if the Java virtual machine supports the boot class path
     * mechanism used by the bootstrap class loader to search for class
     * files.
     *
     * @return <tt>true</tt> if the Java virtual machine supports the
     * class path mechanism; <tt>false</tt> otherwise.
     */
    public static boolean isBootClassPathSupported() {
        return mbeans.getRuntimeMBean().isBootClassPathSupported();
    }

    /**
     * Returns the boot class path that is used by the bootstrap class loader
     * to search for class files.
     *
     * <p> Multiple paths in the boot class path are separated by the
     * path separator character of the platform on which the Java
     * virtual machine is running.
     *
     * <p>A Java virtual machine implementation may not support
     * the boot class path mechanism for the bootstrap class loader
     * to search for class files.
     * The {@link #isBootClassPathSupported} method can be used
     * to determine if the Java virtual machine supports this method.
     *
     * @return the boot class path.
     * @throws java.lang.UnsupportedOperationException
     *     if the Java virtual machine does not support this operation.
     */
    public static String bootClassPath() {
        return mbeans.getRuntimeMBean().getBootClassPath();
    }

    /**
     * Returns the Java class path that is used by the system class loader
     * to search for class files.
     * This method is equivalent to <b>Sys.getProperty("java.class.path")</b>.
     *
     * @return the Java class path.
     */
    public static String classPath() {
        return Env.property("java.class.path");
    }

    /**
     * Returns the Java library path.
     * This method is equivalent to <b>Sys.getProperty("java.library.path")</b>.
     *
     * <p> Multiple paths in the Java library path are separated by the
     * path separator character of the platform of the Java virtual machine
     * being monitored.
     *
     * @return the Java library path.
     */
    public static String libraryPath() {
        return Env.property("java.library.path");
    }

    /**
     * Returns the start time of the Java virtual machine in milliseconds.
     * This method returns the approximate time when the Java virtual
     * machine started.
     *
     * @return start time of the Java virtual machine in milliseconds.
     */
    public static long vmStartTime() {
        return mbeans.getRuntimeMBean().getStartTime();
    }

    /**
     * Returns the uptime of the Java virtual machine in milliseconds.
     *
     * @return uptime of the Java virtual machine in milliseconds.
     */
    public static long vmUptime() {
        return mbeans.getRuntimeMBean().getUptime();
    }
    
    // BTrace exit built-in function
    public static void exit(int exitCode) {
        rt.exit(exitCode);
    }
    
    /**
     * Returns the class loader for the given class. Some implementations may use
     * null to represent the bootstrap class loader. This method will return
     * null in such implementations if this class was loaded by the bootstrap
     * class loader.
     *
     * @param clazz the Class for which the class loader is returned
     */
    public static ClassLoader loader(Class clazz) {
        return clazz.getClassLoader();
    }

    /**
     * Returns the parent class loader of the given loader. Some implementations may
     * use <tt>null</tt> to represent the bootstrap class loader. This method
     * will return <tt>null</tt> in such implementations if this class loader's
     * parent is the bootstrap class loader.
     *
     * @param  loader the loader for which the parent loader is returned
     * @return The parent <tt>ClassLoader</tt>
     */
    public static ClassLoader parentLoader(ClassLoader loader) {
        return loader.getParent();
    }
    
    /**
     * Returns identity string of the form class-name@identity-hash
     *
     * @param obj object for which identity string is returned
     * @return identity string
     */
    public static String identityStr(Object obj) {
        return identityStr(obj);
    }
    
    /**
     * Returns the shallow size of the given object
     * @param obj object for which the sizeof is to be calculated
     * @return the shallow object size
     */
    public static long sizeof(Object obj) {
        return objects.sizeof(obj);
    }

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
    public static int hash(Object obj) {
        return objects.hash(obj);
    }

    /**
     * Returns the same hash code for the given object as
     * would be returned by the default method hashCode(),
     * whether or not the given object's class overrides
     * hashCode(). The hash code for the null reference is zero.
     *
     * @param  obj object for which the hashCode is to be calculated
     * @return the hashCode
     */
    public static int identityHashCode(Object obj) {
        return objects.identityHashCode(obj);
    }

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
    public static boolean compare(Object obj1, Object obj2) {
        return objects.compare(obj1, obj2);
    }
}
