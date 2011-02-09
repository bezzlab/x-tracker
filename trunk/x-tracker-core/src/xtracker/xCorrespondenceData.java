package xtracker;


import java.util.*;
/**
 * xCorrespondenceData is the data structure containing correspondences between rawData peak lists and peptides. <b>Please note that the combination proteinID, peptideSeq MUST be unique for each fileName</b>
 * <p>
 * It is arranged in peptides - that is - for each peptide a matrix containing the correspondence of MS/MS peaks or LC/MS peaks
 * across all raw data files is created.
 * @see peakSelPlugin
 * @see xLoad
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xCorrespondenceData {

    /**
     * The constructor.
     * @param protId is the protein identifier.
     * @param pepSeq is the peptide sequence.
     */
    public xCorrespondenceData(String protId,String pepSeq){
        proteinId=protId;
        peptideSeq=pepSeq;
        lcMsMsCorr = new Vector<xLcMsMsCorr>();
        lcMsCorr = new Vector<xLcMsCorr>();
    }
    
    /**
     * Gets the proteinId this set of correspondences refers to.
     * @return proteinId, the identificator of the peptide the correspondences refer to.
     */
    public String getProteinId(){
        return proteinId;
        
    }
    
    /**
     * Gets the sequence of the peptide this set of correspondences refers to.
     * @return peptideSeq, the sequence of the peptide the correspondences refer to.
     */
    public String getPeptideSeq(){
        return peptideSeq;
        
    }
    
        /**
     * Sets the proteinId of the peptide this set of correspondences refers to.
     * @param p the identificator of the protein the correspondences refer to.
     */
    public void setProteinId(String p){
         proteinId=p;
        
    }
    
     /**
     * Sets the sequence of the peptide this set of correspondences refers to.
      * @param seq is a string with the sequence of the peptide.
     */
    public void setPeptideSeq(String seq){
        peptideSeq=seq;
        
    }
    
    /**
     * Gives the size of the LC/MS correspondences of the current peptide.
     * @return size is an int representing the size of the LC/MS correspondences 
     */
    public int getLcMsCorrSize(){
        return lcMsCorr.size();
    
    }
    
    /**
     * Gives the size of the MS/MS correspondences of the current peptide.
     * @return size is an int representing the size of the MS/MS correspondences 
     */
    public int getLcMsMsCorrSize(){
        return lcMsMsCorr.size();
    
    }
    

    /**
     * Adds a  MS/MS correspondence of the current peptide.
     * @param corrElem is a MS/MS correspondence
     * @see  xLcMsMsCorr
     */
    public void addLcMsMsCorr(xLcMsMsCorr corrElem){
        lcMsMsCorr.addElement(corrElem);
    
    }
    
    /**
     * Adds a  LC/MS correspondence of the current peptide.
     * @param corrElem is a LC/MS correspondence
     * @see  xLcMsCorr
     */
    public void addLcMsCorr(xLcMsCorr corrElem){
        lcMsCorr.addElement(corrElem);
    
    }
    
    
     /**
     * Gets a  MS/MS correspondence of the current peptide.
     * @param index is the index of the element to retrieve.
     * @return lcMsMsCorr is the MS/MS correspondence element requested. If the index
      * is higher than the number of elements in the Vector it returns null.
     * @see  xLcMsMsCorr
     */
    public xLcMsMsCorr getLcMsMsCorrElemAt(int index){
        if(!(index > lcMsMsCorr.size())){
            return lcMsMsCorr.elementAt(index);
        }
        else{
            return null;        
        }
    }
    
     /**
     * Gets a  MS/MS correspondence of the current peptide.
     * @param index is the index of the element to retrieve.
     * @return lcMsMsCorr is the MS/MS correspondence element requested. If the index
      * is higher than the number of elements in the Vector it returns null.
     * @see  xLcMsMsCorr
     */
    public xLcMsCorr getLcMsCorrElemAt(int index){
        if(!(index > lcMsCorr.size())){
            return lcMsCorr.elementAt(index);
        }
        else{
            return null;
        }
    
    }

      /**
       * Gets the array of unique labels of the LC-MS correspondence data.
       * @return an array of unique strings representing the unique labels in the LC-MS correspondence data.
       */
      public String[] getUniqueLabelsOfLcMsCorr(){
            String[] ret=null;
            Vector<String> tmp=new Vector<String>();
            int i=-1;
            for(i=0;i<this.getLcMsCorrSize();i++){
                xLcMsCorr myCorr=this.getLcMsCorrElemAt(i);
                String myLabel=myCorr.getLabel();
                if(!tmp.contains(myLabel)){
                    tmp.addElement(myLabel);
                }
            }

            ret= new String[tmp.size()];
            for(i=0;i<tmp.size();i++){
                ret[i]=tmp.elementAt(i);
            }

            return ret;

    }
         /**
       * Gets the array of unique labels of the LC-MS/MS correspondence data.
       * @return an array of unique strings representing the unique labels in the LC-MS/MS correspondence data.
       */
      public String[] getUniqueLabelsOfLcMsMsCorr(){
            String[] ret=null;
            Vector<String> tmp=new Vector<String>();
            int i=-1;
            for(i=0;i<this.getLcMsCorrSize();i++){
                xLcMsMsCorr myCorr=this.getLcMsMsCorrElemAt(i);
                String myLabel=myCorr.getLabel();
                if(!tmp.contains(myLabel)){
                    tmp.addElement(myLabel);
                }
            }

            ret= new String[tmp.size()];
            for(i=0;i<tmp.size();i++){
                ret[i]=tmp.elementAt(i);
            }

            return ret;

    }

