package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.annotations.Init;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;

import com.google.gson.annotations.SerializedName;


/**
 * Language detection preprocessor.
 * <p>This preprocessor is usually used at the very beginning of the processing
 * pipeline and, with properly trained models, can be used to determine the
 * languge being used by the client.
 *
 * @author Peter Smith
 */
@PreprocessorIO(input = "document", output = "lang")
public class PreprocessorLanguage extends PreprocessorCopy {


    /** Confidence level limit in accepting language prediction */

    private double  confidenceLimit = 0.90;


    /** OpenNLP language detector */

    private transient LanguageDetectorME detector;



    /**
     * Loads the language detection model.
     * <p>The language detection model is usually specific to the identity and must therefore
     * be loaded on object initialization rather than class initialization.
     *
     * @param config     the assistant specific configuration
     * @param logger     the assistant logger
     * @throws ApplicationError on error
     */
    @Init
    public void init(Config config, Logger logger) throws ApplicationError {

        if (!config.getBool("training", false)) {
            String filename = "ld-opennlp.bin";
            String modelFile = config.getString("dir.model") + File.separator + filename;
            logger.info(String.format("Loading language model '%s'", filename));

            try {
                detector = new LanguageDetectorME(new LanguageDetectorModel(new File(modelFile)));
            }
            catch (Exception x) {
                throw new ApplicationError(String.format("Failed to load language model '%s': %s", modelFile, x.getMessage()));
            }
        }
    }


    @Override
    public Object preprocess(ClientSession session, String input) {
        assert input != null : "Null input passed to preprocessor";

        Language lang = detector.predictLanguage(input);

        if (lang.getConfidence() >= confidenceLimit) {
            session.debug(String.format("Detected language: %s (%f)", lang.getLang(), lang.getConfidence()));
            session.getSessionData().setLanguage(lang.getLang());
            return lang.getLang();
        }

        session.debug("Language not detected within confidence limit");
        return "en"; /* Default */
    }

}