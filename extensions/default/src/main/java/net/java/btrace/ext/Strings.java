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
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Resource;
import net.java.btrace.api.extensions.runtime.Exceptions;
import net.java.btrace.api.extensions.runtime.Objects;

/*
 * Wraps the strings related BTrace utility methods
 * @since 1.2
 */
@BTraceExtension
public class Strings {
    @Resource
    private static Objects ctx;
    
    @Resource
    private static Exceptions exc;
    
    public static boolean startsWith(String s, String start) {
        return s.startsWith(start);
    }

    public static boolean endsWith(String s, String end) {
        return s.endsWith(end);
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read. For bootstrap classes, returns the result of
     * calling Object.toString() override. For non-bootstrap classes,
     * default toString() value [className@hashCode] is returned.
     *
     * @param  obj the object whose string representation is returned
     * @return a string representation of the given object.
     */
    public static String str(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            return (String) obj;
        } else if (obj.getClass().getClassLoader() == null) {
            try {
                return obj.toString();
            } catch (NullPointerException e) {
                return "null";
            }
        } else {
            return ctx.identityStr(obj);
        }
    }

    /**
     * Returns a <code>String</code> object representing the specified
     * <code>long</code>.  The argument is converted to signed decimal
     * representation and returned as a string.
     *
     * @param   l a <code>long</code> to be converted.
     * @return  a string representation of the argument in base&nbsp;10.
     */
    public static String str(long l) {
        return Long.toString(l);
    }

    /**
     * Returns a <tt>String</tt> object representing the specified
     * boolean.  If the specified boolean is <code>true</code>, then
     * the string {@code "true"} will be returned, otherwise the
     * string {@code "false"} will be returned.
     *
     * @param b	the boolean to be converted
     * @return the string representation of the specified <code>boolean</code>
     */
    public static String str(boolean b) {
        return Boolean.toString(b);
    }

    /**
     * Returns a <code>String</code> object representing the
     * specified <code>char</code>.  The result is a string of length
     * 1 consisting solely of the specified <code>char</code>.
     *
     * @param c the <code>char</code> to be converted
     * @return the string representation of the specified <code>char</code>
     */
    public static String str(char c) {
        return Character.toString(c);
    }

    /**
     * Returns a <code>String</code> object representing the
     * specified integer. The argument is converted to signed decimal
     * representation and returned as a string.
     *
     * @param   i   an integer to be converted.
     * @return  a string representation of the argument in base&nbsp;10.
     */
    public static String str(int i) {
        return Integer.toString(i);
    }

    /**
     * Returns a string representation of the <code>float</code>
     * argument. All characters mentioned below are ASCII characters.
     * <ul>
     * <li>If the argument is NaN, the result is the string
     * &quot;<code>NaN</code>&quot;.
     * <li>Otherwise, the result is a string that represents the sign and
     *     magnitude (absolute value) of the argument. If the sign is
     *     negative, the first character of the result is
     *     '<code>-</code>' (<code>'&#92;u002D'</code>); if the sign is
     *     positive, no sign character appears in the result. As for
     *     the magnitude <i>m</i>:
     * <ul>
     * <li>If <i>m</i> is infinity, it is represented by the characters
     *     <code>"Infinity"</code>; thus, positive infinity produces
     *     the result <code>"Infinity"</code> and negative infinity
     *     produces the result <code>"-Infinity"</code>.
     * <li>If <i>m</i> is zero, it is represented by the characters
     *     <code>"0.0"</code>; thus, negative zero produces the result
     *     <code>"-0.0"</code> and positive zero produces the result
     *     <code>"0.0"</code>.
     * <li> If <i>m</i> is greater than or equal to 10<sup>-3</sup> but
     *      less than 10<sup>7</sup>, then it is represented as the
     *      integer part of <i>m</i>, in decimal form with no leading
     *      zeroes, followed by '<code>.</code>'
     *      (<code>'&#92;u002E'</code>), followed by one or more
     *      decimal digits representing the fractional part of
     *      <i>m</i>.
     * <li> If <i>m</i> is less than 10<sup>-3</sup> or greater than or
     *      equal to 10<sup>7</sup>, then it is represented in
     *      so-called "computerized scientific notation." Let <i>n</i>
     *      be the unique integer such that 10<sup><i>n</i> </sup>&lt;=
     *      <i>m</i> &lt; 10<sup><i>n</i>+1</sup>; then let <i>a</i>
     *      be the mathematically exact quotient of <i>m</i> and
     *      10<sup><i>n</i></sup> so that 1 &lt;= <i>a</i> &lt; 10.
     *      The magnitude is then represented as the integer part of
     *      <i>a</i>, as a single decimal digit, followed by
     *      '<code>.</code>' (<code>'&#92;u002E'</code>), followed by
     *      decimal digits representing the fractional part of
     *      <i>a</i>, followed by the letter '<code>E</code>'
     *      (<code>'&#92;u0045'</code>), followed by a representation
     *      of <i>n</i> as a decimal integer, as produced by the
     *      method <code>{@link
     *      java.lang.Integer#toString(int)}</code>.
     * </ul>
     * </ul>
     * How many digits must be printed for the fractional part of
     * <i>m</i> or <i>a</i>? There must be at least one digit
     * to represent the fractional part, and beyond that as many, but
     * only as many, more digits as are needed to uniquely distinguish
     * the argument value from adjacent values of type
     * <code>float</code>. That is, suppose that <i>x</i> is the
     * exact mathematical value represented by the decimal
     * representation produced by this method for a finite nonzero
     * argument <i>f</i>. Then <i>f</i> must be the <code>float</code>
     * value nearest to <i>x</i>; or, if two <code>float</code> values are
     * equally close to <i>x</i>, then <i>f</i> must be one of
     * them and the least significant bit of the significand of
     * <i>f</i> must be <code>0</code>.
     * <p>
     *
     * @param   f   the float to be converted.
     * @return a string representation of the argument.
     */
    public static String str(float f) {
        return Float.toString(f);
    }

