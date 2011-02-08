
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerConfigModel.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import java.io.FileNotFoundException;
import com.sun.xml.xsom.XSSchema;
import java.io.File;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import xtrackergui.exceptions.XsdSchemaFileNotSet;
import xtrackergui.utils.fileutils.FileUtils;
import xtrackergui.utils.guiutils.PluginType;
import xtrackergui.utils.parsers.XmlParser;

/**
 *
 * @author andrew bullimore
 */
public class XTrackerConfigModel {

    public enum ConfigModelType {
        
        EXISTINGXML("Existing (xml)"),
        NEWXSD("New (xsd)"),
        UNKNOWN("Unknown");
        
        private final String configModelTypeDesc;
        
        ConfigModelType(String modelType) {
            
            configModelTypeDesc = modelType;    
        }
        
        public String getConfigModelTypeDesc() {
            
            return configModelTypeDesc;   
        }
    };

    // Store the XSOM XSSchema representation of the xtracker configuration file
    private String xsdSchemaFileDirectory;
    private XSSchema configutationSchema;
    private String configurationModelName = null;
    private File configurationFile = null;
    private XTrackerConfigData configurationData;
    private ConfigModelType configModelType = ConfigModelType.UNKNOWN;
    
    // Store the plugin models for each of the five plugin types for this
    // xtracker configuration
    private XTrackerParamFileModelManager paramFileModelManager;

    private Logger logger = Logger.getLogger(xtrackergui.model.XTrackerConfigModel.class.getName());
    
    /**
     *
     *
     */
    public XTrackerConfigModel() {
    }

    /**
     *
     *
     */
    public XTrackerConfigModel(ConfigModelType modelType,
                               String configModeName,
                               File configFileName,
                               XTrackerConfigData configFileData,
                               String xsdSchemaDirectory) {

        configModelType = modelType;
        configurationModelName = configModeName;
        configurationFile = configFileName;
        configurationData = configFileData;
        xsdSchemaFileDirectory = xsdSchemaDirectory;


        paramFileModelManager = new XTrackerParamFileModelManager();
    }
    
    @Override
    public String toString() {

        return new String("XTracker Configuration Model " + configurationModelName);
    }

    /**
     *
     *
     */
    public ConfigModelType getConfigModelType() {

        return configModelType;
    }

    /**
     *
     *
     */
    public XTrackerConfigData getModelConfigData() {

        return configurationData;
    }

    /**
     *
     *
     */
    public File getConfigurationFile() {

        return configurationFile;
    }

    /**
     *
     *
     */
    public void setConfigurationFile(File file) {

        configurationFile = file;
    }
    
    /**
     *
     *
     */
    public void createXTrackerParamFileModel(PluginType pluginType, String xmlFileDirectory, String xsdSchemaFileNameFile, File xsdSchemaFile) throws FileNotFoundException {

        String jarFileName = "";
        String paramFileName = "";
        File paramFile = null;

        switch(pluginType) {

            case RAWDATAPLUGIN: {

                jarFileName = configurationData.getRawDataLoadPluginJarFile();
                paramFileName = configurationData.getRawDataLoadParamFile();
                break;
            }
            case IDENTDATAPLUGIN: {

                jarFileName = configurationData.getIdentDataLoadPluginJarFile();
                paramFileName = configurationData.getIdentDataLoadParamFile();
                break;
            }
            case PEAKSELECTIONPLUGIN: {

                jarFileName = configurationData.getPeakSelPluginJarFile();
                paramFileName = configurationData.getPeakSelParamFile();
                break;
            }
            case QUANTIFICATIONPLUGIN: {

                jarFileName = configurationData.getQuantPluginJarFile();
                paramFileName = configurationData.getQuantParamFile();
                break;
            }
            case OUTPUTPLUGIN: {

                jarFileName = configurationData.getOutputPluginJarFile();
                paramFileName = configurationData.getOutputParamFile();
                break;
            }
            default: {
            }
        }

        if(paramFileName.equals("") == false) {

            paramFile = FileUtils.getFile(xmlFileDirectory + paramFileName);

        /*    if(checkXmlParametersFileIsValid(xmlFileDirectory, paramFile)) {


            } */
        }

        if(paramFileModelManager.doesParamFileModelExistForSchema(xsdSchemaFileNameFile) == false) {

            XTrackerParamFileModel paramFileModel = new XTrackerParamFileModel(xsdSchemaFileNameFile,
                                                                               xsdSchemaFile,
                                                                               paramFileName,
                                                                               paramFile);

            paramFileModel.addJarFileAssocation(jarFileName);
            

            if(paramFile != null) {

                paramFileModel.updateFromXmlFile(paramFile);
                
            } else {

                paramFileModel.initialiseFromXsdSchema();
            }

            paramFileModelManager.addParamFileModel(paramFileModel);
            
        } else {

            XTrackerParamFileModel paramFileModel = paramFileModelManager.getParamFileModelBySchemaFile(xsdSchemaFileNameFile);
            paramFileModel.addJarFileAssocation(jarFileName);
        }
    }

