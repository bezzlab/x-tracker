package xtracker;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Vector;

public class metabolicLabeling implements peakSelPlugin
{
    /**
     * Loads aminoacid mass shifts as well as n-terminus and c-terminus ones. After this it reads in MS/MS identification data loaded into xLoad structure. 
     * For each peptide identified it retrieves the parent Ion m/z, the Retention Time, the sequence and charge and computes the mass shift of the whole peptide. Finally it tries
     * to match the parent Ion m/z +/- the displacement in a MS peak within a specified Retention Time window. If the peak is at "Parent Ion m/z + mass shift" it labels the new peak as "heavy" and the original
     * one as light and viceversa. 
     * It finally populates the xPeaks structure.  
     * @param input the xLoad structure
     * @return xPeaks the peaks selected for quantitation.
     */
    public xPeaks start(xLoad input, String paramFile)
    {   

       
        int i;
        xPeaks ret = new xPeaks();
        
                       int totalAdded=0;
                       int addedCounter=0;
//        xLcMsData myMsData=input.getDataElemAt(0).getLcMsDataElemAt(0);

        //First open the .xml file and retrieve mass shifts.
        massShifts=loadParams(paramFile);
//        for(i=0;i<22;i++){
//            System.out.println(i + ": " + massShifts[i]);
//       }
        
        if(! (mass_type.toLowerCase().equals("average") || mass_type.toLowerCase().equals("monoisotopic"))){
                        System.out.println("ERROR (metabolicLabeling.jar): mass type " + mass_type + " not recognised!");
                        System.exit(1);
                       
                       }

        //Let's process any single file
        for(i=0;i<input.getDataSize();i++){
            
            identInputData myMsMsVals;
            xLoadData myLoadData = input.getDataElemAt(i);
            //xLcMsMsData mySpectrum = null;
            System.out.println("Processing data from " + myLoadData.getFileName() + "...");
             //Let's process any single identification of the file
             int identDataSize=myLoadData.getIdentificationDataSize();   
            for(int j=0;j<identDataSize;j++){
                myMsMsVals=myLoadData.getIdentInputDataElemAt(j);
                String pepSeq=myMsMsVals.getPeptideSeq();
                String pepId=myMsMsVals.getPeptideId();
                //Let's retrieve the mass shift of the peptide identified
                //Remember that it has to be divided by the charge!!!!
                float shift=computePeptideMassShift(pepSeq);
                
                float pepThMass=computeMass(pepSeq);

                float[] retTimesV = new float[myMsMsVals.getMsMsIdentSize()];
                float[] parentMzV = new float[myMsMsVals.getMsMsIdentSize()];
                String[] typesV = new String[myMsMsVals.getMsMsIdentSize()]; 
                String pepType="";
                int counter=0;//counts how many elements are different from -1 in the
                             //two arrays.
                int ind=0;
                int msMsDtSize=myMsMsVals.getMsMsIdentSize();
                for(int k=0;k<msMsDtSize;k++){
                    retTimesV[k]=-1;
                    parentMzV[k]=-1;
                    typesV[k]="";        
                    //Let's retrieve the parent Ion m/z
                    float parentIonMz=0f;
                    xIdentData myIdent=myMsMsVals.getMsMsElemAt(k);
                    xLcMsMsElem msMsElement=myLoadData.getLcMsMsDataElemAt(myIdent.getRetTimeIndex()).getLcMsMsElemAt(myIdent.getParentMassIndex());
                    parentIonMz= msMsElement.getParentIonMz();
                    int charge=msMsElement.getCharge();
                    float diff=parentIonMz*charge - pepThMass;
                    shift= shift/charge;
                    pepType=this.computePeptideType(pepSeq, parentIonMz, shift,charge);
                    System.out.println("Peptide " + pepSeq + "(" +pepType +"): mass="+ pepThMass + " shift=" + shift);
                
                    System.out.println(" - Par Ion's Mass: " + parentIonMz + " difference: " +diff);
                    //Let's retrieve the retention time
                    float retTime;
                     xLcMsMsData msMsDataElem=myLoadData.getLcMsMsDataElemAt(myIdent.getRetTimeIndex());
                    retTime= msMsDataElem.getRetTime();
//                    System.out.println("Retention Time:" + retTime + " Mass target:" + (parentIonMz-shift) + " " + (parentIonMz+shift));
                    boolean searching=true;
                    
                    for(ind=0;(ind<k) && (searching);ind++){
                        if((retTimesV[ind]== retTime) && (parentMzV[ind]==parentIonMz)){
                            searching=false;
                        }
                    
                    }
                    if(searching){
                        retTimesV[ind]=retTime;
                        parentMzV[ind]=parentIonMz;
                        typesV[ind]=pepType;
                        counter++;
                    }
                
                }
                

               for(int k=0;k<counter;k++){
                          
                         float rt=retTimesV[k];
                         float parentMz=parentMzV[k];
                         String typeP=typesV[k];
                         System.out.println("(" +k+ ") Retention Time:" + rt + " Original target:" + (parentMz-mzTolerance) + "-" + (parentMz+mzTolerance) +" original: " + parentMz + " Heavy Mass Matching Target:" +(parentMz+shift-mzTolerance) + "-" + (parentMz+shift+mzTolerance));
                          
//                          Vector[] tentativePeaks=  new Vector[3];
//                          tentativePeaks=computeCorrespondences(rt, parentMz,myLoadData, shift);
                         
                          Vector[] tentativePeaks=  new Vector[2];
                          tentativePeaks=computePeakCorrespondences(rt, parentMz,myLoadData, shift,typeP);
                          System.out.println("Original (" +typeP+"):" + tentativePeaks[0].size());
                          if(typeP.equals("Light")){
                                  System.out.println("Heavy: " + tentativePeaks[1].size());
                          }       
                          else{
                              System.out.println("Light: " + tentativePeaks[1].size());                      
                          }
                          addedCounter=0;
                              int peaksSize=tentativePeaks[0].size();
                              if(peaksSize>0){
                               //First we will assign original peaks.
                              
                              for(int kk=0; kk<peaksSize;kk++){  
                                  
                                    float retentionTime=-1;
                                    float mzValue=-1;
                                    float intValue=-1;
                                    String info=tentativePeaks[0].elementAt(kk).toString();
                                    String[] chunks= new String[3];
                                    chunks=info.split(":");
                                    retentionTime=Float.valueOf(chunks[0]).floatValue();
                                    mzValue=Float.valueOf(chunks[1]).floatValue();
                                    intValue=Float.valueOf(chunks[2]).floatValue();
                                    
                                    xLcMsCorr corr= new xLcMsCorr(myLoadData.getFileName(),kk, mzValue,intValue,typeP, retentionTime);                                    
                                    ret.addCorrLcMsData(pepId, pepSeq, corr);  
                                    addedCounter++;
                                    
                              }
                               //Let's assign matched peaks   
                              peaksSize=tentativePeaks[1].size();
                               for(int kk=0;kk<peaksSize;kk++){
                                    float retentionTime=-1;
                                    float mzValue=-1;
                                    float intValue=-1;
                                    String info=tentativePeaks[1].elementAt(kk).toString();
                                    String[] chunks= new String[3];
                                    chunks=info.split(":");
                                    retentionTime=Float.valueOf(chunks[0]).floatValue();
                                    mzValue=Float.valueOf(chunks[1]).floatValue();
                                    intValue=Float.valueOf(chunks[2]).floatValue();
                                    String peptideType="";
                                   if(typeP.equals("Light")){
                                        peptideType="Heavy";
                                   } 
                                   else{
                                        peptideType="Light";
                                   }
                                    xLcMsCorr corr= new xLcMsCorr(myLoadData.getFileName(),kk, mzValue,intValue,peptideType,retentionTime);                                    
                                    ret.addCorrLcMsData(pepId, pepSeq, corr);
                                    addedCounter++;
                                    //   System.out.println(" --> Heavy " +  vals[1]);
                                }
                               }
                              
                    
                          
                          

 
                         
                         /* for(int kk=0;kk<tentativePeaks[1].size();kk++){
                            System.out.println(" --> Light " + tentativePeaks[1].elementAt(kk));
                          }*/
                          System.out.println("Total number of elem added:" + addedCounter);
                          totalAdded+=addedCounter;
                          
               }
                          
                          
/*                          System.out.println("Heavy: " + tentativePeaks[0].size());
                          System.out.println("Light: " + tentativePeaks[1].size());
                          System.out.println("Original: " + tentativePeaks[2].size());
                          System.out.println("Heavy: " + tentativePeaks[0].size());
addedCounter=0;
                          if(tentativePeaks[0].size() ==0){
                              if(tentativePeaks[1].size() >0){
                                //This means that some Light peaks have been found to correspond to the theoretical
                               //masses of the peptide. An envelope of peaks around the parent Ion MZ of the peptide will be labelled
                               //as light versions.
                              int peaksSize=tentativePeaks[2].size();
                              if(peaksSize>0){
                               //First we will assign Heavy peaks (original peaks were obviously labelled as heavy).
                              
                              for(int kk=0; kk<peaksSize;kk++){  
                                  
                                    float retentionTime=-1;
                                    float mzValue=-1;
                                    float intValue=-1;
                                    String info=tentativePeaks[2].elementAt(kk).toString();
                                    String[] chunks= new String[3];
                                    chunks=info.split(":");
                                    retentionTime=Float.valueOf(chunks[0]).floatValue();
                                    mzValue=Float.valueOf(chunks[1]).floatValue();
                                    intValue=Float.valueOf(chunks[2]).floatValue();
                                    
                                    xLcMsCorr corr= new xLcMsCorr(myLoadData.getFileName(),kk, mzValue,intValue,"Heavy", retentionTime);                                    
                                    ret.addCorrLcMsData(pepId, pepSeq, corr);  
                                    addedCounter++;
                                    
                              }
                               //Let's assign Light peaks   
                              peaksSize=tentativePeaks[1].size();
                               for(int kk=0;kk<peaksSize;kk++){
                                    float retentionTime=-1;
                                    float mzValue=-1;
                                    float intValue=-1;
                                    String info=tentativePeaks[1].elementAt(kk).toString();
                                    String[] chunks= new String[3];
                                    chunks=info.split(":");
                                    retentionTime=Float.valueOf(chunks[0]).floatValue();
                                    mzValue=Float.valueOf(chunks[1]).floatValue();
                                    intValue=Float.valueOf(chunks[2]).floatValue();
                                    
                                    xLcMsCorr corr= new xLcMsCorr(myLoadData.getFileName(),kk, mzValue,intValue,"Light",retentionTime);                                    
                                    ret.addCorrLcMsData(pepId, pepSeq, corr);
                                    addedCounter++;
                                    //   System.out.println(" --> Heavy " +  vals[1]);
                                }
                               }
                              }
                          
                          
                          }
                          else if(tentativePeaks[1].size()==0){
                              
                              
                              int peaksSize=tentativePeaks[2].size();
                              if(peaksSize>0){
                               //First we will assign Light peaks (original peaks were labelled as light).
                              
                              for(int kk=0; kk<peaksSize;kk++){  
                                  
                                    float retentionTime=-1;
                                    float mzValue=-1;
                                    float intValue=-1;
                                    String info=tentativePeaks[2].elementAt(kk).toString();
                                    String[] chunks= new String[3];
                                    chunks=info.split(":");
                                    retentionTime=Float.valueOf(chunks[0]).floatValue();
                                    mzValue=Float.valueOf(chunks[1]).floatValue();
                                    intValue=Float.valueOf(chunks[2]).floatValue();
                                    
                                    xLcMsCorr corr= new xLcMsCorr(myLoadData.getFileName(),kk, mzValue,intValue,"Light",retentionTime);                                    
                                    ret.addCorrLcMsData(pepId, pepSeq, corr);  
                                    addedCounter++;
                                    
                              }
                               //Let's assign Heavy peaks   
                              peaksSize=tentativePeaks[0].size();
                               for(int kk=0;kk<peaksSize;kk++){
                                    float retentionTime=-1;
                                    float mzValue=-1;
                                    float intValue=-1;
                                    String info=tentativePeaks[0].elementAt(kk).toString();
                                    String[] chunks= new String[3];
                                    chunks=info.split(":");
                                    retentionTime=Float.valueOf(chunks[0]).floatValue();
                                    mzValue=Float.valueOf(chunks[1]).floatValue();
                                    intValue=Float.valueOf(chunks[2]).floatValue();
                                    
                                    xLcMsCorr corr= new xLcMsCorr(myLoadData.getFileName(),kk, mzValue,intValue,"Heavy",retentionTime);                                    
                                    ret.addCorrLcMsData(pepId, pepSeq, corr);
                                    
                                    addedCounter++;
                                    
                                    //   System.out.println(" --> Heavy " +  vals[1]);
                                }
                               }
                          
                          
                          }
                          else{
                            //Some peaks match both Heavy and Light variants of the peptide's theoretical
                            //mass therefore nothing can be said about the initial peak (so all these peaks are 
                            //  discarded)
                              
                          
                          }
                          
                          

 
                         
                         // for(int kk=0;kk<tentativePeaks[1].size();kk++){
                          //  System.out.println(" --> Light " + tentativePeaks[1].elementAt(kk));
                         // }
                          System.out.println("Total number of elem added:" + addedCounter);
                          totalAdded+=addedCounter;
 */                          

                
                                         System.out.println("GLOBAL number of elem added:" + totalAdded);
               
            }    
        
        }
        
            
      
        

        
        
        return ret;
    }
    
