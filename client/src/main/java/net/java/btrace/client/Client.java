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
package net.java.btrace.client;

import net.java.btrace.api.core.ValueFormatter;
import net.java.btrace.annotations.DTrace;
import net.java.btrace.annotations.DTraceRef;
import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.extensions.ExtensionsRepositoryFactory;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.api.wireio.Response;
import net.java.btrace.api.wireio.Channel;
import net.java.btrace.org.objectweb.asm.Type;
import net.java.btrace.wireio.commands.EventCommand;
import net.java.btrace.wireio.commands.ExitCommand;
import net.java.btrace.wireio.commands.InstrumentCommand;
import net.java.btrace.wireio.commands.MessageCommand;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;

/**
 * @author A.Sundararajan
 * @author Jaroslav Bachorik
 */
public class Client {

    private static boolean dtraceEnabled;
    private static Method submitFile;
    private static Method submitString;
    private static final String PROJECT_VERSION;
    private static final String DTRACE_DESC;
    private static final String DTRACE_REF_DESC;

    static {
        ResourceBundle rb = ResourceBundle.getBundle("net/java/btrace/client/Bundle");
        PROJECT_VERSION = rb.getString("btrace.version");

        try {
            /*
             * Check for DTrace Consumer class -- if we don't have that
             * either /usr/lib/share/java/dtrace.jar is not in CLASSPATH 
             * or we are not running on Solaris 11+.
             */
            Class dtraceConsumerClass = Class.forName("org.opensolaris.os.dtrace.Consumer");
            /*
             * Check for BTrace's DTrace support class -- if that is available
             * may be the user didn't build BTrace on Solaris 11. S/he built
             * it on Solaris 10 or below or some other OS.
             */
            Class dtraceClass = Class.forName("net.java.btrace.dtrace.DTrace");
            dtraceEnabled = true;
            submitFile = dtraceClass.getMethod("submit",
                    new Class[]{File.class, String[].class,
                        Channel.class
                    });
            submitString = dtraceClass.getMethod("submit",
                    new Class[]{String.class, String[].class,
                        Channel.class
                    });
        } catch (Exception exp) {
            dtraceEnabled = false;
        }
        DTRACE_DESC = Type.getDescriptor(DTrace.class);
        DTRACE_REF_DESC = Type.getDescriptor(DTraceRef.class);
    }

    private static enum State {

        OFFLINE, ATTACHING, ATTACHED, SUBMITTING, RUNNING, EXITING
    }

    public static interface ToolsJarLocator {

        String locateToolsJar();
    }
    
    final private int pid;
    final private VirtualMachine vm;
    private int port;
    private boolean unsafe;
    private boolean dumpClasses;
    private String dumpDir;
    private boolean trackRetransforms;
    private String bootCp;
    private String sysCp;
    private String probeDescPath;
    private ExtensionsRepository extRepository;
    private ToolsJarLocator tjLocator;
    private String agentPath;
    private String bootstrapPath;
    private Lookup commandCtx;
    private PrintWriter printWriter;
    final private ToolsJarLocator DEFAULT_TJ_LOCATOR;
    final private ExtensionsRepository DEFAULT_REPOSITORY;
    final private AtomicReference<State> state = new AtomicReference<State>(State.OFFLINE);
    final private ExecutorService commDispatcher = Executors.newSingleThreadExecutor(new ThreadFactory() {

        public Thread newThread(Runnable r) {
            return new Thread(r, "BTrace Client Comm Dispatcher");
        }
    });
    private Channel channel = null;
    private ValueFormatter vFormatter = null;

