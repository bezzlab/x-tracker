

package xtracker.plugins.quantitation.misc;

/**
 * Class that defines a object Protein, which is used to store the information of a protein from a fasta file.
 *
 * @author laurie Tonon for X-Tracker
 */
public class Protein {

    /**
     * The identifier of the protein
     */
    String id;
    /**
     * The descritpion of the protein (not used in X-Tracker)
     */
    String description;
    /**
     * The sequence of the protein
     */
    String sequence;


    /**
     * Constructor
     */
    public Protein(){
        this.id="";
        this.description="";
        this.sequence="";
    }

    /**
     * Constructor
     * @param _id the identifier of the protein to create
     * @param _description the description of the protein to create
     * @param _sequence the sequence of the protein to create
     */
    public Protein(String _id, String _description, String _sequence){
        this.id=_id;
        this.description=_description;
        this.sequence=_sequence;
    }

    /**
     * Get the description of the protein
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the identifier of the protein
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Get the sequence of the protein
     * @return
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Set the description of the protein
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set the identifier of the protein
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set the sequence of the protein
     * @param sequence
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
    
    public String toString(){
        String ret="Identification number :"+id+"\nDescritpion :"+description+"\nSequence :"+sequence;
        
        return ret;
    }

}
