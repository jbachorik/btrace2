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

import net.java.btrace.BTraceRuntime;
import net.java.btrace.org.objectweb.asm.AnnotationVisitor;
import net.java.btrace.org.objectweb.asm.ClassVisitor;
import net.java.btrace.org.objectweb.asm.MethodVisitor;
import net.java.btrace.org.objectweb.asm.Opcodes;
import net.java.btrace.org.objectweb.asm.Type;
import net.java.btrace.api.extensions.BTraceExtension;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ExtensionRuntimeProcessor extends ClassVisitor {
    private static final String BTRACEEXTENSION_DESC = Type.getDescriptor(BTraceExtension.class);
    private static final String BTRACERUNTIME_TYPE = Type.getInternalName(BTraceRuntime.class);
    private static final String CONTEXT_TYPE = Type.getInternalName(net.java.btrace.api.extensions.Runtime.class);
    private static final String SEND_METHOD = "send";
    private static final String GETCMDFACTORY_METHOD = "getCommandFactory";
    private static final String IDENTITYSTR_METHOD = "identityStr";
    private static final String IDENTITYHASH_METHOD = "identityHashCode";
    private static final String SIZEOF_METHOD = "sizeof";
    private static final String HASH_METHOD = "hash";
    private static final String EQUALS_METHOD = "equals";
    private static final String GETPERFREADER_METHOD = "getPerfReader";
    private static final String GETFILEPATH_METHOD = "getFilePath";
    private static final String TRANSLATE_METHOD = "translate";
    private static final String THROWEXCEPTION_METHOD = "handleException";
    private static final String ALLARGS_METHOD = "$$";
    private static final String ARGACCESS_METHOD = "$";
    private static final String ARGLEN_METHOD = "$length";
    private static final String EXIT_METHOD = "exit";
    private static final String GETMEMORYBEAN_METHOD = "getMemoryBean";
    private static final String GETRUNTIMEBEAN_METHOD = "getRuntimeBean";
    private static final String GETHOTSPOTBEAN_METHOD = "getHotSpotBean";
    private static final String GETGCBEANS_METHOD = "getGarbageCollectionMBeans";
    private static final String GETMPOOLBEANS_METHOD = "getMemoryPoolMXBeans";
    private static final String DTRACEPROBE_METHOD = "dtraceProbe";
    

    private boolean isExtension = false;
    public ExtensionRuntimeProcessor(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    public boolean isApplied() {
        return isExtension;
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String type, boolean visible) {
        if (BTRACEEXTENSION_DESC.equals(type)) {
            isExtension = true;
        }
        return super.visitAnnotation(type, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String sig, String desc, String[] throwables) {
        MethodVisitor mv = super.visitMethod(access, name, sig, desc, throwables);
        return isExtension ? new MethodVisitor(Opcodes.ASM4, mv) {

            @Override
            public void visitMethodInsn(int opcode, String className, String methodName, String sig) {
                if ((opcode == Opcodes.INVOKEVIRTUAL ||
                     opcode == Opcodes.INVOKEINTERFACE) && CONTEXT_TYPE.equals(className)) {
                    if (IDENTITYSTR_METHOD.equals(methodName) || 
                           IDENTITYHASH_METHOD.equals(methodName) || 
                           SIZEOF_METHOD.equals(methodName) ||
                           HASH_METHOD.equals(methodName) ||
                           GETFILEPATH_METHOD.equals(methodName) ||
                           TRANSLATE_METHOD.equals(methodName) ||
                           ALLARGS_METHOD.equals(methodName) ||
                           ARGACCESS_METHOD.equals(methodName) ||
                           EQUALS_METHOD.equals(methodName) ||
                           GETCMDFACTORY_METHOD.equals(methodName) ||
                           GETPERFREADER_METHOD.equals(methodName) ||
                           ARGLEN_METHOD.equals(methodName) ||
                           GETMEMORYBEAN_METHOD.equals(methodName) ||
                           GETHOTSPOTBEAN_METHOD.equals(methodName) ||
                           GETGCBEANS_METHOD.equals(methodName) ||
                           GETMPOOLBEANS_METHOD.equals(methodName) ||
                           GETRUNTIMEBEAN_METHOD.equals(methodName) ||
                           DTRACEPROBE_METHOD.equals(methodName)) {
                        visitMethodInsn(Opcodes.INVOKESTATIC, BTRACERUNTIME_TYPE, methodName, sig);
                        swapPop();
                    } else if (SEND_METHOD.equals(methodName) ||
                               THROWEXCEPTION_METHOD.equals(methodName) ||
                               EXIT_METHOD.equals(methodName)) {
                        visitMethodInsn(Opcodes.INVOKESTATIC, BTRACERUNTIME_TYPE, methodName, sig);
                        visitInsn(Opcodes.POP);
                    } else {
                        super.visitMethodInsn(opcode, className, methodName, sig);
                    }
                } else {
                    super.visitMethodInsn(opcode, className, methodName, sig);
                }
            }
            
            private void swapPop() {
                visitInsn(Opcodes.SWAP); // put *this* on top
                visitInsn(Opcodes.POP); // remove unused *this*
            }
           
        } : mv;
    }
}
