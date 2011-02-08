
//
//    xTrackerGui
//
//    Package: xtrackergui.misc
//    File: PluginClassLoader.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.misc;

import java.net.*;
import java.io.*;
import org.apache.log4j.Logger;

/**
 * Class to dynamically load plugins.
 *
 * (Taken from X-Tracker Project)
 *
 * @author Dr L. Bianco (adjusted by andrew bullimore)
 */
public class PluginClassLoader extends URLClassLoader {

    private Logger logger = Logger.getLogger(PluginClassLoader.class.getName());

     /**
      * Create a PluginClassLoader object
      *
      * @throws MalformedURLException
      * @param path The path of the plugin (must be contained in the Plugins directory of the X-Tracker project)
     */
    public PluginClassLoader(String path) throws MalformedURLException
    {
        super(new URL[] { new File(path).toURI().toURL() });
    }

    
    @Override
    public Class findClass(String name) {
        try {

            return super.findClass(name);
            
        } catch (ClassNotFoundException e) {

            logger.debug("PluginClassLoader::findClass: Problem plugin " + name + " not found");
            return null;
        }
    }
}
