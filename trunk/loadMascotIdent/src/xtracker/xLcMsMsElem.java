package xtracker;


/**
 * xLcMsMsElem is the data structure holding a LC-MS/MS raw data.
 * <p>
 * It is basically a structure containing ParentIonMz, parent Charge and M/Z and Intensity values associated with it.
 * Please note that since lcMsMsElem structure of xLcMsMsData is ordered by parentIonMz, xLcMsMsElem has to implement the Comparable interface and
 * therefore implement the compareTo method.
 * @see        identData_loadPlugin
 * @see xSpectrum
 * @see xLcMsMsData
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */


public class xLcMsMsElem implements Comparable<xLcMsMsElem>{


    /**
     * The constructor
     * @param parIonMzValue is the parent ion mz.
     * @param chargeValue is the parent charge value.
     * @param spec is the MS/MS spectrum.
     */
    public xLcMsMsElem(float parIonMzValue,int chargeValue, xSpectrum spec){
        parentIonMz=parIonMzValue;
        charge=chargeValue;
        spectrum=spec;
    }

      /**
      * Since lcMsMsElem in xLcMsMsData structure is ordered by parentIonMz, xLcMsMsElem has to implement the comparable interface.
      * The compareTo method has therefore to be specified.
      * @param comparable the element to compare to.
      * @return an integer with the result comparison:<br>-1 if the element compared to the present one has parentIonMz higher than this.parentIonMz.
      * <br>0 if the element compared to the present one has parentIonMz equal to this.parentIonMz.
      * <br>1 if the element compared to the present one has parentIonMz smaller than this.parentIonMz.
      */
     public int compareTo(xLcMsMsElem comparable){
        int ret =0;
        if(comparable.getParentIonMz()> this.getParentIonMz()){
            ret=-1;
        }
        else{
            if(comparable.getParentIonMz()< this.getParentIonMz()){
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
     * Gets the parent ion Mz.
     * @return parentIonMz the parent ion Mz value.
     */
    public float getParentIonMz(){
        return parentIonMz;


 }

     /**
     * Sets the parent ion Mz.
     * @param parIonMzValue the parent ion mz value to set.
     */
    public void setParentIonMz(float parIonMzValue){
       parentIonMz=parIonMzValue;


 }

    /**
     * Gets the charge.
     * @return charge the charge.
     */
    public int getCharge(){
        return charge;


 }

     /**
     * Sets the charge.
     * @param chargeValue the charge value to set.
     */
    public void setCharge(int chargeValue){
       charge=chargeValue;


 }

    /**
     * Sets the spectrum.
     * @param spec the spectrum to set.
     */
    public void setSpectrum(xSpectrum spec){
        spectrum=spec;


 }


    /**
     * The parent Ion Mz
     */
    public float parentIonMz;

    /**
     * The charge.
     */
    public int charge;

    /**
     * The Ms/Ms spectrum.
     */
    public xSpectrum spectrum;

}
