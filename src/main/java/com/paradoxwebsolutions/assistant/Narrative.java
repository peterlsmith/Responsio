package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;

import java.util.List;




/**
 * Manages the client intent history for the purpose of matching against configured stories.
 * <p>This class is used to facilitate the matching of stories against the intent history of
 * the client. Steps within stories are matched against steps within the intent history
 * and scored, to give an overall match 'score' for a given story. The highest scoring story
 * (usually the longest) is considered to be the best fit, and is accepted as being correct.
 * If a story is complete (all steps matched), the matching segment of history can be cleaned
 * up (removed). A new instance of this class is created for each story match attempt.
 *
 * @author Peter Smith
 */
public class Narrative {

    /**
     * Narrative checkpointing utility class.
     *
     * @author Peter Smtih
     */
    public class Checkpoint {

        /**
         * Creates a new narrative checkpoint.
         */
        public Checkpoint() {
            step = Narrative.this.step;
            score = Narrative.this.score;
            action = Narrative.this.action;
        }



        /**
         * Restores the narrative to the state represented by this checkpoint.
         */
        public void restore() {
            Narrative.this.step = step;
            Narrative.this.score = score;
            Narrative.this.action = action;
        }



        /**
        * Returns the step/index position of this checkpoint.
        *
        * @return the current step of this checkpoint.
        */
        public int getIndex() {
            return step;
        }



        /**
        * Returns the score of this checkpoint.
        *
        * @return the current score of this checkpoint.
        */
        public double getScore() {
            return score;
        }


        /** The current 'step' position (index into the history) */

        private int         step;

        /** The current score of the matched history segment */

        private double      score;

        /** The associated action with the last segment of the history */

        private Action      action;
    };



    /** The name of the narrative (taken from the currently matching story, and used only for logging) */

    private String          name;


    /** The current intent history being matched */

    private List<IntentData> history;


    /** Tracks the current location in the intent history during story matching */

    private int             step = 0;


    /** The current 'score' associated with the currently matching story */

    private double          score = 0;


    /** Action associated with last matched step */

    private Action          action = null;



    /**
     * Creates a Narrative instance.
     *
     * @param name     the name of the story against which this intent history matches (or is being matched)
     * @param history  the intent history avilable for matching.
     */
    public Narrative(String name, List<IntentData> history) {
        this.name = name;
        this.history = history;
    }



    /**
     * Returns the name of the story against which this intent history is being matched.
     *
     * @return the name of the story against which this intent history is being matched
     */
    public String getName() {
        return name;
    }



    /**
     * Returns whether or not there are more steps available for matching in the history.
     *
     * @return true if there are more history steps available, false otherwise.
     */
    public boolean hasMore() {
        return step < history.size();
    }



    /**
     * Returns the current step/index position.
     *
     * @return the current step position.
     */
    public int getIndex() {
        return step;
    }



    /**
     * Returns the remaining (unmatched) history steps.
     *
     * @return a list of the unused history steps
     */
    public List<IntentData> getRemaining() {
        return history.subList(step, history.size());
    }



    /**
     * Returns the used intent history.
     *
     * @return  a list of the used intents (the ones that matched against the story)
     */
    public List<IntentData> getUsed() {
        return history.subList(0, step);
    }



    /**
     * Returns the current step in the intent history.
     *
     * @return the current step in the intent history
     */
    public IntentData getIntent() {
        if (step >= history.size())
            return null;
        else
            return history.get(step);
    }



    /**
     * Consumes the current story step and moves on to the next.
     *
     * @return a reference to this Narrative instance
     */
    public Narrative next() {
        assert step < history.size() : "Invalid pop past end of history";
        step++;

        return this;
    }



    /**
     * Creates a checkpoint to saves the current narrative status so that it can be restored
     * at a later point.
     * <p>Some story step implementations may need to backtrack when matching potential paths. In this case, they
     * will need to restore the original state of the narrative when they fail to match after consuming one or
     * more path steps.
     *
     * @return a reference to this Narrative instance
     * @see Checkpoint
     */
    public Checkpoint checkpoint() {
        return new Checkpoint();
    }



    /**
     * Adds an amount to the current score.
     *
     * @param score  the amount to add to the score
     * @return       a reference to this Narrative instance
     */
    public Narrative addScore(double score) {
        this.score += score;

        return this;
    }



    /**
     * Sets the action associated with the current matching story step.
     * <p>This is generally set each time a step in a story matches the
     * intent in the history. If there are subsequent matches, it will be
     * overwritten.
     *
     * @param action  the action to set
     * @return        a reference to this Narrative instance
     */
    public Narrative setAction(Action action) {
        this.action = action;
        
        return this;
    }



    /**
     * Returns the score for the current matching story.
     *
     * @return the current score value
     */
    public double getScore() {
        return score;
    }



    /**
     * Returns the last set action.
     *
     * @return  the last set action
     */
    public Action getAction() {
        return action;
    }

}