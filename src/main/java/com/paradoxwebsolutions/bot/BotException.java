package com.paradoxwebsolutions.bot;




/**
 * Exception class for service infrastructure errors.
 *
 * @author Peter Smith
 */
public class BotException extends Exception {

    /** A status code that can be used as an HTTP return status */

    private int status;
    


    /**
     * Creates a BotException instance with a given status code and error message.
     * 
     * @param status  the status code
     * @param error   the error message
     */
    public BotException(int status, String error) {
        super(error);
        this.status = status;
    }



    /**
     * Returns the error status code.
     *
     * @return the error status code
     */
    public int getStatus() {
        return this.status;
    }
    


    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getError() {
        return this.getMessage();
    }
};
