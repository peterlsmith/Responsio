package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;


/**
 * Interface for classes that represent a path segment in a story that
 * can be matched against a narrative.
 * <p>A path segment is usually a single step, but it may be a compound step containing
 * multiple steps, branches, and optional steps.
 *
 * @author Peter Smith
 * @see Story
 * @see Narrative
 * @see Agent
 */
public interface Step {
    /**
     * Determines whether or not this step matches against the narrative.
     * <p>Implementations should consume as much of the narrative as possible when matching and
     * return true if this step was completely matched (in which case the story can move on to
     * match the next step), or false if it was no match or only a partial match.
     * In the case of a false return value, the caller can distinguish between no match and a 
     * partial match by using the {@link Narrative#getIndex} method.
     * 
     * @param assistant  the assistant that to which this step belongs
     * @param session    the client session
     * @param narrative  the narrative to match this step against
     * @return true      if this step has a complete match against the narrative, false otherwise.
     * @throws ApplicationError on error
     * @see Narrative
     * @see Story
     */
    public boolean match(Assistant assistant, ClientSession session, Narrative narrative) throws ApplicationError;
}