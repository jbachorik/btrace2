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

package net.java.btrace.api.extensions.runtime;

/**
 * Standardised objects operations
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 * 
 * @since 2.0
 */
public interface Objects {
    /**
     * Returns identity string of the form class-name@identity-hash
     *
     * @param obj object for which identity string is returned
     * @return identity string
     */
    abstract public String identityStr(Object obj);
    /**
     * Returns the same hash code for the given object as
     * would be returned by the default method hashCode(),
     * whether or not the given object's class overrides
     * hashCode(). The hash code for the null reference is zero.
     *
     * @param  obj object for which the hashCode is to be calculated
     * @return the hashCode
     */
    abstract public int identityHashCode(Object obj);
    
    /**
     * Returns the shallow size of the given object
     * @param obj object for which the sizeof is to be calculated
     * @return the shallow object size
     */
    abstract public long sizeof(Object obj);
    
    /**
     * Returns a hash code value for the object. This method is supported
     * for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>. For bootstrap classes, returns the
     * result of calling Object.hashCode() override. For non-bootstrap classes,
     * the identity hash code is returned.
     *
     * @param obj the Object whose hash code is returned.
     * @return  a hash code value for the given object.
     */
    abstract public int hash(Object obj);
    /**
     * Indicates whether two given objects are "equal to" one another.
     * For bootstrap classes, returns the result of calling Object.equals()
     * override. For non-bootstrap classes, the reference identity comparison
     * is done.
     *
     * @param  obj1 first object to compare equality
     * @param  obj2 second object to compare equality
     * @return <code>true</code> if the given objects are equal;
     *         <code>false</code> otherwise.
     */
    abstract public boolean compare(Object obj1, Object obj2);
}
