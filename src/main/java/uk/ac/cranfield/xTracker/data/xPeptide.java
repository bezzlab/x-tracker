package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import uk.ac.cranfield.xTracker.xTracker;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;

/**
 *
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class xPeptide extends QuantitationLevel {
    private String seq;
    private String peptideID;
    /**
     * features are rawFilesGroup specific, which is corresponding to msrun 
     * so suitable for a hash map: keys are msrun id and values are the list of features
     */
    private HashMap<String,ArrayList<xFeature>> features;
    private HashSet<xModification> modifications;

    public xPeptide(String seq) {
        this(seq, new HashSet<xModification>()); 
    }
    
    public xPeptide(String seq, HashSet<xModification> mods){
        this.seq = seq;
        features = new HashMap<String, ArrayList<xFeature>>();
        modifications = mods;
    }
    /**
     * Get the peptide sequence
     * @return 
     */
    public String getSeq() {
        return seq;
    }
    /**
     * Get all features for a specific msrun
     * @param msrun
     * @return 
     */
    public ArrayList<xFeature> getFeatures(String msrun) {
        if(features.containsKey(msrun)){
            return features.get(msrun);
        }
        return null;
    }
    /**
     * Get all features from all runs
     * @return 
     */
    public ArrayList<xFeature> getAllFeatures(){
        ArrayList<xFeature> ret = new ArrayList<xFeature>();
        for(ArrayList<xFeature> one:features.values()){
            ret.addAll(one);
        }
        return ret;
    }
    /**
     * Get all identifications
     * @return 
     */
    public ArrayList<Identification> getAllIdentifications(){
        ArrayList<Identification> ret = new ArrayList<Identification>();
        for(xFeature feature:getAllFeatures()){
            ret.addAll(feature.getIdentifications());
        }
        return ret;
    }
    /**
     * Get the modification pattern
     * @return 
     */
    public HashSet<xModification> getModifications() {
        return modifications;
    }
    /**
     * Get the feature specific to the msrun and the required charge
     * @param msrun
     * @param charge
     * @return 
     */
    public xFeature getFeature(String msrun, int charge){
        ArrayList<xFeature> featureList = getFeatures(msrun);
        if(featureList == null) return null;
        for(xFeature feature:featureList){
            if(feature.getCharge() == charge) return feature;
        }
        return null;
    }
    /**
     * Add a feature to the msrun-specific feature list
     * @param msrun
     * @param feature 
     */
    public void addFeature(String msrun,xFeature feature) {
        ArrayList<xFeature> featureList = getFeatures(msrun);
        if(featureList==null){
            featureList = new ArrayList<xFeature>();
            features.put(msrun, featureList);
        }
        featureList.add(feature);
    }
    /**
     * Generate a PeptideConsensus mzQuantML element for exportation
     * @return 
     */
    public PeptideConsensus convertToQpeptideConsensus(){
        PeptideConsensus pc = new PeptideConsensus();
        pc.setId(getPeptideID());
        pc.setPeptideSequence(seq);
        //set charge
        HashSet<Integer> charges = new HashSet<Integer>();
        for(String msrun:features.keySet()){
            for (xFeature feature : features.get(msrun)) {
                charges.add(feature.getCharge());
            }
        }
        for(Integer charge:charges){
            pc.getCharge().add(String.valueOf(charge));
        }
        //set modification
        for(xModification mod:modifications){
            pc.getModification().add(mod.convertToQmodification());
        }
        return pc;
    }
    /**
     * Get the peptide id
     * @return 
     */
    public String getPeptideID() {
        return peptideID;
    }
    /**
     * Set the peptide id
     * @param peptideID 
     */
    public void setPeptideID(String peptideID) {
        this.peptideID = peptideID;
    }
    /**
     * Get all msruns where this peptide is observed
     * @return 
     */
    public Set<String> getMSRunIDs(){
        return features.keySet();
    }

    @Override
    public int getCount(){
        if(xTracker.study.getPipelineType()==Study.MS1_TYPE){
            return getAllFeatures().size();
        }else{
            return getAllIdentifications().size();
        }
    }
}