/**
 * Checks the validity of the xCorrespondenceData element
 * <p>  
 * The structure is valid if the following holds:
 * <ul>
 * <li>The peptide id is not empty</li>
 * <li>The peptide sequence is not empty</li>
 * <li>At least one of lcMsMsCorr or lcMsCorr are not empty</li>
 * </ul>
 * @return true if the structure is valid, false otherwise.
 */
    public boolean isValid(){
        boolean peptide_check;
        boolean corr_check;
        peptide_check = (this.getProteinId().length()>0) && (this.getPeptideSeq().length()>0);
        corr_check = (this.getLcMsCorrSize() >0 ) || (this.getLcMsMsCorrSize() > 0);
        return peptide_check && corr_check;
        
        
    }


    /**
     * Gets a vector of xLcMsCorr correspondences having label (experimental condition) equal to the label passed as argument.
     * @param label the label (experimental condition) to look for.
     * @return a vector a vector of xLcMsCorr correspondences having label equal to input label.
     */
    public Vector<xLcMsCorr> getLcMsCorrLabelled(String label){
        Vector<xLcMsCorr> ret = new Vector<xLcMsCorr>();
        for(int i=0;i<getLcMsCorrSize();i++){
            xLcMsCorr myCorr=getLcMsCorrElemAt(i);
            if(myCorr.getLabel().equals(label)){
                ret.addElement(myCorr);
            }

        }

        return ret;

    }


        /**
     * Gets a vector of xLcMsCorr correspondences having label (experimental condition) equal to the label passed as argument.
     * @param label the label (experimental condition) to look for.
     * @return a vector a vector of xLcMsCorr correspondences having label equal to input label.
     */
    public Vector<xLcMsMsCorr> getLcMsMsCorrLabelled(String label){
        Vector<xLcMsMsCorr> ret = new Vector<xLcMsMsCorr>();
        for(int i=0;i<getLcMsMsCorrSize();i++){
            xLcMsMsCorr myCorr=getLcMsMsCorrElemAt(i);
            if(myCorr.getLabel().equals(label)){
                ret.addElement(myCorr);
            }

        }

        return ret;

    }
    
    /**
     * The peptide  id of the protein (or peptide) the correspondences refer to.
     */
    public String proteinId;
    /**
     * The peptide sequence of the peptide (or protein) the correspondences refer to.
     */
    public String peptideSeq;

    
    /**
     * A vector containing the LC/MS raw data element that has been matched to the peptide <code>peptideID</code>.
     * @see xLcMsData
     */
    public Vector<xLcMsCorr> lcMsCorr;
    
    /**
     * A vector containing the MS/MS raw data element that has been matched to the peptide <code>peptideID</code>.
     * @see xLcMsMsData
     */
    public Vector<xLcMsMsCorr> lcMsMsCorr;
    
    
    
}