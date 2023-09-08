package com.paradoxwebsolutions.bot;

/* Imports */

import com.paradoxwebsolutions.core.Config;
import com.paradoxwebsolutions.core.CustomLogHandler;
import com.paradoxwebsolutions.core.Logger;
import com.paradoxwebsolutions.core.ClassInitializer;
import com.paradoxwebsolutions.core.ServiceAPI;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.lang.NoSuchMethodException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * The main bot service request handler framework.
 * <p>Note that this is expected to be used as a base class for initializaing Bot services.
 * It handles loading the service configuration, setting up the logging, and loading any
 * configured extension modules. Sub classes are generally expected to implement the doGet
 * and doPost methods as required.
 *
 * @author Peter Smith
 */
public class BotService extends HttpServlet implements ServiceAPI {

    /** Logger */
    
    protected Logger LOGGER = null;
   

    /** Configuration properties */
    
    private Config config = new Config();

   
    /** The external (non-webapp) root directory for this application */
    
    private String  rootDir = null;

   
    /** Name of this service */
    
    private String  service = null;

    
    /**
     * Servlet initialization. 
     * <p>This is used to perform common initialization functions such as loading the service configuration
     * file and seting up the logging. It also invokes the 'serviceInit' method which can be overridden by
     * sub classes to perform service specific initialization.
     * @throws ServletException on initialization error
     */ 
    public void init() throws ServletException {
        try {
            /*
             * Get all context and app init parameters. App parameters can
             * override context parameters
             */
            for (String key : Collections.list(getServletContext().getInitParameterNames())) {
                config.setString(key, getServletContext().getInitParameter(key));
            }
            for (String key : Collections.list(getInitParameterNames())) {
                config.setString(key, getInitParameter(key));
            }


            /* Get our service id. Everything else will key off this */

            service = config.getString("service", "service");


            /*
             * Locate our root working directory. This can be configured in the tomcat web.xml,
             * or the application web.xml
             */
            rootDir = config.getString(service + ".workdir");
            if (rootDir == null) throw new ServletException("No working directory configuration for service '" + service + "'");

            File root = new File(rootDir);
            if (!root.isDirectory()) throw new ServletException("Invalid working directory configuration");
            config.setString("dir.root", rootDir);
           

            /* Load service configuration file */
            
            Path cfgPath = Paths.get(rootDir, "cfg", service + ".properties");
            if (!Files.isRegularFile(cfgPath)) 
                throw new ServletException(String.format("Invalid configuration file for '%s': %s", service, cfgPath));
            config.load(cfgPath.toFile());


            /* Configure the service logger */

            Config logConfig = config.getConfig("logs.service");
            LOGGER = new Logger(service, logConfig);

            LOGGER.info("Starting service " + getServletConfig().getServletName());
            LOGGER.debug("Service ID: " + service);


            /* Initialize the service sub class */

            serviceInit();


            /* Load any modules */

            ClassInitializer loader = new ClassInitializer();
            String[] modules = Arrays.stream(config.getString("service.modules", "").split("[\\s,]+")).filter(w -> w.length() > 0).toArray(String[]::new);

            for (String module : modules) {
                LOGGER.debug(String.format("Loading module: %s", module));
                loader.loadModule(module, this, config, LOGGER);
            }
        }
        catch (Exception x) {
            throw new ServletException("Service configuration failed: " + x.getMessage());
        }
    }



    /**
     * Performs any necessary service shutdown operations.
     */
    public void destroy() {
    }



    /**
     * Service specific initialization method intended to be overridden by
     * sub classes as needed.
     * @throws ServletException on initialization error
     */
    protected void serviceInit() throws ServletException {}
    

    
    /**
     * Returns the application level configuration object.
     *
     * @return the application configuration as a Config instance
     * @see Config
     */
    protected Config getConfig() {
        return config;
    }



    /**
     * Returns the service name.
     *
     * @return the service name.
     */
    public String getServiceName() {
        return service;
    }



    /**
     * Returns an InputStream instance for an application resource.
     * <p>Application resources are bound into the war/jar file.
     * 
     * @param resourceName  the full pathname of the resource to return
     * @return an InputStream instance for the named resource, or null if named resource does not exist
     */
    public InputStream getResource(String resourceName) {
        /* Note that in our WAR files, we assume all data files are located in WEB-INF */
        
        return getServletContext().getResourceAsStream("WEB-INF/" + resourceName);
    }

}




