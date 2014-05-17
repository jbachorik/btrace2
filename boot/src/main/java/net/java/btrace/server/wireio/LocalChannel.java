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
package net.java.btrace.server.wireio;

import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.wireio.CommandFactory;
import net.java.btrace.api.wireio.Channel;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.wireio.ResponseCommand;

/**
 *
 * @author Jaroslav Bachorik
 */
abstract public class LocalChannel extends Channel {
    private final BlockingQueue<AbstractCommand> in, out;

    public static class Client extends LocalChannel {
        private final CommandFactory cFactory;

        public Client(BlockingQueue<AbstractCommand> in, BlockingQueue<AbstractCommand> out, ExtensionsRepository extRep) {
            super(in, out);
            cFactory = CommandFactory.getInstance(extRep.getClassLoader(getMyLoader()), Command.Target.SERVER);
            init();
        }

        @Override
        protected CommandFactory getCommandFactory() {
            return cFactory;
        }
    }

    public static class Server extends LocalChannel {
        private CommandFactory cFactory;
        public Server(BlockingQueue<AbstractCommand> in, BlockingQueue<AbstractCommand> out, ExtensionsRepository extRep) {
            super(in, out);
            cFactory = CommandFactory.getInstance(extRep.getClassLoader(getMyLoader()), Command.Target.SERVER);
            init();
        }

        @Override
        protected CommandFactory getCommandFactory() {
            return cFactory;
        }
    }

    private LocalChannel(BlockingQueue<AbstractCommand> in, BlockingQueue<AbstractCommand> out) {
        super(in != null && out != null);
        assert in != null;
        assert out != null;

        this.in = in;
        this.out = out;
    }

    @Override
    public void doClose() {
        // noop
    }

    @Override
    final public AbstractCommand readCommand() throws IOException, ClassNotFoundException {
        try {
            AbstractCommand cmd;
            while (true) {
                cmd = in.take();
                if (cmd instanceof ResponseCommand) { // implicitly process the response
                    responseReceived((ResponseCommand)cmd);
                    continue;
                }
                return cmd;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    final public void writeCommand(AbstractCommand cmd) throws IOException {
        try {
            out.put(cmd);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
