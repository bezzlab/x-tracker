package uk.ac.cranfield.xTracker.data;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArrayList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;
import uk.ac.cranfield.xTracker.xTracker;

/**
 * xSpectrumMzML is the data structure holding the spectrum in the base64 string form.
 * This class is suitable for spectra from mzML files
 * <p> 
 * The spectra is retrieved from the mzML unmarshaller and uncompressed into arrays.
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class xSpectrumMzML implements xSpectrum {

    private String id;
    BinaryDataArrayList bdal = null;

    public xSpectrumMzML(String idIn) {
        id = idIn;
    }
    /**
     * Get mz values from base64 string which is uncompressed by the unmarshaller for the mzML file
     * @param filename the file name of the mzML file
     * @return 
     */
    @Override
    public double[] getMzData(String filename) {
        //MS:1000514 m/z array
        return getData(filename,"MS:1000514");
    }

    /**
     * Get intensity values from base64 string which is uncompressed by the unmarshaller for the mzML file
     * @param filename the file name of the mzML file
     * @return 
     */
    @Override
    public double[] getIntensityData(String filename) {
        //MS:1000515 intensity array
        return getData(filename, "MS:1000515");
    }

    private double[] getData(String filename,String accession) {
        try {
            if(bdal == null){
                bdal = xTracker.study.getMzMLUnmarshaller(filename).getSpectrumById(id).getBinaryDataArrayList();
            }
            if (bdal == null || bdal.getCount()==0) return new double[0];
            List<BinaryDataArray> arrays = bdal.getBinaryDataArray();
            for(BinaryDataArray bda: arrays){
                List<CVParam> params = bda.getCvParam();
                for(CVParam param:params){
                    if(param.getAccession().equals(accession)){
                        Number[] values = bda.getBinaryDataAsNumberArray();
                        double[] ret = new double[values.length];
                        for (int i = 0; i < ret.length; i++) {
                            ret[i] = values[i].doubleValue();
                        }
                        return ret;
                    }
                }
            }
        } catch (MzMLUnmarshallerException ex) {
            Logger.getLogger(xSpectrumMzML.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new double[0];
    }

//    @Override
//    public double[] getIntensityData(String filename) {
//        try {
//            if(bdal == null){
//                bdal = xTracker.study.getMzMLUnmarshaller(filename).getSpectrumById(id).getBinaryDataArrayList();
//            }
//            if (bdal == null || bdal.getCount()==0) return new double[0];
//            List<BinaryDataArray> arrays = bdal.getBinaryDataArray();
//            for(BinaryDataArray bda: arrays){
//                List<CVParam> params = bda.getCvParam();
//                for(CVParam param:params){
//                    //MS:1000515 intensity array
//                    if(param.getAccession().equals("MS:1000515")){
//                        Number[] values = bda.getBinaryDataAsNumberArray();
//                        double[] ret = new double[values.length];
//                        for (int i = 0; i < ret.length; i++) {
//                            ret[i] = values[i].doubleValue();
//                        }
//                        return ret;
//                    }
//                }
//            }
//        } catch (MzMLUnmarshallerException ex) {
//            Logger.getLogger(xSpectrumMzML.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return new double[0];
//    }
}
