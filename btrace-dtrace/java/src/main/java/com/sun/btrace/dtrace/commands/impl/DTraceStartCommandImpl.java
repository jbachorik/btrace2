/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.btrace.dtrace.commands.impl;

import com.sun.btrace.api.wireio.Command;
import com.sun.btrace.api.wireio.CommandContext;
import com.sun.btrace.dtrace.commands.DTraceStartCommand;
import com.sun.btrace.spi.wireio.CommandImpl;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=DTraceStartCommand.class)
public class DTraceStartCommandImpl extends CommandImpl<DTraceStartCommand> {
    @Override
    public void execute(CommandContext ctx, DTraceStartCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println("*** DTrace consumer started");
        }
    }
}
