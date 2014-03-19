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
import java.io.EOFException;
import net.java.btrace.api.extensions.ExtensionsRepository;
import java.io.IOException;
import java.io.ObjectInput;

/**
 * A {@linkplain Channel} implementation using sockets for communication
 * @author Jaroslav Bachorik
 */
abstract public class SocketChannel extends Channel {
    final public static SocketChannel NULL = new SocketChannel(null, null, null) {
        @Override
        protected boolean handshake() {
            return false;
        }
    };
    
    final protected static String BTRACE_MAGIC="BTRACE";
    
    final protected ObjectInput input;
    final protected ObjectOutput  output;
    private CommandFactory cFactory;
    final protected ExtensionsRepository extRep;
    
    protected SocketChannel(ObjectInput oi, ObjectOutput  oo, ExtensionsRepository extRep) {
        super(oi != null && oo != null);
        this.input = oi;
        this.output = oo;
        this.extRep = extRep;
    }
    
    @Override
    final public AbstractCommand readCommand() throws IOException, ClassNotFoundException {
        if (input == null) return null;
        try {
            while (true) {
                int id = input.readInt();
                int rx = input.readInt();
                int tx = input.readInt();
                AbstractCommand c = cFactory.restoreCommand(id, rx, tx);
                c.read(input);
                if (c instanceof ResponseCommand) { // implicitly process the response
                    responseReceived((ResponseCommand)c);
                    continue;
                }
                return c;
            }
        } catch (EOFException e) {
            throw e;
        } catch (IOException e) {
            close();
        }
        return AbstractCommand.NULL;
    }
    
    @Override
    final public void writeCommand(AbstractCommand cmd) throws IOException {
        if (output == null) throw new IOException("output command channel not available");
        try {
            output.writeInt(cmd.getType());
            output.writeInt(cmd.getRx());
            output.writeInt(cmd.getTx());
            cmd.write(output);
            output.flush();
        } catch (IOException e) {
            close();
        }
    }
    
    @Override
    final public CommandFactory getCommandFactory() {
        return cFactory;
    }
    
    final protected void setCommandFactory(CommandFactory cf) {
        this.cFactory = cf;
    }
    
    @Override
    final public void doClose() {
        try {
            if (isClosed.compareAndSet(false, true)) {
                input.close();
                output.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }
    
    /**
     * Preforms the handshake
     * @return <b>TRUE</b> if handshake succeeded, <b>FALSE</b> otherwise
     */
    abstract protected boolean handshake();
}
