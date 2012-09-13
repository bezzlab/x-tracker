package uk.ac.cranfield.xTracker.utils;

import uk.ac.cranfield.xTracker.data.xSpectrum;

/**
 *
 * @author Jun Fan@cranfield
 */
public class MS2QuantitationMethod {
    public static double highest(xSpectrum spectrum){
        double ret = 0;
        double[] intensities = spectrum.getIntensityData(null);
        for(double d:intensities){
            if(d>ret) ret = d;
        }
        return ret;
    }

    public static double sumIntensity(xSpectrum spectrum){
        double sum = 0;
        double[] intensities = spectrum.getIntensityData(null);
        for(double d:intensities){
            sum += d;
        }
        return sum;
    }

    public static double trapezoidArea(xSpectrum spectrum){
        double sum = 0;
        double[] mz = spectrum.getMzData(null);
        double[] intensities = spectrum.getIntensityData(null);
        for (int i = 0; i < mz.length-1; i++) {
            sum = sum + (mz[i+1]-mz[i])*(intensities[i]+intensities[i+1])/2;
        }
        return sum;
    }

    /**
     * Computes the area of the peaks. NOTE THAT it assumes
     * the vectors ordered by mz values ascendingly.
     * @param IC the vector of Intensities.
     * @param MZ the vector of MZ values.
     * @return the area of the peak.
     */
//    public float computeAreaSimpson(Vector<Float> MZ, Vector<Float> IC) {
//
//        float ret = 0;
//        float tmp_val = 0;
//        int size = MZ.size();
//        int remainder = size % 3;
//        int i = 0;
//        int starti = 0;
//        int endi = size - 1;
//        float x1 = 0; //RT values
//        float x2 = 0;
//        float x3 = 0;
//        float y1 = 0; //Intensity values
//        float y2 = 0;
//        float y3 = 0;
//        float h = 0; //the common mz step (it's the average of the two steps)
//        //for(int j=0;j<MZ.size();j++){
//        //    System.out.println(j+ ") RT[" + MZ.elementAt(j) + "]=" + IC.elementAt(j));
//        //}
//
//        if (size > 1) {
//            switch (remainder) {
//                case 1: {
//                    //The number of peaks cannot be divided by three, the first and last two peaks will be integrated with
//                    //trapezoid rule.
//                    tmp_val = (IC.elementAt(0) + IC.elementAt(1)) * (MZ.elementAt(1) - MZ.elementAt(0)) / 2;
//                    ret += tmp_val;
//                    starti = 2;
//                    tmp_val = (IC.elementAt(size - 2) + IC.elementAt(size - 1)) * (MZ.elementAt(size - 1) - MZ.elementAt(size - 2)) / 2;
//                    ret += tmp_val;
//                    endi = size - 2;
//
//                }
//                case 2: {
//                    //The number of peaks cannot be divided by three, the first two peaks will be integrated with
//                    //trapezoid rule.
//                    tmp_val = (IC.elementAt(0) + IC.elementAt(1)) * (MZ.elementAt(1) - MZ.elementAt(0)) / 2;
//                    ret += tmp_val;
//                    starti = 2;
//
//                }
//            }
//        }
//        for (i = starti; i < endi; i = i + 3) {
//            x1 = MZ.elementAt(i);
//            x2 = MZ.elementAt(i + 1);
//            x3 = MZ.elementAt(i + 2);
//            y1 = IC.elementAt(i);
//            y2 = IC.elementAt(i + 1);
//            y3 = IC.elementAt(i + 2);
//
//            h = (x3 - x1) / 2;
//
//            // System.out.println("x:" + x1 + " " + x2 + " " + x3 + " y:" + y1 + " " + y2 + " " + y3 + " h:" + h);
//
//            tmp_val = h * (y1 / 3 + (4 * y2) / 3 + y3 / 3);
//            ret += tmp_val;
//        }
//
//        return ret;
//    }


}
