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
    
    private double mz;
//    private HashMap<String,Double> quantities;

    public Identification(String id, String spectraDataLocation, String spectrumID, SpectrumIdentificationItem sii, List<CvParam> cvParams, String identificationFile) {
        this.id = id;
        this.spectraDataLocation = spectraDataLocation;
        this.spectrumID = spectrumID;
        this.sii = sii;
        this.cvParams = cvParams;
        this.identificationFile = identificationFile;
//        quantities = new HashMap<String, Double>();
    }

    public double getMz() {
        return mz;
    }

    public void setMz(double mz) {
        this.mz = mz;
    }

    public xSpectrum getMs2spectrum() {
        return ms2spectrum;
    }

    public void setMs2spectrum(xSpectrum ms2spectrum) {
        this.ms2spectrum = ms2spectrum;
    }

    public String getFeature_ref() {
        return feature_ref;
    }
    
    public void setFeature_ref(String feature_ref) {
        this.feature_ref = feature_ref;
    }

    public List<Float> getSrmIons() {
        return srmIons;
    }

    public void setSrmIons(List<Float> srmIons) {
        this.srmIons = srmIons;
    }

    public List<CvParam> getCvParams() {
        return cvParams;
    }

    public String getId() {
        return id;
    }

    public String getIdentificationFile() {
        return identificationFile;
    }

    public SpectrumIdentificationItem getSii() {
        return sii;
    }

    public String getSpectraDataLocation() {
        return spectraDataLocation;
    }

    public String getSpectrumID() {
        return spectrumID;
    }
    
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
