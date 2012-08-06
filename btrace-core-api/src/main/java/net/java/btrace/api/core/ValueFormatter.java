/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.api.core;

import java.util.Collection;
import java.util.LinkedList;
import net.java.btrace.spi.core.ValueFormatterImpl;

/**
 * Allows for obtaining an object specific format string
 * @author Jaroslav Bachorik
 * @since 2.0
 */
public class ValueFormatter  {
    final private static String DEFAULT_FORMAT = "%15s";
    final private Collection<ValueFormatterImpl> formatters = new LinkedList<ValueFormatterImpl>();
    public ValueFormatter(ClassLoader cl) {
        for(ValueFormatterImpl pf : ServiceLocator.listServices(ValueFormatterImpl.class, cl)) {
            formatters.add(pf);
        }
    }
    
    /**
     * 
     * @param obj The object to get the format string for
     * @return The specific format string or the default <b>"%15s"</b>
     */
    public String getFormat(Object obj) {
        if (obj == null) return DEFAULT_FORMAT;
        
        String f = null;
        for(ValueFormatterImpl pf : formatters) {
            f = pf.getValueFormat(obj);
            if (f != null) {
                break;
            }
        }
        return f != null ? f : DEFAULT_FORMAT;
    }
}
