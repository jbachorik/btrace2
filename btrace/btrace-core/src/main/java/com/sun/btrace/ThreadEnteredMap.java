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

package com.sun.btrace;

import java.io.IOException;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@sun.com>
 */
public class ThreadEnteredMap {
    final private static int SECTIONS = 13;
    final private static int BUCKETS = 27;
    final private static int DEFAULT_BUCKET_SIZE = 4;

    final private Object[][][] map = new Object[SECTIONS][BUCKETS][];
    final private int[][] mapPtr = new int[SECTIONS][BUCKETS];

    private Object nullValue;

    public ThreadEnteredMap(Object nullValue) {
        this.nullValue = nullValue;
    }

    public static void main(String[] args) throws IOException {
        ThreadEnteredMap instance = new ThreadEnteredMap("null");
        long cnt = 0;
        long start = System.nanoTime();
        for(int i=0;i<4000000;i++) {
            cnt += i;
        }
        long dur = System.nanoTime() - start;
        System.err.println("#" + cnt + " in " + dur + "ns");
        System.err.println(dur / 4000000);

        for(int i=0;i<400000;i++) {
            instance.enter("in");
            instance.exit();
        }

        final ThreadEnteredMap tem = new ThreadEnteredMap("null");
        cnt = 0;
        System.err.println("Ready?");
        System.in.read();
        Thread[] thrd = new Thread[200];
        for(int i=0;i<4;i++) {
            final int idx = i;
            thrd[i] = new Thread(new Runnable() {

                public void run() {
                    long cnt = 0;
                    long start = System.nanoTime();
                    for(int i=0;i<4000000;i++) {
                        tem.enter("in");

                        cnt += i;
                        tem.exit();
                    }
                    long dur = System.nanoTime() - start;
                    System.out.println("Thread #" + idx);
                    System.err.println("#" + cnt + " in " + dur + "ns");
                    System.err.println(dur / 4000000);
                }
            }, "Thread#" + i);
        }
        for(int i=0;i<4;i++) {
            thrd[i].start();
        }
        
    }

    public Object get() {
        Thread thrd = Thread.currentThread();
        long thrdId = thrd.getId();
        int sectionId = (int)(((thrdId << 1) - (thrdId << 8)) & (SECTIONS - 1));
        Object[][] section = map[sectionId];
        int[] sectionPtr = mapPtr[sectionId];
        int bucketId = (int)(int)(((thrdId << 1) - (thrdId << 8)) & (BUCKETS - 1));
        synchronized(section) {
            Object[] bucket = section[bucketId];
            if (bucket != null && bucket.length > 0) {
                int ptr = sectionPtr[bucketId];
                for(int i=0;i<ptr;i+=2) {
                    if (bucket[i] == thrd) {
                        return bucket[i+1] == nullValue ? null : bucket[i+1];
                    }
                }
            }
            return null;
        }
    }

    public boolean enter(Object rt) {
        Thread thrd = Thread.currentThread();
        long thrdId = thrd.getId();
        int sectionId = (int)(((thrdId << 1) - (thrdId << 8)) & (SECTIONS - 1));
        Object[][] section = map[sectionId];
        int[] sectionPtr = mapPtr[sectionId];
        int bucketId = (int)(int)(((thrdId << 1) - (thrdId << 8)) & (BUCKETS - 1));
        synchronized(section) {
            Object[] bucket = section[bucketId];
            int ptr = sectionPtr[bucketId];
            if (bucket != null && bucket.length > 0) {
                for(int i=0;i<ptr;i+=2) {
                    if (bucket[i] == thrd) {
                        if (bucket[i+1] == nullValue) {
                            bucket[i+1] = rt;
                            return true;
                        }
                        return false;
                    }
                }
            }
            if (bucket == null || bucket.length == 0) {
                bucket = new Object[DEFAULT_BUCKET_SIZE * 2];
                section[bucketId] = bucket;
            } else {
                if (ptr >= bucket.length) {
                    Object[] newBucket = new Object[bucket.length * 2];
                    System.arraycopy(bucket, 0, newBucket, 0, bucket.length);
                    bucket = newBucket;
                    section[bucketId] = bucket;
                }
            }
            bucket[ptr++] = thrd;
            bucket[ptr++] = rt;
            mapPtr[sectionId][bucketId] = ptr;
            return true;
        }
    }

    public void exit() {
        Thread thrd = Thread.currentThread();
        long thrdId = thrd.getId();
        int sectionId = (int)(((thrdId << 1) - (thrdId << 8)) & (SECTIONS - 1));
        Object[][] section = map[sectionId];
        int[] sectionPtr = mapPtr[sectionId];
        int bucketId = (int)(int)(((thrdId << 1) - (thrdId << 8)) & (BUCKETS - 1));
        synchronized(section) {
            Object[] bucket = section[bucketId];
            if (bucket != null && bucket.length > 0) {
                int ptr = sectionPtr[bucketId];
                for(int i=0;i<ptr;i+=2) {
                    if (bucket[i] == thrd) {
                        bucket[i+1] = nullValue;
                    }
                }
            }
        }
    }
}
