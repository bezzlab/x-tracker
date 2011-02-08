
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.parsers
//    File: XmlParser.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.parsers;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;
import java.io.IOException;
import java.util.List;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import xtrackergui.model.XTrackerConfigData;
import xtrackergui.utils.fileutils.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import xtrackergui.exceptions.XmlFileValidationFailure;
import xtrackergui.exceptions.XsdSchemaFileNotSet;
import xtrackergui.utils.guiutils.PluginType;
/**
 * Utility Class which retrieves information from .xml or .xsd files (xsd schema files are xml files)
 *
 * @author andrew bullimore
 */
public class XmlParser {

    private static Logger logger = Logger.getLogger(xtrackergui.utils.parsers.XmlParser.class.getName());

    /**
     * Private Default Constructor - utility class with static methods,
     * there no requirement to create an instance of this class
     *
     */
    private XmlParser() {
    }

    @Override
    public String toString() {
        
        return new String("Xml Parser class");
    }

    /**
     * Checks if a xml is valid against the corresponding xsd schema, quoted in the xml file
     *
     * @param xmlFile The xml file, as a File object to validate
     * @param doc The DOM document of the parsed xml file
     * @return Ture if the xml file is valid
     */
    public static boolean validateXmlFile(File xsdSchemaFile, Document document) {

        boolean xmlFileValidated = false;

        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        try {

            Source schemaFile = new StreamSource(xsdSchemaFile);

            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));
            xmlFileValidated = true;
            
        } catch (FileNotFoundException ex) {

            logger.error(ex.getMessage());
            
        } catch (SAXException ex) {

            logger.error("xsd Validation error " + ex.getMessage());

        } catch (IOException ex) {

            logger.error(ex.getMessage());
        }

        return xmlFileValidated;
    }

    /**
     * Return the xsd schema file name quoted in an xml file 'xsi:schemaLocation' attribute
     *
     * @throws XsdSchemaFileNotFound
     * @param document The DOM document of the parsed xml file
     * @return The xsd schema file name/ location
     */
    public static String getXsdSchemaFileFromLocation(Document document) throws XsdSchemaFileNotSet {

        String xsdSchemaFileLocation = "";

        document.getDocumentElement().normalize();

        Element documentElement = document.getDocumentElement();
        if(documentElement.getAttributes().getNamedItem("xsi:schemaLocation") != null) {

            xsdSchemaFileLocation = documentElement.getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();

        } else {

            if(documentElement.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation") != null) {

                xsdSchemaFileLocation = documentElement.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation").getTextContent();

            } else {

                throw new XsdSchemaFileNotSet("xsd schema file missing");
            }
        }

        return xsdSchemaFileLocation;
    }
    
    /**
     * A method designed to parse, specfically, xTracker pipeline xml configuration files NB not generic
     *
     * @param configurationFile The xTracker xml configuration file to parse
     */
    public static XTrackerConfigData getXmlConfigurationFileData(String path, File configurationFile) throws FileNotFoundException, XmlFileValidationFailure, XsdSchemaFileNotSet, IOException {

        XTrackerConfigData configurationData = new XTrackerConfigData();

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        
        try {

            // create DOM document representation of the xml file
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(configurationFile);
            // cache the file
            configurationData.setConfigDataDomDocument(document);

            //
            document.getDocumentElement().normalize();

            // Get the xsd schema file from the xml gui configuration file
            String configurationXsdFileName = XmlParser.getXsdSchemaFileFromLocation(document);
            if(configurationXsdFileName.equals("xtracker.xsd") == true) {
                
                File configurationXsdFile = FileUtils.getFile(path + configurationXsdFileName);
                // Validate the xml gui configuration file if its okay carry
                if(XmlParser.validateXmlFile(configurationXsdFile, document)) {

                    configurationData.setIsAnEmptyConfiguration(false);

                    // Get the jar file name and associated parameters file - if present.
                    for(PluginType type : PluginType.values()) {

                        if(type != PluginType.PLUGINTYPENOTDEFINED) {

                            // Retrieve the data for this plugin - get by its tag name
                            // NB node attribute is the jar file and the text node for this tag carries the parameters file
                            // if present
                            Node node = document.getElementsByTagName(type.getpluginXsdElementName()).item(0);
                            switch(type) {

                                case RAWDATAPLUGIN: {

                                    // cache the original starting values for the jar file and params file if present
                                    configurationData.setOrigRawDataLoadPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setOrigRawDataLoadParamFile(node.getTextContent());

                                    configurationData.setRawDataLoadPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setRawDataLoadParamFile(node.getTextContent());
                                    break;
                                }
                                case IDENTDATAPLUGIN: {

                                    // cache the original starting values for the jar file and params file if present
                                    configurationData.setOrigIdentDataLoadPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setOrigIdentDataLoadParamFile(node.getTextContent());

                                    // cache the original starting values for the jar file and params file if present
                                    configurationData.setIdentDataLoadPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setIdentDataLoadParamFile(node.getTextContent());
                                    break;
                                }
                                case PEAKSELECTIONPLUGIN: {

                                    // cache the original starting values for the jar file and params file if present
                                    configurationData.setOrigPeakSelPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setOrigPeakSelParamFile(node.getTextContent());

                                    // cache the original starting values for the jar file and params file if present
                                    configurationData.setPeakSelPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setPeakSelParamFile(node.getTextContent());
                                    break;
                                }
                                case QUANTIFICATIONPLUGIN: {

                                    // cache the original starting values for the jar file and params file if present
                                    configurationData.setOrigQuantPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setOrigQuantParamFile(node.getTextContent());

                                    // cache the original starting values for the jar file and params file if present
                                    configurationData.setQuantPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setQuantParamFile(node.getTextContent());
                                    break;
                                }
                                case OUTPUTPLUGIN: {

                                    // cache the original starting values for the jar file and params file if present
                                    configurationData.setOrigOutputPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setOrigOutputParamFile(node.getTextContent());

                                    // cache the original starting values for the jar file and params file if present
                                    configurationData.setOutputPluginJarFile(node.getAttributes().item(0).getTextContent());
                                    configurationData.setOutputParamFile(node.getTextContent());
                                    break;
                                }
                                default: {
                                }
                            }
                        }
                    }
                
                } else {

                    throw new XmlFileValidationFailure("File " + configurationXsdFile.getName() + " failed validation");
                }

            } else {

                throw new XmlFileValidationFailure("File " + configurationFile.getName() + " is not a valid X-Tracker configuration file");
            }

        } catch(ParserConfigurationException ex) {

            logger.error("xml Parser error getting X-Tracker configuration " + configurationFile.getName(), ex);

        } catch(SAXException ex) {

            logger.error("xml Parser error getting X-Tracker configuration " + configurationFile.getName(), ex);
            
        }
        
       return configurationData;
    }

    public static Document createXmlConfigurationDomDocument(XTrackerConfigData configData) {

        Document document = getEmptyDomDocument();

        if(document != null) {

            Element rootElement = document.createElement(configData.getConfigRootElementName());
            rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttribute("xsi:noNamespaceSchemaLocation", "xtracker.xsd");
            document.appendChild(rootElement);

            String jarFileName = "";
            String pluginParmasFileName = "";
            for(PluginType type : PluginType.values()) {

                if(type != PluginType.PLUGINTYPENOTDEFINED) {

                    switch(type) {

                        case RAWDATAPLUGIN: {

                            jarFileName = configData.getRawDataLoadPluginJarFile();
                            pluginParmasFileName = configData.getRawDataLoadParamFile();
                            break;
                        }
                        case IDENTDATAPLUGIN: {

                            jarFileName = configData.getIdentDataLoadPluginJarFile();
                            pluginParmasFileName = configData.getIdentDataLoadParamFile();
                            break;
                        }
                        case PEAKSELECTIONPLUGIN: {

                            jarFileName = configData.getPeakSelPluginJarFile();
                            pluginParmasFileName = configData.getPeakSelParamFile();
                            break;
                        }
                        case QUANTIFICATIONPLUGIN: {

                            jarFileName = configData.getQuantPluginJarFile();
                            pluginParmasFileName = configData.getQuantParamFile();
                            break;
                        }
                        case OUTPUTPLUGIN: {

                            jarFileName = configData.getOutputPluginJarFile();
                            pluginParmasFileName = configData.getOutputParamFile();
                            break;
                        }
                        default: {
                        }
                    }

                    Element pluginElement = document.createElement(type.getpluginXsdElementName());
                    pluginElement.setAttribute("filename", jarFileName);
                    Text textNode = document.createTextNode(pluginParmasFileName);
                    pluginElement.appendChild(textNode);
                    rootElement.appendChild(pluginElement);

                    jarFileName = "";
                    pluginParmasFileName = "";
                }
            }
        }

        return document;
    }

    /**
     * Returns the GUI screens configuration data derived from file xTrackerGuiConfig.xml
     *
     * @throws FileNotFoundException
     * @throws XmlFileValidationFailure
     * @param configurationFile The xTracker xml configuration file to parse
     * @return Map keyed on the jar file and associated value of XML Schema (XSD) file name - if required
     */
    public static Map<String, String> getXTrackerGuiConfigurationData(File guiXmlConfFile) throws FileNotFoundException, XmlFileValidationFailure {

        Map<String, String> guiConfigData = new HashMap<String, String>();

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);

        try {

            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(guiXmlConfFile);
            document.getDocumentElement().normalize();

            // Get the xsd schema file from the xml gui configuration file
            String xsdGuiConfigurationFileName = XmlParser.getXsdSchemaFileFromLocation(document);
            // Should be 
            File xTrackerGuiXsdFile = FileUtils.getFile(xsdGuiConfigurationFileName);
            // Validate the xml gui configuration file if its okay carry
            if(XmlParser.validateXmlFile(xTrackerGuiXsdFile, document)) {

                // parsing xTrackerGuiConfig file explicitly here, not a generic method and
                // maybe a bit of a dogs dinner - thought about XPATH, its read only data
                Node rootElement = document.getElementsByTagName("xTrackerGuiConfig").item(0);
                NodeList childNodes = rootElement.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {

                    Node node = childNodes.item(i);
                    if ((node.getNodeType() == Node.ELEMENT_NODE) &&
                       (node.getNodeName().equals("jarToXsdSchema"))) {

                        String jarFile = node.getAttributes().item(0).getTextContent().toString().trim();
                        String xsdFile = node.getTextContent().toString().trim();
                        // make sure we've got a jarFile - should have
                        if(jarFile.equals("") == false)

                            if(xsdFile.equals("") == true) {
                            
                                guiConfigData.put(jarFile, null);

                            } else {

                                guiConfigData.put(jarFile, xsdFile);
                            }
          
                        if(logger.isDebugEnabled()) {

                            logger.debug("jar file " + jarFile +
                                         " xsd file " + xsdFile);
                        }
                    }
                }
            } else {

                throw new XmlFileValidationFailure("File " + xTrackerGuiXsdFile.getName() + " failed validation");
            }

        } catch(Exception ex) {

            logger.error("xml Parser error getting X-Tracker GUI config ", ex);
        }

        return guiConfigData;
    }

    /**
     * Return a list of XsdSchemaNodes which forms a map of the contents of the xsd schema file parsed
     *
     * @param xsdFile The xsd schema file to parse
     * @return A list of XsdSchemaNodes
     */
    public static List<XsdSchemaNode> getXsdSchemaMap(File xsdFile) {

        List<XsdSchemaNode> xsdSchemaMap = null;
        final String xsdFileName = xsdFile.getName();

        try {

            XSOMParser xsomParser = new XSOMParser();
            xsomParser.setErrorHandler(new ErrorHandler() {

                @Override
                public void warning(SAXParseException e) throws SAXException {

                    logger.error("XSOM (warning) parsing file " + xsdFileName, e);
                }

                @Override
                public void error(SAXParseException e) throws SAXException {

                    logger.error("XSOM (error) parsing file " + xsdFileName, e);
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {

                    logger.error("XSOM (fatal error) parsing file " + xsdFileName, e);
                }
            });

            xsomParser.parse(xsdFile);

            XSSchemaSet xsSchemaSet = xsomParser.getResult();

            if(xsSchemaSet == null) {

                logger.error("XmlParser::getXsdSchemaMap: Problem - no XSSchemaSet created for file " + xsdFileName, new Exception());

            } else {

                XsdSchemaVisitorParser xsdSchemaTraverser = new XsdSchemaVisitorParser();
                xsdSchemaMap = xsdSchemaTraverser.visit(xsSchemaSet);

                if(xsdSchemaMap != null) {

                    if(logger.isDebugEnabled()) {

                        logger.debug("XmlParser::getXsdSchemaMap: xsd schema map, num of nodes " + xsdSchemaMap.size());
                    }
                }

            }
            
        } catch(Exception e) {

            logger.error("Could not parse xsd schema file " + xsdFileName, e);
        }

        return xsdSchemaMap;
    }

    /**
     *
     *
     */
    public static Document getDomDocumentForXmlFile(File xmlFile) {

        Document document = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // make sure this line is always there otherwise you get crazy errors for weeks, crazy I'm telling you
        factory.setNamespaceAware(true);

        try{

            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(xmlFile);
            document.getDocumentElement().normalize();

        }
        catch (Exception e){

            logger.error("Could not parse file " + xmlFile.getName(), e);
        }

        return document;
    }

    /**
     *
     *
     */
    public static Document getEmptyDomDocument() {

        Document document = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // make sure this line is always there otherwise you get crazy erros for weeks
        factory.setNamespaceAware(true);

        try{

            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.newDocument();
            document.setXmlStandalone(true);
        
        }
        catch (Exception e){

            logger.error("Could not create empty DOM document ", e);
        }

        return document;
    }

    /**
     *
     *
     */
    public static void writeDomDocumentToXmlFile(Document document, File xmlFile) {

        try {

            Source source = new DOMSource(document);
            StreamResult result = new StreamResult(new FileOutputStream(xmlFile));
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

        } catch (TransformerConfigurationException ex) {

        } catch (TransformerException ex) {

        } catch (Exception ex) {

        }
    }
}
