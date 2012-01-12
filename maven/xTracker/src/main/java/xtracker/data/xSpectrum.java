package xtracker.data;

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
public class xSpectrum {

    /**
     * The constructor, creates empty vectors for mz and Intensities.
     */
    public xSpectrum() {
        mz = new ArrayList<Float>();
        intensity = new ArrayList<Float>();
    }

    /**
     * Adds an element to the spectrum. An element is a couple (mzValue,intensityValue).
     * <strong>Please note that the Vector of elements is ordered <u>increasingly</u> by mzValue</strong> 
     * <p>
     * @param mzValue is the m/z value.
     * @param intensityValue is the intensity value. 
     */
    public void addElem(float mzValue, float intensityValue) {
        int pos = Collections.binarySearch(mz, mzValue);
        if (pos < 0) {
            pos = 0 - pos - 1;
        }
        mz.add(pos, mzValue);
        intensity.add(pos, intensityValue);
    }

    /**
     * Performs a binary search looking for the index of the first element in the structure
     * having a mz Value higher than the <code>target</code>.
     * @param target is the target mz value.
     * @return the index of the first element having mz value higher than <code>target</target> <strong>Please note that the method 
     * returns <code>-1</code> if all elements in the structure are smaller than <code>target</code>.</strong>
     */
    public int getIndexOfFirstHigherThan(float target) {
        int index = Collections.binarySearch(mz, target);
        if (index > -1) {
            return index;
        } else {
            return  0 - index - 1;
        }
    }

    /**
     * Performs a binary search looking for the index of the last element in the structure
     * having a mz value smaller than the <code>target</code>.
     * @param target is the target mz value.
     * @return the index of the last element having mz value smaller than <code>target</target>. <strong>Please note that the method 
     * returns <code>-1</code> if all elements in the structure are higher than <code>target</code>.</strong> 
     */
    public int getIndexOfLastSmallerThan(float target) {
        int index = Collections.binarySearch(mz, target);
        //TODO: should be 0-index-1??
        if (index > -1) {
            return index;
        } else {
            return 0 - index - 2;
        }
    }

    /**
     * Performs two binary searches looking for the elements in the structure
     * having a mz value smaller than the <code>upper</code> and higher than <code>lower</code>.
     * @param lower is the lower acceptable mz value.
     * @param upper is the upper acceptable mz value.
     * @return the array containing the index of the lower element having mz smaller than <code>target</target>. <strong>Please note that the method 
     * returns <code>empty array</code> if all elements in the structure are higher than <code>target</code>.</strong> <p>This means that:<br>
     * res[i][0] is the mz value of the i-th peak having mz values between lower and upper.<br>
     * res[i][1] is the intensity value of the i-th peak having mz values between lower and upper.<br>
     */
    public float[][] getSubspectrumBetween(float lower, float upper) {
        float[][] ret;
        int min = 0;
        int max = 0;
        //
        if (lower > upper) {
            float tmp;
            tmp = upper;
            upper = lower;
            lower = tmp;

        }

        max = getIndexOfLastSmallerThan(upper);
        min = getIndexOfFirstHigherThan(lower);
//                System.out.println("Min:" + min + " Max:" +max);
        if ((min == -1) || (max == -1) || (min > max)) {
            ret = new float[0][0];
        } else {
            ret = new float[max - min + 1][2];
            int elCounter = 0;
            for (int i = min; i <= max; i++) {
                ret[elCounter][0] = mz.get(i);
                ret[elCounter][1] = intensity.get(i);
                elCounter++;
            }
        }
        return ret;
    }

    /**
     * Gets the whole spectrum. Please remember that the structure is ordered increasingly by mz.
     * @return A double array of floats representing the spectrum. In particular:<br>
     * ret[i][0] is the i-th mz value.<br>
     * ret[i][1] is the i-th intensity value.
     * 
     */
    public float[][] getSpectrum() {
        float[][] ret = new float[getSize()][2];
        for (int i = 0; i < getSize(); i++) {
            ret[i][0] = mz.get(i);
            ret[i][1] = intensity.get(i);
        }
        return ret;
    }

    /**
     * getSize computes the size of the spectrum.
     * <p>
     * @return the size of the dataset
     */
    public int getSize() {
        return mz.size();
    }
    /**
     * Vector of mz values.
     * @see xLcMsData
     * @see xLcMsMsData
     */
    private List<Float> mz;
    /**
     * The vector of intensities.
     * @see xLcMsData
     * @see xLcMsMsData
     */
    private List<Float> intensity;
}
