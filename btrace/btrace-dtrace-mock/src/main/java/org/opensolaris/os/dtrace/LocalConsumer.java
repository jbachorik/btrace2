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
package org.opensolaris.os.dtrace;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * Interface to the native DTrace library, each instance is a single
 * DTrace consumer.
 *
 * @author Tom Erickson
 */
public class LocalConsumer implements Consumer {
    //
    // Implementation notes:
    //
    // libdtrace is *not* thread-safe.  You cannot make multiple calls
    // into it simultaneously from different threads, even if those
    // threads are operating on different dtrace_hdl_t's.  Calls to
    // libdtrace are synchronized on a global lock, LocalConsumer.class.

    static Logger logger = Logger.getLogger(LocalConsumer.class.getName());

    // Needs to match the version in dtrace_jni.c
    private static final int DTRACE_JNI_VERSION = 3;

    /**
     * Creates a consumer that interacts with the native DTrace library
     * on the local system.
     */
    public
    LocalConsumer()
    {
    }

    public synchronized void
    open(OpenFlag ... flags) throws DTraceException
    {
    }

    public synchronized Program
    compile(String program, String ... macroArgs) throws DTraceException
    {
	return null;
    }

    public synchronized Program
    compile(File program, String ... macroArgs) throws DTraceException,
            IOException, SecurityException
    {
	return null;
    }
    
    public void
    enable() throws DTraceException
    {
	enable(null);
    }

    public synchronized void
    enable(Program program) throws DTraceException
    {
    }

    public synchronized void
    getProgramInfo(Program program) throws DTraceException
    {
    }

    public void
    setOption(String option) throws DTraceException
    {
	
    }

    public void
    unsetOption(String option) throws DTraceException
    {
	
    }

    public synchronized void
    setOption(String option, String value) throws DTraceException
    {
	
    }

    public synchronized long
    getOption(String option) throws DTraceException
    {
	return -1L;
    }

    public final synchronized boolean
    isOpen()
    {
	return false;
    }

    public final synchronized boolean
    isEnabled()
    {
	return false;
    }

    public final synchronized boolean
    isRunning()
    {
	return false;
    }

    public final synchronized boolean
    isClosed()
    {
	return false;
    }

    /**
     * Called in the runnable target of the thread returned by {@link
     * #createThread()} to run this DTrace consumer.
     *
     * @see #createThread()
     */
    protected final void
    work()
    {
	
    }

    /**
     * Creates the background thread started by {@link #go()} to run
     * this consumer.  Override this method if you need to set
     * non-default {@code Thread} options or create the thread in a
     * {@code ThreadGroup}.  If you don't need to create the thread
     * yourself, set the desired options on {@code super.createThread()}
     * before returning it.  Otherwise, the {@code Runnable} target of
     * the created thread must call {@link #work()} in order to run this
     * DTrace consumer.  For example, to modify the default background
     * consumer thread:
     * <pre><code>
     *	protected Thread
     *	createThread()
     *	{
     *		Thread t = super.createThread();
     *		t.setPriority(Thread.MIN_PRIORITY);
     *		return t;
     *	}
     * </code></pre>
     * Or if you need to create your own thread:
     * <pre></code>
     *	protected Thread
     *	createThread()
     *	{
     *		Runnable target = new Runnable() {
     *			public void run() {
     *				work();
     *			}
     *		};
     *		String name = "Consumer " + UserApplication.sequence++;
     *		Thread t = new Thread(UserApplication.threadGroup,
     *			target, name);
     *		return t;
     *	}
     * </code></pre>
     * Do not start the returned thread, otherwise {@code go()} will
     * throw an {@link IllegalThreadStateException} when it tries to
     * start the returned thread a second time.
     */
    protected Thread
    createThread()
    {
	return null;
    }

    /**
     * @inheritDoc
     * @throws IllegalThreadStateException if a subclass calls {@link
     * Thread#start()} on the value of {@link #createThread()}
     * @see #createThread()
     */
    public void
    go() throws DTraceException
    {
	go(null);
    }

    /**
     * @inheritDoc
     * @throws IllegalThreadStateException if a subclass calls {@link
     * Thread#start()} on the value of {@link #createThread()}
     * @see #createThread()
     */
    public synchronized void
    go(ExceptionHandler h) throws DTraceException
    {
	
    }

    /**
     * @inheritDoc
     *
     * @throws IllegalThreadStateException if attempting to {@code
     * stop()} a running consumer while holding the lock on that
     * consumer
     */
    public void
    stop()
    {
	
    }

    public synchronized void
    abort()
    {
	
    }

    /**
     * @inheritDoc
     *
     * @throws IllegalThreadStateException if attempting to {@code
     * close()} a running consumer while holding the lock on that
     * consumer
     */
    public void
    close()
    {
	
    }

    public void
    addConsumerListener(ConsumerListener l)
    {
    }

    public void
    removeConsumerListener(ConsumerListener l)
    {
    }

    public Aggregate
    getAggregate() throws DTraceException
    {
	// include all, clear none
	return getAggregate(null, Collections. <String> emptySet());
    }

    public Aggregate
    getAggregate(Set <String> includedAggregationNames)
            throws DTraceException
    {
	return getAggregate(includedAggregationNames,
		Collections. <String> emptySet());
    }

    public Aggregate
    getAggregate(Set <String> includedAggregationNames,
	    Set <String> clearedAggregationNames)
            throws DTraceException
    {
	return null;
    }

    public synchronized int
    createProcess(String command) throws DTraceException
    {
	return -1;
    }

    public synchronized void
    grabProcess(int pid) throws DTraceException
    {
	
    }

    public synchronized List <ProbeDescription>
    listProbes(ProbeDescription filter) throws DTraceException
    {
	return Collections.EMPTY_LIST;
    }

    public synchronized List <Probe>
    listProbeDetail(ProbeDescription filter) throws DTraceException
    {
	return Collections.EMPTY_LIST;
    }

    public synchronized List <ProbeDescription>
    listProgramProbes(Program program) throws DTraceException
    {
	return Collections.EMPTY_LIST;
    }

    public synchronized List <Probe>
    listProgramProbeDetail(Program program) throws DTraceException
    {
	return Collections.EMPTY_LIST;
    }

    public synchronized String
    lookupKernelFunction(int address)
    {
	return "";
    }

    public synchronized String
    lookupKernelFunction(long address)
    {
	return "";
    }

    public synchronized String
    lookupUserFunction(int pid, int address)
    {
	return "";
    }

    public synchronized String
    lookupUserFunction(int pid, long address)
    {
	return "";
    }

    public String
    getVersion()
    {
	return "";
    }
}