    /**
     * Opens the dataFile xml file and loads mass shifts,the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     * @return ret an array of float mass shifts.
     */
    
    public float[] loadParams(String dataFile){
        //mass shifts.
        //the order is the following:
        /*
         * massShifts[0] <-- C-terminus (daltons) 
         * massShifts[1] <-- N-terminus (daltons)
         * massShifts[2] <-- A  (daltons)
         * massShifts[3] <-- R (daltons)
         * massShifts[4] <-- N (daltons)
         * massShifts[5] <-- D (daltons)
         * massShifts[6] <-- C (daltons)
         * massShifts[7] <-- E (daltons)
         * massShifts[8] <-- Q (daltons)
         * massShifts[9] <-- G (daltons)
         * massShifts[10] <-- H (daltons)
         * massShifts[11] <-- I (daltons)
         * massShifts[12] <-- L (daltons)
         * massShifts[13] <-- K (daltons)
         * massShifts[14] <-- M (daltons)
         * massShifts[15] <-- F (daltons)
         * massShifts[16] <-- P (daltons)
         * massShifts[17] <-- S (daltons)
         * massShifts[18] <-- T (daltons)
         * massShifts[19] <-- W (daltons)
         * massShifts[20] <-- Y (daltons)
         * massShifts[21] <-- V (daltons)
         */

        int i=0;
        for(i=0;i<massShifts.length;i++){
            massShifts[i]=0;
        }
        
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
                   if(item.getNodeName().equals("C_term")){
                        massShifts[0]=Float.valueOf(item.getTextContent());
                        
                   }
                   else if(item.getNodeName().equals("N_term")){
                                        massShifts[1]=Float.valueOf(item.getTextContent());
                          
                   }
                   else if(item.getNodeName().equals("mass_type")){
                       mass_type=item.getTextContent();
                   }
                   else if(item.getNodeName().equals("aminoacid")){ 
                                        //Should already be a character but...
                                        switch(item.getAttributes().item(0).getTextContent().charAt(0)){
                                            case 'A': {
                                                index=2;
                                                break;                
                                                }
                                            
                                            case 'R': {
                                                index=3;
                                                break;                
                                                }
                                            
                                           case 'N': {
                                                index=4;
                                                break;                
                                                }
                                           case 'D': {
                                                index=5;
                                                break;                
                                                }
                                           case 'C': {
                                                index=6;
                                                break;                
                                                }
                                           case 'E': {
                                                index=7;
                                                break;                
                                                }
                                           case 'Q': {
                                                index=8;
                                                break;                
                                                }
                                           case 'G': {
                                                index=9;
                                                break;                
                                                }
                                           case 'H': {
                                                index=10;
                                                break;                
                                                }
                                           case 'I': {
                                                index=11;
                                                break;                
                                                }
                                           case 'L': {
                                                index=12;
                                                break;                
                                                }
                                           case 'K': {
                                                index=13;
                                                break;                
                                                }
                                           case 'M': {
                                                index=14;
                                                break;                
                                                }
                                           case 'F': {
                                                index=15;
                                                break;                
                                                }
                                           case 'P': {
                                                index=16;
                                                break;                
                                                }
                                           case 'S': {
                                                index=17;
                                                break;                
                                                }
                                           case 'T': {
                                                index=18;
                                                break;                
                                                }
                                           case 'W': {
                                                index=19;
                                                break;                
                                                }                                                
                                                
                                           case 'Y': {
                                                index=20;
                                                break;                
                                                }
                                           case 'V': {
                                                index=21;
                                                break;                
                                                }
                                            default : {
                                                System.err.println("ERROR: aminoacid (" + item.getAttributes().item(0).getTextContent().charAt(0) +") in " + dataFile + " not recognized!");
                                                System.exit(1);
                                            
                                            }
                                        
                                        }
                                        
                                        massShifts[index]=Float.valueOf(item.getTextContent());
                                        
                                        
                                        index++;
                   }
                   else if(item.getNodeName().equals("RT_window")){
                            retTimeWindow=Float.valueOf(item.getTextContent());
                   
                   }
                   else if(item.getNodeName().equals("mz_tolerance")){
                            mzTolerance=Float.valueOf(item.getTextContent());
                   
                   }
                }
            }
            
            
        }
        catch(Exception e){
            System.out.println("Exception while reading " + dataFile+ "\n" + e);
            System.exit(1);
        }
        
        return massShifts;
    }
    
    
    /**
     * Given the peptide sequence and a vector of mass shifts 
     * @param pepSeq
     * @return the mass shift relative to the peptide according to mass shifts contained
     * in <code>massShifts</code>
     */
    public float computePeptideMassShift(String pepSeq){
        float ret=0f;
        int i=0;
        int index=0;
//        int charge=1;
        char aminoacid;
        //I have to add C-Terminus and N-terminus shifts if any!
        ret=ret+massShifts[0]+massShifts[1];
        for(i=0;i<pepSeq.length();i++){
            aminoacid=pepSeq.charAt(i);
            switch(aminoacid){
                      case 'A': {
                          index=2;
                          break;                
                       }
                       case 'R': {
                          index=3;
                            //H,R,K increase are each one + charge 
                 //           charge++;
                          break;                
                        }
                       case 'N': {
                            index=4;
                            break;                
                            }
                       case 'D': {
                            index=5;
                            break;                
                            }
                       case 'C': {
                            index=6;
                            break;                
                            }
                       case 'E': {
                            index=7;
                            break;                
                            }
                       case 'Q': {
                            index=8;
                            break;                
                            }
                       case 'G': {
                            index=9;
                            break;                
                            }
                       case 'H': {
                            index=10;
                            //H,R,K increase are each one + charge 
               //             charge++;
 
                            break;                
                            }
                       case 'I': {
                            index=11;
                            break;                
                            }
                       case 'L': {
                            index=12;
                            break;                
                            }
                       case 'K': {
                            index=13;
                            //H,R,K increase are each one + charge 
             //               charge++;
 
                            break;                
                            }
                       case 'M': {
                            index=14;
                            break;                
                            }
                       case 'F': {
                            index=15;
                            break;                
                            }
                       case 'P': {
                            index=16;
                            break;                
                            }
                       case 'S': {
                            index=17;
                            break;                
                            }
                       case 'T': {
                            index=18;
                            break;                
                            }
                       case 'W': {
                            index=19;
                            break;                
                            }                                                

                       case 'Y': {
                            index=20;
                            break;                
                            }
                       case 'V': {
                            index=21;
                            break;                
                            }
                        default : {
                            System.err.println("ERROR (computePeptideMassShift): aminoacid (" + aminoacid +")  not recognized!");
                            System.exit(1);

                        }
        
            }
            ret+=massShifts[index];
            
        }
        
//        System.out.println("Sequence: "+pepSeq+" shift: "+ ret + " charge:" +charge);
 
        
//        return ret/charge;
  return ret;      
    }
    
    
    
    
    public Vector[] computeCorrespondences(float rt, float pMz, xLoadData data, float shift){
        
        
        
        
        Vector[] res = new Vector[3];
        res[0] = new Vector<String>(); //candidate Heavy peaks (format RT:mz:int)
        res[1] = new Vector<String>(); //candidate Light peaks (format RT:mz:int)
        res[2] = new Vector<String>(); //envelope of the original peaks (format RT:mz:int)
        
        xLcMsData myLcMsData;
//        float[] vals = new float[3];
       
            for(int i=0;i<data.getLcMsDataSize();i++){
                myLcMsData=data.getLcMsDataElemAt(i);
//                System.out.println("I:" +i);
              //  System.out.println("Data size:" + myLcMsData.getSize());
                
                    
                int[] candidatePeaks=data.getLcMsIndexOfAllBetweenRT(rt-retTimeWindow, rt+retTimeWindow);
                for(int j=candidatePeaks[0];j<candidatePeaks[1];j++){
                            xLcMsData vals=data.getLcMsDataElemAt(j);
                            float retTime=vals.getRetTime();
                            float elements[][]=vals.getSpectrum().getSubspectrumBetween(pMz+ shift - mzTolerance, pMz + shift + mzTolerance);
                            int elSize=-1;
                            if(! (elements==null)){
                                elSize=elements.length;
                            }
                            else{
                                elSize=-1;
                            }
                             for(int k=0;k<elSize;k++){
                                //OK peak can be possibly assigned as a Heavy peak
                                res[0].add(retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                            
                            }
                            
                            elements=vals.getSpectrum().getSubspectrumBetween(pMz- shift - mzTolerance, pMz - shift + mzTolerance);
                            
                            if(! (elements==null)){
                                elSize=elements.length;
                            }
                            else{
                                elSize=-1;
                            }
                            
                            for(int k=0;k<elSize;k++){
                                //OK peak can be possibly assigned as a Light peak
                                res[1].add(retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                            
                            }

                            elements=vals.getSpectrum().getSubspectrumBetween(pMz- mzTolerance, pMz + mzTolerance);
                            
                            if(! (elements==null)){
                                elSize=elements.length;
                            }
                            else{
                                elSize=-1;
                            }
                            
                            for(int k=0;k<elSize;k++){
                                //OK peak can be possibly assigned as the original peak
                                res[2].add(retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                            
                            }
                            
                            
 /*                           if((vals[1] >= pMz+ shift - mzTolerance ) && (vals[1] <= pMz + shift + mzTolerance )){
                            //OK peak can be possibly assigned as a Heavy peak 
                            res[0].add(i + ":" + j);
                        }
                        else if((vals[1] >= pMz- shift - mzTolerance ) && (vals[1] <= pMz - shift + mzTolerance )){
                            //OK peak can be possibly assigned as a Heavy peak 
                            res[1].add(i + ":" + j);
                        }
                        
                       if((vals[1]>= pMz -mzTolerance)&&(vals[1]<= pMz +mzTolerance)){
                        //Peak can be assigned to the original envelope (even if at this stage we are not sure if it is 
                        //Heavy or light.   
                       res[2].add(i + ":" + j);
                       }     
   */             
                }
                
/*                for(int j=0; j<myLcMsData.getSize();j++){
                    vals=myLcMsData.getElemAt(j);
                       //  System.out.println("J:" +j);
                    if((vals[0]>= rt - retTimeWindow) && (vals[0]<= rt + retTimeWindow)){
                        if((vals[1] >= pMz+ shift - mzTolerance ) && (vals[1] <= pMz + shift + mzTolerance )){
                            //OK peak can be possibly assigned as a Heavy peak 
                            res[0].add(i + ":" + j);
                        }
                        else if((vals[1] >= pMz- shift - mzTolerance ) && (vals[1] <= pMz - shift + mzTolerance )){
                            //OK peak can be possibly assigned as a Heavy peak 
                            res[1].add(i + ":" + j);
                        }
                
                }
            } */
        
         }
        
        return res;
    
    }
    
    public float computeMass(String mySeq){
  
     //Let's take into account the water molecule and the H+ while computing the peptide mass.   
     float ret=18.0153f;   
     ret=ret+1.00794f;
//     float ret=0;
     int i=0;
     
  
/*
#######
# MASSES:  monoisotopic,average
#######
#	'G' => 57.021464, 57.0519
#	'A' => 71.037114, 71.0788
#	'S' => 87.032029, 87.0782
#	'P' => 97.052764, 97.1167
#	'V' => 99.068414, 99.1326
#	'T' => 101.04768, 101.1051
#	'C' => 103.00919, 103.1388
#	'L' => 113.08406, 113.1594
#	'I' => 113.08406, 113.1594
#	'N' => 114.04293, 114.1038
#	'D' => 115.02694, 115.0886
#	'Q' => 128.05858, 128.1307
#	'K' => 128.09496, 128.1741
#	'E' => 129.04259, 129.1155
#	'M' => 131.04048, 131.1926
#	'H' => 137.05891, 137.1411
#	'F' => 147.06841, 147.1766
#	'R' => 156.10111, 156.1875
#	'Y' => 163.06333, 163.1760
#	'W' => 186.07931, 186.2132
#	);
 
*/    
     
     
    
    boolean average=mass_type.toLowerCase().equals("average");
 //   System.out.println("String:" + mySeq);
    for(i=0;i<mySeq.length();i++){
        switch (mySeq.toUpperCase().charAt(i)) {
            case 'G' : {
                        if(average){
                            ret+=57.0519;
                        }
                        else{
                            ret+=57.021464;
                        }
                        break;
                      }
            case 'A' : {
                       if(average){
                            ret+=71.0788;
                        }
                        else{
                             ret+=71.037114;
                        }
                        break;
                      }
           case 'S' : {
                        if(average){
                            ret+=87.0782;
                        }
                        else{
                            ret+=87.032029;
                        }    
                         break;
                      }
           case 'P' : {
                        if(average){
                            ret+=97.1167;
                        }
                        else{               
                            ret+=97.052764;
                        }
                        break;
                      }
           case 'V' : {
                       if(average){
                            ret+=99.1326;
                        }
                        else{               
                            ret+=99.068414;
                        }
                        break;
                      }
           case 'T' : {
                       if(average){
                            ret+=101.1051;
                        }
                        else{               
                            ret+=101.04768;
                        }
                        break;
                      }
          case 'C' : {
                       if(average){
                            ret+=103.1388;
                        }
                        else{               
                            ret+=103.00919;
                        }
                        break;
                     
                      }
          case 'L' : {
                        if(average){
                            ret+=113.1594;
                        }
                        else{               
                            ret+=113.08406;
                        }
                        break;
                      }
         case 'I' : {
                        if(average){
                            ret+=113.1594;
                        }
                        else{               
                            ret+=113.08406;
                        }
                        break;
                      }
            case 'N' : {
                        if(average){
                            ret+=114.1038;
                        }
                        else{               
                            ret+=114.04293;
                        }
                        
                        break;
                      }
           case 'D' : {
                        if(average){
                            ret+=115.0886;
                        }
                        else{               
                            ret+=115.02694;
                        }
                        
                        break;
                      }
           case 'Q' : {
                        if(average){
                            ret+=128.1307;
                        }
                        else{               
                            ret+=128.05858;
                        }
                        
                        break;
                      }
           case 'K' : {
                        if(average){
                            ret+=128.17412;
                        }
                        else{               
                            ret+=128.09496;
                        }
                        
                        break;
                      }
           case 'E' : {
                        
                        if(average){
                            ret+=129.1155;
                        }
                        else{               
                            ret+=129.04259;
                        }        
                        
                        break;
                      }
          case 'M' : {
                        if(average){
                            ret+=131.1926;
                        }
                        else{               
                            ret+=131.04049;
                        }
                        
                        break;
                      }
          case 'H' : {
                        if(average){
                            ret+=137.1411;
                        }
                        else{               
                            ret+=137.05891;
                        }                  
                        
                        break;
                      }
          case 'F' : {
                        if(average){
                            ret+=147.1766;
                        }
                        else{               
                            ret+=147.06841;
                        }
                        
                        break;
                      }
           case 'R' : {
                        if(average){
                            ret+=156.1875;
                        }
                        else{               
                            ret+=156.10111;
                        }
                        
                        break;
                      }
          case 'Y' : {
                        if(average){
                            ret+=163.1760;
                        }
                        else{               
                            ret+=163.06333;
                        }                        
                        
                        break;
                      }
          case 'W' : {  
                        if(average){
                            ret+=186.2132;
                        }
                        else{               
                            ret+=186.07931;
                        }
                        
                        break;
                      }
        }       
    
    }

    return ret;
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
        /**
         * Computes the peptide type (i.e. "Heavy" or "Light" given the sequence and the parent ion Mass.
         * @param pepSeq the peptide sequence.
         * @param parIonMz the parent ion m/z
         * @param shift the shift between Light and Heavy version of the peptide.
         * @return ret a string with the type of the peptide.
         */
        public String computePeptideType(String pepSeq, float parIonMz, float shift, int charge){
            String ret="";
            float thMass=computeMass(pepSeq);
            float parMass=parIonMz*charge;
            
            System.out.println("Computing type on: " + pepSeq + " " + parIonMz + " " + shift + " th. mass:" + thMass);
            
            if(((thMass - parMass)<=2) && ((thMass - parMass)>=-2)){
                ret="Light";
            }
            else{
                if((thMass + shift - parMass)<2 && ((thMass + shift - parMass)>-2)){
                   ret="Heavy"; 
                }
                else{
                    System.out.println("Error (metaboliclabeling.jar) in computePeptideType: " + pepSeq +" cannot be associated to any known type.");
                    System.exit(1);
                }
           }
                
            
            return ret;
        
        }
        
     public void createMsTree(xLoad input){
        /* int file=-1;
         float rt=0f;
         int spectrum=-1;
         int scan=-1;
         float[] vals=new float[3];
         int[] elem= new int[3]; 
         xLoadData data=null;
         xLcMsData lcMsData=null;
         int inputDataSize=input.getDataSize();
         for(int i=0;i<inputDataSize;i++){
            file=i;
            data= input.getDataElemAt(i);
                                System.out.println("i:" + i);
            int dataSize=data.getLcMsDataSize();                    
            for(int j=0;j<dataSize;j++){
                lcMsData=data.getLcMsDataElemAt(j);
                scan=j;
                                    System.out.println(" j:" + j);
                 int siz=lcMsData.getSize();                   
                for(int k=0;k<siz;k++){
                   
                    vals=lcMsData.getElemAt(k);
                    rt=vals[0];
                    spectrum=k;
                    elem[0]=file;
                    elem[1]=scan;
                    elem[2]=spectrum;
                 //   lcMsTree.put(k, k);

                }     
            }
         
         }
         
*/
     
     }
        
     
       public Vector[] computePeakCorrespondences(float rt, float pMz, xLoadData data, float shift, String type){
        
        
        int sign=0; //this variable will be used to twick the value of the shift depending on if the original
                    //peptide is known to be an heavy or light version.
        
        if(type.equals("Light")){
            sign=1;
        }
        else{
            sign=-1;
        
        }
        
        Vector[] res = new Vector[2];
        res[0] = new Vector<String>(); //envelope of original peaks (format RT:mz:int)
        res[1] = new Vector<String>(); //candidate matching peaks (format RT:mz:int)
        
        xLcMsData myLcMsData;
//        float[] vals = new float[3];
       
        System.out.println("Ret time (original): " + (rt-retTimeWindow) + " - " + (rt+retTimeWindow) + " Mass: " + (pMz - mzTolerance) + " - " + (pMz + mzTolerance));
        System.out.println("Ret time (matching): " + (rt-retTimeWindow) + " - " + (rt+retTimeWindow) + " Mass: " + (pMz + sign*shift- mzTolerance) + " - " + (pMz+ sign*shift + mzTolerance));
    //    for(int i=0;i<data.getLcMsDataSize();i++){
    //            myLcMsData=data.getLcMsDataElemAt(i);
//                System.out.println("I:" +i);
              //  System.out.println("Data size:" + myLcMsData.getSize());
                
                   
                
                int[] candidatePeaks=data.getLcMsIndexOfAllBetweenRT(rt-retTimeWindow, rt+retTimeWindow);
                for(int j=candidatePeaks[0];j<=candidatePeaks[1];j++){
                            xLcMsData vals=data.getLcMsDataElemAt(j);
                            float retTime=vals.getRetTime();
                            //ok let's find the original peaks
                            float elements[][]=vals.getSpectrum().getSubspectrumBetween(pMz- mzTolerance, pMz + mzTolerance);
                            int elSize=-1;
                            if(! (elements==null)){
                                elSize=elements.length;
                            }
                            else{
                                elSize=-1;
                            }
                             for(int k=0;k<elSize;k++){
                                //OK adding the original peak
                                System.out.println("Original Values(rt,mz,int): " +retTime + ":" + elements[k][0] +":"+ elements[k][1]); 
                                res[0].add(retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                            
                            }
                            

                            //Let's compute the matching peaks now.
                            elements=vals.getSpectrum().getSubspectrumBetween(pMz+ sign*shift - mzTolerance, pMz + sign*shift + mzTolerance);
                            elSize=-1;
                            if(! (elements==null)){
                                elSize=elements.length;
                            }
                            else{
                                elSize=-1;
                            }
                            
                            for(int k=0;k<elSize;k++){
                                //OK adding matching peak
                                System.out.println("Matching Values(rt,mz,int): " +retTime + ":" + elements[k][0] +":"+ elements[k][1]); 
                                res[1].add(retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                            
                            }

             
                }
                
/*                for(int j=0; j<myLcMsData.getSize();j++){
                    vals=myLcMsData.getElemAt(j);
                       //  System.out.println("J:" +j);
                    if((vals[0]>= rt - retTimeWindow) && (vals[0]<= rt + retTimeWindow)){
                        if((vals[1] >= pMz+ shift - mzTolerance ) && (vals[1] <= pMz + shift + mzTolerance )){
                            //OK peak can be possibly assigned as a Heavy peak 
                            res[0].add(i + ":" + j);
                        }
                        else if((vals[1] >= pMz- shift - mzTolerance ) && (vals[1] <= pMz - shift + mzTolerance )){
                            //OK peak can be possibly assigned as a Heavy peak 
                            res[1].add(i + ":" + j);
                        }
                
                }
            } */
        
      //   }
        
        return res;
    
    }
     
     

   
    private float[] massShifts = new float[22];
    private float retTimeWindow=0f;
    private float mzTolerance=0f;
    private String mass_type="";
    private final static String name = "Metabolic Labeling";
    private final static String version = "1.00";
    private final static String type = "PEAKSEL_plugin";
    private final static String description = "Works from MS/MS data loaded into xLoad structure.\n\tFor each peptide identified it retrieves y5 and b6 ions and considers y ions as Labelled Heavy, while b ions as Labelled Light.\n\tIt finally populates the xPeaks structure.";
}