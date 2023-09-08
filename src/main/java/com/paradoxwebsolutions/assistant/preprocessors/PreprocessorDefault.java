package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.Preprocessor;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.GenericMap;

import java.util.Arrays;


/**
 * Default input preprocessor.
 * <p>This provides a very basic default processing pipeline that converts everything
 * to lower case and splits on basic punctuation. It is not suitable for anything other
 * than very basic systems.
 *
 * @author Peter Smith
 */
public class PreprocessorDefault extends Preprocessor {

    @Override
    public void preprocess(ClientSession session, GenericMap input) {
        assert input != null : "Null input passed to default preprocessor";

        String sentence = (String) input.get("document");
        input.put("tokens", tokenize(sentence));
    }


    /**
     * Tokenize a string.
     * <p>This converts the input to lower case and splits it into tokens
     * on a limited set of punctuation marks.
     *
     * @param sentence  the input string to tokenize
     * @return an array of string tokens
     */
    private String[] tokenize(String sentence) {
        return Arrays.stream(sentence.toLowerCase().split("[\\s,.!?]+")).filter(w -> w.length() > 0).toArray(String[]::new);
    }
}