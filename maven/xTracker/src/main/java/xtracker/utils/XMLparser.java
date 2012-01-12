package xtracker.utils;

import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
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
 *
 * @author Jun Fan@cranfield
 */
public class XMLparser {
    private Document doc;
    private String filename;
    private boolean validated = false;
    public XMLparser(String fileName){
        filename = fileName;
    }
    /**
     * validate the xml file with the xsd file provided
     * @param validatorTagName the tag where contains the location of the xsd file
     */
    public void validate(String validatorTagName){
        if ((filename.indexOf(".xtc") < 0) 
                && (filename.indexOf(".xtp") < 0)
                && (filename.indexOf(".xml") < 0)) {
            return;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(filename);

            // create a SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

//            doc.getDocumentElement().normalize();
            Node nodeLst = doc.getElementsByTagName(validatorTagName).item(0);
            if (nodeLst == null) {
                System.out.println("ERROR: The configuration file can not be validated as " + validatorTagName + " does not exist.");
                System.exit(1);
            }

            String schemaLocation = "";

            if (nodeLst.getAttributes().getNamedItem("xsi:schemaLocation") != null) {
                schemaLocation = nodeLst.getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();
            } else {
                if (nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation") != null) {
                    schemaLocation = nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation").getTextContent();
                } else {
                    System.out.println("ERROR: No .xsd schema is provided for " + filename);
                    System.exit(1);
                }
            }
            // load the xtracker WXS schema
            File xsdFile = new File(schemaLocation);
            if (!xsdFile.exists()) {
                System.out.println("ERROR: Can not find the specified xsd file " + schemaLocation + " for validation");
                System.exit(1);
            }
            Source schemaFile = new StreamSource(xsdFile);
            Schema schema = factory.newSchema(schemaFile);
            // create a Validator instance
            Validator validator = schema.newValidator();
            try {
                validator.validate(new DOMSource(doc));
            } catch (SAXException e) {
                // instance document is invalid!
                System.out.println("\n\nERROR - could not validate the input file " + filename + "!");
                System.out.print(e);
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Exception while reading " + filename + "!\n" + e);
        }
        validated = true;
    }
    
    public boolean isValidated(){
        return validated;
    }
    
    public Node getElement(String tagName){
        if(isValidated()){
            return doc.getElementsByTagName(tagName).item(0);
        }
        return null;
    }
}
