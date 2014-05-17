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
package net.java.btrace.api.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import net.java.btrace.api.core.ServiceLocator;
import net.java.btrace.spi.server.ServerImpl;

/**
 *
 * @author Jaroslav Bachorik
 */
final public class Server {
    public static final int BTRACE_DEFAULT_PORT = 2020;
    public static final String BTRACE_PORT_KEY = "btrace.port";

    public static final class Settings {
        final public boolean debugMode;
        final public boolean trackRetransforms;
        final public String scriptOutputFile;
        final public long fileRollMilliseconds;
        final public boolean unsafeMode;
        final public boolean dumpClasses;
        final public String dumpDir;
        final public boolean stdOut;
        final public String probeDescPath;
        final public String script;
        final public String scriptDir;
        final public String extPath;
        final public boolean noSocketServer;
        final public String bootClassPath;
        final public String systemClassPath;
        final public int port;

        private Settings(boolean debugMode, boolean trackRetransforms, String scriptOutputFile,
                         long fileRollMilliseconds, boolean unsafeMode, boolean dumpClasses,
                         String dumpDir, boolean stdOut, String probeDescPath, String script,
                         String scriptDir, String extPath, boolean noServer, String bootClassPath,
                         String systemClassPath, int port) {
            this.debugMode = debugMode;
            this.trackRetransforms = trackRetransforms;
            this.scriptOutputFile = scriptOutputFile;
            this.fileRollMilliseconds = fileRollMilliseconds;
            this.unsafeMode = unsafeMode;
            this.dumpClasses = dumpClasses;
            this.dumpDir = dumpDir;
            this.stdOut = stdOut;
            this.probeDescPath = probeDescPath;
            this.script = script;
            this.scriptDir = scriptDir;
            this.extPath = extPath;
            this.noSocketServer = noServer;
            this.bootClassPath = bootClassPath;
            this.systemClassPath = systemClassPath;
            this.port = port;
        }

        public static Settings from(Map<String, String> args) {
            String p = args.get("debug");
            boolean debugMode = p != null && !"false".equals(p.toLowerCase());
            p = args.get("trackRetransforms");
            boolean trackRetransforms = p != null && !"false".equals(p);
            String scriptOutputFile = args.get("scriptOutputFile");
            p = args.get("fileRollMilliseconds");
            long fileRollMilliseconds = -1;
            if (p != null && p.length() > 0) {
                try {
                    fileRollMilliseconds = Long.parseLong(p);
                } catch (NumberFormatException nfe) {
                    fileRollMilliseconds = -1;
                }
            }
            p = args.get("unsafe");
            boolean unsafeMode = "true".equals(p);
            p = args.get("dumpClasses");
            boolean dumpClasses = p != null && !"false".equals(p);
            String dumpDir = null;
            if (dumpClasses) {
                dumpDir = args.get("dumpDir");
                if (dumpDir == null) {
                    dumpDir = ".";
                }
            }

            p = args.get("stdout");
            boolean traceToStdOut = p != null && !"false".equals(p);

            String probeDescPath = args.get("probeDescPath");
            if (probeDescPath == null) {
                probeDescPath = ".";
            }
//        ProbeDescriptorLoader.init(probeDescPath);
            p = args.get("script");
            String script = p;

            p = args.get("scriptdir");
            String scriptDir = p;

            String extPath = args.get("extPath");


            p = args.get("noServer");
            boolean noServer = p != null && !"false".equals(p);


            String bootClassPath = args.get("bootClassPath");
            String systemClassPath = args.get("systemClassPath");

            p = args.get("port");
            int port = p != null ? Integer.valueOf(p) : BTRACE_DEFAULT_PORT;
            return new Settings(debugMode, trackRetransforms, scriptOutputFile,
                                fileRollMilliseconds, unsafeMode, dumpClasses,
                                dumpDir, traceToStdOut, probeDescPath, script,
                                scriptDir, extPath, noServer, bootClassPath,
                                systemClassPath, port);
        }

        @Override
        public String toString() {
            return "BTrace Server Settings{" + "debugMode=" + debugMode + ", trackRetransforms=" + trackRetransforms + ", scriptOutputFile=" + scriptOutputFile + ", fileRollMilliseconds=" + fileRollMilliseconds + ", unsafeMode=" + unsafeMode + ", dumpClasses=" + dumpClasses + ", dumpDir=" + dumpDir + ", stdOut=" + stdOut + ", probeDescPath=" + probeDescPath + ", script=" + script + ", scriptDir=" + scriptDir + ", extPath=" + extPath + '}';
        }
    }

    final private ServerImpl impl;

    private Server(ServerImpl impl) {
        this.impl = impl;
    }

    private static class Singleton {
        final private static Server INSTANCE = new Server(ServiceLocator.loadService(ServerImpl.class, ClassLoader.getSystemClassLoader()));
    }

    /**
     * Singleton getter for {@linkplain Server}
     * @return Returns a singleton instance of {@linkplain Server}
     */
    public static Server getDefault() {
        return Singleton.INSTANCE;
    }

    /**
     * Server state check
     * @return <b>TRUE</b> if the server is alive, <b>FALSE</b> otherwise
     * @throws InterruptedException
     */
    public boolean isRunning() throws InterruptedException {
        return impl.isRunning();
    }

    /**
     * Server settings getter
     * @return The used server settings
     */
    public Settings getSetting() {
        return impl.getSettings();
    }

    /**
     * Starts a {@linkplain Server} for a particular application identified
     * by {@linkplain Instrumentation} instance
     * @param instr The {@linkplain Instrumentation} instance obtained from the target application
     * @param settings BTrace server settings (a {@linkplain Settings} instance
     * @throws IOException
     */
    public void start(Instrumentation instr, Settings settings) throws IOException {
        impl.start(instr, settings);
    }

    /**
     * Called upon the target application shutdown. Performs all the necessary cleanup.
     */
    public void shutdown() {
        impl.shutdown();
    }

    /**
     * Loads the BTrace script in the form of a pre-compiled and pre-verified
     * bytecode. Links the script and starts a new {@linkplain Session}
     * @param traceCode The BTrace script in the form of a pre-compiled and
     *                  pre-verified bytecode
     * @param writer The writer used to redirect the script output to
     */
    public void loadBTraceScript(final byte[] traceCode, final PrintWriter writer) {
        impl.loadBTraceScript(traceCode, writer);
    }

    public List<Session> getSessions() {
        return impl.getSessions();
    }
}

