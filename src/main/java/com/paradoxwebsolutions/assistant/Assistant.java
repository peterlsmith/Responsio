package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.assistant.actions.ActionUtter;


import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.Config;


import java.util.ArrayList;
import java.util.List;



/**
 * Primary assistant configuration.
 * <p>This class manages the main configuration of a chatbot. It specifies the assistant 
 * identity (name), input processing pipeline, intents and intent categorizers, NERs,
 * stories, utterances, and other miscellaneous data.
 * 
 * @author Peter Smith
 */
public class Assistant {

    /** The identity (name) of this chatbot */

    private String                  identity;


    /** The user input pre-processing pipeline */

    private List<Preprocessor>      preprocessors = new ArrayList<Preprocessor>();


    /** The categorizers used to process/identify user input */

    private Categorizers            categorizers = new Categorizers();


    /** The configured user intents */

    private Intents                 intents = new Intents();


    /** Configured responses */

    private Utterances              utterances = new Utterances();


    /** User stories */

    private Stories                 stories = new Stories();


    /** The intent to use when one could not be determined from the input */

    private String                  defaultIntent = "oos";


    /** Action to execute as a welcome to new users */

    private Action                  welcomeAction = null;


    /** Action to execute as a welcome to returning users */

    private Action                  welcomeBackAction = null;


    /**
     * Create a new assistant with the given identity (name).
     *
     * @param identity  the identity of this assistant.
     */
    public Assistant(String identity) {
        this.identity = identity;
    }



    /**
     * Returns the identity of this assistant.
     * <p>The identity is simply a unique name for the assistant.
     *
     * @return the identity of this assistant.
     */
    public String getIdentity() {
        return this.identity;
    }



    /**
     * Sets the user input preprocessors to be used by this assistant.
     *
     * @param preprocessors  a list of preprocessor instances to be used for processing user input
     *                       prior to categorization.
     */
    public void setPreprocessors(List<Preprocessor> preprocessors) {
        this.preprocessors = preprocessors;
    }



    /**
     * Returns the list of input preprocessors.
     *
     * @return the list of preprocessors for this assistant.
     */
    public List<Preprocessor> getPreprocessors() {
        return this.preprocessors;
    }



    /**
     * Returns the intents for this assistant.
     * 
     * @return  the intents for this assistant.
     * @see Intent
     * @see Intents
     */
    public Intents getIntents() {
        return this.intents;
    }



    /**
     * Return a named intent.
     *
     * @param intent  the name of the intent to return.
     * @return        the named intent, or null if the named intent does not exist.
     * @see Intent
     */
    public Intent getIntent(String intent) {
        return this.intents.get(intent);
    }



    /**
     * Adds a named intent to this assistant.
     *
     * @param name    the name of the intent to add.
     * @param intent  the new intent to add.
     * @see Intent
     */
    public void addIntent(String name, Intent intent) {
        this.intents.put(name, intent);
    }



    /**
     * Sets the utterances for this assistant.
     *
     * @param utterances  the utterances for this assistant.
     * @see Utterances
     * @see Utterance
     */
    public void setUtterances(Utterances utterances) {
        this.utterances = utterances;
    }



    /**
     * Return a named utterance.
     *
     * @param utterance  the name of the utterance to return.
     * @return           the named utterance, or null if the named utterance does not exist.
     * @see Utterance
     */
    public Utterance getUtterance(String utterance) {
        return this.utterances.get(utterance);
    }



    /**
     * Returns the stories for this assistant.
     * 
     * @return  the stories for this assistant.
     * @see Story
     * @see Stories
     */
    public Stories getStories() {
        return this.stories;
    }



    /**
     * Adds a named story to this assistant.
     *
     * @param name    the name of the story to add
     * @param story   the new story to add
     * @see Story
     */
    public void addStory(String name, Story story) {
        this.stories.put(name, story);
    }



    /**
     * Sets the categorizers for this assistant.
     *
     * @param categorizers  the categorizers for this assistant
     * @see Categorizers
     * @see Categorizer
     */
    public void setCategorizers(Categorizers categorizers) {
        this.categorizers = categorizers;
    }



    /**
     * Return a named categorizer.
     *
     * @param name  the name of the categorizer to return.
     * @return      the named categorizer, or null if the named categorizer does not exist.
     * @see Categorizer
     */
    public Categorizer getCategorizer(String name) {
        return this.categorizers.get(name);
    }



    /**
     * Returns the categorizers for this assistant.
     * 
     * @return  the categorizers for this assistant
     * @see Categorizer
     * @see Categorizers
     */
    public Categorizers getCategorizers() {
        return this.categorizers;
    }


 
    /**
     * Returns the default intent for this assistant.
     * <p>The default intent is the intent used when the categorizers cannot determine the
     * user intent from the input.
     *
     * @return the default intent for this assistant.
     */
    public String getDefaultIntent() {
        return this.defaultIntent;
    }



    /**
     * Sets the default intent for this assistant.
     * <p>The default intent is the intent used when the categorizers cannot determine the
     * user intent from the input.
     *
     * @param defaultIntent  the name of the default intent for this assistant.
     * @see Intent
     */
    public void setDefaultIntent(final String defaultIntent) {
        this.defaultIntent = defaultIntent;
    }



    /**
     * Returns the welcome action for this assistant.
     * <p>The welcome action is the action executed when a client connects to the chatbot
     * for the first time (or since any previous session expired).
     *
     * @return the Action to execute when a new client first connects.
     */
    public Action getWelcomeAction() {
        return this.welcomeAction;
    }



    /**
     * Sets the welcome action for this assistant.
     * <p>The welcome action is the action executed when a client connects to the chatbot
     * for the first time (or since any previous session expired).
     *
     * @param welcomeAction the action to execute when welcoming a new user
     */
    public void setWelcomeAction(final Action welcomeAction) {
        this.welcomeAction = welcomeAction;
    }



    /**
     * Returns the welcome back action for this assistant.
     * <p>The welcome back action is the action executed when a client re-connects
     * to the chatbot following a previous session.
     *
     * @return the Action to execute when a client re-connects
     */
    public Action getWelcomeBackAction() {
        return this.welcomeBackAction;
    }



    /**
     * Sets the welcome back action for this assistant.
     * <p>The welcome back action is the action executed when a client re-connects
     * to the chatbot following a previous session.
     *
     * @param welcomeBackAction the action to execute when welcoming a returning user
     */
    public void setWelcomeBackAction(final Action welcomeBackAction) {
        this.welcomeBackAction = welcomeBackAction;
    }
}
