package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.GenericMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks the status of a client session.
 * <p>Instances of this object are used to aggregate all the necessary information about a user
 * session, including the particular instance of assistant they are interacting with, as well as
 * any session data (cached data values, chat history, etc).
 *
 * @author Peter Smith
 */
public class ClientSession {

    /** Assistant/user specific logger */
    
    private Logger LOGGER = null;


    /** Current user session data */

    private SessionData data;


    /** The assistant */

    private Assistant assistant;




    /**
     * Creates a client session instance with a given session data and assistant.
     *
     * @param data       any user session data
     * @param assistant  the chat assistant
     */
    public ClientSession(SessionData data, Assistant assistant) {
        LOGGER = new Logger(data.getIdentity());
        this.data = data;
        this.assistant = assistant;
        data.setTimestamp();
    }



    /**
     * Returns the current slot configuration and data.
     * <p>Note that this does not create a copy of the slot data - changes
     * that occur to the returned Slots instance will be reflected in the 
     * stored session.
     *
     * @return the current slot data
     */
    public GenericMap getSlots() {
        return data.getSlots();
    }



    /**
     * Returns the current language configuration.
     * <p>This is a utility method - it just passes the call on down
     * to the session data.
     *
     * @return the current language code
     */
    public String getLanguage() {
        return data.getLanguage();
    }



    /**
     * Returns the current user session data instance.
     *
     * @return the current user session data
     * @see SessionData
     */
    public SessionData getSessionData() {
        return this.data;
    }



    /**
     * Return the assistant.
     *
     * @return the active assistant
     * @see Assistant
     */
    public Assistant getAssistant() {
        return this.assistant;
    }



    /**
     * Logs an informational message to the log for this user.
     * <p>Log messages logged via this method are automatically prefixed
     * with the user ID.
     *
     * @param message  the message to log
     */
    public void info(String message) {
        LOGGER.info(this.data.getUserId() + ": " + message);
    }



    /**
     * Logs an debug message to the log for this user.
     * <p>Log messages logged via this method are automatically prefixed
     * with the user ID.
     *
     * @param message  the message to log
     */
    public void debug(String message) {
        LOGGER.debug(this.data.getUserId() + ": " + message);
    }
}