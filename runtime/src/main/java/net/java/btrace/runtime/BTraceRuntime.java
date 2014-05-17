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

import net.java.btrace.api.server.ShutdownHandler;
import net.java.btrace.annotations.OnError;
import net.java.btrace.annotations.OnEvent;
import net.java.btrace.annotations.OnExit;
import net.java.btrace.annotations.OnLowMemory;
import net.java.btrace.annotations.OnTimer;
import java.lang.management.ManagementFactory;
import static java.lang.management.ManagementFactory.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import com.sun.management.HotSpotDiagnosticMXBean;
import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.wireio.Channel;
import net.java.btrace.wireio.commands.ErrorCommand;
import net.java.btrace.wireio.commands.ExitCommand;
import net.java.btrace.wireio.commands.MessageCommand;


import java.lang.management.GarbageCollectorMXBean;

import java.io.File;
import java.io.IOException;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import net.java.btrace.api.core.PerfReader;
import net.java.btrace.api.wireio.Response;
import net.java.btrace.util.BTraceThreadFactory;
import sun.misc.Perf;
import sun.misc.Unsafe;
import sun.reflect.Reflection;

/**
 * Helper class used by BTrace built-in functions and
 * also acts runtime "manager" for a specific BTrace client
 * and sends Commands to the CommandListener passed.
 *
 * @author A. Sundararajan
 * @author Christian Glencross (aggregation support)
 * @author Joachim Skeie (GC MBean support, advanced Deque manipulation)
 * @author KLynch
 */
public final class BTraceRuntime {
    private static final String ALLOWED_CLIENT = "net.java.btrace.server.Session";
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
    // we need Unsafe to load BTrace class bytes as
    // bootstrap class

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    // a dummy BTraceRuntime instance
    final private static BTraceRuntime dummy;
    final public static BTraceRuntime NULL;
    final private static Thread samplerThread;
    volatile public static long TIMESTAMP = 0L;

