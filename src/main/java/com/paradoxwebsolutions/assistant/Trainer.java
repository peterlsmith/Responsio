package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ServiceAPI;

import java.util.List;

/**
 * Interface for model trainers.
 * <p>Assistant components that use models, such as NLP or Neural Network, require training - a process of setting up the
 * model to predict the user intent for a given input. This process is known as 'Training', and is done in advance
 * of the model being used. 
 *
 * @author Peter Smith
 */
public interface Trainer {


    /**
     * Adds intent documents to train this component.
     * <p>Provides documents (processed via the input preprocessor pipeline) for a given intent.
     * This method may be invoked multiple times, once for each intent.
     *
     * @param context  assistant specific context data
     * @param language  the language of the training documents
     * @param intent    the name of the intent for these documwents
     * @param docs      a list of the processed documents to be used for training the intent
     * @throws ApplicationError if any error occurs (e.g. input not valid for this component)
     */
    public void train(final Context context, final String language, final String intent, final List<Input> docs) throws ApplicationError;



    /**
     * Finalizes the training for this component.
     * <p>This method is invoked once after all training documents have been provided.
     *
     * @param context  assistant specific context data
     * @throws ApplicationError if any error occured during training
     */
    default public void train(final Context context) throws ApplicationError {
    }



    /**
     * Utility class for passing context information through to the trainer.
     */
    public static class Context {
        /** The identity of the assistant being trained */

        public String       identity;


        /** A reference to the service API */

        public ServiceAPI   service;


        /** The directory to be used for any data files for this assistant */
        
        public String       modelDir;


        /** The logger to be used for any logging messages */
        
        public Logger       logger;


        /** Any training configuration */

        public Config       config;
    }
}


