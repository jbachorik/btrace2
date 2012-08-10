/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client.commands;


import net.java.btrace.api.core.ValueFormatter;
import net.java.btrace.api.wireio.Command;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.spi.wireio.CommandImpl;
import net.java.btrace.wireio.commands.GridDataCommand;
import java.io.PrintWriter;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=GridDataCommand.class)
public class GridDataCommandImpl extends CommandImpl<GridDataCommand> {    
    @Override
    public void execute(Lookup ctx, GridDataCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        ValueFormatter f = ctx.lookup(ValueFormatter.class);
        if (pw != null) {
            print(cmd, f, pw);
            pw.flush();
        }
    }
 
    private void print(GridDataCommand cmd, ValueFormatter f, PrintWriter out) {
        if (cmd.getPayload() != null) {
            if (cmd.getName() != null && !cmd.getName().equals("")) {
                out.println(cmd.getName());
            }
            print(cmd.getPayload(), f, out);
        }
    }
    
    private void print(GridDataCommand.GridData data, ValueFormatter f, PrintWriter out) {
        String format = data.getFormat();
        for (Object[] dataRow : data.getGrid()) {
            // Convert histograms to strings, and pretty-print multi-line text
            Object[] printRow = dataRow.clone();
            for (int i = 0; i < printRow.length; i++) {
                if (printRow[i] == null) {
                    printRow[i] = "<null>";
                    continue;
                }
//                if (printRow[i] instanceof GridData) {
//                    print((GridData)printRow[i], out);
//                }
                if (printRow[i] instanceof String) {
                    String value = (String) printRow[i];
                    if (value.contains("\n")) {
                        printRow[i] = reformatMultilineValue(value);
                    }
                    continue;
                }
            }

            // Format the text
            String usedFormat = format;
            if (usedFormat == null || usedFormat.length() == 0) {
                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < printRow.length; i++) {
                    buffer.append("  ");
                    buffer.append(f.getFormat(printRow[i]));
                }
                usedFormat = buffer.toString();
            }
            String line = String.format(usedFormat, printRow);

            out.println(line);
        }
    }
    
    /**
     * Takes a multi-line value, prefixes and appends a blank line, and inserts tab characters at the start of every
     * line. This is derived from how dtrace displays stack traces, and it makes for pretty readable output.
     */
    private String reformatMultilineValue(String value) {
        StringBuilder result = new StringBuilder();
        result.append("\n");
        for (String line : value.split("\n")) {
            result.append("\t").append(line);
            result.append("\n");
        }
        return result.toString();
    }
}
