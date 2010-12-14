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

import java.util.*;
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