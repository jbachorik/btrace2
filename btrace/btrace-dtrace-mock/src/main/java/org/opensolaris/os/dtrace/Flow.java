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
 * Description of control flow across function boundaries including
 * direction (entry or return) and depth in the call stack.  This
 * information is added to {@link ProbeData} instances only when the
 * {@link Option#flowindent flowindent} option is used:
 * <pre><code>
 *     Consumer consumer = new LocalConsumer();
 *     consumer.open();
 *     consumer.setOption(Option.flowindent);
 *     ...
 * </code></pre>
 * See the <a
 * href="http://docs.sun.com/app/docs/doc/817-6223/6mlkidlk1?a=view">
 * <b>Examples</b></a> section of the <b>{@code fbt}
 * Provider</b> chapter of the <i>Solaris Dynamic Tracing Guide</i>.
 * <p>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @see Consumer#setOption(String option)
 * @see Option#flowindent
 *
 * @author Tom Erickson
 */
public final class Flow implements Serializable {
    static final long serialVersionUID = -9178272444872063901L;

    /**
     * Indicates direction of flow across a boundary, such as entering
     * or returing from a function.
     */
    public enum Kind {
	/** Entry into a function. */
	ENTRY,
	/** Return from a function. */
	RETURN,
	/** No function boundary crossed. */
	NONE
    }

    /**
     * Creates a {@code Flow} instance with the given flow kind and
     * depth.  Supports XML persistence.
     *
     * @param flowKindName name of enumeration value indicating the
     * direction of flow
     * @param flowDepth current depth in the call stack
     * @throws IllegalArgumentException if there is no {@code Flow.Kind}
     * value with the given name or if the given {@code flowDepth} is
     * negative
     * @throws NullPointerException if the given {@code Flow.Kind} name
     * is {@code null}
     */
    public
    Flow(String flowKindName, int flowDepth)
    {
    }

    /**
     * Gets the direction of the flow of control (entry or return)
     * across a function boundary.
     *
     * @return non-null flow kind indicating direction of flow (entry or
     * return) across a function boundary
     */
    public Kind
    getKind()
    {
	return Kind.NONE;
    }

    /**
     * Gets the current depth in the call stack.
     *
     * @return A non-negative sum of the function entries minus the
     * function returns up until the moment described by this control
     * flow instance.  For example, if the traced flow of control
     * entered two functions but only returned from one, the depth is
     * one (2 entries minus 1 return).
     */
    public int
    getDepth()
    {
	return 0;
    }
}