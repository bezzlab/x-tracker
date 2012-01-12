package xtracker.plugins.output;

import xtracker.plugins.pluginInterface;
import xtracker.data.xQuant;

/**
 * The outPlugin interface.
 * <p>
 * It extends the generic pluginInterface and inherits all the methods of the pluginInterface
 * but it has the <code>start()</code> method which has as input an xQuant structure and as output a
 * void structure.
 * @see pluginInterface
 * @see xQuant 
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
 * X-Tracker Project.
 */
public interface outPlugin extends pluginInterface{
   String type = xtracker.xTracker.OUTPUT_TYPE;
      /**
     * The plugin start method invoked to load the data structures to work on.
     * @param inputData is the xQuant structure, loaded with the quantPlugin, to work on. 
     * @param paramFile is a string containing parameters. If no parameters are needed by the
     * plugin then paramFile is an empty string. 
     * @see xQuant
     * 
     */
    public void start(xQuant inputData, String paramFile);
}


