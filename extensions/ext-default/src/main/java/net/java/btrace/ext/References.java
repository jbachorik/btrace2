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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/*
 * Wraps the references related BTrace utility methods
 * @since 1.3
 */
@BTraceExtension
public class References {

    /**
     * Creates and returns a weak reference to the given object.
     *
     * @param obj object for which a weak reference is created.
     * @return a weak reference to the given object.
     */
    public static WeakReference weakRef(Object obj) {
        return new WeakReference<Object>(obj);
    }

    /**
     * Creates and returns a soft reference to the given object.
     *
     * @param obj object for which a soft reference is created.
     * @return a soft reference to the given object.
     */
    public static SoftReference softRef(Object obj) {
        return new SoftReference<Object>(obj);
    }

    /**
     * Returns the given reference object's referent.  If the reference object has
     * been cleared, either by the program or by the garbage collector, then
     * this method returns <code>null</code>.
     *
     * @param ref reference object whose referent is returned.
     * @return	 The object to which the reference refers, or
     *		 <code>null</code> if the reference object has been cleared.
     */
    public static Object deref(Reference ref) {
        if (ref.getClass().getClassLoader() == null) {
            return ref.get();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
