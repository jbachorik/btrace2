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

package net.java.btrace.wireio.commands;

import net.java.btrace.api.wireio.AbstractCommand;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;

/**
 * Transfer an instrumentation request
 * @author A.Sundararajan
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
final public class InstrumentCommand extends AbstractCommand {
    final private static String[] EMPTY_ARGS = new String[0];
    private byte[] code = new byte[0];
    private String[] args = EMPTY_ARGS;

    public InstrumentCommand(int typeId, int rx, int tx) {
        super(typeId, rx, tx);
    }

    @Override
    final public boolean canBeSpeculated() {
        return false;
    }

    @Override
    final public boolean needsResponse() {
        return true;
    }

    @Override
    final public void write(ObjectOutput out) throws IOException {
        out.writeInt(code != null ? code.length : 0);
        out.write(code != null ? code : new byte[0]);
        out.writeInt(args != null ? args.length : 0);
        if (args != null) {
            for (String a : args) {
                out.writeUTF(a);
            }
        }
    }

    @Override
    final public void read(ObjectInput in) throws IOException {
        int len = in.readInt();
        code = new byte[len];
        in.readFully(code);
        len = in.readInt();
        args = new String[len];
        for (int i = 0; i < len; i++) {
            args[i] = in.readUTF();
        }
    }

    final public byte[] getCode() {
        return code;
    }

    final public String[] getArguments() {
        return args;
    }

    final public void setArgs(String[] args) {
        this.args = args != null ? args : EMPTY_ARGS;
    }

    final public void setCode(byte[] code) {
        this.code = code;
    }
}
