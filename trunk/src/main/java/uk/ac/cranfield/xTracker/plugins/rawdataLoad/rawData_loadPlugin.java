package uk.ac.cranfield.xTracker.plugins.rawdataLoad;

import uk.ac.cranfield.xTracker.plugins.pluginInterface;
/**
 * The rawData_loadPlugin interface.
 * <p>
 * It extends the generic pluginInterface and inherits all the methods of the pluginInterface
 * but it has the <code>start()</code> method which has as output an xLoad structure partially filled with information. In fact
 * it just adds rawData information, identification information will be added right after this plugin by the identData_plugin.
 * @see pluginInterface
 * @see xLoad 
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
 * X-Tracker Project.
 */
public interface rawData_loadPlugin extends pluginInterface{
    
   String type = uk.ac.cranfield.xTracker.xTracker.RAWDATA_LOAD_TYPE;
    /**
     * The plugin start method invoked to load the data structures to work on. In particular, the rawData information will be added
     * to the xLoad structure.
     * @return xLoad is the data structure containing the input (after this plugin rawData only will be inserted into the structure).
     * @param paramFile is a string containing parameters. If no parameters are needed by the
     * plugin then paramFile is an empty string.
     * @see xLoad
     */
//    public Data start(Data data, String paramFile);

}
