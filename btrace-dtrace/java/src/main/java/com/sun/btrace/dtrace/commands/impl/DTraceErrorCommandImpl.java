/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.btrace.dtrace.commands.impl;

import com.sun.btrace.api.wireio.Command;
import com.sun.btrace.api.wireio.CommandContext;
import com.sun.btrace.dtrace.commands.DTraceErrorCommand;
import com.sun.btrace.spi.wireio.CommandImpl;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=DTraceErrorCommand.class)
public class DTraceErrorCommandImpl extends CommandImpl<DTraceErrorCommand> {
    @Override
    public void execute(CommandContext ctx, DTraceErrorCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            Error err = cmd.getErrorEvent() != null ? cmd.getErrorEvent().getError() : null;
            Exception ex = cmd.getException();
            
            if (err != null) {
                err.printStackTrace(pw);
                return;
            }
            if (ex != null) {
                ex.printStackTrace(pw);
            }
        }
    }
}
