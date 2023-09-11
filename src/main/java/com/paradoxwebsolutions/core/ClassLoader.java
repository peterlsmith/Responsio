package com.paradoxwebsolutions.core;

import com.paradoxwebsolutions.core.ApplicationError;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

/**
 * Custom class loader that can be used to load classes from a variety of sources for the
 * purpose of isolating them from the system class loader. 
 * <p>Note that this is not a typical delegating class loader.
 *
 * @author Peter Smith
 */
public class ClassLoader extends java.lang.ClassLoader {


    /**
     * Creates a new ClassLoader instance.
     */
    public ClassLoader() {
        super(ClassLoader.class.getClassLoader());
    }



    /**
     * Loads a class from a file.
     *
     * @param file  the file to load the class from.
     * @return      a class instance representing the loaded class.
     * @throws ApplicationError on error
     */
    public Class<?> loadClass(final File file) throws ApplicationError {

		try {
    		FileInputStream stream = new FileInputStream(file);
            return loadClass(stream);

		} catch (IOException e) {
			throw new ApplicationError(String.format("Failed to load class from file '%s'", file.getName()), e);
		}
    }



    /**
     * Loads a class from a stream.
     *
     * @param stream  the input stream to read the class definition from
     * @return           a class instance representing the loaded class
     * @throws ApplicationError on error
     */
    public Class<?> loadClass(final InputStream stream) throws ApplicationError {

		try {
	    	int size = stream.available();

    		byte[] byteArr = new byte[size];
	    	DataInputStream in = new DataInputStream(stream);
		    in.readFully(byteArr);
		    in.close();

			Class<?> cls = defineClass(null, byteArr, 0, byteArr.length);
			resolveClass(cls);

            return cls;

		} catch (Exception e) {
			throw new ApplicationError("Failed to load class from stream", e);
		}
    }
}

