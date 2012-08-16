/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client.commands;

import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.spi.wireio.CommandImpl;
import net.java.btrace.wireio.commands.ErrorCommand;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=ErrorCommand.class)
public class ErrorCommandImpl extends CommandImpl<ErrorCommand> {
    public void execute(Lookup ctx, ErrorCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println("Target JVM Error");
            if (cmd.getCause() != null) {
                cmd.getCause().printStackTrace(pw);
            }
        }
    }
}
