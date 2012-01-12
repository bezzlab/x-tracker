package xtracker.plugins.identificationLoad;

import xtracker.plugins.pluginInterface;
import xtracker.data.xLoad;


/**
 * The identData_loadPlugin interface.
 * <p>
 * It extends the generic pluginInterface and inherits all the methods of the pluginInterface
 * but it has the <code>start()</code> method which has as input an xLoad structure containing rawData information only.
 * The plugin adds to it identification data.
 * @see pluginInterface
 * @see xLoad
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
 * X-Tracker Project.
 */
public interface identData_loadPlugin extends pluginInterface{

    String type = xtracker.xTracker.IDENTIFICATION_LOAD_TYPE;
    /**
     * The plugin start method invoked to load the identifications data to work on.
     * @return xLoad is the data structure containing the input. At plugin's invokation time it contains only rawData information but
     * identification data are added by this plugin.
     * @param inputData the xLoad data structure partially loaded by rawData_loadPlugins.
     * @param paramFile is a string containing parameters. If no parameters are needed by the
     * plugin then paramFile is an empty string.
     * @see xLoad
     */
    public xLoad start(xLoad inputData, String paramFile);

}
