package com.paradoxwebsolutions.core;


import java.io.InputStream;


/**
 * General application/service API.
 * <p>This interface provides some very basic application level functionality needed by
 * various parts of the system, generally for initialization.
 *
 * @author Peter Smith
 */
public interface ServiceAPI {


    /**
     * Returns the name of the service/application/tool.
     *
     * @return  the name of the service or application
     */
    public String getServiceName();


    /**
     * Returns an input stream representing a named resource.
     * <p>In the case of web services, this provides access to data resources bundled into
     * the war or jar file. In the case of stand-alone command line tools, it usually provides
     * access to a file on the filesystem in a pre-configured location. The goal here is to all
     * the same calling code to function correctly in all cases.
     *
     * @param name  the name of the resource to open
     * @return      an inputstream for the named resource
     */
    public InputStream getResource(String name);
}