    /**
     * Returns a string representation of the <code>double</code>
     * argument. All characters mentioned below are ASCII characters.
     * <ul>
     * <li>If the argument is NaN, the result is the string
     *     &quot;<code>NaN</code>&quot;.
     * <li>Otherwise, the result is a string that represents the sign and
     * magnitude (absolute value) of the argument. If the sign is negative,
     * the first character of the result is '<code>-</code>'
     * (<code>'&#92;u002D'</code>); if the sign is positive, no sign character
     * appears in the result. As for the magnitude <i>m</i>:
     * <ul>
     * <li>If <i>m</i> is infinity, it is represented by the characters
     * <code>"Infinity"</code>; thus, positive infinity produces the result
     * <code>"Infinity"</code> and negative infinity produces the result
     * <code>"-Infinity"</code>.
     *
     * <li>If <i>m</i> is zero, it is represented by the characters
     * <code>"0.0"</code>; thus, negative zero produces the result
     * <code>"-0.0"</code> and positive zero produces the result
     * <code>"0.0"</code>.
     *
     * <li>If <i>m</i> is greater than or equal to 10<sup>-3</sup> but less
     * than 10<sup>7</sup>, then it is represented as the integer part of
     * <i>m</i>, in decimal form with no leading zeroes, followed by
     * '<code>.</code>' (<code>'&#92;u002E'</code>), followed by one or
     * more decimal digits representing the fractional part of <i>m</i>.
     *
     * <li>If <i>m</i> is less than 10<sup>-3</sup> or greater than or
     * equal to 10<sup>7</sup>, then it is represented in so-called
     * "computerized scientific notation." Let <i>n</i> be the unique
     * integer such that 10<sup><i>n</i></sup> &lt;= <i>m</i> &lt;
     * 10<sup><i>n</i>+1</sup>; then let <i>a</i> be the
     * mathematically exact quotient of <i>m</i> and
     * 10<sup><i>n</i></sup> so that 1 &lt;= <i>a</i> &lt; 10. The
     * magnitude is then represented as the integer part of <i>a</i>,
     * as a single decimal digit, followed by '<code>.</code>'
     * (<code>'&#92;u002E'</code>), followed by decimal digits
     * representing the fractional part of <i>a</i>, followed by the
     * letter '<code>E</code>' (<code>'&#92;u0045'</code>), followed
     * by a representation of <i>n</i> as a decimal integer, as
     * produced by the method {@link Integer#toString(int)}.
     * </ul>
     * </ul>
     * How many digits must be printed for the fractional part of
     * <i>m</i> or <i>a</i>? There must be at least one digit to represent
     * the fractional part, and beyond that as many, but only as many, more
     * digits as are needed to uniquely distinguish the argument value from
     * adjacent values of type <code>double</code>. That is, suppose that
     * <i>x</i> is the exact mathematical value represented by the decimal
     * representation produced by this method for a finite nonzero argument
     * <i>d</i>. Then <i>d</i> must be the <code>double</code> value nearest
     * to <i>x</i>; or if two <code>double</code> values are equally close
     * to <i>x</i>, then <i>d</i> must be one of them and the least
     * significant bit of the significant of <i>d</i> must be <code>0</code>.
     * <p>
     *
     * @param   d   the <code>double</code> to be converted.
     * @return a string representation of the argument.
     */
    public static String str(double d) {
        return Double.toString(d);
    }
    
    /**
     * This is synonym to "concat".
     *
     * @see #concat(String, String)
     */
    public static String strcat(String str1, String str2) {
        return concat(str1, str2);
    }

