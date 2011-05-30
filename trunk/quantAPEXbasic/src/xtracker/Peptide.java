

package xtracker;

import java.util.ArrayList;

/**
 * Class that defines a Peptide object. A peptide is defined by its sequence and modifications.
 *
 * @author laurie tonon for X-Tracker
 */
public class Peptide {

    /**
     * amino acids sequence
     */
    private String sequence;

    /**
     * list of modifications
     */
    private ArrayList<String> modifs;

    /**
     * list of positions of modifications
     */
    private ArrayList<Integer> modPositions;

    /**
     * Constructor
     * @param sequence the sequence of the peptide
     * @param mod the list of modifications
     * @param modpos the list of modifications positions
     */
    public Peptide(String sequence, String[] mod, int[] modpos){
        this.sequence=sequence;
        this.modifs=new ArrayList<String>();
        this.modPositions=new ArrayList<Integer>();


        for(int i=0;i<mod.length;i++){
            this.modifs.add(mod[i]);
            this.modPositions.add(modpos[i]);
        }
    }

    /**
     * Sets the modifications positions
     * @param modPositions
     */
    public void setModPositions(ArrayList modPositions) {
        this.modPositions = modPositions;
    }

    /**
     * Sets the modifications
     * @param modifs
     */
    public void setModifs(ArrayList modifs) {
        this.modifs = modifs;
    }

    /**
     * Sets the sequence
     * @param sequence
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns the list of modifications positions
     * @return
     */
    public ArrayList getModPositions() {
        return modPositions;
    }

    /**
     * Returns the list of modifications
     * @return
     */
    public ArrayList getModifs() {
        return modifs;
    }

    /**
     * Returns the sequence
     * @return
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Returns the modification at the index supplied
     * @param index the index on the modification in the array
     * @return
     */
    public String getModificationAtIndex(int index){
        return modifs.get(index);
    }

    /**
     * Returns the modification position at the index supplied
     * @param index the index of the modification position in the array
     * @return
     */
    public int getModPositionAtIndex(int index){
        return modPositions.get(index);
    }
    


}
