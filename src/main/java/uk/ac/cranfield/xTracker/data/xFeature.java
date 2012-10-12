package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import uk.ac.liv.jmzqml.model.mzqml.Feature;

/**
 *
 * @author Jun Fan@cranfield
 */
public class xFeature extends QuantitationLevel{
    /**
     * the charge value
     */
    private int charge;
    /**
     * The ms run id this feature belongs to
     */
    private String msrunID;
    /**
     * Peptide ID, normally in the form of peptideSeq_modificationString
     * For generating feature ID only, not a reference back to peptide
     */
    private String peptideID;
    private ArrayList<Identification> identifications;
    
    public xFeature(String msrun,String peptideID, int charge){
        this.peptideID = peptideID;
        this.charge = charge;
        msrunID = msrun;
        identifications = new ArrayList<Identification>();
    }
    /**
     * Get the charge
     * @return 
     */
    public int getCharge() {
        return charge;
    }
    /**
     * Get the feature id
     * @return 
     */
    public String getId() {
        StringBuilder sb = new StringBuilder();
        sb.append(msrunID);
        sb.append("-");
        sb.append(peptideID);
        sb.append("-");
        sb.append(charge);
        return sb.toString();
    }
    /**
     * Get all identification under the feature
     * @return 
     */
    public ArrayList<Identification> getIdentifications() {
        return identifications;
    }
    /**
     * Add one identification
     * @param identification 
     */
    public void addIdentification(Identification identification){
        identifications.add(identification);
    }
    /**
     * Calculate the average m/z of the region which the feature represents
     * @return 
     */
    public double getMZ(){
        double sum = 0;
        for(Identification iden:identifications){
            sum += iden.getMz();
        }
        return sum/identifications.size();
    }
    /**
     * Unimplemented as now only MS2 considered
     * @return 
     */
    public Double getRT(){
        return null;
    }
    /**
     * Get the feature mzQuantML element
     * @return 
     */
    public Feature convertToQfeature(){
        Feature feature = new Feature();
        feature.setId(getId());
        feature.setCharge(String.valueOf(charge));
        feature.setMz(getMZ());
        feature.setRt(String.valueOf(getRT()));
        return feature;
    }
    
    @Override
    public int getCount(){
        return identifications.size();
    }
}
