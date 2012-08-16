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

package resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jaroslav Bachorik
 */
public class OnMethodTest {
    private int field;

    public OnMethodTest() {}
    private OnMethodTest(String a) {}

    public void noargs() {};
    static public void noargs$static() {};
    public long args(String a, long b, String[] c, int[] d) {return 0L;}
    static public long args$static(String a, long b, String[] c, int[] d) {return 0L;}

    public static long callTopLevelStatic(String a, long b) {
        OnMethodTest instance  = new OnMethodTest();
        return callTargetStatic(a, b) + instance.callTarget(a, b);
    }
    
    public static long callTargetStatic(String a, long b) {
        return 3L;
    }

    public long callTopLevel(String a, long b) {
        return callTarget(a, b) + callTargetStatic(a, b);
    }

    private long callTarget(String a, long b) {
        return 4L;
    }

    public void exception() {
        try {
            throw new IOException("hello world");
        } catch (IOException e) {
            
        }
    }

    public void uncaught() {
        throw new RuntimeException("ho-hey");
    }

    public void array(int a) {
        int[] arr = new int[10];

        int b = arr[a];
        arr[a] = 15;
    }

    public void field() {
        this.field = this.field + 1;
    }

    public void newObject() {
        Map<String, String> m = new HashMap<String, String>();
    }

    public void newArray() {
        int[] a = new int[1];
        int[][] b = new int[1][1];
        String[] c = new String[1];
        String[][] d = new String[1][1];
    }

    public void casts() {
        Map<String, String> c = new HashMap<String, String>();
        HashMap<String, String> d = (HashMap<String, String>)c;

        if (c instanceof HashMap) {
            System.err.println("hey ho");
        }
    }

    public void sync() {
        synchronized(this) {
            System.err.println("ho hey");
        }
    }
}
