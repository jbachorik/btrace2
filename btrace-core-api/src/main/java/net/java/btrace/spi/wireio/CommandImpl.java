/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.spi.wireio;

import net.java.btrace.api.wireio.CommandContext;
import net.java.btrace.api.wireio.AbstractCommand;

/**
 *
 * @author Jaroslav Bachorik
 */
abstract public class CommandImpl<T extends AbstractCommand> {
    final public static CommandImpl NULL = new CommandImpl() {
        public void execute(CommandContext ctx, AbstractCommand cmd) {
            // do nothing
        }
    };
    
    abstract public void execute(CommandContext ctx, T cmd);
}