    /**
     * Concatenates the specified strings together.
     */
    public static String concat(String str1, String str2) {
        return str1.concat(str2);
    }

    /**
     * Compares two strings lexicographically.
     * The comparison is based on the Unicode value of each character in
     * the strings. The character sequence represented by the first
     * <code>String</code> object is compared lexicographically to the
     * character sequence represented by the second string. The result is
     * a negative integer if the first <code>String</code> object
     * lexicographically precedes the second string. The result is a
     * positive integer if the first <code>String</code> object lexicographically
     * follows the second string. The result is zero if the strings
     * are equal; <code>compareTo</code> returns <code>0</code> exactly when
     * the {@link String#equals(Object)} method would return <code>true</code>.
     */
    public static int compareTo(String str1, String str2) {
        return str1.compareTo(str2);
    }

    /**
     * This is synonym to "compareTo" method.
     *
     * @see #compareTo
     */
    public static int strcmp(String str1, String str2) {
        return str1.compareTo(str2);
    }

    /**
     * Compares two strings lexicographically, ignoring case
     * differences. This method returns an integer whose sign is that of
     * calling <code>compareTo</code> with normalized versions of the strings
     * where case differences have been eliminated by calling
     * <code>Character.toLowerCase(Character.toUpperCase(character))</code> on
     * each character.
     */
    public static int compareToIgnoreCase(String str1, String str2) {
        return str1.compareToIgnoreCase(str2);
    }

    /**
     * This is synonym to "compareToIgnoreCase".
     *
     * @see #compareToIgnoreCase
     */
    public static int stricmp(String str1, String str2) {
        return str1.compareToIgnoreCase(str2);
    }

    /**
     * Find String within String
     */
    public static int strstr(String str1, String str2) {
        return str1.indexOf(str2);
    }

    public static int indexOf(String str1, String str2) {
        return str1.indexOf(str2);
    }

    public static int lastIndexOf(String str1, String str2) {
        return str1.lastIndexOf(str2);
    }

    /**
     * Substring
     */
    public static String substr(String str, int start, int length) {
        return str.substring(start, length);
    }

    public static String substr(String str, int start) {
        return str.substring(start);
    }

    /**
     * Returns the length of the given string.
     * The length is equal to the number of <a href="Character.html#unicode">Unicode
     * code units</a> in the string.
     *
     * @param str String whose length is calculated.
     * @return  the length of the sequence of characters represented by this
     *          object.
     */
    public static int length(String str) {
        return str.length();
    }

    /**
     * This is synonym for "length".
     *
     * @see #length(String)
     */
    public static int strlen(String str) {
        return str.length();
    }

    // regular expression matching
    /**
     * Compiles the given regular expression into a pattern.  </p>
     *
     * @param  regex
     *         The expression to be compiled
     *
     * @throws  PatternSyntaxException
     *          If the expression's syntax is invalid
     */
    public static Pattern regexp(String regex) {
        return Pattern.compile(regex);
    }

    /**
     * This is synonym for "regexp".
     *
     * @see #regexp(String)
     */
    public static Pattern pattern(String regex) {
        return regexp(regex);
    }

    /**
     * Compiles the given regular expression into a pattern with the given
     * flags.  </p>
     *
     * @param  regex
     *         The expression to be compiled
     *
     * @param  flags
     *         Match flags, a bit mask that may include
     *         {@link Pattern#CASE_INSENSITIVE}, {@link Pattern#MULTILINE}, {@link Pattern#DOTALL},
     *         {@link Pattern#UNICODE_CASE}, {@link Pattern#CANON_EQ}, {@link Pattern#UNIX_LINES},
     *         {@link Pattern#LITERAL} and {@link Pattern#COMMENTS}
     *
     * @throws  IllegalArgumentException
     *          If bit values other than those corresponding to the defined
     *          match flags are set in <tt>flags</tt>
     *
     * @throws  PatternSyntaxException
     *          If the expression's syntax is invalid
     */
    public static Pattern regexp(String regex, int flags) {
        return Pattern.compile(regex, flags);
    }

    /**
     * This is synonym for "regexp".
     *
     * @see #regexp(String, int)
     */
    public static Pattern pattern(String regex, int flags) {
        return regexp(regex, flags);
    }

    /**
     * Matches the given (precompiled) regular expression and attempts
     * to match the given input against it.
     */
    public static boolean matches(Pattern regex, String input) {
        return regex.matcher(input).matches();
    }

