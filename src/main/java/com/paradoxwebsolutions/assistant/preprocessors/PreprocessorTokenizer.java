package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.Preprocessor;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.ServiceAPI;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Input preprocessor to perform tokenization.
 * <p>This splits an input string into tokens based on a configurable set of delimiters.
 *
 * @author Peter Smith
 */
@PreprocessorIO(input = "document", output = "tokens")
public class PreprocessorTokenizer extends PreprocessorCopy {

    /** The delimiters to use for splitting the input string */

    private String delimiters = "[\\s,.!?]+";


    @Override
    public Object preprocess(ClientSession session, String input) {
        assert input != null : "Null input passed to preprocessor";

        return Arrays.stream(input.split(delimiters)).filter(w -> w.length() > 0).toArray(String[]::new);
    }

}