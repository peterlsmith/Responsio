package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.Preprocessor;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;

import java.util.regex.Pattern;




/**
 * Preprocessing filter to remove named entity recognition (NER) tags from the input.
 *
 * @author Peter Smith
 */
@PreprocessorIO(input = "document", output = "document")
public class PreprocessorNERFilter extends PreprocessorCopy {

    /** Pattern used to find NER tags */

    final static Pattern tag = Pattern.compile("\\{([a-z0-9_]+):([^\\}]+)\\}");


    @Override
    public Object preprocess(ClientSession session, String input) throws ApplicationError {
        assert input != null : "Null input passed to preprocessor";
        
        return tag.matcher(input).replaceAll("$2");
    }

}