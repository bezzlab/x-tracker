package uk.ac.cranfield.xTracker.utils;

import uk.ac.cranfield.xTracker.data.xSpectrum;

/**
 *
 * @author Jun Fan@cranfield
 */
public class MS2QuantitationMethod {
    /**
     * Use the highest intensity value within the range as the quantitation
     * @param spectrum
     * @return 
     */
    public static double highest(xSpectrum spectrum){
        double ret = 0;
        double[] intensities = spectrum.getIntensityData(null);
        for(double d:intensities){
            if(d>ret) ret = d;
        }
        return ret;
    }
    /**
     * Use the sum of intensity values as the quantitation
     * @param spectrum
     * @return 
     */
    public static double sumIntensity(xSpectrum spectrum){
        double sum = 0;
        double[] intensities = spectrum.getIntensityData(null);
        for(double d:intensities){
            sum += d;
        }
        return sum;
    }
    /**
     * Use the sum of areas of trapezoids as the quantitation
     * One trapezoid is constructed by two adjacent intensities as two widths and the mz difference as the height
     * @param spectrum
     * @return 
     */
    public static double trapezoidArea(xSpectrum spectrum){
        double sum = 0;
        double[] mz = spectrum.getMzData(null);
        double[] intensities = spectrum.getIntensityData(null);
        for (int i = 0; i < mz.length-1; i++) {
            sum = sum + (mz[i+1]-mz[i])*(intensities[i]+intensities[i+1])/2;
        }
        return sum;
    }
}
