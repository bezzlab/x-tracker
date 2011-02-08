
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackeConfigModelManager.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import xtrackergui.exceptions.XmlFileValidationFailure;
import xtrackergui.exceptions.XsdSchemaFileNotSet;
import xtrackergui.utils.guiutils.PluginType;
import xtrackergui.utils.parsers.XmlParser;

/**
 * Singleton Class which manages XTrackerConfigModel models (NB currently only one model at a time can be open)
 *
 * @author andrew bullimore
 */
public class XTrackeConfigModelManager {

    // Reference to the single instance of XTrackeConfigModelManager class
    private static XTrackeConfigModelManager xTrackerConfigModelManagerRef;
    // Map to store configuration models - ocurrently only ever one stored
    private Map<String, XTrackerConfigModel> configurationModels;
    // For debugging purposes and running the xTrackerGui project in Netbeans
    // this variable can be set to point to the directory where X-Tracker xsd
    // schema files reside
    private String xsdSchemaFileDirectoryPath = "C:\\Users\\andrew\\Documents\\Bioinformatics\\Proteomics\\xTracker\\xTracker_v1_2";

    /**
     * Create a Configuration model manager
     *
     */
    private XTrackeConfigModelManager() {

        configurationModels = new HashMap<String, XTrackerConfigModel>();
    }

    /**
     * Get the instance of XTrackeConfigModelManager - instantiate if
     * not already done so
     *
     */
    public static XTrackeConfigModelManager createInstance() {

        // Singletoe - enusre we only have one instance
        if (xTrackerConfigModelManagerRef == null) {
            xTrackerConfigModelManagerRef = new XTrackeConfigModelManager();
        }

        return xTrackerConfigModelManagerRef;
    }

    /**
     * Singleton class therefore clones are not supported
     *
     * @throws CloneNotSupportedException
     * @return Object - return type only required to fullfill the Overriding of this function
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        
        throw new CloneNotSupportedException();
    }

    @Override
    public String toString() {

        return new String("\nxTracker Configuration Model Manager has " +
                          configurationModels.size() + " files saved\n");
    }

    /**
     * Create a new (empty) x-tracker configuration model
     *
     * @param configurationModelName Name of the configuration model to create
     * @return returns the name for the configuration supplied in parameter configurationModelName and why not
     * 
     */
    public String createNewConfigurationModel(String configurationModelName) {

        // Create a new empty XTrackerConfigData object
        XTrackerConfigData configData = new XTrackerConfigData();

        // New model therefore only have the xsd files associated with the jar files
        // to start with
        XTrackerConfigModel configModel = new XTrackerConfigModel(XTrackerConfigModel.ConfigModelType.NEWXSD,
                                                                  configurationModelName,
                                                                  null,
                                                                  configData,
                                                                  xsdSchemaFileDirectoryPath);
        
        configurationModels.put(configurationModelName, configModel);

        return configurationModelName;
    }

    /**
     * Open an existing x-tracker configuration file and create a configuration model from the data
     *
     * @param configFile x-tracker xml configuration file
     * @return The name of the configuration opened - the file name of parameter configFile less any file extension
     *
     * IOException mess up FileNotFoundException
     */
    public String openConfigurationModel(String path, File configFile) throws FileNotFoundException, XmlFileValidationFailure, XsdSchemaFileNotSet, IOException {

        String configurationModelName = "";

        // take the configuration name from the opened file
        String tempConfigurationModelName = configFile.getName();

        // remove the .xml extension - assume we have one - filechooser set to
        // look for .xml files probably get problems otherwise
        configurationModelName = tempConfigurationModelName.substring(0, tempConfigurationModelName.lastIndexOf('.'));
        // parse the data from the xml configuration file into a XTrackerConfigData object
        // create a DOM object for the configuration - to up data and write out the config to file
        XTrackerConfigData configData = XmlParser.getXmlConfigurationFileData(path, configFile);

        if(configData.isAnEmptyConfiguration() == false) {

            // Create the model and mark as created from XTrackerConfigModel.ConfigModelType.EXISTINGXML
            XTrackerConfigModel configModel = new XTrackerConfigModel(XTrackerConfigModel.ConfigModelType.EXISTINGXML,
                                                                      configurationModelName,
                                                                      configFile,
                                                                      configData,
                                                                      xsdSchemaFileDirectoryPath);

            configurationModels.put(configurationModelName, configModel);

        }

        return configurationModelName;
    }

    /**
     * Check if the current x-tracker configuration has any missing plugins (all five
     * should be present to run the configuration)
     *
     * @param  configModelToCheck x-tracker configuration model to check for presence of
     *                                      all plugins being set in the configuration
     *
     * @return true if the model has all plugins set in the configuration file
     */
    public boolean checkXTrackerConfigModelHasAllPluginsSet(String configModelToCheck) {

        boolean allPluginsSet = true;

        if(configurationModels.containsKey(configModelToCheck)) {

            XTrackerConfigModel configModel = configurationModels.get(configModelToCheck);
            allPluginsSet = configModel.checkXTrackerConfigModelHasAllPluginsSet(configModelToCheck);
        }

        return allPluginsSet;
    }

