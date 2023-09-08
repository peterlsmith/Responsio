package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.GenericMap;

import java.util.HashMap;


/**
 * Stores user input data as it is being processed through the input processing pipeline
 * and is passed on to the categorizer for intent processing.
 * <p>This is a simple map used to cache the original user input as well as any intermediate 
 * and output stages from the input processing pipeline. Pipelines are configurable, so the
 * exact contents can vary depending upon the configuration. A typical map may look as follows:
 *
 * <pre>{@code
 *
 *  document    [my email address is peter@paradoxwebsolutions.com] 
 *  sentence    [my email address is email_token] 
 *  compound    [PRON:my[my], NOUN:email[email], NOUN:address[address], AUX:is[is], VERB:email_token[email_token]] 
 *  pos         [PRON, NOUN, NOUN, AUX, VERB] 
 *  lemmas      [my, email, address, is, email_token] 
 *  tokens      [my, email, address, is, email_token] 
 *  train       [my, email, address, is, email_token] 
 *
 * }</pre>
 *
 * Values in the map may generally contain strings or string arrays, but may contain any type of object as
 * needed by the categorizer.
 */
public class Input extends GenericMap {

    /** A copy of the original input (not currently used) */

    private final String document;



    /**
     * Create an input instance for the given user input.
     * 
     * @param document  the original, raw user input.
     */
    public Input(final String document) {
        this.document = document;
        put("document", document);
    }
}