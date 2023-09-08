package com.paradoxwebsolutions.assistant;


/**
 * Defines a named entity.
 *
 * @author Peter Smith
 */
public class Entity {

    /** Indicates whether or not this entity is local (should not be copied into a slot) */

    private boolean         local = false;




    /**
     * Returns whether or not this entity definition is local.
     *
     * @return  true if the entity is local, false otherwise
     */
    public boolean isLocal() {
        return local;
    }
}