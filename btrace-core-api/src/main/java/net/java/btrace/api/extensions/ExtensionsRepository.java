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

import net.java.btrace.api.core.ServiceLocator;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 *
 * @author Jaroslav Bachorik
 */
abstract public class ExtensionsRepository {

    private Set<ExtensionPrivilege> getRequestedPrivileges(Attributes attrs) {
        String permissionsList = attrs.getValue(BTRACE_PRIVILEGES_ATTRIBUTE);
        Set<ExtensionPrivilege> requestedPermissions = EnumSet.noneOf(ExtensionPrivilege.class);
        if (permissionsList != null) {
            StringTokenizer st = new StringTokenizer(permissionsList, ",");
            while (st.hasMoreTokens()) {
                String permName = st.nextToken();
                ExtensionPrivilege perm = ExtensionPrivilege.valueOf(permName);
                if (perm != null) {
                    requestedPermissions.add(perm);
                } else {
                    System.err.println("*** invalid permission name: " + permName);
                }
            }
        }
        return requestedPermissions;
    }
    public enum Location {
        CLIENT, SERVER, BOTH
    }
    
    private static final String BTRACE_EXTENSION_ATTRIBUTE = "BTrace-Extension";
    private static final String BTRACE_PRIVILEGES_ATTRIBUTE = "BTrace-Privileges";
    private ClassLoader cLoader = null;
    
    final private Object extensionsLock = new Object();
    // @GuardedBy extensionsLock
    private List<URL> extensions = null;
    final private Set<String> enabledExtensions = new HashSet<String>();
    final private AtomicBoolean extensionsLoaded = new AtomicBoolean(false);
    
    final private Location location;
    final private Set<ExtensionPrivilege> privileges = EnumSet.noneOf(ExtensionPrivilege.class);
    
    ExtensionsRepository(Location l) {
        this.location = l;
    }
    
    ExtensionsRepository(Location l, Set<ExtensionPrivilege> permissions) {
        this.location = l;
        this.privileges.addAll(permissions);
    }

    final public Location getLocation() {
        return location;
    }
    
    final public String getClassPath() {
        Collection<File> jars = getExtensionFiles();

        StringBuilder sb = new StringBuilder();
        
        for(File jar : jars) {
            sb.append(File.pathSeparator).append(jar.getAbsolutePath());
        }
        
        return sb.toString();
    }
    
    final synchronized public ClassLoader getClassLoader() {
        if (cLoader == null) {
            List<URL> locs = getExtensionURLs();
            cLoader = new URLClassLoader(locs.toArray(new URL[locs.size()]), ClassLoader.getSystemClassLoader());
        }
        return cLoader;
    }
    
    final public ClassLoader getClassLoader(ClassLoader parent) {
        List<URL> locs = getExtensionURLs();
        return new URLClassLoader(locs.toArray(new URL[locs.size()]), parent);
    }
    
    final public Class loadExtension(String className) {
        try {
            return getClassLoader().loadClass(className.replace('/', '.'));
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }
    
    final public boolean isExtensionAvailable(String extensionFqn) {
        return listExtensions().contains(extensionFqn);
    }
    
    final public Collection<String> listExtensions() {
        if (extensionsLoaded.compareAndSet(false, true)) {
            for(String svcName : ServiceLocator.listServiceNames(BTraceExtension.class, getClassLoader())) {
                enabledExtensions.add(svcName);
            }
        }
        return Collections.unmodifiableSet(enabledExtensions);
    }
    
    final public List<URL> getExtensionURLs() {
        synchronized(extensionsLock) {
            if (extensions == null) {
                Collection<File> jars = getExtensionFiles();
                extensions = new ArrayList<URL>();
                for(File jar : jars) {
                    try {
                        extensions.add(jar.toURI().toURL());
                    } catch (MalformedURLException ex) {
        //                Logger.getLogger(ExtensionsLocator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return extensions;
    }
    
    abstract public String getExtensionsPath();
    
    final ReadWriteLock extensionJarsLock = new ReentrantReadWriteLock();
    final AtomicReference<List<File>> extensionJarsRef = new AtomicReference<List<File>>();
    private List<File> getExtensionFiles() {
        extensionJarsLock.readLock().lock();
        List<File> jars = extensionJarsRef.get();
        if (jars == null) {
            try {
                extensionJarsLock.readLock().unlock();
                extensionJarsLock.writeLock().lock();
                jars = extensionJarsRef.get();
                if (jars == null) {
                    jars = new ArrayList<File>();

                    String extPath = getExtensionsPath();
                    if (extPath != null) {
                        StringTokenizer st = new StringTokenizer(extPath, File.pathSeparator);
                        while (st.hasMoreTokens()) {
                            collectJars(new File(st.nextToken()), jars);
                        }
                    }
                    extensionJarsRef.set(jars);
                    return jars;
                }
            } finally {
                extensionJarsLock.writeLock().unlock();
            }
        } else {
            extensionJarsLock.readLock().unlock();
        }
        return jars;
    }
    
    private void collectJars(File dir, List<File> jars) {
        if (!dir.exists()) return;
        
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File path) {
                return path.isDirectory() || path.getName().toLowerCase().endsWith(".jar");
            }
        });
        if (files != null) {
            for(File f : files) {
                if (f.isDirectory()) {
                    collectJars(f, jars);
                } else {
                    try {
                        JarFile jf = new JarFile(f);
                        Attributes attrs = jf.getManifest().getMainAttributes();
                        String extLocationStr = attrs.getValue(BTRACE_EXTENSION_ATTRIBUTE);
                        if (extLocationStr != null) {
                            Location extLocation = Location.valueOf(extLocationStr.toUpperCase());
                            if (location == Location.BOTH || extLocation == Location.BOTH || location == extLocation) {
                                if (!hasSignature(attrs)) {
                                    System.err.println("*** attempting to load an extension from unsigned jar: " + jf.getName() + " @" + extLocation.name());
                                }
                                Set<ExtensionPrivilege> requestedPrivileges = getRequestedPrivileges(attrs);
                                if (privileges.containsAll(requestedPrivileges)) {
                                    jars.add(f);
                                } else {
                                    requestedPrivileges.removeAll(privileges);
                                    System.err.println("*** attempting to load an extension with not allowed privileges: " + requestedPrivileges);
                                }
                            }
                        }
                    } catch (IOException e) {}
                }
            }
        }
    }
    
    private static boolean hasSignature(Attributes attrs) {
        for(Object k : attrs.keySet()) {
            if (((Attributes.Name)k).toString().endsWith("-Digest")) {
                return true;
            }
        }
        return false;
    }
}
