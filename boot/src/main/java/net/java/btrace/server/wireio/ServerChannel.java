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
import net.java.btrace.api.wireio.ObjectInputStreamEx;
import net.java.btrace.api.wireio.SocketChannel;
import net.java.btrace.api.wireio.Version;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput ;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import net.java.btrace.api.wireio.Command;

/**
 *
 * @author Jaroslav Bachorik
 */
public final class ServerChannel extends SocketChannel {

    protected ServerChannel(ObjectInput oi, ObjectOutput  oo, ExtensionsRepository extRep) {
        super(oi, oo, extRep);
    }

    public static Channel open(Socket skt, final ExtensionsRepository extRep) {
        try {
            ObjectInputStream ois = new ObjectInputStreamEx(skt.getInputStream(), extRep.getClassLoader());
            ObjectOutput  oos = new ObjectOutputStream(skt.getOutputStream());
            ServerChannel ch = new ServerChannel(ois, oos, extRep);
            if (!ch.handshake()) {
                try {
                    ch.output.close();
                    ch.input.close();
                } catch (IOException e) {
                }
                return null;
            }
            return ch;
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    protected boolean handshake() {
        try {
            init();
            String magic = input.readUTF();
            if (BTRACE_MAGIC.equals(magic)) {
                output.writeUTF(BTRACE_MAGIC);
                output.writeInt(Version.MAJOR);
                output.writeInt(Version.MINOR);
                output.flush();
                boolean cont = input.readBoolean();
                if (cont) {
                    int commandCnt = input.readInt();
                    boolean cmdFactoriesOk = false;
                    try {
                        Class<? extends AbstractCommand>[] mapper = new Class[commandCnt];
                        for (int i = 0; i < commandCnt; i++) {
                            String cmdClass = input.readUTF();
                            Class cmdClz = Class.forName(cmdClass);
                            mapper[i] = cmdClz;
                        }
                        CommandFactory cf = CommandFactory.getInstance(mapper, extRep.getClassLoader(getMyLoader()), Command.Target.SERVER);
                        List<Class<? extends AbstractCommand>> cmds  = cf.listSupportedCommands();
                        output.writeInt(cmds.size() - commandCnt);
                        for(int i=commandCnt;i<cmds.size();i++) {
                            output.writeUTF(cmds.get(i).getName());
                        }
                        output.flush();
                        if (input.readBoolean()) {
                            setCommandFactory(cf);
                            cmdFactoriesOk = true;
                        }
                    } catch (IOException e) {
                        cmdFactoriesOk = false;
                    } catch (ClassNotFoundException e) {
                        cmdFactoriesOk = false;
                    }
                    output.writeBoolean(cmdFactoriesOk);
                    output.flush();
                    return cmdFactoriesOk;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                output.flush();
            } catch (IOException e) {
            }
        }
        return false;
    }
}
