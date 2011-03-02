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