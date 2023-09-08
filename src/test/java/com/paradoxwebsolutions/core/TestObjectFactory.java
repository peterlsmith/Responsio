package com.paradoxwebsolutions.core;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;


public class TestObjectFactory {
  
    /**
     * Basic constructor test
     */
    @Test public void test_member_ctor() {

        ObjectFactory factory = new ObjectFactory();
        
        assertNotEquals("Created instance", factory, null);
    }


    /**
     * Basic serialization test
     */
    @Test public void test_member_toJson_string() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        String value = "hello";

        String json = factory.toJson(value);
        assertEquals("Simple string serialization", "\"hello\"", json);
    }


    /**
     * Basic serialization test
     */
    @Test public void test_member_toJson_int() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        assertEquals("Simple negative integer serialization", "-10", factory.toJson(-10));
        assertEquals("Simple positive integer serialization", "31", factory.toJson(31));
    }


    /**
     * Basic serialization test
     */
    @Test public void test_member_toJson_boolean() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        assertEquals("Simple boolean serialization", "false", factory.toJson(false));
        assertEquals("Simple boolean serialization", "true", factory.toJson(true));
    }


    /**
     * Basic serialization test
     */
    @Test public void test_member_toJson_double() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        assertEquals("Simple negative float serialization", "-1.86", factory.toJson(-1.86));
        assertEquals("Simple positive float serialization", "31.44", factory.toJson(31.44));
    }


    /**
     * Basic serialization test
     */
    @Test public void test_member_toJson_string_array() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        String[] value = new String[] {"goodbye", "cruel", "world"};

        String json = factory.toJson(value);
        assertTrue(json.contains("\"goodbye\""));
        assertTrue(json.contains("\"cruel\""));
        assertTrue(json.contains("\"world\""));
    }



    /**
     * Basic deserialization test
     */
    @Test public void test_member_fromJson_string() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        String josn = "\"hello\"";

        String value = factory.fromJson(josn, String.class);
        assertEquals("Simple string deserialization", "hello", value);
    }


    /**
     * Basic deserialization test
     */
    @Test public void test_member_fromJson_int() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        assertEquals("Simple negative integer deserialization", Integer.valueOf(-10), factory.fromJson("-10", Integer.class));
        assertEquals("Simple positive integer deserialization", Integer.valueOf(31), factory.fromJson("31", Integer.class));
    }


    /**
     * Basic deserialization test
     */
    @Test public void test_member_fromJson_boolean() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        assertEquals("Simple boolean deserialization", Boolean.valueOf(false), factory.fromJson("false", Boolean.class));
        assertEquals("Simple boolean deserialization", Boolean.valueOf(true), factory.fromJson("true", Boolean.class));
    }


    /**
     * Basic deserialization test
     */
    @Test public void test_member_fromJson_double() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        assertEquals("Simple negative float deserialization", Double.valueOf(-1.86), factory.fromJson("-1.86", Double.class));
        assertEquals("Simple positive float deserialization", Double.valueOf(31.44), factory.fromJson("31.44", Double.class));
    }


    /**
     * Basic deserialization test
     */
    @Test public void test_member_fromJson_int_array() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        int[] array = factory.fromJson("[5,2,-11,1]", int[].class);
        assertArrayEquals(new int[] {5,2,-11,1}, array);
    }


    /**
     * Compound serialization test
     */
    @Test public void test_member_toJson_object_1() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        JsonObject json = toJsonObject(factory.toJson(compound()));

        assertTrue(json.has("s"));
        assertEquals("never say never again", json.get("s").getAsString());

        assertTrue(json.has("i"));
        assertEquals(-55, json.get("i").getAsInt());

        assertTrue(json.has("d"));
        assertEquals(3.14, json.get("d").getAsDouble(), 0.0001);

        assertTrue(json.has("b"));
        assertEquals(false, json.get("b").getAsBoolean());
    }



    /**
     * Compound deserialization test
     */
    @Test public void test_member_fromJson_object_1() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        compound_ c = factory.fromJson("{\"s\":\"thunderball\",\"i\":78909689,\"d\":3.1415926535,\"b\":true}", compound_.class);
        assertEquals("thunderball", c.s);
        assertEquals(78909689, c.i);
        assertEquals(3.1415926535, c.d, 0.0000001);
        assertEquals(true, c.b);
    }



    /**
     * Clone test
     */
    @Test public void test_member_clone_object() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        nested_ o1 = nested();
        nested_ o2 = factory.clone(o1);

        assertNotEquals(o1, o2);

    }



    /**
     * Custom serializer test
     */
    @Test public void test_member_registerHandler_serializer() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);
        //serialize(T t, Type type, JsonSerializationContext context)

        ObjectFactory.Serializer<compound_> serializer = (t, type, context) -> new JsonPrimitive("CUSTOM");
        factory.registerHandler(compound_.class, serializer);
        nested_ o = nested();

        String json = factory.toJson(o);
        assertTrue(json.contains("\"c\": \"CUSTOM\""));
    }



    /**
     * Custom deserializer test
     */
    @Test public void test_member_registerHandler_deserializer() throws Exception {

        ObjectFactory factory = new ObjectFactory();
        assertNotEquals("Created instance", factory, null);

        ObjectFactory.Deserializer<compound_> deserializer = (json, type, context) -> compound("goldfinger", 10, -7.77, true);
        factory.registerHandler(compound_.class, deserializer);

        String json = "{ \"c\": \"CUSTOM\", \"s\": \"the world is not enough\", \"i\": 5436, \"d\": -5.97172365, \"b\": false }";
        nested_ o = factory.fromJson(json, nested_.class);

        assertEquals("the world is not enough", o.s);
        assertEquals(5436, o.i);
        assertEquals(-5.97172365, o.d, 0.000001);
        assertEquals(false, o.b);

        assertEquals("goldfinger", o.c.s);
        assertEquals(10, o.c.i);
        assertEquals(-7.77, o.c.d, 0.000001);
        assertEquals(true, o.c.b);
    }



    /** Utility method to create a compound object */

    private static compound_ compound() {
        return new compound_();
    }

    private static compound_ compound(String s, int i, double d, boolean b) {
        compound_ c = new compound_();
        c.s = s;
        c.i = i;
        c.d = d;
        c.b = b;
        return c;
    }

    private static nested_ nested() {
        return new nested_();
    }

    /* Utility method to convert a json string into a json object */

    private static JsonObject toJsonObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
}


/**
 * Support class for testing compound object operations
 */
@Ignore class compound_ {
    public String   s = "never say never again";
    public int      i = -55;
    public double   d = 3.14;
    public boolean  b = false;
};


/**
 * Support class for testing nested object operations
 */
@Ignore class nested_ {
    public compound_    c = new compound_();
    public String       s = "tomorrow never dies";
    public int          i = 1357;
    public double       d = -5.97172365;
    public boolean      b = true;
};
