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

/**
 * This class is a simple implementation of DynamicMBean that exposes
 * a BTrace class as a MBean. The static fields annotated with @Property
 * are exposed as MBean attributes.
 * 
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
import java.lang.reflect.Type;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenType;

/**
 * This is a simple DynamicMBean implementation that exposes the static 
 * fields of BTrace class as attributes. The fields exposed should be
 * annotated as {@linkplain Property}.
 * 
 * @author A. Sundararajan
 */
public abstract class BTraceMBean implements DynamicMBean {
    abstract public Object getAttribute(String name) throws AttributeNotFoundException;

    final public synchronized void setAttribute(Attribute attribute)
            throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
        throw new MBeanException(new RuntimeException("BTrace attributes are read-only"));
    }

    abstract public AttributeList getAttributes(String[] names);

    final public synchronized AttributeList setAttributes(AttributeList list) {
        // we don't support attribute sets -- return an empty list.
        return new AttributeList();
    }

    final public Object invoke(String name, Object[] args, String[] sig)
            throws MBeanException, ReflectionException {
        throw new ReflectionException(new NoSuchMethodException(name));
    }

    abstract public MBeanInfo getMBeanInfo();
    
    abstract public Object convertToOpenTypeValue(OpenType ot, Object value);
    abstract public OpenType typeToOpenType(Type t);
}
