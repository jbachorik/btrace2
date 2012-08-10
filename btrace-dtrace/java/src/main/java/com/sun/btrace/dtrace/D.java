/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.btrace.dtrace;

import com.sun.btrace.api.extensions.BTraceExtension;

import com.sun.btrace.dtrace.commands.DTraceDataCommand;
import com.sun.btrace.dtrace.commands.DTraceDropCommand;
import com.sun.btrace.dtrace.commands.DTraceErrorCommand;
import com.sun.btrace.dtrace.commands.DTraceStartCommand;
import com.sun.btrace.dtrace.commands.DTraceStopCommand;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Resource;
import org.opensolaris.os.dtrace.Aggregate;
import org.opensolaris.os.dtrace.Aggregation;
import org.opensolaris.os.dtrace.AggregationRecord;
import org.opensolaris.os.dtrace.Consumer;
import org.opensolaris.os.dtrace.ConsumerAdapter;
import org.opensolaris.os.dtrace.ConsumerEvent;
import org.opensolaris.os.dtrace.ConsumerException;
import org.opensolaris.os.dtrace.DTraceException;
import org.opensolaris.os.dtrace.DataEvent;
import org.opensolaris.os.dtrace.DropEvent;
import org.opensolaris.os.dtrace.ErrorEvent;
import org.opensolaris.os.dtrace.ExceptionHandler;
import org.opensolaris.os.dtrace.LocalConsumer;
import org.opensolaris.os.dtrace.Option;
import com.sun.btrace.api.extensions.Runtime;
import com.sun.btrace.api.wireio.AbstractCommand;
import com.sun.btrace.wireio.commands.ErrorCommand;
import com.sun.btrace.wireio.commands.MessageCommand;

