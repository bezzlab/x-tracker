package xtracker.data;

import java.util.ArrayList;
import java.util.List;
import xtracker.plugins.quantitation.quantPlugin;

/**
 * xQuantities is the data structure populated by quantPlugins, it contains quantitative information for peptides in the different experimental conditions.
 * <p>
 * It is constituted by a proteinId, peptide sequence, a Vector of modifications and another one corresponding to the modification positions (please note that modifications are optional). Finally, quantities are in a further Vector
 * called quantities and whose size <b>MUST BE equal</b> to the size of labels Vector in xQuantData structure.
 * @see xQuantData
 * @see quantPlugin
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xQuantities {

    /**
     * The constructor. It creates an xQuantities structure initialising the protein id and peptide sequence. Modification vectors and quantities vector are created empty.
     * @param protId the protein Id
     * @param pepSeq the peptide sequence
     * @param numLabels is the number of different labels (sperimental conditions) the peptide has been quantified in. This is assumed to be a <b>positive integer.</b>
     */
    public xQuantities(String protId, String pepSeq, int numLabels) {
        proteinId = protId;
        peptideSeq = pepSeq;
        modifications = new ArrayList<String>();
        positions = new ArrayList<Integer>();
        quantities = new float[numLabels];
        quantErrors = new float[numLabels];
        for (int i = 0; i < numLabels; i++) {
            quantities[i] = 0f;
            quantErrors[i] = -1f;
        }
    }

    /**
     * Adds a modification to the information of the quantified peptide.
     * @param modName the name of the modification
     * @param position the position of the modificated residue (note that 0 means N-Terminus while length(peptideSeq)+1 means C-Terminus).
     */
    public void addModification(String modName, int position) {
        //TODO: to double check the logic
        if ((!(position > peptideSeq.length() + 1)) && (position > -1)) {
            modifications.add(modName);
            positions.add(position);
        }
    }

    /**
     * Gets the index-th modification in the modifications vector.
     * @param index the index of the modification to retrieve.
     * @return a string with the modification name of the index-th modification in the structure. The string is empty if index is
     * out of Vector limits.
     */
    public String getModificationAtIndex(int index) {
        String ret = "";
        if (index < modifications.size()) {
            ret = modifications.get(index);
        }
        return ret;
    }

    public int getModificationSize(){
        return modifications.size();
    }
    /**
     * Gets the index-th modification in the modifications vector.
     * @param index the index of the modification to retrieve.
     * @return an integer with the position within the peptide sequence of the index-th modification in the structure. It returns -1 if the
     * index is out of Vector's bounds while 0 means N-Terminus and length(peptideSeq)+1 means C-Terminus.
     */
    public int getModPositionAtIndex(int index) {
        if (index < positions.size()) {
            return positions.get(index).intValue();
        }
        return -1;
    }

    /**
     * Gets the peptide sequence.
     * @return peptideSeq a string containing the sequence of the peptide.
     */
    public String getPeptideSeq() {
        return peptideSeq;

    }

    /**
     * Gets the protein identifier.
     * @return proteinId a string containing the protein id the peptide belongs to.
     */
    public String getProteinId() {
        return proteinId;

    }

    /**
     * Adds a quantity to the quantities array at position labelIndex. If labelIndex is too big it fails and exits the application with an error.
     * @param labelIndex the index of the label (conditions) the quantity is related to.
     * @param qty the quantity to insert into the array.
     */
    public void addQuantity(int labelIndex, float qty) {
        if (labelIndex > -1 && labelIndex < this.quantities.length) {
            quantities[labelIndex] = qty;
        } else {
            System.out.println("ERROR: xQuantitites.addQuantity trying to add a quantity to a non-existing label. Label index " + labelIndex + " exceeds array's size (" + quantities.length + ").");
            System.exit(1);
        }

    }

    /**
     * Adds a quantity and the corresponding quantification error to the quantities array (and to the quantErrors array) at position labelIndex. If labelIndex is too big it fails and exits the application with an error.
     * @param labelIndex the index of the label (conditions) the quantity is related to.
     * @param qty the quantity to insert into the array.
     * @param qntError the quantification error associated to the quantity. 
     */
    public void addQuantity(int labelIndex, float qty, float qntError) {
        if (labelIndex > -1 && labelIndex < this.quantities.length) {
            quantities[labelIndex] = qty;
            quantErrors[labelIndex] = qntError;
        } else {
            System.out.println("ERROR: xQuantitites.addQuantity trying to add a quantity to a non-existing label. Label index " + labelIndex + " exceeds array's size (" + quantities.length + ").");
            System.exit(1);
        }

    }

    /**
     * Adds a quantity to the quantErrors array at position labelIndex. If labelIndex is too big it fails and exits the application with an error.
     * @param labelIndex the index of the label (conditions) the quantification error is related to.
     * @param qty the quantErorr amount to insert into the array.
     */
    public void addQuantError(int labelIndex, float qty) {
        if (labelIndex > -1 && labelIndex < this.quantErrors.length) {
            quantErrors[labelIndex] = qty;
        } else {
            System.out.println("ERROR: xQuantitites.addQuantError trying to add a quantity to a non-existing label. Label index " + labelIndex + " exceeds array's size (" + quantErrors.length + ").");
            System.exit(1);
        }

    }

    /**
     * Gets the quantity at index labelIndex (which corresponds to a specific experimental condition)
     * @param labelIndex the index of the element to retrive
     * @return the quantity of the peptide measured in the experimental conditions specified by labelIndex.
     */
    public float getQuantityAt(int labelIndex) {
        float ret = -1f;
        if (labelIndex > -1 && labelIndex < quantities.length) {
            ret = quantities[labelIndex];
        }
        return ret;

    }

    /**
     * Gets the quantErrors at index labelIndex (which corresponds to a specific experimental condition)
     * @param labelIndex the index of the element to retrive
     * @return the quantity error of the peptide measured in the experimental conditions specified by labelIndex.
     */
    public float getQuantErrorAt(int labelIndex) {
        float ret = -1f;
        if (labelIndex > -1 && labelIndex < this.quantErrors.length) {
            ret = quantErrors[labelIndex];
        }
        return ret;

    }

    /**
     * Gets the whole array of quantities.
     * @return quantities, the array of quantities.
     */
    public float[] getAllQuantities() {
        return quantities;

    }

    /**
     * Gets the whole array of quantErrors.
     * @return quantErrors, the array of quantification errors.
     */
    public float[] getAllQuantErrors() {
        return quantErrors;

    }

    /**
     * Gets the size of the quantities array.
     * @return the size of the array.
     */
    public int getQuantitiesSize() {
        return quantities.length;
    }

    /**
     * Gets the size of the quantErrors array (note that it has to be the same as getQuantitiesSize().
     * @return the size of the array.
     */
    public int getQuantErrorSize() {
        return quantErrors.length;
    }
    /**
     * The protein Id the peptide sequence belongs to.
     */
    private String proteinId;
    /**
     * The peptide sequence quantities are referred to.
     */
    private String peptideSeq;
    /**
     * All the modifications associated to the peptide quantified. <u>This can be useful to differentiate between
     * modified and ummodified versions of a same peptide if that's the purpose of quantification</u>
     */
    private List<String> modifications;
    /**
     * The positions of modifications associated to the peptide. Please remember that position = 0 means N-Terminus,
     * position = length(peptideSeq)+1 means C-Terminus.
     */
    private List<Integer> positions;
    /**
     * Quantities of the peptide in the different conditions (labels in the xQuantData structure).
     * <u>Note that the size of this array has to be the same of the labels vector in xQuantData</u>
     * @see xQuantData
     */
    private float[] quantities;
    /**
     * Quantitation errors relative to the corresponding element in quantities array. (i.e. quantErrors[i] is the quantitation error of quantities[i]).
     * <u>Note that the size of this array has to be the same of the labels vector in xQuantData</u>. If no error measure is available we suggest to leave all elements set to -1f.
     * @see xQuantData
     */
    private float[] quantErrors;
}
