package xtracker;
import java.util.*;

/**
 * xQuant is the data structure populated by quantPlugins.
 * <p>
 * It is constituted by a vector of entries <code>quantificationData</code> representing quantifications raw data file by raw data file.
 * @see quantPlugin
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xQuant {

    /**
     * The constructor. It creates an empty quantificationData vector.
     */
    public xQuant(){
           quantificationData = new Vector<xQuantData>();

    }


        /**
         * Retrieves the size of the quantificationData structure loaded.
         * <p>
         * @return size the quantificationData size.
         */
    public int getQuantificationDataSize(){
        return quantificationData.size();
    }


    /**
     * Adds a new quantificationData element.
     * @param x the xQuantData to be added to the structure.
     */
    public void addQuantificationDataElem(xQuantData x){
        if(!(x == null)){
           quantificationData.addElement(x);
        }
     }



    /**
     * Gets the i-th element in the quantificationData vector.
     * @param i the index of the element to retrieve.
     * @return the i-th xQuantData element, null if i is out of vector's bounds.
     */
    public xQuantData getElementAtIndex(int i){
        xQuantData ret=null;
        if(! (i>this.quantificationData.size())){
             ret=quantificationData.elementAt(i);
        }
        return ret;
     }

    /**
     * The xQuant structure is well valid if the quantificationData is not empty and every entry is in turn valid.
     * @return true if the structure is valid. False otherwise.
     */
    public boolean isValid(){
        boolean notEmpty=false;
        boolean ret=true;

        int i=-1;
        int size=quantificationData.size();
        if(size>0){
            notEmpty=true;
        }
        for(i=0;i<size;i++){
            ret=(ret && quantificationData.elementAt(i).isValid());
        }
       return (ret&& notEmpty);
    }



    
    /**
     * This is the vector of qunantitative information arraged on a raw data file basis.
     * @see xQuantData
     */
    public Vector<xQuantData> quantificationData;
}
