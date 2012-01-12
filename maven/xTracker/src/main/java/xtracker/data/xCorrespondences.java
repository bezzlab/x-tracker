package xtracker.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/**
 * xCorrespondences is the data structure whereby peptide correspondences are associated to raw data files. All peptide correspondence
 * contained in the
 * @see peakSelPlugin
 * @see xLoad
 * @author Dr. Luca Bianco (l.bianco@cranfield.ac.uk) -- Cranfield Health, Cranfield University -- X-Tracker Project
 */

public class xCorrespondences {

    /**
     * The constructor, creates a xCorrespondences structure for the provided raw data file name.
     * @param fileNM the file name of the raw data correspondences are associated to.
     */
    public xCorrespondences(String fileNM) {
        fileName = fileNM;
        peptideCorrespondenceData = new ArrayList<xCorrespondenceData>();
    }

    /**
     * Gets the number of elements in the peptideCorrespondenceData structure.
     * @return an integer with the number of elements in the structure.
     */
    public int getPeptideCorrespondenceDataSize() {
        return peptideCorrespondenceData.size();
    }

    /**
     * Adds a peptide correspondence to the peptideCorrespondenceData structure if the correspondence is not empty.
     * @param x is the correspondence structure to add. Please note that it is not allowed to have two different xCorrespondenceData elements with the same combination of proteinID, peptideSeq. Therefore <b>if the couple (proteinID,peptideSeq) is already present into the structure nothing is added.</b>
     * @see xCorrespondenceData
     */
    public void addPeptideCorrespondence(xCorrespondenceData x) {
        if (x != null) {
            String pID = x.getProteinId();
            String pSeq = x.getPeptideSeq();
            if (getPeptideCorrespondence(pID, pSeq) == null) {
                peptideCorrespondenceData.add(x);
            }
        }
    }

    /**
     * Gets the index-th element in the peptideCorrespondenceData structure if it exists.
     * @param index is the index of the element to retrive.
     * @return the index-th element in the data structure if it exists, null otherwise.
     */
    public xCorrespondenceData getPeptideCorrespondenceDataElemtAt(int index) {
        if (index > -1 && index < peptideCorrespondenceData.size()) {
            return peptideCorrespondenceData.get(index);
        }
        return null;
    }

    /**
     * Gets the peptide correspondence element associated to protein id protID and to peptide sequence pepSeq if it exists.
     * @param protID is the protein id the correspondence belongs to.
     * @param pepSeq is the peptide sequence the correspondence belongs to.
     * @return the correspondence element associated to protID and pepSeq in the data structure if it exists, null otherwise.
     */
    public xCorrespondenceData getPeptideCorrespondence(String protID, String pepSeq) {
        xCorrespondenceData tmp = null;
        boolean searching = true;
        for (int i = 0; i < peptideCorrespondenceData.size() && searching; i++) {
            tmp = peptideCorrespondenceData.get(i);
            if (tmp.getProteinId().equals(protID) && tmp.getPeptideSeq().equals(pepSeq)) {
                searching = false;
                return tmp;
            }
        }
        return null;
    }

    /**
     * Adds an element to the <code>peptideCorrespondenceData</code> data structure.
     * @param protId is the peptide identifier to which the correspondence has to be added
     * @param pepSeq is the peptide sequence to which the correspondence has to be added
     * @param x the xMsMsCorrData to be added
     */
    public void addCorrLcMsMsData(String protId, String pepSeq, xLcMsMsCorr x) {
        xCorrespondenceData myCorr = getPeptideCorrespondence(protId, pepSeq);
        if (myCorr == null) {
            myCorr = new xCorrespondenceData(protId, pepSeq);
            myCorr.addLcMsMsCorr(x);
            peptideCorrespondenceData.add(myCorr);
        } else {
            myCorr.addLcMsMsCorr(x);
        }
    }

    /**
     * Adds an element to the <code>peptideCorrespondenceData</code> data structure.
     * @param protId is the protein identifier (the peptide belongs to) to which the correspondence has to be added
     * @param pepSeq is the peptide sequence to which the correspondence has to be added
     * @param x the xLcMsCorrData to be added.
     */
    public void addCorrLcMsData(String protId, String pepSeq, xLcMsCorr x) {
        xCorrespondenceData myCorr = this.getPeptideCorrespondence(protId, pepSeq);
        if (myCorr == null) {
            myCorr = new xCorrespondenceData(protId, pepSeq);
            myCorr.addLcMsCorr(x);
            peptideCorrespondenceData.add(myCorr);
        } else {
            myCorr.addLcMsCorr(x);
        }
    }

    /**
     * Gets the raw data file name.
     * @return the file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the raw data file name.
     * @param fileNM the file name.
     */
    public void setFileName(String fileNM) {
        fileName = fileNM;
    }

    /**
     * Checks the validity of the xCorrespondences data structure. The structure is valid if peptideCorrespondences is not empty and all its elements are valid in turn.
     * @return True if the structure is valid, false otherwise.
     */
    public boolean isValid() {
        boolean ret = true;
        boolean fileNMchk = false;
        boolean pepCorrChk = true;
        if (fileName.length() > 0) {
            fileNMchk = true;
        }
        for (int i = 0; i < getPeptideCorrespondenceDataSize() && pepCorrChk; i++) {
            xCorrespondenceData myDta = getPeptideCorrespondenceDataElemtAt(i);
            if (myDta.getPeptideSeq().length() == 0 || myDta.getProteinId().length() == 0 || !myDta.isValid()) {
                pepCorrChk = false;
            }
        }
        ret = fileNMchk && pepCorrChk;
        return ret;
    }

    /**
     * Gets the array of unique labels of the LC-MS correspondence data across all considered raw data files.
     * @return an array of unique strings representing the unique labels in the LC-MS correspondence data.
     */
    public String[] getUniqueLabelsOfLcMsCorr() {
        HashSet<String> tmp = new HashSet<String>();
        for (int i = 0; i < this.getPeptideCorrespondenceDataSize(); i++) {
            xCorrespondenceData myCorrData = getPeptideCorrespondenceDataElemtAt(i);
            for (int j = 0; j < myCorrData.getLcMsCorrSize(); j++) {
                xLcMsCorr myCorr = myCorrData.getLcMsCorrElemAt(j);
                tmp.add(myCorr.getLabel());
            }
        }
        String[] ret = new String[tmp.size()];
        tmp.toArray(ret);
        return ret;
    }

    /**
     * Gets the array of unique labels of the LC-MS/MS correspondence data across all considered raw data files.
     * @return an array of unique strings representing the unique labels in the LC-MS/MS correspondence data.
     */
    public String[] getUniqueLabelsOfLcMsMsCorr() {
        HashSet<String> tmp = new HashSet<String>();
        for (int i = 0; i < getPeptideCorrespondenceDataSize(); i++) {
            xCorrespondenceData myCorrData = getPeptideCorrespondenceDataElemtAt(i);
            for (int j = 0; j < myCorrData.getLcMsCorrSize(); j++) {
                xLcMsMsCorr myCorr = myCorrData.getLcMsMsCorrElemAt(j);
                tmp.add(myCorr.getLabel());
            }
        }
        String[] ret = new String[tmp.size()];
        tmp.toArray(ret);
        return ret;
    }
    /**
     * The fileName of the raw data the correspondences are related to.
     */
    private String fileName;
    /**
     * The set of peptide correspondences associated to this raw data file.
     */
    private List<xCorrespondenceData> peptideCorrespondenceData;
}
