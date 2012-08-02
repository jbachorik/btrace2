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

package net.java.btrace.util;

import net.java.btrace.org.objectweb.asm.MethodVisitor;
import static net.java.btrace.org.objectweb.asm.Opcodes.*;
import net.java.btrace.org.objectweb.asm.Type;
import net.java.btrace.instr.MethodInstrumentor;

/**
 *
 * @author Jaroslav Bachorik
 */
public class TimeStampGenerator extends MethodVisitor {
        final public static String TIME_STAMP_NAME = "$btrace$time$stamp";
    
    private static final String CONSTRUCTOR = "<init>";

    private int[] tsIndex;
    private int[] exitOpcodes;
    private boolean generatingIndex = false;
    private boolean entryCalled = false;

    private String methodName;
    private String className;
    private String superName;
    final private LocalVariablesSorter lvs;

    public TimeStampGenerator(LocalVariablesSorter lvs, final int[] tsIndex, String className, String superName, int access, String name, String desc, MethodVisitor mv, int[] exitOpcodes) {
        super(ASM4, mv);
        this.lvs = lvs;
        this.methodName = name;
        this.className = className;
        this.superName = superName;
        this.tsIndex = tsIndex;
        this.exitOpcodes = new int[exitOpcodes.length];
        System.arraycopy(exitOpcodes, 0, this.exitOpcodes, 0, exitOpcodes.length);
    }

    @Override
    public void visitCode() {
        entryCalled = false;
        
        if (!CONSTRUCTOR.equals(methodName)) {
            generateTS(0);
        }
        
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        if (tsIndex[1] == -1) {
            for(int exitOpcode : exitOpcodes) {
                if (exitOpcode == opcode) {
                    if (tsIndex[0] != -1 && tsIndex[1] == -1) generateTS(1);
                    break;
                }
            }
        }
        super.visitInsn(opcode);
        if (tsIndex[1] != -1) {
            switch (opcode) {
                case RETURN:
                case IRETURN:
                case FRETURN:
                case LRETURN:
                case DRETURN:
                case ARETURN:
                case ATHROW:
                    tsIndex[1] = -1; // reset the exit time stamp as it gets invalidated
            }
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (generatingIndex) {
            super.visitMethodInsn(opcode, owner, name, desc);
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc);
        if (!entryCalled && CONSTRUCTOR.equals(name) && (owner.equals(className) || (superName != null && owner.equals(superName)))) {
            entryCalled = true;
            generateTS(0);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }

    private void generateTS(int index) {
        if (tsIndex != null && tsIndex[index] != -1) return;
        
        if (!((MethodInstrumentor)mv).usesTimeStamp()) return; // the method instrumentor is not using timestamp; no need to generate time stamp collectors
        
        if (tsIndex[index] > -1) return;
        try {
            generatingIndex = true;
            TimeStampHelper.generateTimeStampAccess(this, className);
            tsIndex[index] = lvs.newLocal(Type.LONG_TYPE);
        } finally {
            generatingIndex = false;
        }
    }
}
