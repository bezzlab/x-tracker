package xtracker;



/**
 * xLcMsCorr is the data structure populated by pealSelPlugins and contains the information on all
 * LC/MS peaks from raw data that can be associated to a particular peptide.   
 * <p> 
 * It is constituted by six fields representing the fileID, the peakID, the mz value, the intensity, the retention time and the sperimental conditions characterizing the peaks 
 * associated to the current peptide (i.e. the peptide this LC/MS correlation matrix corresponds to in the xCorrespondenceData structure)..
 * @see peakSelPlugin
 * @see xCorrespondenceData
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xLcMsCorr {
    
    
       /**
     * The constructor. In case no data is known at construction time.
     */
    public xLcMsCorr(){
        fileName = "";
        spectrumID = -1;
        mz = 0.0f;
        label = "";
        RT=-1;
        intensity=0;
        
    }
    
       /**
        * The constructor in case the actual values are known at construction time.
        * @param fileNameVal the name of the file the peak came from.
        * @param spectrumIdVal the identifier of the spectrum the peak came from.
        * @param mzVal the mz value of the peak.
        * @param intensityVal the intensity value of the peak.
        * @param labelVal a string representing the sperimental conditions associated to the peak.
        * @param rtVal the retention time value of the spectrum the peak came from.
        */
    public xLcMsCorr(String fileNameVal, int spectrumIdVal, float mzVal, float intensityVal, String labelVal, float rtVal){
        fileName = fileNameVal;
        spectrumID = spectrumIdVal;
        mz =mzVal;
        label = labelVal;
        RT=rtVal;
        intensity=intensityVal;
        
    }
    
    /**
     * Get the file name of the file the peak comes from.
     * @return fileName the file name.
     */
   public String getFileName(){
    return fileName;
   
   }    
   
   /**
     * Get the spectrumId of the peak.
     * @return spectrumID the file identifier.
     */
   public int getSpectrumId(){
    return spectrumID;
   
   }
   
    /**
     * Get the mz value of the peak.
     * @return mz the mass over charge (MZ) value.
     */
   public float getMz(){
    return mz;
   
   }
   
    /**
     * Get the intensity value of the peak.
     * @return intensity the intensity of the peak.
     */
   public float getIntensity(){
    return intensity;
   
   }
   
    /**
     * Get the lable (i.e. sperimental conditions) that is associated to the peak.
     * @return label the string of the the sperimental conditions associated to the peak.
     */
   public String getLabel(){
    return label;
   }
         
       /**
     * Get the retention time associated to the spectrum the peak belongs to.
     * @return charge the charge associated to the spectrum the peak belongs to.
     */
   public float getRT(){
    return RT;
   }    

   
   
   
   
       /**
     * Set the fileid of the file the peak comes from.
     * @param fileNameVal is the file name.
     */
   public void setFileName(String fileNameVal){
    fileName=fileNameVal;
   
   }    
   
       /**
     * Set the fileid of the file the peak comes from.
     * @param spectrumIdVal is the integer representing the spectrum identifier.
     */
   public void setSpectrumId(int spectrumIdVal){
    spectrumID=spectrumIdVal;
   
   }
   
    /**
     * Set the mz value of the peak.
     * @param mzVal the mass over charge (MZ) value.
     */
   public void setMz(float mzVal){
    mz=mzVal;
   
   }
   
    /**
     * Set the intensity value of the peak.
     * @param intensityVal the intensity of the peak.
     */
   public void setIntensity(float intensityVal){
    intensity=intensityVal;
    
   }
   
    /**
     * Set the lable (i.e. sperimental conditions) that is associated to the peak.
     * @param labelVal the string of the the sperimental conditions associated to the peak.
     */
   public void getLabel(String labelVal){
    label=labelVal;
   }
      
   
     /**
     * Set the retention time associated to the spectrum the peak belongs to.
     * @param rtVal the charge associated to the spectrum the peak belongs to.
     */
   public void setRT(float rtVal){
        RT=rtVal;
   }    

   
   
   
    /**
     * fileNAme is the name of the considered file in the xLoad structure.
     * @see xLoad
     */
    public String fileName;
    

    /**
     * This is the id of the spectrum within the fileID file in the xLoad structure.
     * @see xLoad
     */
    public int spectrumID;
    
    /**
     * This is to the mz value of the peak associated to the peptide within the  fileID file, in the spectrumID spectrum of the xLoad structure.
     * @see xLoad
     */
    public float mz;
    
    /**
     * This is to the intensity value of the peak associated to the peptide within the  fileID file, in the spectrumID spectrum of the xLoad structure.
     * @see xLoad
     */
    public float intensity;
    
    /**
     * This is a string describing the sperimental conditions of peakID, in spectrumID of file fileID in xLoad.
     * @see xLoad
     */
    public String label;    
     
    /**
     * This is a float representing the RT of the spectrum the peakID belongs to, in spectrumID of file fileID in xLoad.
     * @see xLoad
     */
    public float RT;
    


}



