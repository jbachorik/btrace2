/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.commands;


import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.wireio.CommandContext;
import net.java.btrace.spi.wireio.CommandImpl;
import net.java.btrace.wireio.commands.MessageCommand;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=MessageCommand.class)
public class MessageCommandImpl extends CommandImpl<MessageCommand> {
    @Override
    public void execute(CommandContext ctx, MessageCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.print(cmd.getMessage());
            pw.flush();
        }
    }   
}
