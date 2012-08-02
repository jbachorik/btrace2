/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.ext;

import net.java.btrace.api.extensions.BTraceExtension;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.Set;
import static net.java.btrace.ext.Printer.*;

/**
 * Provides the threads related methods to BTrace
 * @author Jaroslav Bachorik
 * @since 1.3
 */
@BTraceExtension
public class Threads {

    private static volatile ThreadMXBean threadMBean;

    // Thread and stack access
    /**
     * Tests whether this thread has been interrupted.  The <i>interrupted
     * status</i> of the thread is unaffected by this method.
     *
     * <p>A thread interruption ignored because a thread was not alive
     * at the time of the interrupt will be reflected by this method
     * returning false.
     *
     * @return <code>true</code> if this thread has been interrupted;
     *         <code>false</code> otherwise.
     */
    public static boolean isInteruppted() {
        return Thread.currentThread().isInterrupted();
    }

    /**
     * Prints the java stack trace of the current thread.
     */
    public static void jstack() {
        jstack(-1);
    }

    /**
     * Prints the java stack trace of the current thread. But,
     * atmost given number of frames.
     *
     * @param numFrames number of frames to be printed. When this is
     *        negative all frames are printed.
     */
    public static void jstack(int numFrames) {
        if (numFrames == 0) {
            return;
        }
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        // passing '5' to skip our own frames to generate stack trace
        stackTrace(st, 5, numFrames);
    }

    /**
     * Prints Java stack traces of all the Java threads.
     */
    public static void jstackAll() {
        jstackAll(-1);
    }

    /**
     * Prints Java stack traces of all the Java threads. But,
     * atmost given number of frames.
     *
     * @param numFrames number of frames to be printed. When this is
     *        negative all frames are printed.
     */
    public static void jstackAll(int numFrames) {
        stackTraceAll(numFrames);
    }

    /**
     * Returns the stack trace of current thread as a String.
     *
     * @return the stack trace as a String.
     */
    public static String jstackStr() {
        return jstackStr(-1);
    }

    /**
     * Returns the stack trace of the current thread as a String
     * but includes atmost the given number of frames.
     *
     * @param  numFrames number of frames to be included. When this is
     *         negative all frames are included.
     * @return the stack trace as a String.
     */
    public static String jstackStr(int numFrames) {
        if (numFrames == 0) {
            return "";
        }
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        // passing '5' to skip our own frames to generate stack trace
        return stackTraceStr(st, 5, numFrames);
    }

    /**
     * Returns the stack traces of all Java threads as a String.
     *
     * @return the stack traces as a String.
     */
    public static String jstackAllStr() {
        return jstackAllStr(-1);
    }

    /**
     * Returns atmost given number of frames in stack traces
     * of all threads as a String.
     *
     * @param numFrames number of frames to be included. When this is
     *        negative all frames are included.
     * @return the stack traces as a String.
     */
    public static String jstackAllStr(int numFrames) {
        if (numFrames == 0) {
            return "";
        }
        return stackTraceAllStr(numFrames);
    }

    /**
     * Prints the stack trace of the given exception object.
     *
     * @param exception throwable for which stack trace is printed.
     */
    public static void jstack(Throwable exception) {
        jstack(exception, -1);
    }

    /**
     * Prints the stack trace of the given exception object. But,
     * prints at most given number of frames.
     *
     * @param exception throwable for which stack trace is printed.
     * @param numFrames maximum number of frames to be printed.
     */
    public static void jstack(Throwable exception, int numFrames) {
        if (numFrames == 0) {
            return;
        }
        StackTraceElement[] st = exception.getStackTrace();
        println(exception.toString());
        stackTrace("\t", st, 0, numFrames);
        Throwable cause = exception.getCause();
        while (cause != null) {
            println("Caused by:");
            st = cause.getStackTrace();
            stackTrace("\t", st, 0, numFrames);
            cause = cause.getCause();
        }
    }

    /**
     * Returns the stack trace of given exception object as a String.
     *
     * @param exception the throwable for which stack trace is returned.
     */
    public static String jstackStr(Throwable exception) {
        return jstackStr(exception, -1);
    }

    /**
     * Returns stack trace of given exception object as a String.
     *
     * @param exception throwable for which stack trace is returned.
     * @param numFrames maximum number of frames to be returned.
     */
    public static String jstackStr(Throwable exception, int numFrames) {
        if (numFrames == 0) {
            return "";
        }
        StackTraceElement[] st = exception.getStackTrace();
        StringBuilder buf = new StringBuilder();
        buf.append(exception.toString());
        buf.append(stackTraceStr("\t", st, 0, numFrames));
        Throwable cause = exception.getCause();
        while (cause != null) {
            buf.append("Caused by:");
            st = cause.getStackTrace();
            buf.append(stackTraceStr("\t", st, 0, numFrames));
            cause = cause.getCause();
        }
        return buf.toString();
    }

