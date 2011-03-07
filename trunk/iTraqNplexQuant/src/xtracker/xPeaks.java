package xtracker;

import java.util.*;
/**
 * xPeaks is the data structure populated by peakSelPlugins.
 * <p>
 * It is constituted by a  vector of entries <code>correspondences</code> representing correspondence data (that is, what LC/MS or MS/MS peak in what file corresponds to s peptide).
 * @see peakSelPlugin
 * @see xCorrespondenceData
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */
public class xPeaks {

      /**
       * The constructor. Creates an empty xLoad object
      */
    public xPeaks(){


        correspondences = new Vector<xCorrespondences>();
     }

    /**
     * Gets the size of the correspondence data structure
     * @return size the size of the correspondence data structure
     * @see xCorrespondenceData
     */
    public int getSize(){

        return correspondences.size();
    }


    /**
     * Gets the index-th element in the correspondence data structure
     * @param index is the index of the element to retrieve
     * @return the xCorrespondenceData element at index <code>index</code> in the correspondences structure.
     * Returns null if index exceed Vector dimensions.
     * @see xCorrespondenceData
     */
    public xCorrespondences getElemAt(int index){
        if(!(index>correspondences.size())){
            return correspondences.elementAt(index);

        }
        else{
            return null;
        }
    }

    /**
     * Adds an element to the <code>correspondeces</code> data structure. Data is inserted preserving the uniqueness of fileName in peptideCorrespondenceData
     * as well as that of the couple proteinId, peptideSeq in xCorrespondenceData structure.
     * @param fileNM is the file name to which the correspondences are associated.
     * @param x the xCorrespondenceData to be added.
     * @see xCorrespondenceData
     * @see xCorrespondences
     */
    public void addPeptideCorrespondence(String fileNM, xCorrespondenceData x){
         xCorrespondences myCorr=this.getCorrOf(fileNM);

         if(myCorr==null){
            myCorr= new xCorrespondences(fileNM);
            myCorr.addPeptideCorrespondence(x);
            correspondences.addElement(myCorr);
         }
         else{
           //OK the filename is already present, let's try to add the correspondences to the same file.
           String protID=x.getProteinId();
           String pepSeq=x.getPeptideSeq();
           xCorrespondenceData tmp=myCorr.getPeptideCorrespondence(protID, pepSeq);
           //ProteinID and peptideSequence have to be unique, let's check if we have already some peaks associated to the couple pepID, pepSeq.
           //If we do not have any correspondence then we add it, otherwise we retrieve the lcMsMsCorr and the lcMsCorr from x and we add them to the
           //peptide.
           if(tmp==null){
                myCorr.addPeptideCorrespondence(x);
           }
           else{
               int size=x.getLcMsCorrSize();
               if(size>0){
                    for(int i=0;i<size;i++){
                        tmp.addLcMsCorr(x.getLcMsCorrElemAt(i));
                    }

               }
               //Let's try now to add LcMsMs correspondences
               size=x.getLcMsMsCorrSize();
               if(size>0){
                    for(int i=0;i<size;i++){
                        tmp.addLcMsMsCorr(x.getLcMsMsCorrElemAt(i));
                    }

                }
           }
         }



    }


    /**
     * Gets the xCorrespondences element associated to the raw data file filename.
     * @param filename the raw data file name.
     * @return xCorrespondenceData if found, null otherwise
     */
    public xCorrespondences getCorrOf(String filename){
        xCorrespondences ret =null;
        xCorrespondences tmp =null;
        int i=0;
        while(i<getSize() && ret==null){
            tmp=getElemAt(i);
            if(tmp.getFileName().equals(filename)){
                ret=tmp;
            }
            i++;
        }
        return ret;
    }


    /**
     * Checks the validity of the xLoad structure.
     * <p>
     * The structure is valid if the following holds:
     * <ul>
     * <li>The correspondences structure is not empty</li>
     * <li>Every element of the correspondences structure is a valid element</li>
     * </ul>
     * @return true if the structure is valid false otherwise.
     */
    public boolean isValid(){

        xCorrespondences myTmpData=null;
        boolean intermediateData=true;
        int i=0;
        if(correspondences.size()>0){
            for(i=0; i<correspondences.size();i++){
                myTmpData=this.getElemAt(i);
                intermediateData= intermediateData && myTmpData.isValid();

            }

            return intermediateData;
        }
        else{
            return false;
        }

    }






     /**
     * The structure of correspondence data
     * @see xCorrespondenceData
     */
    public Vector<xCorrespondences> correspondences;

}
