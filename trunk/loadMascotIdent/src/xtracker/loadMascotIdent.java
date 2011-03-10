package xtracker;

import java.io.File;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

//import java.util.regex.Pattern;
//import java.util.regex.Matcher;



public class loadMascotIdent implements identData_loadPlugin {

    public xLoad start(xLoad inputData, String paramFile) {

       
        xLoad ret = inputData;
        System.out.println(getName() + ": starting...");
        String inputDataFile = "";
        //let's load files to process
        this.loadParams(paramFile);



/*        for(int i=0;i<fixedMods.size();i++){

            System.out.println(fixedMods.elementA+ t(i) + " " + fixedModShifts.elementAt(i).toString());
        
        }
*/

                this.loadMascotMods(identificationFiles.elementAt(0));
                        System.out.println("Mods loaded!");

          for(int i=0;i<mascotVarMods.size();i++){

            System.out.println(mascotVarMods.elementAt(i) + " " + mascotVarModShifts.elementAt(i).toString());
        
        }
                System.out.println("Fixed mods:");

                for(int i=0;i< fixModResidues.size();i++){

                    System.out.println(fixModResidues.elementAt(i) + ") " + mascotFixedMods.elementAt(i) + " " + mascotFixedModShifts.elementAt(i).toString());

        }
//        System.exit(1);

        for (int fileCnt = 0; fileCnt < identificationFiles.size(); fileCnt++) {

            inputDataFile = identificationFiles.elementAt(fileCnt);


            //This void method parses the Mascot XML file inputDataFile and adds identifications
            addIdentifications(inputDataFile, ret, fileCnt);

        }


          //      System.out.println("PRINTING MODIFICATIONS");
/*        for (int fileCnt = 0; fileCnt < identificationFiles.size(); fileCnt++) {
                for(int j=0;j<inputData.getDataElemAt(fileCnt).getIdentificationDataSize();j++){
                   for(int k=0; k<inputData.getDataElemAt(fileCnt).getIdentInputDataElemAt(j).getMsMsIdentSize();k++){
                   xIdentData myMsMsIdent = inputData.getDataElemAt(fileCnt).getIdentInputDataElemAt(j).getMsMsElemAt(k);
                   System.out.println("Peptide:" + inputData.getDataElemAt(fileCnt).getIdentInputDataElemAt(j).peptideSeq + " total shift:" +myMsMsIdent.getTotalModificationMassShift());
                   for(int kk=0;kk<myMsMsIdent.getModificationSize();kk++){
                    xModification myMod=myMsMsIdent.getModificationAtIndex(kk);
                    System.out.println(" - " +myMod.getName() + " affecting " +myMod.getResidue() + " "+ myMod.getMassShift() + " " + myMod.position + " " + myMod.isVariableMod);

                   }

                }
        }
        }

*/
 
           
//Let's remove duplicate peptides identified by Mascot

/**
          for (int fileCnt = 0; fileCnt < identificationFiles.size(); fileCnt++) {
                xLoadData data=inputData.getDataElemAt(fileCnt);

                boolean removed=false;
                int i=0;
                int j=data.getIdentificationDataSize();
                while(i<j){
                    identInputData idData= data.getIdentInputDataElemAt(i);
                    String pSeq=idData.getPeptideSeq();
                    if(data.getAllIdentOfPeptideSeq(pSeq).size()>1){
                        data.deleteIdentOfPeptideSeq(pSeq);
                        i=0;
                        removed=true;
                        j=data.getIdentificationDataSize();
                    }
                    if(!removed){
                        i++;
                    }
                    else{
                        removed=false;
                    }

                }



                }
 **/

/*
          for (int fileCnt = 0; fileCnt < identificationFiles.size(); fileCnt++) {
                xLoadData data=inputData.getDataElemAt(fileCnt);
                for(int j=0;j<data.getIdentificationDataSize();j++){
                    identInputData idData= data.getIdentInputDataElemAt(j);
                    String pSeq=idData.getPeptideSeq();
                    String remove="";
                    if(data.getAllIdentOfPeptideSeq(pSeq).size()>1){
                        remove= " remove it!";
                    }
                    else{
                        remove="";
                    }
                    System.out.println(idData.getPeptideSeq() + " " + idData.getPeptideId() + remove);

                   }

                }
 */







          for (int fileCnt = 0; fileCnt < identificationFiles.size(); fileCnt++) {
                xLoadData data=inputData.getDataElemAt(fileCnt);

                boolean removed=false;
                int i=0;
                int j=data.getIdentificationDataSize();
                while(i<j){
                    identInputData idData= data.getIdentInputDataElemAt(i);
                    String pSeq=idData.getPeptideSeq();
                    String pId=idData.getProteinId();

                    if(data.getAllIdentOfPeptideSeq(pSeq).size()!= data.countIdentofPeptide(pSeq, pId)){
                        data.deleteIdentOfPeptideSeq(pSeq);
                        i=0;
                        removed=true;
                        j=data.getIdentificationDataSize();
                    }
                    if(!removed){
                        i++;
                    }
                    else{
                        removed=false;
                    }

                }



                }

//DEBUG PURPOSES:
/*           Vector<String> pepV=new Vector<String>();
           Vector<Float> rtV=new Vector<Float>();
           Vector<Float> mzV=new Vector<Float>();
           Vector<Integer> chV=new Vector<Integer>();

           for (int fileCnt = 0; fileCnt < identificationFiles.size(); fileCnt++) {
                xLoadData data=inputData.getDataElemAt(fileCnt);
                int identSize=data.getIdentificationDataSize();
                for(int j=0;j<identSize;j++){
                    identInputData msMsId=data.getIdentInputDataElemAt(j);
                    System.out.println("Peptide RT MZ Chg");
                    for(int k=0;k<msMsId.getLcMsMsIdentSize();k++){
                        pepV.add(msMsId.getPeptideSeq());
                        rtV.add(msMsId.getLcMsMsElemAt(k).getRetTime());
                        mzV.add(msMsId.getLcMsMsElemAt(k).getParentMass());
                        chV.add(msMsId.getLcMsMsElemAt(k).getCharge());

                        System.out.println(msMsId.getPeptideSeq() + " " + msMsId.getLcMsMsElemAt(k).getRetTime() + " " + msMsId.getLcMsMsElemAt(k).getParentMass() + " " + msMsId.getLcMsMsElemAt(k).getCharge());

                    }

                }

           } */
          /* System.out.println("PEPS=[");
           for(int i=0;i<pepV.size();i++){
                System.out.println("\""+pepV.elementAt(i)+"\"");
           }
          System.out.println("];");

          System.out.println("RTs=[");
           for(int i=0;i<rtV.size();i++){
                System.out.println(rtV.elementAt(i));
           }
          System.out.println("];");
          System.out.println("MZs=[");
           for(int i=0;i<mzV.size();i++){
                System.out.println(mzV.elementAt(i));
           }
          System.out.println("];");

          System.out.println("CHGs=[");
           for(int i=0;i<chV.size();i++){
                System.out.println(chV.elementAt(i));
           }
          System.out.println("];");
*/
        //System.exit(1);
        return ret;
    }

