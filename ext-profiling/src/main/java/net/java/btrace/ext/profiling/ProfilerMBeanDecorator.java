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
package net.java.btrace.ext.profiling;

import net.java.btrace.api.core.BTraceMBean;
import net.java.btrace.spi.core.MBeanDecoratorImpl;
import java.lang.reflect.Type;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * Profiler specific {@linkplain MBeanDecorator} instance
 * @author Jaroslav Bachorik
 */
@MBeanDecoratorImpl.Registration
public class ProfilerMBeanDecorator extends MBeanDecoratorImpl {
    @Override
    public OpenType toOpenType(Type type, BTraceMBean mbean) {
        try {
            if (type instanceof Class) {
                Class c = (Class) type;
                if (Profiler.class.isAssignableFrom(c)) {
                    CompositeType record = new CompositeType("Record",
                            "Profiler record",
                            new String[]{"block",
                                "invocations",
                                "selfTime.total",
                                "selfTime.percent",
                                "selfTime.avg",
                                "selfTime.max",
                                "selfTime.min",
                                "wallTime.total",
                                "wallTime.percent",
                                "wallTime.avg",
                                "wallTime.max",
                                "wallTime.min"},
                            new String[]{"block",
                                "invocations",
                                "selfTime.total",
                                "selfTime.percent",
                                "selfTime.avg",
                                "selfTime.max",
                                "selfTime.min",
                                "wallTime.total",
                                "wallTime.percent",
                                "wallTime.avg",
                                "wallTime.max",
                                "wallTime.min"},
                            new OpenType[]{
                                SimpleType.STRING,
                                SimpleType.LONG,
                                SimpleType.LONG,
                                SimpleType.DOUBLE,
                                SimpleType.LONG,
                                SimpleType.LONG,
                                SimpleType.LONG,
                                SimpleType.LONG,
                                SimpleType.DOUBLE,
                                SimpleType.LONG,
                                SimpleType.LONG,
                                SimpleType.LONG});
                    CompositeType recordEntry = new CompositeType("Record Entry",
                            "Record map entry",
                            new String[]{"key", "value"},
                            new String[]{"key", "value"},
                            new OpenType[]{
                                SimpleType.STRING,
                                record});
                    CompositeType snapshot = new CompositeType("Snapshot",
                            "Profiler snapshot",
                            new String[]{"startTime", "lastRefresh", "interval", "data"},
                            new String[]{"startTime", "lastRefresh", "interval", "data"},
                            new OpenType[]{
                                SimpleType.LONG,
                                SimpleType.LONG,
                                SimpleType.LONG,
                                new ArrayType(1, recordEntry)});
                    return snapshot;
                }
            }
        } catch (OpenDataException ode) {
            ode.printStackTrace();
        }
        return null;
    }

    @Override
    public Object toOpenTypeValue(OpenType type, Object value, BTraceMBean mbean) {
        if (type instanceof CompositeType) {
            if (value instanceof Profiler && value instanceof Profiler.MBeanValueProvider) {
                CompositeType ct = (CompositeType) type;
                Profiler.MBeanValueProvider p = (Profiler.MBeanValueProvider) value;

                Profiler.Snapshot snapshot = p.getMBeanValue();
                if (snapshot == null) {
                    try {
                        return new CompositeDataSupport(ct,
                                new String[]{"startTime", "lastRefresh", "interval", "data"},
                                new Object[]{
                                    mbean.convertToOpenTypeValue(ct.getType("startTime"), ((Profiler) p).START_TIME),
                                    mbean.convertToOpenTypeValue(ct.getType("lastRefresh"), -1L),
                                    mbean.convertToOpenTypeValue(ct.getType("interval"), 0L),
                                    new CompositeData[0]
                                });
                    } catch (OpenDataException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                CompositeData[] total = new CompositeData[snapshot.total.length];

                long divider = snapshot.timeInterval * 1000000; // converting ms to ns divider
                int index = 0;
                for (Profiler.Record r : snapshot.total) {
                    try {
                        CompositeType at = (CompositeType) ((ArrayType) ct.getType("data")).getElementOpenType();
                        CompositeType rt = (CompositeType) at.getType("value");
                        CompositeData recordData = new CompositeDataSupport(rt,
                                new String[]{"block",
                                    "invocations",
                                    "selfTime.total",
                                    "selfTime.percent",
                                    "selfTime.avg",
                                    "selfTime.max",
                                    "selfTime.min",
                                    "wallTime.total",
                                    "wallTime.percent",
                                    "wallTime.avg",
                                    "wallTime.max",
                                    "wallTime.min"},
                                new Object[]{r.blockName,
                                    r.invocations,
                                    r.selfTime,
                                    (double) (snapshot.timeInterval > 0 ? ((double) r.selfTime / (double) divider) * 100 : 0),
                                    r.selfTime / r.invocations,
                                    r.selfTimeMax,
                                    r.selfTimeMin == Long.MAX_VALUE ? 0 : r.selfTimeMin,
                                    r.wallTime,
                                    (double) (snapshot.timeInterval > 0 ? ((double) r.wallTime / (double) divider) * 100 : 0),
                                    r.wallTime / r.invocations,
                                    r.wallTimeMax,
                                    r.wallTimeMin});
                        total[index] = new CompositeDataSupport(at,
                                new String[]{"key", "value"},
                                new Object[]{r.blockName, recordData});
                    } catch (OpenDataException ode) {
                        ode.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    index++;
                }

                CompositeData snapshotData = null;
                try {
                    snapshotData = new CompositeDataSupport(ct,
                            new String[]{"startTime", "lastRefresh", "interval", "data"},
                            new Object[]{
                                mbean.convertToOpenTypeValue(ct.getType("startTime"), ((Profiler) p).START_TIME),
                                mbean.convertToOpenTypeValue(ct.getType("lastRefresh"), snapshot.timeStamp),
                                mbean.convertToOpenTypeValue(ct.getType("interval"), snapshot.timeInterval),
                                total
                            });
                } catch (OpenDataException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return snapshotData;
            }
        }
        return null;
    }
}
