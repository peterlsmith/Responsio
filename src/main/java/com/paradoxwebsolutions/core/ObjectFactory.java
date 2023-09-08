package com.paradoxwebsolutions.core;


import com.paradoxwebsolutions.core.ApplicationError;


import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Serializes object instances to and from json.
 * Provides methods for serializing objects to and from Json in various forms - strings,
 * files, and Json trees. Custom serializers/deserializers can be configured to control
 * specific object conversion if required. It also provides basic object cloning functionality.
 */
public class ObjectFactory {
    
    /**
     * Interface used for configuring custom class serializers.
     */
    public static interface Serializer<T> {
        /**
         * Serializes an instance of an object to a json elment.
         *
         * @param t        the object instance being serialized
         * @param type     the type of the object being constructed
         * @param context  the Gson serialization context
         * @return         a json element representing the serialized object instance
         * @throws Exception on deserialization error
         */
        public JsonElement serialize(T t, Type type, JsonSerializationContext context) throws Exception;
    }



    /**
     * Interface used for configuring custom class deserializers.
     */
    public static interface Deserializer<T> {

        /**
         * Deserializes an instance of an object from a json elment.
         *
         * @param json     the json element used to construct the object instance (usually a JsonObject instance)
         * @param type     the type of the object being constructed
         * @param context  the Gson deserialization context
         * @return         a type object instance created from the json data
         * @throws Exception on deserialization error
         */
        public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws Exception;
    }


    /** The Gson json tool factory */

    private GsonBuilder gsonBuilder;



    /**
     * Creates a new ObjectFactory instance.
     * <p>Creates a new object factory with default settings. By default, character
     * escaping will be disabled since this class is not intended to generate
     * Json data for inclusion in web pages.
     */
    public ObjectFactory() {
        gsonBuilder = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting();
    }

    

    /**
     * Registers a handler for serializing instances of a given class.
     * <p>This method provides for customization of the serialization processes.
     *
     * @param <T>        the type of object being serialized by the handler.
     * @param cls        the class for objects to be serialized by this serializer.
     * @param serializer the serializer for instance of the class.
     * @return a reference to this object factory instance.
     */
    public <T> ObjectFactory registerHandler(final Class<T> cls, final Serializer<T> serializer) {
        gsonBuilder.registerTypeAdapter(cls, new SerializerWrapper<T>(serializer));
        return this;
    }



    /**
     * Registers a handler for deserializing instances of a given class.
     * <p>This method provides for customization of the deserialization processes.
     *
     * @param <T>          the type of object being deserialized by the handler.
     * @param cls          the class for objects to be deserialized by this deserializer
     * @param deserializer the deserializer for instance of the class
     * @return a reference to this object factory instance.
     */
    public <T> ObjectFactory registerHandler(final Class<T> cls, final Deserializer<T> deserializer) {
        gsonBuilder.registerTypeAdapter(cls, new DeserializerWrapper<T>(deserializer));
        return this;
    }



    /**
     * Load Json data from a file and return as a JsonElement.
     * @param json the json data in string form
     * @return a JsonElement instance representing the json data 
     * @throws ApplicationError if the json could not be loaded or converted
     */
    public JsonElement fromJson(final String json) throws ApplicationError {
        try {
            return JsonParser.parseString(json);
        } catch (Exception x) {
            throw new ApplicationError(String.format("Error parsing json: %s", x.getMessage()));
        }
    }



    /**
     * Load Json data from a reader and return as a JsonElement.
     * @param reader the input reader to read the json data from
     * @return a JsonElement instance representing the json data read
     * @throws ApplicationError if the json could not be loaded or converted
     */
    public JsonElement fromJson(final Reader reader) throws ApplicationError {
        try {
            return JsonParser.parseReader(reader);
        } catch (Exception x) {
            throw new ApplicationError(String.format("Error loading json :%s", x.getMessage()));
        }
    }



    /**
     * Creates a typed object instance from a json string.
     *
     * @param <T>   the type of object being created from the Json string.
     * @param json  the Json string.
     * @param cls   the class of the object to create.
     * @return an instance of the requested type.
     * @throws ApplicationError if the object could not be serialized to an instance of the requested type
     */
    public <T> T fromJson(final String json, final Class<T> cls) throws ApplicationError {
        return fromJson(json, TypeToken.get(cls));
    }



    /**
     * Creates a typed object instance from a file containing Json data.
     *
     * @param <T>   the type of object being created from the file.
     * @param file  the Json file.
     * @param cls   the class of the object to create.
     * @return an instance of the requested type.
     * @throws ApplicationError if the object could not be serialized to an instance of the requested type
     */
    public <T> T fromJson(final File file, final Class<T> cls) throws ApplicationError {
        try {
            return fromJson(fromJson(new FileReader(file)), TypeToken.get(cls));
        }
        catch (Exception x) {
            throw new ApplicationError(String.format("Failed to read file '%s': %s", file.getName(), x.getMessage()), x);
        }
    }



