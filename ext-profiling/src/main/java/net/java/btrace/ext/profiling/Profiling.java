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

package net.java.btrace.ext.profiling;

import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.wireio.commands.GridDataCommand;
import javax.annotation.Resource;
import net.java.btrace.api.extensions.runtime.CommLine;

/**
 * Profiling support. It is a highly specialized aggregation (therefore not
 * included in the generic aggregations support) which is able to calculate
 * clean self time spent in hierarchically called methods (or bigger parts
 * of code)
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class Profiling {
    @Resource
    private static CommLine l;
    
    public static void printSnapshot(final String name, final Profiler.Snapshot snapshot) {
        l.send(GridDataCommand.class, new AbstractCommand.Initializer<GridDataCommand>() {
            public void init(GridDataCommand cmd) {
                cmd.setName(name);
                cmd.setPayload(new GridDataCommand.GridData(null, snapshot.getGridData()));
            }
        });
    }

    /**
     * Prints profiling snapshot using the provided format
     * @param name The name of the aggregation to be used in the textual output
     * @param snapshot The snapshot to print
     * @param format The format to use. It mimics {@linkplain String#format(java.lang.String, java.lang.Object[]) } behaviour
     *               with the addition of the ability to address the key title as a 0-indexed item
     * @see String#format(java.lang.String, java.lang.Object[])
     */
    public static void printSnapshot(final String name, final Profiler.Snapshot snapshot, final String format) {
        l.send(GridDataCommand.class, new AbstractCommand.Initializer<GridDataCommand>() {
            public void init(GridDataCommand cmd) {
                cmd.setName(name);
                cmd.setPayload(new GridDataCommand.GridData(format, snapshot.getGridData()));
            }
        });
    }
    
    /**
     * Creates a new {@linkplain Profiler} instance
     * @return A new {@linkplain Profiler} instance
     */
    public static Profiler newProfiler() {
        return new MethodInvocationProfiler(600);
    }

    /**
     * Creates a new {@linkplain Profiler} instance with the specified
     * expected count of the distinct methods to be recorded.
     * @param expectedBlockCnt The expected count of the distinct blocks
     *                          to be recorded.
     * @return Returns a new {@linkplain Profiler} instance
     */
    public static Profiler newProfiler(int expectedBlockCnt) {
        return new MethodInvocationProfiler(expectedBlockCnt);
    }

    /**
     * Records the entry to a particular code block
     * @param profiler The {@linkplain Profiler} instance to use
     * @param blockName The block identifier
     */
    public static void recordEntry(Profiler profiler, String blockName) {
        profiler.recordEntry(blockName);
    }

    /**
     * Records the exit out of a particular code block
     * @param profiler The {@linkplain Profiler} instance to use
     * @param blockName The block identifier
     * @param duration The time spent in the mentioned block
     */
    public static void recordExit(Profiler profiler, String blockName, long duration) {
        profiler.recordExit(blockName, duration);
    }

    /**
     * Creates a new snapshot of the profiling metrics collected sofar
     * @param profiler The {@linkplain Profiler} instance to use
     * @return Returns an immutable snapshot of the profiling metrics in
     *         the form of a map where the key is the block name and
     *         the value is a map of metrics names and the appropriate
     *         values<br>
     *         The supported metrics names are: "selfTime", "wallTime" and
     *         "invocations"
     */
    public static Profiler.Snapshot snapshot(Profiler profiler) {
        return profiler.snapshot();
    }

    public static Profiler.Snapshot snapshotAndReset(Profiler profiler) {
        return profiler.snapshot(true);
    }

    public static void resetProfiler(Profiler profiler) {
        profiler.reset();
    }
}
