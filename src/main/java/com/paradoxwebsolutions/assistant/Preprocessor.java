package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.GenericMap;

import java.util.HashSet;
import java.util.Set;



/**
 * Base functionality for client input preprocessors.
 * <p>Preprocessors are links in the pipeline that performs preprocessing on the
 * client input prior to categorization and named entity recognition.
 *
 * @author Peter Smith
 */
public abstract class Preprocessor {

    /** The set of named pipelines that this preprocessor applies to */

    private Set<String>     pipelines;


    /**
     * Determines whether or not this preprocessor is active for the named pipeline.
     * <p>Pipelines are named, for example 'train' is usually the name given to the
     * pipeline used during identity setup and training. The pipeline used in the
     * operational system is usually called 'chat'. If a preprocessor is not explicitly
     * configured for a named pipeline, it is assumed to apply to all pipelines.
     *
     * @param pipeline  the name of the pipeline to check
     * @return true if the preprocessor is active for the named pipeline, false otherwise.
     */
    public boolean forPipeline(final String pipeline) {
        return (pipelines == null || pipelines.size() == 0 || pipelines.contains(pipeline));
    }



    /**
     * Performs this preprocessing step on the given user input.
     *
     * @param session  the current user session
     * @param input    the user input, which may contain the results of prior
     *                 preprocessing steps.
     * @throws ApplicationError on error
     */
    public abstract void preprocess(ClientSession session, GenericMap input) throws ApplicationError;
}
