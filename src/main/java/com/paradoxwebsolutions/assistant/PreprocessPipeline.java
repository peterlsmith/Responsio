package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.assistant.preprocessors.PreprocessorDefault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Performs client input preprocessing.
 * <p>This class acts as a container for a number of {@link Preprocessor} instances and is responsible
 * for managing the preprocessing of client input, prior to intent categorization.
 *
 * @author Peter Smith
 */
public class PreprocessPipeline {

    /** A list of preprocesssors to be used in processing inputs */

    private List<Preprocessor>    preprocessors;


    /** Logger */
    
    private Logger LOGGER = null;


    /**
     * Creates a PreprocessPipeline instance.
     *
     * @param assistant  the {@link Assistant} instance to process input for
     * @param pipeline   the name of the preprocessing pipeline to use
     * @throws ApplicationError on error
     */
    public PreprocessPipeline(final Assistant assistant, final String pipeline) throws ApplicationError {
        String identity = assistant.getIdentity();
        assert identity != null : "Invalid processing pipeline configuration - no identity";

        LOGGER = new Logger(identity);
        LOGGER.info(String.format("Initializing PreprocessPipeline for '%s'", pipeline));

        /* Get the preprocessor configuration and filter for the pipeline */

        this.preprocessors = assistant.getPreprocessors().stream().filter((p) -> p.forPipeline(pipeline)).collect(Collectors.toList());
    }



    /**
     * Processes client input according to the preprocessor configuration.
     *
     * @param session   the client session
     * @param document  the raw client input
     * @return a {@link Input} instance with the preprocessed input
     * @throws ApplicationError on error
     */
    public Input preprocess(ClientSession session, String document) throws ApplicationError {

        String prefix = session.getSessionData().getUserId();
        Input input = new Input(document);

        for (Preprocessor preproc : this.preprocessors) {            
            preproc.preprocess(session, input);

            /* Debugging info */

            if (LOGGER.isLoggable(Logger.TRACE)) {
                String name = preproc.getClass().getSimpleName();
                
                for (String key : input.keySet()) {
                    Object value = input.get(key);
                    if (value.getClass().isArray())
                        LOGGER.trace(String.format("%s: Preprocessing pipeline (%s): %s [%s]", prefix, name, key, String.join(", ", (String[]) value)));
                    else
                        LOGGER.trace(String.format("%s: Preprocessing pipeline (%s): %s [%s]", prefix, name, key, value.toString()));
                }
            }
        }

        return input;
    }


}