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
import javax.annotation.Resource;
import net.java.btrace.api.extensions.runtime.Arguments;

/*
 * Wraps the OS related BTrace utility methods
 * @since 1.3
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class Process {
    @Resource
    private static Arguments args;
    @Resource
    private static Runtime rt;
    
    /**
     * Returns n'th command line argument. <code>null</code> if not available.
     *
     * @param n command line argument index
     * @return n'th command line argument
     */
    public static String $(int n) {
        return args.$(n);
    }

    /**
     * Returns the process id of the currently BTrace'd process.
     */
    public static int getpid() {
        int pid = -1;
        try {
            pid = Integer.parseInt($(0));
        } catch (Exception ignored) {
        }
        return pid;
    }

    /**
     * Returns the number of command line arguments.
     */
    public static int $length() {
        return args.$length();
    }

    /**
     * Exits the BTrace session -- note that the particular client's tracing
     * session exits and not the observed/traced program! After exit call,
     * the trace action method terminates immediately and no other probe action
     * method (of that client) will be called after that.
     *
     * @param exitCode exit value sent to the client
     */
    public static void exit(int exitCode) {
        rt.exit(exitCode);
    }

    /**
     * This is same as exit(int) except that the exit code
     * is zero.
     *
     * @see #exit(int)
     */
    public static void exit() {
        exit(0);
    }
}
