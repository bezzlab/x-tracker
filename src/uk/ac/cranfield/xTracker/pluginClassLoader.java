package uk.ac.cranfield.xTracker;

/**
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
import java.net.*;
import java.io.*;

/**
 * pluginClassLoader is the class used to dynamically load plugins.
 * <p>
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
 * X-Tracker Project.
 * This is the plugin class loader.
 */
public class pluginClassLoader extends URLClassLoader {

    /**
     * The constructor.
     * @throws MalformedURLException if some problems arise  
     * @param path is the path of the plugin (which has to be contained in the Plugins directory of the main program). 
     */
    pluginClassLoader(String path) throws MalformedURLException {
        super(new URL[]{new File(path).toURI().toURL()});
    }

    /**
     * Finds and returns the specified class (if it exists).
     * @param name the class name to load.
     * @return the class of the plugin.
     */
    @Override
    public Class findClass(String name) {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
