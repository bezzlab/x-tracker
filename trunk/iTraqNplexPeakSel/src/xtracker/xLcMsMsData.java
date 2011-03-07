package xtracker;
import java.util.*;



/**
 * xLcMsMsData is the data structure holding a MS/MS raw spectra contained in the file specified by fileName of class xLoadData.
 * <p>
 * It is basically an object comprising the following elements:
 * <ul>
 * <li><code>RT</code> the retention time (put -1 if info not available)</li>
 * <li><code>lcMsMsElem</code> a vector of LC-Ms/Ms elements ordered by Parent Ion M/Z</li>
 * </ul>
 * <p>
 * Please note that the structure is ordered increasingly by Retention Time. Also, since lcMsMsData in xLoad data is ordered by Retention time, xLcMs has to implement the comparable interface.
 * The compareTo method has therefore to be specified.
 * @see identData_loadPlugin
 * @see xLcMsData
 * @see xLoad
 * @see xLoadData
 * @see xLcMsMsElem
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xLcMsMsData implements Comparable<xLcMsMsData>{

    /**
     * The constructor.
     * @param retTime is the retention time value.
     */
    public xLcMsMsData(float retTime){
        RT = retTime;
        lcMsMsElem = new Vector<xLcMsMsElem>();
    }

        /**
     * The constructor in case no data is known at construction time.
     */
    public xLcMsMsData(){
        RT = (float)-1.00;
        lcMsMsElem = new Vector<xLcMsMsElem>();
    }


     /**
      * Since lcMsMsData in xLoad data is ordered by Retention time, xLcMsMs has to implement the comparable interface.
      * The compareTo method has therefore to be specified.
      * @param comparable the element to compare to.
      * @return an integer with the result comparison:<br>-1 if the element compared to the present one has retention time higher than this.retTime.
      * <br>0 if the element compared to the present one has retention time equal to this.retTime.
      * <br>1 if the element compared to the present one has retention time smaller than this.retTime.
      */
     public int compareTo(xLcMsMsData comparable){
        int ret =0;
        if(comparable.getRetTime()> this.getRetTime()){
            ret=-1;
        }
        else{
            if(comparable.getRetTime()< this.getRetTime()){
                ret=+1;

            }
            else{
                ret=0;
            }

        }
        return ret;
    }




    /**
     * Adds an element to the peaklist. Any element of the peaklist is a couple (mz,intensity).
     * <p>
     * Please note that the structure is ordered by parIonMz increasingly.
     * @param parIonMzValue is the parent ion mz value.
     * @param chargeValue is the parent ion charge.
     * @param spect is the MS/MS spectrum.
     * @see xLcMsMsElem
     */
    public void addLcMsMsElem(float parIonMzValue, int chargeValue, xSpectrum spect){
        xLcMsMsElem myData=new xLcMsMsElem(parIonMzValue,chargeValue,spect);
         int pos=Collections.binarySearch(lcMsMsElem, myData);
         if(pos<0){
            pos=0-pos-1;
            lcMsMsElem.add( pos,myData);
          }
         else{
                    System.out.println("Error: trying to add LC/MS data to existing retention time!");
                    System.exit(1);

            }

        //OLD CODE
        /* int u_ind=lcMsMsElem.size();
         int l_ind=1;
         int index=-1;
         float value=-1f;


        boolean found = false;

        if(u_ind==0){
                  //Vector empty let's add a element somewhere, does not matter!
                 xLcMsMsElem myData=new xLcMsMsElem(parIonMzValue,chargeValue,spect);
                 lcMsMsElem.addElement(myData);

        }
        else{

            value=lcMsMsElem.elementAt(u_ind-1).getParentIonMz();
            if(parIonMzValue==value){
                    System.out.println("Error: trying to add LC-MS/MS data to existing parent Ion Mz!");
                    System.exit(1);

            }
            if(parIonMzValue> value){
                    /**
                     * The parent ion mz value of the element we are inserting is higher than that of the last element
                     * inserted, so the values will be added at the end of the structure.
                     */
/*                    xLcMsMsElem myData=new xLcMsMsElem(parIonMzValue,chargeValue,spect);
                    lcMsMsElem.addElement(myData);


            }
            else{
                value=lcMsMsElem.elementAt(l_ind-1).getParentIonMz();
                if(parIonMzValue==value){
                    System.out.println("Error: trying to add LC-MS/MS data to existing retention time!");
                    System.exit(1);

                 }
                if(parIonMzValue< value){
                        /**
                         * The mzValue of the element we are inserting is smaller than that of the first element
                         * inserted, so the values will be added at the beginning of the structure.
                         */
  /*                              xLcMsMsElem myData=new xLcMsMsElem(parIonMzValue,chargeValue,spect);
                                lcMsMsElem.add(0,myData);


                    }

                else{
                    /**
                     * The general case... the element we want is somewhere in the middle of the structure.
                     */
/*                     while(! found){
                            if(l_ind+1>=u_ind){
                                found=true;
                                xLcMsMsElem myData=new xLcMsMsElem(parIonMzValue,chargeValue,spect);
                                lcMsMsElem.add(u_ind-1,myData);


                            }
                            else{
                                index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();

                                if(parIonMzValue>value){
                                        l_ind=index;
                                }
                                else{
                                    if(parIonMzValue<value){
                                        u_ind=index;
                                    }
                                 else{
                                        xLcMsMsElem myData=new xLcMsMsElem(parIonMzValue,chargeValue,spect);
                                        lcMsMsElem.add(index,myData);
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
     * Performs a binary search looking for the index of the first element in the LC-MS/MS data structure
     * having a parent ion mz  higher than the <code>target</code>.
     * @param target is the target parent ion mz value.
     * @return the index of the first element having parent ion mz value higher than <code>target</target> <strong>Please note that the method
     * returns <code>-1</code> if all elements in the structure are smaller than <code>target</code>.</strong>
     */
    public int getIndexOfFirstHigherThanMz(float target){
        int ret=-1;
        xLcMsMsElem tmp=new xLcMsMsElem(target,1,null);
        int index=Collections.binarySearch(lcMsMsElem, tmp);
        if(index>0){
         ret=index;
        }
        else{
         ret=0-index-1;
        }

        //OLD CODE
        /* int ret=-1;
        int l_ind=0;
        int u_ind=lcMsMsElem.size()-1;
        int index=-1;
        float val=-1f;
        boolean found =false;
        float parIon=lcMsMsElem.elementAt(u_ind).getParentIonMz();
        if(target>parIon){
            found=true;
            ret=-1;

        }
        while(! found){
            index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
            val=lcMsMsElem.elementAt(index).getParentIonMz();
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
     * having a parent ion mz value smaller than the <code>target</code>.
     * @param target is the target parent ion mz value.
     * @return the index of the last element having parent ion mz smaller than <code>target</target>. <strong>Please note that the method
     * returns <code>-1</code> if all elements in the structure are higher than <code>target</code>.</strong>
     */
    public int getIndexOfLastSmallerThanMz(float target){
        int ret=-1;
        xLcMsMsElem tmp=new xLcMsMsElem(target,1,null);
        int index=Collections.binarySearch(lcMsMsElem, tmp);
        if(index>0){
         ret=index;
        }
        else{
         ret=0-index-2;
        }

        //OLD CODE
        /*  int ret=-1;
        int l_ind=0;
        int u_ind=lcMsMsElem.size()-1;
        int index=-1;
        float val=-1f;
        boolean found =false;
        float parIon=lcMsMsElem.elementAt(0).getParentIonMz();
        if(target<parIon){
            found=true;
            ret=-1;
        }
        while(! found){
            index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
            val=lcMsMsElem.elementAt(index).getParentIonMz();
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
     * having a parent ion mz value smaller than the <code>upper</code> and higher than <code>lower</code>.
     * @param lower is the lower acceptable parent ion mz value.
     * @param upper is the upper acceptable parent ion mz value.
     * @return the array containing the index of the lower element having parent ion mz value smaller than <code>target</target>. <strong>Please note that the method
     * returns the couple <code>(-1,-1)</code> if no such elements can be found.</strong> <p>This means that all elements having index
     * i such that res[0] &lt; i &gt; res[1] have parent ion mz value within the desired interval of parent ion mz values.
     */
    public int[] getIndexOfAllBetweenMz(float lower,float upper){
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
        max=getIndexOfLastSmallerThanMz(upper);
        min=getIndexOfFirstHigherThanMz(lower);

        if((min==-1)||(max==-1)){
            min=-1;
            max=-1;
        }
        ret[0]=min;
        ret[1]=max;
//System.out.println("OUT: " + ret[0] + " " + ret[1]);
        return ret;
    }

    /**
     * Returns the size of the MS/MS spectrum.
     * @return size an integer representing the lcMsMsElem size (that is, how many LC-MS/MS spectra
     * associated at the retention time <code>RT</code>.
     */
    public int getLcMsMsElemSize(){
        return lcMsMsElem.size();
    }

         /**
         * Retrieves the index-th  <code>xLcMsMsElem</code> structure containing the
         * LC-MS/MS raw data.
         * <p>
         * @return xLcMsMsElem the xLcMsMsElem structure containing the LC-MS/MS raw data. If the index
         * is higher than the number of elements in the Vector it returns null.
         * @param index is the index of the raw data vector to retrive.
         */
    public xLcMsMsElem getLcMsMsElemAt(int index){
        if(!(index>lcMsMsElem.size())){
             return lcMsMsElem.elementAt(index);
        }
        else{
            return null;
        }
       }

    /**
     * Returns the retention time at which the MS/MS spectrum was recorded.
     * @return the retention time <code>RT</code>.
     */
    public float getRetTime(){
        return RT;
    }






     /**
     * Sets the retention time at which the MS/MS spectrum was recorded.
     * @param rtValue is the retention time value
     */
    public void setRetentionTime(float rtValue){
        RT=rtValue;
    }


     /**
     * Performs a binary search looking for the index of the element in the LC-MS/MS structure
     * having a parent ion mz equal to the <code>target</code>.
     * @param target is the target parent ion mz.
     * @return the index of element having parent ion mz equal to <code>target</target>. <strong>Please note that the method
     * returns <code>-1</code> if no such parent ion mass value exists.</strong>
     */
    public int getLcMsMsIndexOfParIonMz(float target){
       int ret=-1;
        xLcMsMsElem tmp=new xLcMsMsElem(target,1,null);
        int index=Collections.binarySearch(lcMsMsElem, tmp);
        if(index>0){
         ret=index;
        }
        else{
         ret=-1;
        }

        /* int ret=-1;
        int l_ind=0;
        int u_ind=lcMsMsElem.size()-1;
        int index= -1;
        float val=-1f;
        String tarVal= String.valueOf(target);
        int prec= (tarVal.length()) - tarVal.lastIndexOf(".")-1;
        int tenPowered=Double.valueOf(Math.pow(10, prec)).intValue();
        System.out.println("Target:" + target + " precision:" + prec + " " + tenPowered);
        boolean found =false;

        while(! found){
            val=lcMsMsElem.elementAt(l_ind).getParentIonMz();
            if(val==target){
                found=true;
                ret=l_ind;
            }
            else{

                   val=lcMsMsElem.elementAt(u_ind).getParentIonMz();
                   val=Double.valueOf(Math.floor(val*tenPowered)).floatValue()/tenPowered;
                   System.out.println("Comparing: " +val + " with " + target + " = " + Float.compare(val,target));
                   if(Float.compare(val,target)==0){
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
                    val=lcMsMsElem.elementAt(index).getParentIonMz();
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
     * The retention time
     */
    public float RT;
    /**
     * The Ms/Ms element. Please note that the structure is ordered by parIonMz increasingly.
     * @see xLcMsMsElem
     */
    public Vector<xLcMsMsElem> lcMsMsElem;


}
