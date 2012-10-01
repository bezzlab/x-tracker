package uk.ac.cranfield.xTracker.plugins.quantitation;

import Jama.Matrix;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.ModParam;
import uk.ac.cranfield.xTracker.data.Identification;
import uk.ac.cranfield.xTracker.data.MSRun;
import uk.ac.cranfield.xTracker.data.xPeptideConsensus;
import uk.ac.cranfield.xTracker.data.xFeature;
import uk.ac.cranfield.xTracker.data.xPeptide;
import uk.ac.cranfield.xTracker.data.xProtein;
import uk.ac.cranfield.xTracker.data.xSpectrum;
import uk.ac.cranfield.xTracker.data.xSpectrumList;
import uk.ac.cranfield.xTracker.utils.MS2QuantitationMethod;
import uk.ac.cranfield.xTracker.utils.XMLparser;
import uk.ac.cranfield.xTracker.xTracker;

/**
 *
 * @author Jun Fan@cranfield
 */
public class iTraqQuantitation extends quantitationPlugin{
    private String name = "iTraq N-plex Feature Detection and Quantitation plugin";
    private String version = "1.0";
    private String description = "It is a general plugin that can deal with a generic number N (4 or 8) of iTraq reporter ions\n\tspecified in the mzQuantML configuration file with their M/Z values contained in the parameter file(.xtp).";
    private HashMap<String,Double> ion_mz_map = new HashMap<String,Double>();
    private HashMap<Double,HashMap<Integer,Double>> factors = new HashMap<Double, HashMap<Integer, Double>>();
    private ArrayList<Double> ionMZs = new ArrayList<Double>();
    private ArrayList<Integer> ionMzIntegerPart = new ArrayList<Integer>();
    private ArrayList<Integer> deltas = new ArrayList<Integer>();
//    private HashMap<Double,String> mz_assayID_map;
//    private ArrayList<String> assayIDs;
    private double upperLimit;
    private double lowerLimit;
    private String integrationMethod;
    private final String ITRAQ_INTENSITY = "iTRAQ intensities";
    
