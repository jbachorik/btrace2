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

import java.io.ObjectOutput ;
import net.java.btrace.api.wireio.AbstractCommand;
import java.io.ObjectInput;
import java.io.IOException;

/**
 * A command indicating that the current session is about to end
 * @author A.Sundararajan
 * @author Jaroslav Bachorik
 */
final public class ExitCommand extends AbstractCommand {
    private int exitCode = 0;

    public ExitCommand(int typeId, int rx, int tx) {
        super(typeId, rx, tx);
    }

    /**
     * Exit command may never go on the speculation queue
     * @return FALSE
     */
    @Override
    final public boolean canBeSpeculated() {
        return false;
    }

    @Override
    final public void write(ObjectOutput  out) throws IOException {
        out.writeInt(exitCode);
    }

    @Override
    final public void read(ObjectInput in) throws IOException {
        exitCode = in.readInt();
    }

    /**
     *
     * @return The exit code
     */
    final public int getExitCode() {
        return exitCode;
    }

    final public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public boolean needsResponse() {
        return true;
    }
}
