package xtracker;
import java.util.*;

/**
 * xQuant is the data structure populated by quantPlugins.
 * <p>
 * It is constituted by a filename the quantitation is referred to, an array of labels (corresponding to the different experimental conditions measured)
 * and a vector <code>quantitativeData</code> representing quantifications for peptides identified in the raw data file fileName.
 * @see quantPlugin
 * @see xQuant
 * @see xQuantities
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */

public class xQuantData {
    /**
     * The constructor. It creates an xQuantData element by setting the filename and creating the labels array and quantitativeData Vector.
     * @param fileNM the raw data filename the quantitation results belong to.
     * @param numLabels the number of labels that have been considered in the quantitation experiment.
     */
    public xQuantData(String fileNM, int numLabels){
        fileName = fileNM;
        labels = new String[numLabels];
        quantitativeData = new Vector<xQuantities>();

    }

    /**
     * Sets the whole labels array in a single go. If sizes are not matching it throws an error and terminates the program.
     * @param lbls is an array of strings representing the set of labels (experimental conditions) considered.
     */
    public void setAllLabels(String[] lbls){
        if(lbls.length==labels.length){
            labels=lbls;
        }
        else{
            System.out.println("ERROR: xQuantData.setAllLabels length of labels array ("+labels.length+") and the specified array ("+lbls.length+") do not match!");
            System.exit(1);
        }
    }


    /**
     * Sets a single label (at index ind) in the labels array. If index ind is out of array's bounds an error is printed and the execution is terminated.
     * @param label is the string representing the label (experimental conditions) to add.
     * @param ind is the index of the label to set in the labels array.
     */
    public void setLabelAtIndex(int ind, String label){
        if(ind<labels.length && ind>-1){
            labels[ind]=label;
        }
        else{
            System.out.println("ERROR: xQuantData.setLabelAtIndex index ("+ind+") ouf of boundaries ("+labels.length+")!");
            System.exit(1);
        }

    }

    /**
     * Gets the ind-th label in the labels array. If ind is out of array's bounds it returns the empty string.
     * @param ind the index of the element to retrieve.
     * @return the ind-th label if it exists otherwise empty string.
     */
    public String getLabelAtIndex(int ind){
        String ret="";
        if(ind<labels.length && ind>-1){
            ret=labels[ind];
        }
        return ret;
    }


    /**
     * Gets the number of elements in the labels structure (i.e. how many different conditions we are quantitating on).
     * @return the length of the labels array.
     */
    public int getLabelsSize(){
        return labels.length;

    }

    /**
     * Gets all the labels of this dataset.
     * @return An array of strings containing all the labels for this particular row data file.
     */

    public String[] getAllLabels(){
        return labels;

    }


    /**
     * Gets the raw data fileName quantitation refers to.
     * @return a string with the fileName.
     */
    public String getFileName(){
        return fileName;

    }

    
    /**
     * Adds an element to the quantitativeData structure.
     * @param x is the xQuantities structure to add to the Vector.
     * @see xQuantities
     */
    public void addQuantitativeDataElem(xQuantities x){
        if(x!=null){
            quantitativeData.addElement(x);
        }
    }


    /**
     * Gets the ind-th element in the quantitativeData vector if it exists, null otherwise
     * @param ind the index of the element to retrive.
     * @return  the in-th xQuantities structure in the quantitativeData vector if it exists, null otherwise.
     */
    public xQuantities getQuantitativeDataElemAt(int ind){
        if(!(ind>quantitativeData.size())){
            return quantitativeData.elementAt(ind);

        }
        else{
            return null;
        }

    }

    /**
     * Gets the size of the quantitativeData structure
     * @return an integer with the size of the quantitativeData structure.
     */
    public int getQuantitativeDataSize(){
        return quantitativeData.size();
    }

    /**
     * Checks the validity of the xQuantData structure. In particular xQuantData is valid if:
     * <ul>
     * <li>The fileName is not empty</li>
     * <li>Labels are not empty and each label is unique</li>
     * <li>QuanitativetData is not empty</li>
     * <li>Every element in the quantitativeData structure has peptideSeq and proteinId not empty and the size of quantities array coincides with that of labels.</li>
     * </ul>
     * @return true if the xQuantData structure is valid. False otherwise.
     */
    public boolean isValid(){
        boolean fileNameChk=true;
        boolean labelsChk=true;
        boolean quantDataChk=true;
        int qntSize=quantitativeData.size();
        int labelSize=labels.length;
        if(fileName.equals(null) || fileName.length()==0){
            fileNameChk=false;
        }
        if((labelSize==0) || ! validLabels()){
                labelsChk=false;
        }

        if(qntSize==0){
            quantDataChk=false;
        }
        else{
            for(int i=0;i<qntSize;i++){
                xQuantities myQuantEl=this.getQuantitativeDataElemAt(i);
                if(myQuantEl.getProteinId().length()==0 || myQuantEl.getPeptideSeq().length()==0|| myQuantEl.getQuantitiesSize()!=labelSize){
                    quantDataChk=false;
                }
            }

        }
        return fileNameChk && labelsChk && quantDataChk;
    }



    /**
     * Checks if all the labels are unique.
     * @return True if labels in the labels array are unique, false otherwise.
     */
    private boolean validLabels(){
        int size=labels.length;
        int i,j=0;
        boolean ret=true;
        for(i=0;(i<size)&& ret;i++){
            for(j=i+1;j<size;j++){
                if(labels[i].equals(labels[j])){
                    ret=false;
                }

            }

        }
        return ret;
    }

    /**
     * The fileName of raw data in which the quantified peptides were identified.
     */

    public String fileName;


    /**
     * The array of the sperimental conditions peptides are quantitated on.
     */
    public String[] labels;

    /**
     * The actual quantities peptide by peptide.
     */
    public Vector<xQuantities> quantitativeData;



}
