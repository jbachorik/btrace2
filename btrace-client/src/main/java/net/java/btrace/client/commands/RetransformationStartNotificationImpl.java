/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client.commands;

import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.spi.wireio.CommandImpl;
import net.java.btrace.wireio.commands.RetransformationStartNotification;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=RetransformationStartNotification.class)
public class RetransformationStartNotificationImpl extends CommandImpl<RetransformationStartNotification> {
    public void execute(Lookup ctx, RetransformationStartNotification cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println("Starting class retransformation for " + cmd.getNumClasses() + " classes");
        }
    }
}
