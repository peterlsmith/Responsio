package com.paradoxwebsolutions.expression;

import java.util.HashMap;
import java.util.Map;



/**
 * Context used during the evaluation of an expression.
 * <p>This context can be used to pass arbitrary data through to the expression
 * elements during evaluation in the form of key value pairs.
 *
 * @author Peter Smith
 */
public class ExpressionContext extends HashMap<String, Object> {

    /**
     * Setter method.
     * <p>This is used to set context data (just a simple redirect to the
     * underlying Map put method). This is provided simply to give a
     * uniform get/set interface (rather than get/put).
     *
     * @param key    the name of the context value to set
     * @param value  the value to set
     * @return a reference to this ExpressionContext instance
     */
    public ExpressionContext set(String key, Object value) {
        put(key, value);
        return this;
    }
}