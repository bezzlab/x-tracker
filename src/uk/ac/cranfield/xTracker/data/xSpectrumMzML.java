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
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class xSpectrumMzML implements xSpectrum {

    private String id;
//    MzMLUnmarshaller unmarshaller;
    BinaryDataArrayList bdal = null;

    public xSpectrumMzML(String idIn) {
        id = idIn;
    }

//    public void setUnmarshaller(MzMLUnmarshaller unmarshaller) {
//        try {
//            //        unmarshaller = unmarshallerIn;
//            bdal = unmarshaller.getSpectrumById(id).getBinaryDataArrayList();
//        } catch (MzMLUnmarshallerException ex) {
//            Logger.getLogger(xSpectrumMzML.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
    @Override
    public double[] getMzData(String filename) {
        try {
            if(bdal == null){
                bdal = xTracker.study.getMzMLUnmarshaller(filename).getSpectrumById(id).getBinaryDataArrayList();
            }
            if (bdal == null || bdal.getCount()==0) return new double[0];
            List<BinaryDataArray> arrays = bdal.getBinaryDataArray();
            for(BinaryDataArray bda: arrays){
                List<CVParam> params = bda.getCvParam();
                for(CVParam param:params){
                    //MS:1000514 m/z array
                    if(param.getAccession().equals("MS:1000514")){
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

    @Override
    public double[] getIntensityData(String filename) {
        try {
            if(bdal == null){
                bdal = xTracker.study.getMzMLUnmarshaller(filename).getSpectrumById(id).getBinaryDataArrayList();
            }
            if (bdal == null || bdal.getCount()==0) return new double[0];
            List<BinaryDataArray> arrays = bdal.getBinaryDataArray();
            for(BinaryDataArray bda: arrays){
                List<CVParam> params = bda.getCvParam();
                for(CVParam param:params){
                    //MS:1000515 intensity array
                    if(param.getAccession().equals("MS:1000515")){
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
}
