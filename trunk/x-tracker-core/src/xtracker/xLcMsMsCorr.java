package xtracker;
import java.util.*;

/**
 * xLcMsMsCorr is the data structure populated by pealSelPlugins and contains the information on all
 * MS/MS peaks from raw data that can be associated to a particular peptide.   
 * <p> 
 * It is constituted by eight fields representing the retention time, charge, the mz and intensity value as well as the parent Ion Mz and the sperimental conditions characterizing the peaks. Modifications identified in the peptide to which the peak belongs to can be accomodated in the structure as well.
 * @see peakSelPlugin
 * @see xCorrespondenceData
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */

public class xLcMsMsCorr {
    
    /**
     * The constructor. In case no data is known at construction time.
     */
    public xLcMsMsCorr(){
        mz = 0.0f;
        label = "";
        RT=-1;
        charge=0;
        parentIonMz=0;
        intensity=0;
        modificationName= new Vector<String>();
        position=new Vector<Integer>();
        
    }
    
       /**
        * The constructor in case the actual values are known at construction time.
        * @param mzVal the mz value of the peak.
        * @param intensityVal the intensity value of the peak.
        * @param labelVal a string representing the sperimental conditions associated to the peak.
        * @param rtVal the retention time value of the spectrum the peak came from.
        * @param chargeVal the charge value of the spectrum the peak came from.
        * @param parentIonMzVal the parent Ion MZ value associated to the spectrum the peak came from.
        */
    public xLcMsMsCorr(float mzVal, float intensityVal, String labelVal, float rtVal, int chargeVal, float parentIonMzVal){
        mz =mzVal;
        label = labelVal;
        RT=rtVal;
        charge=chargeVal;
        parentIonMz=parentIonMzVal;
        intensity=intensityVal;
        modificationName= new Vector<String>();
        position=new Vector<Integer>();
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
     * Get the charge associated to the spectrum the peak belongs to.
     * @return charge the charge associated to the spectrum the peak belongs to.
     */
   public int getCharge(){
    return charge;
   } 
   
       /**
     * Get the retention time associated to the spectrum the peak belongs to.
     * @return charge the charge associated to the spectrum the peak belongs to.
     */
   public float getRT(){
    return RT;
   }    

    /**
     * Get the parent ion MZ value associated to the spectrum the peak belongs to.
     * @return parentIonMz the parent Ion MZ of the MS/MS spectrum the peak belongs to.
     */
   public float getParentIonMz(){
    return parentIonMz;
   }    
   
   
   
   
   /**
    * Gets the number of modifications present in the peptide associated to this peak
    * @return the number of modifications.
    */
   public int getModificationSize(){
        return modificationName.size();

   }

   /**
    * Gets the name of the index-th modification.
    * @param index the position of the element to retrive.
    * @return the name of the index-th modification in the modificationName structure if it exists, empty string otherwise.
    */
   public String getModificationNameAtIndex(int index){
       String ret="";
       if(index>-1 && index<modificationName.size()){
            ret=modificationName.elementAt(index);
       }


       return ret;

   }


   /**
    * Gets the position within the peptideSeq of the index-th modification.
    * @param index the position of the element to retrive.
    * @return the position of the index-th modification in the modificationName structure if it exists, -1 otherwise.
    */
   public int getModPositionAtIndex(int index){
       int ret=-1;
       if(index>-1 && index<position.size()){
            ret=position.elementAt(index);
       }

       return ret;
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
   public void setLabel(String labelVal){
    label=labelVal;
   }
      
    /**
     * Set the charge associated to the spectrum the peak belongs to.
     * @param chargeVal the charge associated to the spectrum the peak belongs to.
     */
   public void setCharge(int chargeVal){
     charge = chargeVal;
   } 
   
     /**
     * Set the retention time associated to the spectrum the peak belongs to.
     * @param rtVal the charge associated to the spectrum the peak belongs to.
     */
   public void setRT(float rtVal){
        RT=rtVal;
   }    

    /**
     * Set the parent ion MZ value associated to the spectrum the peak belongs to.
     * @param parentMzVal the parent Ion MZ of the MS/MS spectrum the peak belongs to.
     */
   public void setParentIonMz(float parentMzVal){
    parentIonMz=parentMzVal;
   }
   
     /**
     * Adds a modification to the correspondence data.
     * @param modName is the name of the modification to add.
     * @param pos is the position of the modification within the peptide (0 means N-Terminus, length(peptideSeq)+1 means C-Terminus.
     */
   public void addModification(String modName, int pos){
        modificationName.addElement(modName);
        position.addElement(pos);

   }


    /**
     * This is a string describing the sperimental conditions of the peak.
     * @see xLoad
     */
    public String label;

    /**
     * This is to the mz value of the peak.
     * @see xLoad
     */
    public float mz;
    
    /**
     * This is to the intensity value of the peak.
     * @see xLoad
     */
    public float intensity;
    
  
     
    /**
     * This is a float representing the RT of the spectrum the peak belongs to.
     * @see xLoad
     */
    public float RT;
    
        /**
     * This is an integer representing the charge of precursor the MS/MS peak belongs to.
     * @see xLoad
     */
    public int charge;
    
    /**
     * This is a float representing the parent Ion MZ value of the spectrum the peak belongs to.
     * @see xLoad
     */
    public float parentIonMz;


        /**
     * The vector of modifications identified in the peptide originating this peak.
     * @see xLoad
     */
    public Vector<String> modificationName;


    /**
     * The vector of modification's positions belonging to the peptide originating this peak.
     * @see xLoad
     */
    public Vector<Integer> position;

}
