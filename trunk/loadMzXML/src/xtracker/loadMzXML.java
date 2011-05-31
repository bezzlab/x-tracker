package xtracker;

import java.io.*;





import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.util.*;

public class loadMzXML implements loadPlugin
{



    public xLoad start(String paramFile)
    {


        xLoad ret = new xLoad();
        System.out.println(getName()+": starting...");
        String rawDataFile="";
        String inputDataFile="";
        //let's load files to process
        this.loadParams(paramFile);

//        System.out.println("Please type the raw data file's path in...");
//        BufferedReader keyb = new BufferedReader(new InputStreamReader(System.in));
//        try{
//            rawDataFile = keyb.readLine();
//        }
//        catch(Exception e){
//            System.out.println("Error: Input problems!");
//            System.exit(1);
//        }

        //Let's read the raw data (note example works for .mgf only).




        for(int fileCnt=0; fileCnt<rawDataFiles.size();fileCnt++){

        Scan myscan=null;
        rawDataFile=rawDataFiles.elementAt(fileCnt);
        inputDataFile=identificationFiles.elementAt(fileCnt);
        MSXMLParser myParser = new MSXMLParser(rawDataFile);

        System.out.println("OK parsing " + myParser.getScanCount() + " scans.");

        xLoadData inputData = new xLoadData(rawDataFile);

        xSpectrum msSpectrum;
        int i=0; //the scan counter.
        int totScans=myParser.getScanCount();
        for(i=1;i<=totScans;i++){
     //       System.out.println("Inserting scan " + i + "/" + totScans);
                myscan=myParser.rap(i);
                if(myscan.getMsLevel() == 1){
                    //OK it is a Ms scan: populate xLcMsData
                    float retVal=Double.valueOf(myscan.getDoubleRetentionTime()).floatValue();
                    int numPeaks=myscan.getPeaksCount();
                    float mzVal;
                    float intVal;
                    float peaksVect[][]=myscan.getMassIntensityList();

                    msSpectrum=new xSpectrum();
                    for(int j=0; j<numPeaks;j++){
                        mzVal=peaksVect[0][j];
                        intVal=peaksVect[1][j];
                        //Only values with intensity>0 are stored
                        if(intVal>0){
                            msSpectrum.addElem(mzVal, intVal);
                           // msSpectraTmp.addElem(retVal, mzVal, intVal);
                          //  System.out.println("Values inserted: (" + retVal + "," + mzVal + "," + intVal + ")");

                        }
                    }

                    inputData.addLcMsData(retVal,msSpectrum);

                  //Following lines to debug ms data
                  //   if(i==3){
                  //                     System.out.println(myscan);
                   // }


                }
                else{
                    if(myscan.getMsLevel() == 2){
                       //OK it is a Ms/Ms scan: populate xLcMsMsData
                      float retTime=Double.valueOf(myscan.getDoubleRetentionTime()).floatValue();
                      float parIonMz=myscan.getPrecursorMz();
                      int chargeValue=myscan.getPrecursorCharge();

                      xSpectrum msMsSpectrum=new xSpectrum();
                      float peaksVect[][]=myscan.getMassIntensityList();
                      int numPeaks=myscan.getPeaksCount();
                      float mzVal;
                      float intVal;
                      for(int j=0; j<numPeaks;j++){
                        mzVal=peaksVect[0][j];
                        intVal=peaksVect[1][j];
                        //Only values with intensity>0 are stored
                        if(intVal>0){

                        msMsSpectrum.addElem(mzVal, intVal);


                        }
                    }
                   inputData.addMsMsData(retTime, parIonMz, chargeValue, msMsSpectrum);
                   //System.out.println(myscan);
                  }


              //      System.out.println(myscan);
        }
 //               System.out.println(myscan);
        }



        ret.addDataElem(inputData);

        System.out.println("Done!");
        System.out.println("Datasize: " +ret.getDataSize());
        System.out.println(" Filename: " +ret.getDataElemAt(fileCnt).getFileName());
        System.out.println(" Lc/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsDataSize());
        System.out.println(" Ms/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsMsDataSize());

//        msMsSpectraTmp=ret.getDataElemAt(0).getMsMsDataElemAt(1);
//  msSpectraTmp=ret.getDataElemAt(0).getLcMsDataElemAt(0);
  //      System.out.println("Ret time: "+ msMsSpectraTmp.getRetentionTime() + " Parent ion mz: " + msMsSpectraTmp.getParentIonMz()+ " Charge: " + msMsSpectraTmp.getCharge());
 /* for(i=0; i<msMsSpectraTmp.getSpectrumSize();i++){
            float vals[];
            vals=msMsSpectraTmp.getPeakAt(i);
            System.out.println("(" + vals[0] + ","+ vals[1]+ ")");

        }

*/

/*
  for(i=0; i<ret.getDataElemAt(0).getLcMsDataSize();i++){
        xLcMsData myLCMS=ret.getDataElemAt(0).getLcMsDataElemAt(i);
        float valRT;
        valRT=myLCMS.getRetTime();
        System.out.println("Ret time: "+ valRT);
        xSpectrum sp= myLCMS.getSpectrum();
        float[][] spectrum=sp.getSpectrum();
        for(int j=0;j<sp.getSize();j++){
            System.out.println("MZ:" + spectrum[j][0] + " I:" + spectrum[j][1]);
        }
  }

   System.exit(1);
  */



        //Let's load some identification data
//        System.out.println("Please type the identification data file's path in...");
//        String inputDataFile="";
//        try{
//            inputDataFile = keyb.readLine();
//        }
//        catch(Exception e){
//            System.out.println("Error: Input problems!");
//            System.exit(1);
//        }

        //Let's read the identification data.
        File file;

        file = new File(inputDataFile);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;

        try {
            fis = new FileInputStream(file);
          // Here BufferedInputStream is added for fast reading.
          bis = new BufferedInputStream(fis);
          dis = new DataInputStream(bis);


            //We are considering just a file at the moment
        xLoadData myinputData=ret.getDataElemAt(fileCnt);
        String tmpLine="";
//        xIdentData identTmp;
        int line_count=0;
        while (dis.available() != 0) {

                    //The format of each line of the file has to be the following:
                    //peptide_seq	pep_id	ret_time	prec_mass	score
                   identInputData identTmp;
                   tmpLine=dis.readLine();
//                   System.out.println("(" + line_count + ") " + tmpLine);
                   line_count++;
                   //Let's skip the comment line

                   if( tmpLine.charAt(0) != '#'){
                        String [] tempVals;
                        tempVals = tmpLine.split("\t");
                        int retValIndex=-1;
                        int spectrumIndex=-1;


                         retValIndex=myinputData.getLcMsMsIndexOfRt(Double.valueOf(tempVals[2]).floatValue());
                         if(retValIndex==-1){
                            System.out.println("ERROR (loadMzXML plugin): trying to look for a nonexisting retention value!");
                            System.exit(1);
                         }

                         spectrumIndex= myinputData.getLcMsMsDataElemAt(retValIndex).getLcMsMsIndexOfParIonMz(Double.valueOf(tempVals[3]).floatValue());
                         if(spectrumIndex==-1){
                            System.out.println("ERROR (loadMzXML plugin): trying to look for a nonexisting parent ion mz!");
                            System.exit(1);
                         }
                         Vector<identInputData> myidentsOfPeptide;

                   myidentsOfPeptide= myinputData.getAllIdentOfPeptideSeq(tempVals[0]);

                 //  System.out.println("Trying to add peptide " + "pep_"+ tempVals[1] + "." + " Size of elem already in: " + myidentsOfPeptide.size());

                   if(myidentsOfPeptide.size()<1){

                        identTmp = new identInputData("prot_" + tempVals[1], tempVals[0]);

                        xIdentData myD = new xIdentData(Double.valueOf(tempVals[4]).floatValue(), retValIndex,spectrumIndex);
                        identTmp.addMsMsIdent(myD);
                        myinputData.addIdentificationData(identTmp);
                 //       System.out.println("Added: pep_" + tempVals[1] +"," + tempVals[0] +", -1");
                   }
                   else{

                       spectrumIndex=0;
                       xIdentData myD = new xIdentData(-1, retValIndex,spectrumIndex);
                       myidentsOfPeptide.elementAt(0).addMsMsIdent(myD);


                   }
            }
        }

        } catch (Exception e){
            System.out.println("Problems in LoadMzXML");
            System.out.println(e);}

//        ret.Data.add(0, myinputData);
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
        int index=2;
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try{
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node nodeLst = doc.getElementsByTagName("param").item(0);

            NodeList itemList=  nodeLst.getChildNodes();
            for(i=0; i<itemList.getLength(); i++){
                Node item= itemList.item(i);
                if(item.getNodeType() == Node.ELEMENT_NODE){
                   if(item.getNodeName().equals("datafile")){
                    rawDataFiles.addElement(item.getTextContent());
                    identificationFiles.addElement(item.getAttributes().item(0).getTextContent().toString());
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

    Vector<String> rawDataFiles = new Vector<String>();
    Vector<String> identificationFiles = new Vector<String>();

    private final static String name = "Load MZXML files";
    private final static String version = "1.00";
    private final static String type = "LOAD_plugin";
    private final static String description = "This plugin loads data (both MS and MS/MS) from .mzXML files thanks to preoteome center jrap library.\n\tIt also loads peptide identifications from a text file.";


    //public static xLoad ret;

}