package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;

import java.util.List;


/**
 * Represents a single, unique story for an assistant.
 * <p>A story is a unique conversation, or narrative, in an assistant, represented by a number of
 * {@link Step}s (user intents and associated actions) that define how the conversation should go.
 * Currently, this is just a wrapper around {@link Path} - a Story requires no additional
 * information or functionality. However, it decreases the score a tiny amount in the case
 * of the story being complete. This means it should score slightly lower than a story with
 * a potentially longer path that is not yet complete.
 *
 * @author Peter Smith
 * @see Assistant
 * @see Step
 */
public class Story extends Path {

    @Override
    public boolean match(Assistant assistant, ClientSession session, Narrative narrative) throws ApplicationError {

        boolean isComplete = super.match(assistant, session, narrative);
        if (isComplete) narrative.addScore(-0.1);

        return isComplete;
    }

}