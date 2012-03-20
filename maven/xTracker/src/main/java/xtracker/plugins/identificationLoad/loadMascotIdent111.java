/*
 * --------------------------------------------------------------------------
 * loadMascotIdent111.java
 * --------------------------------------------------------------------------
 * Description:       Plugin to load identification data
 * Developer:         Faviel Gonzalez
 * Created:           06 February 2012
 * Read our documentation file under our Google SVN repository
 * SVN: http://code.google.com/p/x-tracker/
 * Project Website: http://www.x-tracker.info/
 * --------------------------------------------------------------------------
 */
package xtracker.plugins.identificationLoad;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import xtracker.data.xIdentData;
import xtracker.data.xLcMsMsData;
import xtracker.data.xLoad;
import xtracker.data.xLoadData;
import xtracker.data.xModification;
import xtracker.data.identInputData;
import xtracker.utils.XMLparser;

/**
 *
 * @author Faviel Gonzalez
 */

public class loadMascotIdent111 implements identData_loadPlugin {

    Vector<String> identificationFiles = new Vector<String>();
    
    Vector<String> fixedMods = new Vector<String>(); //... Fixed modifications on xtracker config file ...//
    Vector<Float> fixedModShifts = new Vector<Float>();
    
    Vector<String> mascotVarMods = new Vector<String>(); //... Modification variables from mascot file ...//
    Vector<Float> mascotVarModShifts = new Vector<Float>();    
    Vector<String> mascotFixedMods = new Vector<String>(); 
    Vector<Float> mascotFixedModShifts = new Vector<Float>();
    Vector<String> fixModResidues = new Vector<String>();
    
    float scoreThreshold = 0f;
    private final static String name = "loadMascotIdent111";
    private final static String version = "1.0";
    private final static String description = "This plugin loads Mascot XML files.";    
    
    public xLoad start(xLoad inputData, String paramFile) 
    {
        //... Loading data structure to fill identification data (previously loaded with raw data) ...//
        xLoad ret = inputData;
        System.out.println("Loading " + getName() + " plugin ...");
        
        String inputDataFile = "";                  
        
        //... Loading all files included in the .xtp file ...//
        System.out.println("Reading " + paramFile + " file ...");
        this.loadParams(paramFile);                 

        //... Loading modificiations from mascot XML file ...//
        this.loadMascotMods(identificationFiles.elementAt(0));
        System.out.println("Mods loaded!");        
        
        //... Print modification variables and residues ...//
        for (int i = 0; i < mascotVarMods.size(); i++) 
        {
            System.out.println("Mascot modification variable, Name=" + mascotVarMods.elementAt(i) + ", Shift=" + mascotVarModShifts.elementAt(i).toString());
        }
        for (int i = 0; i < fixModResidues.size(); i++) 
        {
            System.out.println("Mascot fixed variable, Residue=(" + fixModResidues.elementAt(i) + "), Name=" + mascotFixedMods.elementAt(i) + ", Shift=" + mascotFixedModShifts.elementAt(i).toString());
        }        
        
        //... Once we have data on the arrays, we need to update the xLoad data structure ...//
        for (int fileCnt = 0; fileCnt < identificationFiles.size(); fileCnt++) 
        {
            inputDataFile = identificationFiles.elementAt(fileCnt);
            
            //... Adding the identifications ...//
            addIdentifications(inputDataFile, ret, fileCnt);
        }        
        System.out.println("Reading identification files");
        for (int fileCnt = 0; fileCnt < identificationFiles.size(); fileCnt++) 
        {
            xLoadData data = inputData.getDataElemAt(fileCnt);

            boolean removed = false;
            int iI = 0;
            int iJ = data.getIdentificationDataSize();
            while (iI < iJ) 
            {
                identInputData idData = data.getIdentInputDataElemAt(iI);
                String pSeq = idData.getPeptideSeq();
                String pId = idData.getProteinId();

                if (data.getAllIdentOfPeptideSeq(pSeq).size() != data.countIdentofPeptide(pSeq, pId)) 
                {
                    data.deleteIdentOfPeptideSeq(pSeq);
                    iI = 0;
                    removed = true;
                    iJ = data.getIdentificationDataSize();
                }
                if (!removed) 
                {
                    iI++;
                } 
                else 
                {
                    removed = false;
                }

            }
        }
        System.out.println("End identification file reading");
        return ret;
    }

