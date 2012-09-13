package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Jun Fan@cranfield
 */
public class AssayPeptide {
    private HashSet<String> sequences;
    private HashMap<String,ArrayList<xPeptide>> peptides;
    
    public AssayPeptide(){
        sequences = new HashSet<String>();
        peptides = new HashMap<String, ArrayList<xPeptide>>();
    }
}
