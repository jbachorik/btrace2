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

package com.sun.btrace.spi.impl;

import com.sun.btrace.api.BTraceCompiler;
import com.sun.btrace.api.BTraceTask;
import com.sun.btrace.spi.BTraceCompilerFactory;
import com.sun.btrace.spi.ToolsJarLocator;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
final public class BTraceCompilerFactoryImpl implements BTraceCompilerFactory {
    final private static Logger LOGGER = Logger.getLogger(BTraceCompilerFactory.class.getName());

    final private static Pattern classNamePattern = Pattern.compile("@BTrace\\s*.+?\\s*class\\s*(.*?)\\s+\\{", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNIX_LINES);

    public BTraceCompiler newCompiler(final BTraceTask task) {
        return new BTraceCompiler() {
            final private com.sun.btrace.compiler.Compiler c = new com.sun.btrace.compiler.Compiler(null, task.isUnsafe());

            @Override
            public byte[] compile(String source, String classPath, Writer errorWriter) {
                try {
                    Matcher matcher = classNamePattern.matcher(source);
                    if (matcher.find()) {
                        if (errorWriter == null) {
                            errorWriter = new PrintWriter(System.out);
                        }
                        String fileName = matcher.group(1) + ".java";
                        String completeCP = getToolsJarPath() + File.pathSeparator + getClientJarPath() + File.pathSeparator + classPath;
                        Map<String, byte[]> compilationMap =c.compile(fileName, source, errorWriter, ".", completeCP);
                        if (compilationMap != null) {
                            return compilationMap.values().iterator().next();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, null, e);
                }
                return new byte[0];
            }

            @Override
            public String getAgentJarPath() {
                return getJarPath("btrace-agent-");
            }

            @Override
            public String getClientJarPath() {
                String base = getJarBaseDir();
                File f = new File(base);
                File[] files = f.listFiles(new FilenameFilter() {

                    public boolean accept(File file, String name) {
                        return name.endsWith(".jar");
                    }
                });
                assert files.length > 0;
                StringBuilder sb = new StringBuilder();
                for(File jar : files) {
                    sb.append(jar.getAbsolutePath()).append(File.pathSeparator);
                }
                return sb.toString();
            }

            final private Object toolsJarLock = new Object();
            private String toolsJar = null;
            @Override
            public String getToolsJarPath() {
                synchronized(toolsJarLock) {
                    if (toolsJar == null) {
                        ServiceLoader<ToolsJarLocator> locator = ServiceLoader.load(ToolsJarLocator.class);
                        if (locator != null) {
                            Iterator<ToolsJarLocator> iter = locator.iterator();
                            while (iter.hasNext()) {
                                File file = iter.next().locateToolsJar(task);
                                if (file != null && file.exists()) {
                                    toolsJar = file.getAbsolutePath();
                                    break;
                                }
                            }
                        }
                    }
                    return toolsJar;
                }
            }

            final private Object jarBaseDirLock = new Object();
            private String jarBaseDir = null;

            private String getJarPath(final String jarBaseName) {
                String base = getJarBaseDir();
                File f = new File(base);
                File[] files = f.listFiles(new FilenameFilter() {

                    public boolean accept(File file, String name) {
                        return name.startsWith(jarBaseName);
                    }
                });
                assert files.length > 0;
                return files[0].getAbsolutePath();
            }
            
            private String getJarBaseDir() {
                synchronized(jarBaseDirLock) {
                    if (jarBaseDir == null) {
                        File f = getContainingJar("com/sun/btrace/BTraceRuntime.class");
                        while (f != null && (!f.exists() || !f.isDirectory())) {
                            f = f.getParentFile();
                        }
                        if (f != null) {
                            jarBaseDir = f.getAbsolutePath();
                        }
                    }
                    return jarBaseDir;
                }
            }

            private File getContainingJar(String clz) {
                File jarFile;
                URL url = getClass().getClassLoader().getResource(clz);
                if ("jar".equals(url.getProtocol())) { //NOI18N

                    String path = url.getPath();
                    int index = path.indexOf("!/"); //NOI18N

                    if (index >= 0) {
                        try {
                            String jarPath = path.substring(0, index);
                            if (jarPath.indexOf("file://") > -1 && jarPath.indexOf("file:////") == -1) {  //NOI18N
                                /* Replace because JDK application classloader wrongly recognizes UNC paths. */
                                jarPath = jarPath.replaceFirst("file://", "file:////");  //NOI18N
                            }
                            url = new URL(jarPath);

                        } catch (MalformedURLException mue) {
                            throw new RuntimeException(mue);
                        }
                    }
                }
                try {
                    jarFile = new File(url.toURI());
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
                assert jarFile.exists();
                return jarFile;
            }
        };
    }

}
