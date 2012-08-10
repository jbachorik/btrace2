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
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.wireio.commands.MessageCommand;
import net.java.btrace.wireio.commands.NumberDataCommand;
import net.java.btrace.wireio.commands.NumberMapDataCommand;
import net.java.btrace.wireio.commands.StringMapDataCommand;
import java.util.Map;
import javax.annotation.Resource;
import net.java.btrace.api.extensions.runtime.CommLine;

/**
 *
 * @author Jaroslav Bachorik
 */
@BTraceExtension
public class Printer {
    @Resource
    private static CommLine l;
    private static final String NULL_MSG = "<null>";
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    public static String INDENT = "    ";
    /**
     * Convenience method for printing textual messages to the BTrace output stream
     * @param str The message to print
     * @param timeStamp Flag indicating whether a {@linkplain System#currentTimeMillis()} timestamp should be attached to the message
     */
    public static void print(final String str, final boolean timeStamp) {        
        l.send(MessageCommand.class, new AbstractCommand.Initializer<MessageCommand> () {

            public void init(MessageCommand cmd) {
                cmd.setMessage(str != null ? str : NULL_MSG);
                if (timeStamp) cmd.setTime(System.currentTimeMillis());
            }
        });
    }

    public static void print(String str) {
        print(str, false);
    }

    /**
     * Convenience method for printing textual messages followed by a CR/LF to the BTrace output stream
     * @param str The message to print
     * @param timeStamp Flag indicating whether a {@linkplain System#currentTimeMillis()} timestamp should be attached to the message
     */
    public static void println(String str, boolean timeStamp) {
        print(str + "\n", timeStamp);
    }

    public static void println(String str) {
        println(str, false);
    }
    
    public static void print(Object obj) {
        print(obj != null ? obj.toString() : obj);
    }

    /**
     * Prints a boolean value.  The string produced by <code>{@link
     * java.lang.String#valueOf(boolean)}</code> is sent to BTrace client
     * for "printing".
     *
     * @param      b   The <code>boolean</code> to be printed
     */

    public static void print(boolean b) {
        print(Boolean.valueOf(b));
    }

    /**
     * Prints a character.  The string produced by <code>{@link
     * java.lang.Character#valueOf(char)}</code> is sent to BTrace client
     * for "printing".
     *
     *
     * @param      c   The <code>char</code> to be printed
     */
    public static void print(char c) {
        print(Character.valueOf(c));
    }

    /**
     * Prints an integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(int)}</code> is sent to BTrace client for "printing".
     *
     * @param      i   The <code>int</code> to be printed
     * @see        java.lang.Integer#toString(int)
     */

    public static void print(int i) {
        print(Integer.valueOf(i));
    }


    /**
     * Prints a long integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(long)}</code> is sent to BTrace client for "printing".
     *
     * @param      l   The <code>long</code> to be printed
     * @see        java.lang.Long#toString(long)
     */
    public static void print(long l) {
        print(Long.valueOf(l));
    }

    /**
     * Prints a floating-point number.  The string produced by <code>{@link
     * java.lang.String#valueOf(float)}</code> is sent to BTrace client for "printing".
     *
     * @param      f   The <code>float</code> to be printed
     * @see        java.lang.Float#toString(float)
     */
    public static void print(float f) {
        print(Float.valueOf(f));
    }


    /**
     * Prints a double-precision floating-point number.  The string produced by
     * <code>{@link java.lang.String#valueOf(double)}</code> is sent to BTrace client
     * for "printing".
     *
     * @param      d   The <code>double</code> to be printed
     * @see        java.lang.Double#toString(double)
     */
    public static void print(double d) {
        print(Double.valueOf(d));
    }

    /**
     * Prints the given object and then prints a newline
     */
    public static void println(Object obj) {
        println(obj != null ? obj.toString() : null);
    }

    /**
     * Prints a boolean and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(boolean)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param b  The <code>boolean</code> to be printed
     */

    public static void println(boolean b) {
        println(Boolean.valueOf(b));
    }

    /**
     * Prints a character and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(char)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param c  The <code>char</code> to be printed.
     */
    public static void println(char c) {
        println(Character.valueOf(c));
    }

    /**
     * Prints an integer and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(int)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param i  The <code>int</code> to be printed.
     */
    public static void println(int i) {
        println(Integer.valueOf(i));
    }

    /**
     * Prints a long and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(long)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param l  a The <code>long</code> to be printed.
     */
    public static void println(long l) {
        println(Long.valueOf(l));
    }


    /**
     * Prints a float and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(float)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param f  The <code>float</code> to be printed.
     */
    public static void println(float f) {
        println(Float.valueOf(f));
    }


    /**
     * Prints a double and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(double)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param d  The <code>double</code> to be printed.
     */
    public static void println(double d) {
        println(Double.valueOf(d));
    }

    /**
     * Terminates the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     */
    public static void println() {
        println("");
    }
    
    /**
     * Prints the given Map.
     *
     * @param name - the name of the map
     * @param data - the map data
     */
    public static void printStringMap(final String name, final Map<String, String> data) {
        l.send(StringMapDataCommand.class, new AbstractCommand.Initializer<StringMapDataCommand>() {

            public void init(StringMapDataCommand cmd) {
                cmd.setName(name);
                cmd.setPayload(data);
            }
        });
    }
    
    /**
     * Prints a number.
     *
     * @param name - name of the number data
     * @param value - value of the numerical data
     */
    public static void printNumber(final String name, final Number value) {
        l.send(NumberDataCommand.class, new AbstractCommand.Initializer<NumberDataCommand>() {
            public void init(NumberDataCommand cmd) {
                cmd.setName(name);
                cmd.setPayload(value);
            }
        });
    }

    /**
     * Prints the given Map.
     *
     * @param name - the name of the map
     * @param data - the map data
     */
    public static void printNumberMap(final String name, final Map<String, ? extends Number> data) {
        l.send(NumberMapDataCommand.class, new AbstractCommand.Initializer<NumberMapDataCommand>() {
            public void init(NumberMapDataCommand cmd) {
                cmd.setName(name);
                cmd.setPayload(data);
            }
        });
    }
    
    /**
     * Prints the elements of the given array as comma
     * separated line bounded by '[' and ']'.
     */
    public static void printArray(Object[] array) {
        StringBuilder buf = new StringBuilder();
        buf.append('[');
        for (Object obj : array) {
            buf.append(obj != null ? obj.toString() : NULL_MSG);
            buf.append(", ");
        }
        buf.append(']');
        println(buf.toString());
    }
}