    private Client(final int pid) throws AttachNotSupportedException, IOException {
        this.pid = pid;
        BTraceLogger.debugPrint("Connecting client to " + pid);

        vm = VirtualMachine.attach(String.valueOf(pid));

        agentPath = findAgentPath();
        DEFAULT_REPOSITORY = ExtensionsRepositoryFactory.builtin(ExtensionsRepository.Location.CLIENT);
        extRepository = DEFAULT_REPOSITORY;

        DEFAULT_TJ_LOCATOR = new ToolsJarLocator() {

            public String locateToolsJar() {
                String javaHome, classPath;

                try {
                    HostIdentifier hostId = new HostIdentifier((String) null);
                    MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(hostId);

                    String uriString = "//" + String.valueOf(pid) + "?mode=r"; // NOI18N
                    VmIdentifier id = new VmIdentifier(uriString);
                    MonitoredVm vm = monitoredHost.getMonitoredVm(id, 0);
                    try {
                        javaHome = (String) vm.findByName("java.property.java.home").getValue();
                        classPath = (String) vm.findByName("java.property.java.class.path").getValue();

                        // try to get absolute path of tools.jar
                        // first check this application's classpath
                        String[] components = classPath.split(File.pathSeparator);
                        for (String c : components) {
                            if (c.endsWith("tools.jar")) {
                                return new File(c).getAbsolutePath();
                            } else if (c.endsWith("classes.jar")) { // MacOS specific
                                return new File(c).getAbsolutePath();
                            }
                        }
                        // we didn't find -- make a guess! If this app is running on a JDK rather 
                        // than a JRE there will be a tools.jar in $JDK_HOME/lib directory.
                        if (System.getProperty("os.name").startsWith("Mac")) {
                            String java_mac_home = javaHome.substring(0, javaHome.indexOf("/Home"));
                            return new File(java_mac_home + "/Classes/classes.jar").getAbsolutePath();
                        } else {
                            File tj = new File(javaHome + "/lib/tools.jar");
                            if (!tj.exists()) {
                                tj = new File(javaHome + "/../lib/tools.jar"); // running on JRE
                            }
                            return tj.getAbsolutePath();
                        }
                    } catch (Exception e) {
                    } finally {
                        monitoredHost.detach(vm);
                    }
                } catch (Exception e) {
                }
                return null;
            }
        };
        tjLocator = DEFAULT_TJ_LOCATOR;

        sysCp = tjLocator.locateToolsJar();

        vFormatter = new ValueFormatter(extRepository.getClassLoader());
        printWriter = new PrintWriter(System.out);
        commandCtx = new Lookup();
        commandCtx.add(this, vFormatter, printWriter);
    }

    public static Client forPID(int pid) {
        try {
            return new Client(pid);
        } catch (AttachNotSupportedException e) {
            BTraceLogger.debugPrint(e);
        } catch (IOException e) {
            BTraceLogger.debugPrint(e);
        }
        return null;
    }

    public String getAgentPath() {
        return agentPath;
    }

