package xtracker;

/**
 * xIdentData is the data structure representing an identification.
 * <p>
 * @see identInputData
 * @see loadPlugin
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */

public class xIdentData {

    /**
     * The constructor
     * @param confidence the confidence level of the identification.
     * @param retTimeInd is the index of the retention time the <u>xLcMsData or xLcMsMsData structures</u> the identification is referred to.
     * @param parentMassInd is the index of the parent mass in the case of LC-MS/MS identifications.
     */
    public xIdentData(float confidence, int retTimeInd,int parentMassInd){
        confidenceLevel=confidence;
        parentMassIndex=parentMassInd;
        retTimeIndex=retTimeInd;
        
    
    }
    
    
    /**
     * Sets the parent mass index the identification refers to.
     * @param index the parent mass index.
     */
    public void setparentMassIndex(int index){
        parentMassIndex=index;
    }
    
    /**
     * Sets the confidence level of the identification.
     * @param confidence of the peptide identification.
     */
    public void setConfidenceLevel(float confidence){
        confidenceLevel=confidence;
    }
    
    
    /**
     * Gets the index of the parent mass the identification refers to.
     * @return parentMassIndex the parent ion mass index in the case of an LC-MS/MS identification.
     */
    public int getParentMassIndex(){
        return parentMassIndex;
    }
    
    /**
     * Gets the index of the retention time the identification refers to.
     * @return retTimeIndex the retention time index within the xLoad structure corresponding to the file fileName of xLoadData.
     */
    public int getRetTimeIndex(){
        return retTimeIndex;
    }
    
        /**
     * Sets the retention time index the identification refers to.
     * @param index the parent mass index.
     */
    public void setRetTimeIndex(int index){
        retTimeIndex=index;
    }
    
    
    /**
     * Gets the confidence level of the identification.
     * @return confidenceLevel the confidence of the identification.
     */
    public float getConfidenceLevel(){
        return confidenceLevel;
    }
    
       
    

    /**
     * A float assessing the confidence level of the identification.
     */
    public float confidenceLevel; 
    
    /**
     * A integer with the index of the retention time the identification corresponds to in the <u>xLoadData</u> structure (lcMsMsData in the case of LC-MS/MS identification, lcMsData otherwise).
     * @see xLcMsData
     */
    public int retTimeIndex;
    
    /**
     * A integer with the index of the parent ion Mass the identification corresponds to in the <u>xLcMsMsData</u> structure. It is unused in the case of LC-MS identifications.
     * @see xLcMsData
     */
    public int parentMassIndex;
    
}
