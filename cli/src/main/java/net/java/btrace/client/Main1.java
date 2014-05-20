/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.extensions.ExtensionsRepositoryFactory;
import net.java.btrace.jps.JpsProxy;
import net.java.btrace.jps.JpsVM;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
public class Main1 {
    public static final int BTRACE_DEFAULT_PORT = 2020;

    final private static String bootstrap;
    final private static String agentpath;
    final private static String defaultExtPath;

    static {
        bootstrap = getJarFor("net/java/btrace/api/core/BTraceRuntime.class");
        agentpath = getJarFor("net/java/btrace/agent/Main.class");

        defaultExtPath = getJarFor("net/java/btrace/ext/Printer.class") + File.pathSeparator +
                         getJarFor("net/java/btrace/ext/profiling/Profiler.class") + File.pathSeparator +
                         getJarFor("net/java/btrace/ext/aggregations/Aggregations.class") + File.pathSeparator +
                         getJarFor("net/java/btrace/ext/collections/Collections.class") + File.pathSeparator +
                         getJarFor("net/java/btrace/ext/export/Export.class") + File.separator +
                         getJarFor("net/java/btrace/ext/sys/Memory.class").replace("null" + File.pathSeparator, "") + File.pathSeparator;
    }


    public static void main(String ... args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.accepts("p").withRequiredArg().ofType(int.class).describedAs("Process PID to attach to");
        parser.accepts("P").withRequiredArg().ofType(int.class).describedAs("BTrace port").defaultsTo(BTRACE_DEFAULT_PORT);
        parser.accepts("s").withRequiredArg().ofType(String.class).describedAs("BTrace script to deploy");
        parser.accepts("x").withOptionalArg().ofType(String.class).describedAs("BTrace extensions location descriptor").defaultsTo("");
        parser.accepts("unsafe");
        parser.accepts("debug");
        parser.accepts("dumpClasses").withOptionalArg().ofType(String.class).describedAs("Debug dump of the modified classes").defaultsTo(System.getProperty("java.io.tmpdir"));

        run(parser.parse(args));
    }