/**
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class D {
    @Resource
    private static Runtime rt;
    
    private static boolean dtraceEnabled;
    final private static AtomicBoolean inited = new AtomicBoolean(false);
    
    /**
     * Submits a D-script string and receives the BTrace script 
     * argument array as DTrace macro arguments. The events
     * from DTrace are wrapped as BTrace commands and listener
     * given is notified.
     *
     * @param program D-script as a string
     */
    public static void submit(String oneliner)  {
        init();
        if (oneliner != null && !oneliner.isEmpty()) {
            try {
                Consumer cons = newConsumer();
                cons.compile(oneliner, rt.$$());
                start(cons);
            } catch (final DTraceException dEx) {
                rt.send(ErrorCommand.class, new AbstractCommand.Initializer<ErrorCommand>() {

                    public void init(ErrorCommand cmd) {
                        cmd.setCause(dEx);
                    }
                });
            }
        }
    }
    
    /**
     * Submits a D-script from given file and receives the BTrace script 
     * argument array as DTrace macro arguments. The events
     * from DTrace are wrapped as BTrace commands and listener
     * given is notified.
     *
     * @param fileName path to D-script file to submit
     */
    public static void submitFile(String fileName) {
        init();
        if (fileName != null && !fileName.isEmpty()) {
            try {
                File dscript = new File(fileName);
                if (dscript.exists()) {
                    Consumer cons = newConsumer();
                    cons.compile(dscript, rt.$$());
                    start(cons);
                }
            } catch (final IOException e) {
                rt.send(ErrorCommand.class, new AbstractCommand.Initializer<ErrorCommand>() {

                    public void init(ErrorCommand cmd) {
                        cmd.setCause(e);
                    }
                });
            } catch (final DTraceException dEx) {
                rt.send(ErrorCommand.class, new AbstractCommand.Initializer<ErrorCommand>() {

                    public void init(ErrorCommand cmd) {
                        cmd.setCause(dEx);
                    }
                });
            }
        }
    }
    
        /**
     * BTrace to DTrace communication channel.
     * Raise DTrace USDT probe from BTrace.
     *
     * @see BTraceExtensionRuntime#dtraceProbe(String,String,int,int)
     */
    public static int probe(String str1, String str2) {
        return probe(str1, str2, -1, -1);
    }

    /**
     * BTrace to DTrace communication channel.
     * Raise DTrace USDT probe from BTrace.
     *
     * @see #dtraceProbe(String,String,int,int)
     */
    public static int probe(String str1, String str2, int i1) {
        return probe(str1, str2, i1, -1);
    }

    /**
     * BTrace to DTrace communication channel.
     * Raise DTrace USDT probe from BTrace.
     *
     * @param str1 first String param to DTrace probe
     * @param str2 second String param to DTrace probe
     * @param i1 first int param to DTrace probe
     * @param i2 second int param to DTrace probe
     */
    public static int probe(String str1, String str2, int i1, int i2) {
        init();
        return dtraceProbe0(str1, str2, i1, i2);
    }
    
    private static void start(Consumer cons) 
                              throws DTraceException {
        cons.enable();
        cons.go(new ExceptionHandler() {
            @Override
            public void handleException(final Throwable th) {
                rt.send(ErrorCommand.class, new AbstractCommand.Initializer<ErrorCommand>() {

                    public void init(ErrorCommand cmd) {
                        cmd.setCause(th);
                    }
                });
            }
        });
    }
    
    private static Consumer newConsumer() 
                            throws DTraceException {
        Consumer cons = new LocalConsumer();
        cons.addConsumerListener(new ConsumerAdapter() {
            @Override
            public void consumerStarted(final ConsumerEvent ce) {
                rt.send(DTraceStartCommand.class, new AbstractCommand.Initializer<DTraceStartCommand>() {

                    public void init(DTraceStartCommand cmd) {
                        cmd.setConsumerEvent(ce);
                    }
                });
            }

            @Override
            public void consumerStopped(final ConsumerEvent ce) {
                Consumer cons = (Consumer)ce.getSource();
                Aggregate ag = null;
                try {
                    ag = cons.getAggregate();
                } catch (final DTraceException dexp) {
                    rt.send(ErrorCommand.class, new AbstractCommand.Initializer<ErrorCommand>() {

                        public void init(ErrorCommand cmd) {
                            cmd.setCause(dexp);
                        }
                    });
                }
                StringBuilder buf = new StringBuilder();
                if (ag != null) {
                    for (Aggregation agg : ag.asMap().values()) {
                        String name = agg.getName();
                        if (name != null && name.length() > 0) {
                            buf.append(name);
                            buf.append('\n');
                        }
                        for (AggregationRecord rec : agg.asMap().values()) {
                            buf.append('\t');
                            buf.append(rec.getTuple());
                            buf.append(" ");
                            buf.append(rec.getValue());
                            buf.append('\n');
                        }
                    }
                }
                final String msg = buf.toString();
                if (msg.length() > 0) {
                    rt.send(MessageCommand.class, new AbstractCommand.Initializer<MessageCommand>() {

                        public void init(MessageCommand cmd) {
                            cmd.setMessage(msg);
                        }
                    });
                }
                rt.send(DTraceStopCommand.class, new AbstractCommand.Initializer<DTraceStopCommand>() {

                    public void init(DTraceStopCommand cmd) {
                        cmd.setConsumerEvent(ce);
                    }
                });
                cons.close();
            }

            @Override
            public void dataReceived(final DataEvent de) {
                rt.send(DTraceDataCommand.class, new AbstractCommand.Initializer<DTraceDataCommand>() {

                    public void init(DTraceDataCommand cmd) {
                        cmd.setDataEvent(de);
                    }
                });
            }

            @Override
            public void dataDropped(final DropEvent de) {
                rt.send(DTraceDropCommand.class, new AbstractCommand.Initializer<DTraceDropCommand>() {

                    public void init(DTraceDropCommand cmd) {
                        cmd.setDropEvent(de);
                    }
                });
            }

            @Override
            public void errorEncountered(final ErrorEvent ee) 
                throws ConsumerException {
                try {
                    super.errorEncountered(ee);
                } catch (final ConsumerException ce) {
                    rt.send(DTraceErrorCommand.class, new AbstractCommand.Initializer<DTraceErrorCommand>() {

                        public void init(DTraceErrorCommand cmd) {
                            cmd.setErrorEvent(ee);
                            cmd.setException(ce);
                        }
                    });
                    throw ce;
                }
            }
        });

        // open DTrace Consumer
        cons.open();

        // unused macro arguments are fine
        cons.setOption(Option.argref, "");
        // if no macro arg passed use "" or NULL
        cons.setOption(Option.defaultargs, "");
        // allow empty D-scripts
        cons.setOption(Option.empty, "");
        // be quiet! equivalent to DTrace's -q
        cons.setOption(Option.quiet, "");
        // undefined user land symbols are fine
        cons.setOption(Option.unodefs, "");
        // allow zero matching of probes (needed for late loading)
        cons.setOption(Option.zdefs, "");
        try {
            int pid = Integer.parseInt(rt.$(0));
            cons.grabProcess(pid);
        } catch (Exception ignored) {
        }
        return cons;
    }
    
    // raise DTrace USDT probe
    private static native int dtraceProbe0(String s1, String s2, int i1, int i2);
    
    
    private static void loadLibrary() {
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                loadBTraceLibrary(ClassLoader.getSystemClassLoader());
                return null;
            }
        });
    }

    private static void loadBTraceLibrary(final ClassLoader loader) {
        boolean isSolaris = System.getProperty("os.name").equals("SunOS");
        if (isSolaris) {
            try {
                System.loadLibrary("btrace");
                dtraceEnabled = true;
            } catch (LinkageError le) {
                if (loader == null
                        || loader.getResource("net/java/btrace") == null) {
                    System.err.println("cannot load libbtrace.so, will miss DTrace probes from BTrace");
                    return;
                }
                String path = loader.getResource("net/java/btrace").toString();
                path = path.substring(0, path.indexOf("!"));
                path = path.substring("jar:".length(), path.lastIndexOf('/'));
                String cpu = System.getProperty("os.arch");
                if (cpu.equals("x86")) {
                    cpu = "i386";
                }
                path += "/" + cpu + "/libbtrace.so";
                try {
                    path = new File(new URI(path)).getAbsolutePath();
                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    System.load(path);
                    dtraceEnabled = true;
                } catch (LinkageError le1) {
                    System.err.println("cannot load libbtrace.so, will miss DTrace probes from BTrace");
                }
            }
        }
    }
    
    private static void init() {
        if (inited.compareAndSet(false, true)) {
            loadLibrary();
        }
    }
}
