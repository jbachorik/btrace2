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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
            BTraceLogger.debugPrint("parsing command line arguments");
            Server.Settings ss = parseArgs(args);
            BTraceLogger.debugPrint("parsed command line arguments");
            Server s = Server.getDefault();
//            registerExitHook(s);
            s.run(inst, ss);
        } catch (IOException ex) {
            ex.printStackTrace();
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
}