    /**
     * Compiles the given regular expression and attempts to match the given
     * input against it.
     *
     * <p> An invocation of this convenience method of the form
     *
     * <blockquote><pre>
     * Pattern.matches(regex, input);</pre></blockquote>
     *
     * behaves in exactly the same way as the expression
     *
     * <blockquote><pre>
     * Pattern.compile(regex).matcher(input).matches()</pre></blockquote>
     *
     * <p> If a pattern is to be used multiple times, compiling it once and reusing
     * it will be more efficient than invoking this method each time.  </p>
     *
     * @param  regex
     *         The expression to be compiled
     *
     * @param  input
     *         The character sequence to be matched
     *
     * @throws  PatternSyntaxException
     *          If the expression's syntax is invalid
     */
    public static boolean matches(String regex, String input) {
        return Pattern.matches(regex, input);
    }

    /**
     * Returns a string representation of the integer argument as an
     * unsigned integer in base&nbsp;16.
     * <p>
     * The unsigned integer value is the argument plus 2<sup>32</sup>
     * if the argument is negative; otherwise, it is equal to the
     * argument.  This value is converted to a string of ASCII digits
     * in hexadecimal (base&nbsp;16) with no extra leading
     * <code>0</code>s. If the unsigned magnitude is zero, it is
     * represented by a single zero character <code>'0'</code>
     * (<code>'&#92;u0030'</code>); otherwise, the first character of
     * the representation of the unsigned magnitude will not be the
     * zero character. The following characters are used as
     * hexadecimal digits:
     * <blockquote><pre>
     * 0123456789abcdef
     * </pre></blockquote>
     * These are the characters <code>'&#92;u0030'</code> through
     * <code>'&#92;u0039'</code> and <code>'&#92;u0061'</code> through
     * <code>'&#92;u0066'</code>.
     *
     * @param   i   an integer to be converted to a string.
     * @return  the string representation of the unsigned integer value
     *          represented by the argument in hexadecimal (base&nbsp;16).
     */
    public static String toHexString(int i) {
        return Integer.toHexString(i);
    }

    /**
     * Returns a string representation of the <code>long</code>
     * argument as an unsigned integer in base&nbsp;16.
     * <p>
     * The unsigned <code>long</code> value is the argument plus
     * 2<sup>64</sup> if the argument is negative; otherwise, it is
     * equal to the argument.  This value is converted to a string of
     * ASCII digits in hexadecimal (base&nbsp;16) with no extra
     * leading <code>0</code>s.  If the unsigned magnitude is zero, it
     * is represented by a single zero character <code>'0'</code>
     * (<code>'&#92;u0030'</code>); otherwise, the first character of
     * the representation of the unsigned magnitude will not be the
     * zero character. The following characters are used as
     * hexadecimal digits:
     * <blockquote><pre>
     * 0123456789abcdef
     * </pre></blockquote>
     * These are the characters <code>'&#92;u0030'</code> through
     * <code>'&#92;u0039'</code> and  <code>'&#92;u0061'</code> through
     * <code>'&#92;u0066'</code>.
     *
     * @param   l a <code>long</code> to be converted to a string.
     * @return  the string representation of the unsigned <code>long</code>
     * 		value represented by the argument in hexadecimal
     *		(base&nbsp;16).
     */
    public static String toHexString(long l) {
        return Long.toHexString(l);
    }

    /**
     * Safely creates a new instance of an appendable string buffer <br>
     * @param threadSafe Specifies whether the buffer should be thread safe
     * @return Returns either {@linkplain StringBuilder} or {@linkplain StringBuffer}
     *         instance depending on whether the instance is required to be
     *         thread safe or not, respectively.
     * @since 1.2
     */
    public static Appendable newStringBuilder(boolean threadSafe) {
        return threadSafe ? new StringBuffer() : new StringBuilder();
    }

    /**
     * Safely creates a new instance of an appendable string buffer <br>
     * The buffer will not be thread safe.
     * @return Returns a new instance of {@linkplain StringBuilder} class
     * @since 1.2
     */
    public static Appendable newStringBuilder() {
        return newStringBuilder(false);
    }

    /**
     * Appends a string to an appendable buffer created by {@linkplain Strings#newStringBuilder()}
     * @param buffer The appendable buffer to append to
     * @param strToAppend The string to append
     * @return Returns the same appendable buffer instance
     * @since 1.2
     */
    public static Appendable append(Appendable buffer, String strToAppend) {
        try {
            return buffer.append(strToAppend);
        } catch (IOException ex) {
            exc.throwException(ex);
        }
        return buffer;

    }

    /**
     * Checks the length of an appendable buffer created by {@linkplain Strings#newStringBuilder()}
     * @param buffer The appendable buffer instance
     * @return Returns the length of the text contained by the buffer
     * @since 1.2
     */
    public static int length(Appendable buffer) {
        if (buffer instanceof StringBuffer) {
            return ((StringBuffer)buffer).length();
        } else if (buffer instanceof StringBuilder) {
            return ((StringBuilder)buffer).length();
        }
        return 0;
    }
}
