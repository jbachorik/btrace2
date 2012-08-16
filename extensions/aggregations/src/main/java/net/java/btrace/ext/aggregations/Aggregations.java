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
package net.java.btrace.ext.aggregations;

import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.wireio.commands.GridDataCommand;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import net.java.btrace.api.extensions.runtime.CommLine;

/*
 * Wraps the aggregations related BTrace utility methods
 * @since 1.3
 * @author Christian Glencross
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class Aggregations {

    @Resource
    private static CommLine l;

    /**
     * Creates a new aggregation based on the given aggregation function type.
     *
     * @param type the aggregating function to be performed on the data being
     * added to the aggregation.
     */
    public static Aggregation newAggregation(AggregationFunction type) {
        return new Aggregation(type);
    }

    /**
     * Creates a grouping aggregation key with the provided value. The value
     * must be a String or Number type.
     *
     * @param element1 the value of the aggregation key
     */
    public static AggregationKey newAggregationKey(Object element1) {
        return new AggregationKey(new Object[]{element1});
    }

    /**
     * Creates a composite grouping aggregation key with the provided values.
     * The values must be String or Number types.
     *
     * @param element1 the first element of the composite aggregation key
     * @param element2 the second element of the composite aggregation key
     */
    public static AggregationKey newAggregationKey(Object element1, Object element2) {
        return new AggregationKey(new Object[]{element1, element2});
    }

    /**
     * Creates a composite grouping aggregation key with the provided values.
     * The values must be String or Number types.
     *
     * @param element1 the first element of the composite aggregation key
     * @param element2 the second element of the composite aggregation key
     * @param element3 the third element of the composite aggregation key
     */
    public static AggregationKey newAggregationKey(Object element1, Object element2, Object element3) {
        return new AggregationKey(new Object[]{element1, element2, element3});
    }

    /**
     * Creates a composite grouping aggregation key with the provided values.
     * The values must be String or Number types.
     *
     * @param element1 the first element of the composite aggregation key
     * @param element2 the second element of the composite aggregation key
     * @param element3 the third element of the composite aggregation key
     * @param element4 the fourth element of the composite aggregation key
     */
    public static AggregationKey newAggregationKey(Object element1, Object element2, Object element3, Object element4) {
        return new AggregationKey(new Object[]{element1, element2, element3, element4});
    }

    /**
     * Adds a value to the aggregation with no grouping key. This method should
     * be used when the aggregation is to calculate only a single aggregated
     * value.
     *
     * @param aggregation the aggregation to which the value should be added
     */
    public static void addToAggregation(Aggregation aggregation, long value) {
        aggregation.add(value);
    }

    /**
     * Adds a value to the aggregation with a grouping key. This method should
     * be used when the aggregation should effectively perform a "group by" on
     * the key value. The aggregation will calculate a separate aggregated value
     * for each unique aggregation key.
     *
     * @param aggregation the aggregation to which the value should be added
     * @param key the grouping aggregation key
     */
    public static void addToAggregation(Aggregation aggregation, AggregationKey key, long value) {
        aggregation.add(key, value);
    }

    /**
     * Resets values within the aggregation to the default. This will affect all
     * values within the aggregation when multiple aggregation keys have been
     * used.
     *
     * @param aggregation the aggregation to be cleared
     */
    public static void clearAggregation(Aggregation aggregation) {
        aggregation.clear();
    }

    /**
     * Removes all aggregated values from the aggregation except for the largest
     * or smallest
     * <code>abs(count)</code> elements.
     *
     * <p>If
     * <code>count</code> is positive, the largest aggregated values in the
     * aggregation will be preserved. If
     * <code>count</code> is negative the smallest values will be preserved. If
     * <code>count</code> is zero then all elements will be removed.
     *
     * <p>Behavior is intended to be similar to the dtrace
     * <code>trunc()</code> function.
     *
     * @param aggregation the aggregation to be truncated
     * @param count the number of elements to preserve. If negative, the
     * smallest <code>abs(count)</code> elements are preserved.
     */
    public static void truncateAggregation(Aggregation aggregation, int count) {
        aggregation.truncate(count);
    }

    public static void printAggregation(final String name, final Aggregation aggregation) {
        printAggregation(name, aggregation, null);
    }

    /**
     * Prints aggregation using the provided format
     *
     * @param name The name of the aggregation to be used in the textual output
     * @param aggregation The aggregation to print
     * @param format The format to use. It mimics {@linkplain String#format(java.lang.String, java.lang.Object[])
     * } behaviour with the addition of the ability to address the key title as
     * a 0-indexed item
     * @see String#format(java.lang.String, java.lang.Object[])
     */
    public static void printAggregation(final String name, final Aggregation aggregation, final String format) {
        printAggregation(name, format, aggregation.getData());
    }

    public static void printAggregation(String name, String format, Collection<Aggregation> aggregationList) {
        Aggregation[] aggregationArray = new Aggregation[aggregationList.size()];
        int index = 0;
        for (Aggregation a : aggregationList) {
            aggregationArray[index] = a;
            index++;
        }
        printAggregation(name, format, aggregationArray);
    }

    /**
     * Precondition: Only values from the first Aggregation are printed. If the
     * subsequent aggregations have values for keys which the first aggregation
     * does not have, these rows are ignored.
     *
     * @param name
     * @param format
     * @param aggregationArray
     */
    static void printAggregation(String name, String format, Aggregation[] aggregationArray) {
        if (aggregationArray.length > 1 && aggregationArray[0].getKeyData().size() > 1) {
            int aggregationDataSize = aggregationArray[0].getKeyData().get(0).getElements().length + aggregationArray.length;

            List<Object[]> aggregationData = new ArrayList<Object[]>();

            //Iterate through all keys in the first Aggregation and build up an array of aggregationData
            for (AggregationKey aggKey : aggregationArray[0].getKeyData()) {
                int aggDataIndex = 0;
                Object[] currAggregationData = new Object[aggregationDataSize];

                //Add the key to the from of the current aggregation Data
                for (Object obj : aggKey.getElements()) {
                    currAggregationData[aggDataIndex] = obj;
                    aggDataIndex++;
                }

                for (Aggregation agg : aggregationArray) {
                    currAggregationData[aggDataIndex] = agg.getValueForKey(aggKey);
                    aggDataIndex++;
                }

                aggregationData.add(currAggregationData);
            }

            printAggregation(name, format, aggregationData);
        }
    }
    
    private static void printAggregation(final String name, final String format, final List<Object[]> data) {
        l.send(GridDataCommand.class, new AbstractCommand.Initializer<GridDataCommand>() {
            public void init(GridDataCommand cmd) {
                cmd.setName(name);
                cmd.setPayload(new GridDataCommand.GridData(format, data));
            }
        });
    }
}
