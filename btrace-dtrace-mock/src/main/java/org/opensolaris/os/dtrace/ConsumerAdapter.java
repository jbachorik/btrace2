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
 * An abstract adapter class for getting events from a {@link Consumer}.
 * The methods in this class are empty except for a few that implement
 * the default behavior of terminating a consumer by throwing an
 * exception.  This class exists as a convenience for implementing
 * consumer listeners.
 *
 * @see Consumer#addConsumerListener(ConsumerListener l)
 *
 * @author Tom Erickson
 */
public abstract class ConsumerAdapter implements ConsumerListener {
    /** Empty method */
    public void dataReceived(DataEvent e) throws ConsumerException {}

    /**
     * Terminates a running {@link Consumer} by throwing an exception.
     *
     * @throws ConsumerException
     */
    public void
    dataDropped(DropEvent e) throws ConsumerException
    {
	throw new ConsumerException("", null);
    }

    /**
     * Terminates a running {@link Consumer} by throwing an exception.
     *
     * @throws ConsumerException
     */
    public void
    errorEncountered(ErrorEvent e) throws ConsumerException
    {
	throw new ConsumerException("", null);
    }

    /** Empty method */
    public void processStateChanged(ProcessEvent e) throws ConsumerException {}
    /** Empty method */
    public void consumerStarted(ConsumerEvent e) {}
    /** Empty method */
    public void consumerStopped(ConsumerEvent e) {}
    /** Empty method */
    public void intervalBegan(ConsumerEvent e) {}
    /** Empty method */
    public void intervalEnded(ConsumerEvent e) {}
}