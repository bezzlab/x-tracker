package xtracker.plugins.peakSelection;

import xtracker.data.xIdentData;
import xtracker.data.xLcMsCorr;
import xtracker.data.xLcMsMsCorr;
import xtracker.data.xLoad;
import xtracker.data.xLoadData;
import xtracker.data.xPeaks;
import xtracker.data.identInputData;
import xtracker.data.xCorrespondenceData;

/**
 * Plugin to select the peaks to compute quantifiation on. In this case it is spectral counting,
 * so there is no need of all the information, just the proteins and their unique parent ion peptides.
 * It reads the xLoad structure, but just the identification data part, and select the unique parent ion per protein.
 * To do that it detects the same or overlapping sequences, and checks if the mass to charge ratio is different.
 * In that case it is considered as a different peptide and stored in the xPeaks structure.
 * @author laurie Tonon for X-Tracker
 */
public class peakSelSpectralCount implements peakSelPlugin {

    /**
     * The start method. 
     * @param inputData the xLoad data structure.		   
     * @param paramFile a string (that might be empty) containing the file name of parameters if any.	
     * @return a valid xPeaks structure.
     */
    public xPeaks start(xLoad input, String paramFile) {
        xPeaks ret = new xPeaks();

        // selects all the raw data, in order to treat one filename at a time
        for (int i = 0; i < input.getDataSize(); i++) {

            xLoadData data = input.getDataElemAt(i); //selects data from one filename

            System.out.println("Starting peak selection on file " + data.getFileName() + " ...");

            for (int j = 0; j < data.getIdentificationDataSize(); j++) { // selects all the identifications

                identInputData identifications = data.getIdentInputDataElemAt(j);// one identification for this filename

                //create a new correspondenceData for the couple protID/peptSeq
                xCorrespondenceData correspondences = new xCorrespondenceData(identifications.getProteinId(), identifications.getPeptideSeq());


                int LcMsMsSize = identifications.getLcMsMsIdentSize();
                if (LcMsMsSize > 0) { // if MsMsIdentification, use this one, better for us

                    // searches for the first LcMsMs identification with modifications
                    int idMod = 0;
                    while ((idMod < LcMsMsSize) && (identifications.getLcMsMsElemAt(idMod).getModificationSize() == 0)) {
                        idMod++;
                    }
                    // if no modifications found, use the first LcMsMs identification
                    if (idMod >= LcMsMsSize) {
                        idMod = 0;
                    }
                    xIdentData identData = identifications.getLcMsMsElemAt(idMod);// gets the first MsMs identification with modifications, we just need one

                    if (identData.getConfidenceLevel() > confidence) { // we need a level of confidence suficient

                        // create a new MsMsCorrelation and add the info inside
                        xLcMsMsCorr MsMsCorrelation = new xLcMsMsCorr();

                        MsMsCorrelation.setLabel("unique peptide");
                        MsMsCorrelation.setRT(identData.getRetTime());
                        MsMsCorrelation.setCharge(identData.getCharge());
                        MsMsCorrelation.setParentIonMz(identData.getParentMass());
                        MsMsCorrelation.setMz(identData.getParentMass());
                        MsMsCorrelation.setIntensity(0);

                        // takes the modifications
                        for (int m = 0; m < identData.getModificationSize(); m++) {
                            MsMsCorrelation.addModification(identData.getModificationElemAt(m).getName(), identData.getModificationElemAt(m).getPosition());
                        }

                        if (this.UniqueParentIon(correspondences, MsMsCorrelation) == false) { // if a correpondence witht he same peptide and m/z value exists, we don't want it twice
                            correspondences.addLcMsMsCorr(MsMsCorrelation);

                        }
                    }

                } else { // if MsIdentification
                    int LcMsSize = identifications.getLcMsIdentSize();
                    if (LcMsSize > 0) {

                        // searches for the first LcMs identification with modifications
                        int idMod = 0;
                        while ((idMod < LcMsSize) && (identifications.getLcMsElemAt(idMod).getModificationSize() == 0)) {
                            idMod++;
                        }
                        // if no modifications found, use the first LcMsMs identification
                        if (idMod >= LcMsSize) {
                            idMod = 0;
                        }
                        // takes the first identification, we just want one
                        xIdentData identData = identifications.getLcMsElemAt(idMod);
                        if (identData.getConfidenceLevel() > confidence) {
                            // create a new MsMsCorrelation and add the info inside
                            xLcMsCorr MsCorrelation = new xLcMsCorr();

                            MsCorrelation.setLabel("unique peptide");
                            MsCorrelation.setRT(identData.getRetTime());
                            MsCorrelation.setMz(identData.getParentMass());
                            MsCorrelation.setIntensity(0);
                            // takes the modifications
                            for (int m = 0; m < identData.getModificationSize(); m++) {
                                MsCorrelation.addModification(identData.getModificationElemAt(m).getName(), identData.getModificationElemAt(m).getPosition());
                            }
                            correspondences.addLcMsCorr(MsCorrelation);
                        }


                    } else {
                        System.err.println("WARNING: no identification for peptide " + identifications.getPeptideSeq() + " in protein " + identifications.getProteinId());
                    }
                }

                if (overlappingSeq(ret, data.getFileName(), correspondences) == false) {
                    // add the correspondence of this peptide to the xPeaks structure
                    ret.addPeptideCorrespondence(data.getFileName(), correspondences);
                }
            } // end creation of correspondences for one peptide


        } // end review of all filenames

        System.out.println("Peak selection over");

        return ret;

    }

