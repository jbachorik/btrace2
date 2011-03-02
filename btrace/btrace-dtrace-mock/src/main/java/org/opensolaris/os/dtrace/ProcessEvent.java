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
package org.opensolaris.os.dtrace;

import java.io.*;
import java.util.EventObject;

/**
 * Notification that the state of a target process designated by {@link
 * Consumer#createProcess(String command)} or {@link
 * Consumer#grabProcess(int pid)} has changed.
 *
 * @see ConsumerListener#processStateChanged(ProcessEvent e)
 *
 * @author Tom Erickson
 */
public class ProcessEvent extends EventObject {
    static final long serialVersionUID = -3779443761929558702L;

    /**
     * Creates a {@link ConsumerListener#processStateChanged(ProcessEvent e)
     * processStateChanged()} event to notify listeners of a process
     * state change.
     *
     * @param source the {@link Consumer} that is the source of this event
     * @throws NullPointerException if the given process state is {@code
     * null}
     */
    public
    ProcessEvent(Object source, ProcessState p)
    {
	super(source);
    }

    /**
     * Gets the process state.
     *
     * @return non-null process state
     */
    public ProcessState
    getProcessState()
    {
	return null;
    }
}