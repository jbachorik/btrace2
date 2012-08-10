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

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import net.java.btrace.api.core.PerfReader;
import net.java.btrace.api.extensions.runtime.Arguments;
import net.java.btrace.api.extensions.runtime.CommLine;
import net.java.btrace.api.extensions.runtime.Exceptions;
import net.java.btrace.api.extensions.runtime.JStat;
import net.java.btrace.api.extensions.runtime.MBeans;
import net.java.btrace.api.extensions.runtime.Objects;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.wireio.AbstractCommand.Initializer;
import net.java.btrace.api.wireio.Response;
import net.java.btrace.runtime.BTraceRuntime;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
public class BTraceRuntimeBridge implements Arguments, Exceptions, JStat, MBeans, Objects, CommLine, net.java.btrace.api.extensions.runtime.Runtime {
    private static BTraceRuntimeBridge instance = null;
    
    public static synchronized BTraceRuntimeBridge getInstance() {
        if (instance == null) {
            instance = new BTraceRuntimeBridge();
        }
        return instance;
    }
    
    private BTraceRuntimeBridge() {}

    @Override
    public int $length() {
        return BTraceRuntime.getCurrent().$length();
    }

    @Override
    public String $(int n) {
        return BTraceRuntime.getCurrent().$(n);
    }

    @Override
    public String[] $$() {
        return BTraceRuntime.getCurrent().$$();
    }

    @Override
    public RuntimeException translate(Exception exp) {
        return BTraceRuntime.translate(exp);
    }

    @Override
    public void throwException(Exception e) {
        BTraceRuntime.handleException(e);
    }

    @Override
    public PerfReader getPerfReader() {
        return BTraceRuntime.getPerfReader();
    }

    @Override
    public MemoryMXBean getMemoryMBean() {
        return BTraceRuntime.getMemoryMBean();
    }

    @Override
    public RuntimeMXBean getRuntimeMBean() {
        return BTraceRuntime.getRuntimeMBean();
    }

    @Override
    public HotSpotDiagnosticMXBean getHotSpotMBean() {
        return BTraceRuntime.getHotSpotMBean();
    }

    @Override
    public List<GarbageCollectorMXBean> getGarbageCollectionMBeans() {
        return BTraceRuntime.getGarbageCollectionMBeans();
    }

    @Override
    public List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        return BTraceRuntime.getMemoryPoolMXBeans();
    }

    @Override
    public String identityStr(Object obj) {
        return BTraceRuntime.identityStr(obj);
    }

    @Override
    public int identityHashCode(Object obj) {
        return BTraceRuntime.identityHashCode(obj);
    }

    @Override
    public long sizeof(Object obj) {
        return BTraceRuntime.sizeof(obj);
    }

    @Override
    public int hash(Object obj) {
        return BTraceRuntime.hash(obj);
    }

    @Override
    public boolean compare(Object obj1, Object obj2) {
        return BTraceRuntime.compare(obj1, obj2);
    }

    @Override
    public <T extends AbstractCommand> Response<T> send(Class<? extends T> cmdClass, Initializer<T> init) {
        return BTraceRuntime.send(cmdClass, init);
    }

    @Override
    public String className() {
        return BTraceRuntime.getClassName();
    }

    @Override
    public String getFilePath(String fileName) {
        return BTraceRuntime.resolveFileName(fileName);
    }

    @Override
    public void exit(int exitCode) {
        BTraceRuntime.exit(exitCode);
    }

    @Override
    public int dtraceProbe(String s1, String s2, int i1, int i2) {
        return -1;
    }
}
