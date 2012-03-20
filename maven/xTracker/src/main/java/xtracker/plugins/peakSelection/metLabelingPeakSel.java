package xtracker.plugins.peakSelection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import xtracker.data.xIdentData;
import xtracker.data.xLcMsCorr;
import xtracker.data.xLcMsData;
import xtracker.data.xLoad;
import xtracker.data.xLoadData;
import xtracker.data.xModification;
import xtracker.data.xPeaks;
import xtracker.data.identInputData;
import xtracker.data.xCorrespondenceData;

public class metLabelingPeakSel implements peakSelPlugin {

    /**
     * Loads aminoacid mass shifts as well as n-terminus and c-terminus ones. After this it reads in MS/MS identification data loaded into xLoad structure.
     * For each peptide identified it retrieves the parent Ion m/z, the Retention Time, the sequence and charge and computes the mass shift of the whole peptide. Finally it tries
     * to match the parent Ion m/z +/- the displacement in a MS peak within a specified Retention Time window. If the peak is at "Parent Ion m/z + mass shift" it labels the new peak as "heavy" and the original
     * one as light and viceversa.
     * It finally populates the xPeaks structure.
     * @param input the xLoad structure
     * @return xPeaks the peaks selected for quantitation.
     */
    public xPeaks start(xLoad input, String paramFile) {
        // As MetabolicLabeling but now, given the monoisotopic peak of a peptide (at MS level),
        // we quantify considering:
        // for the light version: the 4 next isotopic peaks (i.e. 4*proton mass/ charge)
        // for the heavy version: the 4 next isotopic peaks and the 2 previous ones (i.e. range: -2*proton mass/charge ... 4*proton mass/ charge

        int i;
        xPeaks ret = new xPeaks();

        int totalAdded = 0;
        int addedCounter = 0;
        
        System.out.println("Loading " + getName() + " plugin ...");

        //First open the .xml file and retrieve mass shifts.
        massShifts = loadParams(paramFile);

        if (!(mass_type.toLowerCase().equals("average") || mass_type.toLowerCase().equals("monoisotopic"))) {
            System.out.println("ERROR (metLabeling.jar): mass type " + mass_type + " not recognised!");
            System.exit(1);
        }

        //Let's process any single file
        for (i = 0; i < input.getDataSize(); i++) {
            identInputData myMsMsVals;
            xLoadData myLoadData = input.getDataElemAt(i);
            //The raw data file name they are associated to.
            String fileNM = myLoadData.getFileName();
            System.out.println("Processing data from " + fileNM + "...");
            //Let's process any identification of the file

            while (myLoadData.getIdentificationDataSize() > 0) {
                //Let's get the identification we will work on now
                myMsMsVals = myLoadData.getIdentInputDataElemAt(0);
                //Its peptide Sequence and protein Id.
                String pepSeq = myMsMsVals.getPeptideSeq();
                String pepId = myMsMsVals.getProteinId();

                //Let's create the data structure to to add to the return structure.
                xCorrespondenceData myXCorrData = new xCorrespondenceData(pepId, pepSeq);

                //How many identifications do we have for this peptideSequence/peptideId?
                int howMany = myLoadData.countIdentofPeptide(pepSeq, pepId);
                int outOf = myLoadData.getAllIdentOfPeptideSeq(pepSeq).size();

                // We check if the peptide sequence is unique or not (i.e. same peptide sequence cannot appear associated
                // to different peptide ids).
                if (howMany == outOf) {
                    //OK the peptide is unique go on.

                    //Let's retrieve the mass shift of the peptide identified
                    //Remember that it has to be divided by the charge!!!!
                    float shift = computePeptideMassShift(pepSeq);
                    float pepThMass = computeMass(pepSeq);

                    //Let's retrieve all the the identifications of this peptide
                    ArrayList<identInputData> myIdent = myLoadData.getAllIdentOfPeptideSeq(pepSeq);

                    //Let's initialise some vectors we will use in a sec.
                    ArrayList<String> heavyPeps = new ArrayList<String>();
                    ArrayList<String> lightPeps = new ArrayList<String>();

                    //Let's loop through all the identifications
                    for (int myCnt = 0; myCnt < myIdent.size(); myCnt++) {
                        identInputData myDoubIdentData = myIdent.get(myCnt);

                        int idSize = myDoubIdentData.getLcMsMsIdentSize();

                        //Let's go through each LcMsMsIdentification to work out if it is
                        //associated to the heavy or light version of a peptide (or if it unknown, in that case we will
                        //skip the peptide.
                        for (int k = 0; k < idSize; k++) {
                            xIdentData myXidentData = myDoubIdentData.getLcMsMsElemAt(k);

                            //Let's retrieve some important fields of the identification
                            float pIMz = myXidentData.getParentMass();
                            float tModShift = myXidentData.getTotalModificationMassShift();
                            int iChg = myXidentData.getCharge();

                            //Is it a light version?
                            if (Math.abs((pepThMass + tModShift) - pIMz * iChg) <= 3) {
                                //Yes, it's light, add the references to the vector of light
                                //identified peptides
                                lightPeps.add(myCnt + ":" + k);
                            } else {
                                //No, we have to check if it is heavy, if not we will skip the peptide.
                                if (Math.abs((pepThMass + tModShift + shift) - pIMz * iChg) <= 3) {
                                    //Yes, it's heavy, add the references to the vector of heavy
                                    //identified peptides
                                    heavyPeps.add(myCnt + ":" + k);
                                } else {
                                    //Peptide is not heavy version, let's notify the user.
                                    System.out.println("WARNING: Peptide " + pepSeq + " (" + pepId + ") cannot be associated to a type. Skipped!");
                                }
                            }
                        }
                    }

                    //OK all the identification of peptide pepSeq, pepId have been processed and now it'time to
                    //compute their peak correspondences.
                    int lightSize = lightPeps.size();
                    int heavySize = heavyPeps.size();
                    System.out.println("LightSize:" + lightSize + " HeavySize:" + heavySize);
                    if (lightSize > 0 && heavySize > 0) {
                        //OK The same peptide has been identified by the search engine in both heavy and light versions.

                        //To be fair with every peptide we will use the same amount of distinct identifications
                        //for each peptide (i.e. the minimum between heavy identifications and light ones.
                        int toConsider = Math.min(lightSize, heavySize);
                        //Let's first compute peak correspondences of the light version.
                        System.out.println("Light and Heavy version of " + pepSeq + ":");

                        for (int li = 0; li < toConsider; li++) {
                            String indexes[] = new String[2];
                            indexes = lightPeps.get(li).split(":");
                            identInputData myDoubIdentData = myIdent.get(Integer.valueOf(indexes[0]).intValue());
                            xIdentData myXIdntDt = myDoubIdentData.getLcMsMsElemAt(Integer.valueOf(indexes[1]).intValue());
                            System.out.println("- Light mz:" + myXIdntDt.getParentMass() + " (at rt:" + myXIdntDt.getRetTime() + ") charge:+" + myXIdntDt.getCharge());

                            //Ok everything is ready to compute the peak-correspondences for hte light version of the peptide
                            computePeakCorrespondences("Light", pepSeq, pepId, myXIdntDt.getParentMass(), myXIdntDt.getCharge(), myXIdntDt.getRetTime(), myXCorrData, myLoadData, myXIdntDt.getAllModifications());

                            //Now it's time to compute peak correspondences of the heavy version.
                            System.out.println("Heavy version of " + pepSeq + ":");

                            for (int h = 0; h < toConsider; h++) {
                                //String indexes[] = new String[2];
                                indexes = heavyPeps.get(h).split(":");
                                myDoubIdentData = myIdent.get(Integer.valueOf(indexes[0]).intValue());
                                myXIdntDt = myDoubIdentData.getLcMsMsElemAt(Integer.valueOf(indexes[1]).intValue());
                                System.out.println("- Heavy mz:" + myXIdntDt.getParentMass() + " (at rt:" + myXIdntDt.getRetTime() + ") charge:+" + myXIdntDt.getCharge());

                                //Ok everything is ready to compute the peak-correspondences for hte heavy version of the peptide
                                computePeakCorrespondences("Heavy", pepSeq, pepId, myXIdntDt.getParentMass(), myXIdntDt.getCharge(), myXIdntDt.getRetTime(), myXCorrData, myLoadData, myXIdntDt.getAllModifications());
                            }
                        }
                    } else {
                        //oops, just one of the two (heavy or light) versions have been identified... which one is it?
                        if (lightSize > 0) {
                            //OK the identified peptide whas light, let's compute peak correspondences.
                            System.out.println("Just Light version of " + pepSeq + ":");

                            for (int li = 0; li < lightPeps.size(); li++) {
                                String indexes[] = new String[2];
                                indexes = lightPeps.get(li).split(":");
                                identInputData myDoubIdentData = myIdent.get(Integer.valueOf(indexes[0]).intValue());
                                xIdentData myXIdntDt = myDoubIdentData.getLcMsMsElemAt(Integer.valueOf(indexes[1]).intValue());
                                System.out.println("- Light mz:" + myXIdntDt.getParentMass() + " (at rt:" + +myXIdntDt.getRetTime() + ") charge:+" + myXIdntDt.getCharge());

                                //Ok everything is ready to compute the peak-correspondences for hte light version of the peptide
                                computePeakCorrespondences("Light", pepSeq, pepId, myXIdntDt.getParentMass(), myXIdntDt.getCharge(), myXIdntDt.getRetTime(), myXCorrData, myLoadData, myXIdntDt.getAllModifications());

                                //Now the associated heavy peaks:
                                System.out.println("- Heavy exp mz:" + (myXIdntDt.getParentMass() + shift / myXIdntDt.getCharge()) + " (at rt:" + +myXIdntDt.getRetTime() + ") charge:+" + myXIdntDt.getCharge());

                                computePeakCorrespondences("Heavy", pepSeq, pepId, myXIdntDt.getParentMass() + (shift / myXIdntDt.getCharge()), myXIdntDt.getCharge(), myXIdntDt.getRetTime(), myXCorrData, myLoadData, myXIdntDt.getAllModifications());
                            }
                        } else {
                            //OK the identified peptide whas heavy, let's compute peak correspondences.
                            System.out.println("Just Heavy version of " + pepSeq + ":");
                            for (int h = 0; h < heavyPeps.size(); h++) {
                                String indexes[] = new String[2];
                                indexes = heavyPeps.get(h).split(":");
                                identInputData myDoubIdentData = myIdent.get(Integer.valueOf(indexes[0]).intValue());
                                xIdentData myXIdntDt = myDoubIdentData.getLcMsMsElemAt(Integer.valueOf(indexes[1]).intValue());
                                System.out.println("- Heavy exp mz:" + myXIdntDt.getParentMass() + " (at rt:" + myXIdntDt.getRetTime() + ") charge:+" + myXIdntDt.getCharge());

                                Vector<Float> rtH = new Vector<Float>();
                                Vector<Float> intH = new Vector<Float>();
                                List<xModification> mods = myXIdntDt.getAllModifications();

                                //Ok everything is ready to compute the peak-correspondences for the heavy version of the peptide
                                computePeakCorrespondences("Heavy", pepSeq, pepId, myXIdntDt.getParentMass(), myXIdntDt.getCharge(), myXIdntDt.getRetTime(), myXCorrData, myLoadData, myXIdntDt.getAllModifications());

                                //Now the associated light peaks:
                                computePeakCorrespondences("Light", pepSeq, pepId, myXIdntDt.getParentMass() - (shift / myXIdntDt.getCharge()), myXIdntDt.getCharge(), myXIdntDt.getRetTime(), myXCorrData, myLoadData, myXIdntDt.getAllModifications());
                            }
                        }
                    }
                } else {
                    //Nope, the peptide is not unique.
                    System.out.println("WARNING: Peptide " + pepSeq + " is not unique and will be skipped!");
                }

                //Let's remove all the identifications of the peptide from the data structure.
                myLoadData.deleteIdentOfPeptideSeq(pepSeq);
                //Let's add the correspondences to the output data structure
                if (myXCorrData.isValid()) {
                    ret.addPeptideCorrespondence(fileNM, myXCorrData);
                }
            }
        }
        return ret;
    }

