package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.assistant.Preprocessor;
import com.paradoxwebsolutions.assistant.ClientSession;
import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.GenericMap;


/**
 * Input preprocessor that can be used to combine multiple input fields into
 * a single output field.
 * <p>This preprocessor only works on tokenized (string array) input fields, and
 * produces a string array output field. It uses String.format (similar to 'printf')
 * for formatting.
 *
 * @author Peter Smith
 */
public class PreprocessorFormatter extends Preprocessor {


    /** Formatting string, as taken by String.format */

    private String format;


    /** Output parameter name (defaults to 'tokens') */

    private String output;


    /** Arguments to pass in to formatting string */

    private String[] inputs;



    @Override
    public void preprocess(ClientSession session, GenericMap input) throws ApplicationError {
        assert input != null : "Null input passed to preprocessor";
        assert inputs != null : "Format inputs not configured";
        assert inputs.length > 0 : "No format inputs configured";

        int len = ((Object[]) input.get(inputs[0])).length;
        String format = (this.format == null || this.format.length() == 0) ? "%s".repeat(len) : this.format;
        String output = (this.output == null || this.output.length() == 0) ? "tokens" : this.output;


        /* Copy the input data being formatted into a more convenient format */

        String[][] data = new String[len][inputs.length];
        for (int i = 0; i < inputs.length; ++i) {
            String[] param = (String[]) input.get(inputs[i]);
            if (param.length != len) throw new ApplicationError("Unequal input array lengths");
            for (int j = 0; j < len; ++j) data[j][i] = param[j];
        }


        /* Format the data to the new output */

        String[] outputs = new String[len];

        for (int i = 0; i < len; ++i) {
            outputs[i] = String.format(format, (Object[]) data[i]);
        }

        input.put(output, outputs);
    }

}