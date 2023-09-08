package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.Assistant;
import com.paradoxwebsolutions.assistant.Preprocessor;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ServiceAPI;
import com.paradoxwebsolutions.core.annotations.Init;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;


/**
 * Input preprocessor to perform parts-of-speech tagging.
 * <p>This is an OpenNLP based parts of speech tagger.
 *
 * @author Peter Smith
 */
@PreprocessorIO(input = "tokens", output = "pos")
public class PreprocessorPOS extends PreprocessorCopy {

    /** Name of our model resource */

    private static final String resource = "data/models/opennlp/%s-pos-maxent.bin";


    /** Language models used for POS tagging */

    private static final Map<String, POSModel> posModels = new HashMap<String, POSModel>();


    /** Map used to simplify pos tags down to simple noun/verb */

    static Map<String, String> posMap = Stream.of(new String[][] {
            { "VB", "VB" }, 
            { "VBD", "VB" }, 
            { "VBG", "VB" }, 
            { "VBN", "VB" }, 
            { "VBP", "VB" }, 
            { "VBZ", "VB" }, 
            { "VH", "VB" }, 
            { "VHD", "VB" }, 
            { "VHG", "VB" }, 
            { "VHN", "VB" }, 
            { "VHP", "VB" }, 
            { "VHZ", "VB" }, 
            { "VV", "VB" }, 
            { "VVD", "VB" }, 
            { "VVG", "VB" }, 
            { "VVN", "VB" }, 
            { "VVP", "VB" }, 
            { "VVZ", "VB" },
            { "NN", "NN" }, 
            { "NNS", "NN" }, 
            { "NP", "NN" }, 
            { "NPS", "NN" }
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));


    /**
     * Loads the POS models.
     *
     * @param service  the service API instance (used for logging, etc)
     * @param config   application level configuration
     * @param logger   the assistant logger
     * @throws ApplicationError on error
     */
    @Init
    public static void init(ServiceAPI service, Config config, Logger logger) throws ApplicationError {

        /* Load the available POS models */

        try {
            final String[] langs = config.getList("opennlp.pos.languages", new String[] {"en"});

            for (String lang : langs) {
                String modelResource = String.format(resource, lang);
                InputStream is = service.getResource(modelResource);

                if (is == null) throw new ApplicationError(String.format("No POS language model '%s'", modelResource));
                {
                    logger.info(String.format("Loading PreprocessorPOS model '%s'", modelResource));
                    posModels.put(lang, new POSModel(is));
                }
            }
        }
        catch (IOException x) {
            throw new ApplicationError(String.format("Failed to load pos model '%s'", resource));
        }
    }


    /** The POS tagger */

    private transient POSTaggerME posTagger = null;


    /** Flag indicating whether or not to simplify (reduce) the pos tag set */

    private boolean  reduce = false;



    /**
     * Initialization method used to load the POS models.
     * <p>This method is invoked as part of assistant initialization.
     *
     * @param config     assistant specific configuration
     * @throws ApplicationError on error
     */
    @Init
    public void init(Config config) throws ApplicationError {
        String lang = config.getString("lang", "en");
        if (!posModels.containsKey(lang)) throw new ApplicationError(String.format("Unsupported POS language '%s'", lang));

        posTagger = new POSTaggerME(posModels.get(lang));
    }



    /**
     * Performs parts-of-speech tagging on the client input
     */
    @Override
    public Object preprocess(ClientSession session, String[] input) {
        assert input != null : "Null input passed to preprocessor";

        String[] tags;

        synchronized(posTagger) { tags = posTagger.tag(input); }

        if (this.reduce) {
            for (int i = 0; i < tags.length; ++i) {
                if (posMap.containsKey(tags[i])) tags[i] = posMap.get(tags[i]);
            }
        }

        return tags;
    }

}