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
package net.java.btrace.ext;

import net.java.btrace.api.extensions.BTraceExtension;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Wraps the time related BTrace utility methods
 * @since 1.3
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class Time {
    /**
     * Returns the current time in milliseconds.  Note that
     * while the unit of time of the return value is a millisecond,
     * the granularity of the value depends on the underlying
     * operating system and may be larger.  For example, many
     * operating systems measure time in units of tens of
     * milliseconds.
     *
     * @return  the difference, measured in milliseconds, between
     *          the current time and midnight, January 1, 1970 UTC.
     */
    public static long millis() {
        return java.lang.System.currentTimeMillis();
    }

    /**
     * Returns the current value of the most precise available system
     * timer, in nanoseconds.
     *
     * <p>This method can only be used to measure elapsed time and is
     * not related to any other notion of system or wall-clock time.
     * The value returned represents nanoseconds since some fixed but
     * arbitrary time (perhaps in the future, so values may be
     * negative).  This method provides nanosecond precision, but not
     * necessarily nanosecond accuracy. No guarantees are made about
     * how frequently values change. Differences in successive calls
     * that span greater than approximately 292 years (2<sup>63</sup>
     * nanoseconds) will not accurately compute elapsed time due to
     * numerical overflow.
     *
     * @return The current value of the system timer, in nanoseconds.
     */
    public static long nanos() {
        return java.lang.System.nanoTime();
    }

    /**
     * <p>Generates a string timestamp (current date&time)
     * @param format The format to be used - see {@linkplain SimpleDateFormat}
     * @return Returns a string representing current date&time
     * @since 1.1
     */
    public static String timestamp(String format) {
        return new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
    }

    /**
     * <p>Generates a string timestamp (current date&time) in the default system format
     * @return Returns a string representing current date&time
     * @since 1.1
     */
    public static String timestamp() {
        return new SimpleDateFormat().format(Calendar.getInstance().getTime());
    }
}
