package com.paradoxwebsolutions.core;






/**
 * A generic HTTP runtime error.
 * This exception class is used to represent request level errors that should be 
 * returned to the client.
 *
 * @author Peter Smith
 */
public class HttpError extends Exception {
    
    /** The http status code for this error */

    private int status;


    /**
     * Creates a new error with a given message.
     *
     * @param status the http errir status code
     * @param error the error message to report
     */
    public HttpError(int status, String error) {
        super(error);
        this.status = status;
    }



    /**
     * Returns the http status value for this error.
     *
     * @return  the http status value
     */
    public int getStatus() {
        return status;
    }
};
