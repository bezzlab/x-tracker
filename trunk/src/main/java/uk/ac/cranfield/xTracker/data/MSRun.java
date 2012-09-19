package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.List;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.RawFilesGroup;

/**
 * This class is corresponding to RawFilesGroup element in mzQuantML
 * Therefore it holds some information about input files and assays
 * @author Jun Fan@cranfield
 */
public class MSRun {
    private RawFilesGroup rawFilesGroup;
    private List<Assay> assays;
//    private ArrayList<MS1spectrum> MS1spectra;
//    private boolean sorted = false;

    public MSRun(RawFilesGroup rfg){
        rawFilesGroup = rfg;
        assays = new ArrayList<Assay>();
//        MS1spectra = new ArrayList<MS1spectrum>();
    }
    
//    public void addMS1Spectrum(MS1spectrum spec){
//        MS1spectra.add(spec);
//    }
//    
//    public MS1spectrum getSpectrum(){
//        if(!sorted){
//            Collections.sort(MS1spectra);
//            sorted = true;
//        }
//        return MS1spectra.get(0);
//    }
    /**
     * Add an assay to the list
     * @param assay 
     */
    public void addAssay(Assay assay){
        assays.add(assay);
    }

//    public ArrayList<MS1spectrum> getMS1spectra() {
//        return MS1spectra;
//    }
    /**
     * Get all assays related to the MS run
     * @return 
     */
    public List<Assay> getAssays() {
        return assays;
    }
    /**
     * Get the corresponding mzQuantML element: RawFilesGroup
     * @return 
     */
    public RawFilesGroup getRawFilesGroup() {
        return rawFilesGroup;
    }
    /**
     * Get the id
     * @return 
     */
    public String getID(){
        return rawFilesGroup.getId();
    }
}
