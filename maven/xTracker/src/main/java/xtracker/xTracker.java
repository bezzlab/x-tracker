package xtracker;

import xtracker.plugins.rawdataLoad.rawData_loadPlugin;
import xtracker.plugins.quantitation.quantPlugin;
import xtracker.plugins.peakSelection.peakSelPlugin;
import xtracker.plugins.output.outPlugin;
import xtracker.plugins.identificationLoad.identData_loadPlugin;
//import xtracker.plugins.PluginClassLoader;
import xtracker.plugins.pluginInterface;
import xtracker.data.xQuant;
import xtracker.data.xPeaks;
import xtracker.data.xLoad;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;

import org.w3c.dom.Node;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import xtracker.utils.XMLparser;

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
     * The plugin class loader
     */
//    private static PluginClassLoader pluginLoader;
    private final static String version = "1.4";
    /**
     * This array will contain the types of plugins. It is useful to check plugins at the beginning    
     */
    public static final String RAWDATA_LOAD_TYPE = "RAWDATA_LOAD_plugin";
    public static final String IDENTIFICATION_LOAD_TYPE = "IDENTDATA_LOAD_plugin";
    public static final String PEAK_SELECTION_TYPE = "PEAKSEL_plugin";
    public static final String QUANTITATION_TYPE = "QUANT_plugin";
    public static final String OUTPUT_TYPE = "OUTPUT_plugin";
    static private final String[] PLUGIN_TYPES = {RAWDATA_LOAD_TYPE, IDENTIFICATION_LOAD_TYPE, PEAK_SELECTION_TYPE, QUANTITATION_TYPE, OUTPUT_TYPE};
    /**
     * the tags to be searched in the xtc configuration file to find the plugin names
     */
    private static final String[] TAG_NAMES = {"rawdata_loadplugin", "identdata_loadplugin", "peakselplugin", "quantplugin", "outplugin"};
    /**
     * the package name for each subset of plugins
     */
    private static final String[] PLUGIN_PACKAGES = {"rawdataLoad", "identificationLoad", "peakSelection", "quantitation", "output"};
    /**
     * the basic plugin class for each plugin types
     */
//    private static final String[] PLUGIN_BASE_CLASS = {"rawData_loadPlugin","identData_loadPlugin","peakSelPlugin","quantPlugin","outPlugin"};
    /**
     * defines the location of plugins
     */
    static private final String PLUGIN_PATH = "Plugins/";
    private String[] plugins = {"", "", "", "", ""};
    private String[] pluginParam = {"", "", "", "", ""};
    private pluginInterface plugin = null; //the plugin to load

    private void errorUsage() {
        printUsage();
        //Let's list now possible plugins (i.e. plugins that are in the folder Plugins\
        File f = new File(PLUGIN_PATH);  //plugins
        File[] files = f.listFiles();   //array of available plugins
        if (files == null) {
            System.out.println("There are no plugins in the fold " + f.getAbsolutePath());
        } else {
            System.out.println("\n\tThe following plugins are currently available (in \"Plugins\" folder):");
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile() && (files[i].getName().indexOf(".jar")) > 0) {
                    System.out.println("\t - " + files[i].getName());
                }
            }
        }
        //Let's print the index.html help file
