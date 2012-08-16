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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/*
 * Wraps the atomicity related BTrace utility methods
 * @since 1.2
 */
@BTraceExtension
public class Atomic {

    /**
     * Creates a new AtomicInteger with the given initial value.
     *
     * @param initialValue the initial value
     */
    public static AtomicInteger newAtomicInteger(int initialValue) {
        return new AtomicInteger(initialValue);
    }

    /**
     * Gets the current value of the given AtomicInteger.
     *
     * @param ai AtomicInteger whose value is returned.
     * @return the current value
     */
    public static int get(AtomicInteger ai) {
        return ai.get();

    }

    /**
     * Sets to the given value to the given AtomicInteger.
     *
     * @param ai AtomicInteger whose value is set.
     * @param newValue the new value
     */
    public static void set(AtomicInteger ai, int newValue) {
        ai.set(newValue);
    }

    /**
     * Eventually sets to the given value to the given AtomicInteger.
     *
     * @param ai AtomicInteger whose value is lazily set.
     * @param newValue the new value
     */
    public static void lazySet(AtomicInteger ai, int newValue) {
        ai.lazySet(newValue);
    }

    /**
     * Atomically sets the value of given AtomitInteger to the given
     * updated value if the current value {@code ==} the expected value.
     *
     * @param ai AtomicInteger whose value is compared and set.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public static boolean compareAndSet(AtomicInteger ai, int expect, int update) {
        return ai.compareAndSet(expect, update);
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * <p>May <a href="package-summary.html#Spurious">fail spuriously</a>
     * and does not provide ordering guarantees, so is only rarely an
     * appropriate alternative to {@code compareAndSet}.
     *
     * @param ai AtomicInteger whose value is weakly compared and set.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public static boolean weakCompareAndSet(AtomicInteger ai, int expect, int update) {
        return ai.weakCompareAndSet(expect, update);
    }

    /**
     * Atomically increments by one the current value of given AtomicInteger.
     *
     * @param ai AtomicInteger that is incremented.
     * @return the previous value
     */
    public static int getAndIncrement(AtomicInteger ai) {
        return ai.getAndIncrement();
    }

    /**
     * Atomically decrements by one the current value of given AtomicInteger.
     *
     * @param ai AtomicInteger that is decremented.
     * @return the previous value
     */
    public static int getAndDecrement(AtomicInteger ai) {
        return ai.getAndDecrement();
    }

    /**
     * Atomically increments by one the current value of given AtomicInteger.
     *
     * @param ai AtomicInteger that is incremented.
     * @return the updated value
     */
    public static int incrementAndGet(AtomicInteger ai) {
        return ai.incrementAndGet();
    }

    /**
     * Atomically decrements by one the current value of given AtomicInteger.
     *
     * @param ai AtomicInteger whose value is decremented.
     * @return the updated value
     */
    public static int decrementAndGet(AtomicInteger ai) {
        return ai.decrementAndGet();
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param ai AtomicInteger whose value is added to.
     * @param delta the value to add
     * @return the previous value
     */
    public static int getAndAdd(AtomicInteger ai, int delta) {
        return ai.getAndAdd(delta);
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param ai AtomicInteger whose value is added to.
     * @param delta the value to add
     * @return the updated value
     */
    public static int addAndGet(AtomicInteger ai, int delta) {
        return ai.addAndGet(delta);
    }

    /**
     * Atomically sets to the given value and returns the old value.
     *
     * @param ai AtomicInteger whose value is set.
     * @param newValue the new value
     * @return the previous value
     */
    public static int getAndSet(AtomicInteger ai, int newValue) {
        return ai.getAndSet(newValue);
    }

    /**
     * Creates a new AtomicLong with the given initial value.
     *
     * @param initialValue the initial value
     */
    public static AtomicLong newAtomicLong(long initialValue) {
        return new AtomicLong(initialValue);
    }

    /**
     * Gets the current value the given AtomicLong.
     *
     * @param al AtomicLong whose value is returned.
     * @return the current value
     */
    public static long get(AtomicLong al) {
        return al.get();
    }

    /**
     * Sets to the given value.
     *
     * @param al AtomicLong whose value is set.
     * @param newValue the new value
     */
    public static void set(AtomicLong al, long newValue) {
        al.set(newValue);
    }

    /**
     * Eventually sets to the given value to the given AtomicLong.
     *
     * @param al AtomicLong whose value is set.
     * @param newValue the new value
     */
    public static void lazySet(AtomicLong al, long newValue) {
        al.lazySet(newValue);
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * @param al AtomicLong whose value is compared and set.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public static boolean compareAndSet(AtomicLong al, long expect, long update) {
        return al.compareAndSet( expect, update);
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * <p>May fail spuriously
     * and does not provide ordering guarantees, so is only rarely an
     * appropriate alternative to {@code compareAndSet}.
     *
     * @param al AtomicLong whose value is compared and set.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public static boolean weakCompareAndSet(AtomicLong al, long expect, long update) {
        return al.weakCompareAndSet(expect, update);
    }

    /**
     * Atomically increments by one the current value.
     *
     * @param al AtomicLong whose value is incremented.
     * @return the previous value
     */
    public static long getAndIncrement(AtomicLong al) {
        return al.getAndIncrement();
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @param al AtomicLong whose value is decremented.
     * @return the previous value
     */
    public static long getAndDecrement(AtomicLong al) {
        return al.getAndDecrement();
    }

    /**
     * Atomically increments by one the current value.
     *
     * @param al AtomicLong whose value is incremented.
     * @return the updated value
     */
    public static long incrementAndGet(AtomicLong al) {
        return al.incrementAndGet();
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @param al AtomicLong whose value is decremented.
     * @return the updated value
     */
    public static long decrementAndGet(AtomicLong al) {
        return al.decrementAndGet();
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param al AtomicLong whose value is added to.
     * @param delta the value to add
     * @return the previous value
     */
    public static long getAndAdd(AtomicLong al, long delta) {
        return al.getAndAdd(delta);
    }

    /**
     * Atomically adds the given value to the current value.
     *
     * @param al AtomicLong whose value is added to
     * @param delta the value to add
     * @return the updated value
     */
    public static long addAndGet(AtomicLong al, long delta) {
        return al.addAndGet(delta);
    }

    /**
     * Atomically sets to the given value and returns the old value.
     *
     * @param al AtomicLong that is set.
     * @param newValue the new value
     * @return the previous value
     */
    public static long getAndSet(AtomicLong al, long newValue) {
        return al.getAndSet(newValue);
    }
}