    static {
        dummy = new BTraceRuntime();
        NULL = new BTraceRuntime();

        if (Boolean.getBoolean("btrace.timer.sampled")) {
            final long interval = Long.parseLong(System.getProperty("btrace.timer.sampled.interval", "500"));
            long time = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                unsafe.park(false, interval);
            }
            time = System.nanoTime() - time;
            final long step = (long) (time / 1000);

            samplerThread = new Thread(new Runnable() {

                public void run() {
                    while (true) {
                        unsafe.park(false, interval);
                        TIMESTAMP += step;
                    }
                }
            }, "BTrace Sampled Timer");
            samplerThread.setDaemon(true);
            samplerThread.start();
        } else {
            samplerThread = null;
        }
    }
    private static ThreadEnteredMap map = new ThreadEnteredMap(NULL);
    // BTraceRuntime against BTrace class name
    private static ConcurrentMap<String, BTraceRuntime> runtimes =
            new ConcurrentHashMap<String, BTraceRuntime>();
    // jvmstat related stuff
    // to read and write perf counters
    private static volatile Perf perf;
    // interface to read perf counters of this process
    private static volatile PerfReader perfReader;
    // performance counters created by this client
    private static Map<String, ByteBuffer> counters =
            new HashMap<String, ByteBuffer>();
    // Few MBeans used to implement certain built-in functions
    private static volatile MemoryMXBean memoryMBean;
    private static volatile RuntimeMXBean runtimeMBean;
    private static volatile HotSpotDiagnosticMXBean hotspotMBean;
    private static volatile List<GarbageCollectorMXBean> gcBeanList;
    private static volatile List<MemoryPoolMXBean> memPoolList;
    // bytecode generator that generates Runnable implementations
    private static RunnableGenerator runnableGenerator;
    // Per-client state starts here.
    // current thread's exception
    private ThreadLocal<Throwable> currentException = new ThreadLocal<Throwable>();
    // "command line" args supplied by client
    private String[] args;
    // whether current runtime has been disabled?
    private volatile boolean disabled;
    // Class object of the BTrace class [of this client]
    private String className;
    // BTrace Class object corresponding to this client
    private Class clazz;
    // does the client have exit action?
    private Method exitHandler;
    // does the client have exception handler action?
    private Method exceptionHandler;
    // array of timer callback methods
    private Method[] timerHandlers;
    // map of client event handling methods
    private Map<String, Method> eventHandlers;
    // low memory handlers
    private Map<String, Method> lowMemHandlers;
    // timer to run profile provider actions
    private volatile Timer timer;
    // executer to run low memory handlers
    private volatile ExecutorService threadPool;
    // Memory MBean listener
    private volatile NotificationListener memoryListener;
    // Command queue for the client
    private volatile LinkedBlockingQueue<AbstractCommand> queue;

    private static class SpeculativeQueueManager {
        // maximum number of speculative buffers

        private static final int MAX_SPECULATIVE_BUFFERS = Short.MAX_VALUE;
        // per buffer message limit
        private static final int MAX_SPECULATIVE_MSG_LIMIT = Short.MAX_VALUE;
        // next speculative buffer id
        private int nextSpeculationId;
        // speculative buffers map
        private ConcurrentHashMap<Integer, LinkedBlockingQueue<AbstractCommand>> speculativeQueues;
        // per thread current speculative buffer id
        private ThreadLocal<Integer> currentSpeculationId;

        SpeculativeQueueManager() {
            speculativeQueues = new ConcurrentHashMap<Integer, LinkedBlockingQueue<AbstractCommand>>();
            currentSpeculationId = new ThreadLocal<Integer>();
        }

        void clear() {
            speculativeQueues.clear();
            speculativeQueues = null;
            currentSpeculationId.remove();
            currentSpeculationId = null;
        }

        int speculation() {
            int nextId = getNextSpeculationId();
            if (nextId != -1) {
                speculativeQueues.put(nextId,
                        new LinkedBlockingQueue<AbstractCommand>(MAX_SPECULATIVE_MSG_LIMIT));
            }
            return nextId;
        }

        boolean send(AbstractCommand cmd) {
            final Integer curId = currentSpeculationId != null ? currentSpeculationId.get() : null;
            if (curId != null && cmd.canBeSpeculated()) {
                LinkedBlockingQueue<AbstractCommand> sb = speculativeQueues.get(curId);
                if (sb != null) {
                    try {
                        sb.add(cmd);
                    } catch (IllegalStateException ise) {
                        sb.clear();
                        BTraceRuntime current = getCurrent();
                        MessageCommand mc = current.channel.prepareCommand(MessageCommand.class, new AbstractCommand.Initializer<MessageCommand>() {
                            public void init(MessageCommand cmd) {
                                cmd.setMessage("speculative buffer overflow: " + curId);
                            }
                        });
                        
                        sb.add(mc);
                    }
                    return true;
                }
            }
            return false;
        }

        void speculate(int id) {
            validateId(id);
            currentSpeculationId.set(id);
        }

        void commit(int id, LinkedBlockingQueue<AbstractCommand> result) {
            validateId(id);
            currentSpeculationId.set(null);
            LinkedBlockingQueue<AbstractCommand> sb = speculativeQueues.get(id);
            if (sb != null) {
                result.addAll(sb);
                sb.clear();
            }
        }

        void discard(int id) {
            validateId(id);
            currentSpeculationId.set(null);
            speculativeQueues.get(id).clear();
        }

        // -- Internals only below this point
        private synchronized int getNextSpeculationId() {
            if (nextSpeculationId == MAX_SPECULATIVE_BUFFERS) {
                return -1;
            }
            return nextSpeculationId++;
        }

        private void validateId(int id) {
            if (!speculativeQueues.containsKey(id)) {
                throw new RuntimeException("invalid speculative buffer id: " + id);
            }
        }
    }
    // per client speculative buffer manager
    private volatile SpeculativeQueueManager specQueueManager;
    // background thread that sends Commands to the handler
//    private volatile Thread cmdThread;
    private final Instrumentation instrumentation;
    private final ExtensionsRepository repository;