    @Override
    public void start(String paramFile) {
        HashSet<String> quantitationNames = new HashSet<String> ();
        quantitationNames.add(ITRAQ_INTENSITY);
        System.out.println(getName()+" starts");
        loadParam(paramFile);
        checkParameterValues();
        setQuantitationNames();

        Matrix correction = computeCoeffMatrix();
        if(correction.det()==0){
            System.out.println("The quantities can not be corrected as the determinant calculated from supplied parameters is equal to 1");
        }
        HashMap<String,HashMap<Double,String>> mz_assayID_maps = new HashMap<String, HashMap<Double, String>>();
        HashMap<String,ArrayList<String>> allAssayID_map = new HashMap<String, ArrayList<String>>();
        ArrayList<String> allAssayID_list = new ArrayList<String>();
        for(MSRun msrun:xTracker.study.getMSRuns()){
            List<Assay> assays = msrun.getAssays();
            HashMap<Double,String> mz_assayID_map = new HashMap<Double, String>();
            ArrayList<String> assayIDs = new ArrayList<String>();
            for (Assay assay : assays) {
                String assayID = assay.getId();
                List<ModParam> mods = assay.getLabel().getModification();
                String str = null;
                for (ModParam mod : mods) {
                    if (ion_mz_map.containsKey(mod.getCvParam().getName())) {
                        str = mod.getCvParam().getName();
                        break;
                    }
                }
                if (str == null) {
                    System.out.println("Assay " + assayID + " can not be mapped to the plugin parameter file. Please make sure that the content of AssayName element in the plugin parameter file "+paramFile+" must map to the name in cvParam/Modification/Label/Assay in the mzQuantML configuration file");
                    System.exit(1);
                }
                mz_assayID_map.put(ion_mz_map.get(str), assayID);
                assayIDs.add(assayID);
                allAssayID_list.add(assayID);
            }
            mz_assayID_maps.put(msrun.getID(), mz_assayID_map);
            allAssayID_map.put(msrun.getID(), assayIDs);
        }
        
        for (xProtein protein : xTracker.study.getProteins()) {
            for (xPeptideConsensus pepCon : protein.getPeptides()) {
                for (xPeptide peptide : pepCon.getPeptides()) {
                    for (String msrunID : peptide.getMSRunIDs()) {
                        for (xFeature feature : peptide.getFeatures(msrunID)) {
                            for (Identification identification : feature.getIdentifications()) {
                                String rawfile = identification.getSpectraDataLocation();
                                xSpectrum spectrum = identification.getMs2spectrum();
                                double[] mzValues = spectrum.getMzData(rawfile);
                                double[] intenValues = spectrum.getIntensityData(rawfile);
                                double[][] quantities = new double[ionMZs.size()][1];
                                ArrayList<Integer> zeroLocation = new ArrayList<Integer>();
                                for (int i = 0; i < ionMZs.size(); i++) {
                                    double mz = ionMZs.get(i);
                                    double minValue = mz - lowerLimit;
                                    double maxValue = mz + upperLimit;
                                    xSpectrumList peaks = new xSpectrumList();
                                    for (int j = 0; j < mzValues.length; j++) {
                                        //as a peptide-identified spectrum, the maximum mz value must be > 122, which guarantees one mz > maxValue
                                        if (mzValues[j] > maxValue) {
                                            //if using trapzoid area method, needs add one extra data point at both side
                                            //which located at the limit m/z
                                            // same slope (h2-h1)/(mz2-mz1) = (hx-h1)/(maxValue-mz1)  h=>intensity 
                                            if(integrationMethod.equals("Area")){
                                                double heightMaxValue = (intenValues[j]-intenValues[j-1])/(mzValues[j]-mzValues[j-1])*(maxValue-mzValues[j-1])+intenValues[j-1];
                                                peaks.addElem(maxValue, heightMaxValue);
                                            }
                                            break;
                                        }
                                        if (mzValues[j] >= minValue) {
                                            if(integrationMethod.equals("Area")){
                                                if(j==0){//first element and mz value >=minValue
                                                    peaks.addElem(minValue, 0);
                                                }else if(mzValues[j-1]<minValue){ //j>0 guarantees j-1 is valid index; this mz at j is the first in the range
                                                    //(h2-h1)/(mz2-mz1) = (h2-hx)/(mz2-minValue)
                                                    double heightMinValue = intenValues[j]-(intenValues[j]-intenValues[j-1])/(mzValues[j]-mzValues[j-1])*(mzValues[j]-minValue);
                                                    peaks.addElem(minValue, heightMinValue);
                                                }
                                            }
                                            peaks.addElem(mzValues[j], intenValues[j]);
                                        }
                                    }
                                    if (peaks.getSize() == 0) {
                                        quantities[i][0] = 0;
                                        zeroLocation.add(i);
                                        continue;//no peak
                                    }
                                    if (integrationMethod.equals("SumIntensities")) {
                                        quantities[i][0] = MS2QuantitationMethod.sumIntensity(peaks);
                                    } else if (integrationMethod.equals("Area")) {
                                        quantities[i][0] = MS2QuantitationMethod.trapezoidArea(peaks);
                                    } else if (integrationMethod.equals("Highest")) {
                                        quantities[i][0] = MS2QuantitationMethod.highest(peaks);
                                    }
                                }

                                Matrix corrected;
                                if (correction.det() != 0) {
                                    corrected = correction.solve(new Matrix(quantities));
                                } else {
                                    corrected = new Matrix(quantities);
                                }
                                //zero before correction, then it should be zero again after correction
                                //this step can remove the negative value after the corrections
                                for(Integer i:zeroLocation){
                                    corrected.set(i, 0, 0);
                                }
                                for (int i = 0; i < ionMZs.size(); i++) {
                                    double mz = ionMZs.get(i);
                                    identification.addQuantity(ITRAQ_INTENSITY, mz_assayID_maps.get(msrunID).get(mz), corrected.get(i, 0));
                                }
                            }//end of identification
//                            if (xTracker.study.requireFeatureQuantitation()) {
//                                feature.calculateQuantitation(quantitationNames, allAssayID_map.get(msrunID),QuantitationLevel.SUM);
//                            }
                        }//end of feature
                    }
                    if (xTracker.study.requirePeptideQuantitation()) {
                        peptide.calculateQuantitation(quantitationNames, allAssayID_list,xTracker.PEPTIDE_FEATURE_INFERENCE);
                        if(xTracker.study.isRatioRequired()) peptide.calculateRatioDirectlyFromQuantities(quantitationNames);
                    }
                }//end of peptide
//                if (xTracker.study.requireProteinQuantitation()) {
//                    pepCon.calculateQuantitation(quantitationNames, allAssayID_list,QuantitationLevel.SUM);
//                    pepCon.calculateQuantitation(quantitationNames, allAssayID_list,xTracker.PROTEIN_PEPTIDE_INFERENCE);
//                    System.out.println("Peptide consensus");
//                }
            }//end of peptideConsensus
            if (xTracker.study.requireProteinQuantitation()) {
                protein.calculateQuantitation(quantitationNames, allAssayID_list,xTracker.PROTEIN_PEPTIDE_INFERENCE);
                if(xTracker.study.isRatioRequired()){
                    if(xTracker.study.isRatioInferFromPeptide()){
                        protein.calculateRatioFromPeptideRatios(quantitationNames);
                    }else{
                        protein.calculateRatioDirectlyFromQuantities(quantitationNames);
                    }
                }
                if(xTracker.study.getMzQuantML().getStudyVariableList().getStudyVariable().size()>1){
                    protein.calculateForSV();
                }
            }
        }
        System.out.println("iTraq quantitation plugin done");
    }
    
