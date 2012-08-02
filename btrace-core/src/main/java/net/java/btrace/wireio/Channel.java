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
package net.java.btrace.wireio;

import net.java.btrace.api.wireio.CommandFactory;
import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.wireio.Response;
import net.java.btrace.api.wireio.ResponseCommand;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Jaroslav Bachorik
 */
abstract public class Channel {
    protected final AtomicBoolean isClosed = new AtomicBoolean(false);
    private static class ResponseHandler<T> implements Response<T> {
        final private CountDownLatch latch = new CountDownLatch(1);
        
        volatile private T data;
        
        private ResponseHandler() {}
        
        private void setResponse(T data) {
            this.data = data;
            latch.countDown();
        }
        
        public T get() throws InterruptedException {
            latch.await();
            return data;
        }
        
        public T get(long timeout) throws InterruptedException {
            latch.await(timeout, TimeUnit.MILLISECONDS);
            return data;
        }
    }
    
    final private ConcurrentHashMap<Integer, ResponseHandler> responseMap = new ConcurrentHashMap<Integer, ResponseHandler>();
    final private BlockingQueue<AbstractCommand> commandQueue = new ArrayBlockingQueue<AbstractCommand>(128000);
    
    private Thread delayedWriteService = new Thread(new Runnable() {

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
                e.printStackTrace();
            }
        }
    }, "BTrace Delayed Writer");
    
    protected Channel(boolean startDelayedWrite) {
        if (startDelayedWrite) {
            delayedWriteService.setDaemon(true);
            delayedWriteService.start();
        } else {
            delayedWriteService = null;
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
    
    abstract protected CommandFactory getCommandFactory();

    abstract public AbstractCommand readCommand() throws IOException, ClassNotFoundException;

    abstract public void writeCommand(AbstractCommand cmd) throws IOException;
    
    abstract protected void doClose();
    
    final public void close() {
        if (isClosed.compareAndSet(false, true)) {
            delayedWriteService.interrupt();
            doClose();
        }
    }
    
    final public <T extends AbstractCommand, V> Response<V> sendCommand(Class<? extends T> clz) throws IOException {
        return sendCommand(clz, null);
    }
    
    final public <T extends AbstractCommand, V> Response<V> sendCommand(Class<? extends T> clz, AbstractCommand.Initializer<T> init) throws IOException {
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
    
    final public <T> void sendResponse(AbstractCommand cmd, T data) throws IOException {
        ResponseCommand<T> response = getCommandFactory().createResponse(data, cmd.getRx());
        if (response != null) {
            try {
                commandQueue.put(response);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            BTraceLogger.debugPrint("can not instantiate " + ResponseCommand.class + " command!");
        }
    }
    
    final public <T extends AbstractCommand> T prepareCommand(Class<? extends T> clz, AbstractCommand.Initializer<T> init) {
        T cmd = getCommandFactory().createCommand(clz);
        if (cmd != null && init != null) {
            init.init(cmd);
        }
        return cmd;
    }
    
    final public <T> void responseReceived(ResponseCommand<T> cmd) {
        ResponseHandler<T> t = responseMap.get(cmd.getTx());
        t.setResponse(cmd.getPayload());
    }
    
    protected static ClassLoader getMyLoader() {
        ClassLoader myLoader = Channel.class.getClassLoader();
        return myLoader != null ? myLoader : ClassLoader.getSystemClassLoader();
    }
}
