
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerConfigData.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import xtrackergui.utils.guiutils.PluginType;
import xtrackergui.utils.parsers.XsdSchemaNode;

/**
 * Data class to model the data required to define a xtracker configuration
 *
 * @author andrew bullimore
 */
public class XTrackerConfigData {

    Document xmlDocument = null;
    List<XsdSchemaNode> xsdSchemaNodesMap = null;
    // private final strings store the tag/ item/ element name for
    // the configuration allowing the xml file configuration file to be
    // easily parsed
    private final String configRootElementName = "xTrackerPipeline";
    // store the data used to create the class in 'orig' variables
    // store updates to the configuration, if any in the non 'orig'
    // variables
    private String origRawDataLoadPluginJarFile = "";
    private String origRawDataLoadParamFile = "";
    private String rawDataLoadPluginJarFile = "";
    private String rawDataLoadParamFile = "";
    private String origIdentDataLoadPluginJarFile = "";
    private String origIdentDataLoadParamFile = "";
    private String identDataLoadPluginJarFile = "";
    private String identDataLoadParamFile = "";
    private String origPeakSelPluginJarFile = "";
    private String origPeakSelParamFile = "";
    private String peakSelPluginJarFile = "";
    private String peakSelParamFile = "";
    private String origQuantPluginJarFile = "";
    private String origQuantParamFile = "";
    private String quantPluginJarFile = "";
    private String quantParamFile = "";
    private String origOutputPluginJarFile = "";
    private String origOutputParamFile = "";
    private String outputPluginJarFile = "";
    private String outputParamFile = "";
    private String xsdSchemaFileLocation = "";
    private boolean empty = true;

    /**
     * Default constructor - creates empty configuration
     *
     */
    public XTrackerConfigData() {
    }

    /**
     * Create a xTracker configuration
     *
     * @param rawDataJarFile raw data jar file name
     * @param rawDataParamFile raw data parameters file name
     * @param identDataJarFile spectral identifications jar file name
     * @param identDataParamFile spectral identifications parameters file name
     * @param peakSelJarFile peak selection jar file name
     * @param peakSelectParamFile peak selection parameters filename
     * @param quantJarFile quantification jar file name
     * @param quantificationParamFile quantification parameters file name
     * @param outputJarFile output jar file name
     * @param outParamFile output parameters file name
     * 
     */
    public XTrackerConfigData(String rawDataJarFile,
                              String rawDataParamFile,
                              String identDataJarFile,
                              String identDataParamFile,
                              String peakSelJarFile,
                              String peakSelectParamFile,
                              String quantJarFile,
                              String quantificationParamFile,
                              String outputJarFile,
                              String outParamFile) {

        origRawDataLoadPluginJarFile = rawDataJarFile;
        origRawDataLoadParamFile = rawDataParamFile;
        rawDataLoadPluginJarFile = rawDataJarFile;
        rawDataLoadParamFile = rawDataParamFile;

        origIdentDataLoadPluginJarFile = identDataJarFile;
        origIdentDataLoadParamFile = identDataParamFile;
        identDataLoadPluginJarFile = identDataJarFile;
        identDataLoadParamFile = identDataParamFile;

        origPeakSelPluginJarFile = peakSelJarFile;
        origPeakSelParamFile = peakSelectParamFile;
        peakSelPluginJarFile = peakSelJarFile;
        peakSelParamFile = peakSelectParamFile;

        origQuantPluginJarFile = quantJarFile;
        origQuantParamFile = quantificationParamFile;
        quantPluginJarFile = quantJarFile;
        quantParamFile = quantificationParamFile;

        origOutputPluginJarFile = outputJarFile;
        origOutputParamFile = outParamFile;
        outputPluginJarFile = outputJarFile;
        outputParamFile = outParamFile;

        empty = false;
    }

    @Override
    public String toString() {

        String outPutString = "\nXTracker Configuration Data" +
                              "\nraw data jar file " + rawDataLoadPluginJarFile +
                              "\nraw data param file " + rawDataLoadParamFile +
                              "\nident data jar file " + identDataLoadPluginJarFile +
                              "\nident data param file " + identDataLoadParamFile +
                              "\npeak sel jar file " + peakSelPluginJarFile +
                              "\npeak sel param file " + peakSelParamFile +
                              "\nquant jar file " + quantPluginJarFile +
                              "\nquant param file " + quantParamFile +
                              "\noutput jar file " + outputPluginJarFile +
                              "\noutput param file " + outputParamFile;

        return outPutString;
    }