    @Override
    public void setQuantitationNames(){
        xTracker.study.addQuantitationName(ITRAQ_INTENSITY, "");
    }
    /**
     * Computes the coefficients matrix for purity correction.
     * @return ret the coeffiecients matrix.
     */
    private Matrix computeCoeffMatrix() {
        int numReportIons = ionMZs.size();
        Matrix ret = new Matrix(numReportIons, numReportIons, 0);
        //The diagonal of the matrix.
        for (int i = 0; i < numReportIons; i++) {
            double sum = 0;
            HashMap<Integer,Double> factor = factors.get(ionMZs.get(i));
            for(double d:factor.values()){
                sum += d;
            }
            ret.set(i, i, 100 - sum);
        }

        for (int i = 0; i < numReportIons; i++) {
            for (int j = 0; j < numReportIons; j++) {
                if (i != j) {//diagonal has been done
                    int delta = ionMzIntegerPart.get(i)-ionMzIntegerPart.get(j);
                    if(deltas.contains(delta)){//whether this delta range is in the parameter file
                        HashMap<Integer,Double> factor = factors.get(ionMZs.get(j));
                        ret.set(i, j, factor.get(delta));
                    }
                }
            }
        }
        System.out.println("The correction factor matrix is");
        ret.print(ret.getRowDimension(),ret.getColumnDimension());
        return ret;
    }

    private void checkParameterValues() {
        System.out.println(upperLimit);
        System.out.println(lowerLimit);
        System.out.println(integrationMethod);
//        System.out.println(ionMZs);
//        System.out.println(ionMzIntegerPart);
        for(String ion:ion_mz_map.keySet()){
            System.out.println("Ion name:"+ion);
            System.out.println("Mass value:"+ion_mz_map.get(ion));
            HashMap<Integer,Double> factor = factors.get(ion_mz_map.get(ion));
            System.out.println("With factor");
            for(Integer delta:deltas){
                System.out.println(delta+": "+factor.get(delta));
            }
            System.out.println("");
        }
    }

    private void loadParam(String paramFile){
        final String baseElement = "iTraqQuantitation";
        XMLparser parser = new XMLparser(paramFile);
        parser.validate(baseElement);
        System.out.println("Validation successful");
        NodeList assayParamList = parser.getElement("AssayParamList").getChildNodes();
        for (int i = 0; i < assayParamList.getLength(); i++) {
            Node item = assayParamList.item(i);
            if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals("AssayParam")){
                NodeList assayParamChildren = item.getChildNodes();
                String assayName = "";
                double mz = 0;
                HashMap<Integer,Double> oneFactors = new HashMap<Integer, Double>();
                for (int j = 0; j < assayParamChildren.getLength(); j++) {
                    Node assayParamChild = assayParamChildren.item(j);
                    if(assayParamChild.getNodeType() == Node.ELEMENT_NODE){
//                        System.out.println(assayParamChild.getNodeName());
                        if(assayParamChild.getNodeName().equals("AssayName")){
                            assayName = assayParamChild.getTextContent();
                        }else if(assayParamChild.getNodeName().equals("mzValue")){
                            mz = Double.parseDouble(assayParamChild.getTextContent());
                            ionMZs.add(mz);
                            int tmp = (int)Math.round(mz);
                            if(ionMzIntegerPart.contains(tmp)){
                                System.out.println("Please check the plugin parameter file: the mz values for more than one ion are too close to the value "+tmp);
                                System.exit(1);
                            }else{
                                ionMzIntegerPart.add(tmp);
                            }
                        }else if(assayParamChild.getNodeName().equals("CorrectionFactors")){
                            NodeList factorList = assayParamChild.getChildNodes();
                            for (int k = 0; k < factorList.getLength(); k++) {
                                Node factor = factorList.item(k);
                                if(factor.getNodeType() == Node.ELEMENT_NODE){
                                    String str = factor.getAttributes().getNamedItem("deltaMass").getTextContent();
                                    if(str.startsWith("+")) str = str.substring(1);
                                    int delta = Integer.parseInt(str);
                                    if(!deltas.contains(delta)) deltas.add(delta);
                                    double shift = Double.parseDouble(factor.getTextContent());
                                    oneFactors.put(delta, shift);
                                }
                            }
                        }
                    }
                }
                ion_mz_map.put(assayName, mz);
                factors.put(mz, oneFactors);
            }
        }
        Collections.sort(deltas);
        Collections.sort(ionMZs);
        Collections.sort(ionMzIntegerPart);
        NodeList settingList = parser.getElement("Setting").getChildNodes();
        for (int i = 0; i < settingList.getLength(); i++) {
            Node node = settingList.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                if(node.getNodeName().equals("mzRange")){
                    NodeList rangeList = node.getChildNodes();
                    for (int j = 0; j < rangeList.getLength(); j++) {
                        Node range = rangeList.item(j);
                        if(range.getNodeType()==Node.ELEMENT_NODE){
                            if(range.getNodeName().equals("minus")){
                                lowerLimit = Double.parseDouble(range.getTextContent());
                            }else if(range.getNodeName().equals("plus")){
                                upperLimit = Double.parseDouble(range.getTextContent());
                            }
                        }
                    }
                }else if(node.getNodeName().equals("IntegrationMethod")){
                    integrationMethod = node.getTextContent();
                }
            }
        }
    }

    @Override
    public boolean supportMS1(){
        return false;
    }

    @Override
    public boolean supportMS2(){
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
       return description;
    }
}