    /**
     *
     *
     */
    public void setConfigurationModelName(String configName) {

        configurationModelName = configName;
    }

    /**
     *
     *
     */
    public String getConfigurationModelName() {

        return configurationModelName;
    }

    /**
     *
     *
     */
    public boolean doesParamFileModelExistForSchema(String xsdSchemaFileName) {

        return paramFileModelManager.doesParamFileModelExistForSchema(xsdSchemaFileName);
    }

    /**
     *
     *
     */
    public boolean doesParamFileModelExistWithJarFileAssoc(String jarFileName) {

        return paramFileModelManager.doesParamFileModelExistWithJarFileAssoc(jarFileName);
    }

    /**
     *
     *
     */
    public void addParamFileModel(XTrackerParamFileModel paramFileModel) {

        paramFileModelManager.addParamFileModel(paramFileModel);
    }

    /**
     *
     *
     */
    public XTrackerParamFileModel getParamFileModelBySchemaFile(String xsdSchemaFileName) {

        return paramFileModelManager.getParamFileModelBySchemaFile(xsdSchemaFileName);
    }

    /**
     *
     *
     */
    public XTrackerParamFileModel getParamFileModelByJarFileAssoc(String jarFileName) {

        return paramFileModelManager.getParamFileModelByJarFileAssoc(jarFileName);
    }

    /**
     *
     *
     */
    public void removeParamFileModel(String xsdSchemaFileName) {

        paramFileModelManager.removeParamFileModel(xsdSchemaFileName);
    }

    /**
     *
     *
     */
    public void removeAllParamFileModels() {

        paramFileModelManager.removeAllParamFileModels();
    }

    /**
     *
     *
     */
    public String getPluginJarFileByPluginType(PluginType pluginType) {

        String jarFileName = "";

        switch(pluginType) {

            case RAWDATAPLUGIN: {

                jarFileName = configurationData.getRawDataLoadPluginJarFile();
                break;
            }
            case IDENTDATAPLUGIN: {

                jarFileName = configurationData.getIdentDataLoadPluginJarFile();
                break;
            }
            case PEAKSELECTIONPLUGIN: {

                jarFileName = configurationData.getPeakSelPluginJarFile();
                break;
            }
            case QUANTIFICATIONPLUGIN: {

                jarFileName = configurationData.getQuantPluginJarFile();
                break;
            }
            case OUTPUTPLUGIN: {

                jarFileName = configurationData.getOutputPluginJarFile();
                break;
            }
            default: {

            }
        }

        return jarFileName;
    }

    /**
     *
     * @author andrew
     */
    public void setPluginJarFileByPluginType(PluginType pluginType, String jarFileName) {

        switch(pluginType) {

            case RAWDATAPLUGIN: {

                configurationData.setRawDataLoadPluginJarFile(jarFileName);
                break;
            }
            case IDENTDATAPLUGIN: {

                configurationData.setIdentDataLoadPluginJarFile(jarFileName);
                break;
            }
            case PEAKSELECTIONPLUGIN: {

                configurationData.setPeakSelPluginJarFile(jarFileName);
                break;
            }
            case QUANTIFICATIONPLUGIN: {

                configurationData.setQuantPluginJarFile(jarFileName);
                break;
            }
            case OUTPUTPLUGIN: {

                configurationData.setOutputPluginJarFile(jarFileName);
                break;
            }
            default: {

            }
        }
    }


    /**
     *
     *
     */
    public boolean checkXmlFileIsValid(String xmlFileDirectory, File xmlFile) {

        boolean xmlFileIsValid = false;

        try {

            Document document = XmlParser.getDomDocumentForXmlFile(xmlFile);
            if(document != null) {

                String xsdFileFromLocation = XmlParser.getXsdSchemaFileFromLocation(document);
                File xsdFile = FileUtils.getFile(xmlFileDirectory + xsdFileFromLocation);
                xmlFileIsValid = XmlParser.validateXmlFile(xsdFile, document);
            }

        } catch (XsdSchemaFileNotSet ex) {

            logger.error(ex.getMessage());
            
        } catch (FileNotFoundException ex) {

            logger.error(ex.getMessage());
        }
        
        return xmlFileIsValid;
    }

