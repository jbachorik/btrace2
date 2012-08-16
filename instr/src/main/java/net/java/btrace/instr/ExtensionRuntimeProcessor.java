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
package net.java.btrace.instr;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import net.java.btrace.org.objectweb.asm.AnnotationVisitor;
import net.java.btrace.org.objectweb.asm.ClassVisitor;
import net.java.btrace.org.objectweb.asm.MethodVisitor;
import net.java.btrace.org.objectweb.asm.Opcodes;
import net.java.btrace.org.objectweb.asm.Type;
import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.org.objectweb.asm.FieldVisitor;
import net.java.btrace.org.objectweb.asm.commons.StaticInitMerger;
import net.java.btrace.runtime.BTraceRuntimeBridge;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ExtensionRuntimeProcessor extends StaticInitMerger {
    private static final String CLINIT = "<clinit>";
    private static final String INIT = "<init>";
    
    private static final String BTRACEEXTENSION_DESC = Type.getDescriptor(BTraceExtension.class);
    private static final String RESOURCE_DESC = Type.getDescriptor(Resource.class);

    private boolean isExtension = false;
    public ExtensionRuntimeProcessor(ClassVisitor cv) {
        super("bclinit", cv);
    }

    public boolean isApplied() {
        return isExtension;
    }

    abstract private static class InitializerBlock {
        abstract protected void apply(MethodVisitor mv);
    }
    
    private List<InitializerBlock> initBlocks = new LinkedList<InitializerBlock>();
    private String ownerName;
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        ownerName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String type, boolean visible) {
        if (BTRACEEXTENSION_DESC.equals(type)) {
            isExtension = true;
        }
        return super.visitAnnotation(type, visible);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, String signature, Object value) {
        FieldVisitor fv = super.visitField(access, name, desc, signature, value);
        if (!isExtension) return fv;
        
        return new FieldVisitor(Opcodes.ASM4, fv) {
            @Override
            public AnnotationVisitor visitAnnotation(String annType, boolean visible) {
                if (visible && RESOURCE_DESC.equals(annType)) {
                    initBlocks.add(new InitializerBlock() {
                        @Override
                        protected void apply(MethodVisitor mv) {
                            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(BTraceRuntimeBridge.class), "getInstance", "()" + Type.getDescriptor(BTraceRuntimeBridge.class));
                            // (stack): BTraceRuntimeBridge#top
                            mv.visitFieldInsn(Opcodes.PUTSTATIC, ownerName, name, desc);
                            // (stack): empty
                        }
                    });
                }
                return super.visitAnnotation(annType, visible);
            }
            
        };
    }

    @Override
    public void visitEnd() {
        if (isExtension) {
            MethodVisitor mv = visitMethod(Opcodes.ACC_STATIC, CLINIT, "()V", null, null);
            mv.visitCode();
            for(InitializerBlock ib : initBlocks) {
                ib.apply(mv);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(Type.getType(BTraceRuntimeBridge.class).getSize(), 0);
            mv.visitEnd();
        }
        super.visitEnd();
    }
}
