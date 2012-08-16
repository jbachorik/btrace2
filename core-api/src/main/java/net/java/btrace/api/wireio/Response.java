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
package net.java.btrace.api.wireio;

/**
 * Asynchronous response with a typed payload
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 * @since 2.0
 */
public interface Response<T> {
    /**
     * The <b>NULL</b> response. Wraps the <b>NULL</b> value and does not block.
     */
    final public static Response NULL = new Response() {

        public Object get() throws InterruptedException {
            return null;
        }

        public Object get(long timeout) throws InterruptedException {
            return null;
        }
    };
    
    /**
     * Payload accessor.
     * @return Waits for the result to become available and returns it. May return <b>NULL</b> if interrupted.
     * @throws InterruptedException 
     */
    T get() throws InterruptedException;
    /**
     * Payload accessor.
     * @param timeout The timeout in <i>ms</i> to wait for the result
     * @return Waits for the result to become available and returns it. May return <b>NULL</b> if interrupted.
     * @throws InterruptedException 
     */
    T get(long timeout) throws InterruptedException;
}
