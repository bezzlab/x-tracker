package uk.ac.cranfield.xTracker;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.cranfield.xTracker.plugins.pluginInterface;

/**
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class PluginManager {
//    HashMap<String,pluginInterface> pluginNames;
    private String[] pluginNames = {"", "", "", ""};
    private String[] pluginParams = {"", "", "", ""};
    /**
     * the tags to be searched in the xtc configuration file to find the plugin names
     */
    private static final String[] TAG_NAMES = {"identdata_loadplugin", "rawdata_loadplugin", "quantplugin", "outplugin"};
    /**
     * the package name for each subset of plugins
     */
    private static final String[] PLUGIN_PACKAGES = {"identificationLoad", "rawdataLoad", "quantitation", "output"};
    /**
     * the class of middle files between two plugins
     */
//    private static final String[] INPUTS_CLASSES = {"xTracker.data.xLoad","xTracker.data.xLoad","xTracker.data.xLoad","xTracker.data.xPeaks","xTracker.data.xQuant"};
//    private static final String[] INPUTS_CLASSES = {"xTracker.data.Data","xTracker.data.Data","xTracker.data.Data","xTracker.data.Data","xTracker.data.Data"};
    /**
     * the names of method of validation in meddle files 
     */
//    private static final String[] VALIDATION_METHODS = {"isRawDataValid","isIdentDataValid","isValid","isValid"};
    private pluginInterface[] plugins = new pluginInterface[4];        

    public PluginManager(){
    }
    
    public void execute(){
        System.out.println("All required plugins support MS1: "+xTracker.SUPPORT_MS1);
        System.out.println("All required plugins support MS2: "+xTracker.SUPPORT_MS2);
        if((!xTracker.SUPPORT_MS1)&&(!xTracker.SUPPORT_MS2)){
            System.out.println("The combination of plugins used support neither MS1 nor MS2 protocol, please check again");
            System.exit(1);
        }
        System.out.println("Execute the pipeline");
        for (int i = 0; i < plugins.length; i++) {
            pluginInterface plugin = plugins[i];
            System.out.println("");
//            System.out.println(plugin.getClass());
            try {
                plugin.start(pluginParams[i]);
            } catch (SecurityException ex) {
                Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex){
                Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //check the existance of all required pluginNames and context relationship among pluginNames
    }
    
    public void setPlugin(int step,String pluginName,String pluginParamFile){
        int i = step - 1;
        pluginNames[i] = pluginName;
        pluginParams[i] = pluginParamFile;
        int index = pluginName.indexOf(".jar");
        if (index >= 0) {
            pluginName = pluginName.substring(0, index);
        }

        String fullClassPath = "uk.ac.cranfield.xTracker.plugins." + PLUGIN_PACKAGES[i] + "." + pluginName;
        Class pluginClass = null;
        try {
//                System.out.println(fullClassPath);
            pluginClass = Class.forName(fullClassPath);
        } catch (ClassNotFoundException ex) {
            //TODO  load external jar file here
            System.out.println("No plugin available called " + pluginName);
            System.exit(1);
//                pluginClass = Class.forName(pluginName)
        }
        try {
            Constructor ct = pluginClass.getConstructor(new Class[0]);
            plugins[i] = (pluginInterface) ct.newInstance(new Object[0]);
            xTracker.SUPPORT_MS1 = xTracker.SUPPORT_MS1 & plugins[i].supportMS1();
            xTracker.SUPPORT_MS2 = xTracker.SUPPORT_MS2 & plugins[i].supportMS2();
            //not in the core distribution, need to find the external jar file
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        File file = new File(pluginParamFile);
        if (!file.exists()) {
            System.out.println("Can not find the specified plugin parameter file "+file.getAbsolutePath());
            System.exit(1);
        }
    }
}
