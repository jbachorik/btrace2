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

import net.java.btrace.api.server.Session;
import net.java.btrace.runtime.BTraceRuntime;
import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.api.wireio.Response;
import net.java.btrace.instr.ClassFilter;
import net.java.btrace.instr.ClassRenamer;
import net.java.btrace.instr.ClinitInjector;
import net.java.btrace.instr.InstrumentUtils;
import net.java.btrace.instr.Instrumentor;
import net.java.btrace.instr.MethodRemover;
import net.java.btrace.instr.OnMethod;
import net.java.btrace.instr.OnProbe;
import net.java.btrace.instr.Preprocessor;
import net.java.btrace.instr.Verifier;
import net.java.btrace.org.objectweb.asm.ClassReader;
import net.java.btrace.org.objectweb.asm.ClassVisitor;
import net.java.btrace.org.objectweb.asm.ClassWriter;
import net.java.btrace.org.objectweb.asm.Opcodes;
import net.java.btrace.api.wireio.Channel;
import net.java.btrace.api.server.Session.State;
import net.java.btrace.wireio.commands.ErrorCommand;
import net.java.btrace.wireio.commands.ExitCommand;
import net.java.btrace.wireio.commands.RetransformClassNotification;
import java.io.EOFException;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.server.ShutdownHandler;
import net.java.btrace.instr.ProbeDescriptor;
import net.java.btrace.util.BTraceThreadFactory;

/**
 *
 * @author Jaroslav Bachorik
 */
final public class SessionImpl extends Session implements ShutdownHandler {

    final private static ExecutorService handlerPool = Executors.newCachedThreadPool(new BTraceThreadFactory());
    private Future<?> cmdHandler;

    private AtomicReference<State> state = new AtomicReference<State>(State.DISCONNECTED);
    private Lookup lookup = new Lookup();

