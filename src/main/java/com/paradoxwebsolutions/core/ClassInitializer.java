package com.paradoxwebsolutions.core;

import com.paradoxwebsolutions.core.annotations.Init;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import java.util.stream.IntStream;


/**
 * Handles class level initialization for code 'modules'.
 * <p>Code modules are simply java classes that are not explicitly
 * referenced by any active code (and are thus not automatically loaded and initialized). Code
 * modules generally provide functionality extensions or implementations of interfaces, 
 * and are generally loaded then have an initialization method invoked so that they can
 * register their functionality.
 *
 * @author Peter Smith
 */
public class ClassInitializer {

    /**
     * Initialize a class.
     * <p>This method looks for static methods with the @Init annotation and invokes them with
     * any of the supplied arguments.
     * 
     * @param cls     the class to be initialized
     * @param args    an array of typed arguments available to be passed through to any annotated method
     * @throws ApplicationError if the module could not be loaded for any reason
     */
    public void initialize(final Class<?> cls, final Object ...args) throws ApplicationError {
        try {

            /* Scan the class static methods for initialization (the @Init annotation) */

            Method[] methods = cls.getDeclaredMethods();
            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers())) {
                    Init annotation = method.getAnnotation(Init.class);
                    if (annotation != null) {
                        Object[] input = buildArgumentList(method, args);
                        method.invoke(null, input);
                    }
                }
            }
        }
        catch (Exception x) {
            throw new ApplicationError(String.format("Failed to initialize class '%s'", cls.getName()), x);
        }
    }


    /**
     * Invoke a method with some arguments.
     * <p>This implementation can invoke either a static or an instance method, and allows
     * flexibility in the arguments required by the method being invoked.
     *
     * @param instance    the object instance on which the method is to be invoked (null if the method is static)
     * @param method      the method to be invoked
     * @param arguments   any additional arguments that should be passed through to the method if needed
     * @throws ApplicationError if the method could not be found or invoked
     */
    protected static void invokeMethod(final Object instance, final Method method, final Object[] arguments) throws ApplicationError {

        try {
            Object[] input = buildArgumentList(method, arguments);
            method.invoke(instance, input);
        }
        catch (Exception x) {
            throw new ApplicationError(
                String.format(
                    "Failed to invoke method '%s' on instance of %s: %s",
                    method.getName(),
                    instance.getClass().getName(),
                    x.getMessage()),
                x);
        }
    }



    /**
     * Builds an argument list for a method invocation.
     * <p>This implementation allows flexibility in the arguments required by the method being invoked.
     * It matches available arguments provided by the caller to the parameters required by the method
     * using a closest match (least derived) approach. Not all arguments need to be used, but any given
     * arguments can be used once only. Ordering is not important, with the expection that if two arguments
     * provide an identical match to a given parameter, the first in the argument list will be used.
     * This allows a call such as:
     * <pre>
     *    ClassInitializer.invokeMethod(method, A a, B b)
     * </pre>
     * to match all the following methods:
     * <pre>
     *    method()
     *    method(A a)
     *    method(B b)
     *    method(A a, B b)
     *    method(B b, A a)
     * </pre>
     *
     *
     * @param method      the method that will be invoked
     * @param arguments   any arguments that should be passed through to the method if needed
     * @return            an argument list as an array of object that matches the method
     * @throws ApplicationError if a suitable argument list could not be created
     */
    private static Object[] buildArgumentList(final Method method, final Object[] arguments) throws ApplicationError {

        final Object[] args = Arrays.copyOf(arguments, arguments.length);

        try {
            /* Loop through each parameter in the method */

            return Arrays.stream(method.getParameters())

                /* Get the parameter type (class) */

                .map((parameter) -> parameter.getType())

                /* Now patch the parameter type to the available arguments */

                .map(
                    /*
                     * Loop through each argument. If we stream the arguments directly, we do not have the array index value needed
                     * to set the array value to null if the argument is used. If we use an IntStream, we have no ability to map it
                     * back to an object at the end. Thus we start of with an IntStream and map it to a stream of Integer objects. 
                     * This gives us the functionality of an Object stream while still allowing us to easily index the argument
                     * values (taking advantage of autoboxing/unboxing).
                     */
                    (type) -> IntStream.range(0, args.length).mapToObj((i) -> Integer.valueOf(i))

                        /*
                         * Exclude arguments that cannot be assigned to the parameter class. Note that we exclude null argument values since
                         * once an argument is used, we set it to null in the argument array so it cannot be used again (the array itself must
                         * be final).
                         */
                        .filter((i) -> args[i] != null && type.isAssignableFrom(args[i].getClass()))


                        /*
                         * Of those that can be assigned, select the closest argument, or, if two same-type arguments, select the first. Note that
                         * this reduce method will return an empty Optional if the previous filter returned nothing.
                         */
                        .reduce((l, r) -> ((args[l].getClass() != args[r].getClass() && args[r].getClass().isAssignableFrom(args[l].getClass()))) ? r : l)


                        /*
                         * At this point, we have reduced the list of matching arguments to a single Optional. Now remove the argument from the
                         * argument list so it does not get reused. Note that if the reduction above resulted in an empty Optional, the map will
                         * not get invoked.
                         */
                        .map((i) -> {
                            Object value = args[i];
                            args[i] = null;
                            return value;
                        })

                        /**
                         * Force the value to be resolved. If no match was found, we have an empty Optional and this will throw a
                         * NoSuchElementException exception and abort the whole process (as we want).
                         */
                        .get()
                    )

                /* Collect into an array */

                .toArray();
        }
        catch (NoSuchElementException x) {
            throw new ApplicationError(String.format("Failed to build argument list for method '%s' for class '%s'",
                method.getName(),
                method.getDeclaringClass().getName()));
        }
    }
}
