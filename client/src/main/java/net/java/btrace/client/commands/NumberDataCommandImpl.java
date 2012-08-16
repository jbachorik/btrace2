/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client.commands;

import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.spi.wireio.CommandImpl;
import net.java.btrace.wireio.commands.NumberDataCommand;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=NumberDataCommand.class)
public class NumberDataCommandImpl extends CommandImpl<NumberDataCommand> {
    public void execute(Lookup ctx, NumberDataCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println(cmd.getName() + " = " + cmd.getPayload());
        }
    }
}
