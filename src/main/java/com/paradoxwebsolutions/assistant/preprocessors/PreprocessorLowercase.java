package com.paradoxwebsolutions.assistant.preprocessors;


import com.paradoxwebsolutions.assistant.ClientSession;



/**
 * Preprocessing filter to convert client input to lower case.
 *
 * @author Peter Smith
 */
@PreprocessorIO(input = "document", output = "document")
public class PreprocessorLowercase extends PreprocessorCopy {

    @Override
    public Object preprocess(ClientSession session, String input) {
        assert input != null : "Null input passed to preprocessor";

        return input.toLowerCase();
    }

}