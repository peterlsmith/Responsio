package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.StringMap;


/**
 * Data structure used to store information about an intent and any associated named entities for
 * a given user input.
 *
 * @author Peter Smith
 */
public class IntentData {

    /** The name of the intent */

    public String   name;


    /** Any named entities extracted from this intent */

    public StringMap       entities;


    /** Current slot data */

    public GenericMap      slots;



    /**
     * Class constructor.
     * 
     * @param name  the name of the detected intent
     */
    public IntentData(final String name) {
        this.name = name;
        this.entities = new StringMap();
    }



    /**
     * Sets the entities extracted from the user input.
     *
     * @param entities   the entities (name/values) extracted from the user input
     */
    public void setEntities(final StringMap entities) {
        this.entities = entities;
    }



    /**
     * Sets the slot data at the time of the user input.
     *
     * @param slots  the current slot data
     */
    public void setSlots(final GenericMap slots) {
        this.slots = slots;
    }



    /**
     * Returns the name of the user intent.
     *
     * @return  the name of the user intent
     */
    public String getName() {
        return name;
    }



    /**
     * Returns the extracted entities.
     *
     * @return  the named entities
     */
    public StringMap getEntities() {
        return entities;
    }



    /**
     * Returns current slot data.
     *
     * @return  the slot data
     */
    public GenericMap getSlots() {
        return slots;
    }

}