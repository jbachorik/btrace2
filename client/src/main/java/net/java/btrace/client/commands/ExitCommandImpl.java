/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client.commands;


import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.client.Client;
import net.java.btrace.spi.wireio.CommandImpl;
import net.java.btrace.api.wireio.Channel;
import net.java.btrace.wireio.commands.ExitCommand;
import java.io.IOException;
import java.io.PrintWriter;
import net.java.btrace.wireio.commands.ACKCommand;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz = ExitCommand.class, target = Command.Target.CLIENT)
public class ExitCommandImpl extends CommandImpl<ExitCommand> {
    @Override
    public void execute(Lookup ctx, ExitCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println();
            pw.println("Target application has quit with code " + cmd.getExitCode());
            pw.flush();
        }
        Client c = ctx.lookup(Client.class);
        if (c != null) {
            try {
                Channel ch = ctx.lookup(Channel.class);
                if (ch != null) {
                    ch.sendResponse(cmd, ACKCommand.class, true);
                }
            } catch (IOException e) {
                BTraceLogger.debugPrint(e);
            }
            try {
                Thread.sleep(500); // let the other side some time to handle the response
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            c.agentExit(cmd.getExitCode());
        }
    }
}