    /**
     * Gets identifications from the Mascot xml file and stores them in the structure dataSet.
     * @param mascotFile the mascot file.
     * @param dataSet the structure where identifications have to be loaded.
     * @param dataId the dataset id these identifications belong to.
     */
    public void addIdentifications(String mascotFile, xLoad dataSet, int dataId) 
    {
        //... Open mascot file and extract identifications ...//
        File file = new File(mascotFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try 
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node nodeLst = doc.getElementsByTagName("hits").item(0); //... Reading hits ...//
            NodeList hitsList = nodeLst.getChildNodes();                       

            //... Data structures to contain identifications ...//
            xLoadData dataStruct = dataSet.getDataElemAt(dataId);

            //... The following code is to debug raw data acquisition if needed (MS2 data not needed yet) ...//
            int lcMsMsSize = dataStruct.getLcMsMsDataSize();
            System.out.println("LcMSMS size = " + lcMsMsSize);            
            
            //... At the moment there is no reason to load MS2 data, we will be working on the examples on how to associate identifications ...//
            //... with MS1 data                                                                                                             ...//
            xLcMsMsData msMsDt;
            for (int iI = 0; iI < lcMsMsSize; iI++) 
            {
                msMsDt = dataStruct.getLcMsMsDataElemAt(iI);
                System.out.println(iI + ") RT: " + msMsDt.getRetTime());
                int dtSize = msMsDt.getLcMsMsElemSize();
                for (int iJ = 0; iJ < dtSize; iJ++) 
                {
                    System.out.println("  - Parent Ion Mz:" + msMsDt.getLcMsMsElemAt(iJ).getParentIonMz());
                }
            }            
            
            //... Initializing proteinId, peptide sequence and other variables ...//
            String proteinId = "";
            String peptideSeq = "";
            float hitScore = 0.0f;
            float retTime = 0.0f;
            float parIonMz = 0.0f;                      

            identInputData identData = null; //... Data structures on XTracker ...//
            xIdentData myIdent = null;
            
            String[] modification = new String[6];
            int modPosition = -1;
            int numMods = 1;
            boolean isVariableMod = false;
            int dbCharge = -1;
            String mascotPos = "";                     
            int iProteins = 0;
            int iPeptides = 0;
            String sChainToInsert="";
                        
            for (int iI = 0; iI < hitsList.getLength(); iI++) //... Identifying all protein hits from the search ...//
            {                
                NodeList subHitsList = hitsList.item(iI).getChildNodes();
                for (int iJ = 0; iJ < subHitsList.getLength(); iJ++) 
                {
                    Node proteinItem = subHitsList.item(iJ);
                    if (proteinItem.getNodeType() == Node.ELEMENT_NODE) 
                    {
                        if (proteinItem.getNodeName().equals("protein")) //... Reading the protein ...//
                        {
                            iProteins++;
                            proteinId = proteinItem.getAttributes().item(0).getTextContent().toString();
                            NodeList subProteinList = proteinItem.getChildNodes(); //... Protein subnodes ...//
                            for (int iK = 0; iK < subProteinList.getLength(); iK++) 
                            {
                                Node peptideItem = subProteinList.item(iK);
                                if (peptideItem.getNodeType() == Node.ELEMENT_NODE) 
                                {
                                    if (peptideItem.getNodeName().equals("peptide")) //... Reading peptides ...//
                                    {
                                        iPeptides++;                                        
                                        System.out.println("Protein " + iProteins + ": " + proteinId + " Peptide " + iPeptides );
                                        NodeList peptideDataList = peptideItem.getChildNodes(); //... Peptide subnodes ...//
                                        
                                        //... Initializing variables ...//
                                        for (int iA=0;iA<6;iA++) //... Cleaning array ...//
                                        {
                                            modification[iA] = null;
                                        }
                                        int iContMod = 0;
                                        modPosition = -1;
                                        isVariableMod = false;
                                        numMods = -1;
                                        dbCharge = -1;
                                        sChainToInsert = "";
                                        for (int iL = 0; iL < peptideDataList.getLength(); iL++) 
                                        {
                                            Node peptideElem = peptideDataList.item(iL);
                                            if (peptideElem.getNodeType() == Node.ELEMENT_NODE) 
                                            {
                                                if (peptideElem.getNodeName().equals("pep_seq")) 
                                                {
                                                    peptideSeq = peptideElem.getTextContent().toString();
                                                    System.out.println(" - Sequence: " + peptideSeq);

                                                    identData = new identInputData(proteinId, peptideSeq);
                                                } 
                                                else if (peptideElem.getNodeName().equals("pep_exp_mz")) 
                                                {
                                                    parIonMz = Double.valueOf(peptideElem.getTextContent().toString()).floatValue();
                                                    System.out.println(" - Experimental m/z: " + parIonMz);
                                                } 
                                                else if (peptideElem.getNodeName().equals("pep_exp_z")) 
                                                {
                                                    dbCharge = Integer.valueOf(peptideElem.getTextContent().toString()).intValue();
                                                    System.out.println(" - Experimental charge: " + dbCharge);
                                                } 
                                                else if (peptideElem.getNodeName().equals("pep_score")) 
                                                {
                                                    hitScore = Double.valueOf(peptideElem.getTextContent().toString()).floatValue();
                                                    System.out.println(" - Score: " + hitScore);
                                                } 
                                                else if (hitScore >= this.scoreThreshold) //... Verify if hits are over minimum threshold ...//
                                                {
                                                    if (peptideElem.getNodeName().equals("pep_var_mod")) //... Modification variables ...//
                                                    {
                                                        String sModVariable = peptideElem.getTextContent().toString();
                                                        
                                                        System.out.println(" - IndexOf="+sModVariable.indexOf(";"));
                                                        String[] differentMods = new String[3];
                                                        differentMods = sModVariable.split(";"); //... Check if different modifications are separated by colons ..//
                                                        System.out.println(" - Length="+differentMods.length);
                                                        if (differentMods[0].equals("")) //... Verify if has data ...//
                                                        {
                                                            isVariableMod = false; 
                                                        }                   
                                                        else
                                                        {
                                                            for (int iDiffMod=0; iDiffMod < differentMods.length; iDiffMod++)
                                                            {
                                                                //... This code parses the number of modifications (1, 2, n) once we have identified the different modif variables ...//                                                            
                                                                if (differentMods[iDiffMod].startsWith(" "))
                                                                {
                                                                     differentMods[iDiffMod] = differentMods[iDiffMod].substring(1, differentMods[iDiffMod].length());
                                                                }
                                                                System.out.println(" - NewMod="+differentMods[iDiffMod]);
                                                                if (differentMods[iDiffMod].length() > 0) 
                                                                {
                                                                    String[] multipleMods = new String[3]; //... New array for multiple modifications in different modifications ...//
                                                                    numMods = 1;
                                                                    if (differentMods[iDiffMod].matches("^\\d+[\\w|\\D|\\S|\\s|\\W]*")) 
                                                                    {
                                                                        multipleMods = differentMods[iDiffMod].split(" ");
                                                                        numMods = Integer.valueOf(multipleMods[0]).intValue();
                                                                        System.out.println(" - Note: The same modification was repeated several times!");
                                                                        sChainToInsert = differentMods[iDiffMod].replace(numMods + " ", "");
                                                                    }
                                                                    else
                                                                    {
                                                                        sChainToInsert = differentMods[iDiffMod];
                                                                        numMods = 1;                                                                     
                                                                    }
                                                                    for (int iFound=0; iFound<numMods; iFound++)
                                                                    {
                                                                        modification[iContMod] = sChainToInsert;
                                                                        iContMod++;
                                                                    }
                                                                } 
                                                            }
                                                            isVariableMod = true;         
                                                        }
                                                    } 
                                                    else if (peptideElem.getNodeName().equals("pep_var_mod_pos") && isVariableMod) 
                                                    {
                                                        mascotPos = peptideElem.getTextContent().toString();
                                                        mascotPos = mascotPos.replace(".", "");
                                                    } 
                                                    else if (peptideElem.getNodeName().equals("pep_scan_title")) //... Only in some cases the rt is specified in the scan title ...//
                                                    {
                                                        System.out.println(" - Parsing title");
                                                        if (peptideElem.getTextContent().toString().indexOf("rt=")>0) //... Option 1, reading on scan title ...//
                                                        {
                                                            String myTmpString = peptideElem.getTextContent().toString();
                                                            int ind = 0;
                                                            int ind1 = 0;

                                                            ind = myTmpString.indexOf("(rt=");
                                                            if(ind>0)
                                                            {
                                                                ind1 = myTmpString.indexOf(")");
                                                                retTime = Double.valueOf(myTmpString.substring(ind + 4, ind1)).floatValue();
                                                            }
                                                            else
                                                            {
                                                                retTime=0;
                                                            }
                                                        } 
                                                        else 
                                                        {
                                                            //... Read mzML file or MGF (TO DO) ...//                                                            
                                                            retTime = 0;                                                            
                                                        }
                                                        System.out.println(" - retTime=" + retTime);
                                                        
                                                        // ----------------------------------------------------- //
                                                        //... Adding new identification to the data structure ...//
                                                        // ----------------------------------------------------- //
                                                        myIdent = new xIdentData(hitScore, retTime, parIonMz, dbCharge);

                                                        //... Adding Mascot Variable modifications ...//
                                                        int startIndex = 0;
                                                        for (int iM = 0; iM < iContMod; iM++) 
                                                        {
                                                            //... Searching for the position where the modification occurred e.g. 00100000 ...//
                                                            for (int iParams=0; iParams<mascotVarMods.size(); iParams++)
                                                            {
                                                                modPosition = mascotPos.indexOf(Integer.toString(iParams), startIndex);     //... Digit by digit ...//
                                                                if (modPosition > 0)
                                                                {
                                                                    break;
                                                                }
                                                            }                                                             
                                                            startIndex = modPosition + 1; //... Increase position in case it has more than one mod, e.g. 0000000002020 ...//
                                                            
                                                            int mascotModIndex = mascotVarMods.indexOf(modification[iM]);
                                                            System.out.println(" - ModVar=" + modification[iM] + ", Modified Peptide=" + mascotPos + ", Pos=" + modPosition + ", Mascot Index:" + (mascotModIndex));

                                                            xModification myMod = new xModification(modification[iM], mascotVarModShifts.elementAt(mascotModIndex), modPosition, true);
                                                            myIdent.addModification(myMod);
                                                        }
                                                        
                                                        //... Adding fixed modifications ...//
                                                        for (int pepEl = 0; pepEl < peptideSeq.length(); pepEl++) 
                                                        {
                                                            char residue = peptideSeq.charAt(pepEl);
                                                            int myModInd = fixModResidues.indexOf(Character.toString(residue));
                                                            if (myModInd > -1) 
                                                            {
                                                                System.out.println(" - FixModRes=" + mascotFixedMods.elementAt(myModInd) + ", mascotfixedmodshifts=" + mascotFixedModShifts.elementAt(myModInd) + ", PepEl=" + (pepEl + 1));
                                                                xModification myFixMod = new xModification(mascotFixedMods.elementAt(myModInd), mascotFixedModShifts.elementAt(myModInd), pepEl + 1, false);
                                                                myIdent.addModification(myFixMod);
                                                            }
                                                        }
                                                        //... Checking for N-Term and C-Term modification residues ...//
                                                        int myModInd = fixModResidues.indexOf("N-term");
                                                        if (myModInd > -1) 
                                                        {
                                                            System.out.println(" - FixModN-term=" + mascotFixedMods.elementAt(myModInd) + ", mascotfixedmodshifts=" + mascotFixedModShifts.elementAt(myModInd));
                                                            xModification myFixMod = new xModification(mascotFixedMods.elementAt(myModInd), mascotFixedModShifts.elementAt(myModInd), 0, false);
                                                            myIdent.addModification(myFixMod);
                                                        }
                                                        myModInd = fixModResidues.indexOf("C-term");
                                                        if (myModInd > -1) 
                                                        {
                                                            System.out.println(" - FixModC-term=" + mascotFixedMods.elementAt(myModInd) + ", mascotfixedmodshifts=" + mascotFixedModShifts.elementAt(myModInd) + ", peptSeq=" + (peptideSeq.length() + 1));
                                                            xModification myFixMod = new xModification(mascotFixedMods.elementAt(myModInd), mascotFixedModShifts.elementAt(myModInd), peptideSeq.length() + 1, false);
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
            System.out.println("LoadMascotIdent completed!");
        } 
        catch (Exception e) 
        {
            System.out.println("LoadMascotIdent: Exception while reading " + file + "\n" + e);
            System.exit(1);
        }
    }

    /**
     * Opens the xml file and loads mass shifts, the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     */
    public void loadParams(String dataFile) 
    {
        //... Open the xml file ...//
        XMLparser parser = new XMLparser(dataFile);
        
        //... Parsing inputFiles, modificationData and other elements ...//
        parser.validate("param");
        NodeList itemList = parser.getElement("param").getChildNodes();
        for (int iI = 0; iI < itemList.getLength(); iI++) 
        {
            Node item = itemList.item(iI);
            if (item.getNodeType() == Node.ELEMENT_NODE) 
            {
                if (item.getNodeName().equals("inputFiles")) //... Input files ...//
                {
                    Node nodeLstIn = parser.getElement("inputFiles");
                    NodeList itemListIn = nodeLstIn.getChildNodes();
                    for (int iJ = 0; iJ < itemListIn.getLength(); iJ++) 
                    {
                        Node itemI = itemListIn.item(iJ);
                        if (itemI.getNodeName().equals("datafile")) 
                        {
                            identificationFiles.addElement(itemI.getAttributes().item(0).getTextContent().toString()); //... The first attribute represents the XML mascot file ...//
                        }
                    }
                } 
                else 
                {
                    if (item.getNodeName().equals("modificationData")) //... Modification variables ...//
                    {
                        Node nodeLstM = parser.getElement("modificationData");

                        NodeList itemListM = nodeLstM.getChildNodes();
                        for (int iJ = 0; iJ < itemListM.getLength(); iJ++) 
                        {
                            Node itemM = itemListM.item(iJ);
                            if (itemM.getNodeName().equals("modification")) //... Fixed modifications and mass shifts ...//
                            {
                                fixedMods.addElement(itemM.getTextContent());
                                fixedModShifts.addElement(Float.valueOf(itemM.getAttributes().item(0).getTextContent().toString()));
                                System.out.println("Modification: " + itemM.getTextContent() + " (" + itemM.getAttributes().item(0).getTextContent().toString() + ")");
                            }
                        }
                    } 
                    else 
                    {
                        if (item.getNodeName().equals("pep_score_threshold")) 
                        {
                            this.scoreThreshold = Float.valueOf(item.getTextContent()).floatValue();
                            System.out.println("Threshold: "  + Float.valueOf(item.getTextContent()).floatValue());
                        }
                    }
                } //... Else input files
            } //... If has elements
        } //... For
    }

    /**
     * Opens the mascot XML file, loads mass shifts and the information on variable and fixed modifications used in the search.
     * @param dataFile
     */
    public void loadMascotMods(String dataFile) 
    {
        int iI = 0;
        boolean insertFlag = true;

        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try 
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node nodeLst = null;
            
            //... Modification variables ...//
            nodeLst = doc.getElementsByTagName("variable_mods").item(0); 
            if (!(nodeLst == null)) 
            {
                NodeList itemList = nodeLst.getChildNodes();
                for (iI = 0; iI < itemList.getLength(); iI++) 
                {
                    Node item = itemList.item(iI);
                    if (item.getNodeType() == Node.ELEMENT_NODE) 
                    {
                        if (item.getNodeName().equals("modification")) //... Searching nodes ...//
                        {
                            insertFlag = true; //... By default, it is expected to have modification variables ...//
                            NodeList modNodes = item.getChildNodes();

                            for (int iJ = 0; iJ < modNodes.getLength(); iJ++) 
                            {
                                Node mod = modNodes.item(iJ);
                                if (mod.getNodeType() == Node.ELEMENT_NODE) 
                                {
                                    if (mod.getNodeName().equals("name"))  //... Modification name ...//
                                    {
                                        String nameVal = mod.getTextContent().toString();
                                        if (mascotVarMods.indexOf(nameVal) == -1) 
                                        {
                                            mascotVarMods.addElement(nameVal);
                                        } 
                                        else 
                                        {
                                            insertFlag = false; //... no modification variables ...//
                                        }
                                    } 
                                    else 
                                    {
                                        if (mod.getNodeName().equals("delta")) //... Modification shift ...//
                                        {
                                            if (insertFlag) 
                                            {
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

            //... Searching parameters ...//
            nodeLst = null;
            nodeLst = doc.getElementsByTagName("search_parameters").item(0);
            if (!(nodeLst == null)) 
            {
                NodeList itemList = nodeLst.getChildNodes();
                for (iI = 0; iI < itemList.getLength(); iI++) 
                {
                    Node item = itemList.item(iI);
                    if (item.getNodeType() == Node.ELEMENT_NODE) 
                    {
                        if (item.getNodeName().equals("MODS")) //... Looking for fixed modifications on the search ...//
                        {
                            String allFixMods = item.getTextContent().toString();                            
                            if (allFixMods.length() > 0) 
                            {
                                int mySize = allFixMods.split(",").length;
                                String tokens[] = new String[mySize];
                                tokens = allFixMods.split(","); //... If there are several modifications, these are separated by commas ...//
                                for (int iJ = 0; iJ < tokens.length; iJ++) //... Reading all modifications ...//
                                {
                                    String myTok = tokens[iJ];
                                    int iK = fixedMods.indexOf(myTok);
                                    if (iK > -1) 
                                    {
                                        int ind = 0;
                                        int ind1 = 0;
                                        ind = myTok.indexOf("(");
                                        ind1 = myTok.indexOf(")");
                                        String aminos = myTok.substring(ind + 1, ind1);
                                        if (aminos.indexOf("-term") == -1) //... Some modification variables contains residues, e.g. "-term" ...//
                                        {
                                            char res[] = new char[aminos.length()];
                                            res = aminos.toCharArray();
                                            for (int iL = 0; iL < res.length; iL++) 
                                            {
                                                fixModResidues.addElement(String.valueOf(res[iL]));
                                                mascotFixedMods.addElement(myTok);
                                                mascotFixedModShifts.addElement(fixedModShifts.elementAt(iK)); //... It looks into the config file for the corresponding shift value ...//
                                            }
                                        } 
                                        else 
                                        {
                                            fixModResidues.addElement(aminos);
                                            mascotFixedMods.addElement(myTok);
                                            mascotFixedModShifts.addElement(fixedModShifts.elementAt(iK)); //... It looks into the config file for the corresponding shift value ...//
                                        }
                                    } 
                                    else 
                                    {
                                        System.out.println("Error (loadMascotIdent): trying to add a fixed modification not specified in the parameters file (" + myTok + ").");
                                        System.exit(1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } 
        catch (Exception e) 
        {
            System.out.println("Exception while reading " + dataFile + "\n" + e);
            System.exit(1);
        }
    }
    public boolean stop() 
    {
        System.out.println(getName() + ": stopping...");
        return true;
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
}