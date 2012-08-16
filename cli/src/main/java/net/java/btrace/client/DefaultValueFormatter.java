/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client;

import net.java.btrace.spi.core.ValueFormatterImpl;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

/**
 * The default formats for various object types
 * @author Jaroslav Bachorik
 */
@ValueFormatterImpl.Registration
public class DefaultValueFormatter implements ValueFormatterImpl {
    private static final HashMap<Class< ? >, String> typeFormats = new HashMap<Class< ? >, String>();
    static {
        typeFormats.put(Integer.class, "%15d");
        typeFormats.put(Short.class, "%15d");
        typeFormats.put(Byte.class, "%15d");
        typeFormats.put(Long.class, "%15d");
        typeFormats.put(BigInteger.class, "%15d");
        typeFormats.put(Double.class, "%15f");
        typeFormats.put(Float.class, "%15f");
        typeFormats.put(BigDecimal.class, "%15f");
        typeFormats.put(String.class, "%50s");
    }
    
    public String getValueFormat(Object object) {
        if (object == null) {
            return null;
        }
        String usedFormat = typeFormats.get(object.getClass());
        return usedFormat;
    }   
}
