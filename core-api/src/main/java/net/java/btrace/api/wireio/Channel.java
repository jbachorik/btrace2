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

import net.java.btrace.api.core.BTraceLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The representation of the communication channel.
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 * @since 2.0
 */
abstract public class Channel {
    /**
     * <b>Closed</b> flag
     */
    protected final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AtomicBoolean isInited = new AtomicBoolean(false);

    final private ConcurrentHashMap<Integer, ResponseHandler> responseMap = new ConcurrentHashMap<Integer, ResponseHandler>();
    final private BlockingQueue<AbstractCommand> commandQueue = new ArrayBlockingQueue<AbstractCommand>(1280000);

    private Thread delayedWriteService = null;

    protected Channel(boolean useDelayedWrite) {
        if (useDelayedWrite) {
            delayedWriteService = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!isClosed.get()) {
                            try {
                                AbstractCommand cmd = commandQueue.poll(1, TimeUnit.SECONDS);
                                if (cmd != null) {
                                    writeCommand(cmd);
                                }
                            } catch (InterruptedException e) {}
                        }
                    } catch (IOException e) {
                        BTraceLogger.debugPrint(e);
                    }
                }
            }, "BTrace Delayed Writer");
            delayedWriteService.setDaemon(true);
        }
    }

    private <V> ResponseHandler<V> addResponseHandler(AbstractCommand cmd) {
        if (cmd.needsResponse()) {
            ResponseHandler<V> response = new ResponseHandler<V>();
            responseMap.put(cmd.getRx(), response);
            return response;
        }
        return null;
    }

    /**
     *
     * @return Returns the associated {@linkplain CommandFactory}
     */
    abstract protected CommandFactory getCommandFactory();

    /**
     * Reads and returns the next {@linkplain AbstractCommand} from the pipeline.
     * @return Returns the next deserialized {@linkplain AbstractCommand}
     * @throws IOException
     * @throws ClassNotFoundException
     */
    abstract public AbstractCommand readCommand() throws IOException, ClassNotFoundException;

    /**
     * Submits the {@linkplain AbstractCommand} to the pipeline
     * @param cmd The {@linkplain AbstractCommand} to write
     * @throws IOException
     */
    abstract public void writeCommand(AbstractCommand cmd) throws IOException;

    /**
     * Perform the close operation.
     * To be overridden.
     */
    abstract protected void doClose();

    /**
     * Startup the channel
     */
    final protected void init() {
        if (isInited.compareAndSet(false, true)) {
            if (delayedWriteService != null) {
                delayedWriteService.start();
            }
        }
    }

    /**
     * Closes the communication channel
     */
    final public void close() {
        if (isClosed.compareAndSet(false, true)) {
            delayedWriteService.interrupt();
            try {
                // drain the queue
                Collection<AbstractCommand> drainage = new ArrayList<AbstractCommand>();
                commandQueue.drainTo(drainage);
                for (AbstractCommand cmd : drainage) {
                    writeCommand(cmd);
                }
            } catch (IOException e) {
                BTraceLogger.debugPrint(e);
            }
            for(ResponseHandler rh : responseMap.values()) {
                rh.setResponse(null);
            }
            doClose();
        }
    }

    /**
     * Creates and sends a command of the given type
     * @param <T> The command type type
     * @param <V> The response type type
     * @param clz The command type class
     * @return Returns an asynchronous {@linkplain Response}
     * @throws IOException
     */
    final public <T extends AbstractCommand, V> Response<V> sendCommand(Class<? extends T> clz) throws IOException {
        return sendCommand(clz, null);
    }

    /**
     * Creates and sends a command of the given type initialised by the given initialiser
     * @param <T> The command type type
     * @param <V> The response type type
     * @param clz The command type class
     * @param init The initialisation closure
     * @return Returns an asynchronous {@linkplain Response}
     * @throws IOException
     */
    final public <T extends AbstractCommand, V> Response<V> sendCommand(Class<? extends T> clz, AbstractCommand.Initializer<T> init) throws IOException {
        if (isClosed.get()) {
            return Response.NULL;
        }
        T cmd = prepareCommand(clz, init);
        if (cmd != null) {
            try {
                ResponseHandler<V> rslt = addResponseHandler(cmd);
                commandQueue.put(cmd);
                return rslt;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            BTraceLogger.debugPrint("can not instantiate " + clz.getName() + " command!");
        }
        return Response.NULL;
    }

    /**
     * Sends a response of the given type
     * @param <T> The response type type
     * @param cmd The {@linkplain AbstractCommand} to link the response to
     * @param clz The response type
     * @param data The response payload
     * @throws IOException
     */
    final public <T> void sendResponse(AbstractCommand cmd, Class<? extends DataCommand<T>> clz, T data) throws IOException {
        if (isClosed.get()) {
            return;
        }

        AbstractCommand response = getCommandFactory().createResponse(data, clz, cmd.getRx());
        if (response != null) {
            try {
                commandQueue.put(response);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            BTraceLogger.debugPrint("can not instantiate " + clz + " command!");
        }
    }

    /**
     * Creates a command of the given type and with the given initialiser
     * @param <T> The command type type
     * @param clz The command type class
     * @param init The initialiser closure
     * @return A new instance of {@linkplain AbstractCommand}
     */
    final public <T extends AbstractCommand> T prepareCommand(Class<? extends T> clz, AbstractCommand.Initializer<T> init) {
        T cmd = getCommandFactory().createCommand(clz);
        if (cmd != null && init != null) {
            init.init(cmd);
        }
        return cmd;
    }

    /**
     * Response-received hook
     * @param <T> The response command type
     * @param cmd The response command received
     */
    final public <T> void responseReceived(DataCommand<T> cmd) {
        ResponseHandler<T> t = responseMap.get(cmd.getTx());
        t.setResponse(cmd.getPayload());
    }

    /**
     *
     * @return The {@linkplain ClassLoader} used by this channel
     */
    protected static ClassLoader getMyLoader() {
        ClassLoader myLoader = Channel.class.getClassLoader();
        return myLoader != null ? myLoader : ClassLoader.getSystemClassLoader();
    }
}
