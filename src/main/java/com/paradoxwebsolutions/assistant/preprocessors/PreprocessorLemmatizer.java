package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ServiceAPI;
import com.paradoxwebsolutions.core.annotations.Init;

import java.io.InputStream;
import java.io.IOException;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;

import com.google.gson.annotations.SerializedName;


/**
 * Input preprocessor for performing lemmatization.
 * <p>Note that this lemmatizer requires parts-of-speech (POS) tagging to be performed
 * first.
 *
 * @author Peter Smith
 */
@PreprocessorIO(input = "tokens", output = "lemmas")
public class PreprocessorLemmatizer extends PreprocessorCopy {
   

    /** Name of our dictionary resource */

    private static String resource = "data/models/opennlp/en-lemmatizer.dict";


    /* The OpenNLP lemmatizer (this is assumed to be thread safe) */

    private static DictionaryLemmatizer lemmatizer;


    /* The name of the input POS tags */

    @SerializedName("input-pos")
    private String inputPOS;



    /**
     * Loads the lemmatizer dictionary.
     *
     * @param service  the service API instance (used for logging, etc)
     * @param logger   the assistant logger
     * @throws ApplicationError on error
     */
    @Init
    public static void init(ServiceAPI service, Logger logger) throws ApplicationError {
       /* Load the model */

        try {
            InputStream is = service.getResource(resource);
            if (is == null) throw new ApplicationError(String.format("Missing resource '%s'", resource));
            lemmatizer = new DictionaryLemmatizer(is);
            logger.info(String.format("Loaded PreprocessorLemmatizer dictionary '%s'", resource));
        }
        catch (IOException x) {
            throw new ApplicationError(String.format("Failed to load lemmatizer dictionary '%s'", resource));
        }
    }



    @Override
    public void preprocess(ClientSession session, GenericMap input) {

        final String posName = inputPOS != null ? inputPOS : "pos";

        assert input != null : "Null input passed to preprocessor";
        assert input.get(posName) != null : "POS tagging has not been preformed";

        String[] tokens = (String[]) getInput(input);
        String[] tags = (String[]) input.get(posName);
        assert tags != null : "Invalid POS tag configuration";
        assert tokens.length == tags.length : "Token / POS tag array length mismatch";

        String[] lemmas = lemmatizer.lemmatize(tokens, tags);

        for (int i = 0; i < lemmas.length; ++i) {
            if (lemmas[i].equals("O")) lemmas[i] = tokens[i];
        }

        setOutput(input, lemmas);
    }

}