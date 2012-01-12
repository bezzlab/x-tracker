package xtracker.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * xLcMsMsData is the data structure holding a MS/MS raw spectra contained in the file specified by fileName of class xLoadData.
 * <p>
 * It is basically an object comprising the following elements:
 * <ul>
 * <li><code>RT</code> the retention time (put -1 if info not available)</li>
 * <li><code>lcMsMsElem</code> a vector of LC-Ms/Ms elements ordered by Parent Ion M/Z</li>
 * </ul>
 * <p>
 * Please note that the structure is ordered increasingly by Retention Time. Also, since lcMsMsData in xLoad data is ordered by Retention time, xLcMs has to implement the comparable interface.
 * The compareTo method has therefore to be specified.
 * @see identData_loadPlugin
 * @see xLcMsData
 * @see xLoad
 * @see xLoadData
 * @see xLcMsMsElem
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xLcMsMsData implements Comparable<xLcMsMsData> {

    /**
     * The constructor.
     * @param retTime is the retention time value.
     */
    public xLcMsMsData(float retTime) {
        RT = retTime;
        lcMsMsElem = new ArrayList<xLcMsMsElem>();
    }

    /**
     * The constructor in case no data is known at construction time.
     */
    public xLcMsMsData() {
        this(-1.00f);
    }

    /**
     * Since lcMsMsData in xLoad data is ordered by Retention time, xLcMsMsData has to implement the comparable interface.
     * The compareTo method has therefore to be specified.
     * @param comparable the element to compare to.
     * @return an integer with the result comparison:<br>-1 if the element compared to the present one has retention time higher than this.retTime.
     * <br>0 if the element compared to the present one has retention time equal to this.retTime.
     * <br>1 if the element compared to the present one has retention time smaller than this.retTime.
     */
    public int compareTo(xLcMsMsData comparable) {
        if (comparable.getRetTime() > this.getRetTime()) {
            return -1;
        }
        if (comparable.getRetTime() < this.getRetTime()) {
            return 1;
        }
        return 0;
    }

    /**
     * Adds an element to the peaklist. Any element of the peaklist is a couple (mz,intensity).
     * <p>
     * Please note that the structure is ordered by parIonMz increasingly.
     * @param parIonMzValue is the parent ion mz value.
     * @param chargeValue is the parent ion charge.
     * @param spect is the MS/MS spectrum.
     * @see xLcMsMsElem
     */
    public void addLcMsMsElem(float parIonMzValue, int chargeValue, xSpectrum spect) {
        xLcMsMsElem myData = new xLcMsMsElem(parIonMzValue, chargeValue, spect);
        int pos = Collections.binarySearch(lcMsMsElem, myData);
        if (pos < 0) {
            pos = 0 - pos - 1;
            lcMsMsElem.add(pos, myData);
        } else {
            //CUT THIS IS JUST FOR iTRAQ problem with KATHRYN DATA!!!!!
            myData.setParentIonMz(myData.getParentIonMz() + (float) Math.random() / 10);
            pos = Collections.binarySearch(lcMsMsElem, myData);
            if (pos < 0) {
                pos = 0 - pos - 1;
                lcMsMsElem.add(pos, myData);
            } else {
                ///END CUT!!!!
                System.out.println("Error: trying to add LC-MS/MS spectrum to existing retention time and parent Ion M/Z (" + parIonMzValue + ")!");
                System.exit(1);
            }///REMOVE BRACKET AS WELL IT'S JUST FOR ITRAQ PROBLEM
        }
    }

    /**
     * Performs a binary search looking for the index of the first element in the LC-MS/MS data structure
     * having a parent ion mz  higher than the <code>target</code>.
     * @param target is the target parent ion mz value.
     * @return the index of the first element having parent ion mz value higher than <code>target</target> <strong>Please note that the method
     * returns <code>-1</code> if all elements in the structure are smaller than <code>target</code>.</strong>
     */
    public int getIndexOfFirstHigherThanMz(float target) {
        int ret = -1;
        xLcMsMsElem tmp = new xLcMsMsElem(target, 1, null);
        int index = Collections.binarySearch(lcMsMsElem, tmp);
        if (index > -1) {
            ret = index;
        } else {
            ret = 0 - index - 1;
        }
        return ret;
    }

    /**
     * Performs a binary search looking for the index of the last element in the LC-MS/MS structure
     * having a parent ion mz value smaller than the <code>target</code>.
     * @param target is the target parent ion mz value.
     * @return the index of the last element having parent ion mz smaller than <code>target</target>. <strong>Please note that the method
     * returns <code>-1</code> if all elements in the structure are higher than <code>target</code>.</strong>
     */
    public int getIndexOfLastSmallerThanMz(float target) {
        int ret = -1;
        xLcMsMsElem tmp = new xLcMsMsElem(target, 1, null);
        int index = Collections.binarySearch(lcMsMsElem, tmp);
        if (index > -1) {
            ret = index;
        } else {
            ret = 0 - index - 2;
        }
        return ret;
    }

    /**
     * Performs two binary searches looking for the index of the indexes of those elements in the LC-MS/MS structure
     * having a parent ion mz value smaller than the <code>upper</code> and higher than <code>lower</code>.
     * @param lower is the lower acceptable parent ion mz value.
     * @param upper is the upper acceptable parent ion mz value.
     * @return the array containing the index of the lower element having parent ion mz value smaller than <code>target</target>. <strong>Please note that the method
     * returns the couple <code>(-1,-1)</code> if no such elements can be found.</strong> <p>This means that all elements having index
     * i such that res[0] &lt; i &gt; res[1] have parent ion mz value within the desired interval of parent ion mz values.
     */
    public int[] getIndexOfAllBetweenMz(float lower, float upper) {
        int[] ret = new int[2];
        int min = 0;
        int max = 0;
        //
        if (lower > upper) {
            float tmp;
            tmp = upper;
            upper = lower;
            lower = tmp;
        }
        max = getIndexOfLastSmallerThanMz(upper);
        min = getIndexOfFirstHigherThanMz(lower);

        if ((min == -1) || (max == -1)) {
            min = -1;
            max = -1;
        }
        ret[0] = min;
        ret[1] = max;
        System.out.println("OUT: " + ret[0] + " " + ret[1]);
        return ret;
    }

    /**
     * Returns the size of the MS/MS spectrum.
     * @return size an integer representing the lcMsMsElem size (that is, how many LC-MS/MS spectra
     * associated at the retention time <code>RT</code>.
     */
    public int getLcMsMsElemSize() {
        return lcMsMsElem.size();
    }

    /**
     * Retrieves the index-th  <code>xLcMsMsElem</code> structure containing the
     * LC-MS/MS raw data.
     * <p>
     * @return xLcMsMsElem the xLcMsMsElem structure containing the LC-MS/MS raw data. If the index
     * is higher than the number of elements in the Vector it returns null.
     * @param index is the index of the raw data vector to retrive.
     */
    public xLcMsMsElem getLcMsMsElemAt(int index) {
        if (index < lcMsMsElem.size()) {
            return lcMsMsElem.get(index);
        } else {
            return null;
        }
    }

    /**
     * Returns the retention time at which the MS/MS spectrum was recorded.
     * @return the retention time <code>RT</code>.
     */
    public float getRetTime() {
        return RT;
    }

    /**
     * Sets the retention time at which the MS/MS spectrum was recorded.
     * @param rtValue is the retention time value
     */
    public void setRetentionTime(float rtValue) {
        RT = rtValue;
    }

    /**
     * Performs a binary search looking for the index of the element in the LC-MS/MS structure
     * having a parent ion mz equal to the <code>target</code>.
     * @param target is the target parent ion mz.
     * @return the index of element having parent ion mz equal to <code>target</target>. <strong>Please note that the method
     * returns <code>-1</code> if no such parent ion mass value exists.</strong>
     */
    public int getLcMsMsIndexOfParIonMz(float target) {
        int ret = -1;
        xLcMsMsElem tmp = new xLcMsMsElem(target, 1, null);
        int index = Collections.binarySearch(lcMsMsElem, tmp);
        if (index > -1) {
            ret = index;
        } else {
            ret = -1;
        }
        return ret;
    }
    /**
     * The retention time
     */
    private float RT;
    /**
     * The Ms/Ms element. Please note that the structure is ordered by parIonMz increasingly.
     * @see xLcMsMsElem
     */
    private List<xLcMsMsElem> lcMsMsElem;
}