    /**
     * Gets identifications from the Mascot xml file and stores 'em in the structure dataSet.
     * @param mascotFile the mascot file
     * @param dataSet the structure where identifications have to be loaded.
     * @param dataId the dataset id these identifications belong to.
     */
    public void addIdentifications(String mascotFile, xLoad dataSet, int dataId) {
        
        File file = new File(mascotFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node nodeLst = doc.getElementsByTagName("hits").item(0);
            NodeList hitsList = nodeLst.getChildNodes();
            String proteinId = "";
            String peptideSeq = "";
            float hitScore = 0.0f;
            float retTime = 0.0f;
            float parIonMz = 0.0f;
            
            //Data structures to contain identifications
            xLoadData dataStruct = dataSet.getDataElemAt(dataId);

//The following code is to debug raw data acquisition if needed!
            

            int lcMsMsSize=dataStruct.getLcMsMsDataSize();
            xLcMsMsData msMsDt;
            for(int i=0; i<lcMsMsSize;i++){
                msMsDt=dataStruct.getLcMsMsDataElemAt(i);
                System.out.println(i +  ") RT: " + msMsDt.getRetTime());
                int dtSize=msMsDt.getLcMsMsElemSize();
                for(int j=0;j<dtSize;j++){
                    System.out.println("  - Parent Ion Mz:" + msMsDt.getLcMsMsElemAt(j).getParentIonMz());

                }
            }



            identInputData identData =null;
            xIdentData myIdent = null;
            int rtIndex =-1;
            int parMzIndex =-1;
            String modification="";
            int modPosition=-1;
            int numMods=1;
            boolean isVariableMod=false;
            int dbCharge=-1;

          //  boolean unique_pep_seq=true;

            String mascotPos="";

            for (int i = 0; i < hitsList.getLength(); i++) {

                NodeList subHitsList = hitsList.item(i).getChildNodes();
                for (int j = 0; j < subHitsList.getLength(); j++) {
                    Node proteinItem = subHitsList.item(j);
                    if (proteinItem.getNodeType() == Node.ELEMENT_NODE) {
                        if (proteinItem.getNodeName().equals("protein")) {
//                        identificationFiles.addElement(item.getAttributes().item(0).getTextContent().toString());
                            proteinId = proteinItem.getAttributes().item(0).getTextContent().toString();
                            System.out.println("Protein: " + proteinId);
                            NodeList subProteinList = proteinItem.getChildNodes();
                            for (int k = 0; k < subProteinList.getLength(); k++) {
                                Node peptideItem = subProteinList.item(k);
                                if (peptideItem.getNodeType() == Node.ELEMENT_NODE) {
                                    if (peptideItem.getNodeName().equals("peptide")) {
//                        identificationFiles.addElement(item.getAttributes().item(0).getTextContent().toString());
                                        //   System.out.println(" - Peptide: " + peptideItem.getAttributes().item(0).getTextContent().toString());
                                        NodeList peptideDataList = peptideItem.getChildNodes();
                                        modification="";
                                        modPosition=-1;
                                        isVariableMod=false;
                                        modification="";
                                        numMods=-1;
                                        dbCharge=-1;
                                        for (int ii = 0; ii < peptideDataList.getLength(); ii++) {
                                            Node peptideElem = peptideDataList.item(ii);
                                            if (peptideElem.getNodeType() == Node.ELEMENT_NODE) {
                                                if (peptideElem.getNodeName().equals("pep_seq")) {


//                        identificationFiles.addElement(item.getAttributes().item(0).getTextContent().toString());
                                                    peptideSeq = peptideElem.getTextContent().toString();
                                                    System.out.println(" - Peptide: " + peptideSeq);

                                                    identData = new identInputData(proteinId,peptideSeq);



                                                } else if (peptideElem.getNodeName().equals("pep_exp_mz")) {
                                                    parIonMz = Double.valueOf(peptideElem.getTextContent().toString()).floatValue();
                                                    System.out.print("\n - Peptide: " + parIonMz);
                                                } else if (peptideElem.getNodeName().equals("pep_exp_z")) {
                                                    dbCharge = Integer.valueOf(peptideElem.getTextContent().toString()).intValue();
                                                    System.out.print("\n - Peptide: " + parIonMz);
                                                }

                                                else if (peptideElem.getNodeName().equals("pep_score")) {
                                                    hitScore = Double.valueOf(peptideElem.getTextContent().toString()).floatValue();
                                                    System.out.println("  score: " + hitScore);


                                                }
                                                else if(hitScore>=this.scoreThreshold){

                                                    if(peptideElem.getNodeName().equals("pep_var_mod")){
                                                    
                                                    modification=peptideElem.getTextContent().toString();
                                                    if(modification.length()>0){
                                                    String[] multipleMods = new String[3];
                                                    numMods=1;
                                                    if(modification.matches("^\\d+[\\w|\\D|\\S|\\s|\\W]*")){
                                                        multipleMods=modification.split(" ");
                                                        numMods=Integer.valueOf(multipleMods[0]).intValue();
                                                    //    System.out.println("More than one mod!");
                                                        modification=modification.replace(numMods + " ", "");
                                                    }

                                                    if(modification.length()>0){
                                                        isVariableMod=true;
                                                    }
                                                    }
                                                    else{
                                                    numMods=-1;
                                                    }

                                                 }
                                                else if(peptideElem.getNodeName().equals("pep_var_mod_pos") && isVariableMod){
                                                    mascotPos=peptideElem.getTextContent().toString();
                                                    mascotPos=mascotPos.replace(".", "");
                                                    
                                                } else if (peptideElem.getNodeName().equals("pep_scan_title")) {
                                                    System.out.println("Parsing title");
                                                    String myTmpString = peptideElem.getTextContent().toString();
                                                    int ind = 0;
                                                    int ind1 = 0;
                                                   
                                                    ind = myTmpString.indexOf("(rt=");
                                                    if(ind>0){
                                                        ind1 = myTmpString.indexOf(") ");
                                                        retTime = Double.valueOf(myTmpString.substring(ind + 4, ind1)).floatValue();
                                                    }
                                                    else{
                                                        ind = myTmpString.indexOf("Scan Number: ");
                                                        //Correction 13=9+4 for code out of else branch
                                                        if(ind>0){
                                                               retTime = Double.valueOf(myTmpString.substring(ind + 13)).floatValue();
                                                        }
                                                        else{
                                                            System.out.println("Warning: (LoadMascotIdent): missing retention time. Zero assumed");
                                                            retTime=0;
                                                        }
                                                    }
                                                   
                                                    // String rt=myTmpString.substring(ind+4,ind1);
                                                    System.out.println("  title: " + myTmpString + "[" + ind + "," + ind1 + "]" + " retTime:" + retTime);
                                            //        rtIndex = dataStruct.getLcMsMsIndexOfRt(retTime);
                                            //        if(rtIndex==-1){
                                            //                     System.out.println("ERROR (loadMascotIdent plugin): trying to look for a nonexisting retention value (" +retTime+ ")!");
                                            //                     System.exit(1);
                                            //       }


                                                 //   parMzIndex = dataStruct.getLcMsMsDataElemAt(rtIndex).getLcMsMsIndexOfParIonMz(parIonMz);
                                              //     parMzIndex=0;
                                              //      if(parMzIndex==-1){
                                                //                 System.out.println("ERROR (loadMascotIdent plugin): trying to look for a parent Ion Mz value (" +parIonMz+ ")!");
                                               //                  System.exit(1);
                                                 //  }

                                                    myIdent=new xIdentData(hitScore,retTime, parIonMz,dbCharge);

                                                    

                                                    int startIndex=0;
                                                    for(int jj=0;jj<numMods;jj++){
                                                        modPosition=mascotPos.indexOf("1",startIndex);

                                                        startIndex=modPosition+1;
                                                        int mascotModIndex=mascotVarMods.indexOf(modification);
                                                        System.out.println(modification + " replaced pos: " +mascotPos + " " +modPosition + " index:" + mascotModIndex);

                                                        
                                                        xModification myMod= new xModification(modification, mascotVarModShifts.elementAt(mascotModIndex), modPosition, true);
                                                        myIdent.addModification(myMod);
                                                        
                                                    }
                                                    //Let's add fixed modifications!

                                                    for(int pepEl=0;pepEl<peptideSeq.length();pepEl++){
                                                        
                                                        char residue=peptideSeq.charAt(pepEl);
                                                       

                                                        int myModInd=fixModResidues.indexOf(Character.toString(residue));
                                                        if(myModInd>-1){

                                                          xModification myFixMod= new xModification(mascotFixedMods.elementAt(myModInd), mascotFixedModShifts.elementAt(myModInd), pepEl+1, false);
                                                          myIdent.addModification(myFixMod);
                                                        }
                                                    }
                                                    //Checking for N-Term and C-Term modifications
                                                   int myModInd=fixModResidues.indexOf("N-term");
                                                        if(myModInd>-1){
                                                          xModification myFixMod= new xModification(mascotFixedMods.elementAt(myModInd), mascotFixedModShifts.elementAt(myModInd), 0, false);
                                                          myIdent.addModification(myFixMod);
                                                        }

                                                        myModInd=fixModResidues.indexOf("C-term");
                                                        if(myModInd>-1){
                                                          xModification myFixMod= new xModification(mascotFixedMods.elementAt(myModInd), mascotFixedModShifts.elementAt(myModInd), peptideSeq.length()+1, false);
                                                          myIdent.addModification(myFixMod);
                                                        }

                                                    identData.addLcMsMsIdent(myIdent);
                                                    dataStruct.addIdentificationData(identData);



                                                }


                                            }
                                        }

                                    }
                                    }

                                }
                            }

                        }


                    }
                }

            }
        } catch (Exception e) {
            System.out.println("LoadMascotIdent: Exception while reading " + file + "\n" + e);
            System.exit(1);
        }
    }

