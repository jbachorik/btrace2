/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.api.cli;

import net.java.btrace.api.core.ServiceLocator;
import net.java.btrace.spi.cli.ValueFormatterImpl;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ValueFormatter  {
    final private static String DEFAULT_FORMAT = "%15s";
    final private Collection<ValueFormatterImpl> formatters = new LinkedList<ValueFormatterImpl>();
    public ValueFormatter(ClassLoader cl) {
        for(ValueFormatterImpl pf : ServiceLocator.listServices(ValueFormatterImpl.class, cl)) {
            formatters.add(pf);
        }
    }
    
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
