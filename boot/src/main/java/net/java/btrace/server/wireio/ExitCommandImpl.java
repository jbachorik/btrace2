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

import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.spi.wireio.CommandImpl;
import net.java.btrace.api.wireio.Channel;
import net.java.btrace.api.server.Session;
import net.java.btrace.wireio.commands.ExitCommand;
import java.io.IOException;
import net.java.btrace.wireio.commands.ACKCommand;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz = ExitCommand.class, target = Command.Target.SERVER)
public class ExitCommandImpl extends CommandImpl<ExitCommand> {
    @Override
    public void execute(Lookup ctx, final ExitCommand cmd) {
        BTraceLogger.debugPrint("received exit command with exit code " + cmd.getExitCode());
        final Channel ch = ctx.lookup(Channel.class);
        if (ch != null) {
            Session s = ctx.lookup(Session.class);
            if (s != null) {
                BTraceLogger.debugPrint("detaching session");
                s.detach(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ch.sendResponse(cmd, ACKCommand.class, true);
                        } catch (IOException e) {
                            BTraceLogger.debugPrint(e);
                        } finally {
                            ch.close();
                        }
                    }
                });
            } else {
                BTraceLogger.debugPrint("sending ACK to exit");
                // no session to detach; just ACK exitting
                try {
                    ch.sendResponse(cmd, ACKCommand.class, true);
                } catch (IOException e) {
                    BTraceLogger.debugPrint(e);
                } finally {
                    ch.close();
                }
            }
        }
    }
}
