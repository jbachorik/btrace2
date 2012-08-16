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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A service lookup registry.
 * Services are registered in the context by their FQNs and can be looked-up as such
 * @author Jaroslav Bachorik
 * @since 2.0
 */
final public class Lookup {
    final private ConcurrentMap<Class, Set> contextMap = new ConcurrentHashMap<Class, Set>();
    /**
     * Add objects to the context
     * @param instances Objects to add to the context
     */
    public void add(Object ... instances) {
        for(Object i : instances) {
            register(i, i.getClass());
        }
    }
    /**
     * Remove objects from the context
     * @param instances The objects to be removed
     */
    public void remove(Object ... instances) {
        for(Object i : instances) {
            unregister(i, i.getClass());
        }
    }
    /**
     * Find a service implementation of the given type
     * @param <T> Type parameter for the service type
     * @param clz The class of the service type
     * @return Returns an instance of a service or <b>NULL</b>
     */
    public <T> T lookup(Class<? extends T> clz) {
        Iterator<T> iter = lookupAll(clz).iterator();
        if (iter.hasNext()) return iter.next();
        return null;
    }
    
    /**
     * Finds all service implementations of the given type
     * @param <T> Type parameter for the service type
     * @param clz The class of the service type
     * @return Returns a collection of all service instances
     */
    public <T> Collection<T> lookupAll(Class<? extends T> clz) {
        Set<T> insts = contextMap.get(clz);
        return insts != null ? insts : Collections.EMPTY_SET;
    }
    
    private void register(Object o, Class type) {
        if (type == null) return;
        
        Set impls = contextMap.putIfAbsent(type, new HashSet());
        if (impls == null) {
            impls = contextMap.get(type);
        }
        impls.add(o);
        register(o, type.getSuperclass());
        for(Class iType : type.getInterfaces()) {
            register(o, iType);
        }
    }
    
    private void unregister(Object o, Class type) {
        if (type == null) return;
        
        Set impls = contextMap.get(type);
        impls.remove(o);
        register(o, type.getSuperclass());
        for(Class iType : type.getInterfaces()) {
            register(o, iType);
        }
    }
}
