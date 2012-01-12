package xtracker.data;

import java.util.ArrayList;
import java.util.List;
import xtracker.plugins.identificationLoad.identData_loadPlugin;

/**
 * identInputData is the data structure containing identifications information. They are a protein identifier, the peptide sequence and two vectors (at least one of them needs to
 * contain data) representing LC-MS or LC-MS/MS identifications.
 * <p>
 * @see        identData_loadPlugin
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */

public class identInputData {

    /**
     * The constructor in case no data are available at construction time. 
     * Just creates new vectors of xIdentData elements and empty strings.
     */
    public identInputData() {
        this("","");
    }

    /**
     * The constructor. Just creates a new vector of xIdentData elements.
     * @param idVal is the string with the protein identifier.
     * @param seqVal is the string with the peptide sequence.
     */
    public identInputData(String idVal, String seqVal) {
        proteinId = idVal;
        peptideSeq = seqVal;
        lcMsIdent = new ArrayList<xIdentData>();
        lcMsMsIdent = new ArrayList<xIdentData>();
    }

     /**
     * Get the protein id.
     * @return proteinId is the peptide identifier.
     */
    public String getProteinId(){
        return proteinId;
    }
    //manually added
    public String getPeptideId(){
        return proteinId;
    }
 
    /**
     * Get the peptide sequence.
     * @return peptideSeq is the peptide sequence.
     */
    public String getPeptideSeq(){
        return peptideSeq;
    }
    
     /**
     * Set the protein identifier.
     * @param pepId is the new peptide identifier.
     */
    public void setProteinId(String pepId) {
        proteinId = pepId;
    }
    
    /**
     * Set the peptide sequence.
     * @param pepSeq is the new peptide sequence.
     */
    public void setPeptideSeq(String pepSeq){
        peptideSeq=pepSeq;
    } 
    
   /**
    * Returns the size of the list of identifications with LC/MS data.
    * <p>
    * @return the size of the identification.
    */
    public int getLcMsIdentSize(){
        return lcMsIdent.size();
    }
    
    /**
    * Returns the size of the list of identifications with MS/MS data.
    * <p>
    * @return the size of the identification.
    */
    public int getLcMsMsIdentSize() {
        return lcMsMsIdent.size();
    }
    //manually added
    public int getMsMsIdentSize(){
        return lcMsMsIdent.size();
    }
    
    /**
     * Adds an element to the list of LC/MS identifications.
     * <p>
     * @param ident the structure with all the spectra information to be added to the structure.
     */
    public void addLcMsIdent(xIdentData ident) {
        lcMsIdent.add(ident);
    }

    /**
     * Adds an element to the list of MS/MS identifications.
     * <p>
     * @param ident the structure with all the spectra information to be added to the structure.
     */
    public void addLcMsMsIdent(xIdentData ident) {
        lcMsMsIdent.add(ident);
    }

    /**
     * Retrieves an element of LC/MS identifications by index.
     * <p>
     * @param index the index of the element to retrieve.
     * @return xIdentData the identification data contained at the specified index. Returns null if index exceeds vector
     * dimensions.
     */
    public xIdentData getLcMsElemAt(int index) {
        if (index < lcMsIdent.size()) {
            return lcMsIdent.get(index);
        } else {
            return null;
        }
    }

    /**
     * Retrieves an element of MS/MS identifications by index.
     * <p>
     * @param index the index of the element to retrieve.
     * @return xIdentData the identification data contained at the specified index. Returns null if index exceeds vector
     * dimensions.
     */
    public xIdentData getLcMsMsElemAt(int index) {
        if (index < lcMsMsIdent.size()) {
            return lcMsMsIdent.get(index);
        } else {
            return null;
        }
    }
    /**
     * The identifier of the protein peptideSeq belongs to and to which the identifications are referred.
     */
    private String proteinId;
    /**
     * The sequence of the peptide the identifications refer to.
     */
    private String peptideSeq;
    /**
     * <code>lcMsIdent</code> contains the identifications relative to the
     *  LC/MS raw data coming from file <code>fileName</code> of xLoadData.
     * @see xIdentData
     * @see xLoadData
     */
    private List<xIdentData> lcMsIdent;
    /**
     * <code>identifications</code> contains the identifications relative to the
     *  raw data coming from file <code>fileID</code>.
     * @see xIdentData
     */
    private List<xIdentData> lcMsMsIdent;
}