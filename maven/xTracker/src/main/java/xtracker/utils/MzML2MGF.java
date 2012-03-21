/*
 * --------------------------------------------------------------------------
 * MzML2MGF.java
 * --------------------------------------------------------------------------
 * Description:       Plugin to Convert mzML files to MGF
 * Developer:         Faviel Gonzalez
 * Created:           09 February 2012
 * Read our documentation file under our Google SVN repository
 * SVN: http://code.google.com/p/x-tracker/
 * Project Website: http://www.x-tracker.info/
 * --------------------------------------------------------------------------
 */

package xtracker.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.PrecursorList;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;



/**
 *
 * @author Faviel Gonzalez
 */
public class MzML2MGF {
        
    private ArrayList<MzMLUnmarshaller> iUnmarshaller = null;    
    
    /**
     * Plugin to convert MzML files to MGF
     * @param paramFile a string containing the file name of parameters if any.	     
     */    
    public MzML2MGF(String paramFile) {
        
        File xmlFile = new File(paramFile);
        String paramFile2 = "";
        paramFile2 = paramFile.replace(".mzML", ".mgff");
        String sOut = "";
        try{
            
            FileWriter fstream = new FileWriter(paramFile2);
            BufferedWriter out = new BufferedWriter(fstream);                        

            //... Unmarshall data using jzmzML API ...//
            iUnmarshaller = new ArrayList<MzMLUnmarshaller>();
            MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(xmlFile);
            iUnmarshaller.add(unmarshaller);

            //... Reading spectrum data ...//
            MzMLObjectIterator<Spectrum> spectrumIterator = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
            while (spectrumIterator.hasNext())
            {
                //... Reading each spectrum ...//
                Spectrum spectrum = spectrumIterator.next();

                //... Reading CvParam to identify the MS level (1, 2) ...//
                String mslevel = "";
                List<CVParam> specParam = spectrum.getCvParam();
                for (Iterator lCVParamIterator = specParam.iterator(); lCVParamIterator.hasNext();)
                {
                    CVParam lCVParam = (CVParam) lCVParamIterator.next();
                    if (lCVParam.getAccession().equals("MS:1000511"))
                    {
                        mslevel = lCVParam.getValue().trim();
                    }
                }            

                //... Getting Retention Time (rt) ...//
                float rt = 0;
                String unitRT = "";
                List<CVParam> scanParam = spectrum.getScanList().getScan().get(0).getCvParam();
                for (Iterator lCVParamIterator = scanParam.iterator(); lCVParamIterator.hasNext();)
                {
                    CVParam lCVParam = (CVParam) lCVParamIterator.next();
                    if (lCVParam.getAccession().equals("MS:1000016"))
                    {
                        unitRT = lCVParam.getUnitAccession().trim();
                        if (unitRT.equals("UO:0000031"))
                        {    
                            rt = Float.parseFloat(lCVParam.getValue().trim()) * 60;
                        }
                        else
                        {
                            rt = Float.parseFloat(lCVParam.getValue().trim());
                        }
                    }                        
                }            
                if (mslevel.toString().indexOf("2") >= 0)
                {
                    PrecursorList plist = spectrum.getPrecursorList(); //... Get precursor ion ...//
                    if (plist != null)
                    {                    
                        if (plist.getCount().intValue() == 1)
                        {                    
                            List<CVParam> scanPrecParam = plist.getPrecursor().get(0).getSelectedIonList().getSelectedIon().get(0).getCvParam();
                            float parIonMz =0;
                            float peakIntensity =0;
                            int parCharge = 0;

                            //... Detect parent ion m/z and charge 
                            for (Iterator lCVParamIterator = scanPrecParam.iterator(); lCVParamIterator.hasNext();)
                            {
                                CVParam lCVParam = (CVParam) lCVParamIterator.next();
                                if (lCVParam.getAccession().equals("MS:1000744"))
                                {
                                    parIonMz = Float.parseFloat(lCVParam.getValue().trim());
                                }
                                if (lCVParam.getAccession().equals("MS:1000041"))
                                {
                                    parCharge = Integer.parseInt(lCVParam.getValue().trim());
                                }
                                if (lCVParam.getAccession().equals("MS:1000042"))
                                {
                                    peakIntensity = Float.parseFloat(lCVParam.getValue().trim());
                                }                            
                            }      

                            //... Binary data ...//
                            List<BinaryDataArray> bdal = spectrum.getBinaryDataArrayList().getBinaryDataArray();

                            //... Reading mz Values (Peaks) ...//
                            BinaryDataArray mzBinaryDataArray = (BinaryDataArray) bdal.get(0);
                            BinaryDataArray intenBinaryDataArray = (BinaryDataArray) bdal.get(1);
                            Number[] mzValues = mzBinaryDataArray.getBinaryDataAsNumberArray();
                            Number[] intenValues = intenBinaryDataArray.getBinaryDataAsNumberArray();

                            int iJ=0;
                            float mzVal;
                            float intVal;
                            if (parCharge == 0) 
                            {
                                parCharge = 1;
                            }                            
                            //System.out.println("BEGIN IONS");
                            out.write("BEGIN IONS");
                            out.newLine();
                            
                            sOut = "TITLE=Spectrum1 scans:" + spectrum.getIndex() + ",";
                            //System.out.println(sOut);
                            out.write(sOut);
                            out.newLine();
                            
                            sOut = "PEPMASS=" + String.format("%.4f", parIonMz) + " " + String.format("%.4f", peakIntensity);
                            //System.out.println(sOut);                            
                            out.write(sOut);
                            out.newLine();
                            
                            sOut = "CHARGE=" + parCharge + "+";
                            //System.out.println(sOut);                            
                            out.write(sOut);
                            out.newLine();
                            
                            sOut = "RTINSECONDS=" + String.format("%.4f", rt);
                            //System.out.println(sOut);
                            out.write(sOut);
                            out.newLine();
                            
                            sOut = "SCANS=" + spectrum.getIndex();
                            //System.out.println(sOut);
                            out.write(sOut);
                            out.newLine();
                            
                            while (iJ < mzValues.length)
                            {
                                mzVal = mzValues[iJ].floatValue();
                                intVal = intenValues[iJ].floatValue();
                                if (intVal > 0)
                                {
                                    sOut = String.format("%.4f", mzVal) + " " + String.format("%.4f", intVal);
                                    //System.out.println(sOut);
                                    out.write(sOut);
                                    out.newLine();
                                }
                                iJ++;
                            }           
                            //System.out.println("END IONS");
                            out.write("END IONS");
                            out.newLine();
                        }
                    }  //... If precursor ion
                } //... If MS2    
            }   //... While
            out.close();                
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            System.exit(0);                                
        }                
        return;

    }

}

