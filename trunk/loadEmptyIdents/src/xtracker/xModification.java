package xtracker;





/**
 * xModification is the data structure housing amino-acid modifications within identified peptides.
 * <p>
 * It is basically an object comprising the following elements:
 * <ul>
 * <li><code>name</code> the name of the modification (unimod names would be reccomended here).</li>
 * <li><code>massShift</code> a float describing the mass shift induced on the aminoacid by the modification.</li>
 * <li><code>position</code> the position of the modified residue within the peptide.<br><strong>Please note that a value of 0 means N-Terminus modification, a value of lenght(peptideSeq)+1 means C-Terminus.</strong></li>
 * <li><code>position</code> a boolean stating if the modification is variable (isVariableMod=true) or fixed (isVariableMod=false).</li>
 * </ul>

 * @see xIdentData
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xModification {


/**
 * The constructor.
 * @param modName is a string representing the modification name.
 * @param modShift a float representing the shift induced by the modification (in Daltons) it can be average or monoisotopic,
 * depending on the settings of the software.
 * @param modPosition the position of the amino acid (relative to the peptide) affected by the modification.
 * @param isVariable is the modification variable? (false means fixed).
 */

    public xModification(String modName, float modShift,  int modPosition, boolean isVariable){
        name=modName;
        massShift=modShift;
        position=modPosition;
        isVariableMod=isVariable;

    }


    /**
     * Sets the modification name.
     * @param modName the name of the modification.
     */
    public void setName(String modName){
        name=modName;
    }

    /**
     * Gets the modification name.
     * @return name the name of the modification.
     */
    public String getName(){
        return name;
    }


    /**
     * Sets the mass shift induced by the modification.
     * @param masShft is the mass shift induced by  the modification.
     */
    public void setMassShift(float masShft){
        massShift=masShft;
    }

    /**
     * Gets the mass shift induced by the mdification to the residue.
     * @return massShift is the shift induced by the modification.
     */
    public float getMassShift(){
        return massShift;
    }




    /**
     * Sets the position (relative position from 0 to lenght(peptideSeq)+1) of the amino acid affected by the modification.
     * <br>Please note that a value of 0 means N-Terminus modification, a value of lenght(peptideSeq)+1 means C-Terminus.
     * @param modPos is the position of the modified amino acid.
     */
    public void setPosition(int modPos){
        if(modPos>-1){
            position=modPos;
        }
        else{
        System.out.println("Error in setPosition (xModification): negative modification position (" + modPos +").");
        System.exit(1);

        }

    }

    /**
     * Gets the amino acid name.
     * @return position the position of the modified residue within the peptide.
     */
    public int getPosition(){
        return position;
    }



    /**
     * Sets isVariableMod.
     * @param isVariable boolean, true if the modification is variable, false otherwise.
     */
    public void setVariableMod(boolean isVariable){
        isVariableMod=isVariable;
    }


    /**
     * Is the modification a variable one?
     * @return true if the modification is variable, false otherwise.
     */
    public boolean isVariable(){
        return isVariableMod;
    }



/**
 * The name of the modification.
 */
public String name;

/**
 * The mass shift induced by the modification on the peptide.
 */
public float massShift;


/**
 * The relative position, within the peptide, of the amino acid affected by the modification. Acceptable values
 * for position range from 1 to length(peptideSeq).
 * <br>Please note that a value of 0 means N-Terminus modification, a value of lenght(peptideSeq)+1 means C-Terminus.
 */
public int position;


/**
 * A boolen value stating if the modification is variable (isVariableMod=true) or fixed (isVariableMod=false)
 */
public boolean isVariableMod;

}
