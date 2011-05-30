


package xtracker;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Plugin to display the results of a quantification with the emPAI spectral counting method.
 * It displays first a window with the list of proteins and their emPAI score.
 * If the user clicks on a protein, it opens a window with the list of peptides and their
 * modifications.
 * @author laurie Tonon for X-Tracker
 */


public class OutputEMPAI implements outPlugin
{
    
	
     /**
      * The start method. Uses the classes Presentation, Editor and Renderer
      * @param inputData is the xQuant structure to work on.
      * @param paramFile a string containing the file name of parameters.	
      * No need to return anything.	
      */	
    public void start(xQuant InputData, String paramFile)
    {
        
        // create a new output window
        final Presentation p=new Presentation(InputData);
        
                p.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent WinEvent)
                    {
                System.exit(0);
            }
        });
        
        
    }

   


     /**
      * Method to retrieve the name of the plugin.
      * @return A string with the plugin name.	
      */
    public String getName()
    {
        return name;
    }

     /**
      * Method to retrieve the version of the plugin.
      * @return A string with the plugin version.	
      */
    public String getVersion()
    {
        return version;
    }

     /**
      * Method to retrieve the type of the plugin.
      * @return A string with the plugin type.	
      */	
    public String getType()
    {

        return type;
    }

     /**
      * Method to retrieve the description of the plugin.
      * @return A string with the plugin description.	
      */
        public String getDescription()
    {

        return description;
    }


     /**
      * The name of your plugin.
      */ 
    private final static String name = "OutputEMPAI";

     /**
      * The version of the plugin.
      */ 
    private final static String version = "1.0";
     
     /**
      * The plugin type. For an OUTPUT plugin it must be OUTPUT_plugin (do not change it).
      */ 	
    private final static String type = "OUTPUT_plugin";

     /**
      * The description of the plugin.
      */    
    private final static String description = "Plugin to display the results of a quantification with the spectral counting method."+ 
                                                "It displays first a window with the list of proteins and their emPAI score."+ 
                                                 "If the user click on a protein, it opens a window with the list of peptides and their"+
                                                "modifications.";
}
