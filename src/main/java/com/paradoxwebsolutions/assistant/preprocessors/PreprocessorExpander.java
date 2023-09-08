package com.paradoxwebsolutions.assistant.preprocessors;


import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ObjectFactory;
import com.paradoxwebsolutions.core.StringMap;
import com.paradoxwebsolutions.core.annotations.Init;


import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 * Input preprocessor to expand contractions such as I'm, can't, etc.
 * <p>Expansion data is loaded from data files and can be configured per language.
 *
 * @author Peter Smith
 */
@PreprocessorIO(input = "document", output = "document")
public class PreprocessorExpander extends PreprocessorCopy {

    /** Utility class for managing generics */

    private static class LanguageMap extends HashMap<String, StringMap> {};


    /** Per-language expansion maps */

    private static LanguageMap expansions;


    /**
     * Class initialization method that loads the language expansion maps.
     *
     * @param logger  the application logger
     * @throws ApplicationError on error
     */
    @Init
    public static void init(Logger logger) throws ApplicationError {

        /* Load our expansion maps */
        
        final String name = PreprocessorExpander.class.getSimpleName();
        final InputStream in = PreprocessorExpander.class.getResourceAsStream(name + ".json");
        if (in == null) throw new ApplicationError(String.format("Cannot find resource for %s", "PreprocessorExpander"));

        expansions = new ObjectFactory().fromJson(in, LanguageMap.class);

        logger.info(String.format("Loaded %s data file. Supported languages: %s", name, String.join(",", expansions.keySet())));
    }



    @Override
    public Object preprocess(ClientSession session, String doc) {
        assert doc != null : "Null input passed to default preprocessor";

        final StringMap map = expansions.get(session.getLanguage());

        if (map != null &&doc.indexOf('\'') != -1) {
            for (Map.Entry<String, String> replacement : map.entrySet()) {
                doc = doc.replace(replacement.getKey(), replacement.getValue());
            }
        }
        return doc;
    }
}

    
