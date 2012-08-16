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

package samples;

import net.java.btrace.annotations.ProbeMethodName;
import net.java.btrace.annotations.OnMethod;
import net.java.btrace.annotations.Kind;
import net.java.btrace.annotations.BTrace;
import net.java.btrace.annotations.ProbeClassName;
import net.java.btrace.annotations.Location;
import net.java.btrace.annotations.Duration;
import static net.java.btrace.ext.Printer.*;
import static net.java.btrace.ext.Strings.*;

/**
 * A simple BTrace program that prints a class name
 * and method name whenever a webservice is called and
 * also prints time taken by service method. WebService 
 * entry points are annotated javax.jws.WebService and 
 * javax.jws.WebMethod. We insert tracing actions into 
 * every class and method annotated by these annotations. 
 * This way we don't need to know actual webservice 
 * implementor class name.
 */
@BTrace public class WebServiceTracker {
   @OnMethod(
     clazz="@javax.jws.WebService", 
     method="@javax.jws.WebMethod"
   )   
   public static void onWebserviceEntry(@ProbeClassName String pcn, @ProbeMethodName String pmn) {
       print("entering webservice ");
       println(pcn + "." + pmn);
   }

   @OnMethod(
     clazz="@javax.jws.WebService", 
     method="@javax.jws.WebMethod",
     location=@Location(Kind.RETURN)
   )   
   public static void onWebserviceReturn(@ProbeClassName String pcn , @ProbeMethodName String pmn, @Duration long d) {
       print("leaving web service ");
       println(pcn + "." + pmn);
       println("Time taken (msec) " + str(d / 1000));
       println("==========================");
   }

}
