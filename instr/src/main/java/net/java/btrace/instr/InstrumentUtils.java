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

import net.java.btrace.org.objectweb.asm.ClassReader;
import net.java.btrace.org.objectweb.asm.ClassVisitor;
import net.java.btrace.org.objectweb.asm.ClassWriter;
import static net.java.btrace.org.objectweb.asm.Opcodes.*;
import static net.java.btrace.instr.Constants.JAVA_LANG_OBJECT;

/**
 * @author A. Sundararajan
 */
public class InstrumentUtils {
    public static String arrayDescriptorFor(int typeCode) {
        switch (typeCode) {
            case T_BOOLEAN:
                return "[Z";
            case T_CHAR:
                return "[C";
            case T_FLOAT:
                return "[F";
            case T_DOUBLE:
                return "[D";
            case T_BYTE:
                return "[B";
            case T_SHORT:
                return "[S";
            case T_INT:
                return "[I";
            case T_LONG:
                return "[J";
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void accept(ClassReader reader, ClassVisitor visitor) {
        accept(reader, visitor, ClassReader.SKIP_FRAMES);
    }

    public static void accept(ClassReader reader, ClassVisitor visitor, int flags) {
        reader.accept(visitor, flags);
    }
    
    private static boolean isJDK16OrAbove(byte[] code) {
        // skip 0xCAFEBABE magic and minor version
        final int majorOffset = 4 + 2;
        int major = (((code[majorOffset] << 8) & 0xFF00) | 
                ((code[majorOffset + 1]) & 0xFF));
        return major >= 50;
    }
    
    public static ClassWriter newClassWriter() {
        return newClassWriter(null, 
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    }

    public static ClassWriter newClassWriter(byte[] code) {
        int flags = ClassWriter.COMPUTE_MAXS;
        if (isJDK16OrAbove(code)) {
            flags |= ClassWriter.COMPUTE_FRAMES;
        }
        return newClassWriter(null, flags);
    }
    
    public static ClassWriter newClassWriter(ClassReader reader, int flags) {
        // FIXME: getCommonSuperClass is called by ClassWriter to merge two types
        // - persumably to compute stack frame attribute. We get LinkageError
        // when one of the types is the one being written/prepared by this ClassWriter
        // itself! So, I catch LinkageError and return "java/lang/Object" in such cases.
        // Revisit this for a possible better solution.
        ClassWriter cw = null;
        if (reader != null) {
            cw = new ClassWriter(reader, flags) {
                protected String getCommonSuperClass(String type1, String type2) {
                    try {
                        return super.getCommonSuperClass(type1, type2);                   
                    } catch (LinkageError le) {
                        return JAVA_LANG_OBJECT;
                    } catch (RuntimeException re) {
                        return JAVA_LANG_OBJECT;
                    }
                }
            };
        } else {
            cw = new ClassWriter(flags) {
                protected String getCommonSuperClass(String type1, String type2) {
                    try {
                        return super.getCommonSuperClass(type1, type2);
                    } catch (LinkageError le) {
                        return JAVA_LANG_OBJECT;
                    } catch (RuntimeException re) {
                        return JAVA_LANG_OBJECT;
                    }
                }
            };
        }
        return cw;
    }
} 