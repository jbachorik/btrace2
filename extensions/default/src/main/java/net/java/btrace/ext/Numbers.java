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

/*
 * Wraps the numbers related BTrace utility methods
 * @since 1.3
 */
@BTraceExtension
public class Numbers {

    /**
     * Returns a <code>double</code> value with a positive sign, greater
     * than or equal to <code>0.0</code> and less than <code>1.0</code>.
     * Returned values are chosen pseudorandomly with (approximately)
     * uniform distribution from that range.
     */
    public static double random() {
        return Math.random();
    }

    /**
     * Returns the natural logarithm (base <i>e</i>) of a <code>double</code>
     * value.  Special cases:
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is negative infinity.</ul>
     *
     * <p>The computed result must be within 1 ulp of the exact result.
     * Results must be semi-monotonic.
     *
     * @param   a   a value
     * @return  the value ln&nbsp;<code>a</code>, the natural logarithm of
     *          <code>a</code>.
     */
    public strictfp static double log(double a) {
        return Math.log(a);
    }

    /**
     * Returns the base 10 logarithm of a <code>double</code> value.
     * Special cases:
     *
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is negative infinity.
     * <li> If the argument is equal to 10<sup><i>n</i></sup> for
     * integer <i>n</i>, then the result is <i>n</i>.
     * </ul>
     *
     * <p>The computed result must be within 1 ulp of the exact result.
     * Results must be semi-monotonic.
     *
     * @param   a   a value
     * @return  the base 10 logarithm of  <code>a</code>.
     */
    public strictfp static double log10(double a) {
        return Math.log10(a);
    }

    /**
     * Returns Euler's number <i>e</i> raised to the power of a
     * <code>double</code> value.  Special cases:
     * <ul><li>If the argument is NaN, the result is NaN.
     * <li>If the argument is positive infinity, then the result is
     * positive infinity.
     * <li>If the argument is negative infinity, then the result is
     * positive zero.</ul>
     *
     * <p>The computed result must be within 1 ulp of the exact result.
     * Results must be semi-monotonic.
     *
     * @param   a   the exponent to raise <i>e</i> to.
     * @return  the value <i>e</i><sup><code>a</code></sup>,
     *          where <i>e</i> is the base of the natural logarithms.
     */
    public strictfp static double exp(double a) {
        return Math.exp(a);
    }

    /**
     * Returns <code>true</code> if the specified number is a
     * Not-a-Number (NaN) value, <code>false</code> otherwise.
     *
     * @param   d  the value to be tested.
     * @return  <code>true</code> if the value of the argument is NaN;
     *          <code>false</code> otherwise.
     */
    public static boolean isNaN(double d) {
        return Double.isNaN(d);
    }

    /**
     * Returns <code>true</code> if the specified number is a
     * Not-a-Number (NaN) value, <code>false</code> otherwise.
     *
     * @param   f the value to be tested.
     * @return  <code>true</code> if the value of the argument is NaN;
     *          <code>false</code> otherwise.
     */
    public static boolean isNaN(float f) {
        return Float.isNaN(f);
    }

    /**
     * Returns <code>true</code> if the specified number is infinitely
     * large in magnitude, <code>false</code> otherwise.
     *
     * @param   d the value to be tested.
     * @return  <code>true</code> if the value of the argument is positive
     *          infinity or negative infinity; <code>false</code> otherwise.
     */
    public static boolean isInfinite(double d) {
        return Double.isInfinite(d);
    }

    /**
     * Returns <code>true</code> if the specified number is infinitely
     * large in magnitude, <code>false</code> otherwise.
     *
     * @param   f the value to be tested.
     * @return  <code>true</code> if the value of the argument is positive
     *          infinity or negative infinity; <code>false</code> otherwise.
     */
    public static boolean isInfinite(float f) {
        return Float.isInfinite(f);
    }

    // string parsing methods
    /**
     * Parses the string argument as a boolean.  The <code>boolean</code>
     * returned represents the value <code>true</code> if the string argument
     * is not <code>null</code> and is equal, ignoring case, to the string
     * {@code "true"}. <p>
     * Example: {@code Boolean.parseBoolean("True")} returns <tt>true</tt>.<br>
     * Example: {@code Boolean.parseBoolean("yes")} returns <tt>false</tt>.
     *
     * @param      s   the <code>String</code> containing the boolean
     *                 representation to be parsed
     * @return     the boolean represented by the string argument
     */
    public static boolean parseBoolean(String s) {
        return Boolean.parseBoolean(s);
    }

