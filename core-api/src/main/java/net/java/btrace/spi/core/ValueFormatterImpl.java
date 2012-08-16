/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.spi.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows for registering custom object formatters
 * @author Jaroslav Bachorik
 * @since 2.0
 */
public interface ValueFormatterImpl {
    /**
     * Service registration annotation
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    public static @interface Registration {
    }
    
    /**
     * Obtain the format string for the given object
     * @param object The object to retrieve the format string for
     * @return The format string or <b>NULL</b>
     */
    String getValueFormat(Object object);
}
