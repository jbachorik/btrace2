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
package net.java.btrace.api.core;

import java.io.File;
import java.io.FileOutputStream;
import net.java.btrace.api.server.Server;

/**
 *
 * @author Jaroslav Bachorik
 */
final public class BTraceLogger {
    private static boolean isDebug;
    public static void config(Server.Settings settings) {
        isDebug = settings.debugMode || Boolean.getBoolean("net.java.btrace.debug");
    }

    public static void setDebug(boolean val) {
        System.setProperty("net.java.btrace.debug", String.valueOf(val));
    }
    public static boolean isDebug() {
        return isDebug;
    }

    public static boolean isDumpClasses() {
        return true;
    }

    public static void debugPrint(String msg) {
        if (isDebug()) System.out.println("btrace DEBUG:" + msg);
    }

    public static void debugPrint(Throwable th) {
        System.err.println("btrace ERROR: " + th);
        th.printStackTrace(System.err);
    }

    public static void dumpClass(String className, byte[] code) {
        if (isDumpClasses()) {
            try {
                String dumpDir = getDumpDir();
                className = className.replace(".", File.separator).replace("/", File.separator);
                int index = className.lastIndexOf(File.separatorChar);
                StringBuilder buf = new StringBuilder();
                if (!dumpDir.equals(".")) {
                    buf.append(dumpDir);
                    buf.append(File.separatorChar);
                }
                String dir = buf.toString();
                if (index != -1) {
                    dir += className.substring(0, index);
                }
                new File(dir).mkdirs();
                String file;
                if (index != -1) {
                    file = className.substring(index + 1);
                } else {
                    file = className;
                }
                file += ".class";
                new File(dir).mkdirs();
                File out = new File(dir, file);
                FileOutputStream fos = new FileOutputStream(out);
                try {
                    fos.write(code);
                } finally {
                    fos.close();
                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
    }

    private static String getDumpDir() {
        return "/tmp";
//        return System.getProperty("net.java.btrace.dumpDir", ".");
    }
}