    /**
     * Check if the current x-tracker configuration has any un-saved changes or edits
     *
     * @param  configModelToCheck x-tracker configuration model to check for unsaved edits
     * @return true if the model has unsaved changes or edits
     */
    public boolean checkXtrackerConfigModelForUnSavedChanges(String configModelToCheck) {

        boolean modelHasUnsavedChanges = false;

        if(configurationModels.containsKey(configModelToCheck)) {

            XTrackerConfigModel configModel = configurationModels.get(configModelToCheck);
            modelHasUnsavedChanges = configModel.checkModelForUnSavedEdits();
        }

        return modelHasUnsavedChanges;
    }

    /**
     * Saves any changes or edits to the current model - both the configuration
     * data (X-Tracker XMl configuration file) and any associated XML parameter
     * file data
     *
     * @param configModelToSave The configuration model to save
     */
    public void saveXTrackerConfigModel(String configModelToSave) {

        if(configurationModels.containsKey(configModelToSave)) {

            XTrackerConfigModel configModel = configurationModels.get(configModelToSave);
            configModel.saveConfigModelUnSavedEdits();   
        }
    }

    /**
     * Save any changes to the current config models configuration data specifically ie
     * the jar files selected and any associated XML parameter file names
     *
     * @param configModelToSave The configuration model to save
     */
    public void saveXTrackerConfigModelConfigurationData(String configModelToSave) {

        if(configurationModels.containsKey(configModelToSave)) {

            XTrackerConfigModel configModel = configurationModels.get(configModelToSave);
            configModel.saveConfigModelConfigurationDataUnSavedEdits();
        }
    }

    /**
     * Saves any changes or edits to the current model
     *
     * @param configModelToSave The configuration model to save
     */
    public void saveXTrackerParamFileForPlugin(String configModelToSave, PluginType pluginType) {

        if(configurationModels.containsKey(configModelToSave)) {

            XTrackerConfigModel configModel = configurationModels.get(configModelToSave);
            configModel.saveParamFileModelUnSavedEditsForPlugin(pluginType);
        }
    }

    /**
     * Saves any changes or edits to the current model
     *
     * @param configModelToSave The configuration model to save
     */
    public boolean checkXTrackerParamFileForUnsavedEditsForPlugin(String configModelToSave, PluginType pluginType) {

        boolean paramFileHasUnsavedChanges = false;

        if(configurationModels.containsKey(configModelToSave)) {

            XTrackerConfigModel configModel = configurationModels.get(configModelToSave);
            paramFileHasUnsavedChanges = configModel.checkParamFileModelForUnSavedEditsForPlugin(pluginType);
        }

        return paramFileHasUnsavedChanges;
    }

    /**
     * Closes/ removes the current x-tracker configuration model
     *
     * @param currentModelName The configuration model to close
     */
    public void closeXTrackerConfigModel(String currentModelName) {

        configurationModels.remove(currentModelName);
    }

    /**
     * Add a XTrackerConfigModel with the name supplied in parameter configModelName
     *
     * @param configModelName The name of the xTracker configuration model to add
     * @param xTrackerConfigModel The xTracker configuration model to add
     */
    public void addConfigurationModel(String configModelName, XTrackerConfigModel xTrackerConfigModel) {

        // adding xtracker configuration of same name overwrites an existing one
        configurationModels.put(configModelName, xTrackerConfigModel);
    }

    /**
     * Returns the currently active x-tracker configurayion model (NB only one model can be open at a time currently)
     *
     * @param configName The name of the configuration model to return
     * @return a x-tracker configuration model
     */
    public XTrackerConfigModel getCurrentConfigurationModel(String configName) {
        
        return configurationModels.get(configName);
    }

    /**
     * Checks if a x-tracker model exists
     *
     * @param configName The name of the x-tracker configuration model to look for
     * @return true if the given x-tracker configuration model exists
     */
    public boolean doesConfigurationModelExist(String configName) {

        return configurationModels.containsKey(configName);
    }

    /**
     * Remove the specified x-tracker configuration model
     *
     */
    public void removeConfigurationModel(String configName) {

        configurationModels.remove(configName);
    }

    /**
     * Clear/ remove all stored x-tracker configuration models (Not used - currently only one model can be open at a time)
     *
     */
    public void clearAllConfigurationModels() {

        configurationModels.clear();
    }

    /**
     * Get the number of configuration models currently open (Not used - currently only one model can be open at a time)
     *
     * @return Numver of configuration models
     */
    public int configurationModelCount() {

        return configurationModels.size();
    }
}
