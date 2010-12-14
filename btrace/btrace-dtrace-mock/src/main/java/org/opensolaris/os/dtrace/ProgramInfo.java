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
 * Information about a {@link Program} including stability and matching
 * probe count.
 * <p>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @see Consumer#getProgramInfo(Program program)
 * @see Consumer#enable(Program program)
 *
 * @author Tom Erickson
 */
public final class ProgramInfo implements Serializable {
    static final long serialVersionUID = 663862981171935056L;

    /**
     * Creates a {@code ProgamInfo} instance with the given properties.
     * Supports XML persistence.
     *
     * @param minProbeAttr minimum stability levels of the
     * program probe descriptions
     * @param minStatementAttr minimum stability levels of the
     * program action statements (including D variables)
     * @param matchingProbes non-negative count of probes matching the
     * program probe description
     * @throws NullPointerException if {@code minProbeAttr} or {@code
     * minStatementAttr} is {@code null}
     * @throws IllegalArgumentException if {@code matchingProbes} is
     * negative
     */
    public
    ProgramInfo(InterfaceAttributes minProbeAttr,
	    InterfaceAttributes minStatementAttr,
	    int matchingProbes)
    {
    }

    /**
     * Gets the minimum stability levels of the probe descriptions used
     * in a compiled {@link Program}.
     *
     * @return non-null interface attributes describing the minimum
     * stability of the probe descriptions in a D program
     */
    public InterfaceAttributes
    getMinimumProbeAttributes()
    {
	return null;
    }

    /**
     * Gets the minimum stability levels of the action statements
     * including D variables used in a compiled {@link Program}.
     *
     * @return non-null interface attributes describing the minimum
     * stability of the action statements (including D variables) in a D
     * program
     */
    public InterfaceAttributes
    getMinimumStatementAttributes()
    {
	return null;
    }

    /**
     * Gets the number of DTrace probes that match the probe
     * descriptions in a compiled {@link Program}.  This count may be
     * very high for programs that use {@link ProbeDescription}
     * wildcarding (field omission) and globbing (pattern matching
     * syntax).
     *
     * @return non-negative count of probes on the system matching the
     * program descriptions in a compiled D program
     */
    public int
    getMatchingProbeCount()
    {
	return 0;
    }
}