    public void computePeakCorrespondences(String type, String pepSeq, String pepId, float pMz, int pepCharge, float rt, xCorrespondenceData peaksCorr, xLoadData data, List<xModification> mods) {
        float lowerIsoBound = 0.0f;
        float upperIsoBound = 0.0f;

        int noiseMzThresh = 100;
        int percentile = 30;

        float noiseEstIntens[];
        float noiseLevel = 0.0f;

        Vector<Float> XICrt = new Vector<Float>();
        Vector<Float> XICint = new Vector<Float>();
        Vector<Float> XICintN = new Vector<Float>();
        Vector<Float> XICrtN = new Vector<Float>();
        float isotopeDiff = 1.003f; //This is the difference between a C12 and a C13 (N15-N14 should be considered as well, but it is close -- 0.9971 -- therefore it's not considered)

        if (type.equals("Light")) {

            lowerIsoBound = pMz - mzTolerance; //- (isotopeDiff/pepCharge);
            upperIsoBound = pMz + mzTolerance + 1 * (isotopeDiff / pepCharge);
//            lowerIsoBoundM = pMz-mzTolerance - 2*(isotopeDiff/pepCharge);
//            upperIsoBoundM = pMz+mzTolerance + 4*(isotopeDiff/pepCharge);
        } else {
//            lowerIsoBoundM = pMz-mzTolerance; //- (isotopeDiff/pepCharge);
//            upperIsoBoundM = pMz+mzTolerance+ 4*(isotopeDiff/pepCharge);
            lowerIsoBound = pMz - mzTolerance - 2 * (isotopeDiff / pepCharge);
            upperIsoBound = pMz + mzTolerance + 2 * (isotopeDiff / pepCharge);
        }
        System.out.println(type);
        System.out.println("RT range: " + (rt - retTimeWindow) + "-" + (rt + retTimeWindow));
        System.out.println("MZ range: " + lowerIsoBound + "-" + upperIsoBound);

        // int[] candPeaksIndexes=data.getLcMsIndexOfAllBetweenRT(rt-retTimeWindow, rt+retTimeWindow);
        int[] candPeaksIndexes = new int[2];
        candPeaksIndexes[0] = 0;
        candPeaksIndexes[1] = data.getLcMsDataSize() - 1;

        for (int j = candPeaksIndexes[0]; j <= candPeaksIndexes[1]; j++) {
            xLcMsData vals = data.getLcMsDataElemAt(j);
            float retTime = vals.getRetTime();
            //ok let's find the LcMs peaks
//             float elements[][]=vals.getSpectrum().getSubspectrumBetween(lowerIsoBound, upperIsoBound);
            float elements[][] = vals.getSpectrum().getSubspectrumBetween(pMz - noiseMzThresh, pMz + noiseMzThresh);
            int elSize = -1;
            if (elements != null) {
                elSize = elements.length;
            } else {
                elSize = -1;
            }

            noiseEstIntens = new float[elSize];
            for (int k = 0; k < elSize; k++) {
                //OK getting the peaks to compute the background noise level as the 30th percentile (better the percentile-th)
                // System.out.println("Adding ("+pepSeq+ "):" +retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                noiseEstIntens[k] = elements[k][1];
            }
            noiseLevel = 0;
            if (elSize > 1) {
                Arrays.sort(noiseEstIntens);
                //Let's compute the percentile-th percentile

                int indx = (int) Math.ceil(Float.valueOf((elSize * percentile) / 100 + 0.5f).doubleValue());

                noiseLevel = noiseEstIntens[indx];
            }

            //OK Noise was estimated. Let's retrieve only spectra within the peptide bounds to
            //compute the denoised XIC.
            elements = vals.getSpectrum().getSubspectrumBetween(lowerIsoBound, upperIsoBound);
            elSize = -1;
            if (elements != null) {
                elSize = elements.length;
            } else {
                elSize = -1;
            }

            float xicVal = 0.0f;
            float xicValN = 0.0f;
            ///REMOVE THIS!!! It's just to output raw data
            noiseLevel = 0;

            for (int k = 0; k < elSize; k++) {
                //OK getting the peaks to compute the background noise level as the 30th percentile (better the percentile-th)
                // System.out.println("Adding ("+pepSeq+ "):" +retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                xicVal += elements[k][1] - noiseLevel;
                xicValN += elements[k][1];
            }

            //Let's store the denoised xic val for this retention time point
            if (xicVal > 0) {
                XICrt.addElement(retTime);
                XICint.addElement(xicVal);

                //outFile.print(retTime + " " + xicVal +";");
            }

            XICrtN.addElement(retTime);
            XICintN.addElement(xicValN);
            /*      xLcMsCorr myCorr= new xLcMsCorr(elements[k][0],elements[k][1],type,retTime);
            
            for(int kk=0;kk<mods.size();kk++){
            xModification myMod=mods.elementAt(kk);
            String modName=myMod.getName();
            int position=myMod.getPosition();
            myCorr.addModification(modName, position);
            }
            peaksCorr.addLcMsCorr(myCorr);
             */

        }

        //       System.out.println("Original XIC for mass range [" +lowerIsoBound + "," + upperIsoBound+"]");
        //       for(int i=0;i<XICintN.size();i++){
        //          System.out.println(XICrtN.elementAt(i) + " " + XICintN.elementAt(i));

        //       }
        //      System.exit(1);

        //    System.out.println("Denoised XIC for mass range [" +lowerIsoBound + "," + upperIsoBound+"]");
        //       for(int i=0;i<XICint.size();i++){
        //      System.out.println(XICrt.elementAt(i) + " " + XICint.elementAt(i));

        //       }

        //UNCOMMENT WHAT FOLLOWS TO compute the smoothing!!!!
        //  XICint=this.smooth(XICint);

        System.out.println("Smoothed XIC for mass range [" + lowerIsoBound + "," + upperIsoBound + "]");
        for (int i = 0; i < XICint.size(); i++) {
            System.out.println(XICrt.elementAt(i) + " " + XICint.elementAt(i));

        }

        float[] retTimePoints = this.getStartEndRT(XICrt, XICint, rt);

        System.out.println("\n\nRT:" + retTimePoints[0] + " -->" + retTimePoints[1]);

        int indLow = Collections.binarySearch(XICrt, retTimePoints[0]);
        if (indLow < 0) {
            indLow = 0 - indLow - 1;
        }
        int indHigh = Collections.binarySearch(XICrt, retTimePoints[1]);
        if (indHigh < 0) {
            indHigh = 0 - indHigh - 1;
        }
        if (indLow < 0) {
            indLow = 0;
        }
        if (indHigh >= XICrt.size()) {
            indHigh = XICrt.size() - 1;
        }



        // System.out.println("Low ind: " + indLow + " High ind:" + indHigh);

        for (int j = indLow; j <= indHigh; j++) {
            xLcMsCorr myCorr = new xLcMsCorr(j, XICint.elementAt(j), type, XICrt.elementAt(j));

            for (int kk = 0; kk < mods.size(); kk++) {
                xModification myMod = mods.get(kk);
                String modName = myMod.getName();
                int position = myMod.getPosition();
                myCorr.addModification(modName, position);
            }
            // System.out.println("Added peak: " + myCorr.getLabel() + " " + myCorr.getRT() + " " + myCorr.getMz());
            peaksCorr.addLcMsCorr(myCorr);

        }

        //OLD CODE! Needs to recompute XIC at quantification time!

        /*        candPeaksIndexes=data.getLcMsIndexOfAllBetweenRT(retTimePoints[0], retTimePoints[1]);
        for(int j=candPeaksIndexes[0];j<=candPeaksIndexes[1];j++){
        xLcMsData vals=data.getLcMsDataElemAt(j);
        float retTime=vals.getRetTime();
        //ok let's find the LcMs peaks
        float elements[][]=vals.getSpectrum().getSubspectrumBetween(lowerIsoBound, upperIsoBound);
        int elSize=-1;
        if(! (elements==null)){
        elSize=elements.length;
        }
        else{
        elSize=-1;
        }
        for(int k=0;k<elSize;k++){
        //Denoise the peak intensity, if >0 keep it otherwise forget about it
        //  elements[k][1]=elements[k][1];
        //   if(elements[k][1]>0){
        xLcMsCorr myCorr= new xLcMsCorr(elements[k][0],elements[k][1],type,retTime);
        
        for(int kk=0;kk<mods.size();kk++){
        xModification myMod=mods.elementAt(kk);
        String modName=myMod.getName();
        int position=myMod.getPosition();
        myCorr.addModification(modName, position);
        }
        // System.out.println("Added peak: " + myCorr.getLabel() + " " + myCorr.getRT() + " " + myCorr.getMz());
        peaksCorr.addLcMsCorr(myCorr);
        
        //    }
        }
        
        } */

    }

