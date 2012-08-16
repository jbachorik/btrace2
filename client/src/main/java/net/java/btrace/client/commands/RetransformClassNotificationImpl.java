/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client.commands;

import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.spi.wireio.CommandImpl;
import net.java.btrace.wireio.commands.RetransformClassNotification;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=RetransformClassNotification.class)
public class RetransformClassNotificationImpl extends CommandImpl<RetransformClassNotification> {
    public void execute(Lookup ctx, RetransformClassNotification cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println("Retransforming " + cmd.getClassName());
        }
    }
}
