/*
 *  Original code within this program is � J. Craig Venter Institute.  All rights reserved.  April 15, 2008.
 *
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
 * 
*/
package xtracker.plugins.quantitation.misc;

import java.util.Vector;


/**
 * APEXProteinList is a vector extention that holds a collection of APEXProtein objects
 *
 * @author John C. Braisted Modified by Laurie Tonon for X-Tracker
 *
 */
public class APEXProteinList extends Vector {

	/**
	 * Timestamp list id
	 */
	private String listID;
	/**
	 * ordered Attribute names, attribute ordering is important for construction of
	 * training and test matrices.  Training and test much match attribute order.
	 */
	private Vector orderedPepAttrNames;
	
	/**
	 * Constructs an empty protein list
	 */
	public APEXProteinList() {
		super();
		listID = "";
		orderedPepAttrNames = new Vector();
	}

	/**
	 * Constructs protein list with specified id
	 * @param listID list id
	 */
	public APEXProteinList(String listID) {
		super();
		this.listID = listID;
		this.orderedPepAttrNames = new Vector();
	}
	
	/**
	 * Adds <code>APEXProtein</code> to list
         * Checks before if a Protein with the same id is not already in the list
	 * @param protein APEXProtein to add
	 */
	public void addProtein(APEXProtein protein) {
            if(!(containID(protein.getId()))){
		add(protein);
            }
            else{
                
                this.getProtein(protein.getId()).addListPeptide(protein.getPeptideList());
            }
	}

	/**
	 * Retreives the protein at specified index
	 * @param index protein index to retrieve
	 * @return APEXProtein object at specified index`
	 */
	public APEXProtein getProteinAt(int index) {
		return (APEXProtein)(get(index));
	}
	
	/**
	 * dumps the protein count
	 * @return
	 */
	public int getProteinCount() {
		return size();
	}
	
	
	/**
	 * Dumps the total peptide count
	 * @return peptide count
	 */
	public int getTotalPeptideCount() {
		int pepCount = 0;
		for(int prot = 0; prot < this.size(); prot++)
			pepCount+= getProteinAt(prot).getNi();
		return pepCount;
	}
	
	
	/**
	 * @return Returns the listID.
	 */
	public String getListID() {
		return listID;
	}
	
	
	/**
	 * @param listID The listID to set.
	 */
	public void setListID(String listID) {
		this.listID = listID;
	}
	
	
	/**
	 * Returns the list of ordered peptide attribute names
	 * @return Returns the orderedPepAttrNames.
	 */
	public Vector getOrderedPepAttrNames() {
		return orderedPepAttrNames;
	}
	
	
	/**
	 * Sets the ordered peptide attribute names which maintains the set of attributes
	 * and insures consistent order when reporting attributes to either a Weka object or a matrix file
	 * @param orderedPepAttrNames The orderedPepAttrNames to set.
	 */
	public void setOrderedPepAttrNames(Vector orderedPepAttrNames) {
		this.orderedPepAttrNames = orderedPepAttrNames;
	}
	
	/**
	 * Returns the APEXProtein with the specified id or null
	 * @param id protein id String
	 * @return APEXProteint with the id or null if not found
	 */
	public APEXProtein getProtein(String id) {
		APEXProtein protein = null;
		boolean found = false;		
		int index;
		
		for(index = 0; index < size() && !found; index++) {
			if(getProteinAt(index).getId().equals(id)) {
				protein = getProteinAt(index);
				found = true;
			}
		}
		
		return protein;
	}
	
	
	
	/**
	 * Returns all protein ids
	 * @return
	 */
	public String [] getAllListIDs() {
		String [] ids = new String[size()];
		for(int i = 0; i < size(); i++)
			ids[i] = getProteinAt(i).getId();
		return ids;
	}
	
	/**
	 * Returns the sum of ni values
	 * @return
	 */
	public double getNiSum() {
		double niSum = 0;		
		APEXProtein protein;		
		for(int i = 0; i < size(); i++) {
			protein = getProteinAt(i);
			niSum += protein.getNi();
		}		
		return niSum;
	}


        /**
         * Checks if the list contains a protein with a particular id
         * @param proteinID the id of the protein to check
         * @return true if the list contains this protein, false otherwise
         */
        public boolean containID(String proteinID){
            boolean contains=false;
            for(int i=0; i<this.size();i++){
                if(this.getProteinAt(i).getId().equals(proteinID)){
                    contains=true;
                }
            }

            return contains;
        }
}