    /**
     * Creates a typed object instance from an input stream containing Json data.
     *
     * @param <T>   the type of object being created from the file.
     * @param in    the input stream.
     * @param cls   the class of the object to create.
     * @return an instance of the requested type.
     * @throws ApplicationError if the object could not be serialized to an instance of the requested type
     */
    public <T> T fromJson(final InputStream in, final Class<T> cls) throws ApplicationError {
        return fromJson(fromJson(new InputStreamReader(in)), TypeToken.get(cls));
    }



    /**
     * Creates a typed object instance from a Json element tree.
     *
     * @param <T>   the type of object being created from the Json element.
     * @param json  the Json element tree.
     * @param cls   the class of the object to create.
     * @return an instance of the requested type.
     * @throws ApplicationError if the object could not be serialized to an instance of the requested type
     */
    public <T> T fromJson(final JsonElement json, final Class<T> cls) throws ApplicationError {
        return fromJson(json, TypeToken.get(cls));
    }


    /**
     * Serializes an object instance to a Json string.
     *
     * @param <T>       the type of object being converted to Json.
     * @param instance  the object instance to serialize.
     * @return          a string containing the Json.
     * @throws ApplicationError if the object could not be serialized for any reason.
     */
    public <T> String toJson(final T instance) throws ApplicationError {
        try {
            return gsonBuilder.create().toJson(instance);
        }
        catch (Exception x) {
            throw new ApplicationError(String.format("Error serializing configuration: %s", x.getMessage()));
        }
    }



    /**
     * Deep clones an object.
     * Note that this method clones by performing object serialization/deserialization which
     * is not very efficient and should only be used when there are no other alternatives.
     *
     * @param <T>       the type of object being cloned.
     * @param instance  the object instance to clone.
     * @return a cloned instance of the object.
     * @throws ApplicationError if the object could not be cloned for any reason.
     */
    public static <T> T clone(final T instance) throws ApplicationError {
        try {
            Gson gson = new Gson();
            Type type = instance.getClass();
            return gson.fromJson(gson.toJsonTree(instance), type);
        }
        catch (Exception x) {
            throw new ApplicationError(String.format("Error serializing configuration: %s", x.getMessage()));
        }
    }



    /**
     * Creates an object instance from a json string.
     *
     * @param <T>   the type of object being created.
     * @param json  the Json string.
     * @param type  the type of the object to create.
     * @return an instance of the requested type.
     * @throws ApplicationError if the object could not be serialized to an instance of the requested type
     */
    private <T> T fromJson(final String json, final TypeToken<T> type) throws ApplicationError {
        return fromJson(fromJson(json), type);
    }


    /**
     * Creates a typed object instance from a Json element tree.
     *
     * @param <T>   the type of object being created.
     * @param json  the Json element tree.
     * @param type  the type of the object to create.
     * @return an instance of the requested type.
     * @throws ApplicationError if the object could not be serialized to an instance of the requested type.
     */
    private <T> T fromJson(final JsonElement json, final TypeToken<T> type) throws ApplicationError {
        assert json != null : "Null json element passed to 'fromJson'";

        try {
            return gsonBuilder.create().fromJson(json, type);
        }
        catch (Exception x) {x.printStackTrace();
            throw new ApplicationError(String.format("Error processing Json: %s", x.getMessage()));
        }
    }


    /**
     * Wraps an ObjectFactory serializer as a JsonSerializer so it can be registered
     * as a type adapter with Gson.
     * Instances of this class are used internally to wrap external object type serializers.
     */
    private class SerializerWrapper<T> implements JsonSerializer<T> {

        /* The external serialization function */

        private ObjectFactory.Serializer<T> serializer;

        /**
         * Create a wrapper with the specified serialization function.
         * @param serializer the serialization function to invoke for instances of the
         *         typed class.
         */
        public SerializerWrapper(final ObjectFactory.Serializer<T> serializer) {
            this.serializer = serializer;
        }


        /**
         * Gson type handler for intercepting output serialization of specific objects.
         */
        public JsonElement serialize(T t, Type type, JsonSerializationContext context)  {
            try {
                return this.serializer.serialize(t, type, context);
            }
            catch (Exception x) {
                throw new JsonParseException(x.getMessage());
            }
        }
    }


    /**
     * Wraps an ObjectFactory deserializer as a JsonDeserializer so it can be registered
     * as a type adapter with Gson.
     * Instances of this class are used internally to wrap external object type deserializers.
     */
    private class DeserializerWrapper<T> implements JsonDeserializer<T> {

        /* The external deserialization function */

        private ObjectFactory.Deserializer<T> deserializer;

        /**
         * Create a wrapper with the specified deserialization function.
         * @param deserializer the deserialization function to invoke for creating instances of the
         *         typed class.
         */
        public DeserializerWrapper(final ObjectFactory.Deserializer<T> deserializer) {
            assert deserializer != null : "Null deserializer passed to constructor";
            this.deserializer = deserializer;
        }

        /**
         * Gson type handler for intercepting instantiation of specific objects.
         */
        public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                return this.deserializer.deserialize(json, type, context);
            }
            catch (Exception x) {x.printStackTrace();
                throw new JsonParseException(x.getMessage());
            }
        }
    }

}
