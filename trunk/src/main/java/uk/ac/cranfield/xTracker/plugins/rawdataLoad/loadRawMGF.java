//Every plugin has to be in the xtracker package.
package uk.ac.cranfield.xTracker.plugins.rawdataLoad;

//Some useful imports
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;

import java.util.List;
import uk.ac.cranfield.xTracker.Utils;
import uk.ac.liv.jmzqml.model.mzqml.RawFile;
import uk.ac.cranfield.xTracker.data.Identification;
import uk.ac.cranfield.xTracker.data.MSRun;
import uk.ac.cranfield.xTracker.data.xPeptideConsensus;
import uk.ac.cranfield.xTracker.data.xFeature;
import uk.ac.cranfield.xTracker.data.xPeptide;
import uk.ac.cranfield.xTracker.data.xProtein;
import uk.ac.cranfield.xTracker.data.xSpectrum;
import uk.ac.cranfield.xTracker.data.xSpectrumList;
import uk.ac.cranfield.xTracker.xTracker;

public class loadRawMGF extends rawData_loadPlugin {
    /**
     * Loads raw data from MGF files specified in the paramFile
     * The details of MGF format can be found at http://www.matrixscience.com/help/data_file_help.html
     * @param data the main data structure designed to contain everything, at this stage, it should contain identification information
     * @param paramFile the xml file specifying parameters of the plugin
     * @return data which is populated with spectral data within this plugin.
     */
    