//    private final Thread dataLinkThread;
    private final Channel channel;
    private final ShutdownHandler shutdown;

    private BTraceRuntime() {
        instrumentation = null;
        repository = null;
//        dataLinkThread = null;
        channel = null;
        shutdown = null;
    }

    /**
     * Creates a newly configured BTrace runtime instance
     * Intended to be used only from within the BTrace. Trying to instantiate
     * BTraceRuntime from any other class will result in IllegalArgumentException
     * @param shutdown The associated {@linkplain RuntimeShutdownHandler} instance
     * @param runtimeName A runtime name - it is taken from the BTrace script used
     * @param args Arguments passed to the BTrace agent
     * @param inst {@linkplain Instrumentation} instance
     * @param extRepository {@linkplain ExtensionsRepository} instance wrapping access to BTrace extensions
     * 
     * @throws IllegalArgumentException if called from outside of the BTrace core
     */
    public BTraceRuntime(ShutdownHandler shutdown, final String runtimeName, String[] args, Channel commChannel,
//            final CommandListener cmdListener,
            Instrumentation inst, ExtensionsRepository extRepository) {
        if (!Reflection.getCallerClass(2).getName().startsWith("net.java.btrace")) {
            throw new IllegalArgumentException();
        }
        this.args = args;
        this.queue = new LinkedBlockingQueue<AbstractCommand>();
        this.specQueueManager = new SpeculativeQueueManager();
        this.className = runtimeName;
        this.instrumentation = inst;
        this.repository = extRepository;
        this.channel = commChannel;
        runtimes.put(runtimeName, this);
        this.shutdown = shutdown;
    }

    public static String getClassName() {
        return getCurrent().className;
    }
    
    public static boolean classNameExists(String name) {
        return runtimes.containsKey(name);
    }

    public static void init(PerfReader perfRead, RunnableGenerator runGen) {
        Class caller = Reflection.getCallerClass(2);
        if (!caller.getName().startsWith(ALLOWED_CLIENT)) {
            throw new SecurityException("unsafe init");
        }
        perfReader = perfRead;
        runnableGenerator = runGen;
    }

    public Class defineClass(byte[] code) {
        Class caller = Reflection.getCallerClass(2);
        if (!caller.getName().startsWith(ALLOWED_CLIENT)) {
            throw new SecurityException("unsafe defineClass");
        }
        return defineClassImpl(code, true);
    }

    public Class defineClass(byte[] code, boolean mustBeBootstrap) {
        Class caller = Reflection.getCallerClass(2);
        if (!caller.getName().startsWith(ALLOWED_CLIENT)) {
            throw new SecurityException("unsafe defineClass");
        }
        return defineClassImpl(code, mustBeBootstrap);
    }

    /**
     * Enter method is called by every probed method just
     * before the probe actions start.
     */
    public static boolean enter(BTraceRuntime current) {
        if (current.disabled) {
            return false;
        }
        return map.enter(current);
    }

    public static boolean enter() {
        return enter(dummy);
    }

    /**
     * Leave method is called by every probed method just
     * before the probe actions end (and actual probed
     * method continues).
     */
    public static void leave() {
        map.exit();
    }

    /**
     * start method is called by every BTrace (preprocesed) class
     * just at the end of it's class initializer.
     */
    public static void start() {
        BTraceRuntime current = getCurrent();
        if (current != null) {
            current.startImpl();
        }
    }