    public float[] computePeakCorrespondencesNew(String type, float pMz, int pepCharge, float rt, xLoadData data, Vector<Float> rtVals, Vector<Float> intVals) {

        float[] retTimePoints = new float[2];
        float lowerIsoBound = 0.0f;
        float upperIsoBound = 0.0f;

        int noiseMzThresh = 100;
        int percentile = 30;

        float noiseEstIntens[];
        float noiseLevel = 0.0f;

        Vector<Float> XICrt = new Vector<Float>();
        Vector<Float> XICint = new Vector<Float>();
        Vector<Float> XICintN = new Vector<Float>();
        Vector<Float> XICrtN = new Vector<Float>();
        float isotopeDiff = 1.003f; //This is the difference between a C12 and a C13 (N15-N14 should be considered as well, but it is close -- 0.9971 -- therefore it's not considered)

        if (type.equals("Light")) {

            lowerIsoBound = pMz - mzTolerance; //- (isotopeDiff/pepCharge);
            upperIsoBound = pMz + mzTolerance + 1 * (isotopeDiff / pepCharge);
//            lowerIsoBoundM = pMz-mzTolerance - 2*(isotopeDiff/pepCharge);
//            upperIsoBoundM = pMz+mzTolerance + 4*(isotopeDiff/pepCharge);

        } else {

//            lowerIsoBoundM = pMz-mzTolerance; //- (isotopeDiff/pepCharge);
//            upperIsoBoundM = pMz+mzTolerance+ 4*(isotopeDiff/pepCharge);
            lowerIsoBound = pMz - mzTolerance - 2 * (isotopeDiff / pepCharge);
            upperIsoBound = pMz + mzTolerance + 2 * (isotopeDiff / pepCharge);
        }
        System.out.println(type);
        System.out.println("RT range: " + (rt - retTimeWindow) + "-" + (rt + retTimeWindow));
        System.out.println("MZ range: " + lowerIsoBound + "-" + upperIsoBound);

        // int[] candPeaksIndexes=data.getLcMsIndexOfAllBetweenRT(rt-retTimeWindow, rt+retTimeWindow);
        int[] candPeaksIndexes = new int[2];
        candPeaksIndexes[0] = 0;
        candPeaksIndexes[1] = data.getLcMsDataSize() - 1;

        for (int j = candPeaksIndexes[0]; j <= candPeaksIndexes[1]; j++) {
            xLcMsData vals = data.getLcMsDataElemAt(j);
            float retTime = vals.getRetTime();
            //ok let's find the LcMs peaks
//             float elements[][]=vals.getSpectrum().getSubspectrumBetween(lowerIsoBound, upperIsoBound);
            float elements[][] = vals.getSpectrum().getSubspectrumBetween(pMz - noiseMzThresh, pMz + noiseMzThresh);
            int elSize = -1;
            if (elements != null) {
                elSize = elements.length;
            } else {
                elSize = -1;
            }

            noiseEstIntens = new float[elSize];
            for (int k = 0; k < elSize; k++) {
                //OK getting the peaks to compute the background noise level as the 30th percentile (better the percentile-th)
                // System.out.println("Adding ("+pepSeq+ "):" +retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                noiseEstIntens[k] = elements[k][1];
            }
            noiseLevel = 0;
            if (elSize > 1) {
                Arrays.sort(noiseEstIntens);
                //Let's compute the percentile-th percentile

                int indx = (int) Math.ceil(Float.valueOf((elSize * percentile) / 100 + 0.5f).doubleValue());

                noiseLevel = noiseEstIntens[indx];

            }


            //OK Noise was estimated. Let's retrieve only spectra within the peptide bounds to
            //compute the denoised XIC.
            elements = vals.getSpectrum().getSubspectrumBetween(lowerIsoBound, upperIsoBound);
            elSize = -1;
            if (elements != null) {
                elSize = elements.length;
            } else {
                elSize = -1;
            }

            float xicVal = 0.0f;
            float xicValN = 0.0f;
            for (int k = 0; k < elSize; k++) {
                //OK getting the peaks to compute the background noise level as the 30th percentile (better the percentile-th)
                // System.out.println("Adding ("+pepSeq+ "):" +retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                xicVal += elements[k][1] - noiseLevel;
                xicValN += elements[k][1];
            }

            //Let's store the denoised xic val for this retention time point
            if (xicVal > 0) {
                XICrt.addElement(retTime);
                XICint.addElement(xicVal);
            }

            XICrtN.addElement(retTime);
            XICintN.addElement(xicValN);
            /*      xLcMsCorr myCorr= new xLcMsCorr(elements[k][0],elements[k][1],type,retTime);
            
            for(int kk=0;kk<mods.size();kk++){
            xModification myMod=mods.elementAt(kk);
            String modName=myMod.getName();
            int position=myMod.getPosition();
            myCorr.addModification(modName, position);
            }
            peaksCorr.addLcMsCorr(myCorr);
             */

        }

        //       System.out.println("Original XIC for mass range [" +lowerIsoBound + "," + upperIsoBound+"]");
        //       for(int i=0;i<XICintN.size();i++){
        //          System.out.println(XICrtN.elementAt(i) + " " + XICintN.elementAt(i));

        //       }
        //      System.exit(1);

        //    System.out.println("Denoised XIC for mass range [" +lowerIsoBound + "," + upperIsoBound+"]");
        //       for(int i=0;i<XICint.size();i++){
        //      System.out.println(XICrt.elementAt(i) + " " + XICint.elementAt(i));

        //       }

        XICint = this.smooth(XICint);

        System.out.println("Smoothed XIC for mass range [" + lowerIsoBound + "," + upperIsoBound + "]");
        for (int i = 0; i < XICint.size(); i++) {
            System.out.println(XICrt.elementAt(i) + " " + XICint.elementAt(i));
            rtVals.addElement(XICrt.elementAt(i));
            intVals.addElement(XICint.elementAt(i));

        }

        retTimePoints = this.getStartEndRT(XICrt, XICint, rt);

        System.out.println("\n\nRT:" + retTimePoints[0] + " -->" + retTimePoints[1]);



        //THIS CODE HAS TO BE PUT OUTSIDE THE METHOD
    /*
        int indLow=Collections.binarySearch(XICrt, retTimePoints[0]);
        if(indLow<0){
        indLow=0-indLow-1;
        }
        int indHigh=Collections.binarySearch(XICrt, retTimePoints[1]);
        if(indHigh<0){
        indHigh=0-indHigh-1;
        }
        if(indLow<0){
        indLow=0;
        }
        if(indHigh>=XICrt.size()){
        indHigh=XICrt.size()-1;
        }
        
        
        for(int j=indLow;j<=indHigh; j++){
        xLcMsCorr myCorr= new xLcMsCorr(j,XICint.elementAt(j),type,XICrt.elementAt(j));
        
        for(int kk=0;kk<mods.size();kk++){
        xModification myMod=mods.elementAt(kk);
        String modName=myMod.getName();
        int position=myMod.getPosition();
        myCorr.addModification(modName, position);
        }
        // System.out.println("Added peak: " + myCorr.getLabel() + " " + myCorr.getRT() + " " + myCorr.getMz());
        peaksCorr.addLcMsCorr(myCorr);
        
        }
         */



        return retTimePoints;

    }

