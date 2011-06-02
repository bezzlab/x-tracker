package xtracker;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Dimension;
import java.awt.Toolkit;


import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;


public class displayTable implements outPlugin
{
    /**
     * Prints the "3D" matrix with the results.
     * @param InputData the data to be printed.
     */
    public void start(xQuant InputData, String paramFile)
    {
       //Let's first load the control_condition in from the xml parameter file.
       

       this.loadParams(paramFile);

        
        
        myTable frame = new myTable(InputData,normalisation);
		frame.setDefaultCloseOperation( javax.swing.JFrame.EXIT_ON_CLOSE );
		frame.setTitle("X-Tracker's Quantitation Results");
        frame.setSize(800, 600);
        frame.setResizable(true);
        // Get the size of the default screen

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
       // System.out.println("Screen size: "+ dim.width +"x"+dim.height);
        frame.pack();
		frame.setLocationRelativeTo( null );
        frame.setVisible(true);
        


    }
    
    public void loadParams(String dataFile){
        
        int i=0;
        File file = new File(dataFile);
       
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        
        try{ 
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            // create a SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


            doc.getDocumentElement().normalize();

            Node nodeLst = doc.getElementsByTagName("param").item(0);

            String schemaLocation="";

            if(nodeLst.getAttributes().getNamedItem("xsi:schemaLocation") != null){
                schemaLocation=nodeLst.getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();
            }
            else {
                if(nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation")!= null){
                schemaLocation=nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation").getTextContent();
                }
                else{
                    System.out.println("ERROR: No .xsd schema is provided for " + dataFile );
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
                System.out.println("\n\nERRROR - could not validate the input file " + dataFile+"!");
                System.out.print(e);
                System.exit(1);
            }
            nodeLst = doc.getElementsByTagName("param").item(0);
           
            NodeList itemList=  nodeLst.getChildNodes();
            for(i=0; i<itemList.getLength(); i++){
                Node item= itemList.item(i);
                if(item.getNodeType() == Node.ELEMENT_NODE){ 
                   if(item.getNodeName().equals("normalisation")){
                        String normString=item.getTextContent();
                        
                        if(normString.toLowerCase().equals("yes")){
                                this.normalisation=true;

                        }
                        else{
                            if(normString.toLowerCase().equals("no")){
                                this.normalisation=false;

                        }
                            else{
                                System.out.println("ERROR: displayTable.jar Normalisation value:" + normString + " in " +dataFile);
                                System.exit(1);
                            }

                        }
                   }
                }
            }
        }
        catch(Exception e){System.out.println(e);}
    }
    
    public boolean stop()
    {
        System.out.println(getName()+": stopping...");
        return true;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public String getType()
    {

        return type;
    }
    
        public String getDescription()
    {

        return description;
    }
    
    public boolean normalisation=false;
    private final static String name = "DisplayTable";
    private final static String version = "1.0";
    private final static String type = "OUTPUT_plugin";
    private final static String description = "The plugin outputs in a tabular form the results of quantitation.\n\tResults can be normalised accross the different labels or not.";
}