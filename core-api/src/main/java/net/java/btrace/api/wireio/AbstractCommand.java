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

import java.io.ObjectOutput ;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.spi.wireio.CommandImpl;
import java.io.IOException;
import java.io.ObjectInput;

/**
 * Represents the base class for command types.
 * <p>
 * A new command type is created by extending this class. Command types are
 * used to allow decoupling of the command implementation (eg. client vs. server)
 * </p>
 * <p>
 * A specific implementation of a command type will extend {@linkplain CommandImpl}
 * with the type parameter of the command type class to be extended. Also, an {@linkplain Command}
 * annotation is necessary to overcome generics erasure.
 * </p>
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 * @since 2.0
 */
public abstract class AbstractCommand {
    transient final private int type;
    transient final private int rx, tx;

    private final transient CommandImpl impl = CommandImpl.NULL;

    public static final AbstractCommand NULL = new AbstractCommand(-1, 1, -1) {
        @Override
        public void write(ObjectOutput  out) throws IOException {
        }

        @Override
        public void read(ObjectInput in) throws ClassNotFoundException, IOException {
        }
    };

    public static interface Initializer<T extends AbstractCommand> {
        void init(T cmd);
    }

    public AbstractCommand(int type, int rx, int tx) {
        this.type = type;
        this.rx = rx;
        this.tx = tx;
    }

    /**
     *
     * @return Internal type ID
     */
    final public int getType() {
        return type;
    }

    /**
     *
     * @return Internal RX counter
     */
    final public int getRx() {
        return rx;
    }

    /**
     *
     * @return Internal TX counter
     */
    final public int getTx() {
        return tx;
    }

    /**
     * Can this command be placed on speculation queue?
     * @return Returns <b>TRUE</b> if the command can be speculated, <b>FALSE</b> otherwise
     */
    public boolean canBeSpeculated() {
        return true;
    }

    /**
     * Executes the command with the given context
     * @param ctx The execution context - a command can use it to search for specific services and information
     */
    final public void execute(Lookup ctx) {
        impl.execute(ctx, this);
    }

    /**
     * Sync/Async command
     * @return Returns <b>TRUE</b> if the command is synchronous in nature, <b>FALSE</b> otherwise
     */
    public boolean needsResponse() {
        return false;
    }

    /**
     * Serializes the command.
     * To be overridden by subclasses.
     * @param out The output to write the command contents to
     * @throws IOException
     */
    abstract public void write(ObjectOutput  out) throws IOException;
    /**
     * De-serializes the command.
     * To be overridden by subclasses
     * @param in The input to read the command contents from
     * @throws ClassNotFoundException
     * @throws IOException
     */
    abstract public void read(ObjectInput in) throws ClassNotFoundException, IOException;
}
