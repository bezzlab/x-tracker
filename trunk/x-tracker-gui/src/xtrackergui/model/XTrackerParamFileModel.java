
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerParamFileModel.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import xtrackergui.utils.parsers.XmlParser;
import xtrackergui.utils.parsers.XsdSchemaNode;

/**
 * Model class which carries all the information about a particular xTracker parameter file
 * from its xsd schema file information
 *
 * @author andrew bullimore
 */
public class XTrackerParamFileModel {

    private List<String> jarFileAssocations = null;
    private String xsdSchemaFileName = "";
    private File xsdSchemaFile = null;
    private String parametersFileName = "";
    private File parametersFile = null;
    List<XsdSchemaNode> xsdSchemaNodesMap = null;
    private XTrackerXmlDocument paramFileXmlDocument;

    private Logger logger = Logger.getLogger(xtrackergui.model.XTrackerParamFileModel.class.getName());

    /**
     * Default constructor, creates an empty xtracker plugin model
     *
     */
    public XTrackerParamFileModel() {
    }

    /**
     * Create a xTracker parameters file model
     *
     * @param xsdSchemaFile The xsd schema file describing the data required by this model
     * @param paramsFileName The parameter file name associated with the jar file (will be null initially for new configuration models)
     * @param paramsFile The parameter file as a File object
     */
    public XTrackerParamFileModel(String xsdFileName,
                                  File xsdFile,
                                  String paramsFileName,
                                  File paramsFile) {

        jarFileAssocations = new ArrayList<String>();
        xsdSchemaFileName = xsdFileName;
        xsdSchemaFile = xsdFile;
        parametersFileName = paramsFileName;
        parametersFile = paramsFile;
        paramFileXmlDocument = new XTrackerXmlDocument("");
    }

    @Override
    public String toString() {

        StringBuffer outputString = new StringBuffer();
        
        outputString.append(" Param file: " + parametersFileName);

        if(parametersFile != null) {

            outputString.append(" Param file path: " + parametersFile.getAbsolutePath());
        }

        return outputString.toString();
    }

    /**
     * Initialise this xTracker plugin model using the xsd schema file, only
     *
     */
    public void initialiseFromXsdSchema() {

        if(xsdSchemaFile != null) {

            xsdSchemaNodesMap = XmlParser.getXsdSchemaMap(xsdSchemaFile);

            paramFileXmlDocument.initXmlDocument(xsdSchemaNodesMap);
            paramFileXmlDocument.buildXmlDocumentFromXsdSchema();

            logger.debug(paramFileXmlDocument);
            
        } else {

            // No schema file therefore cannot render the screen
        }
    }

    /**
     *
     *
     */
    public void updateFromXmlFile(File xmlParametersFile) {

        if(xsdSchemaFile != null) {

            xsdSchemaNodesMap = XmlParser.getXsdSchemaMap(xsdSchemaFile);
            paramFileXmlDocument.initXmlDocument(xsdSchemaNodesMap);
            paramFileXmlDocument.buildXmlDocumentFromXmlParametersFile(xmlParametersFile);

            logger.debug(paramFileXmlDocument);
        }
    }

    /**
     *
     *
     */
    public void addJarFileAssocation(String jarFileName) {

        jarFileAssocations.add(jarFileName);
    }

    /**
     *
     *
     */
    public boolean isJarFileAssociated(String jarFileName) {

        return jarFileAssocations.contains(jarFileName);
    }

    /**
     *
     *
     */
    public boolean checkParameterModelForUnsavedEdits() {

        return paramFileXmlDocument.xmlDocumentHasEdits();
    }

    /**
     *
     *
     */
    public void saveParameterModelUnsavedEdits() {

        paramFileXmlDocument.saveXmlDocumentEdits();
    }

    /**
     *
     *
     */
    public void rollBackParameterModelUnsavedEdits() {

        paramFileXmlDocument.rollBackXmlDocumentEdits();
    }

    /**
     *
     *
     */
    public String getXsdSchemaFileName() {

        return xsdSchemaFileName;
    }

    /**
     *
     *
     */
    public void setXsdSchemaFileName(String xsdFileName) {

        xsdSchemaFileName = xsdFileName;
    }

    /**
     *
     *
     */
    public File getXsdSchemaFile() {

        return xsdSchemaFile;
    }

    /**
     *
     *
     */
    public String getParametersFileName() {

        return parametersFileName;
    }

    /**
     *
     *
     */
    public void setParametersFileName(String paramsFileName) {

        parametersFileName = paramsFileName;
    }

    /**
     *
     *
     */
    public String getParametersFilePath() {

        String paramFilePath = "";

        if(parametersFile != null) {
            
            try {
                
                paramFilePath = parametersFile.getCanonicalPath();

            } catch (IOException ex) {

                
            }
        }

        return paramFilePath;
    }

    /**
     *
     *
     */
    public File getParameterFile() {

        return parametersFile;
    }

    /**
     *
     *
     */
    public void setParameterFile(File parameters) {

        parametersFile = parameters;
    }

    /**
     *
     *
     */
    public XTrackerXmlDocument getParamFileXmlDocument() {

        return paramFileXmlDocument;
    }
}
