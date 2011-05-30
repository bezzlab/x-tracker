package xtracker;


import java.util.*;
/**
 * xCorrespondenceData is the data structure containing correspondences between rawData peak lists and peptides.
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
     * @param pepId is the peptide identifier.
     * @param pepSeq is the peptide sequence.
     */
    public xCorrespondenceData(String pepId,String pepSeq){
        peptideId=pepId;
        peptideSeq=pepSeq;
        msMsCorr = new Vector<xLcMsMsCorr>();
        lcMsCorr = new Vector<xLcMsCorr>();
    }
    
    /**
     * Gets the peptideId this set of correspondences refers to.
     * @return peptideId, the identificator of the peptide the correspondences refer to.
     */
    public String getPeptideId(){
        return peptideId;
        
    }
    
        /**
     * Gets the sequence of the peptide this set of correspondences refers to.
     * @return peptideSeq, the sequence of the peptide the correspondences refer to.
     */
    public String getPeptideSeq(){
        return peptideSeq;
        
    }
    
        /**
     * Sets the peptideId of the peptide this set of correspondences refers to.
     * @param p the identificator of the peptide the correspondences refer to.
     */
    public void setPeptideId(String p){
         peptideId=p;
        
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
    public int getMsMsCorrSize(){
        return msMsCorr.size();
    
    }
    

    /**
     * Adds a  MS/MS correspondence of the current peptide.
     * @param corrElem is a MS/MS correspondence
     * @see  xLcMsMsCorr
     */
    public void addMsMsCorr(xLcMsMsCorr corrElem){
        msMsCorr.addElement(corrElem);
    
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
     * @return msMsCorr is the MS/MS correspondence element requested. If the index 
      * is higher than the number of elements in the Vector it returns null.
     * @see  xLcMsMsCorr
     */
    public xLcMsMsCorr getMsMsCorrElemAt(int index){
        if(!(index > msMsCorr.size())){
            return msMsCorr.elementAt(index);
        }
        else{
            return null;        
        }
    }
    
     /**
     * Gets a  MS/MS correspondence of the current peptide.
     * @param index is the index of the element to retrieve.
     * @return msMsCorr is the MS/MS correspondence element requested. If the index 
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
 * Checks the validity of the xCorrespondenceData element
 * <p>  
 * The structure is valid if the following holds:
 * <ul>
 * <li>The peptide id is not empty</li>
 * <li>The peptide sequence is not empty</li>
 * <li>At least one of msMsCorr or lcMsCorr are not empty</li>
 * </ul>
 * @return true if the structure is valid, false otherwise.
 */
    public boolean isValid(){
        boolean peptide_check;
        boolean corr_check;
        peptide_check = (this.getPeptideId().length()>0) && (this.getPeptideSeq().length()>0);
        corr_check = (this.getLcMsCorrSize()>0 ) || (this.getMsMsCorrSize() > 0);
        
        return peptide_check && corr_check;
        
    }
    
    
    /**
     * The peptide  id of the peptide (or protein) the correspondences refer to.
     */
    public String peptideId;
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
    public Vector<xLcMsMsCorr> msMsCorr;
    
    
    
}