    /**
     *
     *
     */
    public boolean isAnEmptyConfiguration() {

        return empty;
    }

    /**
     *
     *
     */
    public boolean setIsAnEmptyConfiguration(boolean isEmpty) {

        return empty = isEmpty;
    }

    /**
     *
     *
     */
    public void setConfigDataDomDocument(Document doc) {

        xmlDocument = doc;
    }

    /**
     *
     *
     */
    public Document getConfigDataDomDocument() {

        return xmlDocument;
    }

    /**
     * Check if the xTracker configuration has changed
     *
     * @return true if the configuration has changed
     */
    public boolean configDataHasEdits() {

        boolean hasEdits = true;

        if(rawDataLoadPluginJarFile.equals(origRawDataLoadPluginJarFile) &&
           rawDataLoadParamFile.equals(origRawDataLoadParamFile) &&
           identDataLoadPluginJarFile.equals(origIdentDataLoadPluginJarFile) &&
           identDataLoadParamFile.equals(origIdentDataLoadParamFile) &&
           peakSelPluginJarFile.equals(origPeakSelPluginJarFile) &&
           peakSelParamFile.equals(origPeakSelParamFile) &&
           quantPluginJarFile.equals(origQuantPluginJarFile) &&
           quantParamFile.equals(origQuantParamFile) &&
           outputPluginJarFile.equals(origOutputPluginJarFile) &&
           outputParamFile.equals(origOutputParamFile)) {

            hasEdits = false;
        }

        return hasEdits;
    }

    public boolean configDataSetForAllPluginJars() {

        boolean allPluginsSet = true;

        if(rawDataLoadPluginJarFile.equals("") ||
           identDataLoadPluginJarFile.equals("") ||
           peakSelPluginJarFile.equals("") ||
           quantPluginJarFile.equals("") ||
           outputPluginJarFile.equals("")) {

            allPluginsSet = false;
        }

        return allPluginsSet;
    }

    /**
     * Save the XMl configuration data
     *
     */
    public void saveConfigData() {

        if(xmlDocument != null) {

            for(PluginType type : PluginType.values()) {

                if(type != PluginType.PLUGINTYPENOTDEFINED) {

                    Node node = xmlDocument.getElementsByTagName(type.getpluginXsdElementName()).item(0);
                    switch(type) {

                        case RAWDATAPLUGIN: {

                            if(rawDataLoadPluginJarFile.equals(origRawDataLoadPluginJarFile) == false) {

                                node.getAttributes().item(0).setTextContent(rawDataLoadPluginJarFile);
                                origRawDataLoadPluginJarFile = rawDataLoadPluginJarFile;
                            }

                            if(rawDataLoadParamFile.equals(origRawDataLoadParamFile) == false) {

                                node.setTextContent(rawDataLoadParamFile);
                                origRawDataLoadParamFile = rawDataLoadParamFile;
                            }
                            break;
                        }
                        case IDENTDATAPLUGIN: {

                            if(identDataLoadPluginJarFile.equals(origIdentDataLoadPluginJarFile) == false) {

                                node.getAttributes().item(0).setTextContent(identDataLoadPluginJarFile);
                                origIdentDataLoadPluginJarFile = identDataLoadPluginJarFile;
                            }

                            if(identDataLoadParamFile.equals(origIdentDataLoadParamFile) == false) {

                                node.setTextContent(identDataLoadParamFile);
                                origIdentDataLoadParamFile = identDataLoadParamFile;
                            }
                            break;
                        }
                        case PEAKSELECTIONPLUGIN: {

                            if(peakSelPluginJarFile.equals(origPeakSelPluginJarFile) == false) {
                            
                                node.getAttributes().item(0).setTextContent(peakSelPluginJarFile);
                                origPeakSelPluginJarFile = peakSelPluginJarFile;
                            }
                            
                            if(peakSelParamFile.equals(origPeakSelParamFile) == false) {

                                node.setTextContent(peakSelParamFile);
                                origPeakSelParamFile = peakSelParamFile;
                            }
                            break;
                        }
                        case QUANTIFICATIONPLUGIN: {

                            if(quantPluginJarFile.equals(origQuantPluginJarFile) == false) {
                            
                                node.getAttributes().item(0).setTextContent(quantPluginJarFile);
                                origQuantPluginJarFile = quantPluginJarFile;
                            }

                            if(quantParamFile.equals(origQuantParamFile) == false) {

                                node.setTextContent(quantParamFile);
                                origQuantParamFile = quantParamFile;
                            }
                            break;
                        }
                        case OUTPUTPLUGIN: {

                            if(outputPluginJarFile.equals(origOutputPluginJarFile) == false){
                                
                                node.getAttributes().item(0).setTextContent(outputPluginJarFile);
                                origOutputPluginJarFile = outputPluginJarFile;
                            }

                            if(outputParamFile.equals(origOutputParamFile) == false) {
                            
                                node.setTextContent(outputParamFile);
                                origOutputParamFile = outputParamFile;
                            }
                            break;
                        }
                        default: {
                        }
                    }
                }
            }

        } else {

            // New X-Tracker configuration - no document yet
            // just update all the originals even if they've
            // not changed
            origRawDataLoadPluginJarFile = rawDataLoadPluginJarFile;
            origRawDataLoadParamFile = rawDataLoadParamFile;
            origIdentDataLoadPluginJarFile = identDataLoadPluginJarFile;
            origIdentDataLoadParamFile = identDataLoadParamFile;
            origPeakSelPluginJarFile = peakSelPluginJarFile;
            origPeakSelParamFile = peakSelParamFile;
            origQuantPluginJarFile =  quantPluginJarFile;
            origQuantParamFile = quantParamFile;
            origOutputPluginJarFile = outputPluginJarFile;
            origOutputParamFile = outputParamFile;
        }
    }

