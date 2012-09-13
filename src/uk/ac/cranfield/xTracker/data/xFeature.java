package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import uk.ac.liv.jmzqml.model.mzqml.Feature;

/**
 *
 * @author Jun Fan@cranfield
 */
public class xFeature extends QuantitationLevel{
    private int charge;
//    private String proteinAccession;
    /**
     * Peptide ID, normally in the form of peptideSeq_modificationString
     */
    private String msrunID;
    private String peptideID;
    private ArrayList<Identification> identifications;
    
    public xFeature(String msrun,String peptideID, int charge){
//    public xFeature(String proteinAccession,String peptideID, int charge){
//        this.proteinAccession = proteinAccession;
        this.peptideID = peptideID;
        this.charge = charge;
        msrunID = msrun;
        identifications = new ArrayList<Identification>();
    }

    public int getCharge() {
        return charge;
    }

    public String getId() {
        StringBuilder sb = new StringBuilder();
        sb.append(msrunID);
        sb.append("-");
        sb.append(peptideID);
        sb.append("-");
        sb.append(charge);
//        sb.append("-");
//        sb.append(getMZ());
        return sb.toString();
    }

    public ArrayList<Identification> getIdentifications() {
        return identifications;
    }
    
    public void addIdentification(Identification identification){
        identifications.add(identification);
    }
    
    public double getMZ(){
        double sum = 0;
        for(Identification iden:identifications){
            sum += iden.getMz();
        }
        return sum/identifications.size();
    }
    
    public Double getRT(){
        return null;
    }
    
    public Feature convertToQfeature(){
        Feature feature = new Feature();
        feature.setId(getId());
        feature.setCharge(String.valueOf(charge));
        feature.setMz(getMZ());
        feature.setRt(getRT()+"");
        return feature;
    }
    
    @Override
    public int getCount(){
        return identifications.size();
    }
}
