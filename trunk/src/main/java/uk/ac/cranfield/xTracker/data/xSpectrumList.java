package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * xSpectrumList is the data structure holding the spectrum in the list form
 * This class is suitable for a small collection of spectra (e.g. <1000) from non-mzML file.
 * <p> 
 * It is constituted by a list of <code>mz</code> representing mass over charge values and a list of intensities with the same length. <strong>Please note that
 * values are ordered by mz <u>increasingly</u>.</strong>
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class xSpectrumList implements uk.ac.cranfield.xTracker.data.xSpectrum{
    /**
     * List of mz values.
     */
    private List<Double> mz;
    /**
     * The vector of intensities.
     */
    private List<Double> intensity;

    /**
     * The constructor, creates empty vectors for mz and Intensities.
     */
    public xSpectrumList() {
        mz = new ArrayList<Double>();
        intensity = new ArrayList<Double>();
    }

    /**
     * Adds one peak in the spectrum. A peak is a couple (mzValue,intensityValue).
     * <strong>Please note that mz values are inserted in the <u>increasingly</u> order</strong> 
     * <p>
     * @param mzValue is the m/z value.
     * @param intensityValue is the intensity value. 
     */
    public void addElem(double mzValue, double intensityValue) {
        int pos = Collections.binarySearch(mz, mzValue);
        if (pos < 0) {
            pos = 0 - pos - 1;
        }
        mz.add(pos, mzValue);
        intensity.add(pos, intensityValue);
    }
    /**
     * Get the count of peaks in the spectrum
     * @return the count of peaks in the spectrum
     */
    public int getSize() {
        return mz.size();
    }
    /**
     * Get the list of mz values of the spectrum
     * @param filename useless, just for interface usage
     * @return the list of the ordered mz values
     */
    @Override
    public double[] getMzData(String filename) {
        double[] ret = new double[getSize()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = mz.get(i);
        }
        return ret;
    }
    /**
     * Get the list of intensities values of the spectrum
     * @param filename useless, just for interface usage
     * @return the list of the intensity values according to the ordered mz values
     */
    @Override
    public double[] getIntensityData(String filenames) {
        double[] ret = new double[getSize()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = intensity.get(i);
        }
        return ret;
    }
}
