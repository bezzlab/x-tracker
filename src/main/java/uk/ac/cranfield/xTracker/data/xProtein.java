package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.cranfield.xTracker.xTracker;
/**
 * The Protein class
 * @author Jun Fan@cranfield
 */
public class xProtein extends QuantitationLevel{
    /**
     * the corresponding protein element in mzQuantML
     */
    private Protein protein;
    /**
     * for MS2 pipeline, the list of peptides
     */
    private ArrayList<xPeptideConsensus> peptides;
    /**
     * for MS1 pipeline, keys are assay id, and values are assay-specific peptides
     */
    private HashMap<String,AssayPeptide> assayPeptides;

    public xProtein(Protein pro) {
        protein = pro;
        peptides = new ArrayList<xPeptideConsensus>();
        assayPeptides = new HashMap<String, AssayPeptide>();
    }
    /**
     * Get the protein mzQuantML element
     * @return the protein mzQuantML element
     */
    public Protein getProtein() {
        return protein;
    }
    /**
     * Get the accession of the protein
     * @return the accession
     */
    public String getAccession(){
        return protein.getAccession();
    }
    /**
     * Get the peptide according to the sequence and the modification pattern
     * If not exists, create a new one with the given peptideID
     * @param seq peptide sequence
     * @param mods modification pattern
     * @param modStr the string contains the modification locations
     * @return the peptide
     */
    public xPeptide getPeptide(String seq,String modStr,HashSet<xModification> mods){
        return getPeptide(seq, mods, seq+"_"+modStr);
    }
    /**
     * Get the peptide according to the sequence and the modification pattern
     * If not exists, create a new one with the given peptideID and return it
     * @param seq peptide sequence
     * @param mods modification pattern
     * @param peptideID the peptide id for the new generated peptide object
     * @return the peptide
     */
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
    /**
     * Get all peptide consensus, currently only implemented for MS2
     * @return 
     */
    public ArrayList<xPeptideConsensus> getPeptides() {
        return peptides;
    }
    /**
     * Get all peptides, currently only implemented for MS2
     * @return 
     */
    public ArrayList<xPeptide> getAllPeptides(){
        ArrayList<xPeptide> ret = new ArrayList<xPeptide>();
        for(xPeptideConsensus pc: peptides){
            ret.addAll(pc.getPeptides());
        }
        return ret;
    }
    
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
        int ret = 0;
        for(xPeptideConsensus pc: peptides){
            ret += pc.getCount();
        }
        return ret;
    }
}
