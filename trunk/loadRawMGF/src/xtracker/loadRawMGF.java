//Every plugin has to be in the xtracker package.
package xtracker;



//Some useful imports
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.regex.*;

import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;


public class loadRawMGF implements rawData_loadPlugin
{


    /**
     * Loads raw data from MGF files specified in the paramFile
     * @param paramFile the xml file specifying parameters of the plugin
     * @return xLoad the xLoad structure populated with MS/MS raw data.
     */
    public xLoad start(String paramFile)
    {


        xLoad ret = new xLoad();
        System.out.println(getName()+": starting...");
        String rawDataFile="";
        Pattern myPattern;
        
        //let's load files to process
        this.loadParams(paramFile);



        //Let's load all raw data files specified in input
        for(int fileCnt=0; fileCnt<rawDataFiles.size();fileCnt++){

            //The raw data file.
            rawDataFile=rawDataFiles.elementAt(fileCnt);

            xLoadData myLoadData=new xLoadData(rawDataFile);
            

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
                    float parIonMz=-1f;
                    float retTime=-1f;
                    int charge=-1;
                    float retTimeLoaded=-1f;

                    Vector<Float> mzVals= new Vector<Float>();
                    Vector<Float> intVals= new Vector<Float>();
                    //first use a Scanner to get each line
                    while ( scanner.hasNextLine() ){

                         String line=scanner.nextLine();
                         myPattern = Pattern.compile("^PEPMASS");


                         Matcher m= myPattern.matcher(line);

                         if(m.find()){
                    
                               line=line.substring(8);
                               Float val=0f;
                               String [] tmp= new String[2];
                               //If mz/intensity skip intensity
                               if(line.contains(" ")){
                                                tmp=line.split(" ");
                                                line=tmp[0];
                               }

                               try{
                               parIonMz=Float.valueOf(line).floatValue();
                               }
                               catch(NumberFormatException e){
                                    System.out.println("Error (LoadRawMGF): The value " + line + " is not a valid parent Ion M/Z value");
                                    System.exit(1);
                               }

                         }
                         else{
                            myPattern = Pattern.compile("^CHARGE");
                            m= myPattern.matcher(line);
                            if(m.find()){
                                line=line.substring(7,line.length()-1);
                                
                                try{
                                charge=Integer.valueOf(line).intValue();
                                }
                                catch(NumberFormatException e) {
                                    System.out.println("Error (LoadRawMGF): The value " + line + " is not a valid charge state");
                                    System.exit(1);
                                }

                             
                            }
                            else{
                                myPattern = Pattern.compile("^RTINSECONDS");
                                m= myPattern.matcher(line);
                                if(m.find()){
                                    line=line.substring(12,line.length());

                                try{
                                    retTime=Float.valueOf(line).floatValue();
                                    retTimeLoaded=retTime;
                                }
                                catch(NumberFormatException e) {
                                    System.out.println("Error (LoadRawMGF): The value " + line + " is not a valid retention time in seconds value");
                                    System.out.println(e);
                                    System.exit(1);
                                }


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
                            //            System.out.println("El time:" +line);
                                        try{
                                            retTime=Float.valueOf(line).floatValue();
                                            retTimeLoaded=retTime;
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
                                            retTimeLoaded=retTime;
                                            }
                                            catch(Exception e){
                                            //Just nop. Value is set to 0 anyway.
                                            }
                                        }
                                          else{
                                            int rtTimeIndSN=line.indexOf("Scan Number:");
                                            if(rtTimeIndSN>-1){
                                        //OK Scan Number found!
                                        line=line.substring(rtTimeIndSN+12);
                                     
                                        line=line.trim();

                             
                                        try{
                                            retTime=Float.valueOf(line).floatValue();
                                            retTimeLoaded=retTime;
                                        }
                                        catch(Exception e){
                                            //Just nop. Value is set to 0 anyway.
                                        }

                                          }
                                           // else{
                                             //   System.out.println("RET. TIME:" +retTime);
                                               

                                            //}
                                       }


                                       }
                                   }
                                   else{
                                        myPattern = Pattern.compile("^[0-9]+");
                                        m= myPattern.matcher(line);
                                        if(m.find()){
                                            
                                            String [] tmp= new String[2];
                                            String separator="";
                                            if(line.contains("\t")){
                                                separator="\t";
                                            }
                                            else{
                                                separator=" ";
                                            }
                                            tmp=line.split(separator);
                                            mzVals.add(Float.valueOf(tmp[0]));
                                            intVals.add(Float.valueOf(tmp[1]));
 

                                        }
                                        else{
                                            myPattern = Pattern.compile("^END IONS");
                                            m= myPattern.matcher(line);
                                            if(m.find()){
                                             //Let's reset retTimeLoaded

                                              if(retTimeLoaded==-1){

                                                    System.out.println("WARNING (LoadRawMGF): missing retention time information (zero assumed)");
                                                    retTime=0f;

                                                }
                                               retTimeLoaded=-1f;
                                             //Lets add values!
                                             xSpectrum spec=new xSpectrum();
                                             for(int ii=0;ii<mzVals.size();ii++){
                                                spec.addElem(mzVals.elementAt(ii), intVals.elementAt(ii));
                                       
                                             }
                                             //Some error checking before saving values
                                             if(charge==-1){
                                                System.out.println("Error (LoadRawMGF): Charge value missing!");
                                                System.exit(1);
                                             }
                                             if(retTime==-1f){
                                                System.out.println("Error (LoadRawMGF): Retention time value missing!");
                                                System.exit(1);

                                             }
                                             if(parIonMz==-1f){
                                                System.out.println("Error (LoadRawMGF): Parent Ion M/Z value missing!");
                                                System.exit(1);

                                             }
                                             myLoadData.addLcMsMsData(retTime, parIonMz, charge, spec);
                                            }
                                            else{
                                                    //DO NOTHING or SET TO NOTHING ELEMENTS..
                                                    mzVals.removeAllElements();
                                                    intVals.removeAllElements();
                                            }

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
            ret.addDataElem(myLoadData);



            System.out.println("Done!");
            System.out.println("Datasize: " +ret.getDataSize());
            System.out.println(" Filename: " +ret.getDataElemAt(fileCnt).getFileName());
            System.out.println(" Lc/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsDataSize());
            System.out.println(" Ms/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsMsDataSize());
          
        }



        return ret;
        }




      /**
     * Opens the dataFile xml file and loads raw data files.
     * @param dataFile the parameter files
     */

    public void loadParams(String dataFile){
        //mass shifts.
        //the order is the following:


        int i=0;

        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
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
                   if(item.getNodeName().equals("datafile")){
                    rawDataFiles.addElement(item.getTextContent());
                    System.out.println("Read files:" + item.getTextContent() + " " + item.getAttributes().item(0).getTextContent().toString());
                   }


                }
            }


        }
        catch(Exception e){
            System.out.println("Exception while reading " + dataFile+ "\n" + e);
            System.exit(1);
        }


    }

  

       /**
     * Gets the plugin name.
     * @return pluginName
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the plugin version.
     * @return pluginVersion
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Gets the plugin type.
     * @return pluginType
     */
    public String getType()
    {

        return type;
    }

    /**
     * Gets the plugin description.
     * @return pluginDescription
     */
    public String getDescription()
    {

        return description;
    }

    //The vector of rawDataFiles
    Vector<String> rawDataFiles = new Vector<String>();
    
    //The plugin name
    private final static String name = "LoadMGFdata";
    //The plugin version
    private final static String version = "1.00";
    //The plugin type(do not modify the string otherwise xTracker won't recognize the plugin!)
    private final static String type = "RAWDATA_LOAD_plugin";
    //The plugin description
    private final static String description = "\t\tThis plugin loads MS/MS data from .MGF files.\n\tRetention time has to be specified in the \"TITLE\"\n\tsection of the file as \"Elution: XXX\" or as \"rt=XXX\".\n\tScan numbers are also accepted (though they do not suit some labeling techniques) \"Scan Number:XXX\".";



}