/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.api.cli.apt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import net.java.btrace.annotations.processor.AbstractServiceProviderProcessor;
import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.wireio.Command;
import net.java.btrace.spi.cli.ValueFormatterImpl;
import net.java.btrace.spi.wireio.CommandImpl;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
final public class RegistrationProcessor extends AbstractServiceProviderProcessor {
    @Override 
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(
            ValueFormatterImpl.Registration.class.getCanonicalName()
        );
    }
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element el : roundEnv.getElementsAnnotatedWith(ValueFormatterImpl.Registration.class)) {
            ValueFormatterImpl.Registration sp = el.getAnnotation(ValueFormatterImpl.Registration.class);
            if (sp == null) {
                continue;
            }
            register(el, "META-INF/services/" + ValueFormatterImpl.class.getName());
        }
        return true;
    }
    
}
