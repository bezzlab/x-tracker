/*
 * Class that represents a Spectrum object. contains methods to store the information about a spectrum
 * from the mzML file in an object, and to acess them.
 */

package xtracker;

/**
 *
 * @author laurie Tonon for X-Tracker
 */

import java.io.Serializable;

public class Spectrum implements Serializable{

        /**
         * index of the spectrum
         */
        protected int num = -1;

        /**
         * Ms level (Ms or MsMs)
         */
        protected int msLevel = -1;

        /**
         * retention time
         */
        protected String retentionTime = null;

        /**
         * charge of the parent ion (MsMs)
         */
        protected int precursorCharge = -1;

        /**
         * mz value of the ion (MsMs)
         */
        protected float precursorMz = -1;

        /**
         * number of peaks
         */
        protected int peaksCount = -1;

        /**
         * list of mz and intensity values
         */
        protected float[][] massIntensityList;

        /**
         * precision used in the encoding of the mz values
         */
        protected int precisionMass = -1;

        /**
         * precision used in the encoding of the intensity values
         */
        protected int precisionIntens= -1;
        
        
        

        /**
         * Set the ms level
         * @param msLevel
         */
    public void setMsLevel(int msLevel) {
        this.msLevel = msLevel;
    }

    /**
     * set the index of the spectrum
     * @param num
     */
    public void setNum(int num) {
        this.num = num;
    }
       
    /**
     * set the mz/intensity list
     * @param massIntensityList
     */
    public void setMassIntensityList(float[][] massIntensityList) {
        this.massIntensityList = massIntensityList;
    }

    /**
     * set the number of peaks
     * @param peaksCount
     */
    public void setPeaksCount(int peaksCount) {
        this.peaksCount = peaksCount;
    }

    /**
     * set the charge of the parent ion
     * @param precursorCharge
     */
    public void setPrecursorCharge(int precursorCharge) {
        this.precursorCharge = precursorCharge;
    }

    /**
     * set the m/z of the parent ion
     * @param precursorMz
     */
    public void setPrecursorMz(float precursorMz) {
        this.precursorMz = precursorMz;
    }

    /**
     * set the retention time
     * @param retentionTime
     */
    public void setRetentionTime(String retentionTime) {
        this.retentionTime = retentionTime;
    }

    /**
     * set the precision of the m/z encoding
     * @param precision
     */
    public void setPrecisionMass(int precision) {
        this.precisionMass = precision;
    }

    // set the precision of the intensity encoding
    public void setPrecisionIntens(int precisionIntens) {
        this.precisionIntens = precisionIntens;
    }
    
    
    
    
    
    
    
    /**
     * get the ms level
     * @return
     */
    public int getMsLevel() {
        return msLevel;
    }

    /**
     * get the index of the spectrum
     * @return
     */
    public int getNum() {
        return num;
    }

    /**
     * get the mz/intensity list
     * @return
     */
    public float[][] getMassIntensityList() {
        return massIntensityList;
    }

    /**
     * get the number of peaks
     * @return
     */
    public int getPeaksCount() {
        return peaksCount;
    }

    /**
     * get the charge of the parent ion
     * @return
     */
    public int getPrecursorCharge() {
        return precursorCharge;
    }

    /**
     * get the m/z value of the parent ion
     * @return
     */
    public float getPrecursorMz() {
        return precursorMz;
    }

    /**
     * get the retention time
     * @return
     */
    public double getDoubleRetentionTime() {
        return Double.parseDouble(retentionTime);
    }

    /**
     * get the precision of the m/z encoding
     * @return
     */
    public int getPrecisionMass() {
        return precisionMass;
    }

    /**
     * get the precision of the itensity encoding
     * @return
     */
    public int getPrecisionIntens() {
        return precisionIntens;
    }
        
        
        
     
        

}
