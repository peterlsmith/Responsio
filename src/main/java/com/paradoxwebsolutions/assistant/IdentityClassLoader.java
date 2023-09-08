package com.paradoxwebsolutions.assistant;

import com.paradoxwebsolutions.core.ApplicationError;
import com.paradoxwebsolutions.core.ServiceAPI;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages identity specific classes.
 * <p>Instances of this class are used to isolate identify specific class (e.g. custsom action classes)
 * from the rest of the system.
 *
 * @author Peter Smith
 */
public class IdentityClassLoader extends ClassLoader {

    /**
     * The service API instance.
     * <p>This is used only for passing through to any initialization function of the loaded
     * classes.
     */
    private ServiceAPI api;



    /**
     * Creates a new IdentityClassLoader instance.
     *
     * @param api  the service API instance
     */
    public IdentityClassLoader(ServiceAPI api) {
        super(IdentityClassLoader.class.getClassLoader());
        this.api = api;
    }



    /**
     * Loads all the class files in a given directory.
     * <p>This method is used to load all the classes for a given identity located
     * in a specific directory.
     *
     * @param dir  the path of the directory to load class files from
     * @return     an array of class instances representing all the classes that were loaded
     * @throws ApplicationError on error
     */
    public Class<?>[] loadClasses(String dir) throws ApplicationError {

        File classDir = new File(dir);
        if (!classDir.isDirectory()) throw new ApplicationError(String.format("Invalid class directory '%s'",  dir));


        /* Get a list of the files and load if a java class */

        File[] files = classDir.listFiles();
        List<Class<?>> classes = new ArrayList<Class<?>>();

        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String className = file.getName().substring(0, file.getName().length() - 6);
                classes.add(loadClass(className, file.getPath()));
            }
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }



    /**
     * Loads a class from a file.
     *
     * @param className  the name of the class to load
     * @param classFile  the file to load the class from
     * @return           a class instance representing the loaded class
     * @throws ApplicationError on error
     */
    public Class<?> loadClass(String className, String classFile) throws ApplicationError {

		try {
    		FileInputStream stream = new FileInputStream(classFile);
	    	int size = stream.available();

    		byte[] byteArr = new byte[size];
	    	DataInputStream in = new DataInputStream(stream);
		    in.readFully(byteArr);
		    in.close();

			Class<?> cls = defineClass(className, byteArr, 0, byteArr.length);
			resolveClass(cls);

            /* Invoke any initialization method */

            try {
                cls.getMethod("init", ServiceAPI.class).invoke(null, api);
            }
            catch (NoSuchMethodException x) {
                try {
                    cls.getMethod("init").invoke(null);
                }
                catch (NoSuchMethodException x2) {
                }
            }

            return cls;

		} catch (Exception e) {
			throw new ApplicationError(String.format("Failed to load class from file '%s': %s", classFile, e.getMessage()));
		}
    }
}

