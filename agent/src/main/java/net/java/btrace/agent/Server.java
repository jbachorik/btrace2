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

import net.java.btrace.agent.wireio.ServerChannel;
import net.java.btrace.agent.wireio.LocalChannel;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.wireio.Response;
import net.java.btrace.api.wireio.ResponseHandler;
import net.java.btrace.instr.ExtensionRuntimeProcessor;

/**
 *
 * @author Jaroslav Bachorik
 */
final public class Server {
    private static final int BTRACE_DEFAULT_PORT = 2020;
    private static final String BTRACE_PORT_KEY = "btrace.port";
    
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
    
    private static class Singleton {
        private static final Server INSTANCE = new Server();
    }
    
    final private static ExecutorService localClientProcessor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "BTrace Local Client");
        }
    });
    
    private Queue<ResponseHandler<Boolean>> stateReqQueue = new ConcurrentLinkedQueue<ResponseHandler<Boolean>>();
    
    private volatile boolean running = false;
    
    private Instrumentation instr;
    private ExtensionsRepository repository;
    private Settings currentSettings;
    
    // @GuardedBy sessions
    final private Set<SessionImpl> sessions = new HashSet<SessionImpl>();
    
    private Server() {}
    
    /**
     * Singleton getter for {@linkplain Server}
     * @return Returns a singleton instance of {@linkplain Server}
     */
    public static Server getDefault() {
        return Singleton.INSTANCE;
    }
    
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
    
    public boolean isRunning() throws InterruptedException {
        if (!running) return false;
        
        ResponseHandler<Boolean> r = new ResponseHandler<Boolean>();
        stateReqQueue.add(r);
        return r.get();
    }
    
    public Settings getSetting() {
        return currentSettings;
    }
    
    /**
     * Starts a {@linkplain Server} for a particular application identified
     * by {@linkplain Instrumentation} instance
     * @param instr The {@linkplain Instrumentation} instance obtained from the target application
     * @param settings BTrace server settings (a {@linkplain Settings} instance
     * @throws IOException 
     */
    public void run(Instrumentation instr, Settings settings) throws IOException {      
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
            int runningServerPort = Integer.valueOf(System.getProperty(BTRACE_PORT_KEY, "-1"));
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
    public void shutdown() {
        Collection<SessionImpl> sSessions = new ArrayList<SessionImpl>();
        synchronized(sessions) {
            sSessions.addAll(sessions);
        }
        Iterator<SessionImpl> iter = sSessions.iterator();
        while (iter.hasNext()) {
            iter.next().shutdown(0);
        }
    }
    
    /**
     * Loads the BTrace script in the form of a pre-compiled and pre-verified
     * bytecode. Links the script and starts a new {@linkplain Session}
     * @param traceCode The BTrace script in the form of a pre-compiled and
     *                  pre-verified bytecode
     * @param writer The writer used to redirect the script output to
     */
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
                System.setProperty(BTRACE_PORT_KEY, String.valueOf(port));
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
                        synchronized(sessions) {
                            if (sessions.isEmpty()) {
                                running = false;
                            }
                        }
                    } catch (IOException e) {
                        running = false;
                    }
                }
                BTraceLogger.debugPrint("Leaving BTrace Socket Server");
                try {
                    System.getProperties().remove(BTRACE_PORT_KEY);
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

    private void startProvidedScripts(Settings settings) {
        if (settings.script != null) {
            StringTokenizer tokenizer = new StringTokenizer(settings.script, ",");

            BTraceLogger.debugPrint(((tokenizer.countTokens() == 1) ? "initial script is " : "initial scripts are ") + settings.script);
            while (tokenizer.hasMoreTokens()) {
                loadBTraceScript(tokenizer.nextToken(), settings.stdOut, settings.scriptOutputFile, settings.fileRollMilliseconds);
            }
        }
        
        if (settings.scriptDir != null) {
            File scriptdir = new File(settings.scriptDir);
            if (scriptdir != null && scriptdir.isDirectory()) {
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
    
    private void setupBootClassPath(Settings ss) {
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
    
    private void setupSystemClassPath(Settings ss) {
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
        SessionImpl session = new SessionImpl(ch, Server.this);
        synchronized(sessions) {
            sessions.add(session);
        }
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
        return session;
    }
}
