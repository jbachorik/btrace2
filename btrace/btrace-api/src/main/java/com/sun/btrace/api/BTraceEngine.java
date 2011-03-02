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

package com.sun.btrace.api;

import java.util.ServiceLoader;

/**
 * This class serves as a factory for {@linkplain BTraceTask} instances
 * <p>
 * Usually, when using BTrace on a process with given PID one would do
 * <pre>
 * BTraceTask task = BTraceEngine.newInstance().createTask(PID)
 * </pre>
 * </p>
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@sun.com>
 */
public abstract class BTraceEngine {
    /**
     * Abstract factory class used for separation between API and its implementation
     */
    public static abstract class Factory {
        /**
         * Creates a new instance of {@linkplain BTraceEngine}
         * @return Returns a new instance of {@linkplain BTraceEngine}
         */
        abstract public BTraceEngine createEngine();
    }
    
    private static Factory factory;
    
    static {
        ServiceLoader<BTraceEngine.Factory> factoryLoader = ServiceLoader.load(Factory.class);
        factory = factoryLoader.iterator().next();
    }
    
    /**
     * Factory method for creating a new instance of {@linkplain BTraceEngine}.
     * The created instance should be cached for the further usage.
     * @return Returns a new instance of {@linkplain BTraceEngine}
     */
    final public static BTraceEngine newInstance() {
        return factory.createEngine();
    }
    
    /**
     * Abstract factory method for {@linkplain BTraceTask} instances
     * @param pid The application PID to create the task for
     * @return Returns a {@linkplain BTraceTask} instance bound to the particular java process
     *         or null if it is not possible to run BTrace against the application
     */
    abstract public BTraceTask createTask(int pid);
}
