package xtracker;

import java.util.*;
/**
 * xPeaks is the data structure populated by peakSelPlugins.  
 * <p> 
 * It is constituted by a  vector of entries <code>peptideCorrespondenceData</code> representing correspondence data (that is, what LC/MS or MS/MS peak in what file correspond to the peptide).
 * @see peakSelPlugin
 * @see xCorrespondenceData
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xPeaks {
    
      /**
       * The constructor. Creates an empty xLoad object 
      */
    public xPeaks(){
            
        
        peptideCorrespondenceData = new Vector<xCorrespondenceData>();
     }
    
    /**
     * Gets the size of the correspondence data structure
     * @return size the size of the correspondence data structure 
     * @see xCorrespondenceData
     */
    public int getSize(){
        
        return peptideCorrespondenceData.size();
    }
    
    
    /**
     * Gets the index-th element in the correspondence data structure
     * @param index is the index of the element to retrieve
     * @return the xCorrespondenceData element at index <code>index</code> in the peptideCorrespondenceData structure. 
     * Returns null if index exceed Vector dimensions.
     * @see xCorrespondenceData
     */
    public xCorrespondenceData getElemAt(int index){
        if(!(index>peptideCorrespondenceData.size())){
            return peptideCorrespondenceData.elementAt(index);
    
        }
        else{
            return null;
        }
    }
    
    /**
     * Adds an element to the <code>peptideCorrespondenceData</code> data structure.
     * @param pepId is the peptide identifier to which the correspondence has to be added
     * @param pepSeq is the peptide sequence to which the correspondence has to be added
     * @param x the xMsMsCorrData to be added
     
     */
    public void addCorrMsMsData(String pepId, String pepSeq, xLcMsMsCorr x){
         xCorrespondenceData myCorr= getCorr(pepId,pepSeq);
         
         if(myCorr==null){
            myCorr= new xCorrespondenceData(pepId,pepSeq);
            myCorr.addMsMsCorr(x);
            peptideCorrespondenceData.add(myCorr);
         }
         else{
            myCorr.addMsMsCorr(x);
         }
        
    }
    
    
        /**
     * Adds an element to the <code>peptideCorrespondenceData</code> data structure.
     * @param pepId is the peptide identifier to which the correspondence has to be added
     * @param pepSeq is the peptide sequence to which the correspondence has to be added
     * @param x the xLcMsCorrData to be added
     
     */
    public void addCorrLcMsData(String pepId, String pepSeq, xLcMsCorr x){
         xCorrespondenceData myCorr= getCorr(pepId,pepSeq);
         
         if(myCorr==null){
            myCorr= new xCorrespondenceData(pepId,pepSeq);
            myCorr.addLcMsCorr(x);
            peptideCorrespondenceData.add(myCorr);
         }
         else{
            myCorr.addLcMsCorr(x);
         }
        
    }
    
    /**
     * Gets the xCorrespondenceData associated to the peptide identifier pepId and to the peptide sequence pepSeq
     * @param pepId the peptide identifier
     * @param pepSeq the peptide sequence
     * @return xCorrespondenceData if found, null otherwise
     */
    public xCorrespondenceData getCorr(String pepId,String pepSeq){
        xCorrespondenceData ret =null;
        xCorrespondenceData tmp =null;
        int i=0;
        while(i<getSize() && ret==null){
            tmp=getElemAt(i);
            if(tmp.getPeptideId().equals(pepId) && tmp.getPeptideSeq().equals(pepSeq)){
                ret=tmp;
            }
            i++;
        }
        return ret;
    }
    
    /**
     * Checks the validity of the xLoad structure.
     * <p>
     * The structure is valid if the following holds:
     * <ul>
     * <li>The peptideCorrespondenceData structure is not empty</li>
     * <li>Every element of the peptideCorrespondenceData structure is a valid element</li> 
     * </ul>
     * @return true if the structure is valid false otherwise.
     */
    public boolean isValid(){
    
        xCorrespondenceData myTmpData=null;
        boolean intermediateData=true;
        int i=0;
        if(peptideCorrespondenceData.size()>0){
            for(i=0; i<peptideCorrespondenceData.size();i++){
                myTmpData=this.getElemAt(i);
                intermediateData= intermediateData && myTmpData.isValid();
                
            }
            
            return intermediateData;
        }
        else{
            return false;
        }
    
    }
    

     
     
     /**
     * The structure of correspondence data
     * @see rawInputData
      * @see xCorrespondenceData
     */
    public Vector<xCorrespondenceData> peptideCorrespondenceData;

}
