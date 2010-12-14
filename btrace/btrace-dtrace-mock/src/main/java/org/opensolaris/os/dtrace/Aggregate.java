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
 * Copyright 2008 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 *
 * ident	"%Z%%M%	%I%	%E% SMI"
 */
package org.opensolaris.os.dtrace;

import java.util.*;
import java.beans.*;
import java.io.*;

/**
 * A consistent snapshot of all aggregations requested by a single
 * {@link Consumer}.
 * <p>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @see Consumer#getAggregate()
 *
 * @author Tom Erickson
 */
public final class Aggregate implements Serializable
{
    static final long serialVersionUID = 3180340417154076628L;

    /**
     * Creates an aggregate with the given snaptime and aggregations.
     * Supports XML persistence.
     *
     * @param snaptimeNanos nanosecond timestamp when this aggregate was
     * snapped
     * @param aggregations unordered collection of aggregations
     * belonging to this aggregate
     * @throws NullPointerException if the given collection of
     * aggregations is {@code null}
     * @throws IllegalArgumentException if the record ordinals of the
     * given aggregations are invalid
     */
    public
    Aggregate(long snaptimeNanos, Collection <Aggregation> aggregations)
    {
    }

    /**
     * Gets the nanosecond timestamp of this aggregate snapshot.
     *
     * @return nanosecond timestamp of this aggregate snapshot
     */
    public long
    getSnaptime()
    {
	return -1L;
    }

    /**
     * Gets an unordered list of all aggregations in this aggregate
     * snapshot.  The list is easily sortable using {@link
     * java.util.Collections#sort(List list, Comparator c)} provided any
     * user-defined ordering.  Modifying the returned list has no effect
     * on this aggregate.  Supports XML persistence.
     *
     * @return modifiable unordered list of all aggregations in this
     * aggregate snapshot; list is non-null and possibly empty
     */
    public List <Aggregation>
    getAggregations()
    {
	// Must return an instance of a public, mutable class in order
	// to support XML persistence.
	return Collections.EMPTY_LIST;
    }

    /**
     * Gets the aggregation with the given name if it exists in this
     * aggregate snapshot.
     *
     * @param name  the name of the desired aggregation, or empty string
     * to request the unnamed aggregation.  In D, the unnamed
     * aggregation is used anytime a name does not follow the
     * aggregation symbol '{@code @}', for example:
     * <pre>		{@code @ = count();}</pre> as opposed to
     * <pre>		{@code @counts = count()}</pre> resulting in an
     * {@code Aggregation} with the name "counts".
     *
     * @return {@code null} if no aggregation by the given name exists
     * in this aggregate
     * @see Aggregation#getName()
     */
    public Aggregation
    getAggregation(String name)
    {
	return null;
    }

    /**
     * Gets an unordered list of this aggregate's records. The list is
     * sortable using {@link java.util.Collections#sort(List list,
     * Comparator c)} with any user-defined ordering. Modifying the
     * returned list has no effect on this aggregate.
     *
     * @return a newly created list that copies this aggregate's records
     * by reference in no particular order
     */
    public List <AggregationRecord>
    getRecords()
    {
	return Collections.EMPTY_LIST;
    }

    /**
     * Gets an ordered list of this aggregate's records sequenced by
     * their {@link AggregationRecord#getOrdinal() ordinal} property.
     * Note that the unordered list returned by {@link #getRecords()}
     * can easily be sorted by any arbitrary criteria, for example by
     * key ascending:
     * <pre><code>
     * List <AggregationRecord> records = aggregate.getRecords();
     * Collections.sort(records, new Comparator &lt;AggregationRecord&gt; () {
     * 	public int compare(AggregationRecord r1, AggregationRecord r2) {
     * 		return r1.getTuple().compareTo(r2.getTuple());
     * 	}
     * });
     * </code></pre>
     * Use {@code getOrderedRecords()} instead of {@code getRecords()}
     * when you want to list records as they would be ordered by {@code
     * dtrace(1M)}.
     *
     * @return a newly created list of this aggregate's records
     * in the order used by the native DTrace library
     */
    public List <AggregationRecord>
    getOrderedRecords()
    {
	return Collections.EMPTY_LIST;
    }

    /**
     * Gets a read-only {@code Map} view of this aggregate.
     *
     * @return a read-only {@code Map} view of this aggregate keyed by
     * aggregation name
     */
    public Map <String, Aggregation>
    asMap()
    {
	return Collections.EMPTY_MAP;
    }
}