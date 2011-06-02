package xtracker;




import java.io.*;
import java.util.regex.*;

import java.util.*;


import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class loadAllIdents implements identData_loadPlugin
{


    /**
     * Plugin that does not load any identification.
     * @param inputData the xLoad data structure
     * @param paramFile an (empty) parameter file.
     * @return ret the xLoad structure as in input.
     */
    public xLoad start(xLoad inputData, String paramFile)
    {


         xLoad ret = inputData;
         System.out.println(getName()+": starting...");

        String rawDataFile="";
        Pattern myPattern;

        //let's load files to process
        this.loadParams(paramFile);




        for(int fileCnt=0; fileCnt<rawDataFiles.size();fileCnt++){

            //The raw data file.
            rawDataFile=rawDataFiles.elementAt(fileCnt);

            xLoadData myLoadData=inputData.getDataElemAt(fileCnt);


            File myFile= new File(rawDataFile);
            Scanner scanner=null;
             try{
                scanner = new Scanner(myFile);
             }
             catch(Exception e){
                System.out.println("Error while opening file " + rawDataFile);
                System.out.println(e);

             }
             try {
                    float parIonMz=0f;
                    float retTime=0f;
                    int charge=0;
                    int specCounter=0;

                    //first use a Scanner to get each line
                    while ( scanner.hasNextLine() ){

                         String line=scanner.nextLine();
                         myPattern = Pattern.compile("^PEPMASS");
                         Vector<Float> mzVals= new Vector<Float>();
                         Vector<Float> intVals= new Vector<Float>();

                         Matcher m= myPattern.matcher(line);

                         if(m.find()){
                              // System.out.println(line);
                               line=line.substring(8);
                               parIonMz=Float.valueOf(line).floatValue();

                         }
                         else{
                            myPattern = Pattern.compile("^CHARGE");
                            m= myPattern.matcher(line);
                            if(m.find()){
                                line=line.substring(7,line.length()-1);
                           //     System.out.println(line);
                                charge=Integer.valueOf(line).intValue();
                            }
                            else{
                                   myPattern = Pattern.compile("^TITLE");
                                   m= myPattern.matcher(line);
                                   if(m.find()){
                                        //Let's try to retrieve the retention time (sometimes called elution time).
                                        //In this case we look for "Elution:" in the title or for "rt="
                                       int elTimeInd=line.indexOf("Elution:");
                                       if(elTimeInd>-1){
                                        //OK elution time found!
                                        line=line.substring(elTimeInd+8);
                                        //System.out.println("Elution time:" +line);
                                        line=line.trim();

                                        line=line.substring(0, line.indexOf(" "));
                             //           System.out.println("El time:" +line);
                                        try{
                                            retTime=Float.valueOf(line).floatValue();
                                        }
                                        catch(Exception e){
                                            //Just nop. Value is set to 0 anyway.
                                        }
                                        }
                                       else{
                                          int rtTimeInd=line.indexOf("rt=");
                                          if(rtTimeInd>-1){
                                            //OK retention time found!
                                            line=line.substring(rtTimeInd+3);
                                            line=line.trim();
                                            line=line.substring(0, line.indexOf(" "));
                                            try{
                                            retTime=Float.valueOf(line).floatValue();
                                            }
                                            catch(Exception e){
                                            //Just nop. Value is set to 0 anyway.
                                            }
                                        }
                                       }

                                       }
                                   else{
                                        myPattern = Pattern.compile("^[0-9]+");
                                        m= myPattern.matcher(line);
                                        if(m.find()){
                                            String [] tmp= new String[2];
                                            tmp=line.split(" ");
                                            mzVals.add(Float.valueOf(tmp[0]));
                                            intVals.add(Float.valueOf(tmp[1]));


                                        }
                                        else{
                                            myPattern = Pattern.compile("^END IONS");
                                            m= myPattern.matcher(line);
                                            if(m.find()){
                                             //Lets add values!
                                             xSpectrum spec=new xSpectrum();
                                             for(int ii=0;ii<mzVals.size();ii++){
                                                spec.addElem(mzVals.elementAt(ii), intVals.elementAt(ii)    );
                                             }
                                             String protId="Spect. "+ Integer.toString(specCounter) +" rt=" +retTime;
                                             String pepSeq="Pep " + Integer.toString(specCounter);
                                             identInputData myInputDta= new identInputData(protId, pepSeq);
                                             xIdentData ident = new xIdentData(0,retTime,parIonMz,charge);
                                             myInputDta.addLcMsMsIdent(ident);
                                             myLoadData.addIdentificationData(myInputDta);
                                             specCounter++;
                                            }
                                            else{
                                                    //DO NOTHING or SET TO NOTHING ELEMENTS..

                                            }
                                            

                                        }

                                   }


                                }

                            }
                         }
                    }

             catch(Exception e){
                System.out.println("Problems while reading file " + rawDataFile);
                 System.out.println(e);
             }
            finally {
            //ensure the underlying stream is always closed
                scanner.close();
            }
          //  ret.addDataElem(myLoadData);

            
          System.out.println(myLoadData.getIdentificationDataSize() + " identifications added!");

        }


       
        return ret;
        }




      /**
     * Opens the dataFile xml file and loads mass shifts,the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     * @return ret an array of float mass shifts.
     */

    public void loadParams(String dataFile){
        //mass shifts.
        //the order is the following:


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

            NodeList itemList=  nodeLst.getChildNodes();
            for(i=0; i<itemList.getLength(); i++){
                Node item= itemList.item(i);
                if(item.getNodeType() == Node.ELEMENT_NODE){
                   if(item.getNodeName().equals("datafile")){
                    rawDataFiles.addElement(item.getTextContent());
                   // identificationFiles.addElement(item.getAttributes().item(0).getTextContent().toString());
                  //  System.out.println("Read files:" + item.getTextContent() + " " + item.getAttributes().item(0).getTextContent().toString());
                   }


                }
            }


        }
        catch(Exception e){
            System.out.println("Exception while reading " + dataFile+ "\n" + e);
            System.exit(1);
        }


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


    Vector<String> rawDataFiles = new Vector<String>();
   // Vector<String> identificationFiles = new Vector<String>();


    private final static String name = "LoadAllIds";
    private final static String version = "1.00";
    private final static String type = "IDENTDATA_LOAD_plugin";
    private final static String description = "Loads all spectra as identifications (with proteinId=Spectrum x and peptideId=Pep x).\n\tThis plugin loads all spectra as identifications associated to nonexisting peptides.\n\tUseful when you want to quantitate without knowing peptide identifications.";


    //public static xLoad ret;

}