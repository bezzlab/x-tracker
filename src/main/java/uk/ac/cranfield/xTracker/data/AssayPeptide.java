package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The assay specific peptides for MS1 technology, unimplemented yet
 * @author Jun Fan@cranfield
 */
public class AssayPeptide {
    /**
     * The collections of peptide sequences
     */
    private HashSet<String> sequences;
    /**
     * The keys are assay id and the values are the corresponding list of peptide in that assay
     */
    private HashMap<String,ArrayList<xPeptide>> peptides;
    
    public AssayPeptide(){
        sequences = new HashSet<String>();
        peptides = new HashMap<String, ArrayList<xPeptide>>();
    }
}
