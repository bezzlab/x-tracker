package xtracker;

import java.util.*;

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
    public identInputData(){

        proteinId="";
        peptideSeq="";
        lcMsIdent = new Vector<xIdentData>();
        lcMsMsIdent = new Vector<xIdentData>();
    }

    /**
     * The constructor. Just creates a new vector of xIdentData elements.
     * @param idVal is the string with the protein identifier.
     * @param seqVal is the string with the peptide sequence.
     */
    public identInputData(String idVal, String seqVal){
        proteinId=idVal;
        peptideSeq=seqVal;
        lcMsIdent = new Vector<xIdentData>();
        lcMsMsIdent = new Vector<xIdentData>();
    }


     /**
     * Get the protein id.
     * @return proteinId is the peptide identifier.
     */
    public String getProteinId(){
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
    public void setProteinId(String pepId){
        proteinId=pepId;

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
    public int getLcMsMsIdentSize(){

        return lcMsMsIdent.size();
   }

    /**
    * Adds an element to the list of LC/MS identifications.
    * <p>
    * @param ident the structure with all the spectra information to be added to the structure.
    */
    public void addLcMsIdent(xIdentData ident){

        lcMsIdent.addElement(ident);
   }

    /**
    * Adds an element to the list of MS/MS identifications.
    * <p>
    * @param ident the structure with all the spectra information to be added to the structure.
    */
    public void addLcMsMsIdent(xIdentData ident){

        lcMsMsIdent.addElement(ident);
   }

    /**
    * Retrieves an element of LC/MS identifications by index.
    * <p>
    * @param index the index of the element to retrieve.
    * @return xIdentData the identification data contained at the specified index. Returns null if index exceeds vector
    * dimensions.
    */
    public xIdentData getLcMsElemAt(int index){
        if(!(index>lcMsIdent.size())){
                return lcMsIdent.elementAt(index);
        }
        else{
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
    public xIdentData getLcMsMsElemAt(int index){
        if(!(index>lcMsMsIdent.size())){
                return lcMsMsIdent.elementAt(index);
        }
        else{
            return null;
        }
       }





    /**
     * The identifier of the protein peptideSeq belongs to and to which the identifications are referred.
     */
    public String proteinId;

    /**
     * The sequence of the peptide the identifications refer to.
     */
    public String peptideSeq;


    /**
     * <code>lcMsIdent</code> contains the identifications relative to the
     *  LC/MS raw data coming from file <code>fileName</code> of xLoadData.
     * @see xIdentData
     * @see xLoadData
     */
    public Vector<xIdentData> lcMsIdent;


   /**
     * <code>identifications</code> contains the identifications relative to the
     *  raw data coming from file <code>fileID</code>.
     * @see xIdentData
     */
    public Vector<xIdentData> lcMsMsIdent;
}