package com.paradoxwebsolutions.assistant.ners;


import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.Input;
import com.paradoxwebsolutions.assistant.NER;
import com.paradoxwebsolutions.assistant.Trainer;
import com.paradoxwebsolutions.assistant.ners.trainers.NERDefaultTrainer;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.StringMap;
import com.paradoxwebsolutions.core.annotations.Init;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;



/**
 * Default implementation (OpenNLP based) of a Named Entity Recognizer.
 *
 * @author Peter Smith
 */
public class NERDefault implements NER {


    /** The list of parameter names for which this NER has been trained */

    private List<String> parameters;


    /** The input stream to use for this NER (defaults to 'tokens' but may be configured) */

    private String input = "tokens";


    /** A map of language model files */

    private StringMap models = new StringMap();


    /* A map of loaded name finders (language is the key) */

    private transient Map<String, NameFinderME>  nameFinders = new HashMap<String, NameFinderME>();


    /**
     * Loads the language model files for this NER instance.
     * 
     * @param config  the assistant specific configuration
     * @param logger  the assistant logger
     * @throws ApplicationError on error loading the language models
     */
    @Init
    public void init(Config config, Logger logger) throws ApplicationError {

        String modelDir = config.getString("dir.model") + File.separator;

        /* Load the name find models for all supported languages */

        for (String language : models.keySet()) {
    
            String modelFilename = modelDir + models.get(language);

            try {

                TokenNameFinderModel nerModel = new TokenNameFinderModel(new File(modelFilename));
                NameFinderME nameFinder = new NameFinderME(nerModel);
                nameFinder.clearAdaptiveData();
                nameFinders.put(language, nameFinder);
                logger.info(
                    String.format("Loaded NER model '%s' for language %s (%s)",
                        models.get(language),
                        language,
                        String.join(", ", parameters)
                        ));
            }
            catch (Exception x) {
                throw new ApplicationError(String.format("Failed to load model file '%s': %s", modelFilename, x.getMessage()));
            }
        }

        if (this.input == null) this.input = "tokens";
    }



    @Override
    public void getEntities(final ClientSession session, final Input input, final StringMap entities) throws ApplicationError {

        /* Get the named entity extractor */

        String[] tokens = (String[]) input.get(this.input);
        String lang = session.getSessionData().getLanguage();

        NameFinderME nameFinder = nameFinders.get(lang);
        Span[] nameSpans;


        /* Do the NER. We have not info on thread safety, so assume the worst */

        synchronized(nameFinder) {
            nameSpans = nameFinder.find(tokens);
            if (nameSpans.length == 0) { /* Not sure why this is an issue on the first call */
                session.debug("No span issue - repeating with clone");
                nameSpans = nameFinder.find(tokens.clone());
            }
        }

        if (nameSpans.length > 0) {
            for(Span span: nameSpans) {
                String entity = Arrays.stream(tokens, span.getStart(), span.getEnd()).collect(Collectors.joining(" "));
                entities.put(span.getType(), entity);
                session.debug(String.format("%s[%s]", span.getType(), entity));
            }
        }
        else {
            session.debug("No named entities found");
        }
    }



    @Override
    public List<String> getParameters() {
        return this.parameters;
    }



    /**
     * Set the parameter list for this NER.
     *
     * @param parameters  the named parameter list for this NER
     */
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }



    /**
     * Set a language model filename for this NER.
     *
     * @param lang   the language for which the model applies
     * @param model  the file name of the model file
     */
    public void setModel(final String lang, final String model) {
        this.models.put(lang, model);
    }



    @Override
    public Trainer getTrainer() {
        return new NERDefaultTrainer(this);
    }

}