    /**
     * Method that checks if an MsMsCorrelation does not exist already inside a xCorrespondenceData structure.
     * The aim is to select a unique parent ion MsMs identification
     * @param data a xCorrespondenceData to compare
     * @param MsCorr a xLcMsMsCorr structure to verify
     * @return true if the same MsMsCorrelation exists inside the xCorrespondenceData, false either
     */
    public boolean UniqueParentIon(xCorrespondenceData data, xLcMsMsCorr MsCorr) {

        boolean contains = false;

        try {
            for (int i = 0; i < data.getLcMsMsCorrSize(); i++) {
                xLcMsMsCorr CorrTest = data.getLcMsMsCorrElemAt(i);

                if (CorrTest.getParentIonMz() == MsCorr.getParentIonMz()) {
                    contains = true;
                }
            }
        } catch (Exception e) {
            contains = false;
        }
        return contains;

    }

    /**
     * Method that checks if a MsMsCorrelation with an overlapping sequence does not exist already inside a xCorrespondenceData structure.
     * The aim is to select a unique parent ion MsMs identification
     * @param data a xCorrespondenceData to compare
     * @param filename a filename containing the correspondences to check
     * @param correspondences the correspondences to compare
     * @return true if the a similar MsMsCorrelation exists inside the xCorrespondenceData, false either
     */
    public boolean overlappingSeq(xPeaks data, String filename, xCorrespondenceData correspondences) {
        boolean contains = false;

        try {
            if (data.getSize() > 0) {
                for (int i = 0; i < data.getCorrOf(filename).getPeptideCorrespondenceDataSize(); i++) {

                    xCorrespondenceData CorrTest = data.getCorrOf(filename).getPeptideCorrespondenceDataElemtAt(i);
                    String peptseq = CorrTest.getPeptideSeq();

                    if (peptseq.contains(correspondences.getPeptideSeq()) || correspondences.getPeptideSeq().contains(peptseq)) {
                        if (CorrTest.getLcMsMsCorrSize() > 0 && correspondences.getLcMsMsCorrSize() > 0) {
                            for (int j = 0; j < CorrTest.getLcMsMsCorrSize(); j++) {

                                for (int z = 0; z < correspondences.getLcMsMsCorrSize(); z++) {

                                    if (CorrTest.getLcMsMsCorrElemAt(j).getParentIonMz() == correspondences.getLcMsMsCorrElemAt(z).getParentIonMz()) {

                                        contains = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            contains = false;
        }
        return contains;
    }

    /**
     * Method to retrieve the name of the plugin.
     * @return A string with the plugin name.	
     */
    public String getName() {
        return name;
    }

    /**
     * Method to retrieve the version of the plugin.
     * @return A string with the plugin version.	
     */
    public String getVersion() {
        return version;
    }

    /**
     * Method to retrieve the type of the plugin.
     * @return A string with the plugin type.	
     */
    public String getType() {
        return type;
    }

    /**
     * Method to retrieve the description of the plugin.
     * @return A string with the plugin description.	
     */
    public String getDescription() {
        return description;
    }
    /**
     * The name of your plugin.
     */
    private final static String name = "peakSelSpectralCount";
    /**
     * The version of the plugin.
     */
    private final static String version = "1.0";
    /**
     * The plugin type. For an PEAKSEL plugin it must be PEAKSEL_plugin (do not change it).
     */
//    private final static String type = "PEAKSEL_plugin";
    /**
     * The description of the plugin.
     */
    private final static String description = "Plugin to select the peaks to compute a spectral counting quantification method."
            + "\n For each peptide identified for each protein, it creates a correlation that can be used to count the number of peptides per protein."
            + "\n Careful: In case of MsMs identification, if a peptide with same sequence but different parent ion mz is found, it is added just once, "
            + "which means that the peptide sequence will have two correlation instead of one.";
    private final static int confidence = 15;
}