//        printHtmlHelp("./Plugins/");
        System.exit(0);
    }

    private void printUsage() {
        printVersion();
        System.out.println("     Usage: java -jar xTracker.jar configuration_file.xtc");
        System.out.println("");
        System.out.println("           where configuration_file.xtc is an XML file specifying the five\n\t   plugins and their parameter files.");
        System.out.println("");
        System.out.println("           alternatively:");
        System.out.println("                          java -jar xTracker.jar --pluginInfo plugin.jar");
        System.out.println("");
        System.out.println("           can be used to display information on plugin.jar plugin.");
        System.out.println("");
    }

    public xTracker(String option, String fileName) {
        // Two parameters are ok only if the first is "--pluginInfo" and the second a valid plugin
        //  name.
        if (option.equals("--pluginInfo")) {
            String className = "";    //the classname should be like the filename without the .jar
            int index = -1; //a counter to check if it is a jar or not
            printVersion();

            if ((index = fileName.indexOf(".jar")) >= 0) {
                className = fileName.substring(0, index);
                try {
//                    pluginLoader = new PluginClassLoader(PLUGIN_PATH + fileName);
                } catch (Exception e) {
                    System.err.println("ERROR: ClassLoader for \"" + PLUGIN_PATH + fileName + "\" could not be created");
                    System.out.print(e);
                    System.exit(1);
                }

                // Loading the class
                try {
//                    plugin = (pluginInterface) pluginLoader.findClass("xtracker." + className).newInstance();
                } catch (Exception e) {
                    System.err.println("ERROR: Class \"" + className + "\" could not be loaded");
                    System.out.print(e);
                    System.out.println("\n\n    Common reasons:");
                    System.out.println("     - Is the plugin (" + fileName + ") located in the \"Plugins\" folder?");
                    System.out.println("     - Does the plugin contain the \"package xtracker;\" statement?");
                    plugin = null;
                    System.exit(1);
                }

                if (plugin != null) {
                    System.out.println("    Information for plugin file: " + fileName);
                    System.out.println("     -  Name: " + plugin.getName());
                    System.out.println("     -  Version: " + plugin.getVersion());
                    System.out.println("     -  Type: " + plugin.getType());
                    System.out.println("     -  Description: " + plugin.getDescription());
                }
                //Some text interface
            } else {
                //Nope, it's not a valid plugin    
                System.out.println("    The plugin specified (" + PLUGIN_PATH + fileName + ") is not a valid plugin.");
                System.exit(1);
            }
            printHtmlHelp("./Plugins/");
        } else {
            //First argument is not "--pluginInfo"
            errorUsage();
        }
        System.exit(0);
        return;
    }

    private void printVersion() {
        //A little bit of interface
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("    **************************************");
        System.out.println("    *****  Welcome to X-Tracker v" + version + " *****");
        System.out.println("    **************************************");
        System.out.println("");
        System.out.println("");
    }

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
     *          (e.g. <peakselplugin filename="plugin2.jar">15n14n.xtp</peakselplugin>).
     *      -->
     *
     *          <rawdata_loadplugin filename="loadrawdata.jar">loadRawParams.xtp</rawdata_loadplugin>
     *          <identdata_loadplugin filename="loadidentdata.jar">loadIdentParams.xtp</identdata_loadplugin>
     *          <peakselplugin filename="metLabeling.jar">15n14n.xtp</peakselplugin>
     *          <quantplugin filename="LcMsAreasSimpson.jar"></quantplugin>
     *          <outplugin filename="displayResults.jar">displayParam.xtp</outplugin>
    
     *  </xTrackerPipeline>
     * }
     * </pre>
     */
    public static void main(String[] args) {
        // Input Parameters checking. Three choices are possible:
        // 1 - parameter filespecified (i.e. 1 input parameters)
        //   call java xTracker file.xml
        // 2 - two inputs are specified: the first is the flag --pluginInfo and the second is a plugin name
        // 3 - all other cases: wrong number of elements. Display error message and exit.
        new xTracker("E:\\xtracker\\codes\\iTRAQ_conf.xtc");
//        new xTracker("E:\\xtracker\\codes\\SILAC_Conf_small.xtc");
//        switch (args.length) {
//            case 1: {
//                new xTracker(args[0]);
//                break;
//            }
//            case 2: {
//                new xTracker(args[0], args[1]);
//                break;
//            }
//            default: {
//                // Not enough parameters or too many. Print an error message and all
//                // list all possible plugins in the PLUGIN_PATH directory and exit.
//                new xTracker();
//                break;
//            }
//
//        }
    }

    public xTracker() {
        errorUsage();
    }

    public xTracker(String filename) {
        // First check that all plugins can be loaded and are of the right type.
//        if ((filename.indexOf(".xtc") >= 0) || (filename.indexOf(".xml") >= 0)) {
//            System.out.print("\nChecking plugins in " + filename + " file...\n");
//            //The xml document containing plugins and parameter files.
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            dbf.setNamespaceAware(true);
//            try {
//                DocumentBuilder db = dbf.newDocumentBuilder();
//                Document doc = db.parse(filename);
//
//                // create a SchemaFactory
//                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//
//                doc.getDocumentElement().normalize();
//
//                Node node = doc.getElementsByTagName("xTrackerPipeline").item(0);
//                if(node==null){
//                    System.out.println("ERROR: The configuration file can not be validated as xTrackerPipeline does not exist.");
//                    System.exit(1);
//                }
//
//                String schemaLocation = "";
//
//                if (node.getAttributes().getNamedItem("xsi:schemaLocation") != null) {
//                    schemaLocation = node.getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();
//                } else {
//                    if (node.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation") != null) {
//                        schemaLocation = node.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation").getTextContent();
//                    } else {
//                        System.out.println("ERROR: No .xsd schema is provided for " + filename);
//                        System.exit(1);
//                    }
//                }
//                // load the xtracker WXS schema
//                File xsdFile = new File(schemaLocation);
//                if(!xsdFile.exists()){
//                    System.out.println("ERROR: Can not find the specified xsd file "+schemaLocation+" for validation");
//                    System.exit(1);
//                }
//                Source schemaFile = new StreamSource(xsdFile);
//                Schema schema = factory.newSchema(schemaFile);
//
//                // create a Validator instance
//                Validator validator = schema.newValidator();
//
//                try {
//                    validator.validate(new DOMSource(doc));
//                } catch (SAXException e) {
//                    // instance document is invalid!
//                    System.out.println("\n\nERROR - could not validate the input file " + filename + "!");
//                    System.out.print(e);
//                    System.exit(1);
//                }
        XMLparser parser = new XMLparser(filename);
        parser.validate("xTrackerPipeline");
//        Document doc = Utils.readFromXMLfile(filename, "xTrackerPipeline");
        System.out.println("configuration file validation finished");

        for (int i = 0; i < TAG_NAMES.length; i++) {
            Node node = parser.getElement(TAG_NAMES[i]);
            if(node!=null){
            //The first plugin is the loadplugin
                plugins[i] = node.getAttributes().item(0).getTextContent();
                pluginParam[i] = node.getTextContent();
            }
//                    System.out.println("Plugin for "+TAG_NAMES[i]+": "+plugins[i]+" with parameter file "+pluginParam[i]);
        }

        //ok let's do some cleaning up!
//                db = null;
//        doc = null;
//                node = null;
//    }
//    
//    catch (Exception e
//
//    
//        ) {
//                System.out.println("Exception while reading " + filename + "!\n" + e);
//    }

        anotherRoute();
//            System.exit(0);

//            for (int i = 0; i < plugins.length; i++) {
//                String jarFileName = plugins[i]; //the filename .jar in input
//                String className = "";    //the classname should be like the filename without the .jar
//                String pluginType = ""; //the type of the plugin
//                int index = -1; //a counter to check if it is a jar or not
//                if ((index = jarFileName.indexOf(".jar")) >= 0) {
//                    className = jarFileName.substring(0, index);
//                    try {
//                        pluginLoader = new PluginClassLoader(PLUGIN_PATH + jarFileName);
//                    } catch (Exception e) {
//                        System.err.println("\n\nERROR: ClassLoader for \"" + PLUGIN_PATH + jarFileName + "\" could not be created");
//                        System.out.print(e);
//                        System.exit(1);
//                    }
//                    // Loading the class
//                    try {
//                        plugin = (pluginInterface) pluginLoader.findClass("xtracker." + className).newInstance();
//                    } catch (Exception e) {
//                        System.err.println("ERROR: Class \"" + className + "\" could not be loaded");
//                        System.out.print(e);
//                        System.out.println("\n\n    Common reasons:");
//                        System.out.println("     - Is the plugin (" + jarFileName + ") located in the \"Plugins\" folder?");
//                        System.out.println("     - Does the plugin contain the \"package xtracker;\" statement?");
//                        System.out.println("     - Are you running xTracker from the program main folder?");
//                        plugin = null;
//                        System.exit(1);
//                    }
//
//                    if (plugin != null) {
//                        pluginType = plugin.getType();
//                        if (pluginType.equals(PLUGIN_TYPES[i])) {
//                            //Ok Plugin has correct type
//                            if ((!pluginParam[i].equals("")) && (new File(pluginParam[i]).exists())) {
//                                //OK the parameter file does exist
//                            } else {
//                                if (!pluginParam[i].equals("")) {
//                                    System.err.println("\n\nERROR: File \"" + pluginParam[i] + "\" input of " + "\"" + plugins[i] + "\" could not be found!");
//                                    System.exit(1);
//                                }
//                            }
//                        } else {
//                            //Nope Plugin is not of correct type
//                            System.out.println("\nError: " + plugin.getName() + " (" + jarFileName + ") is a \"" + pluginType + "\" but \"" + PLUGIN_TYPES[i] + "\" is needed instead!");
//                            System.exit(1);
//                        }
//                        plugin = null;
//                    }
//                }
//            }
        //Call the garbage collector
//            System.gc();
//            System.out.print("done!\n");
//        } else {
//            errorUsage();
//            System.exit(0);
//        }

        //OK All checks have been performed and are ok. It's time to start the first plugin.
        // Let's load and execute the LoadPlugin plugin 
        Runtime r = Runtime.getRuntime();
        long freeMem = r.freeMemory();
        // System.out.println("Free memory at the beginning: " +freeMem);
//        String fileName = plugins[0]; //the filename .jar in input
//        String className = "";    //the classname should be like the filename without the .jar
//        int index = -1; //a counter to check if it is a jar or not
//        index = fileName.indexOf(".jar");
//        className = fileName.substring(0, index);
//        try {
//            pluginLoader = new PluginClassLoader(PLUGIN_PATH + fileName);
//        } catch (Exception e) {
//            System.err.println("ERROR: ClassLoader for \"" + PLUGIN_PATH + fileName + "\" could not be created");
//            System.out.print(e);
//            System.exit(1);
//        }
//        // Loading the class
//        try {
//            rawDataInputPlugin = (rawData_loadPlugin) pluginLoader.findClass("xtracker." + className).newInstance();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//        // OK! The rawDataInputPlugin is loaded and ready to run.
//        // Let's execute it to obtain the xLoad structure which in xTracker program
//        // is called inputData.
//        inputData = rawDataInputPlugin.start(pluginParam[0]);
//        // inputData structure has been created Xtracker checks its validity before
//        // going any further!
//        if (!inputData.isRawDataValid()) {
//            // Something went wrong with the LoadPlugin and the xLoad data structure is not
//            // created properly. Stop here.
//            System.out.println("ERROR: The input data structure loaded by loadPlugin (" + rawDataInputPlugin.getName() + ") is not valid!");
//            System.exit(1);
//        }
//
//        freeMem = r.freeMemory();
//        //  System.out.println("Free memory after loadPlugin: " +freeMem);
//
//        /**
//         * The input raw data is now loaded correctly. Now it is time to load the
//         * identDataInput plugin to work on the raw data and identifications.
//         */
//        fileName = plugins[1]; //the filename .jar in input
//        className = "";    //the classname should be like the filename without the .jar
//        index = -1;    //the counter to check if it is a jar or not
//        index = fileName.indexOf(".jar");
//        className = fileName.substring(0, index);
//        try {
//            pluginLoader = new PluginClassLoader(PLUGIN_PATH + fileName);
//        } catch (Exception e) {
//            System.err.println("ERROR: ClassLoader for \"" + PLUGIN_PATH + fileName + "\" could not be created");
//            System.out.print(e);
//            System.exit(1);
//        }
//
//        // Loading the peak selection class
//        try {
////            identDataInputPlugin = (identData_loadPlugin) pluginLoader.findClass("xtracker." + className).newInstance();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//
//        /**
//         * OK! The identDataInputPlugin is loaded and ready to run.
//         * Let's execute it to obtain the xPeaks structure which in xTracker program
//         * is called peaksData.
//         */
////        inputData = identDataInputPlugin.start(inputData, pluginParam[1]);
//
//
//        pluginLoader = null;
//        System.gc();
//
//        freeMem = r.freeMemory();
//        //       System.out.println("Free memory: " +freeMem);
//
//        /**
//         * xLoad structure has been created and identification data has been added. Xtracker checks the identification data validity before
//         * going any further!
//         */
//        if (!inputData.isIdentDataValid()) {
//            // Something went wrong with the identDataInputPlugin and the xLoads data structure is not
//            // created properly. Stop here.
////            System.out.println("ERROR: The input data structure loaded by identDataLoadPlugin (" + identDataInputPlugin.getName() + ") is not valid!");
//            System.exit(1);
//        }
//
//
//
//
//
//
//        /**
//         * The input data is now loaded correctly. Now it is time to load the
//         * peakSelection plugin to work on the raw data and identifications.
//         */
//        fileName = plugins[2]; //the filename .jar in input
//        className = "";    //the classname should be like the filename without the .jar
//        index = -1;    //the counter to check if it is a jar or not
//        index = fileName.indexOf(".jar");
//        className = fileName.substring(0, index);
//        try {
//            pluginLoader = new PluginClassLoader(PLUGIN_PATH + fileName);
//        } catch (Exception e) {
//            System.err.println("ERROR: ClassLoader for \"" + PLUGIN_PATH + fileName + "\" could not be created");
//            System.out.print(e);
//            System.exit(1);
//        }
//
//        // Loading the peak selection class
//        try {
////            peakExtractPlugin = (peakSelPlugin) pluginLoader.findClass("xtracker." + className).newInstance();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//
//        /**
//         * OK! The peakExtractPlugin is loaded and ready to run.
//         * Let's execute it to obtain the xPeaks structure which in xTracker program
//         * is called peaksData.
//         */
////        peaksData = peakExtractPlugin.start(inputData, pluginParam[2]);
//
//
//        /**
//         * NOTE: From here on we will not need inputData anymore but we will carry on with peaksData
//         *       therefore we can get rid of it and freeing resources.
//         */
//        inputData = null;
//        pluginLoader = null;
//        System.gc();
//
//        freeMem = r.freeMemory();
//        //       System.out.println("Free memory: " +freeMem);
//
//        /**
//         * peaks structure has been created Xtracker checks its validity before
//         * going any further!
//         */
//        if (!peaksData.isValid()) {
//            // Something went wrong with the peakSelPlugin and the xPeaks data structure is not
//            // created properly. Stop here.
////            System.out.println("ERROR: The input data structure loaded by peakSelPlugin (" + peakExtractPlugin.getName() + ") is not valid!");
//            System.exit(1);
//        }
//
//
//
//        /**
//         * The peak extraction data is now loaded correctly. Now it is time to load the
//         * Quant plugin to work on the peaks identified and compute quantities.
//         */
//        fileName = plugins[3]; //the filename .jar in input
//        className = "";    //the classname should be like the filename without the .jar
//        index = -1;    //the counter to check if it is a jar or not
//        index = fileName.indexOf(".jar");
//        className = fileName.substring(0, index);
//        try {
//            pluginLoader = new PluginClassLoader(PLUGIN_PATH + fileName);
//        } catch (Exception e) {
//            System.err.println("ERROR: ClassLoader for \"" + PLUGIN_PATH + fileName + "\" could not be created");
//            System.out.print(e);
//            System.exit(1);
//        }
//
//        // Loading the peak selection class
//        try {
//            quantificationPlugin = (quantPlugin) pluginLoader.findClass("xtracker." + className).newInstance();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//        /**
//         * OK! The quantPlugin is loaded and ready to run.
//         * Let's execute it to obtain the xQuant structure which in xTracker program
//         * is called quantData.
//         */
//        quantData = quantificationPlugin.start(peaksData, pluginParam[3]);
//
//
//        /**
//         * quants structure has been created Xtracker checks its validity before
//         * going any further!
//         */
//        if (!quantData.isValid()) {
//            // Something went wrong with the peakSelPlugin and the xPeaks data structure is not
//            // created properly. Stop here.
//            System.out.println("ERROR: The quantification data structure loaded by quantPlugin (" + quantificationPlugin.getName() + ") is not valid!");
//            System.exit(1);
//        }
//
//
//        /**
//         * NOTE: From here on we will not need peaksData anymore but we will carry on with quantData
//         *       therefore we can get rid of it and freeing resources.
//         */
//        peaksData = null;
////        peakExtractPlugin = null;
//        System.gc();
//
//        freeMem = r.freeMemory();
//        //       System.out.println("Free memory: " +freeMem);
//
//
//
//
//        /**
//         * The quantification data is now loaded correctly. Now it is time to load the
//         * output plugin to work on display results.
//         */
//        fileName = plugins[4]; //the filename .jar in input
//        className = "";    //the classname should be like the filename without the .jar
//        index = -1;    //the counter to check if it is a jar or not
//        index = fileName.indexOf(".jar");
//        className = fileName.substring(0, index);
//        try {
//            pluginLoader = new PluginClassLoader(PLUGIN_PATH + fileName);
//        } catch (Exception e) {
//            System.err.println("ERROR: ClassLoader for \"" + PLUGIN_PATH + fileName + "\" could not be created");
//            System.out.print(e);
//            System.exit(1);
//        }
//
//        // Loading the output plugin class
//        try {
//            outputPlugin = (outPlugin) pluginLoader.findClass("xtracker." + className).newInstance();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//        /**
//         * OK! The outPlugin is loaded and ready to run.
//         * Let's execute it to display results.
//         */
//        outputPlugin.start(quantData, pluginParam[4]);
//
//
        System.gc();

        freeMem = r.freeMemory();
        //     System.out.println("Free memory: " +freeMem);
        System.out.println("*************************************************");
        System.out.println("** xTracker finished execution without errors! **");
        System.out.println("*************************************************");

        /**
         * Now some cleaning up and exiting.
         */
//        peaksData = null;
//        System.gc();
//        freeMem = r.freeMemory();
    }

    private void anotherRoute() {
        Object data = null;
        for (int i = 0; i < PLUGIN_PACKAGES.length; i++) {
            String jarFileName = plugins[i]; //the filename .jar in input
            int index = -1; //a counter to check if it is a jar or not
            if ((index = jarFileName.indexOf(".jar")) >= 0) {
                String className = "xtracker.plugins." + PLUGIN_PACKAGES[i] + "." + jarFileName.substring(0, index);
                Class javaClass;

                try {
                    javaClass = Class.forName(className);
                    Constructor ct = javaClass.getConstructor(new Class[0]);
                    System.out.println("\nconstructor created: " + ct);
                    plugin = (pluginInterface) ct.newInstance(new Object[0]);
                } catch (ClassNotFoundException ex) {
                    System.out.println("WRONG: The java class has not been found:" + className);
                    System.exit(1);
                } catch (NoSuchMethodException nsme) {
                    System.out.println("WRONG: The constructor can not be found:" + nsme.getMessage());
                    System.exit(1);
                } catch (Exception e) {
                    System.out.println("WRONG: can not be upcast to plugin interface");
                    System.exit(1);
                }
            }
            switch (i) {
                case 0:
                    rawData_loadPlugin rawDataInputPlugin = (rawData_loadPlugin) plugin;
                    data = rawDataInputPlugin.start(pluginParam[i]);
//                    //inputData structure has been created Xtracker checks its validity before
//                    //going any further!
                    if (!((xLoad) data).isRawDataValid()) {
                        System.out.println("ERROR: The input data structure loaded by loadPlugin (" + plugin.getName() + ") is not valid!");
                        System.exit(1);
                    }
//                    Utils.saveObjectToXMLFile("xLoadRaw.xml", (xLoad) data);
//                    System.exit(0);
                    break;
                case 1:
//                    data = (xLoad)Utils.getObjectFromXMLFile("xLoadRaw.xml");
                    identData_loadPlugin ident = (identData_loadPlugin) plugin;
                    data = ident.start((xLoad) data, pluginParam[i]);
                    if (!((xLoad) data).isIdentDataValid()) {
                        System.out.println("ERROR: The identification data structure loaded by loadPlugin (" + plugin.getName() + ") is not valid!");
                        System.exit(1);
                    }
//                    Utils.saveObjectToXMLFile("xLoadIdent.xml", (xLoad)data);
//                    System.exit(0);
                    break;
                case 2:
//                    data = (xLoad)Utils.getObjectFromXMLFile("xLoadIdent.xml");
                    peakSelPlugin peakSelection = (peakSelPlugin) plugin;
                    data = peakSelection.start((xLoad) data, pluginParam[i]);
                    if (!((xPeaks) data).isValid()) {
                        System.out.println("ERROR: The peak selection data structure loaded by loadPlugin (" + plugin.getName() + ") is not valid!");
                        System.exit(1);
                    }
//                    Utils.saveObjectToXMLFile("xPeaks.xml", (xPeaks)data);
//                    System.exit(0);
                    break;
                case 3:
//                    data = (xPeaks)Utils.getObjectFromXMLFile("xPeaks.xml");
                    quantPlugin quant = (quantPlugin) plugin;
                    data = quant.start((xPeaks) data, pluginParam[i]);
                    if (!((xQuant) data).isValid()) {
                        System.out.println("ERROR: The quantitation data structure loaded by loadPlugin (" + plugin.getName() + ") is not valid!");
                        System.exit(1);
                    }
//                    Utils.saveObjectToXMLFile("xQuant.xml", (xQuant)data);
//                    System.exit(0);
                    break;
                case 4:
//                    data = (xQuant)Utils.getObjectFromXMLFile("xQuant.xml");
                    outPlugin output = (outPlugin) plugin;
                    output.start((xQuant) data, pluginParam[i]);
            }
        }
//        System.exit(0);
    }

    /**
     * This method dynamically creates the index file with the description of
     * the parameters of all plugins.
     * @param descPath the path containing all the .html parameter file descriptions.
     */
    public void printHtmlHelp(String descPath) {

        String indexPath = "Documentation/pluginInfo.html";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String htmlPage = "";

        htmlPage += "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head>\n<title>X-Tracker's Plugin Description Page</title>\n</head>";
        htmlPage += "\n<body style=\"color: rgb(0, 0, 0);\" alink=\"#ee0000\" link=\"#0000ee\" vlink=\"#551a8b\">\n<table style=\"text-align: left; width: 100%; height: 100%;\">\n";
        htmlPage += "\n<tbody>\n<tr>\n\t<td style=\"width: 30px; background-color: rgb(51, 102, 255); vertical-align: top;\"><img alt=\"X-Tracker's parameters\" src=\"img/name.png\"></td>\n\t";
        htmlPage += "<td style=\"width: 7px;\">&nbsp;</td>\n\t<td style=\"vertical-align: top;\">\n\t\t";
        htmlPage += "<h1>This page contains information on all available X-Tracker plugins</h1>\n\t\t";
        htmlPage += "<div align=\"center\"><h3>[Page automatically generated by X-Tracker v." + getVersion() + " on " + sdf.format(cal.getTime()) + "]</h3></div><br>\n\t\t";
        htmlPage += "X-Tracker is a new piece of software allowing Mass Spectrometry-based protein quantitation. Through an abstraction of the main steps involved in quantitation, X-Tracker is able to support quantitation by means of several current protocols, both at MS or Tandem MS level, and provide a flexible, platform-independent and easy-to-use quantitation environment.<br><br>\n\t\t";
        htmlPage += "X-Tracker has been developed in the Bioinformatics Group of Cranfield University, UK.<br><br>Please remember to update this page whenever a new plugin is installed by calling:<br><br><code>java -jar xTracker.jar</code></td><td style=\"text-align: right; height: 100%; width: 39%; vertical-align: top;\"><img style=\"width: 400px;\" alt=\"xTracker\" src=\"img/XtrackerLogo.png\"></td><tr><td style=\"width: 30px; background-color: rgb(51, 102, 255); vertical-align: top;\">&nbsp;</td><td style=\"width: 7px;\">&nbsp;</td><td colspan=\"2\"><br><br><h2>Plugin parameter pages</h2>\n\t\t";

        htmlPage += "\t<table><tr align=\"left\"><th>File</th><th>Name</th><th>Type</th><th>Short Description</th></tr>";
        //Let's list now possible plugin descriptions
        File f = new File(descPath);  //plugins
        File files[] = f.listFiles();   //array of available plugins
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && (files[i].getName().indexOf(".html")) > 0 && (files[i].getName().indexOf("index.html")) == -1) {
                String pN = files[i].getName().substring(0, files[i].getName().indexOf(".html"));
                String pluginData[] = getPluginData(pN);
                if (pluginData[0].length() == 0) {
                    pluginData[0] = "Not available.";
                    pluginData[1] = "Not available.";
                    pluginData[2] = "Not available.";
                } else {
                    if (pluginData[2].length() > 60) {
                        pluginData[2] = pluginData[2].substring(0, 60) + "...";
                    }
                }
                htmlPage += "<tr><td><a href=\"..\\Plugins\\" + files[i].getName() + "\" >" + files[i].getName() + "</a><br></td><td>" + pluginData[1] + "</td><td>" + pluginData[0] + "</td><td>" + pluginData[2] + "</td></tr>\n\t\t";
            }
        }
        htmlPage += "\n\t\t\t</table>";

        htmlPage += "\n\t\t";
        htmlPage += "\n\t\t<br><br></td>\n</tr>\n</tbody>\n</table>\n</body>\n</html>";


        try {
            // Create file
            FileWriter fstream = new FileWriter(indexPath);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(htmlPage);
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error, couldn't write pluginInfo.html file: " + e.getMessage());
        }
        System.out.println("Descriptions of plugin parameters can be found at " + indexPath);
    }

    /**
     * Given a plugin name in input, this plugin retrieves information about it like type, name, description. 
     * @param pluginName the plugin name (without .jar extension)
     * @return a String array where:
     * retVal[0] is pluginType
     * retVal[1] is pluginName and version in parenthesis ex. myPlugin (v.1.00)
     * retVal[2] a short plugin description (max 60 chars).
     */
    public static String[] getPluginData(String pluginName) {
        String[] retVal = {"", "", ""};
        pluginName += ".jar";
        String pluginPath = "Plugins/";
        File pFile = new File(pluginPath + pluginName);
        //if the plugin exists then let's get some information about it otherwise just return empty values
        if (pFile.exists()) {
            pluginInterface plugin = null; //the plugin to load
            String className = "";    //the classname should be like the filename without the .jar
            int index = -1; //a counter to check if it is a jar or not
            if ((index = pluginName.indexOf(".jar")) >= 0) {
                className = pluginName.substring(0, index);
                try {
//                    pluginLoader = new PluginClassLoader(pluginPath + pluginName);
                } catch (Exception e) {
                }

                // Loading the class
                try {
//                    plugin = (pluginInterface) pluginLoader.findClass("xtracker." + className).newInstance();
                    retVal[0] = plugin.getType();
                    retVal[1] = plugin.getName() + " (v." + plugin.getVersion() + ")";
                    retVal[2] = plugin.getDescription();
                } catch (Exception e) {
                }
            }
        }
        return retVal;
    }

    /**
     * Return X-Tracker version number
     * @return version as a string
     */
    public String getVersion() {
        return version;
    }
}