    private static void run(OptionSet optionSet) throws IOException {
        final ConsoleReader cr = new ConsoleReader();

        Integer pid = (Integer)optionSet.valueOf("p");
        String trace = (String)optionSet.valueOf("s");
        String extensions = (String)optionSet.valueOf("x");

        while (pid == null || trace == null) {
            pid = readPid(pid, cr);
            trace = readTrace(trace, cr);
        }

        ExtensionsRepository extRepository = getRepository(defaultExtPath.isEmpty() ? extensions : defaultExtPath + File.pathSeparator + extensions);
        Compiler compiler = new Compiler(optionSet.has("unsafe"), extRepository);
        byte[] code = compiler.compile(trace, ".", null);
        if (code == null) {
            errorExit("BTrace compilation failed", 1);
        }

        final Client client = Client.forPID(Integer.valueOf(pid));
        if (agentpath != null) {
            client.setAgentPath(agentpath);
        }
        if (bootstrap != null) {
            client.setBootstrapPath(bootstrap);
        }

        final ClientWriter cw = new ClientWriter(System.out);

        registerExitHook(client);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        cr.println("*** BTrace CLI Ready ***\nPress Ctrl-o to access the menu.");
                        cr.flush();
                        int ch = cr.readCharacter();
                        if (ch == 15) { // CTRL-o
                            try {
                                cw.park();
                                cr.println("Please enter your option:");
                                cr.println("\t1. exit\n\t2. send an event\n\t3. send a named event\n\n\t0. continue");
                                cr.flush();
                                int option = cr.readCharacter('1', '2', '3', '0');
                                if (option == '1') {
                                    System.exit(0);
                                } else if (option == '2') {
                                    BTraceLogger.debugPrint("sending event command");
                                    sendEvent(client);
                                } else if (option == '3') {
                                    cr.setPrompt("Please enter the event name: ");
                                    String name = cr.readLine();
                                    if (name != null) {
                                        BTraceLogger.debugPrint("sending event command");
                                        sendEvent(client, name);
                                    }
                                } else if (option == '0') {
                                    BTraceLogger.debugPrint("continuing");
                                } else {
                                    cr.println("invalid option!");
                                }
                            } finally {
                                cw.unpark();
                            }
                        }
                    }
                } catch (IOException e) {
                    BTraceLogger.debugPrint(e);
                } catch (InterruptedException e) {
                    BTraceLogger.debugPrint(e);
                }
                System.exit(0);
            }
        });
        t.setDaemon(true);
        t.start();

        BTraceLogger.debugPrint("attaching to process");
        client.
            setPrintWriter(cw).
            setExtRepository(extRepository).
            setUnsafe(optionSet.has("unsafe")).
            setDumpClasses(optionSet.has("dumpClasses")).
            setDumpDir((String)optionSet.valueOf("dumpClasses")).
            setPort((Integer)optionSet.valueOf("P"))
            .attach();

        BTraceLogger.debugPrint("submitting the BTrace program");
        client.submit(trace, code, new String[0]);
    }

    private static Integer readPid(Integer pid, final ConsoleReader cr) throws IOException, NumberFormatException {
        while (pid == null) {
            cr.println("Select a process to attach to:");
            int counter = 0;
            List<JpsVM> vms = new ArrayList<JpsVM>();
            for(JpsVM vm : JpsProxy.getRunningVMs()) {
                vms.add(vm);
                cr.println((counter++ + 1) + ") " + vm.getPid() + "\t" + vm.getMainClass());
            }
            cr.setPrompt("Please, make a choice:");
            cr.flush();
            int ch = cr.readCharacter();

            int option = Integer.parseInt(String.valueOf((char)ch));

            if (option > 0 && option <= counter) {
                pid = vms.get(option - 1).getPid();
                cr.println("Attaching to: " + vms.get(option - 1).getMainClass() + "(PID=" +  pid + ")");
                cr.flush();
            }
        }
        return pid;
    }

    private static String readTrace(String trace, ConsoleReader cr) throws IOException {
        Completer c = new FileNameCompleter();
        try {
            cr.addCompleter(c);
            cr.setPrompt("Select the script to deploy: ");
            while (trace == null || trace.isEmpty()) {
                cr.flush();
                trace = cr.readLine().trim();
            }
            return trace;
        } finally {
            cr.removeCompleter(c);
        }
    }

    private static String getJarFor(String clz) {
        ClassLoader cl =  Main1.class.getClassLoader();
        URL resURL = cl.getResource(clz);
        if (resURL == null)  return null;

        String jarPath = resURL.toString().replace("jar:file:", "");
        jarPath = jarPath.substring(0, jarPath.indexOf(".jar!") + 4);

        return jarPath;
    }

    private static ExtensionsRepository getRepository(String extPath) {
        BTraceLogger.debugPrint("getting repository for " + extPath);
        return ExtensionsRepositoryFactory.composite(
                ExtensionsRepository.Location.BOTH,
                ExtensionsRepositoryFactory.builtin(ExtensionsRepository.Location.BOTH),
                ExtensionsRepositoryFactory.fixed(ExtensionsRepository.Location.BOTH, extPath)
        );
    }

    private static void errorExit(String msg, int code) {
        System.err.println(msg);
        exitCode.set(code);
        System.exit(code);
    }

    private static AtomicInteger exitCode = new AtomicInteger(-1); // -1 == normal shutdown
    private static void registerExitHook(final Client client) {
        BTraceLogger.debugPrint("registering shutdown hook");
        Runtime.getRuntime().addShutdownHook(new Thread(
            new Runnable() {

                public void run() {
                    BTraceLogger.debugPrint("exitting btrace client");
                    client.exit(exitCode.get());
                }
            })
        );
    }

    private static void sendEvent(Client client) throws IOException {
        sendEvent(client, null);
    }

    private static void sendEvent(Client client, final String eName) throws IOException {
        client.sendEvent(eName);
    }
}
