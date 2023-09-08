package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.GenericMap;

import java.util.Arrays;


/**
 * Stores session data for a client.
 * <p>Note that this is used to store raw session data at its most primitive level, and is wrapped
 * by a {@link ClientSession}, which provides higher level functionality (such as client specific logging)
 * to minimize the size of the session data.
 *
 * @author Peter Smith
 */
public class SessionData {

    /** The assistant identity for this session */

    private String identity;


    /** The user id for this session */

    private String userId;


    /** The current language selection */

    private String language = "en";


    /** State cache */

    private GenericMap slots = new GenericMap();


    /** Intent history */

    private IntentData[] history = new IntentData[0];


    /** Timestamp of last activity */

    private long timestamp;


    /**
     * Create a brand new client session, with no history of previous conversations.
     * 
     * @param identity  the identity of the assistant with which this client is conversing
     * @param id        the unique ID of the client
     */
    public SessionData(String identity, String id) {
        this.identity = identity;
        this.userId = id;
        this.timestamp = System.currentTimeMillis();
    }



    /**
     * Return the assistant identity for this session.
     *
     * @return the identity of the assistant associated with this session
     */
    public String getIdentity() {
        return this.identity;
    }



    /**
     * Return the client ID.
     *
     * @return the unique ID assigned to the client
     */
    public String getUserId() {
        return this.userId;
    }



    /**
     * Return the timestamp for this session.
     *
     * @return the timestamp for this session
     */
    public long getTimestamp() {
        return this.timestamp;
    }



    /**
     * Timestamp this session.
     *
     */
    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }



    /**
     * Return the currently active language.
     *
     * @return the currently active language
     */
    public String getLanguage() {
        return this.language;
    }



    /**
     * Sets the currently active language.
     *
     * @param language  the new currently active language
     */
    public void setLanguage(String language) {
        this.language = language;
    }



    /**
     * Add an intent (including slot data) to the history. We must clone
     * the slot data so it doesn't get modified externally.
     *
     * @param intent  the client intent to add to the session history
     */
    public void addHistory(IntentData intent) {
        history = Arrays.copyOf(history, history.length + 1);
        history[history.length - 1] = intent;
    }



    /**
     * Get the current client intent history (narrative).
     * <p>Note that the history as returned by this method is the currently active
     * history - intents that have not been resolved into completed stories. 
     *
     * @return the complete intent history as an array of {@link IntentData}
     */
    public IntentData[] getHistory() {
        return this.history;
    }



    /**
     * Get the current step history (narrative).
     *
     * @param history the new intent history as an array of {@link IntentData}
     */
    public void setHistory(IntentData[] history) {
        this.history = history;
    }



    /**
     * Returns the current (aggregated) slot data.
     * 
     * @return the current slot data
     */
    public GenericMap getSlots() {
        return this.slots;
    }
}