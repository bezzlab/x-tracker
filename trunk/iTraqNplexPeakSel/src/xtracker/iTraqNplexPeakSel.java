package xtracker;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Vector;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

public class iTraqNplexPeakSel implements peakSelPlugin
{
    /**
     * Load reporter ions information and assigned Tandem MS Peaks to the label relative to every specific reporter ion.
     * It finally populates the xPeaks structure.
     * @param input the xLoad structure
     * @return xPeaks the peaks selected for quantitation.
     */
    public xPeaks start(xLoad input, String paramFile)
    {
        
         xPeaks ret = new xPeaks();
         System.out.println(getName()+": starting...");
        //First open the .xml file and retrieve mass shifts.
        loadParams(paramFile);
        //Loope through all input raw data files.
        int rawDataFilesNum=input.getDataSize();
        for(int i=0;i<rawDataFilesNum;i++){

            identInputData myMsMsVals;
            xLoadData myLoadData = input.getDataElemAt(i);
            //Let's get the file name of this dataset.
            String fileNM=myLoadData.getFileName();
            //Let's loop through all the identifications
            for(int j=0; j<myLoadData.getIdentificationDataSize();j++){
                //Loops through all lcMsMsIdentificationData
                myMsMsVals= myLoadData.getIdentInputDataElemAt(j);
                //Retrieving protein id and peptide sequence
                String protId=myMsMsVals.getProteinId();
                String pepSeq=myMsMsVals.getPeptideSeq();


                //Let's create the xCorrespondenceData element to be populated...
                xCorrespondenceData myXcorr= new xCorrespondenceData(protId,pepSeq);

                //Loops through all lcMsMs identifications
                for(int k=0;k<myMsMsVals.getLcMsMsIdentSize();k++){
                    
                    xIdentData myMsMsIdent=myMsMsVals.getLcMsMsElemAt(k);
                    //Let's get the parent Mz and retention time from the search engine's data.
                    float parMassDB=myMsMsIdent.getParentMass();
                    float retTime=myMsMsIdent.getRetTime();

                    //Let's get modifications
                    Vector<xModification> myModsVector= myMsMsIdent.getAllModifications();


                    //Let's get the spectrum associated to the identification. We will need two indexes
                    //The retention time index (used for the xLcMsMsData structure) and the parent ion index (relative to the xLcMsMsElem structure)
                    int elemRTInd=myLoadData.getLcMsMsIndexOfRt(retTime);

//                    for(int luca=0;luca<myLoadData.getLcMsMsDataSize();luca++){
//                        System.out.println(luca + " ret Val:" + myLoadData.getLcMsMsDataElemAt(luca).getRetTime());
//
//                    }

  //                  System.out.println("Ret time:" + retTime + " index of rt:" + elemRTInd);

                    xLcMsMsData myLcMsMsData=myLoadData.getLcMsMsDataElemAt(elemRTInd);
                    //Let's get the Retention time
                    float retTimeVal=myLcMsMsData.getRetTime();
                    int elemMZInd=myLcMsMsData.getLcMsMsIndexOfParIonMz(parMassDB);
                    //This is the sought element!
                    xLcMsMsElem myDtaElem=myLcMsMsData.getLcMsMsElemAt(elemMZInd);
                    //Let's get some info out like parentIonMz, charge and spectrum
                    int charge=myDtaElem.getCharge();
                    float parMz=myDtaElem.getParentIonMz();
                    xSpectrum mySpectrum=myDtaElem.getSpectrum();
                    //Loops through every possible reporter ion
                   
                    for(int h=0; h<reporterIons.size();h++){
                        float myMz=reporterIons.elementAt(h);
                        String myLabel=labels.elementAt(h);
   //                     System.out.println("Spectrum size:" + mySpectrum.getSize() + " looking for subspectrum: ["+(myMz-this.lowerMzTol) + " - " +(myMz+this.upperMzTol) +"]");
                        float mzz[][]=mySpectrum.getSpectrum();
     //                   for(int kkk=0;kkk<mzz.length;kkk++){
     //                       if(mzz[kkk][0]>=myMz-this.lowerMzTol && mzz[kkk][0]<=myMz+this.upperMzTol){
     //                           System.out.println("Mz:" + mzz[kkk][0] + " int:" + mzz[kkk][1]);
     //                       }
     //                   }

                        float[][] mySubSpec=mySpectrum.getSubspectrumBetween(myMz-this.lowerMzTol, myMz+this.upperMzTol);
                        int subSpecSize=mySubSpec.length;
       //                 System.out.println("Subspectrum size:" +subSpecSize);
                        for(int subElCnt=0;subElCnt<subSpecSize;subElCnt++){
                            //Let's create a new xLcMsMsCorr elem

                            //
                            //
                            // WHAT ABOUT FILTERING ON INTENSITY?!?!
                            //
                            //
                            //
         //                   System.out.println(subElCnt + "," + mySubSpec[subElCnt][0]+","+mySubSpec[subElCnt][1]);
                            xLcMsMsCorr corrElem= new xLcMsMsCorr(mySubSpec[subElCnt][0],mySubSpec[subElCnt][1],myLabel,retTimeVal,charge, parMz);
                            //Let's add modifications to the correspondence element.
                            for(int modCnt=0; modCnt<myModsVector.size(); modCnt++){
                                corrElem.addModification(myModsVector.elementAt(modCnt).getName(), myModsVector.elementAt(modCnt).getPosition());
                            
                            }

                            //and add it to the data structure.
                            myXcorr.addLcMsMsCorr(corrElem);

                        }



                    }
                }
                
                //Adds the correspondence data to the return structure
                ret.addPeptideCorrespondence(fileNM, myXcorr);

            }
        }

        return ret;
    }