    /**
     * Parses the string argument as a signed decimal
     * <code>byte</code>. The characters in the string must all be
     * decimal digits, except that the first character may be an ASCII
     * minus sign <code>'-'</code> (<code>'&#92;u002D'</code>) to
     * indicate a negative value. The resulting <code>byte</code> value is
     * returned.
     *
     * @param s		a <code>String</code> containing the
     *                  <code>byte</code> representation to be parsed
     * @return 		the <code>byte</code> value represented by the
     *                  argument in decimal
     */
    public static byte parseByte(String s) {
        return Byte.parseByte(s);
    }

    /**
     * Parses the string argument as a signed decimal
     * <code>short</code>. The characters in the string must all be
     * decimal digits, except that the first character may be an ASCII
     * minus sign <code>'-'</code> (<code>'&#92;u002D'</code>) to
     * indicate a negative value. The resulting <code>short</code> value is
     * returned.
     *
     * @param s		a <code>String</code> containing the <code>short</code>
     *                  representation to be parsed
     * @return          the <code>short</code> value represented by the
     *                  argument in decimal.
     */
    public static short parseShort(String s) {
        return Short.parseShort(s);
    }

    /**
     * Parses the string argument as a signed decimal integer. The
     * characters in the string must all be decimal digits, except that
     * the first character may be an ASCII minus sign <code>'-'</code>
     * (<code>'&#92;u002D'</code>) to indicate a negative value. The resulting
     * integer value is returned.
     *
     * @param s	   a <code>String</code> containing the <code>int</code>
     *             representation to be parsed
     * @return     the integer value represented by the argument in decimal.
     */
    public static int parseInt(String s) {
        return Integer.parseInt(s);
    }

    /**
     * Parses the string argument as a signed decimal
     * <code>long</code>.  The characters in the string must all be
     * decimal digits, except that the first character may be an ASCII
     * minus sign <code>'-'</code> (<code>&#92;u002D'</code>) to
     * indicate a negative value. The resulting <code>long</code>
     * value is returned.
     * <p>
     * Note that neither the character <code>L</code>
     * (<code>'&#92;u004C'</code>) nor <code>l</code>
     * (<code>'&#92;u006C'</code>) is permitted to appear at the end
     * of the string as a type indicator, as would be permitted in
     * Java programming language source code.
     *
     * @param      s   a <code>String</code> containing the <code>long</code>
     *             representation to be parsed
     * @return     the <code>long</code> represented by the argument in
     *		   decimal.
     */
    public static long parseLong(String s) {
        return Long.parseLong(s);
    }

    /**
     * Returns a new <code>float</code> initialized to the value
     * represented by the specified <code>String</code>, as performed
     * by the <code>valueOf</code> method of class <code>Float</code>.
     *
     * @param      s   the string to be parsed.
     * @return the <code>float</code> value represented by the string
     *         argument.
     */
    public static float parseFloat(String s) {
        return Float.parseFloat(s);
    }

    /**
     * Returns a new <code>double</code> initialized to the value
     * represented by the specified <code>String</code>, as performed
     * by the <code>valueOf</code> methcod of class
     * <code>Double</code>.
     *
     * @param      s   the string to be parsed.
     * @return the <code>double</code> value represented by the string
     *         argument.
     */
    public static double parseDouble(String s) {
        return Double.parseDouble(s);
    }

    // boxing methods
    /**
     * Returns a <tt>Boolean</tt> instance representing the specified
     * <tt>boolean</tt> value.  If the specified <tt>boolean</tt> value
     * is <tt>true</tt>, this method returns <tt>Boolean.TRUE</tt>;
     * if it is <tt>false</tt>, this method returns <tt>Boolean.FALSE</tt>.
     *
     * @param  b a boolean value.
     * @return a <tt>Boolean</tt> instance representing <tt>b</tt>.
     */
    public static Boolean box(boolean b) {
        return Boolean.valueOf(b);
    }

