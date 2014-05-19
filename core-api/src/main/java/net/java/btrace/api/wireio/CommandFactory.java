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
import net.java.btrace.api.core.ServiceLocator;
import net.java.btrace.spi.wireio.CommandImpl;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A factory for instances of {@linkplain AbstractCommand}.
 * @author Jaroslav Bachorik
 * @since 2.0
 */
public class CommandFactory {
    private static class FactoryMethod<T extends AbstractCommand> {
        private CommandImpl<T> impl;
        private final Constructor<T> constructor;
        private static Field implFld;
        private final int type;

        static {
            try {
                implFld = AbstractCommand.class.getDeclaredField("impl");
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        implFld.setAccessible(true);
                        return null;
                    }
                });
            } catch (NoSuchFieldException e) {
                implFld = null;
            } catch (SecurityException e) {
                implFld = null;
            }
        }

        public FactoryMethod(CommandImpl<T> impl, Constructor<T> constructor, int type) {
            this.constructor = constructor;
            this.type = type;
            this.constructor.setAccessible(true);
            updateImpl(impl);
        }

        private void updateImpl(CommandImpl<T> impl) {
            this.impl = impl;
        }

        public T newInstance(int rx, int tx) {
            try {
                T instance = constructor.newInstance(type, rx, tx);
                if (implFld != null) {
                    implFld.set(instance, impl);
                }
                return instance;
            } catch (InstantiationException e) {
                e.printStackTrace(System.err);
            } catch (IllegalAccessException e) {
                e.printStackTrace(System.err);
            } catch (IllegalArgumentException e) {
                e.printStackTrace(System.err);
            } catch (InvocationTargetException e) {
                e.printStackTrace(System.err);
            }
            return null;
        }
    }

    private final Map<Integer, FactoryMethod> mapById = new HashMap();
    private final Map<Class<? extends AbstractCommand>, FactoryMethod> mapByType = new WeakHashMap();

    private List<Class<? extends AbstractCommand>> supportedCommands = null;

    private int lastTypeId = 0;
    final private static int MAX_SEQ_NR = 100000;
    private final AtomicInteger rxCntr = new AtomicInteger(0);
    private final Command.Target target;

    private CommandFactory(Iterable<CommandImpl> svcs, Command.Target target) throws NoSuchMethodException {
        this(Collections.EMPTY_MAP, svcs, target);
    }

    private CommandFactory(Class<? extends AbstractCommand>[] mapping, Iterable<CommandImpl> svcs, Command.Target target) throws NoSuchMethodException {
        this(createMapper(mapping), svcs, target);
    }

    private CommandFactory(Map<Class<? extends AbstractCommand>, Integer> mapper, Iterable<CommandImpl> svcs, Command.Target target) throws NoSuchMethodException {
        this.target = target;
        copyInitialMapper(mapper);
        applyMapper(svcs, mapper);
    }

    private void applyMapper(Iterable<CommandImpl> svcs, Map<Class<? extends AbstractCommand>, Integer> mapper) throws SecurityException, NoSuchMethodException {
        int cnt = lastTypeId;
        for(CommandImpl svc : svcs) {
            if (svc == null) continue;

            Command ann = svc.getClass().getAnnotation(Command.class);
            if (ann != null) {
                if (ann.target() != Command.Target.BOTH &&
                    ann.target() != target) continue;

                Class<? extends AbstractCommand> cmdClz = ann.clazz();
                if (!mapByType.containsKey(cmdClz)) {
                    Constructor<? extends AbstractCommand> constructor = cmdClz.getDeclaredConstructor(int.class, int.class, int.class);
                    constructor.setAccessible(true);

                    Integer cmdId = mapper.get(cmdClz);
                    if (cmdId == null) {
                        cmdId = cnt++;
                    }
                    FactoryMethod fm = new FactoryMethod(svc, constructor, cmdId);
                    mapById.put(cmdId, fm);
                    mapByType.put(cmdClz, fm);

                    lastTypeId = cnt;
                } else {
                    mapByType.get(cmdClz).updateImpl(svc);
                }
            }
        }
    }

    private void copyInitialMapper(Map<Class<? extends AbstractCommand>, Integer> mapper) throws NoSuchMethodException, SecurityException {
        for(Map.Entry<Class<? extends AbstractCommand>, Integer> mapping : mapper.entrySet()) {
            Constructor<? extends AbstractCommand> constructor = mapping.getKey().getDeclaredConstructor(int.class, int.class, int.class);
            constructor.setAccessible(true);
            FactoryMethod fm = new FactoryMethod(CommandImpl.NULL, constructor, mapping.getValue());
            int id = mapping.getValue();
            lastTypeId = Math.max(lastTypeId, id + 1);
            mapById.put(mapping.getValue(), fm);
            mapByType.put(mapping.getKey(), fm);
        }
    }

    /**
     * Creates a factory with the given classloader
     * @param cl The {@linkplain ClassLoader} to use
     * @param target Specifies whether this factory applies to Server or Client commands
     * @return Returns a new instance of {@linkplain CommandFactory} or <b>NULL</b>
     */
    public static CommandFactory getInstance(ClassLoader cl, Command.Target target) {
        try {
            Iterable<CommandImpl> rslt = ServiceLocator.listServices(CommandImpl.class, cl);
            if (rslt != null) {
                return new CommandFactory(rslt, target);
            }
        } catch (NoSuchMethodException e) {
        }
        return null;
    }

    /**
     * Creates a factory with the given classloader and a custom command implementation mapping
     * @param mapping The command implementation mapping list; the position is the key
     * @param cl The {@linkplain ClassLoader} to use
     * @param target Specifies whether this factory applies to Server or Client commands
     * @return Returns a new instance of {@linkplain CommandFactory} or <b>NULL</b>
     */
    public static CommandFactory getInstance(Class<? extends AbstractCommand>[] mapping, ClassLoader cl, Command.Target target) {
        ServiceLocator.listServiceNames(CommandImpl.class, cl);
        try {
            Iterable<CommandImpl> rslt = ServiceLocator.listServices(CommandImpl.class, cl);
            if (rslt != null) {
                return new CommandFactory(mapping, rslt, target);
            }
        } catch (NoSuchMethodException e) {
        }
        return null;
    }

    /**
     * Lists all the supported commands
     * @return The list of all the supported commands
     */
    synchronized public List<Class<? extends AbstractCommand>> listSupportedCommands() {
        if (supportedCommands == null) {
            Class<? extends AbstractCommand>[] supported = new Class[mapByType.size()];
            for(Map.Entry<Class<? extends AbstractCommand>, FactoryMethod> entry : mapByType.entrySet()) {
                supported[entry.getValue().type] = entry.getKey();
            }
            supportedCommands = Arrays.asList(supported);
        }
        return supportedCommands;
    }

    /**
     * Creates a new command of <b>&lt;T&gt;</b> type with an appropriate handler.
     * @param <T> Type parameter for the command type
     * @param cmdClass The command type class
     * @return A new instance of <b>&lt;T&gt;</b> or <b>NULL</b> if the factory can not handle this type
     */
    public <T extends AbstractCommand> T createCommand(Class<T> cmdClass) {
        FactoryMethod<T> fm = mapByType.get(cmdClass);
        if (fm != null) {
            T cmd = fm.newInstance(incCounter(), -1);
            return cmd;
        }
        return null;
    }

    /**
     * Creates a new response of <b>&lt;T&gt;</b> type with an appropriate handler.
     * @param <T> Type parameter for the response type
     * @param data The response type class
     * @param clz The command class
     * @param tx The TX value of the command to create the response for
     * @return A new instance of <b>&lt;T&gt;</b> or <b>NULL</b> if the factory can not handle this type
     */
    public <T> DataCommand<T> createResponse(T data, Class<? extends DataCommand<T>> clz, int tx) {
        FactoryMethod<DataCommand<T>> fm = mapByType.get(clz);
        if (fm != null) {
            DataCommand<T> cmd = fm.newInstance(incCounter(), tx);
            cmd.setPayload(data);
            return cmd;
        }
        return null;
    }

    private int incCounter() {
        int cntr = rxCntr.getAndIncrement();
        if (cntr == MAX_SEQ_NR) {
            rxCntr.set(0);
        }
        return cntr;
    }

    /**
     * Restores the deserialized command
     *
     * @param type The command type id
     * @param rx The command RX
     * @param tx The command TX
     * @return Returns a deserialized command or <b>NULL</b>
     */
    public AbstractCommand restoreCommand(int type, int rx, int tx) {
        FactoryMethod fm = mapById.get(type);
        if (fm != null) {
            return fm.newInstance(rx, tx);
        }
        return null;
    }

    /**
     * Allows for ad-hoc addition of command type mappers
     * @param mapping The command implementation mapping list; the position is the key
     */
    public void addMapper(Class<? extends AbstractCommand>[] mapping) {
        int cnt = lastTypeId;
        for(Class<? extends AbstractCommand> cmdClz : mapping) {
            if (cmdClz == null) continue;

            try {
                Constructor<? extends AbstractCommand> constructor = cmdClz.getDeclaredConstructor(int.class, int.class, int.class);
                constructor.setAccessible(true);

                int id = cnt++;
                FactoryMethod fm = new FactoryMethod(CommandImpl.NULL, constructor, id);
                mapById.put(cnt, fm);
                mapByType.put(cmdClz, fm);
            } catch (NoSuchMethodException e) {
                BTraceLogger.debugPrint(e);
            } catch (SecurityException e) {
                BTraceLogger.debugPrint(e);
            }
        }
        lastTypeId = cnt;
    }

    private static Map<Class<? extends AbstractCommand>, Integer> createMapper(Class<? extends AbstractCommand>[] mapping) {
        Map<Class<? extends AbstractCommand>, Integer> tmpMap = new WeakHashMap<Class<? extends AbstractCommand>, Integer>();

        for(int i=0;i<mapping.length;i++) {
            tmpMap.put(mapping[i], i);
        }
        return tmpMap;
    }
}
