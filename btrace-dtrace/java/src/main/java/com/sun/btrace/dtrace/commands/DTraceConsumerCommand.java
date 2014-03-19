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

package com.sun.btrace.dtrace.commands;

import com.sun.btrace.api.wireio.AbstractCommand;
import java.io.ObjectInput;
import java.io.DataOutput;
import java.io.IOException;
import org.opensolaris.os.dtrace.Consumer;
import org.opensolaris.os.dtrace.ConsumerEvent;

/**
 * Abstract command to represent consumer event from DTrace.
 *
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
public abstract class DTraceConsumerCommand extends AbstractCommand {
    private ConsumerEvent ce;

    public DTraceConsumerCommand(int type, int rx, int tx) {
        super(type, rx, tx);
    }

    /**
     * Returns the underlying DTrace ConsumerEvent.
     */
    final public ConsumerEvent getConsumerEvent() {
        return ce;
    }
    
    final public void setConsumerEvent(ConsumerEvent ce) {
        this.ce = ce;
    }

    /**
     * Returns the Consumer object.
     */
    final public Consumer getConsumer() {
        return (Consumer) ce.getSource();
    }

    @Override
    final public void read(ObjectInput in) throws ClassNotFoundException, IOException {
        ce = (ConsumerEvent) in.readObject();
    }

    @Override
    final public void write(ObjectOutput out) throws IOException {
        out.writeObject(ce);
    }
}
