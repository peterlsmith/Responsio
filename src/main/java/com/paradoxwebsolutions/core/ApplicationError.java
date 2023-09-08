package com.paradoxwebsolutions.core;






/**
 * A generic application level runtime error.
 * This exception class is used to represent application level errors, such as 
 * mis-configuration, resource issues, etc.
 *
 * @author Peter Smith
 */
public class ApplicationError extends Exception {
    

    /**
     * Creates a new error with a given message.
     * @param error the error message to report
     */
    public ApplicationError(String error) {
        super(error);
    }
    


    /**
     * Creates a new error with a given message and a cause
     * @param error the error message to report
     * @param cause the exception that caused this error
     */
    public ApplicationError(String error, Exception cause) {
        super(error, cause);
    }

};
