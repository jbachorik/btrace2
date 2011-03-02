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
 * Detail about one or more records dropped by DTrace (not reported to
 * {@link ConsumerListener#dataReceived(DataEvent e)
 * ConsumerListener.dataReceived()}) due to inadequate buffer space.
 * <p>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @see ConsumerListener#dataDropped(DropEvent e)
 *
 * @author Tom Erickson
 */
public final class Drop implements Serializable {
    static final long serialVersionUID = 26653827678657381L;

    /**
     * Indicates what kind of buffer space experienced the data drop
     * (such as principal buffer or aggregation buffer) and possibly a
     * reason.
     */
    public enum Kind {
	/** Drop to principal buffer */
	PRINCIPAL("Principal buffer"),
	/** Drop to aggregation buffer */
	AGGREGATION("Aggregation"),
	/** Dynamic drop */
	DYNAMIC("Dynamic"),
	/** Dynamic drop due to rinsing */
	DYNRINSE("Dynamic (rinse)"),
	/** Dynamic drop due to dirtiness */
	DYNDIRTY("Dynamic (dirty)"),
	/** Speculative drop */
	SPEC("Speculation"),
	/** Speculative drop due to business */
	SPECBUSY("Speculation (busy)"),
	/** Speculative drop due to unavailability */
	SPECUNAVAIL("Speculation (unavailable)"),
	/** Stack string table overflow */
	STKSTROVERFLOW("Stack string table overflow"),
	/** Error in ERROR probe */
	DBLERROR("error in ERROR probe"),
	/** Unrecognized value from native DTrace library */
	UNKNOWN("Unknown");

	private String s;

	private
	Kind(String displayString)
	{
	    s = displayString;
	}

	/**
	 * Overridden to get the default display value.  To
	 * internationalize the display value, use {@link Enum#name()}
	 * instead as an I18N lookup key.
	 */
	public String
	toString()
	{
	    return s;
	}
    }

    /**
     * Creates a {@code Drop} instance with the given CPU, drop kind,
     * drop counts, and default message.  Supports XML persistence.
     *
     * @param dropCPU cpu where drops occurred
     * @param dropKindName name of enumeration value indicating the kind
     * of buffer space where the drop occurred and possibly a reason
     * @param dropCount number of drops
     * @param totalDrops total number of drops since the source {@link
     * Consumer} started running
     * @param defaultDropMessage drop message provided by DTrace
     * @throws IllegalArgumentException if there is no {@code Drop.Kind}
     * value with the given name or if {@code dropCount} or {@code
     * totalDrops} is negative
     * @throws NullPointerException if the given {@code Drop.Kind} name
     * or default message is {@code null}
     */
    public
    Drop(int dropCPU, String dropKindName, long dropCount, long totalDrops,
	    String defaultDropMessage)
    {
    }

    /**
     * Gets the CPU where the drops occurred.
     *
     * @return non-negative CPU ID, or a negative number if the CPU is
     * unknown
     */
    public int
    getCPU()
    {
	return 0;
    }

    /**
     * Gets the kind of drop for all drops included in {@link
     * #getCount()}.
     *
     * @return non-null drop kind
     */
    public Kind
    getKind()
    {
	return Kind.UNKNOWN;
    }

    /**
     * Gets the number of drops reported by this {@code Drop} instance.
     *
     * @return non-negative drop count
     */
    public long
    getCount()
    {
	return -1L;
    }

    /**
     * Gets the total number of drops since the source {@link Consumer}
     * started running.
     *
     * @return non-negative drop total since tracing started
     */
    public long
    getTotal()
    {
	return -1L;
    }

    /**
     * Gets the message provided by DTrace.
     *
     * @return non-null message provided by DTrace
     */
    public String
    getDefaultMessage()
    {
	return "";
    }
}