package xtracker.plugins;
        /**
         * The generic plugin interface.
         * <p>   
         * All the methods that need to be implemented by a plugin not matter what 
         * their type is are the following:
         * <ol>
         *  <li>start (returns true if everything went ok). Starts the plugin's execution.</li>
         * <li>getName (returns a string with the plugin name).</li>
         * <li>getVersion (returns a string with the plugin's version).</li>
         * <li>getType (returns a string with the plugin's type). There are 5 possible types of
         *     plugins:</li>
         *      <ol>
         *       <li> rawData_loadPlugin (type string: RAWDATA_LOAD_plugin)</li>
         *       <li> identData_loadPlugin (type string: IDENTDATA_LOAD_plugin)</li>
         *       <li> PeakSelPlugin (type string: PEAKSEL_plugin)</li>
         *       <li> QuantPlugin (type string: QUANT_plugin)</li>
         *       <li> OutPlugin (type string: OUTPUT_plugin)</li>
         *      </ol>
         *  <li> getDescription (returns a string with the description of the plugin).</li>
         * </ol>
         * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
         * X-Tracker Project
         */


    public interface pluginInterface{
  
        

        /**
         * Gets the name of the plugin
         * @return a string with the plugin name.
         */
        public String getName();
        /**
         * Gets the version of the plugin
         * @return a string with the plugin version.
         */
        public String getVersion();
 
        /**
         * Gets the type of the plugin
         * @return a string with the plugin's type (out of the 4 possible types).
         */
        public String getType();
        
        /**
         * Gets the description of the plugin
         * @return a string with the plugin's description.
         */
        public String getDescription();
    }
    
