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
     */
    private HashMap<String,ArrayList<xFeature>> features;
//    private ArrayList<Identification> identifications;
    private HashSet<xModification> modifications;
//    private HashMap<String,Double> quantitations;
//    private HashMap<String,Float> errors;

    public xPeptide(String seq) {
        this(seq, new HashSet<xModification>()); 
//        quantitations = new HashMap<String, Double>();
//        errors = new HashMap<String, Float>();
    }
    
    public xPeptide(String seq, HashSet<xModification> mods){
        this.seq = seq;
        features = new HashMap<String, ArrayList<xFeature>>();
//        identifications = new ArrayList<Identification>();
        modifications = mods;
    }
    
    public String getSeq() {
        return seq;
    }

    public ArrayList<xFeature> getFeatures(String msrun) {
        if(features.containsKey(msrun)){
            return features.get(msrun);
        }
        return null;
    }
    
    public ArrayList<xFeature> getAllFeatures(){
        ArrayList<xFeature> ret = new ArrayList<xFeature>();
        for(ArrayList<xFeature> one:features.values()){
            ret.addAll(one);
        }
        return ret;
    }
    
    public ArrayList<Identification> getAllIdentifications(){
        ArrayList<Identification> ret = new ArrayList<Identification>();
        for(xFeature feature:getAllFeatures()){
            ret.addAll(feature.getIdentifications());
        }
        return ret;
    }

    public HashSet<xModification> getModifications() {
        return modifications;
    }
    
    public xFeature getFeature(String msrun, int charge){
        ArrayList<xFeature> featureList = getFeatures(msrun);
        if(featureList == null) return null;
        for(xFeature feature:featureList){
            if(feature.getCharge() == charge) return feature;
        }
        return null;
    }

    public void addFeature(String msrun,xFeature feature) {
        ArrayList<xFeature> featureList = getFeatures(msrun);
        if(featureList==null){
            featureList = new ArrayList<xFeature>();
            features.put(msrun, featureList);
        }
        featureList.add(feature);
//        ArrayList<xFeature> featureList;
//        if(features.containsKey(msrun)){
//            featureList = features.get(msrun);
//            featureList.add(feature);
//        }else{
//            featureList = new ArrayList<xFeature>();
//            featureList.add(feature);
//        }
    }
    
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

    public String getPeptideID() {
        return peptideID;
    }

    public void setPeptideID(String peptideID) {
        this.peptideID = peptideID;
    }

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
