/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.btrace.dtrace.commands.impl;

import com.sun.btrace.api.wireio.Command;
import com.sun.btrace.api.wireio.CommandContext;
import com.sun.btrace.dtrace.commands.DTraceDataCommand;
import com.sun.btrace.spi.wireio.CommandImpl;
import java.io.PrintWriter;
import java.util.List;
import org.opensolaris.os.dtrace.ProbeData;
import org.opensolaris.os.dtrace.Record;

/**
 *
 * @author Jaroslav Bachorik
 */
@Command(clazz=DTraceDataCommand.class)
public class DTraceDataCommandImpl extends CommandImpl<DTraceDataCommand> {
    @Override
    public void execute(CommandContext ctx, DTraceDataCommand cmd) {
        PrintWriter pw = ctx.lookup(PrintWriter.class);
        if (pw != null) {
            pw.println(asString(cmd));
        }
    }
    
    private static String asString(DTraceDataCommand dc) {
        ProbeData pd = dc.getDataEvent().getProbeData();
        List<Record> records = pd.getRecords();
        StringBuilder buf = new StringBuilder();
        for (Record rec: records) {
            buf.append(rec);
        }
        return buf.toString();
    }
}
