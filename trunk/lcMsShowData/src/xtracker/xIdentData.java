package xtracker;
import java.util.*;

/**
 * xIdentData is the data structure representing an identification. Since in some occasion it might be useful to store parent Ion m/z values obtained from
 * the search engine, parentMassDB and retTimeDB can contain data obtained from the search engine or repeat some information present in the raw data.
 * <p>
 * @see identInputData
 * @see identData_loadPlugin
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */

public class xIdentData {

    /**
     * The constructor
     * @param confidence the confidence level of the identification.
     * @param retT is the retention time of the <u>xLcMsData or xLcMsMsData structures</u> the identification is referred to.
     * @param parentMz is the index of the parent mass over chargeDB in the case of LC-MS/MS identifications (please note that this can come from the search database and therefore not necessarily coincide with information in the raw data).
     * @param chg is the chargeDB state of the identified peptide
     */
    public xIdentData(float confidence, float retT,float parentMz, int chg){
        confidenceLevel=confidence;
        parentMassDB=parentMz;
        retTimeDB=retT;
        modifications=new Vector<xModification>();
        chargeDB=chg;

    }

    /**
     * Sets the parent mass the identification refers to.
     * @param pMz the parent mass index.
     */
    public void setparentMass(float pMz){
        parentMassDB=pMz;
    }

    /**
     * Sets the confidence level of the identification.
     * @param confidence of the peptide identification.
     */
    public void setConfidenceLevel(float confidence){
        confidenceLevel=confidence;
    }


    /**
     * Gets the parent mass the identification refers to.
     * @return parentMassDB the parent ion mass index in the case of an LC-MS/MS identification.
     */
    public float getParentMass(){
        return parentMassDB;
    }

    /**
     * Gets the retention time the identification refers to.
     * @return retTimeDB the retention time.
     */
    public float getRetTime(){
        return retTimeDB;
    }

        /**
     * Sets the retention time  the identification refers to.
     * @param retT the retention time.
     */
    public void setRetTime(float retT){
        retTimeDB=retT;
    }


    /**
     * Gets the charge state associated to the identified peptide.
     * @return chargeDB the charge state.
     */
    public int getCharge(){
        return chargeDB;
    }

    /**
     * Sets the charge state  the identification refers to.
     * @param chg the charge state.
     */
    public void setCharge(int chg){
        chargeDB=chg;
    }

    /**
     * Gets the confidence level of the identification.
     * @return confidenceLevel the confidence of the identification.
     */
    public float getConfidenceLevel(){
        return confidenceLevel;
    }


    /**
     * Adds a modification to the vector of modifications.
     * @param mod the modification to be added.
     */
    public void addModification(xModification mod) {
        modifications.addElement(mod);
    }


    /**
     * Gets the modIndex-th modification within the vector of modifications.
     * @param modIndex is the modification index in the modifications vector.
     * @return the modIndex-th modification.
     */
    public xModification getModificationElemAt(int modIndex) {
        xModification ret=null;
        if(modIndex<modifications.size()){
            return modifications.elementAt(modIndex);
        }
        else{
            return ret;

        }

    }


    /**
     * Returns how many modifications the peptide has.
     * @return the number of modifications the identified peptide has been subjected to.
     */

    public int getModificationSize(){
        int ret=-1;
        ret=modifications.size();
        return ret;

    }

    /**
     * Gets the mass shift induced by all the modifications identified on the peptide. This is the total mass shift, that is, the sum of
     * each mass shift corresponding to a modified amino acid.
     * @return ret is the sum of all mass shifts.
     */
    public float getTotalModificationMassShift(){
        float ret=0.0f;
        int i=0;
        for(i=0;i<modifications.size();i++){
            ret=ret+modifications.elementAt(i).getMassShift();
        }

        return ret;

    }

    /**
     * Gets the vector of all modifications.
     * @return the vector of all modifications.
     */
    public Vector<xModification> getAllModifications(){
        Vector<xModification> ret=modifications;
        return ret;


    }

    /**
     * A float assessing the confidence level of the identification.
     */
    public float confidenceLevel;

    /**
     * A float representing the retention time value (reported from the search engine) the spectrum associated to this identification is recorded at.
     */
    public float retTimeDB;

    /**
     * A float representing the parent ion Mass (reported from the search engine) the identification corresponds to in the <u>xLcMsMsData</u> structure. It is unused in the case of LC-MS identifications.
     * @see xLcMsData
     */
    public float parentMassDB;

    /**
     * An integer representing the charge state (reported by the search engine) of the identified peptide.
     */
    public int chargeDB;


    /**
     * A vector of modifications associated to the identified peptide.
     */
    public Vector<xModification> modifications;

}
