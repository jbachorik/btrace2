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

package net.java.btrace.api.extensions.runtime;

/**
 * JVM runtime acecssor
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 * @since 2.0
 */
public interface Runtime {
    /**
     * Assembles a BTrace specific path for the given file name
     * @param fileName The file name without any directory information
     * @return Returns a full BTrace specific path to the file with a given file name
     */
    public String getFilePath(String fileName);
    /**
     * Exits the BTrace agent with the given exit code
     * @param exitCode The exit code; will be passed to the BTrace client
     */
    public void exit(int exitCode);
    
    /**
     * BTrace to DTrace communication channel.
     * Raise DTrace USDT probe from BTrace.
     *
     * @param s1 first String param to DTrace probe
     * @param s2 second String param to DTrace probe
     * @param i1 first int param to DTrace probe
     * @param i2 second int param to DTrace probe
     */
    public int dtraceProbe(String s1, String s2, int i1, int i2);
}