    public Client setAgentPath(String agentPath) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.agentPath = agentPath != null ? agentPath : findAgentPath();
        return this;
    }

    public Client setBootstrapPath(String bsPath) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.bootstrapPath = bsPath;
        return this;
    }
    
    public String getBootCp() {
        return bootCp;
    }

    public Client setBootCp(String bootCp) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.bootCp = bootCp;
        return this;
    }

    public boolean isDumpClasses() {
        return dumpClasses;
    }

    public Client setDumpClasses(boolean dumpClasses) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.dumpClasses = dumpClasses;
        return this;
    }

    public String getDumpDir() {
        return dumpDir;
    }

    public Client setDumpDir(String dumpDir) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }

        this.dumpDir = dumpDir;
        return this;
    }
    
    public Client setPrintWriter(PrintWriter pw) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        
        if (this.printWriter != null) {
            commandCtx.remove(this.printWriter);
        }
        this.printWriter = pw;
        if (pw != null) {
            commandCtx.add(this.printWriter);
        }
        return this;
    }

    public ToolsJarLocator getToolsJarLocator() {
        return tjLocator;
    }

    public Client setToolsJarLocator(ToolsJarLocator tjLocator) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.tjLocator = tjLocator != null ? tjLocator : DEFAULT_TJ_LOCATOR;
        return this;
    }

    public ExtensionsRepository getExtRepository() {
        return extRepository;
    }

    public Client setExtRepository(ExtensionsRepository extRepository) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        if (extRepository != this.extRepository) {
            this.extRepository = extRepository != null ? extRepository : DEFAULT_REPOSITORY;
            if (vFormatter != null) {
                commandCtx.remove(vFormatter);
            }
            vFormatter = new ValueFormatter(extRepository.getClassLoader());
            commandCtx.add(vFormatter);
        }
        return this;
    }

    public int getPort() {
        return port;
    }

    public Client setPort(int port) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.port = port;
        return this;
    }

    public String getProbeDescPath() {
        return probeDescPath;
    }

    public Client setProbeDescPath(String probeDescPath) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.probeDescPath = probeDescPath;
        return this;
    }

    public String getSysCp() {
        return sysCp;
    }

    public Client setSysCp(String sysCp) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.sysCp = sysCp != null ? sysCp : tjLocator.locateToolsJar();
        return this;
    }

    public boolean isTrackRetransforms() {
        return trackRetransforms;
    }

    public Client setTrackRetransforms(boolean trackRetransforms) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.trackRetransforms = trackRetransforms;
        return this;
    }

    public boolean isUnsafe() {
        return unsafe;
    }

    public Client setUnsafe(boolean unsafe) {
        if (state.get() != State.OFFLINE) {
            BTraceLogger.debugPrint("Can not change client parameters when already attached");
            return this;
        }
        this.unsafe = unsafe;
        return this;
    }

    public Channel getCommChannel() {
        return channel;
    }

    public Client attach() throws IOException {
        if (setState(State.OFFLINE, State.ATTACHING)) {
            BTraceLogger.debugPrint("checking port availability: " + port);

            int serverPort = Integer.parseInt(vm.getSystemProperties().getProperty("btrace.port", "-1"));
            if (serverPort != -1) {
                if (serverPort != port) {
                    throw new IOException("Can not attach to PID " + pid + " on port " + port + ". There is already a BTrace server active on port " + serverPort + "!");
                }
            } else {
                if (!isPortAvailable(port)) {
                    throw new IOException("Port " + port + " unavailable.");
                }
            }

            BTraceLogger.debugPrint("attached to " + pid);
            if (serverPort == -1) {
                BTraceLogger.debugPrint("loading " + agentPath);

                String agentArgs = "port=" + port;
                if (bootstrapPath != null) {
                    agentArgs += ",bootstrap=" + bootstrapPath;
                }
                if (BTraceLogger.isDebug()) {
                    agentArgs += ",debug=true";
                }
                if (unsafe) {
                    agentArgs += ",unsafe=true";
                }
                if (dumpClasses) {
                    agentArgs += ",dumpClasses=true";
                    agentArgs += ",dumpDir=" + dumpDir;
                }
                if (trackRetransforms) {
                    agentArgs += ",trackRetransforms=true";
                }
                if (bootCp != null) {
                    agentArgs += ",bootClassPath=" + bootCp;
                }
                if (sysCp == null) {
                    sysCp = tjLocator.locateToolsJar();
                }
                agentArgs += ",systemClassPath=" + sysCp;
                agentArgs += ",probeDescPath=" + probeDescPath;
                if (extRepository != DEFAULT_REPOSITORY) {
                    agentArgs += ",extPath=" + extRepository.getExtensionsPath();
                }
                BTraceLogger.debugPrint("agent args: " + agentArgs);

                try {
                    vm.loadAgent(agentPath, agentArgs);
                    setState(State.ATTACHED);
                    BTraceLogger.debugPrint("loaded " + agentPath);
                } catch (AgentLoadException e) {
                    setState(State.OFFLINE);
                    throw new IOException(e);
                } catch (AgentInitializationException e) {
                    setState(State.OFFLINE);
                    throw new IOException(e);
                }
            } else {
                setState(State.ATTACHED);
            }
        }
        return this;
    }

    public Client submit(String fileName, final byte[] code, String[] args) throws IOException {
        if (setState(State.ATTACHED, State.SUBMITTING)) {
            try {
                BTraceLogger.debugPrint("opening socket to " + port);
                Socket sock = null;
                boolean retry;
                do {
                    retry = false;
                    try {
                        sock = new Socket("localhost", port);
                    } catch (ConnectException e) {
                        try {
                            Thread.sleep(500);
                            retry = true;
                        } catch (InterruptedException iEx) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } while (retry);

                if (sock == null) {
                    throw new IOException("Can not open port " + port);
                }

                channel = ClientChannel.open(sock, extRepository);
                if (channel != null) {
                    commandCtx.add(channel);
                    Response<Boolean> f = channel.sendCommand(InstrumentCommand.class, new AbstractCommand.Initializer<InstrumentCommand>() {

                        public void init(InstrumentCommand cmd) {
                            cmd.setCode(code);
                            cmd.setArgs(new String[0]);
                        }
                    });
                    commDispatcher.submit(new Runnable() {

                        public void run() {
                            try {
                                BTraceLogger.debugPrint("entering into command loop");
                                while (state.get() != State.OFFLINE) {
                                    AbstractCommand cmd = channel.readCommand();
                                    cmd.execute(commandCtx);
                                }
                            } catch (IOException ex) {
                                setState(State.OFFLINE);
                                BTraceLogger.debugPrint("exitting due to exception " + ex.getMessage());
                            } catch (ClassNotFoundException ex) {
                                setState(State.OFFLINE);
                                BTraceLogger.debugPrint("exitting due to exception " + ex.getMessage());
                            } finally {
                                commandCtx.remove(channel);
                            }
                        }
                    });
                    Boolean rslt = f.get();
                    if (rslt != null && rslt) {
                        setState(State.SUBMITTING, State.RUNNING);
                    } else {
                        setState(State.OFFLINE);
                    }
                }
            } catch (UnknownHostException uhe) {
                setState(State.OFFLINE);
                throw new IOException(uhe);
            } catch (IOException e) {
                setState(State.OFFLINE);
                throw e;
            } catch (InterruptedException e) {
                setState(State.OFFLINE);
                Thread.currentThread().interrupt();
            } finally {
                setState(State.SUBMITTING, State.RUNNING);
            }
        }
        return this;
    }
    
    public void exit(int exitCode) {
        if (setState(State.RUNNING, State.EXITING)) {
            sendExit(exitCode);
            if (setState(State.EXITING, State.OFFLINE)) {
                commDispatcher.shutdown();
            }
        }
    }
    
    public void agentExit(int exitCode) {
        setState(State.OFFLINE);
        System.exit(exitCode);
    }
    
    public void sendEvent(final String eName) {
        if (state.get() == State.RUNNING) {
            try {
                channel.sendCommand(EventCommand.class, new AbstractCommand.Initializer<EventCommand>() {

                    public void init(EventCommand cmd) {
                        cmd.setEvent(eName);
                    }
                });
            } catch (IOException e) {
                BTraceLogger.debugPrint(e);
            }
        }
    }

    private void notifyStateChange() {
        synchronized(state) {
            state.notifyAll();
        }
    }
    
    private void sendExit(final int exitCode) {
        try {
            Response<Void> r = channel.sendCommand(ExitCommand.class, new AbstractCommand.Initializer<ExitCommand>() {

                public void init(ExitCommand cmd) {
                    cmd.setExitCode(exitCode);
                }
            });
            r.get();
        } catch (IOException e) {
            BTraceLogger.debugPrint(e);
        } catch (InterruptedException e) {
            BTraceLogger.debugPrint(e);
            Thread.currentThread().interrupt();
        }
    }

    private static String getLibBaseDir() {
        String tmp = Client.class.getClassLoader().getResource("net/java/btrace").toString();
        if (!tmp.startsWith("jar:file:")) return null;
        tmp = tmp.substring(0, tmp.indexOf("!"));
        tmp = tmp.substring("jar:".length(), tmp.lastIndexOf("/"));
        String baseDir = ".";
        try {
            baseDir = new File(new URI(tmp)).getAbsolutePath();
        } catch (URISyntaxException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return baseDir;
    }

    private static boolean isPortAvailable(int port) {
        Socket clSocket = null;
        try {
            clSocket = new Socket("127.0.0.1", port);
        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
            clSocket = null;
        }
        if (clSocket != null) {
            try {
                clSocket.close();
            } catch (IOException e) {
            }
            return false;
        }
        return true;
    }

    private static String findAgentPath() {
        String agentPath = "/agent/btrace-agent-" + PROJECT_VERSION + ".jar";
        String tmp = getLibBaseDir();
        return tmp + agentPath;
    }

    private void warn(Channel channel, final String msg) {
        try {
            channel.sendCommand(MessageCommand.class, new AbstractCommand.Initializer<MessageCommand>() {

                public void init(MessageCommand cmd) {
                    cmd.setMessage("WARNING: " + msg + "\n");
                }
            });
        } catch (IOException exp) {
            if (BTraceLogger.isDebug()) {
                exp.printStackTrace();
            }
        }
    }
    
    private void setState(State reqState) {
        state.set(reqState);
        notifyStateChange();
    }
    
    private boolean setState(State expState, State reqState) {
        if (state.compareAndSet(expState, reqState)) {
            notifyStateChange();
            return true;
        }
        return false;
    }
    
    private void waitForState(State reqState) {
        while (state.get() != reqState) {
            synchronized(state) {
                try {
                    state.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
}