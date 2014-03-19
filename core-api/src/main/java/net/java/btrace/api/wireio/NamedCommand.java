/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.api.wireio;

import java.io.ObjectOutput ;
import java.io.IOException;
import java.io.ObjectInput;

/**
 * A command with associated name
 * 
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 * @since 2.0
 */
abstract public class NamedCommand extends AbstractCommand {
    private String name;
    
    public NamedCommand(int typeId, int rx, int tx) {
        super(typeId, rx, tx);
    }

    final public String getName() {
        return name;
    }

    final public void setName(String name) {
        this.name = name;
    }

    @Override
    public void read(ObjectInput in) throws ClassNotFoundException, IOException {
        name = in.readUTF();
    }

    @Override
    public void write(ObjectOutput  out) throws IOException {
        out.writeUTF(name == null ? "" : name);
    }
}
