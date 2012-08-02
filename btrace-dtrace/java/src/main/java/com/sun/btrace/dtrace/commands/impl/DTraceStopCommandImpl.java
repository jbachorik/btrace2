/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.btrace.dtrace.commands.impl;

import com.sun.btrace.api.wireio.Command;
import com.sun.btrace.api.wireio.CommandContext;
import com.sun.btrace.dtrace.commands.DTraceStopCommand;
import com.sun.btrace.spi.wireio.CommandImpl;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=DTraceStopCommand.class)
public class DTraceStopCommandImpl extends CommandImpl<DTraceStopCommand> {
    @Override
    public void execute(CommandContext ctx, DTraceStopCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println("*** DTrace consumer stopped");
        }
    }
}
