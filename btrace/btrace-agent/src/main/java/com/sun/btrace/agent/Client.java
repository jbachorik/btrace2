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

package com.sun.btrace.agent;

import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.List;
import com.sun.btrace.org.objectweb.asm.ClassReader;
import com.sun.btrace.org.objectweb.asm.ClassWriter;
import com.sun.btrace.org.objectweb.asm.ClassVisitor;
import com.sun.btrace.BTraceRuntime;
import com.sun.btrace.CommandListener;
import com.sun.btrace.comm.ErrorCommand;
import com.sun.btrace.comm.ExitCommand;
import com.sun.btrace.comm.InstrumentCommand;
import com.sun.btrace.comm.OkayCommand;
import com.sun.btrace.comm.RenameCommand;
import com.sun.btrace.PerfReader;
import com.sun.btrace.comm.RetransformClassNotification;
import com.sun.btrace.comm.RetransformationStartNotification;
import com.sun.btrace.runtime.ClassFilter;
import com.sun.btrace.runtime.ClassRenamer;
import com.sun.btrace.runtime.ClinitInjector;
import com.sun.btrace.runtime.Instrumentor;
import com.sun.btrace.runtime.InstrumentUtils;
import com.sun.btrace.runtime.MethodRemover;
import com.sun.btrace.runtime.NullPerfReaderImpl;
import com.sun.btrace.runtime.Preprocessor;
import com.sun.btrace.runtime.Verifier;
import com.sun.btrace.runtime.OnMethod;
import com.sun.btrace.runtime.OnProbe;
import com.sun.btrace.runtime.RunnableGeneratorImpl;
import com.sun.btrace.util.NullVisitor;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;

/**
 * Abstract class that represents a BTrace client
 * at the BTrace agent.
 *
 * @author A. Sundararajan
 */
abstract class Client implements ClassFileTransformer, CommandListener {
    protected final Instrumentation inst;
    private volatile BTraceRuntime runtime;
    private volatile String className;
    private volatile Class btraceClazz;
    private volatile byte[] btraceCode;
//    private volatile List<WeakReference<Class<?>>> classes = new ArrayList<WeakReference<Class<?>>>();
    private volatile List<OnMethod> onMethods;
    private volatile List<OnProbe> onProbes;
    private volatile ClassFilter filter;
    private volatile boolean skipRetransforms;
    private volatile boolean hasSubclassChecks;
    protected final boolean debug = Main.isDebug();
    protected final boolean trackRetransforms = Main.isRetransformTracking();

    static {
        ClassFilter.class.getClass();
        InstrumentUtils.class.getClass();
        Instrumentor.class.getClass();
        ClassReader.class.getClass();
        ClassWriter.class.getClass();

        BTraceRuntime.init(createPerfReaderImpl(), new RunnableGeneratorImpl());
    }

    private static PerfReader createPerfReaderImpl() {
        // see if we can access any jvmstat class
        try {
            Class.forName("sun.jvmstat.monitor.MonitoredHost"); // NOI18N
            return (PerfReader) Class.forName("com.sun.btrace.runtime.PerfReaderImpl").newInstance(); // NOI18N
        } catch (Exception exp) {
            // no luck, create null implementation
            return new NullPerfReaderImpl();
        }
    }

    final private ClassFileTransformer clInitTransformer = new ClassFileTransformer() {

        @Override
        public byte[] transform(ClassLoader loader, final String cname, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (!hasSubclassChecks || classBeingRedefined != null || isBTraceClass(cname) || isSensitiveClass(cname)) return null;
            
            if (!skipRetransforms) {
                if (debug) Main.debugPrint("injecting <clinit> for " + cname); // NOI18N
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
                ClinitInjector injector = new ClinitInjector(cw, className, cname);
                InstrumentUtils.accept(cr, injector);
                if (injector.isTransformed()) {
                    byte[] instrumentedCode = cw.toByteArray();
                    Main.dumpClass(className, cname + "_clinit", instrumentedCode); // NOI18N
                    return instrumentedCode;
                }
            } else {
                if (debug) Main.debugPrint("client " + className + ": skipping transform for " + cname); // NOI18N
            }
            return null;
        }
    };
    
