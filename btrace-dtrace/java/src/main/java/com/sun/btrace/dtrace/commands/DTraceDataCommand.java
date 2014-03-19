/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.btrace.dtrace.commands;

import com.sun.btrace.api.wireio.AbstractCommand;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.DataOutput;
import org.opensolaris.os.dtrace.DataEvent;

/**
 * Command to represent data event from DTrace.
 * 
 * @author A. Sundararajan
 * @author Jaroslav Bachorik
 */
public class DTraceDataCommand extends AbstractCommand {
    private DataEvent de;

    public DTraceDataCommand(int type, int rx, int tx) {
        super(type, rx, tx);
    }
    
    @Override
    final public void write(ObjectOutput out) throws IOException {
        out.writeObject(de);
    }

    @Override
    final public void read(ObjectInput in) throws ClassNotFoundException, IOException {
        de = (DataEvent) in.readObject();
    }
    
    /**
     * Returns the underlying DTrace DataEvent.
     */
    final public DataEvent getDataEvent() {
        return de;
    }
    
    final public void setDataEvent(DataEvent de) {
        this.de = de;
    }
}