    /**
     * Performs smoothing with a seven points triangular function of the vector in input
     * @param vect the input vector
     * @return smoothed vector
     */
    public Vector smooth(Vector<Float> vect) {
        Vector<Float> ret = new Vector<Float>();
        int size = vect.size();
        for (int i = 0; i < size; i++) {
            if (i < 3 || i > size - 4) {
                ret.add(vect.elementAt(i));
            } else {
                float val = vect.elementAt(i - 3) + 2 * vect.elementAt(i - 2) + 3 * vect.elementAt(i - 1) + 4 * vect.elementAt(i) + 3 * vect.elementAt(i + 1) + 2 * vect.elementAt(i + 2) + vect.elementAt(i + 3);
                ret.add(val / 16);
            }
        }

        return ret;
    }

    /**
     * Returns the initial and ending retention times associated to this peak. Uses the FWHMM method (full width half maximum).
     * @param rtV vector or retention times
     * @param intV vector of intensities
     * @param retTime measured retention time for the identified peptide
     * @return the initial and ending RT values of the peak
     */
    //TODO: bug existing apparently in this method, out of bounds  Vector.elementAt
    //called by computePeakCorrespondences, start
    public float[] getStartEndRT(Vector<Float> rtV, Vector<Float> intV, float retTime) {
        float[] ret = new float[2];
        if(rtV.isEmpty()){
            return ret;
        }
        //Let's first compute the maximum of the intensity close to the retention time value in input
        //(the boundaries are given by this.retTimeWindow
        int lowerInd = Collections.binarySearch(rtV, retTime - retTimeWindow);
        if (lowerInd < 0) {
            //TODO: why -2, not -1
            lowerInd = 0 - lowerInd - 2;
        }
        int upperInd = Collections.binarySearch(rtV, retTime + retTimeWindow);
        if (upperInd < 0) {
            upperInd = 0 - upperInd - 1;
        }

        //If all RT has to be considered lowerInd can be negative and upperInd can be > size of vector, let's fix this.
        if (lowerInd < 0) {
            lowerInd = 0;
        }
        if (upperInd >= rtV.size()) {
            upperInd = rtV.size() - 1;
        }

        // tmpRT=rtV.subList(lowerInd, upperInd);
        float maxVal = -1f;
        int maxInd = -1;

        for (int i = lowerInd; i <= upperInd; i++) {
            if (maxVal < intV.elementAt(i)) {
                maxVal = intV.elementAt(i);
                maxInd = i;
            }
        }
        System.out.println("DEBUG: lowerInd "+lowerInd+" upperInd "+upperInd+" vector size "+rtV.size()+" "+intV.size());
        //Let's find the leftmost value higher than half maxVal
        int L = maxInd;
        int R = maxInd;
        while (L >= lowerInd && intV.elementAt(L) >= (maxVal / 2)) {
            L--;
        }
        while (R <= upperInd && intV.elementAt(R) >= (maxVal / 2)) {
            R++;
        }
        System.out.println("L: "+L+ " R: "+R);
        //Error correction (i.e. make sure we are within array boundaries)
        if (L < 0) {
            L = 0;
        }
        if (R >= rtV.size()) {
            R = rtV.size() - 1;
        }
        System.out.println("After adjustment L: "+L+ " R: "+R);

        float fwhmCenter = (rtV.elementAt(L) + rtV.elementAt(R)) / 2;

        ret[0] = fwhmCenter - (rtV.elementAt(R) - rtV.elementAt(L));
        ret[1] = fwhmCenter + (rtV.elementAt(R) - rtV.elementAt(L));
        return ret;
    }