    Client(Instrumentation inst) {
        this.inst = inst;
    }

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
                if (debug) Main.debugPrint("skipping transform for BTrace class " + cname); // NOI18N
                return null;
            }
            if (!skipRetransforms) {
                if (classBeingRedefined != null) {
                    if (filter.isCandidate(classBeingRedefined)) {
                        return doTransform(classBeingRedefined, cname, classfileBuffer);
                    } else {
                        if (debug) Main.debugPrint("client " + className + ": skipping transform for " + cname); // NOi18N
                    }
                } else if (!hasSubclassChecks) {
                    if (filter.isCandidate(classfileBuffer)) {
                        return doTransform(classBeingRedefined, cname, classfileBuffer);
                    } else {
                        if (debug) Main.debugPrint("client " + className + ": skipping transform for " + cname); // NOI18N
                    }
                }
            } else {
                if (debug) Main.debugPrint("client " + className + ": skipping transform for " + cname); // NOI18N
                return null;
            }
            return null; // ignore
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof IllegalClassFormatException) {
                throw (IllegalClassFormatException)e;
            }
            return null;
        } finally {
            if (entered) {
                BTraceRuntime.leave();
            }
        }
    }
    
    void registerTransformer() {
        inst.addTransformer(clInitTransformer, false);
        inst.addTransformer(this, true);
    }
    
    void unregisterTransformer() {
        inst.removeTransformer(this);
        inst.removeTransformer(clInitTransformer);
    }

    private byte[] doTransform(Class<?> classBeingRedefined, String cname, byte[] classfileBuffer) {
        if (debug) Main.debugPrint("client " + className + ": instrumenting " + cname); // NOI18N
        if (trackRetransforms) {
            this.runtime.send(new RetransformClassNotification(cname));
        }
//        classes.add(new WeakReference<Class<?>>(classBeingRedefined));
        return instrument(classBeingRedefined, cname, classfileBuffer);
    }

    protected synchronized void onExit(int exitCode) {
        if (shouldAddTransformer()) {
            if (debug) Main.debugPrint("onExit: removing transformer for " + className); // NOI18N
            unregisterTransformer();
//            Set<Class<?>> retransforming = new HashSet<Class<?>>(classes.size());
//            for(WeakReference<Class<?>> clzRef : classes) {
//                Class clz = clzRef.get();
//                if (clz != null) {
//                    retransforming.add(clz);
//                }
//            }
//            try {
//                if (debug) Main.debugPrint("onExit: removing instrumentation"); // NOI18N
//                inst.retransformClasses(retransforming.toArray(new Class[retransforming.size()]));
//            } catch (UnmodifiableClassException e) {
//                Main.debugPrint(e);
//            }
        }
        try {
            if (debug) Main.debugPrint("onExit: closing all"); // NOI18N
            Thread.sleep(300);
            closeAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException ioexp) {
            if (debug) Main.debugPrint(ioexp);
        } finally {
            this.runtime = null;
            this.btraceClazz = null;
        }
    }

    protected Class loadClass(InstrumentCommand instr) throws IOException {
        String[] args = instr.getArguments();
        this.btraceCode = instr.getCode();
        try {
            verify(btraceCode);
        } catch (Throwable th) {
            if (debug) Main.debugPrint(th);
            errorExit(th);
            return null;
        }

        this.filter = new ClassFilter(onMethods);
        if (debug) Main.debugPrint("created class filter"); // NOI18N

        ClassWriter writer = InstrumentUtils.newClassWriter(btraceCode);
        ClassReader reader = new ClassReader(btraceCode);
        ClassVisitor visitor = new Preprocessor(writer);
        Main.dumpClass(className + "_orig", className + "_orig", btraceCode); // NOI18N
        if (BTraceRuntime.classNameExists(className)) {
            className += "$" + getCount(); // NOI18N
            if (debug) Main.debugPrint("class renamed to " + className); // NOI18N
            onCommand(new RenameCommand(className));
            visitor = new ClassRenamer(className, visitor);
        }
        try {
            if (debug) Main.debugPrint("preprocessing BTrace class " + className); // NOI18N
            InstrumentUtils.accept(reader, visitor);
            if (debug) Main.debugPrint("preprocessed BTrace class " + className); // NOI18N
            btraceCode = writer.toByteArray();
        } catch (Throwable th) {
            if (debug) Main.debugPrint(th);
            errorExit(th);
            return null;
        }
        Main.dumpClass(className + "_proc", className + "_proc", btraceCode); // NOI18N
        if (debug) Main.debugPrint("creating BTraceRuntime instance for " + className); // NOI18N
        this.runtime = new BTraceRuntime(className, args, this, inst);
        if (debug) Main.debugPrint("created BTraceRuntime instance for " + className); // NOI18N
        if (debug) Main.debugPrint("removing @OnMethod, @OnProbe methods"); // NOI18N
        byte[] codeBuf = removeMethods(btraceCode);
        Main.dumpClass(className, className, codeBuf);
        if (debug) Main.debugPrint("removed @OnMethod, @OnProbe methods"); // NOI18N
        if (debug) Main.debugPrint("sending Okay command"); // NOI18N
        runtime.send(new OkayCommand());
        // This extra BTraceRuntime.enter is needed to
        // check whether we have already entered before.
        boolean enteredHere = BTraceRuntime.enter();
        try {
            // The trace class static initializer needs to be run
            // without BTraceRuntime.enter(). Please look at the
            // static initializer code of trace class.
            BTraceRuntime.leave();
            if (debug) Main.debugPrint("about to defineClass " + className); // NOI18N
            if (shouldAddTransformer()) {
                this.btraceClazz = runtime.defineClass(codeBuf);
            } else {
                this.btraceClazz = runtime.defineClass(codeBuf, false);
            }
            if (debug) Main.debugPrint("defineClass succeeded for " + className); // NOI18N
        } catch (Throwable th) {
            if (debug) Main.debugPrint(th);
            errorExit(th);
            return null;
        } finally {
            // leave BTraceRuntime enter state as it was before
            // we started executing this method.
            if (! enteredHere) BTraceRuntime.enter();
        }
        return this.btraceClazz;
    }

    protected abstract void closeAll() throws IOException;

    protected void errorExit(Throwable th) throws IOException {
        if (debug) Main.debugPrint("sending error command"); // NOI18N
        onCommand(new ErrorCommand(th));
        if (debug) Main.debugPrint("sending exit command"); // NOI18N
        onCommand(new ExitCommand(1));
        closeAll();
    }

    // package privates below this point
    final BTraceRuntime getRuntime() {
        return runtime;
    }

    final String getClassName() {
        return className;
    }

    final Class getBTraceClass() {
        return btraceClazz;
    }

    final boolean isCandidate(Class c) {
        String cname = c.getName().replace('.', '/'); // NOI18N
        if (c.isInterface() || c.isPrimitive() || c.isArray()) {
            return false;
        }
        if (isBTraceClass(cname)) {
            return false;
        } else {
            return filter.isCandidate(c);
        }
    }

    final boolean shouldAddTransformer() {
        return onMethods != null && onMethods.size() > 0;
    }

    final void skipRetransforms() {
        skipRetransforms = true;
    }

    final void startRetransformClasses(int numClasses) {
        runtime.send(new RetransformationStartNotification(numClasses));
        if (Main.isDebug()) Main.debugPrint("calling retransformClasses (" + numClasses + " classes to be retransformed)"); // NOI18N
    }

    // Internals only below this point
    private static boolean isBTraceClass(String name) {
        return name.startsWith("com/sun/btrace/"); // NOI18N
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
        return name.equals("java/lang/Object") || // NOI18N
               name.startsWith("java/lang/ThreadLocal") || // NOI18N
               name.startsWith("sun/reflect") || // NOI18N
               name.equals("sun/misc/Unsafe")  || // NOI18N
               name.startsWith("sun/security/") || // NOI18N
               name.equals("java/lang/VerifyError"); // NOI18N
    }

    private byte[] instrument(Class clazz, String cname, byte[] target) {
        byte[] instrumentedCode;
        try {
            ClassWriter writer = InstrumentUtils.newClassWriter(target);
            ClassReader reader = new ClassReader(target);
            Instrumentor i = new Instrumentor(clazz, className,  btraceCode, onMethods, writer);
            InstrumentUtils.accept(reader, i);
            if (Main.isDebug() && !i.hasMatch()) {
                Main.debugPrint("*WARNING* No method was matched for class " + cname); // NOI18N
            }
            instrumentedCode = writer.toByteArray();
        } catch (Throwable th) {
            Main.debugPrint(th);
            return null;
        }
        Main.dumpClass(className, cname, instrumentedCode);
        return instrumentedCode;
    }

    private void verify(byte[] buf) {
        ClassReader reader = new ClassReader(buf);
        Verifier verifier = new Verifier(new NullVisitor(), Main.isUnsafe());
        if (debug) Main.debugPrint("verifying BTrace class"); // NOI18N
        InstrumentUtils.accept(reader, verifier);
        className = verifier.getClassName().replace('/', '.');
        if (debug) Main.debugPrint("verified '" + className + "' successfully"); // NOI18N
        onMethods = verifier.getOnMethods();
        onProbes = verifier.getOnProbes();
        if (onProbes != null && !onProbes.isEmpty()) {
            // map @OnProbe's to @OnMethod's and store
            onMethods.addAll(Main.mapOnProbes(onProbes));
        }
        for(OnMethod om : onMethods) {
            if (om.getClazz().startsWith("+")) {
                hasSubclassChecks = true;
                break;
            }
        }
    }

    private static byte[] removeMethods(byte[] buf) {
        ClassWriter writer = InstrumentUtils.newClassWriter(buf);
        ClassReader reader = new ClassReader(buf);
        InstrumentUtils.accept(reader, new MethodRemover(writer));
        return writer.toByteArray();
    }

    private static long count = 0L;
    private static long getCount() {
        return count++;
    }
}