    /**
     * Returns a reference to the currently executing thread object.
     *
     * @return  the currently executing thread.
     */
    public static Thread currentThread() {
        return Thread.currentThread();
    }

    /**
     * Returns the identifier of the given Thread.  The thread ID is a positive
     * <tt>long</tt> number generated when the given thread was created.
     * The thread ID is unique and remains unchanged during its lifetime.
     * When a thread is terminated, the thread ID may be reused.
     */
    public static long threadId(Thread thread) {
        return thread.getId();
    }

    /**
     * Returns the state of the given thread.
     * This method is designed for use in monitoring of the system state,
     * not for synchronization control.
     */
    public static Thread.State threadState(Thread thread) {
        return thread.getState();
    }

    /**
     * Returns <tt>true</tt> if and only if the current thread holds the
     * monitor lock on the specified object.
     *
     * <p>This method is designed to allow a program to assert that
     * the current thread already holds a specified lock:
     * <pre>
     *     assert Thread.holdsLock(obj);
     * </pre>
     *
     * @param  obj the object on which to test lock ownership
     * @throws NullPointerException if obj is <tt>null</tt>
     * @return <tt>true</tt> if the current thread holds the monitor lock on
     *         the specified object.
     */
    public static boolean holdsLock(Object obj) {
        return Thread.holdsLock(obj);
    }

    /**
     * Prints the Java level deadlocks detected (if any).
     */
    public static void deadlocks() {
        deadlocks(true);
    }

    /**
     * Prints deadlocks detected (if any). Optionally prints
     * stack trace of the deadlocked threads.
     *
     * @param stackTrace boolean flag to specify whether to
     *        print stack traces of deadlocked threads or not.
     */
    public static void deadlocks(boolean stackTrace) {
        initThreadMBean();
        if (threadMBean.isSynchronizerUsageSupported()) {
            long[] tids = threadMBean.findDeadlockedThreads();
            if (tids != null && tids.length > 0) {
                ThreadInfo[] infos = threadMBean.getThreadInfo(tids, true, true);
                StringBuilder sb = new StringBuilder();
                for (ThreadInfo ti : infos) {
                    sb.append("\"").append(ti.getThreadName()).append("\"" + " Id=").append(ti.getThreadId()).append(" in ").append(ti.getThreadState());
                    if (ti.getLockName() != null) {
                        sb.append(" on lock=").append(ti.getLockName());
                    }
                    if (ti.isSuspended()) {
                        sb.append(" (suspended)");
                    }
                    if (ti.isInNative()) {
                        sb.append(" (running in native)");
                    }
                    if (ti.getLockOwnerName() != null) {
                        sb.append(INDENT).append(" owned by ").append(ti.getLockOwnerName()).append(" Id=").append(ti.getLockOwnerId());
                        sb.append(LINE_SEPARATOR);
                    }

                    if (stackTrace) {
                        // print stack trace with locks
                        StackTraceElement[] stacktrace = ti.getStackTrace();
                        MonitorInfo[] monitors = ti.getLockedMonitors();
                        for (int i = 0; i < stacktrace.length; i++) {
                            StackTraceElement ste = stacktrace[i];
                            sb.append(INDENT).append("at ").append(ste.toString());
                            sb.append(LINE_SEPARATOR);
                            for (MonitorInfo mi : monitors) {
                                if (mi.getLockedStackDepth() == i) {
                                    sb.append(INDENT).append("  - locked ").append(mi);
                                    sb.append(LINE_SEPARATOR);
                                }
                            }
                        }
                        sb.append(LINE_SEPARATOR);
                    }

                    LockInfo[] locks = ti.getLockedSynchronizers();
                    sb.append(INDENT).append("Locked synchronizers: count = ").append(locks.length);
                    sb.append(LINE_SEPARATOR);
                    for (LockInfo li : locks) {
                        sb.append(INDENT).append("  - ").append(li);
                        sb.append(LINE_SEPARATOR);
                    }
                    sb.append(LINE_SEPARATOR);
                }
                print(sb.toString());
            }
        }
    }

    /**
     * Returns the name of the given thread.
     *
     * @param thread thread whose name is returned
     */
    public static String name(Thread thread) {
        return thread.getName();
    }

    /**
     * Returns the current number of live threads including both
     * daemon and non-daemon threads.
     *
     * @return the current number of live threads.
     */
    public static long threadCount() {
        initThreadMBean();
        return threadMBean.getThreadCount();
    }

