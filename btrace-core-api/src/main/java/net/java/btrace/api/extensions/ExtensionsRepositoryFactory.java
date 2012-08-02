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
package net.java.btrace.api.extensions;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ExtensionsRepositoryFactory {
    private final static String EXTPATH_PROP = "btrace.extensions.path";
    
    private static final class DefaultExtensionsRepository extends ExtensionsRepository {
        public DefaultExtensionsRepository(Location l) {
            super(l);
        }
        
        @Override
        public String getExtensionsPath() {
            String userExtPath = System.getProperty(EXTPATH_PROP);
            StringBuilder extPathBuilder = new StringBuilder();
            String baseDir = getLibBaseDir();
            if (baseDir != null) {
                extPathBuilder.append(baseDir);
                if (userExtPath != null && !userExtPath.isEmpty()) {
                    extPathBuilder.append(File.pathSeparator).append(userExtPath);
                }

                return extPathBuilder.toString();
            }
            return null;
        }
        
        private String getLibBaseDir() {
            ClassLoader cl = ExtensionsRepository.class.getClassLoader();
            URL rsrc = cl != null ? cl.getResource("com/sun/btrace") : ClassLoader.getSystemResource("com/sun/btrace");
            if (rsrc != null) {
                String tmp = rsrc.toString();
                String baseDir = ".";
                if (tmp.contains("!")) {
                    tmp = tmp.substring(0, tmp.indexOf("!"));
                    if (tmp.startsWith("jar:")) {
                        tmp = tmp.substring("jar:".length(), tmp.lastIndexOf("/"));
                        try {
                            baseDir = new File(new URI(tmp)).getAbsolutePath();
                        } catch (URISyntaxException ex) {
                //            Logger.getLogger(ExtensionsLocator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                return baseDir + "/ext/";
            }
            return null;

        }
    }
    
    private static final ExtensionsRepository DEFAULT_SERVER = new DefaultExtensionsRepository(ExtensionsRepository.Location.SERVER);
    private static final ExtensionsRepository DEFAULT_CLIENT = new DefaultExtensionsRepository(ExtensionsRepository.Location.CLIENT);
    private static final ExtensionsRepository DEFAULT_BOTH = new DefaultExtensionsRepository(ExtensionsRepository.Location.BOTH);
    
    public static ExtensionsRepository builtin(ExtensionsRepository.Location location) {
        switch (location) {
            case SERVER: return DEFAULT_SERVER;
            case CLIENT: return DEFAULT_CLIENT;
            default: return DEFAULT_BOTH;
        }
    }
    
    public static ExtensionsRepository fixed(ExtensionsRepository.Location location, final String userExtPath) {
        return new ExtensionsRepository(location) {

            @Override
            public String getExtensionsPath() {
                return userExtPath;
            }
        };
    }
    
    public static ExtensionsRepository composite(ExtensionsRepository.Location location, ExtensionsRepository ... reps) {
        Set<String> paths = new HashSet<String>();
        StringBuilder sb = new StringBuilder();
        for(ExtensionsRepository rep : reps) {
            if (location == ExtensionsRepository.Location.BOTH || location == rep.getLocation()) {
                String ePath = rep.getExtensionsPath();
                if (ePath != null) {
                    paths.add(ePath);
                }
            }
        }
        for(String path : paths) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparatorChar);
            }
            sb.append(path);
        }
        return fixed(location, sb.toString());
    }
}
