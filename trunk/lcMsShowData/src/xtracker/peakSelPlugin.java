package xtracker;

/**
 * The peakSelPlugin interface.
 * <p>
 * It extends the generic pluginInterface and inherits all the methods of the pluginInterface
 * but it has the <code>start()</code> method which has as input an xLoad structure and as output a
 * xPeaks structure.
 * @see pluginInterface
 * @see xLoad
 * @see xPeaks 
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
 * X-Tracker Project.
 */
public interface peakSelPlugin extends pluginInterface{
         
      /**
     * The plugin start method invoked to load the data structures to work on.
     * @return the int return value has not been decided yet. 
     * @param inputData is the xLoad structure, loaded with the loadPlugin, to work on. 
     * @param paramFile is a string containing parameters. If no parameters are needed by the
     * plugin then paramFile is an empty string. 
     * @see xLoad
     * 
     */
        public xPeaks start(xLoad inputData, String paramFile);
}


