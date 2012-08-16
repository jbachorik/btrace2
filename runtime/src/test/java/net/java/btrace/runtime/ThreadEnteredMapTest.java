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

package net.java.btrace.runtime;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ThreadEnteredMapTest {

    public ThreadEnteredMapTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private ThreadEnteredMap map;

    @Before
    public void setUp() {
        map = new ThreadEnteredMap("null");
    }

    @After
    public void tearDown() {
        map = null;
    }

    @Test
    public void testGetEmpty() {
        System.out.println("getEmpty");
        Object expResult = null;
        Object result = map.get();
        assertEquals(expResult, result);
    }

    @Test
    public void testEnteredCurThrd() {
        System.out.println("enteredCurThrd");
        Object myval = new Object();
        assertTrue(map.enter(myval));
        assertEquals(myval, map.get());
    }

    @Test
    public void testExitedCurThrd() {
        System.out.println("exitedCurThrd");
        Object myval = new Object();
        map.enter(myval);
        map.exit();
        assertNull(map.get());
    }

    @Test
    public void testEnteredManyThrds() throws InterruptedException {
        System.out.println("enteredManyThrds");

        final CountDownLatch latch = new CountDownLatch(4096);
        final AtomicBoolean rslt = new AtomicBoolean(true);

        final Object lock = new Object();
        Thread[] thrds = new Thread[4096];
        for(int i=0;i<4096;i++) {
            thrds[i] = new Thread(new Runnable() {
                Object myval = new Object();
                public void run() {
                    synchronized(lock) {
                        boolean outcome = map.enter(myval);
                        outcome = outcome && myval.equals(map.get());
                        rslt.compareAndSet(true, outcome);
                        latch.countDown();
                    }
                }
            }, "Thrd#" + i);
        }
        for(int i=4095;i>=0;i--) {
            thrds[i].start();
        }
        latch.await();
        assertTrue(rslt.get());
    }
}