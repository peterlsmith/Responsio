package com.paradoxwebsolutions.assistant.categorizers;


import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.Categorizer;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.assistant.Input;
import com.paradoxwebsolutions.assistant.IntentScores;
import com.paradoxwebsolutions.assistant.Trainer;
import com.paradoxwebsolutions.assistant.categorizers.trainers.CategorizerDefaultTrainer;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ResourceAPI;
import com.paradoxwebsolutions.core.StringMap;
import com.paradoxwebsolutions.core.annotations.Init;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;




/**
 * A default implementation of an intent categorizer, based on OpenNLP.
 *
 * @author Peter Smith
 */
public class CategorizerDefault extends Categorizer {

    /** The default confidence threshold needed to identify an intent */

    private double defaultConfidenceThreshold = 0.75;


    /** The pipeline input to use for categorization */

    private String input = "tokens";


    /** The pipeline input to use for training */

    private String trainingInput = "tokens";


    /** The supported languages and model files */

    private StringMap models = new StringMap();


    /** The categorizers for supported langauges */

    private transient Map<String, DocumentCategorizerME> categorizers = new HashMap<String, DocumentCategorizerME>();



    /**
     * Custom initialization (called after deserialization is complete).
     *
     * @param assistant  the assistant to which this categorizer belongs
     * @param resource   resource API for loading data files
     * @param logger     the identity specific logger for outputting messages
     * @throws ApplicationError on error
     */
    @Init
    public void init(Assistant assistant, ResourceAPI resource, Logger logger) throws ApplicationError {

        /* Loop through configured languages and load categorizer models */

        for (String language : models.keySet()) {
            String filename = models.get(language);
            logger.info(String.format("Loading categorizer model '%s", filename));

            try {
                DoccatModel model = new DoccatModel(resource.getInputStream(filename));
                this.categorizers.put(language, new DocumentCategorizerME(model));
            }
            catch (Exception x) {
                throw new ApplicationError(String.format("Failed to load categorizer model '%s'", filename, x));
            }
        }
    }



    /**
     * Identifies the intent of the client input.
     *
     * @param session  the client session
     * @param input    the preprocessed client input
     * @return         An IntentScores instance containing the 0-1 based scores of the most relevant intents
     * @throws         ApplicationError on error
     */
    public IntentScores getIntent(final ClientSession session, final Input input) throws ApplicationError {

        String[] tokens = (String[]) input.get(this.input);
        session.debug("CategorizerDefault categorizing: " + String.join(",", tokens));


        /* Get the cateogizer for the selected language */

        String language = session.getSessionData().getLanguage();
        DocumentCategorizerME categorizer = categorizers.get(language);


        /* Score the intents */

        Map<String, Double> catScores;
        synchronized(categorizer) {
            catScores = categorizer.scoreMap(tokens);
        }
        session.debug("CategorizerDefault scores: " + String.join(":", catScores.entrySet().stream().map(e -> e.getKey() + "(" + e.getValue() + ")").collect(Collectors.toList())));


        /* Discard anything that doesn't meet our threshold */

        IntentScores scores = new IntentScores();
        for (Map.Entry<String, Double> score : catScores.entrySet()) {
            if (score.getValue() >= defaultConfidenceThreshold) scores.put(score.getKey(), score.getValue());
        }

        return scores;
    }



    /**
     * Sets the name of the categorizer model file for a given lanaguage.
     *
     * @param language  the language to set the mode file for
     * @param model     the name of the model file for the language
     */
    public void setModel(final String language, final String model) {
        this.models.put(language, model);
    }



    /**
     * Returns a trainer (if required) for this categorizer.
     *
     * @return a trainer instance for this categorizer
     */
    public Trainer getTrainer() {
        return new CategorizerDefaultTrainer(this, input);
    }
}