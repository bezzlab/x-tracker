package xtracker;

import java.util.*;


/**
 * xMsMsSpectra is the data structure holding a MS/MS raw spectra contained in the file specified by fileID of class rawInputData.  
 * <p> 
 * It is basically an object comprising the following elements:
 * <ul>
 * <li><code>RT</code> the retention time (put -1 if info not available)</li>
 * <li><code>parentIonMz</code> the parent ion mass over charge M/Z (put -1 if info not available)</li>
 * <li><code>charge</code> the charge (put -1 if info not available)</li>
 * <li><code>mz</code> an array of MS/MS mass over charge M/Z values</li>
 * <li><code>intensity</code> an array of MS/MS intensities</li>
 * </ul>
 * @see loadPlugin
 * @see xMsMsSpectra
 * @see rawInputData
 * @author Dr. Luca Bianco -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xMsMsSpectra {

    /**
     * The constructor. 
     * @param retTime is the retention time value.
     * @param parIonMz is the parent ion mz value
     * @param chargeValue is the charge.
     */
    public xMsMsSpectra(float retTime, float parIonMz, int chargeValue){
        RT = retTime;
        parentIonMz = parIonMz;
        charge = chargeValue;
        mz = new Vector<Float>();
        intensity = new Vector<Float>();
    }
    
        /**
     * The constructor in case no data is known at construction time. 
     */
    public xMsMsSpectra(){
        RT = (float)-1.00;
        parentIonMz = (float)-1.00;
        charge = -1;
        mz = new Vector<Float>();
        intensity = new Vector<Float>();
    }
    /**
     * Adds an element to the peaklist. Any element of the peaklist is a couple (mz,intensity). 
     * @param mzValue the mz value of the peaklist (it is normally the X coordinate).
     * @param intensityValue the intensity value of the peaklist (it is normally the Y coordinate).
     */
    public void addPeakListElem(float mzValue, float intensityValue){
        mz.add(mzValue);
        intensity.add(intensityValue);
    
    }
    /**
     * Returns the retention time at which the MS/MS spectrum was recorded.
     * @return the retention time <code>RT</code>.
     */
    public float getRetentionTime(){
        return RT;
    }

    /**
     * Returns the charge of this MS/MS spectrum.
     * @return the charge value <code>charge</code>.
     */    
    public int getCharge(){
        return charge;
    }
    
    /**
     * Returns the mz values of the peaklist of this MS/MS spectrum.
     * @return a Vector of Floats representing mz values of the peaklist <code>mz</code>.
     */    
    public Vector<Float> getMz(){
        return mz;
    }
   
     /**
     * Returns the intensity values of the peaklist of this MS/MS spectrum.
     * @return a Vector of Floats representing intensity values of the peaklist <code>intensity</code>.
     */    
    public Vector<Float> getIntensity(){
        return intensity;
    }
    
    /**
     * Returns the parent ion mz corresponding to this MS/MS.
     * @return the parent ion mz value <code>parentIonMz</code>.
     */    
    public float getParentIonMz(){
        return parentIonMz;
    }
    
     /**
     * Sets the retention time at which the MS/MS spectrum was recorded.
     * @param rtValue is the retention time value
     */
    public void setRetentionTime(float rtValue){
        RT=rtValue;
    }
    
     /**
     * Sets the parent ion mz of the MS/MS spectrum.
     * @param mzValue is the parent ion mz value
     */
    public void setParentIonMz(float mzValue){
        parentIonMz=mzValue;
    }
     /**
     * Sets the charge of the MS/MS spectrum.
     * @param chargeValue is the parent ion mz value
     */
    public void setCharge(int chargeValue){
        charge=chargeValue;
    }
    
    
    /**
     * The retention time 
     */
    public float RT;
    /**
     * The partent Ion M/Z
     */
    public float parentIonMz;
    /**
     * The charge
     */
    public int charge;
    /**
     * The peak list's mz values
     */
    public Vector<Float> mz;
    
    /**
     * The peak list's intensity values
     */
    public Vector<Float> intensity;
    
}
