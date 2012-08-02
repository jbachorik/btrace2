/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.btrace.dtrace.commands.impl;

import com.sun.btrace.api.wireio.Command;
import com.sun.btrace.api.wireio.CommandContext;
import com.sun.btrace.dtrace.commands.DTraceDropCommand;
import com.sun.btrace.spi.wireio.CommandImpl;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=DTraceDropCommand.class)
public class DTraceDropCommandImpl extends CommandImpl<DTraceDropCommand> {
    @Override
    public void execute(CommandContext ctx, DTraceDropCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println(asString(cmd));
        }
    }
    
    private static String asString(DTraceDropCommand cmd) {
        return cmd.getDropEvent().getDrop().getDefaultMessage();
    }
}
