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
package net.java.btrace.client;

import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.wireio.CommandFactory;
import net.java.btrace.api.wireio.Channel;
import net.java.btrace.api.wireio.SocketChannel;
import net.java.btrace.api.wireio.Version;
import net.java.btrace.api.wireio.ObjectInputStreamEx;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * The client side of the BTrace communication channel
 * @author Jaroslav Bachorik
 * 
 * @since 2.0
 */
public final class ClientChannel extends SocketChannel {

    protected ClientChannel(ObjectInput oi, ObjectOutput oo, ExtensionsRepository extRep) {
        super(oi, oo, extRep);
    }

    public static Channel open(Socket skt, ExtensionsRepository extRep) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(skt.getOutputStream());
            ObjectInputStream ois = new ObjectInputStreamEx(skt.getInputStream(), extRep.getClassLoader());
            
            ClientChannel ch = new ClientChannel(ois, oos, extRep);
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
            output.writeUTF(BTRACE_MAGIC);
            output.flush();
            String magic = input.readUTF();
            if (BTRACE_MAGIC.equals(magic)) {
                int majVer = input.readInt();
                int minVer = input.readInt();
                if (majVer < Version.MAJOR || (majVer == Version.MAJOR && minVer <= Version.MINOR)) {
                    output.writeBoolean(true);
                    CommandFactory cf = CommandFactory.getInstance(extRep.getClassLoader(getMyLoader()));
                    List<Class<? extends AbstractCommand>> supportedCmds = cf.listSupportedCommands();
                    if (BTraceLogger.isDebug()) {
                        BTraceLogger.debugPrint("sending list of supported commands (" + supportedCmds.size() + ")");
                        for (Class<? extends AbstractCommand> clz : supportedCmds) {
                            BTraceLogger.debugPrint("\t*" + clz.getName());
                        }
                    }
                    int myCmdLen = supportedCmds.size();
                    output.writeInt(myCmdLen);
                    for (Class<? extends AbstractCommand> cmdType : supportedCmds) {
                        output.writeUTF(cmdType.getName());
                    }
                    output.flush();
                    int cmdLen = input.readInt();
                    Class<? extends AbstractCommand>[] serverCmds = null;
                    try {
                        serverCmds = new Class[cmdLen];
                        for (int i = 0; i < cmdLen; i++) {
                            String cmdClass = input.readUTF();
                            Class cmdClz = Class.forName(cmdClass);
                            if (!supportedCmds.contains(cmdClz)) {
                                serverCmds[i] = cmdClz;
                            }
                        }
                        cf.addMapper(serverCmds);
                        setCommandFactory(cf);
                        output.writeBoolean(true);                        
                    } catch (ClassNotFoundException e) {
                        output.writeBoolean(false);
                    }
                    output.flush();
                    return input.readBoolean();
                }
                output.writeBoolean(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return false;
    }
    
}
