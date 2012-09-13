package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * xSpectrum is the data structure housing the peak list (i.e. intensity over m/z values) both at the MS and MS/MS levels.  
 * <p> 
 * It is constituted by a vector of entries <code>mz</code> representing mass over charge values and a vector of intensities. <strong>Please note that
 * values are ordered by mz <u>increasingly</u>.</strong>
 * @see xLcMsData
 * @see xLcMsMsData
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xSpectrumList implements uk.ac.cranfield.xTracker.data.xSpectrum{
    /**
     * List of mz values.
     * @see xLcMsData
     * @see xLcMsMsData
     */
    private List<Double> mz;
    /**
     * The vector of intensities.
     * @see xLcMsData
     * @see xLcMsMsData
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
     * Adds an element to the spectrum. An element is a couple (mzValue,intensityValue).
     * <strong>Please note that the Vector of elements is ordered <u>increasingly</u> by mzValue</strong> 
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
     * getSize computes the size of the spectrum.
     * <p>
     * @return the size of the dataset
     */
    public int getSize() {
        return mz.size();
    }

    @Override
    public double[] getMzData(String filename) {
        double[] ret = new double[getSize()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = mz.get(i);
        }
        return ret;
    }

    @Override
    public double[] getIntensityData(String filenames) {
        double[] ret = new double[getSize()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = intensity.get(i);
        }
        return ret;
    }
}