    /**
     * Opens the dataFile xml file and loads mass shifts,the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     */
    public void loadParams(String dataFile) {
        
        int i = 0;

        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
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

            NodeList itemList = nodeLst.getChildNodes();
            for (i = 0; i < itemList.getLength(); i++) {
                Node item = itemList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {

                    if(item.getNodeName().equals("inputFiles")){
                        
                        Node nodeLstIn= doc.getElementsByTagName("inputFiles").item(0);
                        NodeList itemListIn = nodeLstIn.getChildNodes();
                        for (int j = 0; j < itemListIn.getLength(); j++) {
                        Node itemI = itemListIn.item(j);

                        if (itemI.getNodeName().equals("datafile")) {
                            
                            identificationFiles.addElement(itemI.getAttributes().item(0).getTextContent().toString());
                        // System.out.println("Read files:" + itemI.getTextContent() + " " + itemI.getAttributes().item(0).getTextContent().toString());
                        }
                       }
                    }
                    else{
                        if(item.getNodeName().equals("modificationData")){

                            Node nodeLstM = doc.getElementsByTagName("modificationData").item(0);

                            NodeList itemListM = nodeLstM.getChildNodes();

                            for (int j = 0; j < itemListM.getLength(); j++) {
                                Node itemM = itemListM.item(j);

  
                                if(itemM.getNodeName().equals("modification")){
                                    fixedMods.addElement(itemM.getTextContent());
                                  //  System.out.println("Mod:" +itemM.getTextContent());

                                    fixedModShifts.addElement(Float.valueOf(itemM.getAttributes().item(0).getTextContent().toString()));
                                    System.out.println("Modification:" +itemM.getTextContent() + " (" + itemM.getAttributes().item(0).getTextContent().toString() + ")");

                        }
                        }

                        }
                        
                        
                        
                        else{
                                if(item.getNodeName().equals("pep_score_threshold")){
                                    this.scoreThreshold=Float.valueOf(item.getTextContent()).floatValue();
                                    
                                }


                        }


                    }


                }
            }


        } catch (Exception e) {
            System.out.println("Exception while reading " + dataFile + "\n" + e);
            System.exit(1);
        }


    }


     /**
     * Opens the mascot xml file and loads mass shifts, and loads the information on variable and fixed modifications used in the search.
     * @param dataFile
     */
    public void loadMascotMods(String dataFile) {
        //mass shifts.
        //the order is the following:




        int i = 0;
        boolean insertFlag=true;

        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node nodeLst = null;
            nodeLst=doc.getElementsByTagName("variable_mods").item(0);
            if(! (nodeLst == null)){
            NodeList itemList = nodeLst.getChildNodes();
            for (i = 0; i < itemList.getLength(); i++) {
                Node item = itemList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    if (item.getNodeName().equals("modification")) {

                        insertFlag=true;
                        NodeList modNodes = item.getChildNodes();

                        for (int j = 0; j < modNodes.getLength(); j++) {
                            Node mod = modNodes.item(j);
                            if (mod.getNodeType() == Node.ELEMENT_NODE) {
                                if (mod.getNodeName().equals("name")) {
                                    String nameVal=mod.getTextContent().toString();
                                    if(mascotVarMods.indexOf(nameVal)==-1){
                                        mascotVarMods.addElement(nameVal);
                                    }
                                    else{
                                        insertFlag=false;
                                    }
                                }
                                else{
                                    if(mod.getNodeName().equals("delta")){
                                        if(insertFlag){
                                        mascotVarModShifts.addElement(Float.valueOf(mod.getTextContent().toString()));
                                        }
                                    }

                                }
                            }
                            }
                    }



                }
            }
            }

           //Variable modifications have been loaded, let's load fixed modifications.
           nodeLst=null;
           nodeLst = doc.getElementsByTagName("search_parameters").item(0);
           if(! (nodeLst == null)){
            NodeList itemList = nodeLst.getChildNodes();
            for (i = 0; i < itemList.getLength(); i++) {
                Node item = itemList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    if (item.getNodeName().equals("MODS")) {
                        String allFixMods=item.getTextContent().toString();
                        //If some fixed modifications exist
                        if(allFixMods.length()>0){
                        int mySize=allFixMods.split(",").length;

                        String tokens[]= new String[mySize];
                        tokens=allFixMods.split(",");
                        for(int j=0;j<tokens.length;j++){
                            String myTok=tokens[j];
                            



                            int k=fixedMods.indexOf(myTok);
                           
                            if(k>-1){
                                int ind = 0;
                                int ind1 = 0;
                                ind = myTok.indexOf("(");
                                ind1 = myTok.indexOf(")");
                                String aminos=myTok.substring(ind+1, ind1);
                                if(aminos.indexOf("-term")==-1){
                                    char res[]= new char[aminos.length()];
                                    res=aminos.toCharArray();
                                    for(int ii=0;ii<res.length;ii++){
                                        fixModResidues.addElement(String.valueOf(res[ii]));
                                        mascotFixedMods.addElement(myTok);
                                        mascotFixedModShifts.addElement(fixedModShifts.elementAt(k));

                                    }

                                }
                                else{

                                fixModResidues.addElement(aminos);
                                mascotFixedMods.addElement(myTok);
                                mascotFixedModShifts.addElement(fixedModShifts.elementAt(k));
                                }
                              //  System.out.println("Token:" + myTok +" " +ind + "," +ind1 + "=" +aminos);



                            }
                            else{
                                System.out.println("Error (loadMascotIdent): trying to add a fixed modification not specified in the parameters file (" +myTok +").");
                                System.exit(1);
                            }

                        }
                    }
                    }
                }
            }
           }
        } catch (Exception e) {
            System.out.println("Exception while reading " + dataFile + "\n" + e);
            System.exit(1);
        }



    }


    public boolean stop() {
        System.out.println(getName() + ": stopping...");
        return true;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {

        return type;
    }

    public String getDescription() {

        return description;
    }
    Vector<String> identificationFiles = new Vector<String>();
    Vector<String> fixedMods = new Vector<String>();
    Vector<Float> fixedModShifts = new Vector<Float>();
    Vector<String> mascotFixedMods = new Vector<String>();
    Vector<Float> mascotFixedModShifts = new Vector<Float>();
    Vector<String> fixModResidues = new Vector<String>();

    Vector<String> mascotVarMods = new Vector<String>();
    Vector<Float> mascotVarModShifts = new Vector<Float>();

    float scoreThreshold=0f;
    
    private final static String name = "Load_MascotXML";
    private final static String version = "1.00";
    private final static String type = "IDENTDATA_LOAD_plugin";
    private final static String description = "Loads Mascot xml identification files\n\tThis plugin loads peptide identifications from a Mascot .xml file\n\tor set of files. It expects retention \n\ttimes (associated to spectra to which identifications belong)\n\twithin the <pep_scan_title> tag.\n\tPeptides with same sequence identified in more than a protein are\n\tremoved.\n\n";
    //public static xLoad ret;
}