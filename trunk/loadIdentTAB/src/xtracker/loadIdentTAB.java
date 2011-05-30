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

public class loadIdentTAB implements identData_loadPlugin
{



    public xLoad start(xLoad inputData, String paramFile)
    {


     xLoad ret = inputData;
     System.out.println(getName()+": starting...");
     String inputDataFile="";
     //let's load files to process
     this.loadParams(paramFile);

     for(int fileCnt=0; fileCnt<identificationFiles.size();fileCnt++){

        inputDataFile=identificationFiles.elementAt(fileCnt);


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
        int line_count=0;
        while (dis.available() != 0) {

                    //The format of each line of the file has to be the following:
                    //peptide_seq	pep_id	ret_time	prec_mass	score
                   identInputData identTmp;
                   tmpLine=dis.readLine();
                 //  System.out.println("(" + line_count + ") " + tmpLine);
                   line_count++;
                   //Let's skip the comment line

                   if( tmpLine.charAt(0) != '#'){
                        String [] tempVals;
                        tempVals = tmpLine.split("\t");
                       // int retValIndex=-1;
                      //  int spectrumIndex=-1;
                      float retTime=0f;
                      float parentMass=0f;
                      int charge=-1;
                      retTime=Double.valueOf(tempVals[2]).floatValue();
                      parentMass=Double.valueOf(tempVals[3]).floatValue();
                      charge= Integer.valueOf(tempVals[5]).intValue();

                     //    retValIndex=myinputData.getLcMsMsIndexOfRt(Double.valueOf(tempVals[2]).floatValue());
                     //    if(retValIndex==-1){
                    //        System.out.println("ERROR (loadIdentTAB plugin): trying to look for a nonexisting retention value!");
                    //        System.exit(1);
                    //     }

                     //    spectrumIndex= myinputData.getLcMsMsDataElemAt(retValIndex).getLcMsMsIndexOfParIonMz(Double.valueOf(tempVals[3]).floatValue());
                      //   if(spectrumIndex==-1){
                       //     System.out.println("ERROR (loadIdentTAB plugin): trying to look for a nonexisting parent ion mz!");
                        //    System.exit(1);
                        // }
                         Vector<identInputData> myidentsOfPeptide;

                   myidentsOfPeptide= myinputData.getAllIdentOfPeptideSeq(tempVals[0]);

                 //  System.out.println("Trying to add peptide " + "pep_"+ tempVals[1] + "." + " Size of elem already in: " + myidentsOfPeptide.size());

                   if(myidentsOfPeptide.size()<1){

                        identTmp = new identInputData("prot_" + tempVals[1], tempVals[0]);

//                        xIdentData myD = new xIdentData(Double.valueOf(tempVals[4]).floatValue(), retValIndex,spectrumIndex);
                        xIdentData myD = new xIdentData(Double.valueOf(tempVals[4]).floatValue(), retTime,parentMass,charge);
                        identTmp.addLcMsMsIdent(myD);
                        myinputData.addIdentificationData(identTmp);
                 //       System.out.println("Added: pep_" + tempVals[1] +"," + tempVals[0] +", -1");
                   }
                   else{

 //                      spectrumIndex=0;
 //                      xIdentData myD = new xIdentData(-1, retValIndex,spectrumIndex);
                       parentMass=0;
                       xIdentData myD = new xIdentData(-1, retTime, parentMass, charge);
                       myidentsOfPeptide.elementAt(0).addLcMsMsIdent(myD);


                   }
            }
        }

        } catch (Exception e){
            System.out.println("Problems in LoadIdentTab plugin.");
            System.out.println(e);}

//        ret.Data.add(0, myinputData);
        }



        return ret;
        }




      /**
     * Opens the dataFile xml file and loads mass shifts,the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     */

    public void loadParams(String dataFile){
        //mass shifts.
        //the order is the following:




        int i=0;
        int index=2;
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
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
                    identificationFiles.addElement(item.getAttributes().item(0).getTextContent().toString());
                   // System.out.println("Read files:" + item.getTextContent() + " " + item.getAttributes().item(0).getTextContent().toString());
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


    Vector<String> identificationFiles = new Vector<String>();

    private final static String name = "Load MZXML files";
    private final static String version = "1.00";
    private final static String type = "IDENTDATA_LOAD_plugin";
    private final static String description = "This plugin loads peptide identifications from a text, TAB separated, file.";


    //public static xLoad ret;

}