package xtracker;

import java.util.*;

/**
 * xLoad is the data structure populated by loadPlugins.
 * <p>
 * It is constituted by a vector of entries <code>data</code> representing raw data (one for each file that is loaded).
 * @see identData_loadPlugin
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xLoad {
     /**
       * The constructor. Creates an empty xLoad object
      */
    public xLoad(){

        Data = new Vector<xLoadData>();

     }


        /**
         * Retrieves the size of the Data structure loaded.
         * <p>
         * @return size the Data size.
         */
    public int getDataSize(){
        return Data.size();
    }

         /**
         * Retrieves the <code>xLoadData</code> structure containing all raw and identification data.
         * <p>
         * @return data the xLoadData structure containing all raw and identification data. If the index
         * is higher than the number of elements in the Vector it returns null.
         * @param index is the index of the raw data vector to retrive.
         */
    public xLoadData getDataElemAt(int index){
        if(!(index>Data.size())){
             return Data.elementAt(index);
        }
        else{
            return null;
        }
       }




         /**
         * Adds <code>xLoadData</code> to the Vector of all raw data.
         * <p>
         * @param d the xLoadData structure containing raw data loaded from a file.
         * @return boolean true if the file was xml and the set worked fine false otherwise.
         * @see xLoadData
         */
     public boolean addDataElem(xLoadData d){

        if (! (d ==null)){
            Data.addElement(d);

        }
        else{
            System.out.println("Error: Input data is empty!");
        }
        return (! (d==null));
    }



     /**
      * isRawDataValid checks the validity of the xLoad data structure with respect to raw data.
      * @return true if the structure is valid, false otherwise.
      * @see xLoadData

      */

     public boolean isRawDataValid(){
        boolean check_data =true;
        xLoadData tmp =null;

        for(int i=0;i<this.getDataSize();i++){
            tmp=this.getDataElemAt(i);
             check_data= (check_data) && tmp.isRawDataValid();
        }


        System.out.println("Data CHECK: " +check_data);
        return check_data;

     }


     /**
      * isIdentDataValid checks the validity of the xLoad data structure with respect to identification data.
      * @return true if the structure is valid, false otherwise.
      * @see xLoadData

      */

     public boolean isIdentDataValid(){
        boolean check_data =true;
        xLoadData tmp =null;

        for(int i=0;i<this.getDataSize();i++){
            tmp=this.getDataElemAt(i);
             check_data= (check_data) && tmp.isIdentDataValid();
        }


        System.out.println("Data CHECK: " +check_data);
        return check_data;

     }


    /**
     * The structure of raw data and identifications.
     * @see xLoadData
     */
    public Vector<xLoadData> Data;

}