    /**
     * Get the root/ document xml file element name - xTrackerPipeline
     *
     */
    public String getConfigRootElementName() {

        return configRootElementName;
    }

    /**
     * Get the raw data plugin jar file name
     *
     */
    public String getRawDataLoadPluginJarFile() {

        return rawDataLoadPluginJarFile;
    }

    /**
     * Set the original raw data load plugin jar file name
     *
     * @param rawDataJarFile The raw data jar file name
     */
    public void setOrigRawDataLoadPluginJarFile(String rawDataJarFile) {

        origRawDataLoadPluginJarFile = rawDataJarFile;
    }

    /**
     * Set the raw data load plugin jar file name
     *
     * @param rawDataJarFile The raw data jar file name
     */
    public void setRawDataLoadPluginJarFile(String rawDataJarFile) {

        rawDataLoadPluginJarFile = rawDataJarFile;
    }

    /**
     * Get the raw data plugin parameters file name
     *
     */
    public String getRawDataLoadParamFile() {

        return rawDataLoadParamFile;
    }

    /**
     * Set the original raw data plugin parameters file name
     *
     * @param rawDataParamFile Raw data parameters file
     */
    public void setOrigRawDataLoadParamFile(String rawDataParamFile) {

        origRawDataLoadParamFile = rawDataParamFile;
    }

    /**
     * Set the raw data plugin parameters file name
     *
     * @param rawDataParamFile Raw data parameters file
     */
    public void setRawDataLoadParamFile(String rawDataParamFile) {

        rawDataLoadParamFile = rawDataParamFile;
    }

    /**
     * Get the spectral identity data plugin jar file name
     *
     */
    public String getIdentDataLoadPluginJarFile() {
        
        return identDataLoadPluginJarFile;
    }

    /**
     * Set the original spectral identity data plugin jar file name
     *
     * @param identDataJarFile spectral identity plugin jar file name
     */
    public void setOrigIdentDataLoadPluginJarFile(String identDataJarFile) {

        origIdentDataLoadPluginJarFile = identDataJarFile;
    }

    /**
     * Set the spectral identity data plugin jar file name
     *
     * @param identDataJarFile spectral identity plugin jar file name
     */
    public void setIdentDataLoadPluginJarFile(String identDataJarFile) {

        identDataLoadPluginJarFile = identDataJarFile;
    }

    /**
     * Get the spectral identity data plugin parameters file name
     *
     */
    public String getIdentDataLoadParamFile() {

        return identDataLoadParamFile;
    }

    /**
     * Set the original spectral identity data plugin parameters file name
     *
     * @param identDataParamFile spectral identity plugin parameters file name
     */
    public void setOrigIdentDataLoadParamFile(String identDataParamFile) {

        origIdentDataLoadParamFile = identDataParamFile;
    }

