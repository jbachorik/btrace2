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
 * User-defined application behavior after an exception terminates
 * a running DTrace consumer.  The {@link Consumer} that threw the
 * exception is stopped automatically whether or not an {@code
 * ExceptionHandler} is set, but a handler must be set to do anything
 * other than print a stack trace to {@code stderr}.
 *
 * @see Consumer#go(ExceptionHandler handler)
 *
 * @author Tom Erickson
 */
public interface ExceptionHandler {
    /**
     * Defines what to do after an exception terminates a running {@link
     * Consumer}.  For example, a handler might be implemented to
     * display details about what went wrong.
     *
     * @param e  a {@link DTraceException} if encountered in the native
     * DTrace library, a {@link ConsumerException} if thrown from a
     * {@link ConsumerListener} method to terminate the consumer, or a
     * {@link RuntimeException} to indicate an unexpected error in the
     * Java DTrace API.
     */
    public void handleException(Throwable e);
}