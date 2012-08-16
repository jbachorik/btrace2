/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package resources;

import javax.annotation.Resource;
import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.extensions.runtime.Objects;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
@BTraceExtension
public class SampleExtNoClinit {
    @Resource
    private static Objects objs;
    
    public static void doSample(Object param) {
        System.out.println("***" + objs.hash(param));
    }
}
