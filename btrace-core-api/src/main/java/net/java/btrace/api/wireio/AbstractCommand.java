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

import net.java.btrace.api.wireio.CommandContext;
import net.java.btrace.spi.wireio.CommandImpl;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public abstract class AbstractCommand implements Serializable {
    transient final private int type;
    transient final private int rx, tx;
    
    transient private CommandImpl impl = CommandImpl.NULL;
    
    public static final AbstractCommand NULL = new AbstractCommand(-1, 1, -1) {
        @Override
        public void write(ObjectOutput out) throws IOException {
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

    final public int getType() {
        return type;
    }

    final public int getRx() {
        return rx;
    }

    final public int getTx() {
        return tx;
    }
    
    public boolean canBeSpeculated() {
        return true;
    }
    
    final public void execute(CommandContext ctx) {
        impl.execute(ctx, this);
    }
    
    public boolean needsResponse() {
        return false;
    }
        
    abstract public void write(ObjectOutput out) throws IOException;
    abstract public void read(ObjectInput in) throws ClassNotFoundException, IOException;
}
