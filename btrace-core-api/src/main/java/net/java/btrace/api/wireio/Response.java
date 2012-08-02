/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.api.wireio;

/**
 *
 * @author jbachorik
 */
public interface Response<T> {
    final public static Response NULL = new Response() {

        public Object get() throws InterruptedException {
            return null;
        }

        public Object get(long timeout) throws InterruptedException {
            return null;
        }
    };
    
    T get() throws InterruptedException;
    T get(long timeout) throws InterruptedException;
}
