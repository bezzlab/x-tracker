package uk.ac.cranfield.xTracker;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.cranfield.xTracker.plugins.pluginInterface;

/**
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class PluginManager {
    final static int IDENTIFICATION = 1;
    final static int SPECTRA = 2;
    final static int QUANTITATION = 3;
    final static int OUTPUT = 4;

    private ArrayList<Plugin> plugins = new ArrayList<Plugin>();
    
    private HashMap<String,String> packages = new HashMap<String, String>();
    private HashMap<String,Integer> types = new HashMap<String, Integer>();
    private HashMap<Integer,Boolean> flags = new HashMap<Integer,Boolean>();

    public PluginManager(){
        //keys are the values used in the mzQuantML configuration file
        //values are the package name
        packages.put("load identification", "identificationLoad");
        packages.put("load raw spectra", "rawdataLoad");
        packages.put("feature detection and quantitation", "quantitation");
        packages.put("output", "output");
        types.put("load identification", IDENTIFICATION);
        types.put("load raw spectra", SPECTRA);
        types.put("feature detection and quantitation", QUANTITATION);
        types.put("output", OUTPUT);
        flags.put(IDENTIFICATION, Boolean.FALSE);
        flags.put(SPECTRA, Boolean.FALSE);
        flags.put(QUANTITATION, Boolean.FALSE);
        flags.put(OUTPUT, Boolean.FALSE);
    }
    /**
     * after the pipeline has been generated, execute it
     */
    public void execute(){
        System.out.println("All required plugins support MS1: "+xTracker.SUPPORT_MS1);
        System.out.println("All required plugins support MS2: "+xTracker.SUPPORT_MS2);
        if((!xTracker.SUPPORT_MS1)&&(!xTracker.SUPPORT_MS2)){
            System.out.println("");
            System.out.println("The combination of plugins used support neither MS1 nor MS2 protocol, please check again");
            System.exit(1);
        }
        Collections.sort(plugins);
        //check whether all steps have been set
        for(String type:types.keySet()){
            if(!flags.get(types.get(type))){
                System.out.println("The "+type+" type plugin is missing");
                System.exit(1);
            }
        }
        
        System.out.println("Execute the pipeline");
        for (int i = 0; i < plugins.size(); i++) {
            System.out.println("");
            try {
                Plugin plugin = plugins.get(i);
                plugin.getPlugin().start(plugin.getParam());
            } catch (SecurityException ex) {
                Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex){
                Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * set flag for emPAI method which does not need spectral files
     */
    public void emPaiSpecial(){
        flags.put(SPECTRA, Boolean.TRUE);
    }
    /**
     * get the package name from the type defined in the configuration file
     * @param type
     * @return 
     */
    private String getPackageName(String type){
        if(!packages.containsKey(type)) {
            System.out.println("Please only use one value from "+Arrays.toString(packages.keySet().toArray())+" in the mzQuantML configuration file");
            System.exit(1);
        }
        return packages.get(type);
    }
    /**
     * get the corresponding predefined type value from the type defined in the configuration file
     * @param type
     * @return 
     */
    private int getType(String type){
        if(!types.containsKey(type)) {
            System.out.println("Please only use one value from "+Arrays.toString(packages.keySet().toArray())+" in the mzQuantML configuration file");
            System.exit(1);
        }
        return types.get(type);
    }
    /**
     * add the plugin to the pipeline
     * @param pluginName
     * @param pluginParamFile
     * @param pluginType 
     */
    public void addPlugin(String pluginName,String pluginParamFile, String pluginType){
        int index = pluginName.indexOf(".jar");
        if (index >= 0) {
            pluginName = pluginName.substring(0, index);
        }
        pluginType = pluginType.toLowerCase();
        
        int type = getType(pluginType);
        switch(type){
            case IDENTIFICATION:
            case SPECTRA:
            case QUANTITATION:
                if(flags.get(type)){
                    System.out.println("At the moment, xTracker only allows one "+pluginType+" plugin in the pipeline");
                    System.exit(1);
                }
                flags.put(type, Boolean.TRUE);
                break;
            case OUTPUT:
                flags.put(type, Boolean.TRUE);
        }
        Plugin plugin = new Plugin(pluginName, pluginParamFile, type, getPackageName(pluginType));
        plugins.add(plugin);
    }
}
/**
 * the comparison is implemented to order the plugin in the pipeline to follow order:
 * identification, spectra, quantitation and output
 * @author Jun Fan@cranfield
 */
class Plugin implements Comparable<Plugin> {
    /**
     * plugin name
     */
    String name = "";
    /**
     * plugin type
     */
    int type = 0;
    /**
     * plugin parameter file
     */
    String param = "";
    
    pluginInterface plugin;
    
    public Plugin(String name,String param,int type,String packageName){
        this.name = name;
        this.type = type;
        this.param = param;
        
        String fullClassPath = "uk.ac.cranfield.xTracker.plugins." + packageName + "." + name;
        Class pluginClass = null;
        try {
//                System.out.println(fullClassPath);
            pluginClass = Class.forName(fullClassPath);
        } catch (ClassNotFoundException ex) {
            //TODO  load external jar file here
//                pluginClass = Class.forName(pluginName)
            System.out.println("No plugin available called " + name);
            System.exit(1);
        }
        try {
            Constructor ct = pluginClass.getConstructor(new Class[0]);
            plugin = (pluginInterface) ct.newInstance(new Object[0]);
            xTracker.SUPPORT_MS1 = xTracker.SUPPORT_MS1 & plugin.supportMS1();
            xTracker.SUPPORT_MS2 = xTracker.SUPPORT_MS2 & plugin.supportMS2();
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //there should be parameter file required for spectra plugin as they are defined in the identification plugin parameter (relationship)
        if(param.length()==0){
            if(type!=PluginManager.SPECTRA){
                System.out.println("The plugin in any type except loading spectra always needs a parameter file. However plugin "+name+" does not have one");
                System.exit(1);
            }
        }else{
            File file = new File(param);
            if (!file.exists()) {
                System.out.println("Can not find the specified plugin parameter file "+file.getAbsolutePath());
                System.exit(1);
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getParam() {
        return param;
    }

    public int getType() {
        return type;
    }

    public pluginInterface getPlugin() {
        return plugin;
    }

    @Override
    public int compareTo(Plugin o) {
        return this.getType() - o.getType();
    }
}