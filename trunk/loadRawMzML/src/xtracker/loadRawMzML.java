/**
 * Class that corresponds to the plugin to load raw data in X-Tracker in mzML format.
 * Uses the other classes of the package to parse the mzML file and fill the xLoad data structure.
 */

package xtracker;

import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  
 * @author laurie Tonon for x-Tracker
 */
public class loadRawMzML implements rawData_loadPlugin{

    	
     /**
      * The start method. 
      * @param paramFile a string (that might be empty) containing the file name of parameters if any.	
      * @return a valid xLoad structure filled with raw data information only.
	  */	
    public xLoad start(String paramFile)
    {
                // xLoad data structure to fill with raw data
		xLoad ret = new xLoad();

		System.out.println(getName()+": starting...");

                // name of the mzML file to read
                String rawDataFile="";
                
                // load the parameters from the xml file
                this.loadParams(paramFile);

                // read each mzML files one after the other
                for(int fileCnt=0; fileCnt<rawDataFiles.size();fileCnt++){
                    
                    Spectrum mySpectrum=null;

                    rawDataFile=rawDataFiles.elementAt(fileCnt);

                    // check the size of the file and decide which parser to use
                    int parserType=ToolmzML.choiceParser(rawDataFile);
                    
                    if(parserType == 0){

                        // ********************************************
                        // USE DOM PARSER
                        // ********************************************
                        
                        System.out.println("Parser used: dom");
                        mzMLDomParser myParser=new mzMLDomParser(rawDataFile);
                        
                        System.out.println("OK parsing " + myParser.getSpectrumCounts().size() + " scans.");
                        
                        xLoadData inputData = new xLoadData(rawDataFile);
                        
                        xSpectrum msSpectrum; // spectrum to add to xLoad
                        int i=0; //the spectrum counter.
                        int totSpectra=myParser.getSpectrumCounts().size(); // number of spectra to parse

                        //===============================================
                        // parse each spectrum
                        //===============================================
                        
                        for(i=0;i<totSpectra;i++){

                              // call the parser and get the information of this spectrum
                              mySpectrum=myParser.rap(i);
                              
                              if(mySpectrum.getMsLevel() == 1){ // MS level
                                  float retVal=Double.valueOf(mySpectrum.getDoubleRetentionTime()).floatValue();
                                  int numPeaks=mySpectrum.getPeaksCount();
                                  float mzVal;
                                  float intVal;
                                  float peaksVect[][]=mySpectrum.getMassIntensityList();
                                  
                                   msSpectrum=new xSpectrum();

                                   // add all the mz and intentity values to the spectrum
                                   for(int j=0; j<numPeaks;j++){
                                        mzVal=peaksVect[j][0];
                                        intVal=peaksVect[j][1];

                                        //Only values with intensity>0 are stored
                                        if(intVal>0){
                                                msSpectrum.addElem(mzVal, intVal);
                                        }
                                    }
                                  // add the spectrum with its retention time to xLoad
                                 inputData.addLcMsData(retVal,msSpectrum);
                              }
                              else if(mySpectrum.getMsLevel() ==2){ // Ms Ms level
                                  
                                  float retTime=Double.valueOf(mySpectrum.getDoubleRetentionTime()).floatValue();
                                  float parIonMz=mySpectrum.getPrecursorMz();
                                  int chargeValue=mySpectrum.getPrecursorCharge();
                                  
                                  xSpectrum msMsSpectrum=new xSpectrum();
                                  float peaksVect[][]=mySpectrum.getMassIntensityList();
                                  int numPeaks=mySpectrum.getPeaksCount();
                                  
                                  float mzVal;
                                  float intVal;

                                  // add all the mz and intentity values to the spectrum
                                  for(int j=0; j<numPeaks;j++){
                                      
                                      mzVal=peaksVect[j][0];
                                     intVal=peaksVect[j][1];
                                     //Only values with intensity>0 are stored
                                     if(intVal>0){

                                        msMsSpectrum.addElem(mzVal, intVal);

                                    }
                          
                                  }
                                  // add the spectrum with its retention time and parent ion info to xLoad
                                  inputData.addLcMsMsData(retTime, parIonMz, chargeValue, msMsSpectrum);
                                  
                              }
                         }

                        // add the complete dataset to xLoad
                        ret.addDataElem(inputData);

                        System.out.println("Done!");
                        System.out.println("Datasize: " +ret.getDataSize());
                        System.out.println(" Filename: " +ret.getDataElemAt(fileCnt).getFileName());
                        System.out.println(" Lc/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsDataSize());
                        System.out.println(" Ms/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsMsDataSize());
                        
                    }
                    
                    else if(parserType == 1){
                        // ********************************************
                        // USE SAX PARSER
                        // ********************************************
                        
                        System.out.println("Parser used: sax");
                        MzMLParser myParser=new MzMLParser(rawDataFile);
                        
                        System.out.println("OK parsing " + myParser.getSpectrumCount() + " scans.");
                        
                        xLoadData inputData = new xLoadData(rawDataFile);
                        
               
                        xSpectrum msSpectrum; // spectrum to add to xLoad
                        int i=0; //the spectrum counter.
                        int totSpectra=myParser.getSpectrumCount(); // number of spectra to parse
                        float mzVal;
                        float intVal;

                        //===============================================
                        // parse each spectrum
                        //===============================================
                        
                        for(i=1;i<=totSpectra;i++){
                            
                              mySpectrum=myParser.rap(i);
                              
                              if(mySpectrum.getMsLevel() == 1){ //Ms level
                                  float retVal=Double.valueOf(mySpectrum.getDoubleRetentionTime()).floatValue();
                                  int numPeaks=mySpectrum.getPeaksCount();
                                  mzVal=0;
                                  intVal=0;
                                  float peaksVect[][]=mySpectrum.getMassIntensityList();
                                  
                                   msSpectrum=new xSpectrum();

                                   // add all the mz and intentity values to the spectrum
                                   for(int j=0; j<numPeaks;j++){
                                        mzVal=peaksVect[j][0];
                                        intVal=peaksVect[j][1];

                                        //Only values with intensity>0 are stored
                                        if(intVal>0){
                                                msSpectrum.addElem(mzVal, intVal);
                                        }
                                    }
                                   // add the spectrum with its retention time to xLoad
                                 inputData.addLcMsData(retVal,msSpectrum);
                                 
                              }
                              else if(mySpectrum.getMsLevel() ==2){ // MsMs level
                                  
                                  float retTime=Double.valueOf(mySpectrum.getDoubleRetentionTime()).floatValue();
                                  float parIonMz=mySpectrum.getPrecursorMz();
                                  int chargeValue=mySpectrum.getPrecursorCharge();
                                  
                                  xSpectrum msMsSpectrum=new xSpectrum();
                                  float peaksVect[][]=mySpectrum.getMassIntensityList();
                                  int numPeaks=mySpectrum.getPeaksCount();
                                  
                                   mzVal=0;
                                   intVal=0;

                                   // add all the mz and intentity values to the spectrum
                                  for(int j=0; j<numPeaks;j++){
                                      
                                      mzVal=peaksVect[j][0];
                                     intVal=peaksVect[j][1];
                                     //Only values with intensity>0 are stored
                                     if(intVal>0){

                                        msMsSpectrum.addElem(mzVal, intVal);

                                    }
                          
                                  }
                                  // add the spectrum with its retention time and parent ion info to xLoad
                                  inputData.addLcMsMsData(retTime, parIonMz, chargeValue, msMsSpectrum);
                                  
                              }
                              
                         }
                        // add the complete dataset to xLoad
                        ret.addDataElem(inputData);

                        System.out.println("Done!");
                        System.out.println("Datasize: " +ret.getDataSize());
                        System.out.println(" Filename: " +ret.getDataElemAt(fileCnt).getFileName());
                        System.out.println(" Lc/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsDataSize());
                        System.out.println(" Ms/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsMsDataSize());
                        
                        
                        
                    }
                    
                    else{
                        
                        System.err.println("Error: an error occured in the selection of the parser to read the file");
                        System.exit(1);
                    }
                }
                

		return ret;
        
    }

   


     /**
      * Method to retrieve the name of the plugin.
      * @return A string with the plugin name.	
      */
    public String getName()
    {
        return name;
    }

     /**
      * Method to retrieve the version of the plugin.
      * @return A string with the plugin version.	
      */
    public String getVersion()
    {
        return version;
    }

     /**
      * Method to retrieve the type of the plugin.
      * @return A string with the plugin type.	
      */	
    public String getType()
    {

        return type;
    }

     /**
      * Method to retrieve the description of the plugin.
      * @return A string with the plugin description.	
      */
        public String getDescription()
    {

        return description;
    }

     /**
      * Method that loads the parameters used in the plugin from an xml file
      * @param dataFile The complete path to the xml file to read in
      */
     public void loadParams(String dataFile){
        

        int i=0;

        // open the xml file
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        //======================================================

        try{
            // Parse the file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            //================================================

            Node nodeLst = doc.getElementsByTagName("param").item(0);

            NodeList itemList=  nodeLst.getChildNodes();

            // read all the tags inside param
            for(i=0; i<itemList.getLength(); i++){

                Node item= itemList.item(i);

                if(item.getNodeType() == Node.ELEMENT_NODE){

                   if(item.getNodeName().equals("datafile")){ // find the tags with the mzML files

                    rawDataFiles.addElement(item.getTextContent()); // add each MzML file to read to the list
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
      * list of mzML file to read in
      */
    Vector<String> rawDataFiles = new Vector<String>();
    
        
     /**
      *  The name of the plugin.
      */ 
    private final static String name = "loadRawMzML";

     /**
      * The version of the plugin.
      */ 
    private final static String version = "1.0";
     
     /**
      * The plugin type. For an RAW DATA LOAD plugin it must be RAWDATA_LOAD_plugin.
      */ 	
    private final static String type = "RAWDATA_LOAD_plugin";

     /**
      * The description of the plugin.
      */    
    private final static String description = "This plugin loads raw data (MS and MS/MS) from .mzML files. Use a parser adapted to the size of the xml-based file to import the data.";
}

    

