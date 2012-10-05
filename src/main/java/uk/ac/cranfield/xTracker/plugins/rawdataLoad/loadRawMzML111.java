/*
 * --------------------------------------------------------------------------
 * loadRawMzML111.java
 * --------------------------------------------------------------------------
 * Description:       Plugin to load mzML files (Version 1.1)
 * Developer:         Jun Fan,Faviel Gonzalez
 * Created:           03 February 2012
 * Read our documentation file under our Google SVN repository
 * SVN: http://code.google.com/p/x-tracker/
 * Project Website: http://www.x-tracker.info/
 * --------------------------------------------------------------------------
 */
package uk.ac.cranfield.xTracker.plugins.rawdataLoad;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import uk.ac.cranfield.xTracker.Utils;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.ParamGroup;
import uk.ac.ebi.jmzml.model.mzml.Precursor;
import uk.ac.ebi.jmzml.model.mzml.PrecursorList;
import uk.ac.ebi.jmzml.model.mzml.Scan;
import uk.ac.ebi.jmzml.model.mzml.ScanList;
import uk.ac.ebi.jmzml.model.mzml.SelectedIonList;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.liv.jmzqml.model.mzqml.RawFile;
import uk.ac.cranfield.xTracker.data.Identification;
import uk.ac.cranfield.xTracker.data.MSRun;
import uk.ac.cranfield.xTracker.data.xPeptideConsensus;
import uk.ac.cranfield.xTracker.data.Study;
import uk.ac.cranfield.xTracker.data.xFeature;
import uk.ac.cranfield.xTracker.data.xPeptide;
import uk.ac.cranfield.xTracker.data.xProtein;
import uk.ac.cranfield.xTracker.data.xSpectrum;
import uk.ac.cranfield.xTracker.data.xSpectrumMzML;
import uk.ac.cranfield.xTracker.xTracker;

/**
 *
 * @author Jun Fan@cranfield,Faviel Gonzalez@liverpool
 */
public class loadRawMzML111 extends rawData_loadPlugin {

    ArrayList<String> rawDataFiles = new ArrayList<String>();
    private final static String name = "loadRawMzML111";
    private final static String version = "1.0";
    private final static String description = "This plugin loads raw data (MS and MS/MS) from .mzML raw files (Version 1.1.1)";
    /**
     * The start method. 
     * @param paramFile a string (that might be empty) containing the file name of parameters if any.	
     * @return a valid xLoad structure filled with raw data information only.
     */
    @Override
    public void start(String paramFile) {
        System.out.println("Loading " + getName() + " plugin ...");

        //this involves with reading raw files which are stored in msrun, so the only case loop through msrun first
        if(xTracker.study.getPipelineType()==Study.MS2_TYPE){
            for (MSRun msrun : xTracker.study.getMSRuns()) {
                HashMap<String, HashMap<String, Identification>> index = new HashMap<String, HashMap<String, Identification>>();
                for (xProtein protein : xTracker.study.getProteins()) {
                    ArrayList<xPeptideConsensus> peptideCons = protein.getPeptides();
                    for (xPeptideConsensus pepCon : peptideCons) {
                        for(xPeptide peptide:pepCon.getPeptides()){
                            ArrayList<xFeature> features = peptide.getFeatures(msrun.getID());
                            if(features==null) continue;//this peptide is not detected in that identification file
                            for(xFeature feature: features){
                                for(Identification identification:feature.getIdentifications()){
                                    String rawfile = identification.getSpectraDataLocation();
                                    if(rawfile.toLowerCase().endsWith(".mzml")){
                                        MzMLUnmarshaller unmarshaller = xTracker.study.getMzMLUnmarshaller(rawfile);
                                        if(unmarshaller==null){
                                            System.out.println("Creating unmarshaller for the raw file:"+rawfile);
                                            String location = Utils.locateFile(rawfile, xTracker.folders);
                                            File file = new File(location);
                                            if(!file.exists()){
                                                System.out.println("The required raw file "+file.getAbsolutePath()+" does not exist");
                                                System.exit(1);
                                            }
                                            unmarshaller = new MzMLUnmarshaller(file);
                                            xTracker.study.addMzMLUnmarshaller(rawfile, unmarshaller);
                                        }
                                        Spectrum spec = null;
                                        try{
                                            spec = unmarshaller.getSpectrumById(identification.getSpectrumID());
                                        }catch(Exception e){
                                        }
                                        if(spec==null){
                                            HashMap<String, Identification> map;
                                            if (index.containsKey(rawfile)) {
                                                map = index.get(rawfile);
                                            } else {
                                                map = new HashMap<String, Identification>();
                                                index.put(rawfile, map);
                                            }
                                            map.put(identification.getSpectrumID(), identification);
                                        }else{
                                            xSpectrum spectrum = new xSpectrumMzML(identification.getSpectrumID());
                                            identification.setMs2spectrum(spectrum);
                                        }
                                    }else{
                                        System.out.println("Un-recognized file suffix for this "+getName()+" plugin from the raw file "+rawfile);
                                    }
                                }
                            }
                        }
                    }
                }
                for (RawFile rawfile : msrun.getRawFilesGroup().getRawFile()) {
                    System.out.println(rawfile.getLocation());
                    HashMap<String, Identification> map = index.get(rawfile.getLocation());
                    if(map == null) continue;
                    MzMLUnmarshaller unmarshaller = xTracker.study.getMzMLUnmarshaller(rawfile.getLocation());
                    MzMLObjectIterator<Spectrum> spectrumIterator = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
                    while (spectrumIterator.hasNext()) {
                        //read next spectrum from XML file
                        Spectrum spectrum = spectrumIterator.next();
                        String title = parseScanTitle(spectrum);
                        if(title.length()>0){
                            if(map.containsKey(title)){
                                map.get(title).setMs2spectrum(new xSpectrumMzML(spectrum.getId()));
                            }
                        }
                    }
                }
            }//end of msrun
        }else{//MS1
//            MzMLUnmarshaller unmarshaller = xTracker.study.getMzMLUnmarshaller("rawfilename");
//            //... Reading spectrum data ...//
//            MzMLObjectIterator<Spectrum> spectrumIterator = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
////            xLoadData inputData = new xLoadData(rawDataFile);
//            while (spectrumIterator.hasNext()) {
//                //... Reading each spectrum ...//
//                Spectrum spectrum = spectrumIterator.next();
//                //... Reading CvParam to get spectrum title//
//                String scanTitle = parseScanTitle(spectrum);
//                //... Reading CvParam to identify the MS level (1, 2) ...//
//                int msLevel = parseMsLevel(spectrum);
////                System.out.println("SpectrumID = " + spectrum.getId() + " with MS level = " + msLevel);
//                //... Getting Retention Time (rt) ...//
//                double rt = parseRetentionTime(spectrum);
//                if (msLevel == 1) {
//                    //TODO: after retention time is done
//                    xSpectrum msSpectrum = new xSpectrumMzML(spectrum.getId());
////                    inputData.addLcMsData((float) rt, msSpectrum);
//                    // System.out.println("It has read the first spectrum with mz and intensities at a given rt. ");                        
//                }
//            }
        }
        System.out.println("Load raw mzML plugin done");
    }

