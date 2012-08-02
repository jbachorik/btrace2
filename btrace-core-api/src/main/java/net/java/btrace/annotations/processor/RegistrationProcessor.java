/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.annotations.processor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.wireio.Command;
import net.java.btrace.spi.wireio.CommandImpl;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
final public class RegistrationProcessor extends AbstractServiceProviderProcessor {
    @Override 
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            BTraceExtension.class.getCanonicalName(),
            Command.class.getCanonicalName()
        ));
    }
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element el : roundEnv.getElementsAnnotatedWith(BTraceExtension.class)) {
            BTraceExtension sp = el.getAnnotation(BTraceExtension.class);
            if (sp == null) {
                continue;
            }
            register(el, "META-INF/services/" + BTraceExtension.class.getName());
        }
        for (Element el : roundEnv.getElementsAnnotatedWith(Command.class)) {
            Command sp = el.getAnnotation(Command.class);
            if (sp == null) {
                continue;
            }
            register(el, "META-INF/services/" + CommandImpl.class.getName());
        }
        return true;
    }
    
}
