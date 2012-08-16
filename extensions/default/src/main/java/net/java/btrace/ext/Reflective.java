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
package net.java.btrace.ext;

import net.java.btrace.api.extensions.BTraceExtension;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.annotation.Resource;
import net.java.btrace.api.extensions.runtime.Exceptions;
import sun.reflect.Reflection;

import static net.java.btrace.ext.Printer.*;

/*
 * Wraps the reflection related BTrace utility methods
 * @since 1.3
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class Reflective {
    @Resource
    private static Exceptions exc;
    
    // standard stack depth decrement for Reflection.getCallerClass() calls
    private static final int STACK_DEC = 4;
    
    /**
     * Returns the runtime class of the given Object.
     *
     * @param  obj the Object whose Class is returned
     * @return the Class object of given object
     */
    public static Class classOf(Object obj) {
        return obj.getClass();
    }

    /**
     * Returns the Class object representing the class or interface
     * that declares the field represented by the given Field object.
    
     * @param field whose declaring Class is returned
     */
    public static Class declaringClass(Field field) {
        return field.getDeclaringClass();
    }

    /**
     * Returns the name of the given Class object.
     */
    public static String name(Class clazz) {
        return clazz.getName();
    }

    /**
     * Returns the name of the Field object.
     *
     * @param  field Field for which name is returned
     * @return name of the given field
     */
    public static String name(Field field) {
        return field.getName();
    }

    /**
     * Returns the type of the Field object.
     *
     * @param  field Field for which type is returned
     * @return type of the given field
     */
    public static Class type(Field field) {
        return field.getType();
    }

    /**
     * Returns the access flags of the given Class.
     */
    public static int accessFlags(Class clazz) {
        return clazz.getModifiers();
    }

    /**
     * Returns the access flags of the given Field.
     */
    public static int accessFlags(Field field) {
        return field.getModifiers();
    }

    /**
     * Returns the current context class loader
     */
    public static ClassLoader contextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    // get Class of the given name
    /**
     * Returns Class object for given class name.
     */
    public static Class classForName(String name) {
        return classForName(name, getCallerLoader());
    }

    /**
     * Returns the Class for the given class name
     * using the given class loader.
     */
    public static Class classForName(String name, ClassLoader cl) {
        try {
            return Class.forName(name, false, cl);
        } catch (ClassNotFoundException exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Determines if the class or interface represented by the first
     * <code>Class</code> object is either the same as, or is a superclass or
     * superinterface of, the class or interface represented by the second
     * <code>Class</code> parameter. It returns <code>true</code> if so;
     * otherwise it returns <code>false</code>.
     */
    public static boolean isAssignableFrom(Class<?> a, Class<?> b) {
        return a.isAssignableFrom(b);
    }

    /**
     * Determines if the specified <code>Object</code> is assignment-compatible
     * with the object represented by the specified <code>Class</code>. This method is
     * the dynamic equivalent of the Java language <code>instanceof</code>
     * operator. The method returns <code>true</code> if the specified
     * <code>Object</code> argument is non-null and can be cast to the
     * reference type represented by this <code>Class</code> object without
     * raising a <code>ClassCastException.</code> It returns <code>false</code>
     * otherwise.
     *
     * @param  clazz the class that is checked.
     * @param  obj the object to check.
     * @return  true if <code>obj</code> is an instance of the given class.
     */
    public static boolean isInstance(Class clazz, Object obj) {
        return clazz.isInstance(obj);
    }

    /**
     * Returns the <code>Class</code> representing the superclass of the entity
     * (class, interface, primitive type or void) represented by the given
     * <code>Class</code>.  If the given <code>Class</code> represents either the
     * <code>Object</code> class, an interface, a primitive type, or void, then
     * null is returned.  If the given object represents an array class then the
     * <code>Class</code> object representing the <code>Object</code> class is
     * returned.
     *
     * @param clazz the Class whose super class is returned.
     * @return the superclass of the class represented by the given object.
     */
    public static Class getSuperclass(Class clazz) {
        return clazz.getSuperclass();
    }

    /**
     * Determines if the specified <code>Class</code> object represents an
     * interface type.
     *
     * @param clazz the Class object to check.
     * @return  <code>true</code> if the Class represents an interface;
     *          <code>false</code> otherwise.
     */
    public static boolean isInterface(Class clazz) {
        return clazz.isInterface();
    }

    /**
     * Determines if the given <code>Class</code> object represents an array class.
     *
     * @param clazz Class object to check.
     * @return  <code>true</code> if the given object represents an array class;
     *          <code>false</code> otherwise.
     */
    public static boolean isArray(Class clazz) {
        return clazz.isArray();
    }

    /**
     * Returns whether the given Class represent primitive type or not.
     */
    public static boolean isPrimitive(Class clazz) {
        return clazz.isPrimitive();
    }

    /**
     * returns component type of an array Class.
     */
    public static Class getComponentType(Class clazz) {
        return clazz.getComponentType();
    }

    // Accessing fields by reflection
    /**
     * Returns a <code>Field</code> object that reflects the specified declared
     * field of the class or interface represented by the given <code>Class</code>
     * object. The <code>name</code> parameter is a <code>String</code> that
     * specifies the simple name of the desired field. Returns <code>null</code> on not finding
     * field if throwException parameter is <code>false</code>. Else throws a <code>RuntimeException</code>
     * when field is not found.
     *
     * @param clazz Class whose field is returned
     * @param name the name of the field
     * @param throwException whether to throw exception on failing to find field or not
     * @return the <code>Field</code> object for the specified field in this
     * class
     */
    public static Field field(Class clazz, String name, boolean throwException) {
        return getField(clazz, name, throwException);
    }

    /**
     * Returns a <code>Field</code> object that reflects the specified declared
     * field of the class or interface represented by the given <code>Class</code>
     * object. The <code>name</code> parameter is a <code>String</code> that
     * specifies the simple name of the desired field. Throws a <code>RuntimeException</code>
     * when field is not found.
     *
     * @param clazz Class whose field is returned
     * @param name the name of the field
     * @return the <code>Field</code> object for the specified field in this
     * class
     */
    public static Field field(Class clazz, String name) {
        return field(clazz, name, true);
    }

    /**
     * Returns a <code>Field</code> object that reflects the specified declared
     * field of the class or interface represented by the given <code>Class</code>
     * object. The <code>name</code> parameter is a <code>String</code> that
     * specifies the simple name of the desired field. Returns <code>null</code> on not finding
     * field if throwException parameter is <code>false</code>. Else throws a <code>RuntimeException</code>
     * when field is not found.
     *
     * @param clazz Class whose field is returned
     * @param name the name of the field
     * @param throwException whether to throw exception on failing to find field or not
     * @return the <code>Field</code> object for the specified field in this
     * class
     */
    public static Field field(String clazz, String name, boolean throwException) {
        return field(classForName(clazz, getCallerLoader()), name, throwException);
    }

    /**
     * Returns a <code>Field</code> object that reflects the specified declared
     * field of the class or interface represented by the given <code>Class</code>
     * object. The <code>name</code> parameter is a <code>String</code> that
     * specifies the simple name of the desired field. Throws a <code>RuntimeException</code>
     * when field is not found.
     *
     * @param clazz Class whose field is returned
     * @param name the name of the field
     * @return the <code>Field</code> object for the specified field in this
     * class
     */
    public static Field field(String clazz, String name) {
        return field(classForName(clazz, getCallerLoader()), name);
    }

    // field value get methods
    /**
     * Gets the value of a static <code>byte</code> field.
     *
     * @param field Field object whose value is returned.
     * @return the value of the <code>byte</code> field
     */
    public static byte getByte(Field field) {
        checkStatic(field);
        try {
            return field.getByte(null);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of an instance <code>byte</code> field.
     *
     * @param field Field object whose value is returned.
     * @param obj the object to extract the <code>byte</code> value
     * from
     * @return the value of the <code>byte</code> field
     */
    public static byte getByte(Field field, Object obj) {
        try {
            return field.getByte(obj);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of a static <code>short</code> field.
     *
     * @param field Field object whose value is returned.
     * @return the value of the <code>short</code> field
     */
    public static short getShort(Field field) {
        checkStatic(field);
        try {
            return field.getShort(null);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of an instance <code>short</code> field.
     *
     * @param field Field object whose value is returned.
     * @param obj the object to extract the <code>short</code> value
     * from
     * @return the value of the <code>short</code> field
     */
    public static short getShort(Field field, Object obj) {
        try {
            return field.getShort(obj);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of a static <code>int</code> field.
     *
     * @param field Field object whose value is returned.
     * @return the value of the <code>int</code> field
     */
    public static int getInt(Field field) {
        checkStatic(field);
        try {
            return field.getInt(null);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of an instance <code>int</code> field.
     *
     * @param field Field object whose value is returned.
     * @param obj the object to extract the <code>int</code> value
     * from
     * @return the value of the <code>int</code> field
     */
    public static int getInt(Field field, Object obj) {
        try {
            return field.getInt(obj);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of a static <code>long</code> field.
     *
     * @param field Field object whose value is returned.
     * @return the value of the <code>long</code> field
     */
    public static long getLong(Field field) {
        checkStatic(field);
        try {
            return field.getLong(null);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of an instance <code>long</code> field.
     *
     * @param field Field object whose value is returned.
     * @param obj the object to extract the <code>long</code> value
     * from
     * @return the value of the <code>long</code> field
     */
    public static long getLong(Field field, Object obj) {
        try {
            return field.getLong(obj);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of a static <code>float</code> field.
     *
     * @param field Field object whose value is returned.
     * @return the value of the <code>float</code> field
     */
    public static float getFloat(Field field) {
        checkStatic(field);
        try {
            return field.getFloat(null);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of an instance <code>float</code> field.
     *
     * @param field Field object whose value is returned.
     * @param obj the object to extract the <code>float</code> value
     * from
     * @return the value of the <code>float</code> field
     */
    public static float getFloat(Field field, Object obj) {
        try {
            return field.getFloat(obj);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of a static <code>double</code> field.
     *
     * @param field Field object whose value is returned.
     * @return the value of the <code>double</code> field
     */
    public static double getDouble(Field field) {
        checkStatic(field);
        try {
            return field.getDouble(null);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of an instance <code>double</code> field.
     *
     * @param field Field object whose value is returned.
     * @param obj the object to extract the <code>double</code> value
     * from
     * @return the value of the <code>double</code> field
     */
    public static double getDouble(Field field, Object obj) {
        try {
            return field.getDouble(obj);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of a static <code>boolean</code> field.
     *
     * @param field Field object whose value is returned.
     * @return the value of the <code>boolean</code> field
     */
    public static boolean getBoolean(Field field) {
        checkStatic(field);
        try {
            return field.getBoolean(null);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of an instance <code>boolean</code> field.
     *
     * @param field Field object whose value is returned.
     * @param obj the object to extract the <code>boolean</code> value
     * from
     * @return the value of the <code>boolean</code> field
     */
    public static boolean getBoolean(Field field, Object obj) {
        try {
            return field.getBoolean(obj);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of a static <code>char</code> field.
     *
     * @param field Field object whose value is returned.
     * @return the value of the <code>char</code> field
     */
    public static char getChar(Field field) {
        checkStatic(field);
        try {
            return field.getChar(null);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of an instance <code>char</code> field.
     *
     * @param field Field object whose value is returned.
     * @param obj the object to extract the <code>char</code> value
     * from
     * @return the value of the <code>char</code> field
     */
    public static char getChar(Field field, Object obj) {
        try {
            return field.getChar(obj);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of a static reference field.
     *
     * @param field Field object whose value is returned.
     * @return the value of the reference field
     */
    public static Object get(Field field) {
        checkStatic(field);
        try {
            return field.get(null);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Gets the value of an instance reference field.
     *
     * @param field Field object whose value is returned.
     * @param obj the object to extract the reference value
     * from
     * @return the value of the reference field
     */
    public static Object get(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (Exception exp) {
            throw exc.translate(exp);
        }
    }

    /**
     * Print all instance fields of an object as name-value
     * pairs. Includes the inherited fields as well.
     *
     * @param obj Object whose fields are printed.
     */
    public static void printFields(Object obj) {
        printFields(obj, false);
    }

    /**
     * Print all instance fields of an object as name-value
     * pairs. Includes the inherited fields as well. Optionally,
     * prints name of the declaring class before each field - so that
     * if same named field in super class chain may be disambiguated.
     *
     * @param obj Object whose fields are printed.
     * @param classNamePrefix flag to tell whether to prefix field names
     *        names by class name or not.
     */
    public static void printFields(Object obj, boolean classNamePrefix) {
        StringBuilder buf = new StringBuilder();
        buf.append('{');
        addFieldValues(buf, obj, obj.getClass(), classNamePrefix);
        buf.append('}');
        println(buf.toString());
    }

    /**
     * Print all static fields of the class as name-value
     * pairs. Includes the inherited fields as well.
     *
     * @param clazz Class whose static fields are printed.
     */
    public static void printStaticFields(Class clazz) {
        printStaticFields(clazz, false);
    }

    /**
     * Print all static fields of the class as name-value
     * pairs. Includes the inherited fields as well. Optionally,
     * prints name of the declaring class before each field - so that
     * if same named field in super class chain may be disambigated.
     *
     * @param clazz Class whose static fields are printed.
     * @param classNamePrefix flag to tell whether to prefix field names
     *        names by class name or not.
     */
    public static void printStaticFields(Class clazz, boolean classNamePrefix) {
        StringBuilder buf = new StringBuilder();
        buf.append('{');
        addStaticFieldValues(buf, clazz, classNamePrefix);
        buf.append('}');
        println(buf.toString());
    }
        
    private static void addStaticFieldValues(StringBuilder buf,
        Class clazz, boolean classNamePrefix) {
        Field[] fields = getAllFields(clazz);
        for (Field f : fields) {
            int modifiers = f.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                if (classNamePrefix) {
                    buf.append(f.getDeclaringClass().getName());
                    buf.append('.');
                }
                buf.append(f.getName());
                buf.append('=');
                try {
                    Object val = f.get(null);
                    buf.append(val != null ? val.toString() : val);
                } catch (Exception exp) {
                    throw exc.translate(exp);
                }
                buf.append(", ");
            }
        }
        Class sc = clazz.getSuperclass();
        if (sc != null) {
           addStaticFieldValues(buf, sc, classNamePrefix);
        }
    }
    
    private static Field getField(final Class clazz, final String name,
            final boolean throwError) {
        return AccessController.doPrivileged(new PrivilegedAction<Field>() {
            public Field run() {
                try {
                    Field field = clazz.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (Exception exp) {
                    if (throwError) {
                       throw exc.translate(exp);
                    } else {
                       return null;
                    }
                }
            }
        });
    }

    private static Field[] getAllFields(final Class clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Field[]>() {
            public Field[] run() {
                try {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field f : fields) {
                        f.setAccessible(true);
                    }
                    return fields;
                } catch (Exception exp) {
                    throw exc.translate(exp);
                }
            }
        });
    }

    private static void addFieldValues(StringBuilder buf, Object obj,
        Class clazz,  boolean classNamePrefix) {
        Field[] fields = getAllFields(clazz);
        for (Field f : fields) {
            int modifiers = f.getModifiers();
            if (! Modifier.isStatic(modifiers)) {
                if (classNamePrefix) {
                    buf.append(f.getDeclaringClass().getName());
                    buf.append('.');
                }
                buf.append(f.getName());
                buf.append('=');
                try {
                    Object val = f.get(obj);
                    buf.append(val != null ? val.toString() : val);
                } catch (Exception exp) {
                    throw exc.translate(exp);
                }
                buf.append(", ");
            }
        }
        Class sc = clazz.getSuperclass();
        if (sc != null) {
           addFieldValues(buf, obj, sc, classNamePrefix);
        }
    }
    
    private static void checkStatic(Field field) {
        if (! Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException(field.getName() +
                " is not a static field");
        }
    }
    
    private static Class getCallerClass(int dec) {
        Class cClass = null;
        do {
            cClass = Reflection.getCallerClass(dec++);
            if (!cClass.getName().startsWith("net.java.btrace")) {
                return cClass;
            }
        } while (cClass != null);
        return null;
    }
    
    private static Class getCallerClass() {
        return getCallerClass(STACK_DEC);
    }
    
    private static ClassLoader getCallerLoader() {
        Class cClass = getCallerClass(STACK_DEC);
        if (cClass != null) {
            return cClass.getClassLoader();
        } else {
            return ClassLoader.getSystemClassLoader();
        }
    }
}
