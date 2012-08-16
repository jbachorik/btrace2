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
package net.java.btrace.runtime;

/**
 * This class is a simple implementation of DynamicMBean that exposes
 * a BTrace class as a MBean. The static fields annotated with @Property
 * are exposed as MBean attributes.
 * 
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
import net.java.btrace.annotations.BTrace;
import net.java.btrace.annotations.Property;
import net.java.btrace.api.core.BTraceMBean;
import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.spi.core.MBeanDecoratorImpl;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * This is a simple DynamicMBean implementation that exposes the static 
 * fields of BTrace class as attributes. The fields exposed should be
 * annotated as {@linkplain Property}.
 * 
 * @author A. Sundararajan
 */
final class BTraceMBeanImpl extends BTraceMBean {

    private Class clazz;
    private Map<String, Field> attributes;
    private String beanName;
    private MBeanInfo cachedBeanInfo;
    private ExtensionsRepository repository;

    public BTraceMBeanImpl(Class clazz, ExtensionsRepository repository) {
        this.clazz = clazz;
        this.attributes = getJMXAttributes(clazz);
        this.beanName = getBeanName(clazz);
        this.repository = repository;
    }

    public synchronized Object getAttribute(String name)
            throws AttributeNotFoundException {
        Field field = attributes.get(name);
        if (field == null) {
            throw new AttributeNotFoundException("No such property: " + name);
        }
        return getFieldValue(field);
    }

    public synchronized AttributeList getAttributes(String[] names) {
        AttributeList list = new AttributeList();
        for (String name : names) {
            Field field = attributes.get(name);
            Object value = null;
            if (field != null) {
                value = getFieldValue(field);
            }
            if (value != null) {
                list.add(new Attribute(name, value));
            }
        }
        return list;
    }

    public synchronized MBeanInfo getMBeanInfo() {
        if (cachedBeanInfo != null) {
            return cachedBeanInfo;
        }
        SortedSet<String> names = new TreeSet<String>();
        for (String name : attributes.keySet()) {
            names.add((String) name);
        }
        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[names.size()];
        Iterator<String> it = names.iterator();
        for (int i = 0; i < attrs.length; i++) {
            String name = it.next();
            Field field = attributes.get(name);
            Property attr = field.getAnnotation(Property.class);
            String description = attr.description();
            if (description.isEmpty()) {
                description = name;
            }
            Descriptor descriptor = new DescriptorSupport();
            OpenType ot = typeToOpenType(field.getGenericType());
            if (ot != null) {
                descriptor.setField("openType", ot);
            }

            attrs[i] = new MBeanAttributeInfo(
                    name,
                    field.getType().getName(),
                    description,
                    true, // isReadable
                    false, // isWritable
                    false, // isIs
                    descriptor);
        }

        BTrace info = (BTrace) clazz.getAnnotation(BTrace.class);
        String description = info.description();
        if (description.isEmpty()) {
            description = "BTrace MBean : " + beanName;
        }
        cachedBeanInfo = new MBeanInfo(
                beanName,
                description,
                attrs,
                null, // constructors
                null,
                null); // notifications
        return cachedBeanInfo;
    }

    public static void registerMBean(Class clazz, ExtensionsRepository repository) {
        if (isMBean(clazz)) {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            BTraceMBeanImpl bean = new BTraceMBeanImpl(clazz, repository);
            try {
                ObjectName on = new ObjectName("btrace:name=" + bean.beanName);
                if (server.isRegistered(on)) {
                    server.unregisterMBean(on);
                }
                server.registerMBean(bean, on);
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception exp) {
                throw new RuntimeException(exp);
            }
        }
    }

    // internals only below this point
    private static String getBeanName(Class clazz) {
        BTrace info = (BTrace) clazz.getAnnotation(BTrace.class);
        String beanName = info.name();
        if (beanName.isEmpty()) {
            beanName = clazz.getName();
        }
        return beanName;
    }

