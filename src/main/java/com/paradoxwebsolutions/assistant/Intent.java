package com.paradoxwebsolutions.assistant;


/**
 * Defines an intent for an identity.
 *
 * @author Peter Smith
 */
public class Intent {

    /** The named entity recognizers for this intent (null if none configured) */

    private NERs            ners;


    /** The categorizer confidence threshold required for this intent to be accepted */

    private double          confidenceThreshold = 0.0;


    /** Entity definitions for this intent */

    private Entities        entities;



    /**
     * Sets the name entity recognizers (NERs) for this intent.
     * 
     * @param ners  the new name entity recognizers, or null disable named entity recognition.
     */
    public void setNers(final NERs ners) {
        this.ners = ners;
    }



    /**
     * Returns the named entity recognizers (NERs) for this intent.
     * 
     * @return  the named entity recognizers or null if not enabled for this intent.
     */
    public NERs getNers() {
        return this.ners;
    }



    /**
     * Sets the confidence threshold for this intent.
     * <p>The confidence threshold determines the probability cutoff below which this intent will
     * not be considered as a potential match.
     *
     * @param confidenceThreshold  the new confidence threshold to set
     */
    public void setConfidenceThreshold(final double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }



    /**
     * Returns the confidence threshold for this intent.
     *
     * @return  the confidence threshold for this intent
     */
    public double getConfidenceThreshold() {
        return this.confidenceThreshold;
    }



    /**
     * Sets the entities for this intent.
     * <p>Entities are extract from the user input and provide specialization of the intent.
     *
     * @param entities  the entity definition
     */
    public void setEntities(final Entities entities) {
        this.entities = entities;
    }



    /**
     * Returns a named entity for this intent.
     *
     * @param name  the name of the entity definition to return
     * @return      the entity definition
     */
    public Entity getEntity(final String name) {
        assert entities != null && entities.containsKey(name) : String.format("Invalid entity name '%s'", name);

        return entities.get(name);
    }

}