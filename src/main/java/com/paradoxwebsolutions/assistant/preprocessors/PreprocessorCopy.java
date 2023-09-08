package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.assistant.Preprocessor;
import com.paradoxwebsolutions.assistant.ClientSession;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Utility class used to configure which inputs and outputs a 
 * preprocessor will use. 
 * <p>This is primarily used as a base class
 * for other preprocessors, but can be used directly to copy
 * preprocessing outputs. It simplifies the handling of input
 * and output values by doing the type conversion as necessary.
 * Derived classes can take advantage of the simplified process
 * functions or override the primary process method as usual.
 *
 * @author Peter Smith
 */
public class PreprocessorCopy extends Preprocessor {

    /** The default key name of the input data */

    private transient String defaultInput = "tokens";


    /** The default key name for the output data */

    private transient String defaultOutput = "tokens";


    /** The key name of the input data (if configured) */

    private String input;


    /** The key name for the output data (if configured) */

    private String output;



    /**
     * Create an instance of this preprocessor.
     * <p>This constructor looks for the ProcessorIO annotation and, if found, uses it
     * to configure the default input and output fields for the processor.
     */
    public PreprocessorCopy() {
        Class<?> cls = this.getClass();
        Annotation a = cls.getAnnotation(PreprocessorIO.class);

        if (a != null) {
            PreprocessorIO io = (PreprocessorIO) a;
            defaultInput = io.input();
            defaultOutput = io.output();
        }
    }



    /**
     * Sets the default key names to be used for input and output data.
     * <p>This is generally invoked by derived classes to set up default values
     * for when no explicit input/outputkeys are configured.
     *
     * @param input   the key name for the default input
     * @param output  the key name for the default output
     */
    protected void setIODefaults(final String input, final String output) {
        defaultInput = input;
        defaultOutput = output;
    }



    /**
     * Returns the input data.
     *
     * @param input   the preprocessed input data map
     * @return the input data.
     */
    protected Object getInput(GenericMap input) {
        return input.get(this.input != null ? this.input : defaultInput);
    }



    /**
     * Returns whether or not output has been explicitly configured.
     *
     * @return true if output has been configured, false otherwise
     */
    protected boolean hasOutput() {
        return this.output != null;
    }




    /**
     * Sets the output data.
     *
     * @param input   the preprocessed input data map
     * @param value   the value to set as the output of this preprocessor
     */
    protected void setOutput(GenericMap input, Object value) {
        input.put(output != null ? output : defaultOutput, value);
    }



    @Override
    public void preprocess(ClientSession session, GenericMap input) throws ApplicationError {
        assert input != null : "Null input passed to preprocessor";
        assert getInput(input) != null : "Missing preprocessor input";

        Object in = getInput(input);
        Object out;

        if (in.getClass() == String.class)
            out = preprocess(session, (String) in);
        else if (in.getClass() == String[].class)
            out = preprocess(session, (String[]) in);
        else
            throw new ApplicationError(String.format("Unsupported preprocess input type %s", in.getClass().getSimpleName()));

        setOutput(input, out);
    }



    /**
     * Performs the preprocessing action on a string value.
     * <p>This can be overriden by derived classes.
     *
     * @param session  the client session
     * @param input    the input value to process
     * @return the output value as an object
     * @throws ApplicationError on error
     */
    public Object preprocess(ClientSession session, String input) throws ApplicationError {
        return input;
    }



    /**
     * Performs the preprocessing action on a string array value.
     * <p>This can be overriden by derived classes.
     *
     * @param session  the client session
     * @param input    the input value to process
     * @return the output value as an object
     * @throws ApplicationError on error
     */
    public Object preprocess(ClientSession session, String[] input) throws ApplicationError {
        return input;
    }
}



/**
 * Annotation for configuring preprocessor default input and output fields.
 * <p>Classes that derive from this one may use this annotation to specify the
 * default names for the preprocessor input and output fields. This default names
 * only apply if the input and output field names are not explicitly configured.
 *
 * @author Peter Smith
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface PreprocessorIO {
    /**
     * Returns the name of the processor input data.
     * @return  the name of the processor input data
     */
    public String input() default "tokens";


    /**
     * Returns the name of the processor output data.
     * @return  the name of the processor output data
     */
    public String output() default "tokens";
}

