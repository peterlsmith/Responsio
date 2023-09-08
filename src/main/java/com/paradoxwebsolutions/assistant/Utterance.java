package com.paradoxwebsolutions.assistant;

import java.util.HashMap;

/**
 * Interfaces for utterances - textual responses that get returned to the client.
 *
 * @author Peter Smith
 */
public class Utterance extends HashMap<String, String> {

    /**
     * Returns the text utterance for the requested language.
     *
     * @param language  the language to return the utterance for
     * @return          the text message to return to the client
     */
    public String getContent(final String language) {
        return getOrDefault(language, get("en"));
    }
}