    private double[] parsePrecursor(Spectrum spectrum) {
        double[] ret = {0,0}; //element 0 mz, element 1 charge
	PrecursorList precursorList = spectrum.getPrecursorList();
	if ((precursorList == null) || (precursorList.getCount() == 0))  return ret;

	List<Precursor> precursorElements = precursorList.getPrecursor();
	for (Precursor precursor : precursorElements) {
	    SelectedIonList selectedIonList = precursor.getSelectedIonList();
	    if ((selectedIonList == null)  || (selectedIonList.getCount() == 0)) return ret;
	    List<ParamGroup> selectedIonParams = selectedIonList.getSelectedIon();
	    if (selectedIonParams == null) continue;

	    for (ParamGroup pg : selectedIonParams) {
		List<CVParam> pgCvParams = pg.getCvParam();
		for (CVParam param : pgCvParams) {
		    String accession = param.getAccession();
		    String value = param.getValue();
		    if ((accession == null) || (value == null))	continue;
		    // MS:1000040 is used in mzML 1.0,
		    // MS:1000744 is used in mzML 1.1.0
		    if (accession.equals("MS:1000040")  || accession.equals("MS:1000744")) {
			ret[0] = Double.parseDouble(value);
		    }
		    if (accession.equals("MS:1000041")) {
			ret[1] = Integer.parseInt(value);
		    }
		}
	    }
	}
	return ret;
    }

    private double parseRetentionTime(Spectrum spectrum) {
        ScanList scanList = spectrum.getScanList();
        if (scanList == null) return 0;
        List<Scan> scanElements = scanList.getScan();
        if (scanElements == null) return 0;
        for (Scan scan : scanElements) {
            List<CVParam> cvParams = scan.getCvParam();
            if (cvParams == null) continue;
            for (CVParam param : cvParams) {
                String accession = param.getAccession();
                String value = param.getValue();
                if ((accession == null) || (value == null)) continue;
                // "Scan start time" MS:1000016
                if (accession.equals("MS:1000016")) {
                    // MS:1000038 is used in mzML 1.0, while UO:0000031
                    // is used in mzML 1.1.0 
                    String unitAccession = param.getUnitAccession();
                    if ((unitAccession == null) || (unitAccession.equals("MS:1000038")) || unitAccession.equals("UO:0000031")) {
                        return Double.parseDouble(value);
                    } else {
                        return Double.parseDouble(value) / 60d;
                    }
                }
            }
        }
        return 0;
    }

    private int parseMsLevel(Spectrum spectrum) {
        List<CVParam> specParams = spectrum.getCvParam();
        if (specParams == null) {
            return 1;
        }
        for (CVParam param : specParams) {
            if (param.getAccession().equals("MS:1000511")) {
                return Integer.parseInt(param.getValue());
            }
        }
        return 1;
    }
    
    private String parseScanTitle(Spectrum spectrum) {
        List<CVParam> specParams = spectrum.getCvParam();
        if (specParams == null) {
            return "";
        }
        for (CVParam param : specParams) {
            if (param.getAccession().equals("MS:1000796")) {
                return param.getValue();
            }
        }
        return "";
    }

    /**
     * Method that loads the parameters used in the plugin from an xml file
     * @param dataFile The complete path to the xml file to read in
     */

    @Override
    public boolean supportMS1(){
        return true;
    }

    @Override
    public boolean supportMS2(){
        return true;
    }

    /**
     * Method to retrieve the name of the plugin.
     * @return A string with the plugin name.	
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Method to retrieve the version of the plugin.
     * @return A string with the plugin version.	
     */
    @Override
    public String getVersion() {
        return version;
    }
    /**
     * Method to retrieve the description of the plugin.
     * @return A string with the plugin description.	
     */
    @Override
    public String getDescription() {
        return description;
    }
}
