package xtracker;
/**
 *
 * @author Dr. Luca Bianco -- Cranfield Health, Cranfield University --
 * X-Tracker Project
 * The plugin interface.
 */


    public interface pluginInterface{
        /** 
         * All the methods that need to be implemented by a plugin not matter what 
         * their type is are the following:
         * 1 - start (returns true if everything went ok). Starts the plugin's execution.
         * 2 - stop (returns true if everything went ok). Stops the plugin's execution.
         * 3 - getName (returns a string with the plugin name).
         * 4 - getVersion (returns a string with the plugin's version).
         * 5 - getType (returns a string with the plugin's type). There are 4 possible types of
         *     plugins:
         *       - (i) LoadPlugin (type string: LOAD_plugin)
         *       - (ii) PeakSelPlugin (type string: PEAKSEL_plugin)
         *       - (iii) QuantPlugin (type string: QUANT_plugin)
         *       - (iv) OutPlugin (type string: OUTPUT_plugin)
         * 
         *  6 - getDescription (returns a string with the description of the plugin).
         */  
        
        //start: returns true if everything went ok. Starts the plugin's execution
       // public boolean start();
        //getName: returns a string with the plugin name.
        public String getName();
        //getVersion: returns a string with the plugin's version.
        public String getVersion();
        //getType: returns a string with the plugin's type (out of the 4 possible types).
        public String getType();
        //getDescription: returns a string with the plugin's description.
        public String getDescription();
    }
    
