package com.paradoxwebsolutions.expression;



/**
 * Class used to register named functions or operators.
 * <p>Instances of this class are used for registering and creating named functions
 * and operators that can be used in expressions.
 *
 * @author Peter Smith
 */
public abstract class Factory {
 
    /** The name of the expresssion or function created by this factory */

    private String name;


    /**
     * Construct a factory instance.
     *
     * @param name       the name of the operator created by this factory
     */
    public Factory(String name) {
        this.name = name;
    }



    /**
     * Returns the name of this operator.
     * Note that the name is simple the operator text, e.g. '!=', '+', 'or', etc,
     * or the function name, e.g. 'length'
     *
     * @return  the name of this operator
     */
    public String getName() {
        return this.name;
    }



   /**
     * Creates an instance of an expression for performing an operation on the provided
     * expression input. 
     * <p>Factory instances must implement this method.
     *
     * @param input  the arguments to be used for input to the function. 
     * @return an expression representing the function to be evaluated.
     * @throws ExpressionError if the expression could not be generated.
     */
    public abstract Expression create(Expression ...input) throws ExpressionError;
}

