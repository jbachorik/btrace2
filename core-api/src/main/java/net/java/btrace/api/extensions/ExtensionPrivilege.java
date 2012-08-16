/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.api.extensions;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 * @since 2.0
 * 
 * BTrace privileges enumeration
 */
public enum ExtensionPrivilege {

    /**
     * Extension can contain loops
     */
    LOOPS,
    /**
     * Extension can call external methods (not further checked)
     */
    EXTERNAL_METHOD_CALLS,
    /**
     * Extension can mutate any accessible fields of external instances/classes
     */
    EXTERNAL_FIELD_CHANGE,
    /**
     * Extension can create new objects/arrays
     */
    INSTANTIATE,
    /**
     * Extension can contain synchronized blocks/methods
     */
    SYNCHRONIZED,
    /**
     * Extension does not request any additional permissions
     */
    NONE
}
