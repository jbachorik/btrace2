/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2007 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 *
 * ident	"%Z%%M%	%I%	%E% SMI"
 */
package org.opensolaris.os.dtrace;

import java.io.*;
import java.beans.*;

/**
 * State of a target process designated by {@link
 * Consumer#createProcess(String command)} or {@link
 * Consumer#grabProcess(int pid)}.
 * <p>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @see ConsumerListener#processStateChanged(ProcessEvent e)
 *
 * @author Tom Erickson
 */
public final class ProcessState implements Serializable {
    static final long serialVersionUID = -3395911213431317292L;

    /**
     * State of a target process.
     */
    public enum State {
	/** Process is running. */
	RUN,
	/** Process is stopped. */
	STOP,
	/** Process is lost to control. */
	LOST,
	/** Process is terminated (zombie). */
	UNDEAD,
	/** Process is terminated (core file). */
	DEAD
    }

    /**
     * Creates a {@code ProcessState} instance with the given state.
     *
     * @param pid non-negative target process ID
     * @param processState target process state
     * @param processTerminationSignal signal that terminated the target
     * process, {@code -1} if the process was not terminated by a signal
     * or if the terminating signal is unknown
     * @param processTerminationSignalName name of the signal that
     * terminated the target process, {@code null} if the process was
     * not terminated by a signal or if the terminating signal is
     * unknown
     * @param processExitStatus target process exit status, {@code null}
     * if the process has not exited or the exit status is unknown
     * @param msg message included by DTrace, if any
     * @throws NullPointerException if the given process state is {@code
     * null}
     * @throws IllegalArgumentException if the given process ID is negative
     */
    public
    ProcessState(int pid, State processState,
	    int processTerminationSignal,
	    String processTerminationSignalName,
	    Integer processExitStatus, String msg)
    {
    }

    /**
     * Supports XML persistence.
     *
     * @see #ProcessState(int pid, State processState, int
     * processTerminationSignal, String processTerminationSignalName,
     * Integer processExitStatus, String msg)
     * @throws IllegalArgumentException if there is no {@link
     * ProcessState.State} value with the given state name.
     */
    public
    ProcessState(int pid, String processStateName,
	    int processTerminationSignal,
	    String processTerminationSignalName,
	    Integer processExitStatus, String msg)
    {
    }

    /**
     * Gets the process ID.
     *
     * @return non-negative target process ID
     */
    public int
    getProcessID()
    {
	return 0;
    }

    /**
     * Gets the process state.
     *
     * @return non-null target process state
     */
    public State
    getState()
    {
	return State.DEAD;
    }

    /**
     * Gets the signal that terminated the process.
     *
     * @return termination signal, {@code -1} if the process was not
     * terminated by a signal or if the terminating signal is unknown
     */
    public int
    getTerminationSignal()
    {
	return -1;
    }

    /**
     * Gets the name of the signal that terminated the process.
     *
     * @return termination signal name, {@code null} if the process was
     * not terminated by a signal or if the terminating signal is
     * unknown
     */
    public String
    getTerminationSignalName()
    {
	return null;
    }

    /**
     * Gets the process exit status.
     *
     * @return exit status, or {@code null} if the process has not
     * exited or the exit status is unknown
     */
    public Integer
    getExitStatus()
    {
	return null;
    }

    /**
     * Gets the message from DTrace describing this process state.
     *
     * @return DTrace message, or {@code null} if DTrace did not include
     * a message with this process state
     */
    public String
    getMessage()
    {
	return "";
    }
}