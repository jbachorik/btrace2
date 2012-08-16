/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.instr;

import java.io.IOException;
import net.java.btrace.instr.ExtensionRuntimeProcessor;
import net.java.btrace.instr.InstrumentUtils;
import net.java.btrace.org.objectweb.asm.ClassReader;
import net.java.btrace.org.objectweb.asm.ClassWriter;
import net.java.btrace.support.InstrumentorTestBase;
import org.junit.Test;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 */
public class ExtensionRuntimeProcessorTest extends InstrumentorTestBase {
    
    public ExtensionRuntimeProcessorTest() {
    }
    
    @Override
    protected void transform(String traceName) throws IOException {
        ClassReader reader = new ClassReader(originalBC);
        ClassWriter writer = InstrumentUtils.newClassWriter();

        InstrumentUtils.accept(reader, new ExtensionRuntimeProcessor(writer));

        transformedBC = writer.toByteArray();
    }
   
    @Test
    public void testNoClinitContextInjection() throws Exception {
        originalBC = loadTargetClass("SampleExtNoClinit");
        System.out.println("=== Testing Extension Context Injection");
        transform(null);
        System.out.println("... Transformed");
        checkTransformation("\n// access flags 0xA\n" +
                            "private static bclinit0()V\n" +
                            "INVOKESTATIC net/java/btrace/runtime/BTraceRuntimeBridge.getInstance ()Lnet/java/btrace/runtime/BTraceRuntimeBridge;\n" +
                            "PUTSTATIC resources/SampleExtNoClinit.objs : Lnet/java/btrace/api/extensions/runtime/Objects;\n" +
                            "RETURN\n" +
                            "MAXSTACK = 1\n" +
                            "MAXLOCALS = 0\n" +
                            "\n" +
                            "// access flags 0xA\n" +
                            "private static <clinit>()V\n" +
                            "INVOKESTATIC resources/SampleExtNoClinit.bclinit0 ()V\n" +
                            "RETURN\n" +
                            "MAXSTACK = 0\n" +
                            "MAXLOCALS = 0");
    }
    @Test
    public void testClinitContextInjection() throws Exception {
        originalBC = loadTargetClass("SampleExtClinit");
        System.out.println("=== Testing Extension Context Injection");
        transform(null);
        System.out.println("... Transformed");
        checkTransformation("// access flags 0xA\n" +
                            "private static bclinit0()V\n" +
                            "\n" +
                            "// access flags 0xA\n" +
                            "private static <clinit>()V\n" +
                            "INVOKESTATIC resources/SampleExtClinit.bclinit0 ()V\n" +
                            "INVOKESTATIC resources/SampleExtClinit.bclinit1 ()V\n" +
                            "RETURN\n" +
                            "MAXSTACK = 0\n" +
                            "MAXLOCALS = 0\n" +
                            "\n" +
                            "// access flags 0xA\n" +
                            "private static bclinit1()V\n" +
                            "INVOKESTATIC net/java/btrace/runtime/BTraceRuntimeBridge.getInstance ()Lnet/java/btrace/runtime/BTraceRuntimeBridge;\n" +
                            "PUTSTATIC resources/SampleExtClinit.objs : Lnet/java/btrace/api/extensions/runtime/Objects;\n" +
                            "RETURN\n" +
                            "MAXSTACK = 1\n" +
                            "MAXLOCALS = 0");
    }
}