    /**
     * Opens the dataFile xml file and loads mass shifts,the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     * @return ret an array of float mass shifts.
     */
    public float[] loadParams(String dataFile) {
        //mass shifts.
        //the order is the following:
        /*
         * massShifts[0] <-- C-terminus (daltons)
         * massShifts[1] <-- N-terminus (daltons)
         * massShifts[2] <-- A  (daltons)
         * massShifts[3] <-- R (daltons)
         * massShifts[4] <-- N (daltons)
         * massShifts[5] <-- D (daltons)
         * massShifts[6] <-- C (daltons)
         * massShifts[7] <-- E (daltons)
         * massShifts[8] <-- Q (daltons)
         * massShifts[9] <-- G (daltons)
         * massShifts[10] <-- H (daltons)
         * massShifts[11] <-- I (daltons)
         * massShifts[12] <-- L (daltons)
         * massShifts[13] <-- K (daltons)
         * massShifts[14] <-- M (daltons)
         * massShifts[15] <-- F (daltons)
         * massShifts[16] <-- P (daltons)
         * massShifts[17] <-- S (daltons)
         * massShifts[18] <-- T (daltons)
         * massShifts[19] <-- W (daltons)
         * massShifts[20] <-- Y (daltons)
         * massShifts[21] <-- V (daltons)
         */

        int i = 0;
        for (i = 0; i < massShifts.length; i++) {
            massShifts[i] = 0;
        }

        int index = 2;
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            // create a SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            doc.getDocumentElement().normalize();

            Node nodeLst = doc.getElementsByTagName("param").item(0);

            String schemaLocation = "";

            if (nodeLst.getAttributes().getNamedItem("xsi:schemaLocation") != null) {
                schemaLocation = nodeLst.getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();
            } else {
                if (nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation") != null) {
                    schemaLocation = nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation").getTextContent();
                } else {
                    System.out.println("ERROR: No .xsd schema is provided for " + dataFile);
                    System.exit(1);
                }
            }

            // load the xtracker WXS schema
            Source schemaFile = new StreamSource(new File(schemaLocation));
            Schema schema = factory.newSchema(schemaFile);

            // create a Validator instance
            Validator validator = schema.newValidator();

            try {
                validator.validate(new DOMSource(doc));
            } catch (SAXException e) {
                // instance document is invalid!
                System.out.println("\n\nERRROR - could not validate the input file " + dataFile + "!");
                System.out.print(e);
                System.exit(1);
            }


            nodeLst = doc.getElementsByTagName("param").item(0);

            NodeList itemList = nodeLst.getChildNodes();
            for (i = 0; i < itemList.getLength(); i++) {
                Node item = itemList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    if (item.getNodeName().equals("C_term")) {
                        massShifts[0] = Float.valueOf(item.getTextContent());
                    } else if (item.getNodeName().equals("N_term")) {
                        massShifts[1] = Float.valueOf(item.getTextContent());
                    } else if (item.getNodeName().equals("mass_type")) {
                        mass_type = item.getTextContent();
                    } else if (item.getNodeName().equals("incorporation_rate")) {
                        incorpRate = Float.valueOf(item.getTextContent());
                        System.out.println("INC RATE:" + incorpRate);
                    } else if (item.getNodeName().equals("aminoacid")) {
                        //Should already be a character but...
                        switch (item.getAttributes().item(0).getTextContent().charAt(0)) {
                            case 'A': {
                                index = 2;
                                break;
                            }
                            case 'R': {
                                index = 3;
                                break;
                            }
                            case 'N': {
                                index = 4;
                                break;
                            }
                            case 'D': {
                                index = 5;
                                break;
                            }
                            case 'C': {
                                index = 6;
                                break;
                            }
                            case 'E': {
                                index = 7;
                                break;
                            }
                            case 'Q': {
                                index = 8;
                                break;
                            }
                            case 'G': {
                                index = 9;
                                break;
                            }
                            case 'H': {
                                index = 10;
                                break;
                            }
                            case 'I': {
                                index = 11;
                                break;
                            }
                            case 'L': {
                                index = 12;
                                break;
                            }
                            case 'K': {
                                index = 13;
                                break;
                            }
                            case 'M': {
                                index = 14;
                                break;
                            }
                            case 'F': {
                                index = 15;
                                break;
                            }
                            case 'P': {
                                index = 16;
                                break;
                            }
                            case 'S': {
                                index = 17;
                                break;
                            }
                            case 'T': {
                                index = 18;
                                break;
                            }
                            case 'W': {
                                index = 19;
                                break;
                            }

                            case 'Y': {
                                index = 20;
                                break;
                            }
                            case 'V': {
                                index = 21;
                                break;
                            }
                            default: {
                                System.err.println("ERROR: aminoacid (" + item.getAttributes().item(0).getTextContent().charAt(0) + ") in " + dataFile + " not recognized!");
                                System.exit(1);
                            }
                        }
                        massShifts[index] = Float.valueOf(item.getTextContent());
                        index++;
                    } else if (item.getNodeName().equals("RT_window")) {
                        retTimeWindow = Float.valueOf(item.getTextContent());
                    } else if (item.getNodeName().equals("mz_tolerance")) {
                        mzTolerance = Float.valueOf(item.getTextContent());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while reading " + dataFile + "\n" + e);
            System.exit(1);
        }
        return massShifts;
    }

    /**
     * Given the peptide sequence and a vector of mass shifts
     * @param pepSeq
     * @return the mass shift relative to the peptide according to mass shifts contained
     * in <code>massShifts</code>
     */
    public float computePeptideMassShift(String pepSeq) {
        float ret = 0f;
        int i = 0;
        int index = 0;
//        int charge=1;
        char aminoacid;

        for (i = 0; i < pepSeq.length(); i++) {
            aminoacid = pepSeq.charAt(i);
            switch (aminoacid) {
                case 'A': {
                    index = 2;
                    break;
                }
                case 'R': {
                    index = 3;
                    //H,R,K increase are each one + charge
                    //           charge++;
                    break;
                }
                case 'N': {
                    index = 4;
                    break;
                }
                case 'D': {
                    index = 5;
                    break;
                }
                case 'C': {
                    index = 6;
                    break;
                }
                case 'E': {
                    index = 7;
                    break;
                }
                case 'Q': {
                    index = 8;
                    break;
                }
                case 'G': {
                    index = 9;
                    break;
                }
                case 'H': {
                    index = 10;
                    //H,R,K increase are each one + charge
                    //             charge++;

                    break;
                }
                case 'I': {
                    index = 11;
                    break;
                }
                case 'L': {
                    index = 12;
                    break;
                }
                case 'K': {
                    index = 13;
                    //H,R,K increase are each one + charge
                    //               charge++;

                    break;
                }
                case 'M': {
                    index = 14;
                    break;
                }
                case 'F': {
                    index = 15;
                    break;
                }
                case 'P': {
                    index = 16;
                    break;
                }
                case 'S': {
                    index = 17;
                    break;
                }
                case 'T': {
                    index = 18;
                    break;
                }
                case 'W': {
                    index = 19;
                    break;
                }

                case 'Y': {
                    index = 20;
                    break;
                }
                case 'V': {
                    index = 21;
                    break;
                }
                default: {
                    System.err.println("ERROR (computePeptideMassShift): aminoacid (" + aminoacid + ")  not recognized!");
                    System.exit(1);
                }
            }
            ret += massShifts[index];
        }
        //Let's take into account incorporation rate:
        ret = ret * incorpRate;
        //I have to add C-Terminus and N-terminus shifts if any!
        ret = ret + massShifts[0] + massShifts[1];
//        System.out.println("Sequence: "+pepSeq+" shift: "+ ret + " charge:" +charge);
//        return ret/charge;
        return ret;
    }

    public Vector[] computeCorrespondences(float rt, float pMz, xLoadData data, float shift) {
        Vector[] res = new Vector[3];
        res[0] = new Vector<String>(); //candidate Heavy peaks (format RT:mz:int)
        res[1] = new Vector<String>(); //candidate Light peaks (format RT:mz:int)
        res[2] = new Vector<String>(); //envelope of the original peaks (format RT:mz:int)

        xLcMsData myLcMsData;
//        float[] vals = new float[3];

        for (int i = 0; i < data.getLcMsDataSize(); i++) {
            myLcMsData = data.getLcMsDataElemAt(i);
//                System.out.println("I:" +i);
            //  System.out.println("Data size:" + myLcMsData.getSize());


            int[] candidatePeaks = data.getLcMsIndexOfAllBetweenRT(rt - retTimeWindow, rt + retTimeWindow);
            for (int j = candidatePeaks[0]; j < candidatePeaks[1]; j++) {
                xLcMsData vals = data.getLcMsDataElemAt(j);
                float retTime = vals.getRetTime();
                float elements[][] = vals.getSpectrum().getSubspectrumBetween(pMz + shift - mzTolerance, pMz + shift + mzTolerance);
                int elSize = -1;
                if (elements != null) {
                    elSize = elements.length;
                } else {
                    elSize = -1;
                }
                for (int k = 0; k < elSize; k++) {
                    //OK peak can be possibly assigned as a Heavy peak
                    res[0].add(retTime + ":" + elements[k][0] + ":" + elements[k][1]);

                }

                elements = vals.getSpectrum().getSubspectrumBetween(pMz - shift - mzTolerance, pMz - shift + mzTolerance);

                if (elements != null) {
                    elSize = elements.length;
                } else {
                    elSize = -1;
                }

                for (int k = 0; k < elSize; k++) {
                    //OK peak can be possibly assigned as a Light peak
                    res[1].add(retTime + ":" + elements[k][0] + ":" + elements[k][1]);

                }

                elements = vals.getSpectrum().getSubspectrumBetween(pMz - mzTolerance, pMz + mzTolerance);

                if (elements != null) {
                    elSize = elements.length;
                } else {
                    elSize = -1;
                }

                for (int k = 0; k < elSize; k++) {
                    //OK peak can be possibly assigned as the original peak
                    res[2].add(retTime + ":" + elements[k][0] + ":" + elements[k][1]);

                }


                /*                           if((vals[1] >= pMz+ shift - mzTolerance ) && (vals[1] <= pMz + shift + mzTolerance )){
                //OK peak can be possibly assigned as a Heavy peak
                res[0].add(i + ":" + j);
                }
                else if((vals[1] >= pMz- shift - mzTolerance ) && (vals[1] <= pMz - shift + mzTolerance )){
                //OK peak can be possibly assigned as a Heavy peak
                res[1].add(i + ":" + j);
                }
                
                if((vals[1]>= pMz -mzTolerance)&&(vals[1]<= pMz +mzTolerance)){
                //Peak can be assigned to the original envelope (even if at this stage we are not sure if it is
                //Heavy or light.
                res[2].add(i + ":" + j);
                }
                 */
            }

            /*                for(int j=0; j<myLcMsData.getSize();j++){
            vals=myLcMsData.getElemAt(j);
            //  System.out.println("J:" +j);
            if((vals[0]>= rt - retTimeWindow) && (vals[0]<= rt + retTimeWindow)){
            if((vals[1] >= pMz+ shift - mzTolerance ) && (vals[1] <= pMz + shift + mzTolerance )){
            //OK peak can be possibly assigned as a Heavy peak
            res[0].add(i + ":" + j);
            }
            else if((vals[1] >= pMz- shift - mzTolerance ) && (vals[1] <= pMz - shift + mzTolerance )){
            //OK peak can be possibly assigned as a Heavy peak
            res[1].add(i + ":" + j);
            }
            
            }
            } */

        }

        return res;

    }

    public float computeMass(String mySeq) {

        //Let's take into account the water molecule and the H+ while computing the peptide mass.
        float ret = 18.0153f;
        ret = ret + 1.00794f;
//     float ret=0;
        int i = 0;


        /*
        #######
        # MASSES:  monoisotopic,average
        #######
        #	'G' => 57.021464, 57.0519
        #	'A' => 71.037114, 71.0788
        #	'S' => 87.032029, 87.0782
        #	'P' => 97.052764, 97.1167
        #	'V' => 99.068414, 99.1326
        #	'T' => 101.04768, 101.1051
        #	'C' => 103.00919, 103.1388
        #	'L' => 113.08406, 113.1594
        #	'I' => 113.08406, 113.1594
        #	'N' => 114.04293, 114.1038
        #	'D' => 115.02694, 115.0886
        #	'Q' => 128.05858, 128.1307
        #	'K' => 128.09496, 128.1741
        #	'E' => 129.04259, 129.1155
        #	'M' => 131.04048, 131.1926
        #	'H' => 137.05891, 137.1411
        #	'F' => 147.06841, 147.1766
        #	'R' => 156.10111, 156.1875
        #	'Y' => 163.06333, 163.1760
        #	'W' => 186.07931, 186.2132
        #	);
        
         */



        boolean average = mass_type.toLowerCase().equals("average");
        //   System.out.println("String:" + mySeq);
        for (i = 0; i < mySeq.length(); i++) {
            switch (mySeq.toUpperCase().charAt(i)) {
                case 'G': {
                    if (average) {
                        ret += 57.0519;
                    } else {
                        ret += 57.021464;
                    }
                    break;
                }
                case 'A': {
                    if (average) {
                        ret += 71.0788;
                    } else {
                        ret += 71.037114;
                    }
                    break;
                }
                case 'S': {
                    if (average) {
                        ret += 87.0782;
                    } else {
                        ret += 87.032029;
                    }
                    break;
                }
                case 'P': {
                    if (average) {
                        ret += 97.1167;
                    } else {
                        ret += 97.052764;
                    }
                    break;
                }
                case 'V': {
                    if (average) {
                        ret += 99.1326;
                    } else {
                        ret += 99.068414;
                    }
                    break;
                }
                case 'T': {
                    if (average) {
                        ret += 101.1051;
                    } else {
                        ret += 101.04768;
                    }
                    break;
                }
                case 'C': {
                    if (average) {
                        ret += 103.1388;
                    } else {
                        ret += 103.00919;
                    }
                    break;

                }
                case 'L': {
                    if (average) {
                        ret += 113.1594;
                    } else {
                        ret += 113.08406;
                    }
                    break;
                }
                case 'I': {
                    if (average) {
                        ret += 113.1594;
                    } else {
                        ret += 113.08406;
                    }
                    break;
                }
                case 'N': {
                    if (average) {
                        ret += 114.1038;
                    } else {
                        ret += 114.04293;
                    }

                    break;
                }
                case 'D': {
                    if (average) {
                        ret += 115.0886;
                    } else {
                        ret += 115.02694;
                    }

                    break;
                }
                case 'Q': {
                    if (average) {
                        ret += 128.1307;
                    } else {
                        ret += 128.05858;
                    }

                    break;
                }
                case 'K': {
                    if (average) {
                        ret += 128.17412;
                    } else {
                        ret += 128.09496;
                    }

                    break;
                }
                case 'E': {

                    if (average) {
                        ret += 129.1155;
                    } else {
                        ret += 129.04259;
                    }

                    break;
                }
                case 'M': {
                    if (average) {
                        ret += 131.1926;
                    } else {
                        ret += 131.04049;
                    }

                    break;
                }
                case 'H': {
                    if (average) {
                        ret += 137.1411;
                    } else {
                        ret += 137.05891;
                    }

                    break;
                }
                case 'F': {
                    if (average) {
                        ret += 147.1766;
                    } else {
                        ret += 147.06841;
                    }

                    break;
                }
                case 'R': {
                    if (average) {
                        ret += 156.1875;
                    } else {
                        ret += 156.10111;
                    }

                    break;
                }
                case 'Y': {
                    if (average) {
                        ret += 163.1760;
                    } else {
                        ret += 163.06333;
                    }

                    break;
                }
                case 'W': {
                    if (average) {
                        ret += 186.2132;
                    } else {
                        ret += 186.07931;
                    }

                    break;
                }
            }
        }
        return ret;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {

        return type;
    }

    public String getDescription() {

        return description;
    }

    /**
     * Computes the peptide type (i.e. "Heavy" or "Light" given the sequence and the parent ion Mass.
     * @param pepSeq the peptide sequence.
     * @param parIonMz the parent ion m/z
     * @param shift the shift between Light and Heavy version of the peptide.
     * @param modMassShift is the mass shift derived by modifications
     * @return ret a string with the type of the peptide.
     */
    public String computePeptideType(String pepSeq, float parIonMz, float shift, int charge, float modMassShift) {
        String ret = "";
        float thMass = computeMass(pepSeq) + modMassShift;
        float parMass = parIonMz * charge;

        System.out.println("Computing type on: " + pepSeq + " (+" + charge + ") " + parIonMz + " " + shift + " th. mass:" + thMass);

//            if(((thMass - parMass)<=2) && ((thMass - parMass)>=-2)){
        if ((Math.abs(thMass - parMass) <= 3)) {
            ret = "Light";
        } else {
//                if((thMass + shift*charge - parMass)<2 && ((thMass + shift*charge - parMass)>-2)){
            if (Math.abs(thMass + shift * charge - parMass) <= 3) {
                ret = "Heavy";
            } else {
                System.out.println("Error (metlabeling.jar) in computePeptideType: " + pepSeq + " cannot be associated to any known type.");
                System.exit(1);
            }
        }


        return ret;

    }

    public Vector[] computePeakCorrespondences(float rt, float pMz, xLoadData data, float shift, String type, int pepCharge) {




        float lowerIsoBound = 0.0f;
        float upperIsoBound = 0.0f;

        float lowerIsoBoundM = 0.0f;
        float upperIsoBoundM = 0.0f;

        float isotopeDiff = 1.003f; //This is the difference between a C12 and a C13 (N15-N14 should be considered as well, but it is close -- 0.9971 -- therefore it's not considered)

        if (type.equals("Light")) {

            lowerIsoBound = pMz - mzTolerance; //- (isotopeDiff/pepCharge);
            upperIsoBound = pMz + mzTolerance + 4 * (isotopeDiff / pepCharge);
            lowerIsoBoundM = pMz - mzTolerance - 2 * (isotopeDiff / pepCharge);
            upperIsoBoundM = pMz + mzTolerance + 4 * (isotopeDiff / pepCharge);

        } else {

            lowerIsoBoundM = pMz - mzTolerance; //- (isotopeDiff/pepCharge);
            upperIsoBoundM = pMz + mzTolerance + 4 * (isotopeDiff / pepCharge);
            lowerIsoBound = pMz - mzTolerance - 2 * (isotopeDiff / pepCharge);
            upperIsoBound = pMz + mzTolerance + 4 * (isotopeDiff / pepCharge);
        }

        Vector[] res = new Vector[2];
        res[0] = new Vector<String>(); //envelope of original peaks (format RT:mz:int)
        res[1] = new Vector<String>(); //candidate matching peaks (format RT:mz:int)

        xLcMsData myLcMsData;
//        float[] vals = new float[3];

        //      System.out.println("Ret time (original): " + (rt-retTimeWindow) + " - " + (rt+retTimeWindow) + " Mass: " + lowerIsoBound + " - " + upperIsoBound);
        //      System.out.println("Ret time (matching): " + (rt-retTimeWindow) + " - " + (rt+retTimeWindow) + " Mass: " + lowerIsoBoundM + " - " + upperIsoBoundM);
        //    for(int i=0;i<data.getLcMsDataSize();i++){
        //            myLcMsData=data.getLcMsDataElemAt(i);
//                System.out.println("I:" +i);
        //  System.out.println("Data size:" + myLcMsData.getSize());



        int[] candidatePeaks = data.getLcMsIndexOfAllBetweenRT(rt - retTimeWindow, rt + retTimeWindow);
        for (int j = candidatePeaks[0]; j <= candidatePeaks[1]; j++) {
            xLcMsData vals = data.getLcMsDataElemAt(j);
            float retTime = vals.getRetTime();
            //ok let's find the original peaks
            float elements[][] = vals.getSpectrum().getSubspectrumBetween(lowerIsoBound, upperIsoBound);
            int elSize = -1;
            if (elements != null) {
                elSize = elements.length;
            } else {
                elSize = -1;
            }
            for (int k = 0; k < elSize; k++) {
                //OK adding the original peak
                //        System.out.println("Original Values(rt,mz,int): " +retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                res[0].add(retTime + ":" + elements[k][0] + ":" + elements[k][1]);

            }


            //Let's compute the matching peaks now.
            elements = vals.getSpectrum().getSubspectrumBetween(lowerIsoBoundM, upperIsoBoundM);
            elSize = -1;
            if (elements != null) {
                elSize = elements.length;
            } else {
                elSize = -1;
            }

            for (int k = 0; k < elSize; k++) {
                //OK adding matching peak
                //        System.out.println("Matching Values(rt,mz,int): " +retTime + ":" + elements[k][0] +":"+ elements[k][1]);
                res[1].add(retTime + ":" + elements[k][0] + ":" + elements[k][1]);

            }


        }

        /*                for(int j=0; j<myLcMsData.getSize();j++){
        vals=myLcMsData.getElemAt(j);
        //  System.out.println("J:" +j);
        if((vals[0]>= rt - retTimeWindow) && (vals[0]<= rt + retTimeWindow)){
        if((vals[1] >= pMz+ shift - mzTolerance ) && (vals[1] <= pMz + shift + mzTolerance )){
        //OK peak can be possibly assigned as a Heavy peak
        res[0].add(i + ":" + j);
        }
        else if((vals[1] >= pMz- shift - mzTolerance ) && (vals[1] <= pMz - shift + mzTolerance )){
        //OK peak can be possibly assigned as a Heavy peak
        res[1].add(i + ":" + j);
        }
        
        }
        } */

        //   }


        return res;

    }
    private float[] massShifts = new float[22];
    private float retTimeWindow = 0f;
    private float mzTolerance = 0f;
    private float incorpRate = 1.0f; //if not present then 100% is considered
    private String mass_type = "";
    private final static String name = "Metabolic Labeling";
    private final static String version = "1.01";
//    private final static String type = "PEAKSEL_plugin";
    private final static String description = "Works from MS/MS data loaded into xLoad structure.\n\tFor each peptide identified it retrieves y5 and b6 ions and considers y ions as Labelled Heavy, while b ions as Labelled Light.\n\tIt finally populates the xPeaks structure.";
}