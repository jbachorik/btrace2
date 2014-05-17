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
package net.java.btrace.api.server;

import java.util.Observable;

/**
 * A session represents one connection between client and agent executing
 * one tracing class. A session resides in a {@linkplain Server} representing
 * the traced application.
 * @author Jaroslav Bachorik
 */
abstract public class Session extends Observable implements ShutdownHandler {
    /**
     * An enum describing the session state
     */
    public static enum State {
        /**
         * Session is not connected; the agent has been loaded, probe submitted
         * but the client has not yet been connected or the session has finished
         */
        DISCONNECTED,
        /**
         * Session is in the process of being disconnected
         */
        DISCONNECTING,
        /**
         * Session is connected; the agent has been loaded, probe submitted
         * and the client is receiving data
         */
        CONNECTED
    }

    /**
     * Send a named event in the scope of the session
     * @param name The event name
     */
    abstract public void event(String name);

    /**
     * Loads the precompiled and pre-verified trace class
     * @param traceCode The precompiled and pre-verified trace class (bytecode)
     * @param args An array of additional free-form arguments
     * @return Returns true if the trace class has been successfully loaded
     */
    abstract public boolean loadTraceClass(byte[] traceCode, String[] args);

    /**
     * Ends the session; performs cleanup and confirms the success if necessary
     * @param detachHook A hook executed right before the session is completely destroyed or NULL
     * @return Returns false if the detach fails for any reason
     */
    abstract public boolean detach(Runnable detachHook);

    /**
     * Ends the session; performs cleanup and confirms the success if necessary
     * @see Session#detach(java.lang.Runnable) with detachHook set to NULL
     * @return
     */
    final public boolean detach() {
        return detach(null);
    }

    /**
     * Start the previously configured session
     */
    abstract public void start();
}