    /**
     * Opens the dataFile xml file and loads reporter ions masses, labels as well as tolerances.
     * @param dataFile the xml file
     */
    public void loadParams(String dataFile){
        
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try{
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
               // create a SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            doc.getDocumentElement().normalize();

            Node nodeLst = doc.getElementsByTagName("iTraq").item(0);

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

 
            //Let's get tolerances first.
            nodeLst = doc.getElementsByTagName("settings").item(0);


            NodeList itemList=  nodeLst.getChildNodes();
            for(int i=0; i<itemList.getLength(); i++){
                Node item= itemList.item(i);
                if(item.getNodeType() == Node.ELEMENT_NODE){
                   if(item.getNodeName().equals("mzLowerThreshold")){
                        lowerMzTol=Float.valueOf(item.getTextContent());
           //             System.out.println("Lower tol:" +lowerMzTol);

                   }
                   else if(item.getNodeName().equals("mzUpperThreshold")){
                         upperMzTol=Float.valueOf(item.getTextContent());
             //            System.out.println("Upper tol:" +upperMzTol);
                   }
                   else if(item.getNodeName().equals("intThreshold")){
                       intThreshold=Float.valueOf(item.getTextContent());
               //        System.out.println("Int thresh:" +intThreshold);
                   }

                }
            }
            //Let's load reporter ions (and corresponding labels)
          
            nodeLst = doc.getElementsByTagName("reporterIons").item(0);
            itemList=  nodeLst.getChildNodes();
            for(int i=0; i<itemList.getLength(); i++){
                
                Node item= itemList.item(i);
                if(item.getNodeType() == Node.ELEMENT_NODE){
                    //System.out.println(item.getTextContent());
                   if(item.getNodeName().equals("reporter")){
                        String mzVal=item.getAttributes().getNamedItem("mz").toString();
                        String label=item.getAttributes().getNamedItem("label").toString();
                        //Let's remove the mz=" and the final "
                        mzVal=mzVal.substring(4, mzVal.length()-1);
                        //Let's remove the label=" and the final "
                        label=label.substring(7,label.length()-1);
                 //       System.out.println("Reporter: " +mzVal);
                        reporterIons.addElement(Float.valueOf(mzVal));
                        labels.addElement(label);
                //        System.out.println(" - Label: " +label);
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
        



   




    private Vector<Float> reporterIons = new Vector<Float>();
    private Vector<String> labels = new Vector<String>();
    private float lowerMzTol=0f;
    private float upperMzTol=0f;
    private float intThreshold=0f;


    private final static String name = "iTRAQ-N-PlexPeakSel";
    private final static String version = "1.01";
    private final static String type = "PEAKSEL_plugin";
    private final static String description = "Implements peak selection for iTRAQ.\n\tIt is a general plugin that can load a generic number N of reporter ions\n\tspecified in input with their M/Z values and labels.";
}