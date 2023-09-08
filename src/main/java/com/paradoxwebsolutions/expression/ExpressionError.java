package com.paradoxwebsolutions.expression;



/**
 * Error class used to report failures in the parsing or processing
 * of an expression.
 *
 * @author Peter Smith
 */
public class ExpressionError extends Exception {

    /**
     * Creates a new instance of an ExpressionError.
     *
     * @param message  the error message
     */
    public ExpressionError(String message) {
        super(message);
    }
}