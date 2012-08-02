/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.spi.cli;

/**
 *
 * @author Jaroslav Bachorik
 */
public interface ValueFormatterImpl {
    public static @interface Registration {
        
    }
    
    String getValueFormat(Object object);
}
