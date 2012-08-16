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

package net.java.btrace.api.extensions.util;

import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.extensions.ExtensionsRepository;
import java.lang.reflect.Method;

/**
 * Extended call target validator.
 * Allows for method calls to registered and activated extensions from the BTrace scripts.
 * 
 * @author Jaroslav Bachorik
 * @since 2.0
 */
public class CallTargetValidator {
    private ExtensionsRepository repository;
    
    public CallTargetValidator(ExtensionsRepository repository) {
        this.repository = repository;
        
        System.err.println(">>> located extensions");
        for(String s : repository.listExtensions()) {
            System.err.println(">>> " + s);
        }
    }
    
    public boolean isCallTargetValid(String name, int numArgs) {       
        for(String extName : repository.listExtensions()) {
            if (hasMethod(repository.loadExtension(extName), name, numArgs)) return true;
        }
        return false;
    }
    
    private boolean hasMethod(Class clz, String name, int numArgs) {
        if (clz == null) return false;
        for (Method m : clz.getMethods()) {
            if (m.getName().equals(name) &&
                m.getParameterTypes().length == numArgs) {
                return true;
            } else if (m.getName().equals(name) &&
                    m.getParameterTypes().length < numArgs &&
                    m.isVarArgs()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isCallTargetValid(String className, String methodName, int numArgs) {
        Class extClass = repository.loadExtension(className);
        if (extClass != null && extClass.getAnnotation(BTraceExtension.class) != null) {
            for (Method m : extClass.getMethods()) {
                if (m.getName().equals(methodName) &&
                    m.getParameterTypes().length == numArgs) {
                    return true;
                } else if (m.getName().equals(methodName) &&
                        m.getParameterTypes().length < numArgs &&
                        m.isVarArgs()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isClassValid(String className) {
        return repository.isExtensionAvailable(className.replace('/', '.'));
    }
}
