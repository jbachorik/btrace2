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

package net.java.btrace.spi.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.java.btrace.api.core.BTraceMBean;
import java.lang.reflect.Type;
import javax.management.openmbean.OpenType;

/**
 * An SPI class for providing custom types exposed through the BTrace MBeean
 * <p>
 * An implementation must be annotated by {@linkplain BTraceMBean.Decorator}
 * </p>
 * @author Jaroslav Bachorik
 * @since 2.0
 */
abstract public class MBeanDecoratorImpl {
    /**
     * Service registration annotation
     * @author Jaroslav Bachorik
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    public static @interface Registration {
    }
    
    /**
     * Converts an actual {@linkplain Type} instance to an {@linkplain OpenType} instance
     * @param type The {@linkplain Type} instance to be converted
     * @param mbean The {@linkplain BTraceMBean} context instance
     * @return Returns the converted {@linkplain OpenType} instance or NULL if not applicable
     */
    public OpenType toOpenType(Type type, BTraceMBean mbean) {
        return null;
    }
    
    /**
     * Converts a value to the specified {@linkplain OpenType} type
     * @param type The {@linkplain OpenType} type of the value
     * @param value The value to be converted
     * @param mbean The {@linkplain BTraceMBean} context instance
     * @return Returns the converted value of the given type or NULL if not applicable
     */
    public Object toOpenTypeValue(OpenType type, Object value, BTraceMBean mbean) {
        return null;
    }
}
