package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.ObjectFactory;


import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;


/**
 * Utility class used to serialize {@link Assistant} objects from json.
 * <p>This class extends {@link com.paradoxwebsolutions.core.ObjectFactory} to provide
 * customizer loaders for assistant polymorphic classes by automatically adding a 
 * type attribute on serialization, and by reading the type value on deserialization to
 * create the appropriate class instance.
 *
 * @author Peter Smith
 * @see com.paradoxwebsolutions.core.ObjectFactory
 */
public class AssistantFactory extends ObjectFactory {
  

    /** The default class loader to be used for instantiating objects */

    private ClassLoader classLoader;



    /**
     * Creates an assistant factory instance with a given class loader.
     *
     * @param classLoader  the class loader to use for custom object instantiation.
     */
    public AssistantFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;

        /* Configure custom serializers needed for an Assistant instance */

        registerHandler(Action.class,        new AssistantObjectSerializer<Action>());
        registerHandler(Categorizer.class,   new AssistantObjectSerializer<Categorizer>());
        registerHandler(IntentMatcher.class, new AssistantObjectSerializer<IntentMatcher>());
        registerHandler(NER.class,           new AssistantObjectSerializer<NER>());
        registerHandler(Preprocessor.class,  new AssistantObjectSerializer<Preprocessor>());
        registerHandler(Step.class,          new AssistantObjectSerializer<Step>());

        registerHandler(Action.class,        new AssistantObjectDeserializer<Action>());
        registerHandler(Categorizer.class,   new AssistantObjectDeserializer<Categorizer>());
        registerHandler(IntentMatcher.class, new AssistantObjectDeserializer<IntentMatcher>());
        registerHandler(NER.class,           new AssistantObjectDeserializer<NER>());
        registerHandler(Preprocessor.class,  new AssistantObjectDeserializer<Preprocessor>());
        registerHandler(Step.class,          new AssistantObjectDeserializer<Step>());
    }



    /**
     * Serializes instances of assistant component classes.
     * All assistant component classes follow the same pattern when represented by json
     * and can be serialized by this generic handler.
     */
    private class AssistantObjectSerializer<T> implements ObjectFactory.Serializer<T> {  

        /**
        * Serialization method.
        * @see ObjectFactory.Serializer
        */
        @Override
        public JsonElement serialize(T t, Type type, JsonSerializationContext context) throws Exception {

            JsonElement element = context.serialize(t);
            element.getAsJsonObject().addProperty("type", t.getClass().getName());

            return element;
        }
    }



    /**
     * Customized deserialization support for some assistant config entities.
     */
    private class AssistantObjectDeserializer<T> implements ObjectFactory.Deserializer<T> {

        /**
        * Deserialization method.
        * @see ObjectFactory.Deserializer
        */
        @Override
        public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws Exception {

            /* Get the class name of the object we want to create */

            JsonObject jsonObject = json.getAsJsonObject();
            String className = jsonObject.get("type").getAsString();


            /* Load the appropriate class and deserialize into it */

            Class<?> cls = classLoader.loadClass(className);
            return context.deserialize(jsonObject, cls);
        }
    }
}
