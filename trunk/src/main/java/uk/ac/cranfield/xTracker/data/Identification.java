package uk.ac.cranfield.xTracker.data;

import java.util.List;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.liv.jmzqml.model.mzqml.Feature;

/**
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class Identification extends QuantitationLevel{
    /**
     * identification id, could be scan title in MGF, or SpectrumIdentificationResult id in mzIdentML
     */
    private String id;
    /**
     * spectral file location
     */
    private String spectraDataLocation;
    /**
     * the spectrum id in the spectral file
     */
    private String spectrumID;
    /**
     * the corresponding spectrum where the identification is detected
     */
    private xSpectrum ms2spectrum = null;
    /**
     * the top SII in the SIR, which pass the threshold and ranks first
     */
    private SpectrumIdentificationItem sii;
    /**
     * the CV parameters which contains the extra information including confidence level
     */
    private List<CvParam> cvParams;
    /**
     * the id of the feature this identification relates to
     */
    private String feature_ref;
    /**
     * the name of the identification file
     */
    private String identificationFile;
    /**
     * the list of m/z values to monitor for product ions
     */
    private List<Float> srmIons;
    
    private double mz = 0d;
//    private HashMap<String,Double> quantities;

    public Identification(String id, String spectraDataLocation, String spectrumID, SpectrumIdentificationItem sii, List<CvParam> cvParams, String identificationFile) {
        this.id = id;
        this.spectraDataLocation = spectraDataLocation;
        this.spectrumID = spectrumID;
        this.sii = sii;
        this.cvParams = cvParams;
        this.identificationFile = identificationFile;
    }
    /**
     * Get the mz value
     * @return 
     */
    public double getMz() {
        return mz;
    }
    /**
     * Set the mz value
     * @param mz 
     */
    public void setMz(double mz) {
        this.mz = mz;
    }
    /**
     * Get the MS2 spectrum where this identification occurs
     * @return 
     */
    public xSpectrum getMs2spectrum() {
        return ms2spectrum;
    }
    /**
     * Set the MS2 spectrum where this identification occurs
     * @param ms2spectrum 
     */
    public void setMs2spectrum(xSpectrum ms2spectrum) {
        this.ms2spectrum = ms2spectrum;
    }
    /**
     * Get the related feature
     * @return 
     */
    public String getFeature_ref() {
        return feature_ref;
    }
    /**
     * Set the related feature
     * @param feature_ref 
     */
    public void setFeature_ref(String feature_ref) {
        this.feature_ref = feature_ref;
    }
    /**
     * Get the expected product ion mz values in SRM
     * @return 
     */
    public List<Float> getSrmIons() {
        return srmIons;
    }
    /**
     * Set the expected product ion mz values in SRM
     * @param srmIons 
     */
    public void setSrmIons(List<Float> srmIons) {
        this.srmIons = srmIons;
    }
    /**
     * Get the extra information about this identification
     * @return 
     */
    public List<CvParam> getCvParams() {
        return cvParams;
    }
    /**
     * Get the identification id
     * @return 
     */
    public String getId() {
        return id;
    }
    /**
     * Get the identification file where this identification is contained
     * @return 
     */
    public String getIdentificationFile() {
        return identificationFile;
    }
    /**
     * Get the spectral identification item for this identification from the file in mzIdentML format
     * @return 
     */
    public SpectrumIdentificationItem getSii() {
        return sii;
    }
    /**
     * Get the spectral file
     * @return 
     */
    public String getSpectraDataLocation() {
        return spectraDataLocation;
    }
    /**
     * Get the spectrum id
     * @return 
     */
    public String getSpectrumID() {
        return spectrumID;
    }
    /**
     * Get the feature mzQuantML element for MS2 pipeline
     * @return 
     */
    public Feature convertToQfeature(){
        Feature feature = new Feature();
        feature.setId(getMz()+"_"+getId());
        if (getSii() == null) {//not from mzIdentML file
            feature.setSpectrumRefs(id);
        } else {
            feature.setSpectrumRefs(getSii().getId());
        }
        feature.setMz(getMz());
        feature.setRt("");
        return feature;
    }
}
