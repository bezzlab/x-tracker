package xtracker;

import java.util.*;

/**
 * xLoadData is the data structure populated by loadPlugins and contained in the Data vector of xLoad.
 * <p>
 * It is constituted by a single <code>parameterFile</code> (an xml file) and vector of entries <code>data</code> representing raw data (one for each file that is loaded).
 * @see identData_loadPlugin
 * @see xLoad
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xLoadData {
     /**
       * The constructor. Creates an empty xLoad object
      * @param fileValue is the fileName
      */
    public xLoadData(String fileValue){


        fileName=fileValue;
        lcMsData= new Vector<xLcMsData>();
        lcMsMsData = new Vector<xLcMsMsData>();
        identificationData = new Vector<identInputData>();
     }

    /**
         * Retrieves the <code>fileName</code> (that is the name of the file containing raw data processed).
         * <p>
         * @return fileName the file containing all the raw data.
         */
    public String getFileName(){
        return fileName;
    }

        /**
         * Sets the <code>fileName</code> (that is the name of the file containing raw data processed).
         * <p>
         * @param file the string containing the input file name.
         */
    public void setFileName(String file){
            fileName=file;
        }

            /**
         * Gets the number of elements in the <code>lcMsData</code> vector.
         * <p>
         * @return number of elements in lcMsData or zero if empty.
         * @see xLcMsData
         */
     public int getLcMsDataSize(){

        return lcMsData.size();
    }

        /**
         * Gets the number of elements in the <code>lcMsMsData</code> vector.
         * <p>
         * @return number of elements in lcMsMsData or zero if empty.
         * @see xLcMsMsData
         */
     public int getLcMsMsDataSize(){

        return lcMsMsData.size();
    }

            /**
         * Gets the number of elements in the <code>identificationData</code> vector.
         * <p>
         * @return number of elements in identificationData or zero if empty.
         * @see identInputData
         */
     public int getIdentificationDataSize(){

        return (identificationData.size());
    }

        /**
         * Adds <code>xLcMsData</code> to the Vector of all LC/MS data. <strong>Please note that the structure is
         * order by retention time in <u>ascending</u> way.</strong>
         * <p>
         * @param rt is the retention time.
         * @param spec is the spectrum.
         * @see xSpectrum
         * @see xLcMsData
         */
     public void addLcMsData(float rt, xSpectrum spec){

         xLcMsData myData=new xLcMsData(rt,spec);
         int pos=Collections.binarySearch(lcMsData, myData);
         if(pos<0){
            pos=0-pos-1;
            lcMsData.add( pos,myData);
          }
         else{
                    System.out.println("Error: trying to add LC/MS data to existing retention time!");
                    System.exit(1);

            }







         //OLD CODE!

/*         int u_ind=lcMsData.size();
         int l_ind=1;
         int index=-1;
         float value=-1f;


        boolean found = false;

        if(u_ind==0){
                  //Vector empty let's add a value somewhere, does not matter!
                 xLcMsData myData=new xLcMsData(rt,spec);
                 lcMsData.addElement(myData);

        }
        else{

            value=lcMsData.elementAt(u_ind-1).getRetTime();
            if(rt==value){
                    System.out.println("Error: trying to add LC/MS data to existing retention time!");
                    System.exit(1);

            }
            if(rt> value){
                    /**
                     * The retention time value of the element we are inserting is higher than that of the last element
                     * inserted, so the values will be added at the end of the structure.
                     */
     /*               xLcMsData myData=new xLcMsData(rt,spec);
                    lcMsData.addElement(myData);


            }
            else{
                value=lcMsData.elementAt(l_ind-1).getRetTime();
                if(rt==value){
                    System.out.println("Error: trying to add LC/MS data to existing retention time!");
                    System.exit(1);

                 }
                if(rt< value){
                        /**
                         * The mzValue of the element we are inserting is smaller than that of the first element
                         * inserted, so the values will be added at the beginning of the structure.
                         */
/*                                xLcMsData myData=new xLcMsData(rt,spec);
                                lcMsData.add(0,myData);


                    }

                else{
                    /**
                     * The general case... the element we want is somewhere in the middle of the structure.
                     */
/*                     while(! found){
                            if(l_ind+1>=u_ind){
                                found=true;
                                xLcMsData myData=new xLcMsData(rt,spec);
                                lcMsData.add(u_ind-1,myData);


                            }
                            else{
                                index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
                                value=lcMsData.elementAt(index).getRetTime();
                                if(rt>value){
                                        l_ind=index;
                                }
                                else{
                                    if(rt<value){
                                        u_ind=index;
                                    }
                                 else{
                                        xLcMsData myData=new xLcMsData(rt,spec);
                                        lcMsData.add(index,myData);
                                        found=true;
                                    }
                                }
                            }
                     }

                }


            }



        }



*/

    }

        /**
         * Adds an <code>xLcMsMsData</code> element to the Vector of all MS/MS data.
         * <p>
         * Please note that the structure lcMsMsData is ordered by Retention time increasingly and each of its elements
         * contains a Vector of xLcMsMsElem ordered by parent ion Mz value increasingly.
         * @param rtValue the retention time value.
         * @param parentIonMzValue the parent ion mz value.
         * @param chargeVal the charge.
         * @param spec the spectrum to add.
         * @see xLcMsMsData
         * @see xLcMsMsElem
         */
     public void addLcMsMsData(float rtValue, float parentIonMzValue, int chargeVal, xSpectrum spec){

         xLcMsMsData myData=new xLcMsMsData(rtValue);
         int pos=Collections.binarySearch(lcMsMsData, myData);
         if(pos<0){
            pos=0-pos-1;
            myData.addLcMsMsElem(parentIonMzValue, chargeVal, spec);
            lcMsMsData.add( pos,myData);
          }
         else{
             //OK RT already present, let's just add the xLcMsMsElem to it
             myData=lcMsMsData.elementAt(pos);
             myData.addLcMsMsElem(parentIonMzValue, chargeVal, spec);
             lcMsMsData.setElementAt(myData, pos);

            }

   //OLD CODE
  /*       int u_ind=lcMsMsData.size();
         int l_ind=1;
         int index=-1;
         float value=-1f;


        boolean found = false;

        if(u_ind==0){
                  //Vector empty let's add a value somewhere, does not matter!
                 xLcMsMsData myData=new xLcMsMsData(rtValue);
                 myData.addLcMsMsElem(parentIonMzValue, chargeVal, spec);
                 lcMsMsData.addElement(myData);

        }
        else{

            value=lcMsMsData.elementAt(u_ind-1).getRetTime();
            if(rtValue==value){
                 xLcMsMsData myData=lcMsMsData.elementAt(u_ind-1);
                 myData.addLcMsMsElem(parentIonMzValue, chargeVal, spec);
                 lcMsMsData.setElementAt(myData, u_ind-1);

            }
            if(rtValue> value){
                    /**
                     * The retention time value of the element we are inserting is higher than that of the last element
                     * inserted, so the values will be added at the end of the structure.
                     */
/*                    xLcMsMsData myData=new xLcMsMsData(rtValue);
                    myData.addLcMsMsElem(parentIonMzValue, chargeVal, spec);
                    lcMsMsData.addElement(myData);


            }
            else{
                value=lcMsMsData.elementAt(l_ind-1).getRetTime();
                if(rtValue==value){
                    xLcMsMsData myData=lcMsMsData.elementAt(l_ind-1);
                    myData.addLcMsMsElem(parentIonMzValue, chargeVal, spec);
                    lcMsMsData.setElementAt(myData, l_ind-1);

                 }
                if(rtValue< value){
                        /**
                         * The mzValue of the element we are inserting is smaller than that of the first element
                         * inserted, so the values will be added at the beginning of the structure.
                         */
  /*                              xLcMsMsData myData=new xLcMsMsData(rtValue);
                                myData.addLcMsMsElem(parentIonMzValue, chargeVal, spec);
                                lcMsMsData.add(0,myData);


                    }

                else{
                    /**
                     * The general case... the element we want is somewhere in the middle of the structure.
                     */
   /*                  while(! found){
                            if(l_ind+1>=u_ind){
                                found=true;
                                xLcMsMsData myData=new xLcMsMsData(rtValue);
                                myData.addLcMsMsElem(parentIonMzValue, chargeVal, spec);
                                lcMsMsData.add(u_ind-1,myData);

                            }
                            else{
                                index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
                                value=lcMsMsData.elementAt(index).getRetTime();
                                if(rtValue>value){
                                        l_ind=index;
                                }
                                else{
                                    if(rtValue<value){
                                        u_ind=index;
                                    }
                                 else{
                                        xLcMsMsData myData=lcMsMsData.elementAt(index);
                                        myData.addLcMsMsElem(parentIonMzValue, chargeVal, spec);
                                        lcMsMsData.setElementAt(myData, index);
                                    }
                                }
                            }
                     }

                }


            }



        }
    */

    }

        /**
         * Adds <code>identInputData</code> to the Vector of all identification data.
         * <p>
         * @param d the identInputData structure containing raw data loaded from a file.
         * @return boolean true if the file was xml and the set worked fine false otherwise.
         * @see identInputData
         */
     public boolean addIdentificationData(identInputData d){

        if (! (d==null)){
            identificationData.add(d);

        }
        else{
            System.out.println("Error: trying to add empty identification data!");
            System.exit(1);
        }
        return (! (d==null));
    }



        /**
         * Retrieves the index-th  <code>xLcMsData</code> structure containing the
         * LC/MS raw data.
         * <p>
         * @return xLcMsData the xLcMsData structure containing the LC/MS raw data. If the index
         * is higher than the number of elements in the Vector it returns null.
         * @param index is the index of the raw data vector to retrive.
         */
    public xLcMsData getLcMsDataElemAt(int index){
        if(!(index>lcMsData.size())){
             return lcMsData.elementAt(index);
        }
        else{
            return null;
        }
       }


            /**
         * Retrieves the index-th  <code>xLcMsMsData</code> structure containing the
         * MS/MS raw data.
         * <p>
         * @return xLcMsMsData the xLcMsMsData structure containing the MS/MS raw data. If the index
         * is higher than the number of elements in the Vector it returns null.
         * @param index is the index of the raw data vector to retrive.
         */
    public xLcMsMsData getLcMsMsDataElemAt(int index){
        if(!(index>lcMsMsData.size())){
             return lcMsMsData.elementAt(index);
        }
        else{
            return null;
        }
       }

        /**
         * Retrieves the <code>identInputData</code> structure containing all the identification data.
         * <p>
         * @return data the identInputData structure containing all raw data. If the index
         * is higher than the number of elements in the Vector it returns null.
         * @param index is the index of the identification data vector to retrive.
         */
    public identInputData getIdentInputDataElemAt(int index){
            if(!(index>identificationData.size())){
                return identificationData.elementAt(index);
            }
            else{
                return null;
            }
        }






     /**
      * isRawDataValid checks the validity of the xLoadData structure with respect to raw data.
      * <p>
      * xLoadData raw data structure is valid if all the following conditions are met:
      * <ul>
      *     <li>The raw data file <code>fileName</code>is not empty.</li>
      *     <li>The lcMsData structure is not empty or the lcMsMsData structure is not empty.</li>
      * </ul>
      * @return true if the structure is valid, false otherwise.
      * @see xLoad
      * @see identInputData
      */

     public boolean isRawDataValid(){
        boolean check_file;
        boolean check_lcms_data;
        boolean check_msms_data;



        check_file = ( fileName.length()>0);

        check_lcms_data = (! lcMsData.isEmpty());
        check_msms_data = (! lcMsMsData.isEmpty());

        System.out.println("xLoadData: FILE's CHECK: " +check_file);
        System.out.println("xLoadData: RAW DATA CHECK: " + (check_lcms_data ||check_msms_data));

        return check_file && (check_lcms_data ||check_msms_data);

     }


     /**
      * isIdentDataValid checks the validity of the xLoadData structure with respect to identification data.
      * <p>
      * xLoadData structure is valid if all the following conditions are met:
      * <ul>
      *     <li>The identification data structure is not empty.</li>
      * </ul>
      * @return true if the structure is valid, false otherwise.
      * @see xLoad
      * @see identInputData
      */

     public boolean isIdentDataValid(){
        boolean check_identification_data;


        check_identification_data = (! identificationData.isEmpty());

        System.out.println("xLoadData: IDENTIFICATION DATA CHECK: " + check_identification_data);

        return check_identification_data;

     }


    /**
     * Deletes the identifications associated to the protein protId
     * @param pId the protein identifier of the identifications to be deleted.
     */
     public void deleteIdentOfProteinId(String pId){
         for(int i= this.getIdentificationDataSize()-1;i>-1;i--){
             if(identificationData.elementAt(i).getProteinId().equals(pId)){
                identificationData.removeElementAt(i);
             }
         }

     }

    /**
     * Deletes the identifications associated to the peptide having peptide sequence pepSeq
     * @param pepSeq the peptide sequence of the identifications to be deleted.
     */
     public void deleteIdentOfPeptideSeq(String pepSeq){
         for(int i= this.getIdentificationDataSize()-1;i>-1;i--){
             if(identificationData.elementAt(i).getPeptideSeq().equals(pepSeq)){
                identificationData.removeElementAt(i);

             }
         }

     }

     /**
      * Counts how many identifications belong to peptide having sequence pepSeq and protein id protId
      * @param pepSeq the peptide sequence
      * @param protId the protein id
      * @return ret the number of identifications
      */
     public int countIdentofPeptide(String pepSeq, String protId){
        int ret=0;

        for(int i=0;i<this.getIdentificationDataSize();i++){
            identInputData myDta=identificationData.elementAt(i);
             if(myDta.getPeptideSeq().equals(pepSeq)&& myDta.getProteinId().equals(protId)){
                ret++;

             }
         }
         return ret;

     }



     /**
      * Retrieves all the identifications of a particular protein id.
      * @param protId is the protein id the identifications are related to.
      * @return Vector<identInputData> the vector of identifications related to peptide having id protId.
      * @see identInputData
      */
     public Vector<identInputData> getAllIdentOfProteinId(String protId){
        Vector<identInputData> ret = new Vector<identInputData>();
        identInputData tmpElem=null;
        int i=0;
        for(i=0;i<this.getIdentificationDataSize();i++){
            tmpElem=this.getIdentInputDataElemAt(i);
            if(tmpElem.getProteinId().equals(protId)){
                ret.add(tmpElem);
            }

        }


        return ret;

     }

     /**
      * Retrieves all the identifications of a particular peptide sequence.
      * @param pepSeq is the peptide sequence identifications are related to.
      * @return Vector<identInputData> the vector of identifications related to peptide having sequence pepSeq.
      * @see identInputData
      */
     public Vector<identInputData> getAllIdentOfPeptideSeq(String pepSeq){
        Vector<identInputData> ret = new Vector<identInputData>();
        identInputData tmpElem=null;
        int i=0;
        for(i=0;i<this.getIdentificationDataSize();i++){
            tmpElem=this.getIdentInputDataElemAt(i);
            if(tmpElem.getPeptideSeq().equals(pepSeq)){
                ret.add(tmpElem);
            }

        }


        return ret;

     }




    /**
     * Performs a binary search looking for the index of the first element in the LC-MS data structure
     * having a retention time higher than the <code>target</code>.
     * @param target is the target retention time (i.e. the rt for which we want to get the first element in the structure having retention time higher).
     * @return the index of the first element having retention time higher than <code>target</target> <strong>Please note that the method
     * returns <code>-1</code> if all elements in the structure are smaller than <code>target</code>.</strong>
     */
    public int getLcMsIndexOfFirstHigherThanRT(float target){
        int ret=-1;
        xLcMsData tmp=new xLcMsData(target,null);
        int index=Collections.binarySearch(lcMsData, tmp);
        if(index>0){
         ret=index;
        }
        else{
         ret=0-index-1;
        }



//OLD CODE!!!
/*        int l_ind=0;
        int u_ind=lcMsData.size()-1;
        int index=-1;
        float val=-1f;
        boolean found =false;
        float rt=lcMsData.elementAt(u_ind).getRetTime();
        if(target>rt){
            found=true;
            ret=-1;

        }
        while(! found){
            index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
            val=lcMsData.elementAt(index).getRetTime();
            if(l_ind+1>=u_ind){
                found=true;
                ret=u_ind;
            }
            else{
                if(val>target){
                    u_ind=index;
                }
                else{
                    if(val<target){
                       l_ind=index;
                    }
                    else{
                        found=true;
                        ret=index;

                    }

                }

            }

        }
*/
       return ret;
    }



    /**
     * Performs a binary search looking for the index of the last element in the LC/MS structure
     * having a retention time smaller than the <code>target</code>.
     * @param target is the target retention time (i.e. the rt for which we want to get the last element in the structure having retention time smaller).
     * @return the index of the last element having retention time smaller than <code>target</target>. <strong>Please note that the method
     * returns <code>-1</code> if all elements in the structure are higher than <code>target</code>.</strong>
     */
    public int getLcMsIndexOfLastSmallerThanRT(float target){

       int ret=-1;
       xLcMsData tmp=new xLcMsData(target,null);
       int index=Collections.binarySearch(lcMsData, tmp);
       if(index>0){
        ret=index;
       }
       else{
        ret=0-index-2;
       }



       //OLD CODE
       /*
       int l_ind=0;
        int u_ind=lcMsData.size()-1;
        int index=-1;
        float val=-1f;
        boolean found =false;
        float rt=lcMsData.elementAt(0).getRetTime();
        if(target<rt){
            found=true;
            ret=-1;
        }
        while(! found){
            index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
            val=lcMsData.elementAt(index).getRetTime();
            if(l_ind+1>=u_ind){
                found=true;
                ret=l_ind;
            }
            else{
                if(val>target){
                    u_ind=index;
                }
                else{
                    if(val<target){
                       l_ind=index;
                    }
                    else{
                        found=true;
                        ret=index;

                    }

                }

            }

        }
*/
       return ret;

    }



        /**
     * Performs two binary searches looking for the index of the indexes of those elements in the LC/MS structure
     * having a retention time smaller than the <code>upper</code> and higher than <code>lower</code>.
     * @param lower is the smallest acceptable retention time.
     * @param upper is the biggest acceptable retention time.
     * @return the array containing the index of the lower element having retention time smaller than <code>target</target>. <strong>Please note that the method
     * returns the couple <code>(-1,-1)</code> if no such elements can be found.</strong> <p>This means that all elements having index
     * i such that res[0] &lt; i &gt; res[1] have retention time within the desired interval of retention times.
     */
    public int[] getLcMsIndexOfAllBetweenRT(float lower,float upper){
        int[] ret= new int[2];
        int min=0;
        int max=0;
        //
        if(lower>upper){
            float tmp;
            tmp=upper;
            upper=lower;
            lower=tmp;

        }
        max=getLcMsIndexOfLastSmallerThanRT(upper);
        min=getLcMsIndexOfFirstHigherThanRT(lower);

        if((min==-1)||(max==-1)){
            min=-1;
            max=-1;
        }
        ret[0]=min;
        ret[1]=max;

        return ret;
    }



    /**
     * Gets the index-th Retention Time Value from the LC/MS matrix.
     * <p>
     * @param index is the index of the RT that has to be retrieved.
     * @return The index-th Retention Time Value or  <code>-1</code> if index exceeds allowed dimensions.
     */
    public float getLcMsRtAt(int index){
        if(!(index> lcMsData.size())){
            float ret=lcMsData.elementAt(index).getRetTime();
            return ret;
        }
        else{
            return -1;
        }

    }


         /**
     * Gets the index-th Spectrum (i.e. peak list, mz-intensity couples) from the LC/MS matrix.
     * <p>
     * @param index is the index of the Spectrum that has to be retrieved.
     * @return The index-th Spectrum or  <code>null</code> if index exceeds allowed dimensions.
     */
    public xSpectrum getLcMsSpectrumElemAtIndex(int index){
        if(!(index> lcMsData.size())){
            xSpectrum ret=lcMsData.elementAt(index).getSpectrum();
            return ret;
        }
        else{
            return null;
        }

    }

    /**
     * Performs a binary search looking for the index of the last element in the LC/MS structure
     * having a retention time equal to the <code>target</code>.
     * @param target is the target retention time .
     * @return the index of element having retention time equal to <code>target</target>. <strong>Please note that the method
     * returns <code>-1</code> if no such retention time value exists.</strong>
     */
    public int getLcMsIndexOfRt(float target){
        int ret=-1;

        xLcMsData tmp=new xLcMsData(target,null);
        int index=Collections.binarySearch(lcMsData, tmp);
        if(index>0){
         ret=index;
        }
        else{
         ret=-1;
        }


    //OLD CODE
    /*    int ret=-1;
        int l_ind=0;
        int u_ind=lcMsData.size()-1;
        int index= -1;
        float val=-1f;
        boolean found =false;

        while(! found){
            val=lcMsData.elementAt(l_ind).getRetTime();
            if(val==target){
                found=true;
                ret=l_ind;
            }
            else{

                   val=lcMsData.elementAt(u_ind).getRetTime();
                   if(val==target){
                           found=true;
                           ret=u_ind;
                        }
                }
            if((found==false)&&(l_ind+1>=u_ind)){
                found=true;
                ret=-1;

            }

            else{
                if(!found){
                    index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
                    val=lcMsData.elementAt(index).getRetTime();
                        if(val>target){
                            u_ind=index;
                        }
                        else{
                                if(val<target){
                                    l_ind=index;
                                }
                                else{
                                     found=true;
                                    ret=index;
                                }

                        }

              }
            }
        }
*/
       return ret;
    }



    /**
     * Gets the Spectrum element from the matrix corresponding to a retention value rtValue.
     * <p>
     * @param rtValue is the retention time of the Spectrum that has to be retrieved.
     * @return The Spectrum corresponding to a retention Value of rtValue or  <code>null</code> if such a spectrum does not exist.
     */
    public xSpectrum getLcMsSpectrumAtRT(float rtValue){
        xSpectrum ret;
        int index=this.getLcMsIndexOfRt(rtValue);

        if(index>-1){
            ret=lcMsData.elementAt(index).getSpectrum();
        }
        else{
            ret=null;
        }
        return ret;
    }


        /**
     * Performs a binary search looking for the index of the first element in the LC-MS/MS data structure
     * having a retention time higher (or equal) than the <code>target</code>.
     * @param target is the target retention time (i.e. the rt for which we want to get the first element in the structure having retention time higher).
     * @return the index of the first element having retention time higher than <code>target</target> <strong>Please note that the method
     * returns <code>-1</code> if all elements in the structure are smaller than <code>target</code>.</strong>
     */
    public int getLcMsMsIndexOfFirstHigherThanRT(float target){

        int ret=-1;
        xLcMsMsData tmp=new xLcMsMsData(target);
        int index=Collections.binarySearch(lcMsMsData, tmp);
        if(index>0){
         ret=index;
        }
        else{
         ret=0-index-1;
        }
        //OLD CODE
        /*   int ret=-1;
        int l_ind=0;
        int u_ind=lcMsMsData.size()-1;
        int index=-1;
        float val=-1f;
        boolean found =false;
        float rt=lcMsMsData.elementAt(u_ind).getRetTime();
        if(target>rt){
            found=true;
            ret=-1;

        }
        while(! found){
            index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
            val=lcMsMsData.elementAt(index).getRetTime();
            if(l_ind+1>=u_ind){
                found=true;
                ret=u_ind;
            }
            else{
                if(val>target){
                    u_ind=index;
                }
                else{
                    if(val<target){
                       l_ind=index;
                    }
                    else{
                        found=true;
                        ret=index;

                    }

                }

            }

        }
*/
       return ret;
    }



    /**
     * Performs a binary search looking for the index of the last element in the LC-MS/MS structure
     * having a retention time smaller (or equal) than the <code>target</code>.
     * @param target is the target retention time (i.e. the rt for which we want to get the last element in the structure having retention time smaller).
     * @return the index of the last element having retention time smaller than <code>target</target>. <strong>Please note that the method
     * returns <code>-1</code> if all elements in the structure are higher than <code>target</code>.</strong>
     */
    public int getLcMsMsIndexOfLastSmallerThanRT(float target){
       int ret=-1;

       xLcMsMsData tmp=new xLcMsMsData(target);
       int index=Collections.binarySearch(lcMsMsData, tmp);
       if(index>0){
        ret=index;
       }
       else{
        ret=0-index-2;
       }

        //OLD CODE
        /*        int l_ind=0;
        int u_ind=lcMsMsData.size()-1;
        int index=-1;
        float val=-1f;
        boolean found =false;
        float rt=lcMsMsData.elementAt(0).getRetTime();
        if(target<rt){
            found=true;
            ret=-1;
        }
        while(! found){
            index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
            val=lcMsMsData.elementAt(index).getRetTime();
            if(l_ind+1>=u_ind){
                found=true;
                ret=l_ind;
            }
            else{
                if(val>target){
                    u_ind=index;
                }
                else{
                    if(val<target){
                       l_ind=index;
                    }
                    else{
                        found=true;
                        ret=index;

                    }

                }

            }

        }
*/
       return ret;
    }



        /**
     * Performs two binary searches looking for the index of the indexes of those elements in the LC-MS/MS structure
     * having a retention time smaller than the <code>upper</code> and higher than <code>lower</code>.
     * @param lower is the lower acceptable retention time.
     * @param upper is the upper acceptable retention time.
     * @return the array containing the index of the lower element having retention time smaller than <code>target</target>. <strong>Please note that the method
     * returns the couple <code>(-1,-1)</code> if no such elements can be found.</strong> <p>This means that all elements having index
     * i such that res[0] &lt; i &gt; res[1] have retention time within the desired interval of retention times.
     */
    public int[] getLcMsMsIndexOfAllBetweenRT(float lower,float upper){
        int[] ret= new int[2];
        int min=0;
        int max=0;
        //
        if(lower>upper){
            float tmp;
            tmp=upper;
            upper=lower;
            lower=tmp;

        }
        max=getLcMsMsIndexOfLastSmallerThanRT(upper);
        min=getLcMsMsIndexOfFirstHigherThanRT(lower);

        if((min==-1)||(max==-1)){
            min=-1;
            max=-1;
        }
        ret[0]=min;
        ret[1]=max;

  //      System.out.println("Index of last smaller than " + upper + ":" +max);
  //      System.out.println("Index of first bigger than " + lower + ":" +min);
        return ret;
    }



    /**
     * Gets the index-th Retention Time Value from the LC-MS/MS matrix.
     * <p>
     * @param index is the index of the RT that has to be retrieved.
     * @return The index-th Retention Time Value or  <code>-1</code> if index exceeds allowed dimensions.
     */
    public float getLcMsMsRtAt(int index){
        if(!(index> lcMsMsData.size())){
            float ret=lcMsMsData.elementAt(index).getRetTime();
            return ret;
        }
        else{
            return -1;
        }

    }



    /**
     * Performs a binary search looking for the index of the element in the LC-MS/MS structure
     * having a retention time equal to the <code>target</code>.
     * @param target is the target retention time.
     * @return the index of the last element having retention time equal to <code>target</target>. <strong>Please note that the method
     * returns <code>-1</code> no such retention time element exists.</strong>
     */

    public int getLcMsMsIndexOfRt(float target){
        int ret=-1;

        xLcMsMsData tmp=new xLcMsMsData(target);
        int index=Collections.binarySearch(lcMsMsData, tmp);
        if(index>0){
         ret=index;
        }
        else{
         ret=-1;
        }

        //OLD CODE
        /*   int ret=-1;
        int l_ind=0;
        int u_ind=lcMsMsData.size()-1;
        int index= -1;
        float val=-1f;
        boolean found =false;

        while(! found){
            val=lcMsMsData.elementAt(l_ind).getRetTime();
            if(val==target){
                found=true;
                ret=l_ind;
            }
            else{

                   val=lcMsMsData.elementAt(u_ind).getRetTime();
                   if(val==target){
                           found=true;
                           ret=u_ind;
                        }
                }
            if((found==false)&&(l_ind+1>=u_ind)){
                found=true;
                ret=-1;

            }

            else{
                if(!found){
                    index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
                    val=lcMsMsData.elementAt(index).getRetTime();
                        if(val>target){
                            u_ind=index;
                        }
                        else{
                                if(val<target){
                                    l_ind=index;
                                }
                                else{
                                     found=true;
                                    ret=index;
                                }

                        }

              }
            }
        }
*/
       return ret;
    }






    /**
     * The raw data file all the information in this object refer to.
     */
    public String fileName;
    /**
     * The structure of LC/MS raw data
     *  From it is possible to get various information including:
     * <ul>
     *  <li>The MS spectrum: fix the Retention Time and get all Intensity values over M/Z.</li>
     *  <li>The Single Ion Chromatogram: fix the M/Z of interest and get all Intensity values over Retention Time.</li>
     *  <li>The Total Ion Chromatogram (TIC): for each Retention Time instant sum over all the m/z the Intensity.</li>
     *  <li>The Extracted Ion Chromatogram (XIC): set a M/Z window and sum over it all the Intensity values.</li>
     * </ul>
     * @see xLcMsData
     */
    public Vector<xLcMsData> lcMsData;
    /**
     * The structure of MS/MS raw data
     * @see xLcMsMsData
     */
    public Vector<xLcMsMsData> lcMsMsData;

    /**
     * The structure of identification data associated to the fileName raw data
     * @see identInputData
     */
    public Vector<identInputData> identificationData;


}

