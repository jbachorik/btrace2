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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import net.java.btrace.api.server.Server;

/**
 *
 * @author Jaroslav Bachorik
 */
public class Main {
    public static final int BTRACE_DEFAULT_PORT = 2020;

    public static void agentmain(String args, Instrumentation inst) {
        doMain(args, inst);
    }

    public static void premain(String args, Instrumentation inst) {
        doMain(args, inst);
    }

    private static void setupBootstrap(String args, Instrumentation inst) throws IOException {
        String blPath = null;

        String bootlibKey = "bootstrap=";
        int blStart = args.indexOf(bootlibKey);
        if (blStart > -1) {
            int blEnd = args.indexOf(",", blStart);
            if (blEnd > -1) {
                blPath = args.substring(blStart + bootlibKey.length(), blEnd).trim();
            }
        }
        if (blPath == null) {
            ClassLoader cl = Main.class.getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            URL blPathURL = cl.getResource(Main.class.getName().replace('.', '/') + ".class");
            blPath = blPathURL.toString().replace("jar:file:", "");
            blPath = blPath.substring(0, blPath.indexOf(".jar!") + 4).replace("btrace-agent", "btrace-boot"); // NOI18N
        }
        inst.appendToBootstrapClassLoaderSearch(new JarFile(blPath));
    }

    private static void doMain(String args, Instrumentation inst) {
        try {
            setupBootstrap(args, inst);
            Server s = Server.getDefault();
            if (!s.isRunning()) {
                Map<String, String> argMap = mapArgs(args);
                String p = argMap.get("help");
                if (p != null) {
                    usage();
                    return;
                }
                Server.Settings ss = Server.Settings.from(argMap);
                BTraceLogger.config(ss);

                BTraceLogger.debugPrint("parsed command line arguments");
                s.start(inst, ss);
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

    private static Map<String, String> mapArgs(String args) {
        if (args == null) {
            args = "";
        }
        String[] pairs = args.split(",");

        Map<String, String> argMap = new HashMap<String, String>();
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

        return argMap;
    }

    // This is really a *private* interface to Glassfish monitoring.
    // For now, please avoid using this in any other scenario.
    // Keeping this only for compatibility sake; GF should switch to using net.java.btrace.agent.Server#loadBTraceScript() method
    public static void handleFlashLightClient(byte[] code, PrintWriter writer) {
        Server.getDefault().loadBTraceScript(code, writer);
    }
}
