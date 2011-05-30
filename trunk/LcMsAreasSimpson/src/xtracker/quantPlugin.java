package xtracker;

/**
 * The quantPlugin interface.
 * <p>
 * It extends the generic pluginInterface and inherits all the methods of the pluginInterface
 * but it has the <code>start()</code> method which has as input an xLoad structure and as output a
 * xQuant structure.
 * @see pluginInterface
 * @see xQuant
 * @see xPeaks 
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
 * X-Tracker Project.
 */
public interface quantPlugin extends pluginInterface{
         
      /**
     * The plugin start method invoked to load the data structures to work on.
     * @return the int return value has not been decided yet. 
     * @param peaksData is the xPeaks structure, loaded with the peakSelPlugin, to work on. 
     * @param paramFile is a string containing parameters. If no parameters are needed by the
     * plugin then paramFile is an empty string.
     * @see xPeaks
     * @see xQuant
     * 
     */
        public xQuant start(xPeaks peaksData, String paramFile);
}


