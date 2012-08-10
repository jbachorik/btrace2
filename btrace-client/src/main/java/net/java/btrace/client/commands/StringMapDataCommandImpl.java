/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client.commands;

import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.spi.wireio.CommandImpl;
import net.java.btrace.wireio.commands.StringMapDataCommand;
import java.io.PrintWriter;
import java.util.Map;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=StringMapDataCommand.class)
public class StringMapDataCommandImpl extends CommandImpl<StringMapDataCommand> {
    public void execute(Lookup ctx, StringMapDataCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println("String map [" + cmd.getName() + "]");
            StringBuilder sb = new StringBuilder();
            int maxSize = 0;
            for(Map.Entry<String, String> e : cmd.getPayload().entrySet()) {
                sb.append(e.getKey()).append(" = ").append(e.getValue()).append("\n");
                maxSize = Math.max(maxSize, e.getKey().length() + e.getValue().length() + 3);
            }
            sb.insert(0, "\n");
            for(int i=0;i<maxSize;i++) {
                sb.insert(0, "=");
            }
            pw.print(sb);
        }
    }
}
