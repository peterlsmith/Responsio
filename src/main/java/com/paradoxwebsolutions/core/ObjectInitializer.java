package com.paradoxwebsolutions.core;


import com.paradoxwebsolutions.core.annotations.Init;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.lang.annotation.Annotation;


/** 
 * Performs object tree initialization.
 * <p>This class can be used to initialize complete object trees (hierarchical structures of object)
 * by search for, and invoking methods tagged with the {@link com.paradoxwebsolutions.core.annotations.Init}
 * annotation. The annotated methods to be invoked do not have to conform to any specific parameter list.
 * Instead, arguments passed through to the method will be pulled from a set of arguments provided in the
 * initial call. Invoked methods may use any or all of these arguments in any order, but each argument in
 * the pool can be used once only on any given method invocation.
 * Not that this class is not as robust as, say, Spring - it uses reflection to drill down the
 * object hierarchy as well as looking for collection/map interfaces, but there may be cases where it cannot
 * correctly locate all relevant object instances - test well and us with care.
 *
 * @author Peter Smith
 */
public class ObjectInitializer extends ClassInitializer {
    /**
     * Defines the interface used for class filtering.
     * <p>A class filter can be provided to control which class instances may (or may not) be scanned
     * for annotations. By default, objects are restricted to the com.paradoxwebsolutions
     * namespace.
     *
     * @author Peter Smith
     */
    @FunctionalInterface
    public interface Filter {
        /**
         * Class filter function.
         * 
         * @param cls   the Class instance for the class that has been annotated
         * @return true if the class should be scanned further for annotations and child objects, false otherwise
         */
        boolean apply(Class cls);
    }


    /** A record of objects scanned so we can prevent recursion problems */

    private Set<Object> ledger = new HashSet<>();


    /** A class filter */

    private Filter filter;



    /**
     * Creates an ObjectInitializer instance with a default class filter.
     * <p>The default class filter limits scanned object to the com.paradoxwebsolutions namespace.
     *
     */
    public ObjectInitializer() {
        this.filter = (cls) -> cls.getName().startsWith("com.paradoxwebsolutions.");
    }



    /**
     * Creates an ObjectInitializer instance with a given filter.
     *
     * @param filter  the class filter, or null to disable filtering entirely.
     */
    public ObjectInitializer(Filter filter) {
        this.filter = filter;
    }



    /**
     * Initializes an object tree.
     *
     * @param instance   the object instance to initialize
     * @param arguments  a set of arguments that can be passed though to the initialization methods
     * @throws ApplicationError on error
     */
    public void initialize(Object instance, Object ...arguments) throws ApplicationError {
        this.scanObjectHierarchy(Init.class, instance, arguments);
    }



    /**
     * Recurses down an object hierarchy looking for, and invoking, annotated methods.
     *
     * @param <T>              the annotation class instance
     * @param annotationClass  the class of the annotation being searched for
     * @param object           the root of the object hierarchy being scanned
     * @param arguments        a list of available arguments to be used for invoking the methods
     * @throws ApplicationError on error
     */
    private <T extends Annotation>  void scanObjectHierarchy(Class<T> annotationClass, Object object, Object[] arguments) throws ApplicationError {

        /*
         * Check to see if this object should be scanned. By default we do not scan
         * Object or String.
         */
        if (object == null || ledger.contains(object)) return;

        Class<?> cls = object.getClass();
        if (cls.getName().equals("java.lang.Object") || cls.getName().equals("java.lang.String")) return;


        /* We need to scan this object */

        ledger.add(object);


        /*
         * We scan by drilling down through the hierarchy until we hit a class that should not be
         * scanned - usually because it is outside our package and therefore cannot contain any further classes of
         * interest, with the exception of collections.
         */
        while (cls.getSuperclass() != null && (filter == null || filter.apply(cls))) {

            /*
            * First, scan non-static methods in the object to find any that are annotated 
            */
            for (Method method : cls.getMethods()) { /* Interested in all public methods, including those on superclasses */
                if (!Modifier.isStatic(method.getModifiers())) {
                    T annotation = method.getAnnotation(annotationClass);
                    if (annotation != null) invokeMethod(object, method, arguments);
                }
            }



            /* Next, process fields specific to the current class */

            for (Field field : cls.getDeclaredFields()) {
                /* Exclude primitives, enums, and static fields */

                Class<?> fieldClass = field.getType();
                if (fieldClass.isPrimitive() || field.isEnumConstant() || Modifier.isStatic(field.getModifiers())) continue;
                if (!field.canAccess(object) && !field.trySetAccessible()) continue;


                /* Check for a null value, which can be ignored */

                try {
                    /* Now look at the field value. Nulls can be ignored */

                    Object fieldValue = field.get(object);
                    if (fieldValue == null) continue;

                    /* 
                     * If the field is an array, we need to loop through each index
                     */
                    if (fieldClass.isArray()) {
                        Class<?> objectClass = fieldClass.getComponentType();
                        Object[] array = (Object[]) fieldValue;
                        for (int i = 0; i < array.length; ++i) {
                            scanObjectHierarchy(annotationClass, array[i], arguments);
                        }
                    }
                    else {
                            scanObjectHierarchy(annotationClass, fieldValue, arguments);
                    }
                }
                catch (IllegalAccessException x) {
                }
            }

            /* Move up to the superclass */

            cls = cls.getSuperclass();
        }



        /*
         * Now handle collections and maps - these are special cases.
         */
        cls = object.getClass();
        if (Collection.class.isAssignableFrom(cls)) {
            /* This is a collection - we need to process its contents */

            for (Object child : Collection.class.cast(object).toArray()) {
                scanObjectHierarchy(annotationClass, child, arguments);
            }
        }
        else if (Map.class.isAssignableFrom(cls)) {
            
            /* This is a map - we need to process its contents */

            Map<?,?> map = Map.class.cast(object);
            Set<?> set = map.keySet();

            for (Object key : set) {
                scanObjectHierarchy(annotationClass, key, arguments);
                scanObjectHierarchy(annotationClass, map.get(key), arguments);
            }
        }

    }
}