    /**
     * Returns a <tt>Character</tt> instance representing the specified
     * <tt>char</tt> value.
     *
     * @param  c a char value.
     * @return a <tt>Character</tt> instance representing <tt>c</tt>.
     */
    public static Character box(char c) {
        return Character.valueOf(c);
    }

    /**
     * Returns a <tt>Byte</tt> instance representing the specified
     * <tt>byte</tt> value.
     *
     * @param  b a byte value.
     * @return a <tt>Byte</tt> instance representing <tt>b</tt>.
     */
    public static Byte box(byte b) {
        return Byte.valueOf(b);
    }

    /**
     * Returns a <tt>Short</tt> instance representing the specified
     * <tt>short</tt> value.
     *
     * @param  s a short value.
     * @return a <tt>Short</tt> instance representing <tt>s</tt>.
     */
    public static Short box(short s) {
        return Short.valueOf(s);
    }

    /**
     * Returns a <tt>Integer</tt> instance representing the specified
     * <tt>int</tt> value.
     *
     * @param  i an <code>int</code> value.
     * @return a <tt>Integer</tt> instance representing <tt>i</tt>.
     */
    public static Integer box(int i) {
        return Integer.valueOf(i);
    }

    /**
     * Returns a <tt>Long</tt> instance representing the specified
     * <tt>long</tt> value.
     *
     * @param  l a long value.
     * @return a <tt>Long</tt> instance representing <tt>l</tt>.
     */
    public static Long box(long l) {
        return Long.valueOf(l);
    }

    /**
     * Returns a <tt>Float</tt> instance representing the specified
     * <tt>float</tt> value.
     *
     * @param  f a float value.
     * @return a <tt>Float</tt> instance representing <tt>f</tt>.
     */
    public static Float box(float f) {
        return Float.valueOf(f);
    }

    /**
     * Returns a <tt>Double</tt> instance representing the specified
     * <tt>double</tt> value.
     *
     * @param  d a double value.
     * @return a <tt>Double</tt> instance representing <tt>d</tt>.
     */
    public static Double box(double d) {
        return Double.valueOf(d);
    }

    // unboxing methods
    /**
     * Returns the value of the given <tt>Boolean</tt> object as a boolean
     * primitive.
     *
     * @param b the Boolean object whose value is returned.
     * @return  the primitive <code>boolean</code> value of the object.
     */
    public static boolean unbox(Boolean b) {
        return b.booleanValue();
    }

    /**
     * Returns the value of the given <tt>Character</tt> object as a char
     * primitive.
     *
     * @param ch the Character object whose value is returned.
     * @return  the primitive <code>char</code> value of the object.
     */
    public static char unbox(Character ch) {
        return ch.charValue();
    }

    /**
     * Returns the value of the specified Byte as a <code>byte</code>.
     *
     * @param b Byte that is unboxed
     * @return  the byte value represented by the <code>Byte</code>.
     */
    public static byte unbox(Byte b) {
        return b.byteValue();
    }

    /**
     * Returns the short value represented by <code>Short</code>.
     *
     * @param s Short that is unboxed.
     * @return  the short value represented by the <code>Short</code>.
     */
    public static short unbox(Short s) {
        return s.shortValue();
    }

    /**
     * Returns the value of represented by <code>Integer</code>.
     *
     * @param i Integer that is unboxed.
     * @return  the int value represented by the <code>Integer</code>.
     */
    public static int unbox(Integer i) {
        return i.intValue();
    }

    /**
     * Returns the long value represented by the specified <code>Long</code>.
     *
     * @param l Long to be unboxed.
     * @return  the long value represented by the <code>Long</code>.
     */
    public static long unbox(Long l) {
        return l.longValue();
    }

    /**
     * Returns the float value represented by the specified <code>Float</code>.
     *
     * @param f Float to be unboxed.
     * @return  the float value represented by the <code>Float</code>.
     */
    public static float unbox(Float f) {
        return f.floatValue();
    }

    /**
     * Returns the double value represented by the specified <code>Double</code>.
     *
     * @param d Double to be unboxed.
     */
    public static double unbox(Double d) {
        return d.doubleValue();
    }
}
