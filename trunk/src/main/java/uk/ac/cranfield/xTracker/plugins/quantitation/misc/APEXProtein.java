/*
 *  Original code within this program is � J. Craig Venter Institute.  All rights reserved.  April 15, 2008.
 *
THIS SOFTWARE IS PROVIDED ìAS ISî WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
INCLUDING WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR USE OF A
PARTICULAR PURPOSE, OR NONINFRINGEMENT;


IN UTILIZING THIS SOFTWARE, USER AGREES THAT JCVI WILL NOT BE LIABLE FOR ANY LOSS OR DAMAGES,
EITHER ACTUAL OR CONSEQUENTIAL, ARISING OUT OF OR RELATING TO THESE TERMS, OR TO USERíS OR ANY
THIRD PARTIES USE OR INABILITY TO USE THE SOFTWARE, OR TO USERíS RELIANCE UPON INFORMATION
OBTAINED FROM OR THROUGH A JCVI WEBSITE. IN PARTICULAR, JCVI WILL HAVE NO LIABILITY FOR ANY
CONSEQUENTIAL, INDIRECT, PUNITIVE, SPECIAL OR INCIDENTAL DAMAGES, WHETHER FORSEEABLE OR
UNFORSEEABLE (INCLUDING, BUT NOT LIMITED TO, CLAIMS FOR DEFAMATION, ERRORS, LOSS OF DATA,
OR INTERRUPTION IN AVAILABILITY OF DATA), ARISING OUT OF OR RELATING TO THESE TERMS, USERíS
USE OR INABILITY TO USE THE SOFTWARE, OR ANY PURCHASES ON A JCVI SITE, OR TO USERíS RELIANCE
UPON INFORMATION OBTAINED FROM OR THROUGH A JCVI SITE, WHETHER BASED ON CONTRACT, TORT, STATUTORY,
OR OTHER LAW, EXCEPT ONLY IN THE CASE OF DEATH OR PERSONAL INJURY WHERE AND ONLY TO THE
EXTENT THAT APPLICABLE LAW REQUIRES SUCH LIABILITY.

 *  Author: John C. Braisted
 *
 *  Modified by: Laurie Tonon for X-tracker
 */
package uk.ac.cranfield.xTracker.plugins.quantitation.misc;

import java.util.ArrayList;


/**
 * The <code>APEXProtein</code> object is a container that holds information related to a protein
 * Aside from the protein's sequence, accession,
 * the object can hold an indexed list of Peptide objects that correpond to
 * peptides associated with protease digestion
 *
 * @author John C. Braisted Modified by Laurie Tonon for X-Tracker
 *
 */
public class APEXProtein {

    /**
     * Primary protein id
     */
    private String id;
    /**
     * Protein sequence
     */
    private String sequence;
    /**
     * List of peptides
     */
    private ArrayList<Peptide> peptideList;
    /**
     * List of tryptic peptides
     */
    private String[] listCleavedPeptides;

    //Computational fields
    //These fields hold the basic values for computing the APEX scores

    /**
     * Protein's predicted peptide count
     */
    private double Oi;
    /**
     * Protein's observed peptide count
     */
    private int ni;
    /**
     * Protein identification probability
     */
    private double pi;
    /**
     * False positive error rate for this protein and all others
     * with higher APEX values in the XML files
     */
    private double fper;
    /**
     * Relative APEX value
     */
    private double relAPEXScore;
    /**
     * Normalized APEX Value
     */
    private double normalizedAPEXScore;
//    /**
//     * indicates if Oi has been set for this protein
//     */
//    private boolean isOiSet;

    /**
     * Default Constructor
     */
    public APEXProtein() {
        peptideList = new ArrayList<Peptide>();

    }

    /**
     * Constructs a <code>APEXProtein</code> object using provided parameters
     * @param id protein id
     */
    public APEXProtein(String id) {
        this.id = id;
        peptideList = new ArrayList<Peptide>();
        sequence = "";
    }

    /**
     * Adds a peptide to the protein's list of related peptides
     * @param peptide Peptide to add
     */
    public void addPeptide(Peptide peptide) {
        peptideList.add(peptide);
        ni = peptideList.size();
    }

    /**
     * Adds a list of peptide to the protein's list of related peptides
     * @param listPeptide a list of Peptides to add
     */
    public void addListPeptide(ArrayList<Peptide> listPeptide) {
        for (int i = 0; i < listPeptide.size(); i++) {
            peptideList.add(listPeptide.get(i));
        }
        ni = peptideList.size();
    }

    /**
     * Returns the peptide at the supplied index
     * @param index peptide index
     * @return
     */
    public Peptide getPeptideAt(int index) {
        return (Peptide) (peptideList.get(index));
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return Returns the ni.
     */
    public int getNi() {
        return ni;
    }

    /**
     * @param ni The ni to set.
     */
    public void setNi(int ni) {
        this.ni = ni;
    }

    /**
     * @return Returns the oi.
     */
    public double getOi() {
        return Oi;
    }

    /**
     * @param oi The oi to set.
     */
    public void setOi(double oi) {
        
        Oi = oi;
    }



    /**
     * @return Returns the pi.
     */
    public double getPi() {
        return pi;
    }

    /**
     * @param pi The pi to set.
     */
    public void setPi(double pi) {
        this.pi = pi;
    }

    /**
     * @return Returns the seq.
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * @param seq The seq to set.
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * @param peptideList The peptideList to set.
     */
    public void setPeptideList(ArrayList<Peptide> peptideList) {
        this.peptideList = peptideList;
        this.ni = peptideList.size();
    }

    /**
     * Returns a list of Peptide objects
     * @return Vector of peptides
     */
    public ArrayList<Peptide> getPeptideList() {
        return peptideList;
    }


    /**
     * Returns the relative apex score
     * @return
     */
    public double getRelAPEXScore() {
        return relAPEXScore;
    }

    /**
     * sets the relative apex score
     * @param relAPEXScore apex score to assign
     */
    public void setRelAPEXScore(double relAPEXScore) {
        this.relAPEXScore = relAPEXScore;
    }

    /**
     * Returns the normalized apex score value
     * @return
     */
    public double getNormalizedAPEXScore() {
        return normalizedAPEXScore;
    }

    /**
     * Returns the list of theoretical peptides
     * @return
     */
    public String[] getListCleavedPeptides() {
        return listCleavedPeptides;
    }

    /**
     * setes the normalized apex score
     * @param normalizedAPEXScore
     */
    public void setNormalizedAPEXScore(double normalizedAPEXScore) {
        this.normalizedAPEXScore = normalizedAPEXScore;
    }

    /**
     *
     * @return the false positive error value
     */
    public double getFper() {
        return fper;
    }

    /**
     * sets the false positive error value
     * @param fper
     */
    public void setFper(double fper) {
        this.fper = fper;
    }



    /**
     * sets the list of tryptic peptides
     * @param listCleavedPeptides
     */
    public void setListCleavedPeptides(String[] listCleavedPeptides) {
        this.listCleavedPeptides = listCleavedPeptides;
    }
}