    public static boolean isMBean(Class clazz) {
        // if atleast one field is annotated as @Property, we create MBean
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Property.class)) {
                return true;
            }
        }
        return false;
    }

    private Object getFieldValue(Field field) {
        try {
            Object value = field.get(null);
            OpenType ot = typeToOpenType(field.getGenericType());
            if (ot != null) {
                return convertToOpenTypeValue(ot, value);
            } else {
                // no conversion attempted!
                return value;
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    private static Map<String, Field> getJMXAttributes(Class clazz) {
        try {
            Map<String, Field> fields = new HashMap<String, Field>();
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                        && field.isAnnotationPresent(Property.class)) {
                    Property attr = field.getAnnotation(Property.class);
                    if (attr != null) {
                        field.setAccessible(true);
                        String attrName = attr.name();
                        if (attrName.isEmpty()) {
                            attrName = field.getName();
                            // remove BTRACE_FIELD_PREFIX ("$") from field name
                            attrName = attrName.substring(1);
                        }
                        fields.put(attrName, field);
                    }
                }
            }
            return fields;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    final private static Map<Class, OpenType> classToOpenTypes = new HashMap<Class, OpenType>();
    static {
        classToOpenTypes.put(Byte.TYPE, SimpleType.BYTE);
        classToOpenTypes.put(Byte.class, SimpleType.BYTE);
        classToOpenTypes.put(Short.TYPE, SimpleType.SHORT);
        classToOpenTypes.put(Short.class, SimpleType.SHORT);
        classToOpenTypes.put(Integer.TYPE, SimpleType.INTEGER);
        classToOpenTypes.put(Integer.class, SimpleType.INTEGER);
        classToOpenTypes.put(Long.TYPE, SimpleType.LONG);
        classToOpenTypes.put(Long.class, SimpleType.LONG);
        classToOpenTypes.put(Float.TYPE, SimpleType.FLOAT);
        classToOpenTypes.put(Float.class, SimpleType.FLOAT);
        classToOpenTypes.put(Double.TYPE, SimpleType.DOUBLE);
        classToOpenTypes.put(Double.class, SimpleType.DOUBLE);
        classToOpenTypes.put(Boolean.TYPE, SimpleType.BOOLEAN);
        classToOpenTypes.put(Boolean.class, SimpleType.BOOLEAN);
        classToOpenTypes.put(Character.TYPE, SimpleType.CHARACTER);
        classToOpenTypes.put(Character.class, SimpleType.CHARACTER);
        classToOpenTypes.put(AtomicInteger.class, SimpleType.INTEGER);
        classToOpenTypes.put(AtomicLong.class, SimpleType.LONG);
        classToOpenTypes.put(BigInteger.class, SimpleType.BIGINTEGER);
        classToOpenTypes.put(BigDecimal.class, SimpleType.BIGDECIMAL);
        classToOpenTypes.put(String.class, SimpleType.STRING);
        classToOpenTypes.put(ObjectName.class, SimpleType.OBJECTNAME);
        classToOpenTypes.put(Date.class, SimpleType.DATE);
    }
    
    private static class IterableServiceLoader<T> implements Iterable<T> {
        private ServiceLoader<T> loader;
        
        public IterableServiceLoader(ServiceLoader<T> l) {
            this.loader = l;
        }
        
        public Iterator<T> iterator() {
            return loader.iterator();
        }
    }
    
    private Iterable<MBeanDecoratorImpl> listDecorators() {
        final ServiceLoader<MBeanDecoratorImpl> decoratorLoader = ServiceLoader.load(MBeanDecoratorImpl.class, repository.getClassLoader(BTraceMBeanImpl.class.getClassLoader()));

        return new IterableServiceLoader<MBeanDecoratorImpl>(decoratorLoader);
    }
    
    public OpenType typeToOpenType(Type t) {
        OpenType ot = null;
        for (MBeanDecoratorImpl d : listDecorators()) {
            ot = d.toOpenType(t, this);
            if (ot != null) {
                return ot;
            }
        }
        
        // FIXME: This is highly incomplete, revisit...
        // just enough to get Maps for now.
        try {
            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                Type rawType = pt.getRawType();
                if (rawType instanceof Class) {
                    Class rt = (Class) rawType;
                    Type[] argTypes = pt.getActualTypeArguments();
                    if (Map.class.isAssignableFrom(rt)) {
                        OpenType keyType = typeToOpenType(argTypes[0]);
                        OpenType valueType = typeToOpenType(argTypes[1]);
                        if (keyType != null && valueType != null) {
                            CompositeType rowType = new CompositeType("Map",
                                    "Map of data",
                                    new String[]{"key", "value"},
                                    new String[]{"key", "value"},
                                    new OpenType[]{keyType, valueType});
                            return new ArrayType(1, rowType);
                        }
                    }
                }
            }
        } catch (OpenDataException ode) {
            ode.printStackTrace();
        }

        // nothing seems working...
        return null;
    }

    public Object convertToOpenTypeValue(OpenType ot, Object value) {
        Object val = null;
        for (MBeanDecoratorImpl d : listDecorators()) {
            val = d.toOpenTypeValue(ot, value, this);
            if (val != null) {
                return val;
            }
        }
        
        if (ot instanceof SimpleType) {
            if (value instanceof AtomicInteger) {
                return Integer.valueOf(((AtomicInteger) value).get());
            } else if (value instanceof AtomicLong) {
                return Long.valueOf(((AtomicLong) value).get());
            } else {
                return value;
            }
        } else if (ot instanceof ArrayType) {
            ArrayType at = (ArrayType) ot;
            OpenType et = at.getElementOpenType();
            if (value instanceof Map && et instanceof CompositeType) {
                CompositeType ct = (CompositeType) et;
                Map<Object, Object> map = new HashMap((Map<Object, Object>) value);
                CompositeData[] array = new CompositeData[map.size()];
                OpenType keyType = ct.getType("key");
                OpenType valueType = ct.getType("value");
                int index = 0;
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    Map<String, Object> row = new HashMap<String, Object>();
                    row.put("key", convertToOpenTypeValue(keyType, entry.getKey()));
                    row.put("value", convertToOpenTypeValue(valueType, entry.getValue()));
                    try {
                        array[index] = new CompositeDataSupport(ct, row);
                    } catch (OpenDataException ode) {
                        ode.printStackTrace();
                    }
                    index++;
                }
                return array;
            }
        }
        return value;
    }
}
