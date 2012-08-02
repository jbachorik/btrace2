/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.api.wireio;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Jaroslav Bachorik
 */
final public class ResponseCommand<T> extends DataCommand<T> {
    public ResponseCommand(int type, int rx, int tx) {
        super(type, rx, tx);
    }

    @Override
    public void read(ObjectInput in) throws ClassNotFoundException, IOException {
        super.read(in);
        setPayload((T)in.readObject());
    }

    @Override
    public void write(ObjectOutput out) throws IOException {
        super.write(out);
        out.writeObject(getPayload());
    }
}