    private String className;
    private volatile List<OnMethod> onMethods;
    private volatile List<OnProbe> onProbes;
    private volatile boolean hasSubclassChecks;
    private volatile ClassFilter filter;
    private volatile boolean skipRetransforms;
    private volatile boolean trackRetransforms;
    private volatile byte[] btraceCode;
    private BTraceRuntime runtime;
    private Class btraceClazz;
    final private Set<String> instrumentedClasses = new HashSet<String>();
    final private ClassFileTransformer clInitTransformer = new ClassFileTransformer() {

        @Override
        public byte[] transform(ClassLoader loader, final String cname, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (!hasSubclassChecks || classBeingRedefined != null || isBTraceClass(cname) || isSensitiveClass(cname)) {
                return null;
            }

            if (!skipRetransforms) {
                BTraceLogger.debugPrint("injecting <clinit> for " + cname); // NOI18N
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
                ClinitInjector injector = new ClinitInjector(cw, className, cname);
                InstrumentUtils.accept(cr, injector);
                if (injector.isTransformed()) {
                    byte[] instrumentedCode = cw.toByteArray();
                    BTraceLogger.dumpClass(cname + "_clinit", instrumentedCode); // NOI18N
                    return instrumentedCode;
                }
            } else {
                BTraceLogger.debugPrint("client " + className + ": skipping transform for " + cname); // NOI18N
            }
            return null;
        }
    };
    final private ClassFileTransformer traceTransformer = new ClassFileTransformer() {

        @Override
        public byte[] transform(
                ClassLoader loader,
                final String cname,
                Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[] classfileBuffer)
                throws IllegalClassFormatException {
            boolean entered = BTraceRuntime.enter();
            try {
                if (isBTraceClass(cname) || isSensitiveClass(cname)) {
                    BTraceLogger.debugPrint("skipping transform for BTrace class " + cname); // NOI18N
                    return null;
                }

                if (classBeingRedefined != null) {
                    // class already defined; retransforming
                    if (!skipRetransforms && filter.isCandidate(classBeingRedefined)) {
                        return doTransform(classBeingRedefined, cname, classfileBuffer);
                    } else {
                        BTraceLogger.debugPrint("client " + className + ": skipping transform for " + cname); // NOi18N
                    }
                } else {
                    // class not yet defined
                    if (!hasSubclassChecks) {
                        if (filter.isCandidate(classfileBuffer)) {
                            return doTransform(classBeingRedefined, cname, classfileBuffer);
                        } else {
                            BTraceLogger.debugPrint("client " + className + ": skipping transform for " + cname); // NOI18N
                        }
                    }
                }

                return null; // ignore
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof IllegalClassFormatException) {
                    throw (IllegalClassFormatException) e;
                }
                return null;
            } finally {
                if (entered) {
                    BTraceRuntime.leave();
                }
            }
        }
    };

    SessionImpl(Object... ctx) throws IOException {
        lookup.add(this);
        lookup.add(ctx);
    }

    State getState() {
        return state.get();
    }

    @Override
    public void start() {
        startCommandHandler();
    }

    @Override
    public void event(String name) {
        runtime.handleEvent(name);
    }

    @Override
    public boolean loadTraceClass(byte[] traceCode, String[] args) {
        Throwable capturedError = null;
        try {
            try {
                verify(traceCode);
            } catch (Throwable th) {
                capturedError = th;
                return false;
            }
            SessionImpl.this.filter = new ClassFilter(onMethods);
            BTraceLogger.debugPrint("created class filter"); // NOI18N
            ClassWriter writer = InstrumentUtils.newClassWriter(traceCode);
            ClassReader reader = new ClassReader(traceCode);
            ClassVisitor visitor = new Preprocessor(writer);
            String traceName = BTraceRuntime.getValidTraceClassName(className);
            BTraceLogger.dumpClass(traceName + "_orig", traceCode); // NOI18N
            if (!traceName.equals(className)) {
                BTraceLogger.debugPrint("class " + className + " renamed to " + traceName); // NOI18N
                // FIXME
                //            onCommand(new RenameCommand(className));
                visitor = new ClassRenamer(traceName, visitor);
            }
            className = traceName;
            try {
                BTraceLogger.debugPrint("preprocessing BTrace class " + className); // NOI18N
                InstrumentUtils.accept(reader, visitor);
                BTraceLogger.debugPrint("preprocessed BTrace class " + className); // NOI18N
                traceCode = writer.toByteArray();
            } catch (Throwable th) {
                capturedError = th;
                return false;
            }

            Instrumentation instr = getInstrumentation();

            BTraceLogger.dumpClass(className + "_proc", traceCode); // NOI18N
            SessionImpl.this.btraceCode = traceCode;
            BTraceLogger.debugPrint("creating BTraceRuntime instance for " + className); // NOI18N
            SessionImpl.this.runtime = new BTraceRuntime(this, className, args, getChannel(), instr, lookup.lookup(ExtensionsRepository.class));
            BTraceLogger.debugPrint("created BTraceRuntime instance for " + className); // NOI18N
            BTraceLogger.debugPrint("removing @OnMethod, @OnProbe methods"); // NOI18N
            byte[] codeBuf = removeMethods(traceCode);
            BTraceLogger.dumpClass(traceName, codeBuf);
            BTraceLogger.debugPrint("removed @OnMethod, @OnProbe methods"); // NOI18N
            // This extra BTraceRuntime.enter is needed to
            // check whether we have already entered before.
            boolean enteredHere = BTraceRuntime.enter();
            try {
                // The trace class static initializer needs to be run
                // without BTraceRuntime.enter(). Please look at the
                // static initializer code of trace class.
                BTraceRuntime.leave();
                BTraceLogger.debugPrint("about to defineClass " + className); // NOI18N
                if (shouldAddTransformer()) {
                    SessionImpl.this.btraceClazz = runtime.defineClass(codeBuf);
                } else {
                    SessionImpl.this.btraceClazz = runtime.defineClass(codeBuf, false);
                }
                BTraceLogger.debugPrint("defineClass succeeded for " + className); // NOI18N
            } catch (Throwable th) {
                capturedError = th;
                return false;
            } finally {
                // leave BTraceRuntime enter state as it was before
                // we started executing this method.
                if (!enteredHere) {
                    BTraceRuntime.enter();
                }
            }
            if (btraceClazz != null) {
                if (shouldAddTransformer()) {
                    instr.addTransformer(traceTransformer, true);
                    instr.addTransformer(clInitTransformer, false);
                }
                List<Class> clzs = new ArrayList<Class>();
                for (Class clz : instr.getAllLoadedClasses()) {
                    if (instr.isModifiableClass(clz)) {
                        if (clz.getAnnotation(BTraceExtension.class) != null || filter.isCandidate(clz)) {
                            clzs.add(clz);
                        }
                    }
                }
                instr.retransformClasses(clzs.toArray(new Class[clzs.size()]));
            }
        } catch (UnmodifiableClassException e) {
            capturedError = e;
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (capturedError == null) {
                    return true;
                } else {
                    BTraceLogger.debugPrint(capturedError);
                    errorExit(capturedError);
                }
            } catch (IOException e) {
                // IGNORE
            }
        }
        return false;
    }

    @Override
    public void shutdown(final int exitCode) {
        if (setState(State.CONNECTED, State.DISCONNECTING)) {
            try {
                Response<Void> r = getChannel().sendCommand(ExitCommand.class, new AbstractCommand.Initializer<ExitCommand>() {

                    @Override
                    public void init(ExitCommand cmd) {
                        cmd.setExitCode(exitCode);
                    }
                });
                try {
                    // wait for response
                    r.get(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cleanup();
            } catch (IOException e) {
                BTraceLogger.debugPrint(e);
            } finally {
                getChannel().close();
                setState(State.DISCONNECTING, State.DISCONNECTED);
            }
        }
    }

    @Override
    public boolean detach(Runnable detachHook) {
        if (setState(State.CONNECTED, State.DISCONNECTING)) {
            if (detachHook != null) {
                detachHook.run();
            }
            cleanup();
            BTraceLogger.debugPrint("Session disconnected");
            return setState(State.DISCONNECTING, State.DISCONNECTED);
        }
        return false;
    }

    private void cleanup() {
        if (cmdHandler.cancel(true)) {
            Instrumentation instr = getInstrumentation();
            if (shouldAddTransformer()) {
                instr.removeTransformer(traceTransformer);
                instr.removeTransformer(clInitTransformer);
            }
            try {
                List<Class> toRetransform = new ArrayList<Class>();
                for (Class clz : instr.getAllLoadedClasses()) {
                    if (instrumentedClasses.contains(clz.getName())) {
                        toRetransform.add(clz);
                    }
                }
                if (!toRetransform.isEmpty()) {
                    instr.retransformClasses(toRetransform.toArray(new Class[toRetransform.size()]));
                }
            } catch (UnmodifiableClassException ex) {
                BTraceLogger.debugPrint(ex);
            } catch (InternalError e) {
                // nothing to do here; jvm is shutting down, class redefinition fails
            } catch (Throwable t) {
                BTraceLogger.debugPrint(t);
            }
            instrumentedClasses.clear();
            runtime.shutdown();
            runtime = null;
        }
    }

    private void setState(State newState) {
        State oldState = state.getAndSet(newState);
        setChanged();
        notifyObservers(oldState);
    }

    private boolean setState(State expState, State newState) {
        if (state.compareAndSet(expState, newState)) {
            setChanged();
            notifyObservers(expState);
            return true;
        }
        return false;
    }

    private void startCommandHandler() {
        setState(State.CONNECTED);
        cmdHandler = handlerPool.submit(new Runnable() {

            public void run() {
                Channel ch = getChannel();
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        BTraceLogger.debugPrint("SERVER: Reading command");
                        AbstractCommand cmd = ch.readCommand();
                        BTraceLogger.debugPrint("Command: " + cmd);
                        if (cmd != null) {
                            try {
                                cmd.execute(lookup);
                            } catch (Throwable t) {
                                BTraceLogger.debugPrint(t);
                            }
                        } else {
                            break;
                        }
                    } catch (ClassNotFoundException e) {
                        BTraceLogger.debugPrint(e);
                        detach();
                        ch.close();
                        break;
                    } catch (EOFException e) {
                        if (getState() == State.CONNECTED) {
                            detach();
                            ch.close();
                        }
                        break;
                    } catch (IOException e) {
                        BTraceLogger.debugPrint(e);
                        detach();
                        ch.close();
                        break;
                    }
                }
            }
        });
    }

    private void verify(byte[] buf) {
        ClassReader reader = new ClassReader(buf);
        Verifier verifier = new Verifier(new ClassVisitor(Opcodes.ASM4) {
        }, false, lookup.lookup(ExtensionsRepository.class));
        BTraceLogger.debugPrint("verifying BTrace class"); // NOI18N
        InstrumentUtils.accept(reader, verifier);
        className = verifier.getClassName().replace('/', '.');
        BTraceLogger.debugPrint("verified '" + className + "' successfully"); // NOI18N
        onMethods = verifier.getOnMethods();
        onProbes = verifier.getOnProbes();
        if (onProbes != null && !onProbes.isEmpty()) {
            // map @OnProbe's to @OnMethod's and store
            onMethods.addAll(mapOnProbes(onProbes));
        }
        for (OnMethod om : onMethods) {
            if (om.getClazz().startsWith("+")) {
                hasSubclassChecks = true;
                break;
            }
        }
    }

    /**
     * Maps a list of @OnProbe's to a list @OnMethod's using probe descriptor
     * XML files.
     */
    private static List<OnMethod> mapOnProbes(List<OnProbe> onProbes) {
        List<OnMethod> res = new ArrayList<OnMethod>();
        for (OnProbe op : onProbes) {
            String ns = op.getNamespace();
            BTraceLogger.debugPrint("about to load probe descriptor for " + ns);

            // load probe descriptor for this namespace
            ProbeDescriptor probeDesc = ProbeDescriptorLoader.load(ns);
            if (probeDesc == null) {
                BTraceLogger.debugPrint("failed to find probe descriptor for " + ns);
                continue;
            }
            // find particular probe mappings using "local" name
            OnProbe foundProbe = probeDesc.findProbe(op.getName());
            if (foundProbe == null) {
                BTraceLogger.debugPrint("no probe mappings for " + op.getName());
                continue;
            }
            BTraceLogger.debugPrint("found probe mappings for " + op.getName());

            Collection<OnMethod> omColl = foundProbe.getOnMethods();
            for (OnMethod om : omColl) {
                // copy the info in a new OnMethod so that
                // we can set target method name and descriptor
                // Note that the probe descriptor cache is used
                // across BTrace sessions. So, we should not update
                // cached OnProbes (and their OnMethods).
                OnMethod omn = new OnMethod();
                omn.copyFrom(om);
                omn.setTargetName(op.getTargetName());
                omn.setTargetDescriptor(op.getTargetDescriptor());
                res.add(omn);
            }
        }
        return res;
    }

    private boolean shouldAddTransformer() {
        return onMethods != null && onMethods.size() > 0;
    }

    private static byte[] removeMethods(byte[] buf) {
        ClassWriter writer = InstrumentUtils.newClassWriter(buf);
        ClassReader reader = new ClassReader(buf);
        InstrumentUtils.accept(reader, new MethodRemover(writer));
        return writer.toByteArray();
    }

    private void errorExit(final Throwable th) throws IOException {
        BTraceLogger.debugPrint("sending error command"); // NOI18N
        getChannel().sendCommand(ErrorCommand.class, new AbstractCommand.Initializer<ErrorCommand>() {

            @Override
            public void init(ErrorCommand cmd) {
                cmd.setCause(th);
            }
        });

        BTraceLogger.debugPrint("sending exit command"); // NOI18N
        Response<Void> r = getChannel().sendCommand(ExitCommand.class, new AbstractCommand.Initializer<ExitCommand>() {

            @Override
            public void init(ExitCommand cmd) {
                cmd.setExitCode(1);
            }
        });
        try {
            r.get();
        } catch (InterruptedException e) {
            BTraceLogger.debugPrint(th);
            Thread.currentThread().interrupt();
        }
    }

    private static boolean isBTraceClass(String name) {
        return name != null ? name.startsWith("net/java/btrace") : false; // NOI18N
    }

    /*
     * Certain classes like java.lang.ThreadLocal and it's
     * inner classes, java.lang.Object cannot be safely
     * instrumented with BTrace. This is because BTrace uses
     * ThreadLocal class to check recursive entries due to
     * BTrace's own functions. But this leads to infinite recursions
     * if BTrace instruments java.lang.ThreadLocal for example.
     * For now, we avoid such classes till we find a solution.
     */
    private static boolean isSensitiveClass(String name) {
        return name == null ||
                name.equals("java/lang/Object") || // NOI18N
                name.startsWith("java/lang/ThreadLocal") || // NOI18N
                name.startsWith("sun/reflect") || // NOI18N
                name.equals("sun/misc/Unsafe") || // NOI18N
                name.startsWith("sun/security/") || // NOI18N
                name.equals("java/lang/VerifyError"); // NOI18N
    }

    private byte[] doTransform(Class<?> classBeingRedefined, final String cname, byte[] classfileBuffer) {
        BTraceLogger.debugPrint("client " + className + ": instrumenting " + cname); // NOI18N
        if (trackRetransforms) {
            try {
                getChannel().sendCommand(RetransformClassNotification.class, new AbstractCommand.Initializer<RetransformClassNotification>() {

                    @Override
                    public void init(RetransformClassNotification cmd) {
                        cmd.setClassName(cname);
                    }
                });
            } catch (IOException ex) {
                BTraceLogger.debugPrint(ex);
            }
        }
//        classes.add(new WeakReference<Class<?>>(classBeingRedefined));
        return instrument(classBeingRedefined, cname, classfileBuffer);
    }

    private byte[] instrument(Class clazz, String cname, byte[] target) {
        byte[] instrumentedCode;
        try {
            ClassWriter writer = InstrumentUtils.newClassWriter(target);
            ClassReader reader = new ClassReader(target);
            Instrumentor i = new Instrumentor(clazz, className, btraceCode, onMethods, writer);
            InstrumentUtils.accept(reader, i);
            if (!i.hasMatch()) {
                BTraceLogger.debugPrint("*WARNING* No method was matched for class " + cname); // NOI18N
            } else {
                instrumentedClasses.add(cname.replace('/', '.'));
            }
            instrumentedCode = writer.toByteArray();
        } catch (Throwable th) {
            BTraceLogger.debugPrint(th);
            return null;
        }
        BTraceLogger.dumpClass(cname, instrumentedCode);
        return instrumentedCode;
    }

    private Instrumentation getInstrumentation() {
        return lookup.lookup(Instrumentation.class);
    }

    private Channel getChannel() {
        return lookup.lookup(Channel.class);
    }
}
