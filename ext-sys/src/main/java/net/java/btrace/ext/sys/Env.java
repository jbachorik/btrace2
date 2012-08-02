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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;
import sun.security.action.GetPropertyAction;

/*
 * Wraps the environment related BTrace utility methods
 * @since 1.3
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class Env {

    /**
     * Gets the system property indicated by the specified key.
     *
     * @param      key   the name of the system property.
     * @return     the string value of the system property,
     *             or <code>null</code> if there is no property with that key.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static String property(String key) {
        return AccessController.doPrivileged(
            new GetPropertyAction(key));
    }

    /**
     * Returns all Sys properties.
     *
     * @return the system properties
     */
    public static Properties properties() {
        return AccessController.doPrivileged(
            new PrivilegedAction<Properties>() {
                public Properties run() {
                    return System.getProperties();
                }
            }
        );
    }

    /**
     * Prints all Sys properties.
     */
    public static void printProperties() {
//                BTraceRuntime.printMap(properties());
    }

    /**
     * Gets the value of the specified environment variable. An
     * environment variable is a system-dependent external named
     * value.
     *
     * @param  name the name of the environment variable
     * @return the string value of the variable, or <code>null</code>
     *         if the variable is not defined in the system environment
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     */
    public static String getenv(final String name) {
        return AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                public String run() {
                    return System.getenv(name);
                }
            }
        );
    }

    /**
     * Returns an unmodifiable string map view of the current system environment.
     * The environment is a system-dependent mapping from names to
     * values which is passed from parent to child processes.
     *
     * @return the environment as a map of variable names to values
     */
    public static Map<String, String> getenv() {
        return AccessController.doPrivileged(
            new PrivilegedAction<Map<String, String>>() {
                public Map<String, String> run() {
                    return System.getenv();
                }
            }
        );
    }

    /**
     * Prints all system environment values.
     */
    public static void printEnv() {
//                BTraceRuntime.printMap(getenv());
    }

    /**
     * Returns the number of processors available to the Java virtual machine.
     *
     * <p> This value may change during a particular invocation of the virtual
     * machine.  Applications that are sensitive to the number of available
     * processors should therefore occasionally poll this property and adjust
     * their resource usage appropriately. </p>
     *
     * @return  the maximum number of processors available to the virtual
     *          machine; never smaller than one
     */
    public static long availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }
}
