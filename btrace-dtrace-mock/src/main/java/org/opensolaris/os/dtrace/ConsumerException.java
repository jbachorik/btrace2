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

/**
 * Exception thrown by a {@link ConsumerListener} to terminate a running
 * {@link Consumer}.
 *
 * @author Tom Erickson
 */
public class ConsumerException extends Exception {
    static final long serialVersionUID = -2125855097525822644L;

    /**
     * Creates a consumer exception with the given message.
     *
     * @see #ConsumerException(String message, Object
     * dtraceNotificationObject)
     */
    public
    ConsumerException(String message)
    {
	super(message);
    }

    /**
     * Creates an exception thrown by a {@link ConsumerListener}
     * implementation to terminate a running {@link Consumer}, usually
     * in response to a drop or an error reported by the native DTrace
     * library.  Optionally includes the object reported by the native
     * DTrace library so it can be used by an {@link ExceptionHandler}
     * to display details about why the consumer terminated.
     *
     * @param message   default display message explaining why the
     * consumer was terminated.
     * @param notification usually the object passed to a {@link
     * ConsumerListener} from DTrace that prompted this exception.  The
     * notification could be any of the following: <ul> <li>a {@link
     * Drop} passed to {@link ConsumerListener#dataDropped(DropEvent e)
     * dataDropped()}</li> <li>an {@link Error} passed to {@link
     * ConsumerListener#errorEncountered(ErrorEvent e)
     * errorEncountered()}</li> <li>a {@link ProcessState} passed to
     * {@link ConsumerListener#processStateChanged(ProcessEvent e)
     * processStateChanged()}</li> </ul> or it could be a user-defined
     * object that describes anything unexpected in {@link
     * ConsumerListener#dataReceived(DataEvent e) dataReceived()} or
     * that defines an arbitrary error threshold.  An {@link
     * ExceptionHandler} should be defined to handle any type of
     * notification object set by user code.  May be {@code null}.
     * @see Consumer#go(ExceptionHandler h)
     */
    public
    ConsumerException(String message, Object notification)
    {
	super(message);
    }

    /**
     * Gets the optional object from the {@link ConsumerListener} that
     * communicates to the {@link ExceptionHandler} why the listener
     * threw this exception.  Usually this is the object from DTrace
     * (such as an {@link org.opensolaris.os.dtrace.Error Error}) that
     * prompted the exception, simply forwarded to the exception
     * handler.
     *
     * @return an object that communicates to the {@link
     * ExceptionHandler} why the {@link ConsumerListener} threw this
     * exception, may be {@code null}
     * @see Consumer#go(ExceptionHandler h)
     * @see #ConsumerException(String message,
     * Object dtraceNotificationObject)
     */
    public Object
    getNotificationObject()
    {
	return null;
    }
}