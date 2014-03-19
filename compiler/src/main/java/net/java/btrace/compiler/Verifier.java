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

package net.java.btrace.compiler;

import net.java.btrace.api.extensions.util.CallTargetValidator;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import com.sun.source.tree.Tree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import com.sun.tools.javac.util.Context;

/**
 * An annotation processor that validates a BTrace program.
 * Safety rules (such as no loops, no new/throw etc.) are
 * enforced. This uses javac's Tree API in addition to JSR 269.
 *
 * @author A. Sundararajan
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Verifier extends AbstractProcessor
                            implements TaskListener {
    /**
     * @since 2.0
     */
    final static public String INLINED_INSTR_MARKER = "net/java/btrace/InlineMarker";
    /**
     * @since 2.0
     */
    final static public String INLINED_INSTR_START = "start";
    /**
     * @since 2.0
     */
    final static public String INLINED_INSTR_END = "end";
    
    private boolean unsafe;
    private List<String> classNames =
        new ArrayList<String>();
    private Trees treeUtils;
    private List<CompilationUnitTree> compUnits =
        new ArrayList<CompilationUnitTree>();
    private ClassTree currentClass;
    private final AttributionTaskListener listener = new AttributionTaskListener();

    private CallTargetValidator ctValidator;
    
    public Verifier(CallTargetValidator ctValidator, boolean unsafe) {
        this.unsafe = unsafe;
        this.ctValidator = ctValidator;
    }

    public Verifier(CallTargetValidator ctValidator) {
        this(ctValidator, false);
    }

    public void init(ProcessingEnvironment pe) {
        super.init(pe);
        treeUtils = Trees.instance(pe);
    }

    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        return true;
    }

    public void started(TaskEvent e) {
    }

    public void finished(TaskEvent e) {
        CompilationUnitTree ct = e.getCompilationUnit();
        if (ct != null) {
            compUnits.add(ct);
        }
        listener.finished(e);
    }

    List<String> getClassNames() {
        return classNames;
    }
    
    CallTargetValidator getCallTargetValidator() {
        return ctValidator;
    }

    CompilationUnitTree getCompilationUnit() {
        for (CompilationUnitTree ct : compUnits) {
            for (Tree clazz : ct.getTypeDecls()) {
                if (clazz.equals(currentClass)) {
                    return ct;
                }
            }
        }
        return null;
    }

    Trees getTreeUtils() {
        return treeUtils;
    }

    SourcePositions getSourcePositions() {
        return treeUtils.getSourcePositions();
    }

    ProcessingEnvironment getProcessingEnvironment() {
        return processingEnv;
    }

    Messager getMessager() {
        return processingEnv.getMessager();
    }

    Elements getElementUtils() {
        return processingEnv.getElementUtils();
    }

    Types getTypeUtils() {
        return processingEnv.getTypeUtils();
    }

    Locale getLocale() {
        return processingEnv.getLocale();
    }

    // verify each BTrace class
    private boolean verify(ClassTree ct, Element topElement) {
        currentClass = ct;
        CompilationUnitTree cut = getCompilationUnit();
        String className = ct.getSimpleName().toString();
        ExpressionTree pkgName = cut.getPackageName();
        if (pkgName != null) {
            className = pkgName + "." + className;
        }
        classNames.add(className);
        Boolean value = unsafe? Boolean.TRUE : 
            ct.accept(new VerifierVisitor(this, topElement), null);
        return value == null? true : value.booleanValue();
    }

    /**
     * A task listener that invokes the processor whenever a class is fully
     * analyzed.
     */
    private final class AttributionTaskListener implements TaskListener {

        @Override
        public void finished(TaskEvent e) {
            if (e.getKind() != TaskEvent.Kind.ANALYZE) return;
            TypeElement elem = e.getTypeElement();
            for(Tree t : e.getCompilationUnit().getTypeDecls()) {
                if (t.getKind() == Tree.Kind.CLASS) {
                    if (((JCClassDecl)t).sym.equals(elem)) {
                        currentClass = (ClassTree)t;
                        break;
                    }
                }
            }
            if (currentClass != null) {
                verify(currentClass, elem);
            }
        }

        @Override
        public void started(TaskEvent e) { }

    }

    /**
     * A task listener multiplexer.
     */
    private static class TaskListeners implements TaskListener {
        private final List<TaskListener> listeners = new ArrayList<TaskListener>();

        public void add(TaskListener listener) {
            listeners.add(listener);
        }

        public void remove(TaskListener listener) {
            listeners.remove(listener);
        }

        @Override
        public void finished(TaskEvent e) {
            for (TaskListener listener : listeners)
                listener.finished(e);
        }

        @Override
        public void started(TaskEvent e) {
            for (TaskListener listener : listeners)
                listener.started(e);
        }
    }
}
