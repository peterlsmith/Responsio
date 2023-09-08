package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.core.GenericMap;
import com.paradoxwebsolutions.core.ObjectFactory;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.gson.JsonObject;


public class TestPreprocessorLowercase {

    public static String quotes(final String in) {return in.replace('\'', '"');}

    private static final ObjectFactory factory = new ObjectFactory();
    private static final String   input1  = "Hello, my name is john!";
    private static final String   output1 = "hello, my name is john!";


    /**
     * Basic constructor test
     */
    @Test public void testPreprocessorLowercaseConstructor() {

        PreprocessorLowercase proc = new PreprocessorLowercase();
        
        assertNotEquals("Created instance", proc, null);
    }


    /**
     * Basic constructor test
     */
    @Test public void testPreprocessorLowercaseProcess1() throws Exception {

        PreprocessorLowercase proc = factory.fromJson(quotes("{'input': 'in', 'output': 'out'}"), PreprocessorLowercase.class);
        assertNotEquals("Created instance", proc, null);

        GenericMap input = new GenericMap();
        input.put("in", input1);

        proc.preprocess(null, input);

        assertTrue("token output has been produced", input.containsKey("out"));
        assertEquals("token output is an array of strings", String.class, input.get("out").getClass());

        String out = (String) input.get("out");
        assertEquals("Token output as expected", output1, out);
    }

}