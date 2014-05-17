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
package net.java.btrace.server;

import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.extensions.ExtensionsRepositoryFactory;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.instr.ClassFilter;
import net.java.btrace.instr.InstrumentUtils;
import net.java.btrace.instr.Instrumentor;
import net.java.btrace.org.objectweb.asm.ClassReader;
import net.java.btrace.org.objectweb.asm.ClassWriter;
import net.java.btrace.api.wireio.Channel;
import net.java.btrace.wireio.commands.InstrumentCommand;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import net.java.btrace.api.core.ServiceProvider;
import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.server.Server;
import net.java.btrace.api.server.Server.Settings;
import net.java.btrace.api.server.Session;
import net.java.btrace.api.wireio.ResponseHandler;
import net.java.btrace.instr.ExtensionRuntimeProcessor;
import net.java.btrace.server.wireio.LocalChannel;
import net.java.btrace.server.wireio.ServerChannel;
import net.java.btrace.spi.server.ServerImpl;
import net.java.btrace.util.BTraceThreadFactory;

/**
 *
 * @author Jaroslav Bachorik
 */
@ServiceProvider(service=ServerImpl.class)
final public class BTraceServer implements ServerImpl {
    final private static ClassFileTransformer extensionTransformer = new ClassFileTransformer() {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            byte[] bytecode = null;
            if (classBeingRedefined != null) {
                // class already defined; retransforming
                if (classBeingRedefined.getAnnotation(BTraceExtension.class) != null) {
                    bytecode = injectExtensionContext(classfileBuffer);
                }
            } else {
                // class not yet defined
                bytecode = injectExtensionContext(classfileBuffer);
            }
            if (bytecode != null) BTraceLogger.dumpClass(className, bytecode);
            return bytecode;
        }
    };

    // sensitive classes preload
    static {
        ClassFilter.class.getClass();
        InstrumentUtils.class.getClass();
        Instrumentor.class.getClass();
        ClassReader.class.getClass();
        ClassWriter.class.getClass();
    }

    final private static ExecutorService localClientProcessor = Executors.newCachedThreadPool(new BTraceThreadFactory("BTrace Local Client"));

    private Queue<ResponseHandler<Boolean>> stateReqQueue = new ConcurrentLinkedQueue<ResponseHandler<Boolean>>();

    private volatile boolean running = false;

    private Instrumentation instr;
    private ExtensionsRepository repository;
    private Server.Settings currentSettings;

    // @GuardedBy sessions
    final private Set<SessionImpl> sessions = new CopyOnWriteArraySet<SessionImpl>();

    private static byte[] injectExtensionContext(byte[] target) {
        try {
            ClassWriter writer = InstrumentUtils.newClassWriter(target);
            ClassReader reader = new ClassReader(target);

            ExtensionRuntimeProcessor injector = new ExtensionRuntimeProcessor(writer);
            InstrumentUtils.accept(reader, injector);
            return injector.isApplied() ? writer.toByteArray() : null;
        } catch (Throwable th) {
            BTraceLogger.debugPrint(th);
            return null;
        }
    }

    @Override
    public boolean isRunning() throws InterruptedException {
        if (!running) return false;

        ResponseHandler<Boolean> r = new ResponseHandler<Boolean>();
        stateReqQueue.add(r);
        return r.get();
    }

    @Override
    public Settings getSettings() {
        return currentSettings;
    }

    /**
     * Starts a {@linkplain Server} for a particular application identified
     * by {@linkplain Instrumentation} instance
     * @param instr The {@linkplain Instrumentation} instance obtained from the target application
     * @param settings BTrace server settings (a {@linkplain Settings} instance
     * @throws IOException
     */
    @Override
    public void start(Instrumentation instr, Server.Settings settings) throws IOException {
        // need to capture the class loads of extensions
        instr.addTransformer(extensionTransformer, true);

        this.instr = instr;
        this.repository = ExtensionsRepositoryFactory.composite(
                ExtensionsRepository.Location.SERVER,
                ExtensionsRepositoryFactory.builtin(ExtensionsRepository.Location.SERVER),
                ExtensionsRepositoryFactory.fixed(ExtensionsRepository.Location.SERVER, settings.extPath)
        );
        currentSettings = settings;

        ProbeDescriptorLoader.init(settings.probeDescPath);
        setupBootClassPath(settings);
        setupSystemClassPath(settings);

        startProvidedScripts(settings);

        if (!settings.noSocketServer) {
            int runningServerPort = Integer.valueOf(System.getProperty(Server.BTRACE_PORT_KEY, "-1"));
            if (runningServerPort != -1 && runningServerPort != settings.port) {
                BTraceLogger.debugPrint("Can not start BTrace socket server on port " + settings.port + ". There is already a server running on port " + runningServerPort);
            } else {
                if (runningServerPort == -1) {
                    startSocketServer(settings.port);
                }
                // else just reuse the already running socket server
            }
        }
    }

    /**
     * Called upon the target application shutdown. Performs all the necessary cleanup.
     */
    @Override
    public void shutdown() {
        for(Session s : sessions) {
            s.shutdown(0);
        }
    }

    /**
     * Loads the BTrace script in the form of a pre-compiled and pre-verified
     * bytecode. Links the script and starts a new {@linkplain Session}
     * @param traceCode The BTrace script in the form of a pre-compiled and
     *                  pre-verified bytecode
     * @param writer The writer used to redirect the script output to
     */
    @Override
    public void loadBTraceScript(final byte[] traceCode, final PrintWriter writer) {
        try {
            if (traceCode == null || traceCode.length == 0) {
                BTraceLogger.debugPrint("refusing empty script class data");
                return;
            }

            final BlockingQueue<AbstractCommand> q1 = new ArrayBlockingQueue<AbstractCommand>(500);
            final BlockingQueue<AbstractCommand> q2 = new ArrayBlockingQueue<AbstractCommand>(500);

            final ExtensionsRepository extRepo = ExtensionsRepositoryFactory.composite(
                ExtensionsRepository.Location.BOTH,
                ExtensionsRepositoryFactory.builtin(ExtensionsRepository.Location.BOTH),
                ExtensionsRepositoryFactory.fixed(ExtensionsRepository.Location.BOTH, currentSettings.extPath)
            );

            Channel serverChannel = new LocalChannel.Server(q1, q2, extRepo);

            final CountDownLatch latch = new CountDownLatch(1);
            final SessionImpl newSession = addServerSession(serverChannel, latch);

            localClientProcessor.submit(new Runnable() {
                @Override
                public void run() {
                    final AtomicBoolean interrupted = new AtomicBoolean(false);
                    // FIXME
                    Lookup ctx = new Lookup();
                    ctx.add(writer);
                    Channel channel = new LocalChannel.Client(q2, q1, extRepo);
                    try {
                        ctx.add(channel);
                        channel.sendCommand(InstrumentCommand.class, new AbstractCommand.Initializer<InstrumentCommand>() {

                            @Override
                            public void init(InstrumentCommand cmd) {
                                cmd.setCode(traceCode);
                            }
                        });

                        while (!interrupted.get()) {
                            AbstractCommand cmd = channel.readCommand();
                            cmd.execute(ctx);
                        }
                        writer.flush();
                    } catch (Exception e) {
                        BTraceLogger.debugPrint(e);
                    } finally {
                        ctx.remove(channel);
                    }
                }
            });
            latch.await();
        } catch (IOException e) {
            BTraceLogger.debugPrint(e);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
            Thread.currentThread().interrupt();
        } catch (RuntimeException re) {
            BTraceLogger.debugPrint(re);
        }
    }

    @Override
    public List<Session> getSessions() {
        return new ArrayList<Session>(sessions);
    }

    private void loadBTraceScript(final byte[] traceCode, boolean traceToStdOut, String scriptOutputFile, long fileRollMilliseconds) {
        final PrintWriter traceWriter;
        if (traceToStdOut) {
            traceWriter = new PrintWriter(System.out);
        } else {
            if (fileRollMilliseconds != -1) {
                traceWriter = new PrintWriter(new BufferedWriter(TraceOutputWriter.rollingFileWriter(new File(scriptOutputFile), 100, fileRollMilliseconds, TimeUnit.MILLISECONDS)));
            } else {
                traceWriter = new PrintWriter(new BufferedWriter(TraceOutputWriter.fileWriter(new File(scriptOutputFile))));
            }
        }

        loadBTraceScript(traceCode, traceWriter);
    }

    private void loadBTraceScript(String filename, boolean traceToStdOut, String scriptOutputFile, long fileRollMilliseconds) {
        try {
            if (!filename.endsWith(".class")) {
                BTraceLogger.debugPrint("refusing " + filename + ". script should be a pre-compiled .class file");
                return;
            }

            final File traceScript = new File(filename);
            if (!traceScript.exists()) {
                BTraceLogger.debugPrint("script " + traceScript + " does not exist!");
                return;
            }

            String currentBtraceScriptOutput = scriptOutputFile;
            if (!traceToStdOut) {
                String agentName = System.getProperty("btrace.agent", null);

                if (currentBtraceScriptOutput == null || currentBtraceScriptOutput.length() == 0) {
                    currentBtraceScriptOutput = filename + (agentName != null ? "." + agentName : "") + ".btrace";
                    BTraceLogger.debugPrint("scriptOutputFile not specified. defaulting to " + currentBtraceScriptOutput);
                }
            }

            loadBTraceScript(readAll(filename), traceToStdOut, currentBtraceScriptOutput, fileRollMilliseconds);
        } catch (IOException e) {
            BTraceLogger.debugPrint(e);
        }
    }

    private void startSocketServer(final int port) throws IOException {
        final ServerSocket ss = new ServerSocket(port);
        ss.setSoTimeout(1000);

        final Thread shutdownThread = new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }
        });

        Runtime.getRuntime().addShutdownHook(shutdownThread);

        new Thread(new Runnable() {

            @Override
            public void run() {
                running = true;
                boolean wasTimeout = false;
                System.setProperty(Server.BTRACE_PORT_KEY, String.valueOf(port));
                while (running) {
                    try {
                        while (!stateReqQueue.isEmpty()) {
                            ResponseHandler<Boolean> r = stateReqQueue.poll();
                            if (r != null) {
                                r.setResponse(running);
                            }
                        }
                        if (!wasTimeout) {
                            BTraceLogger.debugPrint("wating for client");
                        }
                        final Socket s = ss.accept();
                        wasTimeout = false;
                        BTraceLogger.debugPrint("client accepted");
                        Channel ch = ServerChannel.open(s, getExtensionRepository());
                        addServerSession(ch);
                    } catch (SocketTimeoutException e) {
                        wasTimeout = true;
                        running = !sessions.isEmpty();
                    } catch (IOException e) {
                        running = false;
                    }
                }
                BTraceLogger.debugPrint("Leaving BTrace Socket Server");
                try {
                    System.getProperties().remove(Server.BTRACE_PORT_KEY);
                    ss.close();
                } catch (IOException e) {
                    BTraceLogger.debugPrint(e);
                }
                try {
                    instr.removeTransformer(extensionTransformer);
                    Runtime.getRuntime().removeShutdownHook(shutdownThread);
                } catch (IllegalStateException e) {
                    // trying to remove shutdown hook while the shutdown is in progress
                }
            }
        }, "BTrace Socket Server").start();
    }

    private void startProvidedScripts(Server.Settings settings) {
        if (settings.script != null) {
            StringTokenizer tokenizer = new StringTokenizer(settings.script, ",");

            BTraceLogger.debugPrint(((tokenizer.countTokens() == 1) ? "initial script is " : "initial scripts are ") + settings.script);
            while (tokenizer.hasMoreTokens()) {
                loadBTraceScript(tokenizer.nextToken(), settings.stdOut, settings.scriptOutputFile, settings.fileRollMilliseconds);
            }
        }

        if (settings.scriptDir != null) {
            File scriptdir = new File(settings.scriptDir);
            if (scriptdir.isDirectory()) {
                BTraceLogger.debugPrint("found scriptdir: " + scriptdir.getAbsolutePath());
                File[] files = scriptdir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        loadBTraceScript(file.getAbsolutePath(), settings.stdOut, settings.scriptOutputFile, settings.fileRollMilliseconds);
                    }
                }
            }
        }
    }

    Instrumentation getInstrumentation() {
        return instr;
    }

    ExtensionsRepository getExtensionRepository() {
        return repository;
    }

    private void setupBootClassPath(Server.Settings ss) {
        StringBuilder bpcpBuilder = new StringBuilder(ss.bootClassPath != null ? ss.bootClassPath : "");
        bpcpBuilder.append(File.pathSeparator).append(repository.getClassPath());

        String bootClassPath = bpcpBuilder.toString();

        if (bootClassPath != null) {
            BTraceLogger.debugPrint("Bootstrap ClassPath: " + bootClassPath);

            StringTokenizer tokenizer = new StringTokenizer(bootClassPath, File.pathSeparator);
            try {
                while (tokenizer.hasMoreTokens()) {
                    String path = tokenizer.nextToken();
                    instr.appendToBootstrapClassLoaderSearch(new JarFile(new File(path)));
                }
            } catch (IOException ex) {
                BTraceLogger.debugPrint("adding to boot classpath failed!");
                BTraceLogger.debugPrint(ex);
            }
        }
    }

    private void setupSystemClassPath(Server.Settings ss) {
        if (ss.systemClassPath != null) {
            StringTokenizer tokenizer = new StringTokenizer(ss.systemClassPath, File.pathSeparator);
            try {
                while (tokenizer.hasMoreTokens()) {
                    String path = tokenizer.nextToken();
                    instr.appendToSystemClassLoaderSearch(new JarFile(new File(path)));
                }
            } catch (IOException ex) {
                BTraceLogger.debugPrint("adding to system classpath failed!");
                BTraceLogger.debugPrint(ex);
            }
        }
    }

    private static byte[] readAll(String fileName) throws IOException {
        File file = new File(fileName);
        if (!(file.exists() && file.isFile())) {
            return null;
        }
        int size = (int) file.length();
        FileInputStream fis = new FileInputStream(file);
        try {
            byte[] buf = new byte[size];
            fis.read(buf);
            return buf;
        } finally {
            fis.close();
        }
    }

    private SessionImpl addServerSession(Channel ch) throws IOException {
        return addServerSession(ch, null);
    }

    private SessionImpl addServerSession(Channel ch, final CountDownLatch initLatch) throws IOException {
        SessionImpl session = new SessionImpl(ch, getExtensionRepository(), getInstrumentation());
        sessions.add(session);
        session.addObserver(new Observer() {
            @Override
            public void update(Observable oSession, Object oldState) {
                SessionImpl.State s = ((SessionImpl)oSession).getState();
                switch (s) {
                    case DISCONNECTING: {
                        BTraceLogger.debugPrint("Shutting down session");
                        break;
                    }
                    case DISCONNECTED: {
                        sessions.remove((SessionImpl)oSession);
                        ((SessionImpl)oSession).deleteObserver(this);
                        break;
                    }
                    case CONNECTED: {
                        if (initLatch != null) {
                            initLatch.countDown();
                        }
                        break;
                    }
                    default: {
                        BTraceLogger.debugPrint("Unknown session state - " + s);
                    }
                }
            }
        });
        session.start();
        return session;
    }
}
