package com.paradoxwebsolutions.assistant.preprocessors;

import com.paradoxwebsolutions.core.GenericMap;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.gson.JsonObject;


public class TestPreprocessorDefault {


    private static final String document1 = "Hello, my name is Bob!";
    private static final String[] tokens1 = new String[] {"hello", "my", "name", "is", "bob"};

    /**
     * Basic constructor test
     */
    @Test public void testPreprocessorDefaultConstructor() {

        PreprocessorDefault proc = new PreprocessorDefault();
        
        assertNotEquals("Created instance", proc, null);
    }


    /**
     * Basic constructor test
     */
    @Test public void testPreprocessorDefaultProcess1() {

        PreprocessorDefault proc = new PreprocessorDefault();
        assertNotEquals("Created instance", proc, null);

        GenericMap input = new GenericMap();
        input.put("document", document1);

        proc.preprocess(null, input);

        assertTrue("token output has been produced", input.containsKey("tokens"));
        assertEquals("token output is an array of strings", String[].class, input.get("tokens").getClass());

        String[] tokens = (String[]) input.get("tokens");
        assertArrayEquals("Token output as expected", tokens1, tokens);
    }

}