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
package net.java.btrace.compiler;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;

/** 
 * This code is based on PCPP code from the GlueGen project.
 * 
 * A Reader implementation which finds lines ending in the backslash
 * character ('\') and concatenates them with the next line. 
 * 
 * @author Kenneth B. Russell (original author)
 * @author A. Sundararajan (changes documented below)
 * 
 * Changes:
 * 
 *     * Changed the package name.
 *     * Formatted with NetBeans.
 */
public class ConcatenatingReader extends FilterReader {
    // Any leftover characters go here
    private char[] curBuf;
    private int curPos;
    private BufferedReader in;
    private static String newline = System.getProperty("line.separator");

    /** This class requires that the input reader be a BufferedReader so
    it can do line-oriented operations. */
    public ConcatenatingReader(BufferedReader in) {
        super(in);
        this.in = in;
    }

    public int read() throws IOException {
        char[] tmp = new char[1];
        int num = read(tmp, 0, 1);
        if (num < 0) {
            return -1;
        }
        return tmp[0];
    }

    // It's easier not to support mark/reset since we don't need it
    public boolean markSupported() {
        return false;
    }

    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public boolean ready() throws IOException {
        if (curBuf != null || in.ready()) {
            return true;
        }
        return false;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        if (curBuf == null) {
            nextLine();
        }

        if (curBuf == null) {
            return -1;
        }

        int numRead = 0;

        while ((len > 0) && (curBuf != null) && (curPos < curBuf.length)) {
            cbuf[off] = curBuf[curPos];
            ++curPos;
            ++off;
            --len;
            ++numRead;
            if (curPos == curBuf.length) {
                nextLine();
            }
        }

        return numRead;
    }

    public long skip(long n) throws IOException {
        long numSkipped = 0;

        while (n > 0) {
            int intN = (int) n;
            char[] tmp = new char[intN];
            int numRead = read(tmp, 0, intN);
            n -= numRead;
            numSkipped += numRead;
            if (numRead < intN) {
                break;
            }
        }
        return numSkipped;
    }

    private void nextLine() throws IOException {
        String cur = in.readLine();
        if (cur == null) {
            curBuf = null;
            return;
        }
        // The trailing newline was trimmed by the readLine() method. See
        // whether we have to put it back or not, depending on whether the
        // last character of the line is the concatenation character.
        int numChars = cur.length();
        boolean needNewline = true;
        if ((numChars > 0) &&
                (cur.charAt(cur.length() - 1) == '\\')) {
            --numChars;
            needNewline = false;
        }
        char[] buf = new char[numChars + (needNewline ? newline.length() : 0)];
        cur.getChars(0, numChars, buf, 0);
        if (needNewline) {
            newline.getChars(0, newline.length(), buf, numChars);
        }
        curBuf = buf;
        curPos = 0;
    }

    // Test harness
  /*
    public static void main(String[] args) throws IOException {
    if (args.length != 1) {
    System.out.println("Usage: java ConcatenatingReader [file name]");
    System.exit(1);
    }
    ConcatenatingReader reader = new ConcatenatingReader(new BufferedReader(new FileReader(args[0])));
    OutputStreamWriter writer = new OutputStreamWriter(System.out);
    char[] buf = new char[8192];
    boolean done = false;
    while (!done && reader.ready()) {
    int numRead = reader.read(buf, 0, buf.length);
    writer.write(buf, 0, numRead);
    if (numRead < buf.length)
    done = true;
    }
    writer.flush();
    }
     */
}
