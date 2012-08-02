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

package com.sun.btrace.samples;

import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.DTraceRef;
import com.sun.btrace.annotations.Location;
import static com.sun.btrace.ext.Printer.*;
import com.sun.btrace.ext.Strings;
import com.sun.btrace.ext.Reflective;
import com.sun.btrace.ext.Threads;


import com.sun.btrace.dtrace.D;

/*
 * This sample demonstrates associating a D-script
 * with a BTrace program using @DTraceRef annotation.
 * BTrace client looks for absolute or relative path for
 * the D-script and submits it to kernel *before* submitting
 * BTrace program to BTrace agent.
 */
@DTraceRef("classload.d")
@BTrace public class DTraceRefDemo {
   static {
       D.submitFile("classload.d");
   }
   
   @OnMethod(
     clazz="java.lang.ClassLoader",
     method="defineClass"
   )
   public static void defineClass() {
       println("user defined loader load start");
   }

   @OnMethod(
     clazz="java.lang.ClassLoader", 
     method="defineClass",
     location=@Location(Kind.RETURN)
   )   
   public static void defineclass(Class cl) {
       println(Strings.strcat("loaded ", Reflective.name(cl)));
       Threads.jstack();
       println("==========================");
   }
}
