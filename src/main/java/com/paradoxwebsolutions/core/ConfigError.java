package com.paradoxwebsolutions.core;






/**
 * Application level exception base class.
 * This exception class is used to represent application level errors, such as 
 * mis-configuration, resource issues, etc.
 * @author Peter Smith
 */
public class ConfigError extends ApplicationError {
    

    /**
     * Creates a configuration error instance with the given error message.
     * @param error the error message
     */
    public ConfigError(String error) {
        super(error);
    }
    


    /**
     * Returns the error message.
     * Note thta this is just a pass-thru to the getMessage method.
     * @return the error message
     */
    public String getError() {
        return this.getMessage();
    }
};
