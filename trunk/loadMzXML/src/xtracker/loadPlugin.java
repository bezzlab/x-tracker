package xtracker;


/**
 * The loadPlugin interface.
 * <p>
 * It extends the generic pluginInterface and inherits all the methods of the pluginInterface
 * but it has the <code>start()</code> method which has as output an xLoad structure.
 * @see pluginInterface
 * @see xLoad 
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
 * X-Tracker Project.
 */
public interface loadPlugin extends pluginInterface{
    
    
    /**
     * The plugin start method invoked to load the data structures to work on.
     * @return xLoad is the data structure containing the input. 
     * @param paramFile is a string containing parameters. If no parameters are needed by the
     * plugin then paramFile is an empty string.
     * @see xLoad
     */
    public xLoad start(String paramFile);

}
