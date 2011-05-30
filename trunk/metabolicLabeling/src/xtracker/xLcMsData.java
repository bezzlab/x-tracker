package xtracker;

import java.util.*;

/**
 * xLcMsData is the data structure holding a LC/MS raw data.  
 * <p> 
 * It is basically a LC-MS matrix element containing Retention Time, and all M/Z and Intensity values associated with it.
 * Please note that only non-zero elements are represented.
 * @see        loadPlugin
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */

public class xLcMsData {

    /**
     * The constructor, sets the Retention Time and the spectrum.
     * @param rtValue the retention time.
     * @param spec is the spectrum.
     */
    public xLcMsData(float rtValue, xSpectrum spec){
       RT =rtValue;  
       spectrum = spec;
       
     }
    
    
    /**
     * Gets the spectrum.
     * @return xSpectrum the spectrum.
     */
    public xSpectrum getSpectrum(){
        return spectrum;

        
 }
  
     /**
     * Gets the retention time.
     * @return RT the retention time. 
     */
    public float getRetTime(){
        return RT;

        
 }   
    
      /**
     * Sets the retention time.
     * @param retValue the retention time to set. 
     */
    public void setRetTime(float retValue){
        RT=retValue;

        
 }   
    
    
    /**
     * Sets the spectrum.
     * @param spec the spectrum to set. 
     */
    public void setSpectrum(xSpectrum spec){
        spectrum=spec;

        
 }   

    
    /**
     * Retention Times
     */
    public float RT;

    /**
     * Spectra
     */
    public xSpectrum spectrum;
    
    
}