    /**
     * Returns the peak live thread count since the Java virtual machine
     * started or peak was reset.
     *
     * @return the peak live thread count.
     */
    public static long peakThreadCount() {
        initThreadMBean();
        return threadMBean.getPeakThreadCount();
    }

    /**
     * Returns the total number of threads created and also started
     * since the Java virtual machine started.
     *
     * @return the total number of threads started.
     */
    public static long totalStartedThreadCount() {
        initThreadMBean();
        return threadMBean.getTotalStartedThreadCount();
    }

    /**
     * Returns the current number of live daemon threads.
     *
     * @return the current number of live daemon threads.
     */
    public static long daemonThreadCount() {
        initThreadMBean();
        return threadMBean.getDaemonThreadCount();
    }

    /**
     * Returns the total CPU time for the current thread in nanoseconds.
     * The returned value is of nanoseconds precision but
     * not necessarily nanoseconds accuracy.
     * If the implementation distinguishes between user mode time and system
     * mode time, the returned CPU time is the amount of time that
     * the current thread has executed in user mode or system mode.
     */
    public static long currentThreadCpuTime() {
        initThreadMBean();
        threadMBean.setThreadCpuTimeEnabled(true);
        return threadMBean.getCurrentThreadCpuTime();
    }

    /**
     * Returns the CPU time that the current thread has executed
     * in user mode in nanoseconds.
     * The returned value is of nanoseconds precision but
     * not necessarily nanoseconds accuracy.
     */
    public static long currentThreadUserTime() {
        initThreadMBean();
        threadMBean.setThreadCpuTimeEnabled(true);
        return threadMBean.getCurrentThreadUserTime();
    }

    // stack trace functions
    private static String stackTraceAllStr(int numFrames, boolean printWarning) {
        Set<Map.Entry<Thread, StackTraceElement[]>> traces =
                Thread.getAllStackTraces().entrySet();
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<Thread, StackTraceElement[]> t : traces) {
            buf.append(t.getKey().toString());
            buf.append(LINE_SEPARATOR);
            buf.append(LINE_SEPARATOR);
            StackTraceElement[] st = t.getValue();
            buf.append(stackTraceStr("\t", st, 0, numFrames, printWarning));
            buf.append(LINE_SEPARATOR);
        }
        return buf.toString();
    }

    private static String stackTraceAllStr(int numFrames) {
        return stackTraceAllStr(numFrames, false);
    }

    private static void stackTraceAll(int numFrames) {
        print(stackTraceAllStr(numFrames, true));
    }

    private static String stackTraceStr(StackTraceElement[] st,
            int start, int numFrames) {
        return stackTraceStr(null, st, start, numFrames, false);
    }

    private static String stackTraceStr(String prefix, StackTraceElement[] st,
            int start, int numFrames) {
        return stackTraceStr(prefix, st, start, numFrames, false);
    }

    private static String stackTraceStr(String prefix, StackTraceElement[] st,
            int start, int numFrames, boolean printWarning) {
        start = start > 0 ? start : 0;
        numFrames = numFrames > 0 ? numFrames : st.length - start;

        int limit = start + numFrames;
        limit = limit <= st.length ? limit : st.length;

        if (prefix == null) {
            prefix = "";
        }

        StringBuilder buf = new StringBuilder();
        for (int i = start; i < limit; i++) {
            if (prefix != null) {
                buf.append(prefix);
            }
            buf.append(st[i].toString());
            buf.append(LINE_SEPARATOR);
        }
        if (printWarning && limit < st.length) {
            if (prefix != null) {
                buf.append(prefix);
            }
            buf.append(st.length - limit);
            buf.append(" more frame(s) ...");
            buf.append(LINE_SEPARATOR);
        }
        return buf.toString();
    }

    private static void stackTrace(StackTraceElement[] st,
            int start, int numFrames) {
        stackTrace(null, st, start, numFrames);
    }

    private static void stackTrace(String prefix, StackTraceElement[] st,
            int start, int numFrames) {
        print(stackTraceStr(prefix, st, start, numFrames, true), false);
    }

    private static ThreadMXBean getThreadMBean() {
        try {
            return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<ThreadMXBean>() {

                        public ThreadMXBean run() throws Exception {
                            return ManagementFactory.getThreadMXBean();
                        }
                    });
        } catch (Exception exp) {
            throw new UnsupportedOperationException(exp);
        }
    }

    private static void initThreadMBean() {
        if (threadMBean == null) {
            synchronized (Threads.class) {
                if (threadMBean == null) {
                    threadMBean = getThreadMBean();
                }
            }
        }
    }
}
