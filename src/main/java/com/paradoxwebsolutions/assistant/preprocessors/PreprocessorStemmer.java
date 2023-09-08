package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.Preprocessor;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.ServiceAPI;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import opennlp.tools.stemmer.PorterStemmer;



/**
 * Input preprocessor to stem words.
 * <p>This preprocessor is an OpenNLP implementation of a word stemmer.
 * Note that this implementation only supports the English language.
 */
@PreprocessorIO(input = "tokens", output = "tokens")
public class PreprocessorStemmer extends PreprocessorCopy {

    
    /* Stemmer */

    private transient PorterStemmer stemmer = new PorterStemmer();


    @Override
    public Object preprocess(ClientSession session, String[] input) {
        assert input != null : "Null input passed to preprocessor";

        return Arrays.stream(input).map(w -> this.stemmer.stem(w)).toArray(String[]::new);
    }

}