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
package net.java.btrace.agent;

import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.util.Messages;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import net.java.btrace.instr.OnMethod;
import net.java.btrace.instr.OnProbe;
import net.java.btrace.instr.ProbeDescriptor;

/**
 *
 * @author Jaroslav Bachorik
 */
public class Main {
    public static final int BTRACE_DEFAULT_PORT = 2020;
    
    private static volatile Instrumentation inst;
        // #BTRACE-42: Non-daemon thread prevents traced application from exiting
    private static final ThreadFactory daemonizedThreadFactory = new ThreadFactory() {

        ThreadFactory delegate = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread result = delegate.newThread(r);
            result.setDaemon(true);
            return result;
        }
    };
    private static final Map<String, String> argMap = new HashMap<String, String>();
    private static final ExecutorService serializedExecutor = Executors.newSingleThreadExecutor(daemonizedThreadFactory);
    private static final Map<Server, Thread> shutdownHooks = new HashMap<Server, Thread>();
    
    static {
        BTraceLogger.class.getClass();
    }
    
    private static void registerExitHook(final Server s) {
        final Thread[] shutdownHook = new Thread[1];
        shutdownHook[0] = new Thread(
            new Runnable() {

                public void run() {
                    s.shutdown();
                    shutdownHooks.remove(s);
                }
            }
        );
        Runtime.getRuntime().addShutdownHook(shutdownHook[0]);
        shutdownHooks.put(s, shutdownHook[0]);
    }
    
    public static void agentmain(String args, Instrumentation inst) {
        doMain(args, inst);
    }
    
    public static void premain(String args, Instrumentation inst) {
        doMain(args, inst);
    }
    
    private static void doMain(String args, Instrumentation inst) {
        try {
            Server s = Server.getDefault();
            if (!s.isRunning()) {
                BTraceLogger.debugPrint("parsing command line arguments");
                Server.Settings ss = parseArgs(args);
                BTraceLogger.debugPrint("parsed command line arguments");
                s.run(inst, ss);
            } else {
                BTraceLogger.debugPrint("server already running with settings: " + s.getSetting());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void usage() {
        System.out.println(Messages.get("btrace.agent.usage"));
        System.exit(0);
    }

    private static Server.Settings parseArgs(String args) {
        if (args == null) {
            args = "";
        }
        String[] pairs = args.split(",");
        argMap.clear();
        for (String s : pairs) {
            int i = s.indexOf('=');
            String key, value = "";
            if (i != -1) {
                key = s.substring(0, i).trim();
                if (i + 1 < s.length()) {
                    value = s.substring(i + 1).trim();
                }
            } else {
                key = s;
            }
            argMap.put(key, value);
        }

        String p = argMap.get("help");
        if (p != null) {
            usage();
        }
        
        Server.Settings s = Server.Settings.from(argMap);
        BTraceLogger.debugPrint(s.toString());
        return s;
    }

    // This is really a *private* interface to Glassfish monitoring.
    // For now, please avoid using this in any other scenario.
    // Keeping this only for compatibility sake; GF should switch to using net.java.btrace.agent.Server#loadBTraceScript() method
    public static void handleFlashLightClient(byte[] code, PrintWriter writer) {
        Server.getDefault().loadBTraceScript(code, writer);
    }
    
    /**
     * Maps a list of @OnProbe's to a list @OnMethod's using
     * probe descriptor XML files.
     */
    static List<OnMethod> mapOnProbes(List<OnProbe> onProbes) {
        List<OnMethod> res = new ArrayList<OnMethod>();
        for (OnProbe op : onProbes) {
            String ns = op.getNamespace();
            BTraceLogger.debugPrint("about to load probe descriptor for " + ns);
            
            // load probe descriptor for this namespace
            ProbeDescriptor probeDesc = ProbeDescriptorLoader.load(ns);
            if (probeDesc == null) {
                BTraceLogger.debugPrint("failed to find probe descriptor for " + ns);
                continue;
            }
            // find particular probe mappings using "local" name
            OnProbe foundProbe = probeDesc.findProbe(op.getName());
            if (foundProbe == null) {
                BTraceLogger.debugPrint("no probe mappings for " + op.getName());
                continue;
            }
            BTraceLogger.debugPrint("found probe mappings for " + op.getName());

            Collection<OnMethod> omColl = foundProbe.getOnMethods();
            for (OnMethod om : omColl) {
                // copy the info in a new OnMethod so that
                // we can set target method name and descriptor
                // Note that the probe descriptor cache is used
                // across BTrace sessions. So, we should not update
                // cached OnProbes (and their OnMethods).
                OnMethod omn = new OnMethod();
                omn.copyFrom(om);
                omn.setTargetName(op.getTargetName());
                omn.setTargetDescriptor(op.getTargetDescriptor());
                res.add(omn);
            }
        }
        return res;
    }
}
