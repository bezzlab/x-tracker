package xtracker;



/**
 * xLcMsData is the data structure holding a LC/MS raw data.
 * <p>
 * It is basically a LC-MS matrix element containing Retention Time, and all M/Z and Intensity values associated with it.
 * Please note that only non-zero elements are represented. Also, since lcMsData in xLoad data is ordered by Retention time, xLcMs has to implement the comparable interface.
      * The compareTo method has therefore to be specified.
 * @see        identData_loadPlugin
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */

public class xLcMsData implements Comparable<xLcMsData>{

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
      * Since lcMsData in xLoad data is ordered by Retention time, xLcMs has to implement the comparable interface.
      * The compareTo method has therefore to be specified.
      * @param comparable the element to compare to.
      * @return an integer with the result comparison:<br>-1 if the element compared to the present one has retention time higher than this.retTime.
      * <br>0 if the element compared to the present one has retention time equal to this.retTime.
      * <br>1 if the element compared to the present one has retention time equsmaller than this.retTime.
      */
     public int compareTo(xLcMsData comparable){
        int ret =0;
        if(comparable.getRetTime()> this.getRetTime()){
            ret=-1;
        }
        else{
            if(comparable.getRetTime()< this.getRetTime()){
                ret=+1;

            }
            else{
                ret=0;
            }

        }
        return ret;
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
