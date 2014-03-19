/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.api.server;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
public interface ShutdownHandler {
    /**
     * This method is invoked on explicit shutdown (eg. the traced application exits)
     * @param exitCode The code the traced application exits with
     */
    void shutdown(int exitcode);
}