    /**
     * Set the spectral identity data plugin parameters file name
     *
     * @param identDataParamFile spectral identity plugin parameters file name
     */
    public void setIdentDataLoadParamFile(String identDataParamFile) {

        identDataLoadParamFile = identDataParamFile;
    }

    /**
     * Get the Peak Selection plugin jar file name
     *
     */
    public String getPeakSelPluginJarFile() {

        return peakSelPluginJarFile;
    }

    /**
     * Set the original Peak Selection plugin jar file name
     *
     * @param peakSelJarFile Peak Selection plugin jar file name
     */
    public void setOrigPeakSelPluginJarFile(String peakSelJarFile) {

        origPeakSelPluginJarFile = peakSelJarFile;
    }


    /**
     * Set the Peak Selection plugin jar file name
     *
     * @param peakSelJarFile Peak Selection plugin jar file name
     */
    public void setPeakSelPluginJarFile(String peakSelJarFile) {

        peakSelPluginJarFile = peakSelJarFile;
    }

    /**
     * Get the Peak Selection plugin parameters file name
     *
     */
    public String getPeakSelParamFile() {

        return peakSelParamFile;
    }

    /**
     * Set the original peak Selection plugin parameters file name
     *
     * @param peakSelectParamFile Peak Selection plugin parameters file name
     */
    public void setOrigPeakSelParamFile(String peakSelectParamFile) {

        origPeakSelParamFile = peakSelectParamFile;
    }

    /**
     * Set the Peak Selection plugin parameters file name
     *
     * @param peakSelectParamFile Peak Selection plugin parameters file name
     */
    public void setPeakSelParamFile(String peakSelectParamFile) {

        peakSelParamFile = peakSelectParamFile;
    }

    /**
     * Get the Quantification plugin jar file name
     *
     */
    public String getQuantPluginJarFile() {
        
        return quantPluginJarFile;
    }

    /**
     * Set the Original Quantification plugin jar file name
     *
     * @param quantJarFile Quantification plugin jar file name
     */
    public void setOrigQuantPluginJarFile(String quantJarFile) {

        origQuantPluginJarFile = quantJarFile;
    }

    /**
     * Set the Quantification plugin jar file name
     *
     * @param quantJarFile Quantification plugin jar file name
     */
    public void setQuantPluginJarFile(String quantJarFile) {

        quantPluginJarFile = quantJarFile;
    }

    /**
     * Get the Quantification plugin parameters file name
     *
     */
    public String getQuantParamFile() {

        return quantParamFile;
    }

    /**
     * Set the original quantification plugin parameters file name
     *
     * @param quantifcationParamFile Quantification plugin parameters file name
     */
    public void setOrigQuantParamFile(String quantifcationParamFile) {

        origQuantParamFile = quantifcationParamFile;
    }

    /**
     * Set the Quantification plugin parameters file name
     *
     * @param quantifcationParamFile Quantification plugin parameters file name
     */
    public void setQuantParamFile(String quantifcationParamFile) {

        quantParamFile = quantifcationParamFile;
    }

    /**
     * Get the Output plugin jar file name
     *
     */
    public String getOutputPluginJarFile() {

        return outputPluginJarFile;
    }

    /**
     * Set the original output plugin jar file name
     *
     * @param outputJarFile Output plugin jar file name
     */
    public void setOrigOutputPluginJarFile(String outputJarFile) {

        origOutputPluginJarFile = outputJarFile;
    }

    /**
     * Set the output plugin jar file name
     *
     * @param outputJarFile Output plugin jar file name
     */
    public void setOutputPluginJarFile(String outputJarFile) {

        outputPluginJarFile = outputJarFile;
    }

    /**
     * Get the Output plugin parameters file name
     *
     */
    public String getOutputParamFile() {

        return outputParamFile;
    }

    /**
     * Set the original output plugin parameters file name
     *
     * @param outParamFile Output parameters file name
     */
    public void setOrigOutputParamFile(String outParamFile) {

        origOutputParamFile = outParamFile;
    }

    /**
     * Set the Output plugin parameters file name
     *
     * @param outParamFile Output parameters file name
     */
    public void setOutputParamFile(String outParamFile) {

        outputParamFile = outParamFile;
    }
}