//    public void handleExit(int exitCode) {
//        exitImpl(exitCode);
//    }

    public void handleEvent(String event) {
        if (eventHandlers != null) {
            Method eventHandler = eventHandlers.get(event);
            if (eventHandler != null) {
                BTraceRuntime oldRuntime = (BTraceRuntime) map.get();
                leave();
                try {
                    eventHandler.invoke(null, (Object[]) null);
                } catch (Throwable ignored) {
                } finally {
                    if (oldRuntime != null) {
                        enter(oldRuntime);
                    }
                }
            }
        }
    }

    /**
     * One instance of BTraceRuntime is created per-client.
     * This forClass method creates it. Class passed is the
     * preprocessed BTrace program of the client.
     */
    public static BTraceRuntime forClass(Class cl) {
        BTraceRuntime runtime = runtimes.get(cl.getName());
        runtime.init(cl);
        return runtime;
    }

    /**
     * Utility to create a new ThreadLocal object. Called
     * by preprocessed BTrace class to create ThreadLocal
     * for each @TLS variable.
     * @param initValue Initial value.
     *                  This value must be either a boxed primitive or {@linkplain Cloneable}.
     *                  In case a {@linkplain Cloneable} value is provided the value is never used directly
     *                  - instead, a new clone of the value is created per thread.
     */
    public static ThreadLocal newThreadLocal(
            final Object initValue) {
        return new ThreadLocal() {

            @Override
            protected Object initialValue() {
                if (initValue == null) {
                    return initValue;
                }

                if (initValue instanceof Cloneable) {
                    try {
                        Class clz = initValue.getClass();
                        Method m = clz.getDeclaredMethod("clone");
                        m.setAccessible(true);
                        return m.invoke(initValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                return initValue;
            }
        };
    }

    // The following constants are copied from VM code
    // for jvmstat.
    // perf counter variability - we always variable variability
    private static final int V_Variable = 3;
    // perf counter units
    private static final int V_None = 1;
    private static final int V_String = 5;
    private static final int PERF_STRING_LIMIT = 256;

    /**
     * Utility to create a new jvmstat perf counter. Called
     * by preprocessed BTrace class to create perf counter
     * for each @Export variable.
     */
    public static void newPerfCounter(String name, String desc, Object value) {
        Perf perf = getPerf();
        char tc = desc.charAt(0);
        switch (tc) {
            case 'C':
            case 'Z':
            case 'B':
            case 'S':
            case 'I':
            case 'J':
            case 'F':
            case 'D': {
                long initValue = (value != null) ? ((Number) value).longValue() : 0L;
                ByteBuffer b = perf.createLong(name, V_Variable, V_None, initValue);
                b.order(ByteOrder.nativeOrder());
                counters.put(name, b);
            }
            break;

            case '[':
                break;
            case 'L': {
                if (desc.equals("Ljava/lang/String;")) {
                    byte[] buf;
                    if (value != null) {
                        buf = getStringBytes((String) value);
                    } else {
                        buf = new byte[PERF_STRING_LIMIT];
                        buf[0] = '\0';
                    }
                    ByteBuffer b = perf.createByteArray(name, V_Variable, V_String,
                            buf, buf.length);
                    counters.put(name, b);
                }
            }
            break;
        }
    }

    /**
     * Return the value of integer perf. counter of given name.
     */
    public static int getPerfInt(String name) {
        return (int) getPerfLong(name);
    }

    /**
     * Write the value of integer perf. counter of given name.
     */
    public static void putPerfInt(int value, String name) {
        long l = (long) value;
        putPerfLong(l, name);
    }

    /**
     * Return the value of float perf. counter of given name.
     */
    public static float getPerfFloat(String name) {
        int val = getPerfInt(name);
        return Float.intBitsToFloat(val);
    }

    /**
     * Write the value of float perf. counter of given name.
     */
    public static void putPerfFloat(float value, String name) {
        int i = Float.floatToRawIntBits(value);
        putPerfInt(i, name);
    }

    /**
     * Return the value of long perf. counter of given name.
     */
    public static long getPerfLong(String name) {
        ByteBuffer b = counters.get(name);
        synchronized (b) {
            long l = b.getLong();
            b.rewind();
            return l;
        }
    }

    /**
     * Write the value of float perf. counter of given name.
     */
    public static void putPerfLong(long value, String name) {
        ByteBuffer b = counters.get(name);
        synchronized (b) {
            b.putLong(value);
            b.rewind();
        }
    }

    /**
     * Return the value of double perf. counter of given name.
     */
    public static double getPerfDouble(String name) {
        long val = getPerfLong(name);
        return Double.longBitsToDouble(val);
    }

    /**
     * write the value of double perf. counter of given name.
     */
    public static void putPerfDouble(double value, String name) {
        long l = Double.doubleToRawLongBits(value);
        putPerfLong(l, name);
    }

    /**
     * Return the value of String perf. counter of given name.
     */
    public static String getPerfString(String name) {
        ByteBuffer b = counters.get(name);
        byte[] buf = new byte[b.limit()];
        byte t = (byte) 0;
        int i = 0;
        synchronized (b) {
            while ((t = b.get()) != '\0') {
                buf[i++] = t;
            }
            b.rewind();
        }
        try {
            return new String(buf, 0, i, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // ignore, UTF-8 encoding is always known
        }
        return "";
    }

    /**
     * Write the value of float perf. counter of given name.
     */
    public static void putPerfString(String value, String name) {
        ByteBuffer b = counters.get(name);
        byte[] v = getStringBytes(value);
        synchronized (b) {
            b.put(v);
            b.rewind();
        }
    }

    /**
     * Handles exception from BTrace probe actions.
     */
    public static void handleException(Throwable th) {
        BTraceRuntime current = getCurrent();
        if (current != null) {
            current.handleExceptionImpl(th);
        } else {
            th.printStackTrace();
        }
    }

    // package-private interface to BTraceUtils class.
    // BTrace exit built-in function
    public static void exit(int exitCode) {
        BTraceRuntime rt = getCurrent();
        Throwable th =rt. currentException.get();
        if (!(th instanceof ExitException)) {
            rt.currentException.set(null);
        }
        throw new ExitException(exitCode);
    }

    static long sizeof(Object obj) {
        return getCurrent().instrumentation.getObjectSize(obj);
    }

    // BTrace command line argument functions
    public int $length() {
        return args == null ? 0 : args.length;
    }

    public String[] $$() {
        String[] ret = new String[args.length];
        System.arraycopy(args, 0, ret, 0, args.length);
        return ret;
    }
    
    public String $(int n) {
        if (n >= 0 && n < args.length) {
            return args[n];
        } else {
            return null;
        }
    }

    // BTrace perf counter reading functions
    static int perfInt(String name) {
        return getPerfReader().perfInt(name);
    }

    static long perfLong(String name) {
        return getPerfReader().perfLong(name);
    }

    static String perfString(String name) {
        return getPerfReader().perfString(name);
    }
    
    /**
     * Get the current thread BTraceRuntime instance
     * if there is one.
     */
    public static BTraceRuntime getCurrent() {
        BTraceRuntime current = (BTraceRuntime) map.get();
        assert current != null : "BTraceRuntime is null!";
        return current;
    }
    
    public static String getValidTraceClassName(String origClassName) {
        int cntr = 1;
        
        String className = origClassName;
        while (true) {
            try {
                ClassLoader.getSystemClassLoader().loadClass(className);
                className = origClassName + "$" + (cntr++);
            } catch (ClassNotFoundException e) {
                break;
            }
        }
        return className;
    }
    
    /**
     * Returns identity string of the form class-name@identity-hash
     *
     * @param obj object for which identity string is returned
     * @return identity string
     */
    public static String identityStr(Object obj) {
        int hashCode = java.lang.System.identityHashCode(obj);
        return obj.getClass().getName() + "@" + Integer.toHexString(hashCode);
    }
    
    /**
     * Returns the same hash code for the given object as
     * would be returned by the default method hashCode(),
     * whether or not the given object's class overrides
     * hashCode(). The hash code for the null reference is zero.
     *
     * @param  obj object for which the hashCode is to be calculated
     * @return the hashCode
     */
    public static int identityHashCode(Object obj) {
        return java.lang.System.identityHashCode(obj);
    }

    /**
     * Returns a hash code value for the object. This method is supported
     * for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>. For bootstrap classes, returns the
     * result of calling Object.hashCode() override. For non-bootstrap classes,
     * the identity hash code is returned.
     *
     * @param obj the Object whose hash code is returned.
     * @return  a hash code value for the given object.
     */
    public static int hash(Object obj) {
        if (obj.getClass().getClassLoader() == null) {
            return obj.hashCode();
        } else {
            return java.lang.System.identityHashCode(obj);
        }
    }

    /**
     * Indicates whether two given objects are "equal to" one another.
     * For bootstrap classes, returns the result of calling Object.equals()
     * override. For non-bootstrap classes, the reference identity comparison
     * is done.
     *
     * @param  obj1 first object to compare equality
     * @param  obj2 second object to compare equality
     * @return <code>true</code> if the given objects are equal;
     *         <code>false</code> otherwise.
     */
    public static boolean compare(Object obj1, Object obj2) {
        if (obj1 instanceof String) {
            return obj1.equals(obj2);
        } else if (obj1.getClass().getClassLoader() == null) {
            if (obj2 == null || obj2.getClass().getClassLoader() == null) {
                return obj1.equals(obj2);
            } // else fall through..
        }
        return obj1 == obj2;
    }

    // private methods below this point

    private ExecutorService getThreadPool() {
        if (threadPool == null) {
            synchronized (this) {
                if (threadPool == null) {
                    threadPool = Executors.newFixedThreadPool(1,
                            new BTraceThreadFactory("BTrace Runtime"));
                }
            }
        }
        return threadPool;
    }

    private void initMemoryListener() {
        memoryListener = new NotificationListener() {

            public void handleNotification(Notification notif, Object handback) {
                String notifType = notif.getType();
                if (notifType.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                    CompositeData cd = (CompositeData) notif.getUserData();
                    final MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);
                    String name = info.getPoolName();
                    final Method handler = lowMemHandlers.get(name);
                    if (handler != null) {
                        getThreadPool().submit(new Runnable() {

                            public void run() {
                                try {
                                    if (handler.getParameterTypes().length == 1) {
                                        handler.invoke(null, info.getUsage());
                                    } else {
                                        handler.invoke(null, (Object[]) null);
                                    }
                                } catch (Throwable th) {
                                }
                            }
                        });
                    }
                }
            }
        };
    }

    public static RuntimeMXBean getRuntimeMBean() {
        if (runtimeMBean == null) {
            synchronized (BTraceRuntime.class) {
                if (runtimeMBean == null) {
                    try {
                        return AccessController.doPrivileged(
                                new PrivilegedExceptionAction<RuntimeMXBean>() {

                                    public RuntimeMXBean run() throws Exception {
                                        return ManagementFactory.getRuntimeMXBean();
                                    }
                                });
                    } catch (Exception exp) {
                        throw new UnsupportedOperationException(exp);
                    }
                }
            }
        }
        return runtimeMBean;
    }

    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        if (memPoolList == null) {
            synchronized (BTraceRuntime.class) {
                if (memPoolList == null) {
                    try {
                        memPoolList = AccessController.doPrivileged(
                                new PrivilegedExceptionAction<List<MemoryPoolMXBean>>() {

                                    public List<MemoryPoolMXBean> run() throws Exception {
                                        return ManagementFactory.getMemoryPoolMXBeans();
                                    }
                                });
                    } catch (Exception exp) {
                        throw new UnsupportedOperationException(exp);
                    }
                }
            }
        }
        return memPoolList;
    }
    
    /**
     * 
     * @return Returns the {@linkplain HotSpotDiagnosticMXBean} MBean for the running JVM
     */
    public static HotSpotDiagnosticMXBean getHotSpotMBean() {
        if (hotspotMBean == null) {
            synchronized (BTraceRuntime.class) {
                if (hotspotMBean == null) {
                    try {
                        hotspotMBean = AccessController.doPrivileged(
                                new PrivilegedExceptionAction<HotSpotDiagnosticMXBean>() {

                                    public HotSpotDiagnosticMXBean run() throws Exception {
                                        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                                        Set<ObjectName> s = server.queryNames(new ObjectName(HOTSPOT_BEAN_NAME), null);
                                        Iterator<ObjectName> itr = s.iterator();
                                        if (itr.hasNext()) {
                                            ObjectName name = itr.next();
                                            HotSpotDiagnosticMXBean bean =
                                                    ManagementFactory.newPlatformMXBeanProxy(server,
                                                    name.toString(), HotSpotDiagnosticMXBean.class);
                                            return bean;
                                        } else {
                                            return null;
                                        }
                                    }
                                });
                    } catch (Exception exp) {
                        throw new UnsupportedOperationException(exp);
                    }
                }
            }
        }
        return hotspotMBean;
    }

    /**
     * 
     * @return Returns a list of {@linkplain GarbageCollectorMXBean} instances registered in the running JVM
     */
    public static List<GarbageCollectorMXBean> getGarbageCollectionMBeans() {
        if (gcBeanList == null) {
            synchronized (BTraceRuntime.class) {
                if (gcBeanList == null) {
                    try {
                        gcBeanList = AccessController.doPrivileged(
                                new PrivilegedExceptionAction<List<GarbageCollectorMXBean>>() {

                                    public List<GarbageCollectorMXBean> run() throws Exception {
                                        return ManagementFactory.getGarbageCollectorMXBeans();
                                    }
                                });
                    } catch (Exception exp) {
                        throw new UnsupportedOperationException(exp);
                    }
                }
            }
        }
        return gcBeanList;
    }

    public static PerfReader getPerfReader() {
        if (perfReader == null) {
            throw new UnsupportedOperationException();
        }
        return perfReader;
    }

    private static RunnableGenerator getRunnableGenerator() {
        return runnableGenerator;
    }
    
    public static <T extends AbstractCommand> Response<T> send(Class<? extends T> cmdClass, AbstractCommand.Initializer<T> init) {
        return send(cmdClass, init, getCurrent());
    }
    
    private static <T extends AbstractCommand> Response<T> send(Class<? extends T> cmdClass, AbstractCommand.Initializer<T> init, BTraceRuntime rt) {
        try {
            return rt.channel.sendCommand(cmdClass, init);
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return null;
    }

    public int speculation() {
        return specQueueManager.speculation();
    }

    public void speculate(int id) {
        specQueueManager.speculate(id);
    }

    public void discard(int id) {
        specQueueManager.discard(id);
    }

    public void commit(int id) {
        specQueueManager.commit(id, queue);
    }

    public void shutdown() {
        disabled = true;
        if (timer != null) {
            timer.cancel();
        }

        if (memoryListener != null && memoryMBean != null) {
            NotificationEmitter emitter = (NotificationEmitter) memoryMBean;
            try {
                emitter.removeNotificationListener(memoryListener);
            } catch (ListenerNotFoundException lnfe) {
            }
        }

        synchronized(this) {
            if (threadPool != null) {
                threadPool.shutdownNow();
            }
        }
        
        specQueueManager.clear();
        runtimes.remove(className);
    }
    
    public static void retransform(String runtimeName, Class<?> clazz) {
        try {
            BTraceRuntime rt = runtimes.get(runtimeName);
            if (rt != null && rt.instrumentation.isModifiableClass(clazz)) {
                rt.instrumentation.retransformClasses(clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static RuntimeException translate(Exception exp) {
        if (exp instanceof RuntimeException) {
            return (RuntimeException) exp;
        } else {
            return new RuntimeException(exp);
        }
    }

    private void handleExceptionImpl(final Throwable th) {
        if (currentException.get() != null) {
            return;
        }
        leave();
        currentException.set(th);
        try {
            if (th instanceof ExitException) {
                exitImpl(((ExitException) th).exitCode());
            } else {
                if (exceptionHandler != null) {
                    try {
                        exceptionHandler.invoke(null, th);
                    } catch (Throwable ignored) {
                    }
                } else {
                    try {
//                         Do not call send(Command). Exception messages should not
//                         go to speculative buffers!
                        ErrorCommand ec = channel.prepareCommand(ErrorCommand.class, new AbstractCommand.Initializer<ErrorCommand>() {
                            public void init(ErrorCommand cmd) {
                                cmd.setCause(th);
                            }
                        });
                        
                        queue.put(ec);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        } finally {
            currentException.set(null);
        }
    }

    private void startImpl() {
        if (timerHandlers != null && timerHandlers.length != 0) {
            timer = new Timer(true);
            RunnableGenerator gen = getRunnableGenerator();
            Runnable[] runnables = new Runnable[timerHandlers.length];
            if (gen != null) {
                generateRunnables(gen, runnables);
            } else {
                wrapToRunnables(runnables);
            }
            for (int index = 0; index < timerHandlers.length; index++) {
                Method m = timerHandlers[index];
                OnTimer tp = m.getAnnotation(OnTimer.class);
                long period = tp.value();
                final Runnable r = runnables[index];
                timer.schedule(new TimerTask() {

                    public void run() {
                        r.run();
                    }
                }, period, period);
            }
        }

        if (!lowMemHandlers.isEmpty()) {
            initMemoryListener();
            ((NotificationEmitter) getMemoryMBean()).addNotificationListener(memoryListener, null, null);
        }

        leave();
    }

    private void generateRunnables(RunnableGenerator gen, Runnable[] runnables) {
        final MemoryClassLoader loader = AccessController.doPrivileged(
                new PrivilegedAction<MemoryClassLoader>() {

                    public MemoryClassLoader run() {
                        return new MemoryClassLoader(clazz.getClassLoader());
                    }
                });

        for (int index = 0; index < timerHandlers.length; index++) {
            Method m = timerHandlers[index];
            try {
                final String className = "net/java/btrace/BTraceRunnable$" + index;
                final byte[] buf = gen.generate(m, className);
                Class cls = AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Class>() {

                            public Class run() throws Exception {
                                return loader.loadClass(className.replace('/', '.'), buf);
                            }
                        });
                runnables[index] = (Runnable) cls.newInstance();
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception exp) {
                throw new RuntimeException(exp);
            }
        }
    }

    private void wrapToRunnables(Runnable[] runnables) {
        for (int index = 0; index < timerHandlers.length; index++) {
            final Method m = timerHandlers[index];
            runnables[index] = new Runnable() {

                public void run() {
                    try {
                        m.invoke(null, (Object[]) null);
                    } catch (Throwable th) {
                    }
                }
            };
        }
    }

    private synchronized void exitImpl(final int exitCode) {
        System.err.println("[btrace] Notifyng BTrace client about the application shutdown ...");
        if (exitHandler != null) {
            try {
                exitHandler.invoke(null, exitCode);
            } catch (Throwable ignored) {
            }
        }

        if (shutdown == null) {
            shutdown();
                
            try {
                channel.sendCommand(ExitCommand.class, new AbstractCommand.Initializer<ExitCommand>() {

                    public void init(ExitCommand cmd) {
                        cmd.setExitCode(exitCode);
                    }
                });
            } catch (IOException e) {
                BTraceLogger.debugPrint(e);
            } finally {
                channel.close();
            }
        } else {
            getThreadPool().submit(new Runnable() {

                @Override
                public void run() {
                    shutdown.shutdown(exitCode);
                }
            });
            
        }
    }

    private static Perf getPerf() {
        if (perf == null) {
            synchronized (BTraceRuntime.class) {
                if (perf == null) {
                    perf = (Perf) AccessController.doPrivileged(new Perf.GetPerfAction());
                }
            }
        }
        return perf;
    }

    private static byte[] getStringBytes(String value) {
        byte[] v = null;
        try {
            v = value.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        byte[] v1 = new byte[v.length + 1];
        System.arraycopy(v, 0, v1, 0, v.length);
        v1[v.length] = '\0';
        return v1;
    }

    private Class defineClassImpl(byte[] code, boolean mustBeBootstrap) {
        ClassLoader loader = null;
        if (!mustBeBootstrap) {
            loader = new ClassLoader(null) {
            };
        }
        Class cl = unsafe.defineClass(className, code, 0, code.length, loader, null);
        unsafe.ensureClassInitialized(cl);
        return cl;
    }

    private void init(Class cl) {
        if (this.clazz != null) {
            return;
        }

        this.clazz = cl;
        List<Method> timersList = new ArrayList<Method>();
        this.eventHandlers = new HashMap<String, Method>();
        this.lowMemHandlers = new HashMap<String, Method>();

        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            int modifiers = m.getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                continue;
            }

            OnEvent oev = m.getAnnotation(OnEvent.class);
            if (oev != null && m.getParameterTypes().length == 0) {
                eventHandlers.put(oev.value(), m);
            }

            OnError oer = m.getAnnotation(OnError.class);
            if (oer != null) {
                Class[] argTypes = m.getParameterTypes();
                if (argTypes.length == 1 && argTypes[0] == Throwable.class) {
                    this.exceptionHandler = m;
                }
            }

            OnExit oex = m.getAnnotation(OnExit.class);
            if (oex != null) {
                Class[] argTypes = m.getParameterTypes();
                if (argTypes.length == 1 && argTypes[0] == int.class) {
                    this.exitHandler = m;
                }
            }

            OnTimer ot = m.getAnnotation(OnTimer.class);
            if (ot != null && m.getParameterTypes().length == 0) {
                timersList.add(m);
            }

            OnLowMemory olm = m.getAnnotation(OnLowMemory.class);
            if (olm != null) {
                Class[] argTypes = m.getParameterTypes();
                if ((argTypes.length == 0)
                        || (argTypes.length == 1 && argTypes[0] == MemoryUsage.class)) {
                    lowMemHandlers.put(olm.pool(), m);
                }
            }
        }

        for (MemoryPoolMXBean mpoolBean : getMemoryPoolMXBeans()) {
            String name = mpoolBean.getName();
            if (lowMemHandlers.containsKey(name)) {
                Method m = lowMemHandlers.get(name);
                OnLowMemory olm = m.getAnnotation(OnLowMemory.class);
                if (mpoolBean.isUsageThresholdSupported()) {
                    mpoolBean.setUsageThreshold(olm.threshold());
                }
            }
        }

        timerHandlers = new Method[timersList.size()];
        timerHandlers = timersList.toArray(timerHandlers);

        BTraceMBeanImpl.registerMBean(clazz, repository);
    }

    public static String resolveFileName(String name) {
        if (name.indexOf(File.separatorChar) != -1) {
            throw new IllegalArgumentException("directories are not allowed");
        }
        StringBuilder buf = new StringBuilder();
        buf.append('.');
        buf.append(File.separatorChar);
        BTraceRuntime runtime = getCurrent();
        buf.append("btrace");
        if (runtime.args != null && runtime.args.length > 0) {
            buf.append(runtime.args[0]);
        }
        buf.append(File.separatorChar);
        buf.append(runtime.className);
        new File(buf.toString()).mkdirs();
        buf.append(File.separatorChar);
        buf.append(name);
        return buf.toString();
    }

    public static MemoryMXBean getMemoryMBean() {
        if (memoryMBean == null) {
            synchronized (BTraceRuntime.class) {
                if (memoryMBean == null) {
                    try {
                        memoryMBean = AccessController.doPrivileged(
                                new PrivilegedExceptionAction<MemoryMXBean>() {

                                    public MemoryMXBean run() throws Exception {
                                        return ManagementFactory.getMemoryMXBean();
                                    }
                                });
                    } catch (Exception exp) {
                        throw new UnsupportedOperationException(exp);
                    }
                }
            }
        }
        return memoryMBean;
    }
}
