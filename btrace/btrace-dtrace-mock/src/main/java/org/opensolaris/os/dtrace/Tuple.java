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

import java.io.*;
import java.util.*;
import java.beans.*;
import java.util.*;

/**
 * Multi-element key to a value in an {@link Aggregation}.
 * <p>
 * Tuple equality is based on the length of each tuple and the equality
 * of each corresponding element.  The natural ordering of tuples is
 * based on a lenient comparison designed not to throw exceptions when
 * corresponding elements are not mutually comparable or the number of
 * tuple elements differs.
 * <p>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @author Tom Erickson
 */
public final class Tuple implements Serializable, Comparable <Tuple>,
       Iterable<ValueRecord>
{
    static final long serialVersionUID = 5192674716869462720L;

    /**
     * The empty tuple has zero elements and may be used to obtain the
     * singleton {@link AggregationRecord} of a non-keyed {@link
     * Aggregation}, such as the one derived from the D statement
     * <code>&#64;a = count()</code>.  (In D, an aggregation without
     * square brackets aggregates a single value.)
     */
    public static final Tuple EMPTY = new Tuple();

    /**
     * Creates a tuple with the given elements in the given order.
     *
     * @param tupleElements ordered series of tuple elements
     * @throws NullPointerException if the given array or any of its
     * elements is {@code null}
     */
    public
    Tuple(ValueRecord ... tupleElements)
    {
    }

    /**
     * Creates a tuple with the given element list in the given list
     * order.
     *
     * @param tupleElements ordered list of tuple elements
     * @throws NullPointerException if the given list or any of its
     * elements is {@code null}
     */
    public
    Tuple(List <ValueRecord> tupleElements)
    {
    }


    /**
     * Gets a modifiable list of this tuple's elements in the same order
     * as their corresponding variables in the original D program tuple.
     * Modifying the returned list has no effect on this tuple.
     * Supports XML persistence.
     *
     * @return a modifiable list of this tuple's elements in the same order
     * as their corresponding variables in the original D program tuple
     */
    public List <ValueRecord>
    getElements()
    {
	return Collections.EMPTY_LIST;
    }

    /**
     * Gets a read-only {@code List} view of this tuple.
     *
     * @return a read-only {@code List} view of this tuple
     */
    public List <ValueRecord>
    asList()
    {
	return Collections.EMPTY_LIST;
    }

    /**
     * Gets the number of elements in this tuple.
     *
     * @return non-negative element count
     */
    public int
    size()
    {
	return 0;
    }

    /**
     * Returns {@code true} if this tuple has no elements.
     *
     * @return {@code true} if this tuple has no elements, {@code false}
     * otherwise
     * @see Tuple#EMPTY
     */
    public boolean
    isEmpty()
    {
	return true;
    }

    /**
     * Gets the element at the given tuple index (starting at zero).
     *
     * @return non-null tuple element at the given zero-based index
     */
    public ValueRecord
    get(int index)
    {
	return null;
    }

    /**
     * Gets an iterator over the elements of this tuple.
     *
     * @return an iterator over the elements of this tuple
     */
    public Iterator<ValueRecord>
    iterator()
    {
	return null;
    }

    /**
     * Defines the natural ordering of tuples.  Uses a lenient algorithm
     * designed not to throw exceptions.  Sorts tuples by the natural
     * ordering of corresponding elements, starting with the first pair
     * of corresponding elements and comparing subsequent pairs only
     * when all previous pairs are equal (as a tie breaker).  If
     * corresponding elements are not mutually comparable, it compares
     * the string values of those elements.  If all corresponding
     * elements are equal, then the tuple with more elements sorts
     * higher than the tuple with fewer elements.
     *
     * @return a negative integer, zero, or a postive integer as this
     * tuple is less than, equal to, or greater than the given tuple
     * @see Tuple#compare(Tuple t1, Tuple t2, int pos)
     */
    public int
    compareTo(Tuple t)
    {
	return 0;
    }

    /**
     * Compares corresponding tuple elements at the given zero-based
     * index. Elements are ordered as defined in the native DTrace
     * library, which treats integer values as unsigned when sorting.
     *
     * @param t1 first tuple
     * @param t2 second tuple
     * @param pos nth tuple element, starting at zero
     * @return a negative integer, zero, or a postive integer as the
     * element in the first tuple is less than, equal to, or greater
     * than the element in the second tuple
     * @throws IndexOutOfBoundsException if the given tuple index {@code
     * pos} is out of range {@code (pos < 0 || pos >= size())} for
     * either of the given tuples
     */
    public static int
    compare(Tuple t1, Tuple t2, int pos)
    {
	return 0;
    }
}