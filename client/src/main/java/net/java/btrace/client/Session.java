/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.core.Lookup;
import net.java.btrace.api.core.ValueFormatter;
import net.java.btrace.api.wireio.AbstractCommand;
import net.java.btrace.api.wireio.Channel;
import net.java.btrace.api.wireio.Response;
import net.java.btrace.wireio.commands.InstrumentCommand;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
public class Session {
    final private Channel channel;
    final private byte[] trace;
    final private String[] args;
    final private Lookup ctx;
    final private ExecutorService commDispatcher = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "BTrace Client Comm Dispatcher");
        }
    });
    
    public Session(Channel channel, byte[] trace, String[] args, ValueFormatter vf) throws IOException {
        this.channel = channel;
        this.trace = trace;
        this.args = args;
        this.ctx = new Lookup();
        this.ctx.add(vf, channel);
        
        start();
    }
    
    private void start() throws IOException {
        Response<Boolean> f = channel.sendCommand(InstrumentCommand.class, new AbstractCommand.Initializer<InstrumentCommand>() {
            public void init(InstrumentCommand cmd) {
                cmd.setCode(trace);
                cmd.setArgs(args);
            }
        });
        commDispatcher.submit(new Runnable() {
            public void run() {
                try {
                    BTraceLogger.debugPrint("entering into command loop");
                    AbstractCommand cmd = channel.readCommand();
                    cmd.execute(ctx);
                } catch (IOException ex) {

                    BTraceLogger.debugPrint("exitting due to exception " + ex.getMessage());
                } catch (ClassNotFoundException ex) {
                    BTraceLogger.debugPrint("exitting due to exception " + ex.getMessage());
                }
            }
        });
        try {
            Boolean rslt = f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