    /**
     *
     *
     */
    public String getPluginParamFileByPluginType(PluginType pluginType) {

        String paramFileName = "";

        switch(pluginType) {

            case RAWDATAPLUGIN: {

                paramFileName = configurationData.getRawDataLoadParamFile();
                break;
            }
            case IDENTDATAPLUGIN: {

                paramFileName = configurationData.getIdentDataLoadParamFile();
                break;
            }
            case PEAKSELECTIONPLUGIN: {

                paramFileName = configurationData.getPeakSelParamFile();
                break;
            }
            case QUANTIFICATIONPLUGIN: {

                paramFileName = configurationData.getQuantParamFile();
                break;
            }
            case OUTPUTPLUGIN: {

                paramFileName = configurationData.getOutputParamFile();
                break;
            }
            default: {

            }
        }

        return paramFileName;
    }

    /**
     *
     *
     */
    public void setPluginParamFileByPluginType(PluginType pluginType, String paramFileName) {

        switch(pluginType) {

            case RAWDATAPLUGIN: {

                configurationData.setRawDataLoadParamFile(paramFileName);
                break;
            }
            case IDENTDATAPLUGIN: {

                configurationData.setIdentDataLoadParamFile(paramFileName);
                break;
            }
            case PEAKSELECTIONPLUGIN: {

                configurationData.setPeakSelParamFile(paramFileName);
                break;
            }
            case QUANTIFICATIONPLUGIN: {

                configurationData.setQuantParamFile(paramFileName);
                break;
            }
            case OUTPUTPLUGIN: {

                configurationData.setOutputParamFile(paramFileName);
                break;
            }
            default: {

            }
        }
    }

    /**
     * Check if the current x-tracker configuration has any missing plugins (all five
     * should be present to run the configuration)
     *
     * @param  configModelToCheck x-tracker configuration model to check for presence of
     *                                      all plugins being set in the configuration
     *
     * @return true if the model has all plugins set in the configuration file/ data
     */
    public boolean checkXTrackerConfigModelHasAllPluginsSet(String configModelToCheck) {
        
        return configurationData.configDataSetForAllPluginJars();
    }

    /**
     *
     *
     */
    public boolean checkModelForUnSavedEdits() {

        boolean hasEdits = false;

        if(configurationData.configDataHasEdits()) {

        //    paramFileModelManager.
            hasEdits = true;
        }

        if(hasEdits == false) {

            for(PluginType type : PluginType.values()) {

                String jarFileName = "";
                if(type != PluginType.PLUGINTYPENOTDEFINED) {

                    switch(type) {

                        case RAWDATAPLUGIN: {

                            jarFileName = configurationData.getRawDataLoadPluginJarFile();
                            break;
                        }
                        case IDENTDATAPLUGIN: {

                            jarFileName = configurationData.getIdentDataLoadPluginJarFile();
                            break;
                        }
                        case PEAKSELECTIONPLUGIN: {

                            jarFileName = configurationData.getPeakSelPluginJarFile();
                            break;
                        }
                        case QUANTIFICATIONPLUGIN: {

                            jarFileName = configurationData.getQuantPluginJarFile();
                            break;
                        }
                        case OUTPUTPLUGIN: {

                            jarFileName = configurationData.getOutputPluginJarFile();
                            break;
                        }
                        default: {
                        }
                    }
                }

                // break as soon as we get a model with edits - some models with a
                // few jar files associated with them may get checked more than once
                XTrackerParamFileModel model = paramFileModelManager.getParamFileModelByJarFileAssoc(jarFileName);
                if(model != null) {
                    
                    if(model.checkParameterModelForUnsavedEdits()) {

                        hasEdits = true;
                        break;
                    }
                }
            }
        }

        return hasEdits;
    }

    /**
     *
     *
     */
    public void saveConfigModelUnSavedEdits() {

        saveConfigModelConfigurationDataUnSavedEdits();
        saveAllParamFileModelUnSavedEdits();
    }

    /**
     *
     *
     */
    public void saveConfigModelConfigurationDataUnSavedEdits() {

        if(configurationData.configDataHasEdits()) {

            configurationData.saveConfigData();

            if(configurationFile != null) {

                Document configDataDomDocument = configurationData.getConfigDataDomDocument();
                if(configDataDomDocument != null) {
                    // should'nt be null but what to do if it is??
                    XmlParser.writeDomDocumentToXmlFile(configDataDomDocument, configurationFile);
                }
            }
        }
    }
    
