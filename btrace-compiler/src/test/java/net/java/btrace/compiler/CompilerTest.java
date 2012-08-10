package net.java.btrace.compiler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import junit.framework.TestCase;
import net.java.btrace.annotations.BTrace;
import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.extensions.ExtensionsRepositoryFactory;
import net.java.btrace.org.objectweb.asm.ClassReader;
import org.junit.Test;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
public class CompilerTest extends TestCase {
    private Compiler c;
    
    public CompilerTest(String testName) {
        super(testName);
    }
    
    private String findJarPath(Class clazz) {
        String className = clazz.getName().replace('.', '/') + ".class";
        URL x =ClassLoader.getSystemResource(className);
        String path = x.toString();
        path = path.replace("jar:file:", "");
        return path.substring(0, path.indexOf(".jar!") + 4);
    }
    
    private String getClasspath() {
        String target = System.getProperty("user.dir") + File.separator + "target";
        String coreApiJar = findJarPath(BTrace.class);
        
        return coreApiJar + File.pathSeparator + target + File.separator + "classes" + File.pathSeparator + target + File.separator + "test-classes";
    }
    
    private String asmify(byte[] bytecode) {
        StringWriter sw = new StringWriter();
        TraceClassVisitor acv = new TraceClassVisitor(new PrintWriter(sw));
        new org.objectweb.asm.ClassReader(bytecode).accept(acv, ClassReader.SKIP_FRAMES);
        return sw.toString();
    }
    
    @Override
    protected void setUp() throws Exception {
        c = new Compiler(null, false, ExtensionsRepositoryFactory.builtin(ExtensionsRepository.Location.SERVER));
    }
    
    @Override
    protected void tearDown() throws Exception {
        c = null;
    }
    
    @Test
    public void testCompiledScript() throws Exception {        
//        URL u = ClassLoader.getSystemResource("traces/AllMethods1.java");
//        File f = new File(u.toURI());
//        
//        for(Map.Entry<String, byte[]> e : c.compile(f, new PrintWriter(System.err), null, getClasspath()).entrySet()) {
//            System.err.println(e.getKey());
//            System.err.println(asmify(e.getValue()));
//        }
    }
}
