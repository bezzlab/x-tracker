package xtracker;


import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * xTracker is the main class of the whole project. 
 * It contains the main and it manages plugins and data structures.
 * <p>
 * It loads the four plugins and checks if they are of the correct type before running each one of them.
 * X-tracker also checks the integrity of data structures after the execution of each script.
 * @see        xTracker
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
 * X-Tracker Project
 */
public class xTracker {

  /**
     * The Main class.  
     * <p>
     * @param args is the array of parameters passed by command line. 
     * Xtracker main program input is an .xml file describing the five plugins building xTracker's pipeline.<br>
     * A sample xml input file is reported below:
     * <pre>
     * {@code
     *  <?xml version="1.0" encoding="utf-8"?>
     *      <!-- Specifies all the parameters needed by the xTracker program.
     *           Parameters are a series of xml files specifying particular inputs (id any needed) for each
     *           plugin of the system.
     *      -->
     *  <xTrackerPipeline>
     *     <!--
     *          Specifies the path of the plugin jars as well as the parameters those plugins will work on.
     *          Tags MUST BE:
     *              rawdata_loadplugin
     *              identdata_loadplugin
     *              peakselplugin
     *              quantplugin
     *              outplugin
     *          the attribute "filename" will point to the .jar plugin (which has to be put in the Plugins folder)
     *          and the parameter file is the content of the tag
     *          (e.g. <peakselplugin filename="plugin2.jar">15n14n.xml</peakselplugin>).
     *      -->
     *
     *          <rawdata_loadplugin filename="loadrawdata.jar">loadRawParams.xml</rawdata_loadplugin>
     *          <identdata_loadplugin filename="loadidentdata.jar">loadIdentParams.xml</identdata_loadplugin>
     *          <peakselplugin filename="metLabeling.jar">15n14n.xml</peakselplugin>
     *          <quantplugin filename="LcMsAreasSimpson.jar"></quantplugin>
     *          <outplugin filename="displayResults.jar">displayParam.xml</outplugin>

     *  </xTrackerPipeline>
   * }
   * </pre>
     */
    public static void main(String[] args) {
        // Some strings: the name of Load plugins, PeakSel plugins, Quant plugins
        // Out plugins and the path of the plugins directory.      
        String inRawDataPluginStr="RAWDATA_LOAD_plugin";
        String inIdentDataPluginStr="IDENTDATA_LOAD_plugin";
        String peakPluginStr="PEAKSEL_plugin";
        String quantPluginStr="QUANT_plugin";
        String outPluginStr="OUTPUT_plugin";
        String pluginPath="Plugins/";
        pluginInterface plugin=null; //the plugin to load
        
        
        // To make sure the Garbage collector is invoked when needed we need a runtime object.
        // the garbage collector r.gc() will be invoked after every plugin has finished its computations.
        Runtime r = Runtime.getRuntime();
        // for debugging purposes we might want ot determine the current amount of free memory
        long freeMem = r.freeMemory();
    
    
        // Input Parameters checking. Three choices are possible:
        // 1 - parameter filespecified (i.e. 1 input parameters)
        //   call java xTracker file.xml
        // 2 - two inputs are specified: the first is the flag --pluginInfo and the second is a plugin name
        // 3 - all other cases: wrong number of elements. Display error message and exit.
        
                        String[] pluginTypes = new String[5]; //This array will contain the types of plugins. It is useful to check plugins at the beginning
                        pluginTypes[0]=inRawDataPluginStr;
                        pluginTypes[1]=inIdentDataPluginStr;
                        pluginTypes[2]=peakPluginStr;
                        pluginTypes[3]=quantPluginStr;
                        pluginTypes[4]=outPluginStr;
                       
                        //The array carrying the jar file of each plugin.
                        String [] plugins = new String[5];
                        for(int i=0;i<5;i++){
                            plugins[i]="";
                        }
                        //The array carrying the parameter file for each plugin.
                        String [] pluginInputs = new String[5];
                        for(int i=0;i<5;i++){
                            pluginInputs[i]=null;
                        }
                        
        switch (args.length){
            
            case 1 :    {
                        // Four parameters are ok go on!
                        // First check that all plugins can be loaded and are of the right type.
                        System.out.print("\nChecking plugins in "  + args[0] +" file...\n");
                        
                        //The xml document containing plugins and parameter files.
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setNamespaceAware(true);
                         try{ 
                                DocumentBuilder db = dbf.newDocumentBuilder();
                                Document doc = db.parse(args[0]);

                                // create a SchemaFactory
                                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

                                
                                doc.getDocumentElement().normalize();

                                Node nodeLst = doc.getElementsByTagName("xTrackerPipeline").item(0);

                                String schemaLocation="";

                                if(nodeLst.getAttributes().getNamedItem("xsi:schemaLocation") != null){
                                    schemaLocation=nodeLst.getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();
                                }
                                else {
                                    if(nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation")!= null){
                                    schemaLocation=nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation").getTextContent();
                                    }
                                    else{
                                        System.out.println("ERROR: No .xsd schema is provided for " + args[0] );
                                        System.exit(1);
                                    }
                                }

                              
                                
                                // load the xtracker WXS schema
                                Source schemaFile = new StreamSource(new File(schemaLocation));
                                Schema schema = factory.newSchema(schemaFile);

                                // create a Validator instance
                                Validator validator = schema.newValidator();

                                try {
                                    validator.validate(new DOMSource(doc));
                                } catch (SAXException e) {
                                    // instance document is invalid!
                                    System.out.println("\n\nERRROR - could not validate the input file " + args[0]+"!");
                                    System.out.print(e);
                                    System.exit(1);
                                }
                               




                                nodeLst = doc.getElementsByTagName("rawdata_loadplugin").item(0);
                                //The first plugin is the loadplugin
                                plugins[0]=nodeLst.getAttributes().item(0).getTextContent();
                                pluginInputs[0]=nodeLst.getTextContent();
                                nodeLst = doc.getElementsByTagName("identdata_loadplugin").item(0);
                                //The first plugin is the loadplugin
                                plugins[1]=nodeLst.getAttributes().item(0).getTextContent();
                                pluginInputs[1]=nodeLst.getTextContent();

                                nodeLst = doc.getElementsByTagName("peakselplugin").item(0);
                                //The second plugin is the peakselplugin
                                plugins[2]=nodeLst.getAttributes().item(0).getTextContent();
                                pluginInputs[2]=nodeLst.getTextContent();
                                nodeLst = doc.getElementsByTagName("quantplugin").item(0);
                                //The first plugin is the loadplugin
                                plugins[3]=nodeLst.getAttributes().item(0).getTextContent();
                                pluginInputs[3]=nodeLst.getTextContent();
                                nodeLst = doc.getElementsByTagName("outplugin").item(0);
                                //The first plugin is the loadplugin
                                plugins[4]=nodeLst.getAttributes().item(0).getTextContent();
                                pluginInputs[4]=nodeLst.getTextContent();
                                
                                //ok let's do some cleaning up!
                                db=null;
                                doc=null;
                                nodeLst=null;
                         }
                         catch(Exception e){System.out.println("Exception while reading " + args[0]+ "!\n" + e);}
                        for(int i=0;i<plugins.length;i++){
                               String fileName = plugins[i]; //the filename .jar in input
                               String className ="";    //the classname should be like the filename without the .jar
                               String pluginType=""; //the type of the plugin
                               int index=-1; //a counter to check if it is a jar or not
                               if ((index = fileName.indexOf(".jar")) >= 0){
                                    className = fileName.substring(0, index);
                                
                                    try
                                    {
                                        pluginLoader = new pluginClassLoader(pluginPath+fileName);
                                    }
                                    catch (Exception e){
                                        System.err.println("\n\nERROR: ClassLoader for \""+pluginPath+fileName+"\" could not be created");
                                        System.out.print(e);
                                        System.exit(1);
                                    }
                                    
                                    // Loading the class
                                    try
                                    {
                                     plugin = (pluginInterface)pluginLoader.findClass("xtracker."+className).newInstance();
                                    
                                    }
                                    catch (Exception e)
                                    {
                                        System.err.println("ERROR: Class \""+className+"\" could not be loaded");
                                        
                                        System.out.print(e);
                                        
                                        System.out.println("\n\n    Common reasons:");
                                        System.out.println("     - Is the plugin ("+fileName+") located in the \"Plugins\" folder?");
                                        System.out.println("     - Does the plugin contain the \"package xtracker;\" statement?");
                                        System.out.println("     - Are you running xTracker from the program main folder?");
                                        plugin = null;
                                        System.exit(1);
                                    }
                                    
                                    if(plugin != null){
                                        pluginType = plugin.getType();
                                        if(pluginType.equals(pluginTypes[i])){
                                        //Ok Plugin has correct type 
                                        if((! pluginInputs[i].equals("")) && (new File(pluginInputs[i]).exists())){
                                            //OK the parameter file does exist
                                        }
                                        else{
                                            if(! pluginInputs[i].equals("")){
                                                   System.err.println("\n\nERROR: File \""+pluginInputs[i]+"\" input of " + "\"" + plugins[i] + "\" could not be found!");
                                                   System.exit(1);
                                            }
                                        
                                        }
                                       

                                        }
                                        else{
                                           //Nope Plugin is not of correct type 
                                            System.out.println("\nError: " + plugin.getName() + " ("+ fileName+ ") is a \"" + pluginType + "\" but \"" +  pluginTypes[i] +"\" is needed instead!");
                                            System.exit(1);
                                        }
                                        plugin=null;
                                       
                                    }
                               }
                        }
                        //Call the garbage collector
                        System.gc();
                        System.out.print("done!\n");
                        break;
                        }
            case 2 :    {
                        // Two parameters are ok only if the first is "--pluginInfo" and the second a valid plugin 
                       //  name.
                       
                           if(args[0].equals("--pluginInfo")){
                               String fileName = args[1]; //the filename .jar in input
                               String className ="";    //the classname should be like the filename without the .jar
                               String pluginVersion=""; //the version of the plugin
                               String pluginType=""; //the type of the plugin
                               String pluginName=""; //the name of the plugin
                               String pluginDescription=""; //the description of the plugin
                               int index=-1; //a counter to check if it is a jar or not
   
                               //A little bit of interface
                                System.out.println("");
                                System.out.println("");
                                System.out.println("");
                                System.out.println("    **************************************");
                                System.out.println("    *****  Welcome to X-Tracker v1.3 *****");
                                System.out.println("    **************************************");
                                System.out.println("");
                                System.out.println("");
                           
                                if ((index = fileName.indexOf(".jar")) >= 0){
                                    className = fileName.substring(0, index);
                                
                                    try
                                    {
                                        pluginLoader = new pluginClassLoader(pluginPath+fileName);
                                    }
                                    catch (Exception e){
                                        System.err.println("ERROR: ClassLoader for \""+pluginPath+fileName+"\" could not be created");
                                        System.out.print(e);
                                        System.exit(1);
                                    }
                                    
                                    // Loading the class
                                    try
                                    {
                                     plugin = (pluginInterface)pluginLoader.findClass("xtracker."+className).newInstance();
                                    }
                                    catch (Exception e)
                                    {
                                        System.err.println("ERROR: Class \""+className+"\" could not be loaded");
                                        
                                        System.out.print(e);
                                        
                                        System.out.println("\n\n    Common reasons:");
                                        System.out.println("     - Is the plugin ("+fileName+") located in the \"Plugins\" folder?");
                                        System.out.println("     - Does the plugin contain the \"package xtracker;\" statement?");
                                        plugin = null;
                                        System.exit(1);
                                    }
                           
                                    if(plugin != null){
                                        pluginName = plugin.getName();
                                        pluginVersion = plugin.getVersion();
                                        pluginDescription = plugin.getDescription();
                                        pluginType = plugin.getType();
                                    }
                                    //Some text interface
                            
                                    System.out.println("    Information for plugin file: " + fileName);
                                    System.out.println("     -  Name: " + pluginName);
                                    System.out.println("     -  Version: " + pluginVersion);
                                    System.out.println("     -  Type: " + pluginType);
                                    System.out.println("     -  Description: " + pluginDescription);
                                }
                                else{
                                    //Nope, it's not a valid plugin    
                                    System.out.println("    The plugin specified (" + pluginPath + fileName + ") is not a valid plugin." );
                                    System.exit(1);
                               }
                           //collecting some garbage     
                           plugin=null;
                           pluginName=null;
                           pluginVersion = null;
                           pluginDescription = null;
                           pluginType = null;
                           r.gc();
                       }    
                       else{
                            //First argument is not "--pluginInfo"
                            System.out.println("");
                            System.out.println("");
                            System.out.println("");
                                System.out.println("    **************************************");
                                System.out.println("    *****  Welcome to xTracker v1.3  *****");
                                System.out.println("    **************************************");
                            System.out.println("");
                            System.out.println("");
                            System.out.println("     Usage: java -jar xTracker.jar configuration_file.xtc");
                            System.out.println("");
                            System.out.println("           where parameter_file is a xml file specifying the five plugins\n\t   and parameters.");
                            System.out.println("");
                            System.out.println("           alternatively:");
                            System.out.println("                          java -jar xTracker.jar --pluginInfo plugin.jar");
                            System.out.println("");
                            System.out.println("           can be used to display information on plugin.jar plugin.");
                            System.out.println("");
                       
                            //Let's list now possible plugins (i.e. plugins that are in the folder Plugins\
                            File f = new File(pluginPath);  //plugins
                            File files[] = f.listFiles();   //array of available plugins
                            
                            System.out.println("\n\tThe following plugins are currently available (in \"Plugins\" folder):");    
                            
                            for(int i=0;i<files.length;i++){
                                if(files[i].isFile() && (files[i].getName().indexOf(".jar"))>0){
                                    System.out.println("\t - " + files[i].getName());
        
                                }
                            }     
                           //collecting some garbage
                           plugin=null;
                           f=null;
                           files=null;
                           r.gc();
                       }
                       System.exit(0);
                       break;    
                       }
            default : {
                            // Not enough parameters or too many. Print an error message and all 
                            // list all possible plugins in the pluginPath directory and exit.
                            System.out.println("");
                            System.out.println("");
                            System.out.println("");
                                System.out.println("    **************************************");
                                System.out.println("    *****  Welcome to xTracker v1.3  *****");
                                System.out.println("    **************************************");
                            System.out.println("");
                            System.out.println("");
                            System.out.println("     Usage: java -jar xTracker.jar configuration_file.xtc");
                            System.out.println("");
                            System.out.println("           where parameter_file is a xml file specifying the five plugins\n\t   and parameters.");
                            System.out.println("");
                            System.out.println("           alternatively:");
                            System.out.println("                          java -jar xTracker.jar --pluginInfo plugin.jar");
                            System.out.println("");
                            System.out.println("           can be used to display information on plugin.jar plugin.");
                            System.out.println("");
                        
                                                        //Let's list now possible plugins (i.e. plugins that are in the folder Plugins\
                            File f = new File(pluginPath);  //plugins
                            File files[] = f.listFiles();   //array of available plugins
                            
                            System.out.println("\n\tThe following plugins are currently available (in \"Plugins\" folder):");    
                            
                            for(int i=0;i<files.length;i++){
                                if(files[i].isFile() && (files[i].getName().indexOf(".jar"))>0){
                                    System.out.println("\t - " + files[i].getName());
        
                                }
                            }
 
                           //collecting some garbage!
                           plugin=null; 
                           f=null;
                           files=null;
                           r.gc();
                           System.exit(0);
                    break;       
                }
                       
        }
        
        //OK All checks have been performed and are ok. It's time to start the first plugin.
        /**
         * Let's load and execute the LoadPlugin plugin 
         */

         freeMem = r.freeMemory();
        // System.out.println("Free memory at the beginning: " +freeMem);

        
        String fileName = plugins[0]; //the filename .jar in input
        String className ="";    //the classname should be like the filename without the .jar
        int index=-1; //a counter to check if it is a jar or not
        index = fileName.indexOf(".jar");
        className = fileName.substring(0, index);
         try{
              pluginLoader = new pluginClassLoader(pluginPath+fileName);
             }
        catch (Exception e){
            System.err.println("ERROR: ClassLoader for \""+pluginPath+fileName+"\" could not be created");
            System.out.print(e);
            System.exit(1);
            }
             
         // Loading the class
         try{
               rawDataInputPlugin = (rawData_loadPlugin)pluginLoader.findClass("xtracker."+className).newInstance();
            }
          catch (Exception e){System.out.println(e);}
          
          /**
           * OK! The rawDataInputPlugin is loaded and ready to run.
           * Let's execute it to obtain the xLoad structure which in xTracker program
           * is called inputData.
           */
           inputData=rawDataInputPlugin.start(pluginInputs[0]);
          
           /**
            * inputData structure has been created Xtracker checks its validity before
            * going any further!
            */
           
           if(!inputData.isRawDataValid()){
                // Something went wrong with the LoadPlugin and the xLoad data structure is not
                // created properly. Stop here.
                System.out.println("ERROR: The input data structure loaded by loadPlugin (" +rawDataInputPlugin.getName()+ ") is not valid!");
                System.exit(1);
            }

         freeMem = r.freeMemory();
       //  System.out.println("Free memory after loadPlugin: " +freeMem);
           




            /**
             * The input raw data is now loaded correctly. Now it is time to load the
             * identDataInput plugin to work on the raw data and identifications.
             */
           fileName = plugins[1]; //the filename .jar in input
           className ="";    //the classname should be like the filename without the .jar
           index=-1;    //the counter to check if it is a jar or not
           index = fileName.indexOf(".jar");
            className = fileName.substring(0, index);
            try{
                  pluginLoader = new pluginClassLoader(pluginPath+fileName);
                }
            catch (Exception e){
                System.err.println("ERROR: ClassLoader for \""+pluginPath+fileName+"\" could not be created");
                System.out.print(e);
                System.exit(1);
            }

         // Loading the peak selection class
         try{
               identDataInputPlugin = (identData_loadPlugin)pluginLoader.findClass("xtracker."+className).newInstance();
            }
          catch (Exception e){System.out.println(e);}


          /**
           * OK! The identDataInputPlugin is loaded and ready to run.
           * Let's execute it to obtain the xPeaks structure which in xTracker program
           * is called peaksData.
           */
           inputData=identDataInputPlugin.start(inputData, pluginInputs[1]);


           pluginLoader=null;
           System.gc();

           freeMem = r.freeMemory();
    //       System.out.println("Free memory: " +freeMem);

           /**
            * xLoad structure has been created and identification data has been added. Xtracker checks the identification data validity before
            * going any further!
            */


           if(!inputData.isIdentDataValid()){
                // Something went wrong with the identDataInputPlugin and the xLoads data structure is not
                // created properly. Stop here.
                System.out.println("ERROR: The input data structure loaded by identDataLoadPlugin (" +identDataInputPlugin.getName()+ ") is not valid!");
                System.exit(1);
            }






            /**
             * The input data is now loaded correctly. Now it is time to load the
             * peakSelection plugin to work on the raw data and identifications.
             */
           fileName = plugins[2]; //the filename .jar in input
           className ="";    //the classname should be like the filename without the .jar
           index=-1;    //the counter to check if it is a jar or not
           index = fileName.indexOf(".jar");
            className = fileName.substring(0, index);
            try{
                  pluginLoader = new pluginClassLoader(pluginPath+fileName);
                }
            catch (Exception e){
                System.err.println("ERROR: ClassLoader for \""+pluginPath+fileName+"\" could not be created");
                System.out.print(e);
                System.exit(1);
            }
             
         // Loading the peak selection class
         try{
               peakExtractPlugin = (peakSelPlugin)pluginLoader.findClass("xtracker."+className).newInstance();
            }
          catch (Exception e){System.out.println(e);}
           
           
          /**
           * OK! The peakExtractPlugin is loaded and ready to run.
           * Let's execute it to obtain the xPeaks structure which in xTracker program
           * is called peaksData.
           */
           peaksData=peakExtractPlugin.start(inputData, pluginInputs[2]);
          
           
           /**
            * NOTE: From here on we will not need inputData anymore but we will carry on with peaksData
            *       therefore we can get rid of it and freeing resources.
            */
           inputData=null;
           pluginLoader=null;
           System.gc();
        
           freeMem = r.freeMemory();
    //       System.out.println("Free memory: " +freeMem);
         
           /**
            * peaks structure has been created Xtracker checks its validity before
            * going any further!
            */
           
           
           if(!peaksData.isValid()){
                // Something went wrong with the peakSelPlugin and the xPeaks data structure is not
                // created properly. Stop here.
                System.out.println("ERROR: The input data structure loaded by peakSelPlugin (" +peakExtractPlugin.getName()+ ") is not valid!");
                System.exit(1);
            }
           
           
          
             /**
             * The peak extraction data is now loaded correctly. Now it is time to load the
             * Quant plugin to work on the peaks identified and compute quantities.
             */
           fileName = plugins[3]; //the filename .jar in input
           className ="";    //the classname should be like the filename without the .jar
           index=-1;    //the counter to check if it is a jar or not
           index = fileName.indexOf(".jar");
            className = fileName.substring(0, index);
            try{
                  pluginLoader = new pluginClassLoader(pluginPath+fileName);
                }
            catch (Exception e){
                System.err.println("ERROR: ClassLoader for \""+pluginPath+fileName+"\" could not be created");
                System.out.print(e);
                System.exit(1);
            }
             
         // Loading the peak selection class
         try{
               quantificationPlugin = (quantPlugin)pluginLoader.findClass("xtracker."+className).newInstance();
            }
          catch (Exception e){System.out.println(e);}
           
          /**
           * OK! The quantPlugin is loaded and ready to run.
           * Let's execute it to obtain the xQuant structure which in xTracker program
           * is called quantData.
           */   
         quantData=quantificationPlugin.start(peaksData, pluginInputs[3]);
            
            
           /**
            * quants structure has been created Xtracker checks its validity before
            * going any further!
            */
           
           
           if(!quantData.isValid()){
                // Something went wrong with the peakSelPlugin and the xPeaks data structure is not
                // created properly. Stop here.
                System.out.println("ERROR: The quantification data structure loaded by quantPlugin (" +quantificationPlugin.getName()+ ") is not valid!");
                System.exit(1);
            }
         
         
           /**
            * NOTE: From here on we will not need peaksData anymore but we will carry on with quantData
            *       therefore we can get rid of it and freeing resources.
            */
           peaksData=null;
           peakExtractPlugin=null;
           System.gc();
        
           freeMem = r.freeMemory();
    //       System.out.println("Free memory: " +freeMem);
            
           
           
           
            /**
             * The quantification data is now loaded correctly. Now it is time to load the
             * output plugin to work on display results.
             */
           fileName = plugins[4]; //the filename .jar in input
           className ="";    //the classname should be like the filename without the .jar
           index=-1;    //the counter to check if it is a jar or not
           index = fileName.indexOf(".jar");
           className = fileName.substring(0, index);
            try{
                  pluginLoader = new pluginClassLoader(pluginPath+fileName);
                }
            catch (Exception e){
                System.err.println("ERROR: ClassLoader for \""+pluginPath+fileName+"\" could not be created");
                System.out.print(e);
                System.exit(1);
            }
             
         // Loading the output plugin class
         try{
               outputPlugin = (outPlugin)pluginLoader.findClass("xtracker."+className).newInstance();
            }
          catch (Exception e){System.out.println(e);}
           
          /**
           * OK! The outPlugin is loaded and ready to run.
           * Let's execute it to display results.
           */   
         outputPlugin.start(quantData, pluginInputs[4]);
         
         
         System.gc();
        
           freeMem = r.freeMemory();
      //     System.out.println("Free memory: " +freeMem);
         
         System.out.println("xTracker finished execution without errors!");
     
           
           
           
         /**
          * Now some cleaning up and exiting.
          */   
         
         peaksData=null;
         System.gc();
         freeMem = r.freeMemory();
       //  System.out.println("Free memory: " +freeMem);
    }