    @Override
    public void start(String paramFile) {
        System.out.println(getName() + ": starting...");
        String rawDataFile = "";
        //this involves with reading raw files which are stored in msrun, so the only case loop through msrun first
        for(MSRun msrun:xTracker.study.getMSRuns()){
            HashMap<String,HashMap<String,ArrayList<Identification>>> index = new HashMap<String, HashMap<String, ArrayList<Identification>>>();
            for (xProtein protein:xTracker.study.getProteins()){
                ArrayList<xPeptideConsensus> peptideCons = protein.getPeptides();
                for (xPeptideConsensus pepCon : peptideCons) {
                    for (xPeptide peptide : pepCon.getPeptides()) {
                        ArrayList<xFeature> features = peptide.getFeatures(msrun.getID());
                        if(features==null) continue;//this peptide is not detected in that identification file
                        for (xFeature feature : features) {
                            for (Identification identification : feature.getIdentifications()) {
                                String location = identification.getSpectraDataLocation();
                                if(!location.toLowerCase().endsWith(".mgf")) continue;
                                HashMap <String,ArrayList<Identification>> map;
                                if(index.containsKey(location)){
                                    map = index.get(location);
                                }else{
                                    map = new HashMap<String, ArrayList<Identification>>();
                                    index.put(location, map);
                                }
                                ArrayList<Identification> idents;
                                final String specID = identification.getSpectrumID().trim();
                                if (map.containsKey(specID)){
                                    idents = map.get(specID);
                                }else{
                                    idents = new ArrayList<Identification>();
                                    map.put(specID, idents);
                                }
                                idents.add(identification);
                            }
                        }
                    }
                }
            }
            if(index.keySet().isEmpty()) continue;
            for(RawFile rawfile:msrun.getRawFilesGroup().getRawFile()){
                System.out.println(rawfile.getLocation());
                HashMap<String,ArrayList<Identification>> map = index.get(rawfile.getLocation());
                try{
                    String location = Utils.locateFile(rawfile.getLocation(), xTracker.folders);
                    BufferedReader in = new BufferedReader(new FileReader(location));
                    //PEPMASS is a must-have parameter, check for every MS2 spectrum
                    boolean foundPepmass = false;

                    String title = "";
                    //use array list to represent sepctrum
                    xSpectrum spec = new xSpectrumList();

                    String string = null;
                    // load the file reference
                    int lineNum = 0; //for quick locating the error in the mgf file
                    while ((string = in.readLine()) != null) {
                        lineNum++;
                        string = string.trim();
                        //find the first BEGIN IONS which indicates it is a MS2 file
                        if (string.startsWith("BEGIN IONS")) {
                            // nadda, we're going to be in a MS2 spectrum
                            break;
                        }
                    }
                    //the first BEGIN IONS line is already in the variable string
                    while ((string = in.readLine()) != null) {
                        lineNum++;
                        // make sure lines with a single white space are recognized as an
                        string = string.trim();
                        // if blank, get next
                        if (string.equals("")) {
                            continue;
                        }
                        // try to find parent mass info
                        // currently discard the intensity of parent ion
                        if (string.startsWith("PEPMASS")) {
                            Pattern pattern = Pattern.compile("^PEPMASS=(\\d*\\.?\\d+)");
                            Matcher m = pattern.matcher(string);
                            if (m.find()) {
                                foundPepmass = true;
                            }
                            continue;
                        }
                        //scan title is used to map spectra to identifications
                        //title line also can contain information for RT
                        //the keywords could include but not limited to elution (time)?, rt=?
                        //scan(s)? (number)? is also a possibility
                        if (string.startsWith("TITLE")) {
                            title = string.substring(6);
//                        System.out.println("title line: " + lineNum + " with title:" + title+"@@");
                            continue;
                        }
                        // handle peaks
                        if (string.startsWith("BEGIN IONS")) {
                            // nadda, we're into a new spectrum, clear all other values and set to default
                            foundPepmass = false;
                            title = "";
                            spec = new xSpectrumList();
                        }

                        // check for blank peak lists
                        if (string.contains("END IONS")) {
                            continue;
                        }

                        // if the line starts with 1-9, the actual peak values
                        if (string.charAt(0) >= '1' && string.charAt(0) <= '9') {
                            // parse all peaks
                            for (; string.indexOf("END IONS") == -1 && string != null; string = in.readLine()) {
                                // try, skip bad lines
                                try {
                                    // split on all whitespace
                                    String[] split = string.split("\\s+");
                                    //  set the first entry as parent ion mass and the associated charge
                                    try {
                                        double mz = Double.parseDouble(split[0]);
                                        double intensity = Double.parseDouble(split[1]);
                                        ((xSpectrumList) spec).addElem(mz, intensity);
//                                    System.out.println("Line: " + lineNum + " mz:" + mz + " intensity:" + intensity);
                                    } catch (NumberFormatException e) { // Any non-Peak data will return null (e.g. END IONS)
                                        System.out.println("Wrong spectral data at line " + lineNum);
                                        System.exit(1);
                                    }
                                } catch (Exception e) {
                                    // noop
                                }
                                lineNum++;
                            }
                            //now end ions line, validate the retrieved values;
                            //in the old implementation charge is checked, which is not a requirement
//                        System.out.println("Current line "+lineNum+ ": "+string);
                            if (!foundPepmass) {
                                System.out.println("No pepmass found in the section above over line " + lineNum);
                                System.exit(1);
                            }
                            if(map.containsKey(title)){
                                for(Identification ident: map.get(title)){
                                    ident.setMs2spectrum(spec);
                                }
//                                map.get(title).setMs2spectrum(spec);
                            }
                        }
                    }
                }catch(Exception e){
                    System.exit(1);
                }
            }
        }
        System.out.println(getName()+" finished");
    }

    @Override
    public boolean supportMS1(){
        return false;
    }

    @Override
    public boolean supportMS2(){
        return true;
    }

    /**
     * Gets the plugin name.
     * @return pluginName
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the plugin version.
     * @return plugin version
     */
    @Override
    public String getVersion() {
        return version;
    }
    /**
     * Gets the plugin description.
     * @return plugin description
     */
    @Override
    public String getDescription() {
        return description;
    }
    //The vector of rawDataFiles
    List<String> rawDataFiles = new ArrayList<String>();
    //The plugin name
    private final static String name = "Load MGF spectral file";
    //The plugin version
    private final static String version = "1.00";
    //The plugin type(do not modify the string otherwise xTracker won't recognize the plugin!)
//    private final static String type = "RAWDATA_LOAD_plugin";
    //The plugin description
    private final static String description = "\t\tThis plugin loads MS/MS data from .MGF files.\n\tRetention time has to be specified in the \"TITLE\"\n\tsection of the file as \"Elution: XXX\" or as \"rt=XXX\".\n\tScan numbers are also accepted (though they do not suit some labeling techniques) \"Scan Number:XXX\".";
}