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
 * A value accumulated by an aggregating DTrace action such as {@code
 * count()} or {@code sum()}.  Each {@code AggregationValue} is
 * associated with a {@link Tuple} in an {@link AggregationRecord}.  In
 * other words it is a value in a key-value pair (each pair representing
 * an entry in a DTrace aggregation).
 * <p>
 * This value may be a single number or consist of multiple numbers,
 * such as a value distribution.  In the latter case, it still has a
 * single, composite value useful for display and/or comparison.
 *
 * @see AggregationRecord
 *
 * @author Tom Erickson
 */
public interface AggregationValue {
    /**
     * Gets the numeric value of this instance.
     *
     * @return non-null numeric value
     */
    public Number getValue();
}