    /**
     * Return X-Tracker version number
     * @return version as a string
    */

    public String getVersion()
    {
        return version;
    }

    /**
     * The plugin class loader
     */
    private static pluginClassLoader pluginLoader;
    /**
     * Data structures loaded by loadPlugins
     * @see rawData_loadPlugin
     * @see xLoad
     */
    public static  xLoad inputData;

        /**
     * Data structures loaded by peakSelPlugins
     * @see peakSelPlugin
     * @see xPeaks
     * @see xLoad
     */
    public static  xPeaks peaksData;
    
    
     /**
     * Data structures loaded by quantlPlugins
     * @see quantPlugin
     * @see xPeaks
     * @see xQuant
     */
    public static  xQuant quantData;
    
    /**
      * The first plugin: it loads raw input data.
      * @see rawData_loadPlugin
      */
    private static rawData_loadPlugin rawDataInputPlugin;
    
    /**
      * The second plugin: it loads identification data.
      * @see rawData_loadPlugin
      */
    private static identData_loadPlugin identDataInputPlugin;
    
    /**
     * The second plugin to extract peaks.
     * @see peakSelPlugin
     */
    private static peakSelPlugin peakExtractPlugin;

     /**
      * The third plugin to load input data.
      * @see quantPlugin
      */
    private static quantPlugin quantificationPlugin;

     /**
      * The fourth plugin to output results.
      * @see quantPlugin
      */
    private static outPlugin outputPlugin;

     /**
      * This string holds the version number
      */
    private final static String version = "1.3";
    
}
