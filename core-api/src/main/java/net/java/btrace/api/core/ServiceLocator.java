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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ServiceLocator {
    private static class ClassLoadingIterator<T extends Class<?>> implements Iterator<T> {
        private String clzName;
        private ClassLoader[] loaders;
        private int loaderIndex = 0;
        
        private Iterator<String> services = null;
        private ClassLoader currentLoader;
        
        public ClassLoadingIterator(String clzName, ClassLoader[] loaders) {
            this.clzName = clzName;
            this.loaders = loaders;
        }
        
        public boolean hasNext() {
            keepInvariant();
            return services != null ? services.hasNext() : false;
        }

        public T next() {
            keepInvariant();
            if (services != null) {
                String service = services.next();
                try {
                    return (T) currentLoader.loadClass(service);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
            return null;
        }

        private void keepInvariant() {
            if ((services == null || !services.hasNext()) && loaders != null && loaders.length > loaderIndex) {
                currentLoader = loaders[loaderIndex++];
                services = collectServices(clzName, currentLoader).iterator();
            }
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    } 
    
    private static class InstantiatingIterator<T> implements Iterator<T> {
        private ClassLoadingIterator<Class<T>> clzIter;
        
        public InstantiatingIterator(String clzName, ClassLoader ... loaders) {
            clzIter = new ClassLoadingIterator<Class<T>>(clzName, loaders);
        }
        
        public boolean hasNext() {
            return clzIter.hasNext();
        }

        public T next() {
            Class<? extends T> clz = null;
            do {
                clz = clzIter.next();
                if (clz != null) {
                    try {
                        return clz.newInstance();
                    } catch (InstantiationException e) {
                        clz = null;
                    } catch (IllegalAccessException e) {
                        clz = null;
                    }
                }
            } while (clzIter.hasNext());
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    public static Iterable<String> listServiceNames(Class<?> clz) {
        return collectServices(clz.getName(), Thread.currentThread().getContextClassLoader());
    }
    
    public static Iterable<String> listServiceNames(Class<?> clz, ClassLoader ... loaders) {
        Collection<String> services = new ArrayList<String>();
        
        for(ClassLoader cl : loaders ) {
            services.addAll(collectServices(clz.getName(), cl));
        }
        return services;
    }
    
    public static <T> Iterable<Class<? extends T>> listServiceClasses(final Class<? extends T> clz) {
        return listServiceClasses(clz, new ClassLoader[] {Thread.currentThread().getContextClassLoader()});
    }
    
    public static <T> Iterable<Class<? extends T>> listServiceClasses(final Class<? extends T> clz, final ClassLoader ... loaders) {
        return new Iterable<Class<? extends T>>() {

            public Iterator<Class<? extends T>> iterator() {
                return new ClassLoadingIterator<Class<? extends T>>(clz.getName(), loaders);
            }
        };
    }
    
    public static <T> Iterable<T> listServices(final Class<? extends T> clz) {   
        return listServices(clz, new ClassLoader[] {Thread.currentThread().getContextClassLoader()});
    }
    
    public static <T> Iterable<T> listServices(final Class<? extends T> clz, final ClassLoader ... loaders) {
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return new InstantiatingIterator<T>(clz.getName(), loaders);
            }
        };
    }
    
    public static <T> T loadService(final Class<? extends T> clz) {
        return loadService(clz, Thread.currentThread().getContextClassLoader());
    }
    
    public static <T> T loadService(final Class<? extends T> clz, final ClassLoader ... loaders) {
        Iterator<T> i = new InstantiatingIterator<T>(clz.getName(), loaders);
        if (i.hasNext()) return i.next();
        return null;
    }
    
    private static class ServiceLine implements Comparable<ServiceLine> {
        private int position;
        private String serviceName;

        public ServiceLine(int position, String serviceName) {
            this.position = position;
            this.serviceName = serviceName;
        }

        public ServiceLine(String serviceName) {
            this(100, serviceName);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + this.position;
            hash = 97 * hash + (this.serviceName != null ? this.serviceName.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ServiceLine other = (ServiceLine) obj;
            if (this.position != other.position) {
                return false;
            }
            if ((this.serviceName == null) ? (other.serviceName != null) : !this.serviceName.equals(other.serviceName)) {
                return false;
            }
            return true;
        }

        public int compareTo(ServiceLine o) {
            if (this.position < o.position) return -1;
            if (this.position > o.position) return 1;
            return 0;
        }
    }
    
    private static Collection<String> collectServices(String clzName, ClassLoader cl) {
        List<ServiceLine> services = new ArrayList<ServiceLine>();
        InputStream is = null;
        try {
//            System.err.println("*** ClassLoader: ");
//            debugCL(cl);
//            System.err.println("*** getting resource for " + "META-INF/services/" + clzName);
            Enumeration<URL> urls = cl != null ? cl.getResources("META-INF/services/" + clzName) : ClassLoader.getSystemResources("META-INF/services/" + clzName);
            int curpos = 100;
            while (urls.hasMoreElements()) {
                is = urls.nextElement().openStream();
                if (is != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
                    try {
                        String line = br.readLine();
                        if (line.startsWith("#")) {
                            curpos = Integer.valueOf(line.substring(1));
                            continue;
                        }
                        while (line != null) {
                            services.add(new ServiceLine(curpos, line));
                            line = br.readLine();
                        }
                    } finally {
                        try {
                            br.close();
                        } catch (Exception e) {}
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {}
        }

        Collections.sort(services);
        List<String> serviceNames = new ArrayList<String>();
        for(ServiceLine sl : services) {
            serviceNames.add(sl.serviceName);
        }
        return serviceNames;
    }
    
//    private static void debugCL(ClassLoader cl) {
//        if (cl == null) {
//            System.err.println("*** BOOTCLASSLOADER");
//            return;
//        }
//        if (cl instanceof URLClassLoader) {
//            for(URL u : ((URLClassLoader)cl).getURLs()) {
//                System.err.println("*** url = " + u);
//            }
//        }
//        debugCL(cl.getParent());
//    }
}