    /**
     *
     *
     */
    public void saveAllParamFileModelUnSavedEdits() {

        for(PluginType type : PluginType.values()) {

            String jarFileName = "";
            if(type != PluginType.PLUGINTYPENOTDEFINED) {

                switch(type) {

                    case RAWDATAPLUGIN: {

                        jarFileName = configurationData.getRawDataLoadPluginJarFile();
                        break;
                    }
                    case IDENTDATAPLUGIN: {

                        jarFileName = configurationData.getIdentDataLoadPluginJarFile();
                        break;
                    }
                    case PEAKSELECTIONPLUGIN: {

                        jarFileName = configurationData.getPeakSelPluginJarFile();
                        break;
                    }
                    case QUANTIFICATIONPLUGIN: {

                        jarFileName = configurationData.getQuantPluginJarFile();
                        break;
                    }
                    case OUTPUTPLUGIN: {

                        jarFileName = configurationData.getOutputPluginJarFile();
                        break;
                    }
                    default: {
                    }
                }
            }

            XTrackerParamFileModel model = paramFileModelManager.getParamFileModelByJarFileAssoc(jarFileName);
            if(model != null) {

                if(model.checkParameterModelForUnsavedEdits()) {

                    model.saveParameterModelUnsavedEdits();

                    String xsdFileName = model.getXsdSchemaFileName();
                    XTrackerXmlDocument xmlDocument = model.getParamFileXmlDocument();
                    Document domDoc = xmlDocument.createDomDocument(xsdFileName);
                    File existingParamsFile = model.getParameterFile();
                    if(existingParamsFile != null) {

                        XmlParser.writeDomDocumentToXmlFile(domDoc, existingParamsFile);
                    }
                }
            }
        }
    }

    /**
     *
     *
     */
    public boolean checkParamFileModelForUnSavedEditsForPlugin(PluginType pluginType) {

        boolean modelHasEdits = false;

        if(pluginType != PluginType.PLUGINTYPENOTDEFINED) {

            String jarFileName = "";

            switch(pluginType) {

                case RAWDATAPLUGIN: {

                    jarFileName = configurationData.getRawDataLoadPluginJarFile();
                    break;
                }
                case IDENTDATAPLUGIN: {

                    jarFileName = configurationData.getIdentDataLoadPluginJarFile();
                    break;
                }
                case PEAKSELECTIONPLUGIN: {

                    jarFileName = configurationData.getPeakSelPluginJarFile();
                    break;
                }
                case QUANTIFICATIONPLUGIN: {

                    jarFileName = configurationData.getQuantPluginJarFile();
                    break;
                }
                case OUTPUTPLUGIN: {

                    jarFileName = configurationData.getOutputPluginJarFile();
                    break;
                }
                default: {
                }
            }

            XTrackerParamFileModel model = paramFileModelManager.getParamFileModelByJarFileAssoc(jarFileName);
            if(model != null) {

                if(model.checkParameterModelForUnsavedEdits()) {

                    modelHasEdits = true;
                }
            }
        }

        return modelHasEdits;
    }

    /**
     *
     *
     */
    public void saveParamFileModelUnSavedEditsForPlugin(PluginType pluginType) {

        if(pluginType != PluginType.PLUGINTYPENOTDEFINED) {

            String jarFileName = "";

            switch(pluginType) {

                case RAWDATAPLUGIN: {

                    jarFileName = configurationData.getRawDataLoadPluginJarFile();
                    break;
                }
                case IDENTDATAPLUGIN: {

                    jarFileName = configurationData.getIdentDataLoadPluginJarFile();
                    break;
                }
                case PEAKSELECTIONPLUGIN: {

                    jarFileName = configurationData.getPeakSelPluginJarFile();
                    break;
                }
                case QUANTIFICATIONPLUGIN: {

                    jarFileName = configurationData.getQuantPluginJarFile();
                    break;
                }
                case OUTPUTPLUGIN: {

                    jarFileName = configurationData.getOutputPluginJarFile();
                    break;
                }
                default: {
                }
            }

            XTrackerParamFileModel model = paramFileModelManager.getParamFileModelByJarFileAssoc(jarFileName);
            if(model != null) {

                if(model.checkParameterModelForUnsavedEdits()) {

                    model.saveParameterModelUnsavedEdits();

                    String xsdFileName = model.getXsdSchemaFileName();
                    XTrackerXmlDocument xmlDocument = model.getParamFileXmlDocument();
                    Document domDoc = xmlDocument.createDomDocument(xsdFileName);
                    File existingParamsFile = model.getParameterFile();
                    if(existingParamsFile != null) {

                        XmlParser.writeDomDocumentToXmlFile(domDoc, existingParamsFile);
                    }
                }
            }
        }
    }
}
