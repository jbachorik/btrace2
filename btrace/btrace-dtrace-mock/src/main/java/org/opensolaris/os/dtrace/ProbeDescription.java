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
import java.text.ParseException;
import java.io.*;
import java.beans.*;

/**
 * A DTrace probe description consists of provider, module, function,
 * and name.  A single probe description may identify a single DTrace
 * probe or match multiple probes.  Any field may be wildcarded by
 * omission (set to null) or set to a glob-style pattern:
 * <pre>
 *    *		Matches any string, including the null string
 *    ?		Matches any single character
 *    [ ... ]	Matches any one of the enclosed characters. A pair of
 *    			characters separated by - matches any character
 *    			between the pair, inclusive. If the first
 *    			character after the [ is !, any character not
 *    			enclosed in the set is matched.
 *    \		Interpret the next character as itself, without any
 *    			special meaning
 * </pre>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @see Consumer#listProbes(ProbeDescription filter)
 *
 * @author Tom Erickson
 */
public final class ProbeDescription implements Serializable,
        Comparable <ProbeDescription>
{
    static final long serialVersionUID = 5978023304364513667L;

    /**
     * Instance with empty provider, module, function, and name fields
     * matches all DTrace probes on a system.
     */
    public static final ProbeDescription EMPTY =
	    new ProbeDescription(null, null, null, null);

    /**
     * Enumerates the provider, module, function, and name fields of a
     * probe description.
     */
    public enum Spec {
	/** Probe provider */
	PROVIDER,
	/** Probe module */
	MODULE,
	/** Probe function */
	FUNCTION,
	/** Probe name (unqualified) */
	NAME
    };

    /**
     * Creates a fully qualified probe description from the name given
     * in the format <i>{@code provider:module:function:name}</i> or
     * else a probe description that specifies only the unqualified
     * probe name.
     *
     * @param probeName either the fully qualified name in the format
     * <i>{@code provider:module:function:name}</i> or else (if no colon
     * is present) the unqualified name interpreted as {@code
     * :::probeName}
     * @see ProbeDescription#ProbeDescription(String probeProvider,
     * String probeModule, String probeFunction, String probeName)
     * @see ProbeDescription#parse(String s)
     */
    public
    ProbeDescription(String probeName)
    {
    }

    /**
     * Creates a probe description that specifies the probe name
     * qualified only by the function name.
     *
     * @see ProbeDescription#ProbeDescription(String probeProvider,
     * String probeModule, String probeFunction, String probeName)
     */
    public
    ProbeDescription(String probeFunction, String probeName)
    {
	this(null, null, probeFunction, probeName);
    }

    /**
     * Creates a probe description that specifies the probe name
     * qualified by the function name and module name.
     *
     * @see ProbeDescription#ProbeDescription(String probeProvider,
     * String probeModule, String probeFunction, String probeName)
     */
    public
    ProbeDescription(String probeModule, String probeFunction,
	    String probeName)
    {
	this(null, probeModule, probeFunction, probeName);
    }

    /**
     * Creates a fully qualified probe description.  If no pattern
     * syntax is used and no field is omitted, the resulting description
     * matches at most one DTrace probe.
     *
     * @param probeProvider provider name, may be null or empty to match
     * all providers or use pattern syntax to match multiple providers
     * @param probeModule module name, may be null or empty to match all
     * modules or use pattern syntax to match multiple modules
     * @param probeFunction function name, may be null or empty to match
     * all functions or use pattern syntax to match multiple functions
     * @param probeName unqualified probe name, may be null or empty to
     * match all names or use pattern syntax to match multiple names
     */
    public
    ProbeDescription(String probeProvider,
	    String probeModule,
	    String probeFunction,
	    String probeName)
    {
    }

    /**
     * Supports XML persistence.
     */
    public
    ProbeDescription(int probeID,
	    String probeProvider,
	    String probeModule,
	    String probeFunction,
	    String probeName)
    {
	this(probeProvider, probeModule, probeFunction, probeName);
    }

    /**
     * Generates a probe description from a string in the same format
     * returned by {@link #toString()}.  Parses the string from right to
     * left.
     * <pre><code>
     * <i>provider:module:function:name</i>
     * </code></pre>
     *
     * @return non-null probe description
     * @throws ParseException if {@code s} does not have the expected
     * format.  The error offset is the index of the first unexpected
     * character encountered starting from the last character and
     * reading backwards.
     * @throws NullPointerException if the given string is {@code null}
     */
    public static ProbeDescription
    parse(String s) throws ParseException
    {
	return null;
    }

    /**
     * Gets the probe ID.
     *
     * @return ID generated from a sequence by the native DTrace
     * library, identifies the probe among all probes on the system
     */
    public int
    getID()
    {
	return 0;
    }

    /**
     * Gets the provider name.
     *
     * @return non-null provider name, may be an empty string to
     * indicate omission
     */
    public String
    getProvider()
    {
	return "";
    }

    /**
     * Gets the module name.
     *
     * @return non-null module name, may be an empty string to indicate
     * omission
     */
    public String
    getModule()
    {
	return "";
    }

    /**
     * Gets the function name.
     *
     * @return non-null function name, may be an empty string to
     * indicate omission
     */
    public String
    getFunction()
    {
	return "";
    }

    /**
     * Gets the unqualified probe name.
     *
     * @return non-null probe name, may be an empty string to indicate
     * omission
     */
    public String
    getName()
    {
	return "";
    }

    /**
     * Returns {@code true} if provider, module, function, and name are
     * all omitted.  An empty probe description matches all DTrace
     * probes on a system.
     *
     * @return {@code true} if all probe fields are omitted, {@code
     * false} otherwise
     */
    public boolean
    isEmpty()
    {
	return true;
    }

    /**
     * Compares the specified object with this probe description for
     * equality.  Defines equality as having the same fields.  Omitted
     * fields must be omitted in both instances in order for them to be
     * equal, but it makes no difference whether {@code null} or empty
     * string was used to indicate omission.
     *
     * @return {@code true} if and only if all corresponding fields of
     * both probe descriptions are either both omitted (null or empty)
     * or else equal as defined by {@link String#equals(Object o)
     * String.equals()}
     */
    public boolean
    equals(Object o)
    {
	return false;
    }

    /**
     * Defines the natural ordering of probe descriptions.  Returns the
     * natural ordering of the first unequal pair of corresponding
     * fields (starting with the provider and continuing to the
     * unqualified name only if all other fields are equal).
     * Corresponding fields are equal if they are both omitted or both
     * equal as defined by {@link String#equals(Object o)
     * String.equals()}.  It makes no difference if {@code null} or
     * empty string is used to indicate omission.  The behavior is
     * consistent with the {@link #equals(Object o) equals()} method.
     *
     * @return -1, 0, or 1 as this probe description is less than, equal
     * to, or greater than the given probe description
     */
    public int
    compareTo(ProbeDescription p)
    {
	return 0;
    }

    /**
     * Overridden to ensure that equal probe descriptions have equal
     * hashcodes.
     */
    @Override
    public int
    hashCode()
    {
	return 0;
    }
}