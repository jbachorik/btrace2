/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.nbapi.options.impl;

import com.sun.btrace.api.BTraceSettings;
import com.sun.btrace.spi.BTraceSettingsProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
@ServiceProvider(service=BTraceSettingsProvider.class)
public class BTraceSettingsProviderImpl implements BTraceSettingsProvider {
    public BTraceSettings getSettings() {
        return net.java.btrace.nbapi.options.BTraceSettings.sharedInstance();
    }
}
