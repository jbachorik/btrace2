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
import java.beans.*;

/**
 * Probe stability information.  Does not identify a probe, but gives
 * information about a single probe identified by a {@link
 * ProbeDescription}.  A {@code ProbeDescription} can match multiple
 * probes using pattern syntax (globbing) and wildcarding (field
 * omission), but it does not normally make sense to associate a {@code
 * ProbeInfo} with a {@code ProbeDescription} unless that description
 * matches exactly one probe on the system.  A {@link Probe} pairs a
 * {@code ProbeDescription} with information about the DTrace probe it
 * identifies.
 * <p>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @see Consumer#listProbeDetail(ProbeDescription filter)
 * @see Consumer#listProgramProbeDetail(Program program)
 *
 * @author Tom Erickson
 */
public final class ProbeInfo implements Serializable {
    static final long serialVersionUID = 1057402669978245904L;

    /**
     * Creates a {@code ProbeInfo} instance from the given attributes.
     * Supports XML persistence.
     *
     * @throws NullPointerException if any parameter is null
     */
    public
    ProbeInfo(InterfaceAttributes singleProbeAttributes,
	    InterfaceAttributes argAttributes)
    {
    }

    /**
     * Gets the interface attributes of a probe.
     *
     * @return non-null attributes including stability levels and
     * dependency class
     */
    public InterfaceAttributes
    getProbeAttributes()
    {
	return null;
    }

    /**
     * Gets the interface attributes of the arguments to a probe.
     *
     * @return non-null attributes including stability levels and
     * dependency class of the arguments to a probe
     */
    public InterfaceAttributes
    getArgumentAttributes()
    {
	return null;
    }
}