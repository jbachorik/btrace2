/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.nbapi.options;

import net.java.btrace.nbapi.options.impl.BTraceSettingsImpl;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
final public class BTraceSettings extends com.sun.btrace.api.BTraceSettings {
    final private static class Singleton {
        final private static BTraceSettings INSTANCE = new BTraceSettings();
    }

    final static private BTraceSettingsImpl delegate = new BTraceSettingsImpl();

    final static public BTraceSettings sharedInstance() {
        return Singleton.INSTANCE;
    }

    public boolean isDebugMode() {
        return delegate.isDebugMode();
    }

    public void setDebugMode(boolean debugMode) {
        delegate.setDebugMode(debugMode);
    }

    public String getDumpClassPath() {
        return delegate.getDumpClassPath();
    }

    public void setDumpClassPath(String dumpClassPath) {
        delegate.setDumpClassPath(dumpClassPath);
    }

    public boolean isDumpClasses() {
        return delegate.isDumpClasses();
    }

    public void setDumpClasses(boolean dumpClasses) {
        delegate.setDumpClasses(dumpClasses);
    }
}
