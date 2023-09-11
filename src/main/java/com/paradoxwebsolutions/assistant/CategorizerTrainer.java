package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.ResourceAPI;

import java.util.List;

/**
 * Interface for categorizer trainers.
 * <p>Categorizers that use models, such as NLP or Neural Network, require training - a process of setting up the
 * model to predict the user intent for a given input. This process is known as 'Training', and is done in advance
 * of the model being used. The categorizer API provides a method to return a trainer for a given categorizer.
 *
 * @author Peter Smith
 * @see Categorizer
 */
public interface CategorizerTrainer {


    /**
     * Provides training data for the categorizer for a specific intent.
     * <p>This method is called mulitple times, once for each intent and language combination.
     * Adds intent documents to train this categorizer.
     *
     * @param context     the training context
     * @param language    the language for this set of documents
     * @param intent      the name of the intent being trained
     * @param docs        a list of input preprocessors containing the processed documents
     * @throws ApplicationError if any error occurs (e.g. input not valid for this categorizer)
     */
    public abstract void train(final Context context, final String language, final String intent, final List<Input> docs) throws ApplicationError;



    /**
     * Finish training the model.
     * <p>This method is invoked once after all training data has been supplied. It gives the trainer
     * an opportunity to complete the training, save any output files, and update the categorizer as
     * required.
     *
     * @param context  assistant specific context data
     * @throws ApplicationError if any error occured during training
     */
    public void train(final Context context) throws ApplicationError;



    /**
     * Utility class for passing context information through to the trainer.
     */
    public static class Context {
        /** The identity of the assistant being trained */

        public String       identity;


        /** A reference to the resource API */

        public ResourceAPI   resource;


        /** The directory to be used for any data files for this assistant */
        
        public String       modelDir;
    }
}


