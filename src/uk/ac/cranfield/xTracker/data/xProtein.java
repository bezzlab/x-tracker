package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.cranfield.xTracker.xTracker;
/**
 *
 * @author Jun Fan@cranfield
 */
public class xProtein extends QuantitationLevel{
    private Protein protein;
//    private String name;
//    private String accession;
    /**
     * for MS2 pipeline, keys are MSRun ids and values are the lists of corresponding peptides
     */
//    private HashMap<String,ArrayList<xPeptideConsensus>> peptides;
    private ArrayList<xPeptideConsensus> peptides;
    private HashMap<String,AssayPeptide> assayPeptides;

    public xProtein(Protein pro) {
        protein = pro;
        peptides = new ArrayList<xPeptideConsensus>();
        assayPeptides = new HashMap<String, AssayPeptide>();
    }

    public Protein getProtein() {
        return protein;
    }
    
    public String getAccession(){
        return protein.getAccession();
    }
    //guaranteed a valid xPeptide object will be returned
//    public xPeptide getPeptide(String msrun,String seq,HashSet<xModification> mods){
//    public xPeptide getPeptide(String seq,HashSet<xModification> mods){
    public xPeptide getPeptide(String seq,HashSet<xModification> mods,String peptideSequence,String modStr){
        return getPeptide(seq, mods, peptideSequence+"_"+modStr);
    }
    
    public xPeptide getPeptide(String seq,HashSet<xModification> mods,String peptideID){
        if(xTracker.study.getPipelineType()==Study.MS2_TYPE){//MS2
            //find the peptideConsensus first
            xPeptideConsensus pepCon = null;
            for(xPeptideConsensus peptideCon:peptides){
                if(peptideCon.getSeq().equals(seq)){
                    pepCon = peptideCon;
                    break;
                }
            }
            if(pepCon == null){//this sequence seq has not been seen before
                pepCon = new xPeptideConsensus(seq);
                xPeptide peptide = new xPeptide(seq, mods);
                peptide.setPeptideID(getAccession()+"-"+peptideID);
                pepCon.addPeptide(peptide);
                peptides.add(pepCon);
                return peptide;
            }
            xPeptide peptide = pepCon.getPeptide(mods);
            if(peptide == null){
                peptide = new xPeptide(seq, mods);
                peptide.setPeptideID(getAccession()+"-"+peptideID);
                pepCon.addPeptide(peptide);
            }
            return peptide;
        }else{//MS1
            //TODO
            return null;
        }
    }

    public ArrayList<xPeptideConsensus> getPeptides() {
        return peptides;
    }
    
    public ArrayList<xPeptide> getAllPeptides(){
        ArrayList<xPeptide> ret = new ArrayList<xPeptide>();
        for(xPeptideConsensus pc: peptides){
            ret.addAll(pc.getPeptides());
        }
        return ret;
    }
    
//    public Set<String> getMSRunIDs(){
//        return peptides.keySet();
//    }
//    
//    public ArrayList<xPeptideConsensus> getPeptideCons(String msrunID){
//        if(peptides.containsKey(msrunID)){
//            return peptides.get(msrunID);
//        }
//        return null;
//    }
    
    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(!(obj instanceof xProtein)) return false;
        xProtein pro = (xProtein)obj;
        if(this.getAccession().endsWith(pro.getAccession())) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.getAccession() != null ? this.getAccession().hashCode() : 0);
        return hash;
    }
    
    @Override
    public int getCount(){
//        return peptides.size();
        int ret = 0;
        for(xPeptideConsensus pc: peptides){
            ret += pc.getCount();
        }
        return ret;
    }
}
