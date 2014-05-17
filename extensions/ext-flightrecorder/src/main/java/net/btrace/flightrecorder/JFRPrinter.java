// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MessagePrinter.java
package net.btrace.flightrecorder;

import com.oracle.jrockit.jfr.*;
import java.net.URISyntaxException;
import net.java.btrace.api.extensions.BTraceExtension;

@BTraceExtension
public class JFRPrinter {
    public static void print(String message) {
        MessageEvent e = new MessageEvent(token, message);
        e.commit();
    }
    private static EventToken token;
    private static Producer producer;

    static {
        try {
            producer = new Producer("BTrace Message Printer", "Captures textual BTrace messages", "http://kenai.com/btrace");
            token = producer.addEvent(MessageEvent.class);
            producer.register();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InvalidEventDefinitionException e) {
            e.printStackTrace();
        } catch (InvalidValueException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
