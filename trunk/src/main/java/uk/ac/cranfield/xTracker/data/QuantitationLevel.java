package uk.ac.cranfield.xTracker.data;

import java.util.HashMap;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.cranfield.xTracker.Utils;
import uk.ac.cranfield.xTracker.utils.GenericInferenceMethod;
import uk.ac.cranfield.xTracker.xTracker;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.StudyVariable;
/**
 *
 * @author Jun Fan@cranfield
 */
public class QuantitationLevel {
    private HashMap<String,HashMap<String,Double>> quantities = new HashMap<String, HashMap<String, Double>>();
    private HashMap<String,HashMap<String,Double>> studyVariables = new HashMap<String, HashMap<String, Double>>();
    private HashMap<String,HashMap<String,Double>> ratios = new HashMap<String, HashMap<String, Double>>();
    static private QuantitationMisc misc = new QuantitationMisc();
    private static IntensityWeights intensityWeights = new IntensityWeights();

    public static final int SUM = 0;
    public static final int MEAN = 1;
    public static final int MEDIAN = 2;
    public static final int WEIGHTED_AVERAGE = 3;
    public static final int INTENSITY_WEIGHTED_AVERAGE = 4;

    public void addQuantity(String name,String assayID,Double quantity){
        HashMap<String,Double> quan;
        if(quantities.containsKey(name)){
            quan = quantities.get(name);
        }else{
            quan = new HashMap<String, Double>();
            quantities.put(name, quan);
        }
        quan.put(assayID, quantity);
    }
/**
 * Get quantity for the given quantitation name and assay ID
 * @param name the quantitation name, e.g. reporter ion intensity
 * @param assayID the assay id
 * @return quantity
 */
    public Double getQuantity(String name,String assayID){
        if (quantities.containsKey(name) && quantities.get(name).containsKey(assayID)){
            return quantities.get(name).get(assayID);
        }
        return null;
    }

    public HashMap<String, Double> getQuantities(String name){
        return quantities.get(name);
    }

    public HashMap<String, Double> getRatios(String name){
        return ratios.get(name);
    }

    public HashMap<String, Double> getStudyVariableQuantities(String name){
        return studyVariables.get(name);
    }

    public Double getStudyVariableQuantity(String name, String sv){
        if (studyVariables.containsKey(name)&& studyVariables.get(name).containsKey(sv)){
            return studyVariables.get(name).get(sv);
        }
        return null;
    }

    public void calculateForSV(){
        //calculate quantitation for SV
        for (String name:quantities.keySet()){
            HashMap<String,Double> values = new HashMap<String, Double>();
            for(StudyVariable sv:xTracker.study.getMzQuantML().getStudyVariableList().getStudyVariable()){
                ArrayList<Double> assayValues = new ArrayList<Double>();
                for(Object obj:sv.getAssayRefs()){
                    assayValues.add(quantities.get(name).get(((Assay)obj).getId()));
                }
                Double result = null;

                switch (xTracker.SV_ASSAY_INFERENCE) {
                    case MEAN:
                    case WEIGHTED_AVERAGE:
                        result = Utils.mean(assayValues);
                        break;
                    case MEDIAN:
                        result = Utils.median(assayValues);
                        break;
                    case SUM:
                        result = Utils.sum(assayValues);
                        break;
                    default:
                        System.out.println("Unrecognized calculation method for SV calculation");
                        System.exit(1);
                }
                values.put(sv.getId(), result);
            }
            studyVariables.put(name, values);
        }
        //calculate ratio for SV
        for (String name : quantities.keySet()) {
            HashMap <String,Double> ratioValues = ratios.get(name);
            if (ratioValues == null) ratioValues = new HashMap<String, Double>();
            for (xRatio ratio : xTracker.study.getRatios()) {
                if(ratio.getType().equals(xRatio.ASSAY)) continue;
            //ratio for SV
                HashMap <String,Double> svValues = studyVariables.get(name);
                Double numerator = svValues.get(ratio.getNumerator());
                Double denominator = svValues.get(ratio.getDenominator());
                Double ratioValue;
                if (numerator == null || denominator == null
                        || numerator.isNaN() || denominator.isNaN()
                        || numerator.isInfinite() || denominator.isInfinite()){
                    ratioValue = Double.NaN;
                }else if (denominator == 0) {
                    ratioValue = Double.POSITIVE_INFINITY;
                } else {
                    ratioValue = numerator / denominator;
                }
                ratioValues.put(ratio.getId(), ratioValue);
            }
            ratios.put(name, ratioValues);
        }
    }

    public void setQuantities(String name,HashMap<String, Double> quantities) {
        this.quantities.put(name, quantities);
    }

    public void setRatios(String name,HashMap<String, Double> ratios) {
        this.ratios.put(name, ratios);
    }

    public void calculateQuantitation(Set<String> quantitationNames,ArrayList<String> assayIDs,int type){
        try {
            //get the method for the current object according to its class type (e.g. xProtein or xPeptide)
            Method m = this.getClass().getDeclaredMethod(misc.getMethodName(this.getClass()), new Class[0]);
            ArrayList<QuantitationLevel> results = (ArrayList<QuantitationLevel>)m.invoke(this, new Object[0]);
            ArrayList<Integer> count = new ArrayList<Integer>();
            for(String name:quantitationNames){
                //the hash map is the quantitation map for the lower level entity (assay ID=> quan) for the current quantitation name
                ArrayList<HashMap<String, Double>> lowerLevelQuant = new ArrayList<HashMap<String, Double>>();
                for(QuantitationLevel one: results){
                    if(type==WEIGHTED_AVERAGE) count.add(one.getCount());
                    lowerLevelQuant.add(one.getQuantities(name));
                }
                HashMap<String,Double> quan = null;
                switch(type){
                    case SUM:
                        quan = GenericInferenceMethod.sum(lowerLevelQuant, assayIDs);
                        break;
                    case MEAN:
                        quan = GenericInferenceMethod.mean(lowerLevelQuant, assayIDs);
                        break;
                    case MEDIAN:
                        quan = GenericInferenceMethod.median(lowerLevelQuant, assayIDs);
                        break;
                    case WEIGHTED_AVERAGE:
                        quan = GenericInferenceMethod.weightedAverage(lowerLevelQuant, assayIDs, count);
                        break;
                    default:
                        System.out.println("Unrecognized quantitation method, exit");
                        System.exit(1);
                }
                this.setQuantities(name, quan);
            }
        } catch (IllegalAccessException ex) {
            Logger.getLogger(QuantitationLevel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(QuantitationLevel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(QuantitationLevel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(QuantitationLevel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(QuantitationLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getCount(){
        return 1;
    }

    public void calculateRatioDirectlyFromQuantities(HashSet<String> quantitationNames){
        for (String quantitationName : quantitationNames) {
            HashMap<String, Double> ratioValues = new HashMap<String, Double>();
            for (xRatio ratio : xTracker.study.getRatios()) {
                if (ratio.getType().equals(xRatio.STUDY_VARIABLE)) continue;
                Double numerator = getQuantity(quantitationName, ratio.getNumerator());
                Double denominator = getQuantity(quantitationName, ratio.getDenominator());
                double ratioValue;
                if (numerator ==null || denominator == null
                        || numerator.isNaN() || denominator.isNaN()
                        || numerator.isInfinite() || denominator.isInfinite()){
                    ratioValue = Double.NaN;
                } else if (denominator == 0) {
                    ratioValue = Double.POSITIVE_INFINITY;
                } else {
                    ratioValue = numerator / denominator;
                }
                ratioValues.put(ratio.getId(), ratioValue);
            }
            setRatios(quantitationName, ratioValues);
        }
    }

    /**
     * Wrapper method providing backwards compatibility for code written
     * when different types of ratio inference weren't supported.
     */
    public void calculateRatioFromPeptideRatios(HashSet<String> quantitationNames){
        calculateRatioFromPeptideRatios(quantitationNames, xTracker.PROTEIN_PEPTIDE_RATIO_INFERENCE);
    }

    public void calculateRatioFromPeptideRatios(HashSet<String> quantitationNames, int type){
        if(this instanceof xProtein){
            xProtein pro = (xProtein)this;
            ArrayList<xPeptide> peptides = pro.getAllPeptides();
            ArrayList<String> ratioIDs = new ArrayList<String>();
            for (xRatio ratio : xTracker.study.getRatios()) {
                if (ratio.getType().equals(xRatio.ASSAY)) ratioIDs.add(ratio.getId());
            }
            for (String quantitationName : quantitationNames) {

                ArrayList<HashMap<String, Double>> ratioValues = new ArrayList<HashMap<String, Double>>();
                for(xPeptide peptide:peptides){
                    ratioValues.add(peptide.getRatios(quantitationName));
                }

                HashMap<String,Double> proRatios;
                switch(type){
                    case SUM:
                        proRatios = GenericInferenceMethod.sum(ratioValues, ratioIDs);
                        setRatios(quantitationName, proRatios);
                        break;
                    case MEAN:
                        proRatios = GenericInferenceMethod.mean(ratioValues, ratioIDs);
                        setRatios(quantitationName, proRatios);
                        break;
                    case MEDIAN:
                        proRatios = GenericInferenceMethod.median(ratioValues, ratioIDs);
                        setRatios(quantitationName, proRatios);
                        break;
                    case INTENSITY_WEIGHTED_AVERAGE: // This method is only valid for reporter ion intensity
                        // Calculating intensity weights, only needs to be done once
                        if (!intensityWeights.isCalculated()) intensityWeights.calculateWeights();
                        ArrayList<Double> weights = intensityWeights.getPeptideWeights(peptides);
                        proRatios = GenericInferenceMethod.intensityWeightedAverage(ratioValues, ratioIDs, weights);
                        setRatios(quantitationName, proRatios);
                        break;
                    default:
                        System.out.println("Unrecognized ratio calculation method, exit");
                        System.exit(1);
                }
            }
        }
    }
}

class QuantitationMisc{
    private HashMap<Class,String> methods = new HashMap<Class, String>();

    public QuantitationMisc() {
//        methods.put(xProtein.class, "getPeptides");
        methods.put(xProtein.class, "getAllPeptides");
//        methods.put(xPeptideConsensus.class, "getPeptides");
        methods.put(xPeptide.class, "getAllFeatures");
        methods.put(xFeature.class, "getIdentifications");
    }

    String getMethodName(Class clazz){
        if(clazz == xPeptide.class && xTracker.study.getPipelineType() == Study.MS2_TYPE) return "getAllIdentifications";
        return methods.get(clazz);
    }
}

/*
 * Handles intensity-based peptide ratio to protein ratio inference
 *
 */
class IntensityWeights {
    private ArrayList<WeightBin> weightBins = new ArrayList<WeightBin>();
    private boolean calculated = false;

    public void calculateWeights() {
        // To be made into parameters, currently changed by hand for testing
        int binNo = 8;
        String duplicate1 = "_114";
        String duplicate2 = "_115";
        double expectedRatio = 1;

        weightBins = new ArrayList<WeightBin>();

        // Getting a map of peptide variances, sorted by reference intensity
        TreeMap<Double, Double> variances = new TreeMap<Double, Double>();
        for (xProtein protein : xTracker.study.getProteins()) {
            for (xPeptideConsensus pepCon : protein.getPeptides()) {
                for (xPeptide peptide : pepCon.getPeptides()) {
                    HashMap<String, Double> intensities = peptide.getQuantities("reporter ion intensity");
                    double log2ref = Utils.log2(getReferenceIntensity(intensities.values()));
                    double log2ratio = Utils.log2(intensities.get(duplicate1)/intensities.get(duplicate2));
                     // Technical duplicate
                    double log2variance = Math.pow((Utils.log2(expectedRatio)-log2ratio), 2);

                    variances.put(log2ref, log2variance);
                }
            }
        }

        ArrayList<Double> log2intensities = new ArrayList<Double>(variances.keySet());
        ArrayList<Double> log2variances = new ArrayList<Double>(variances.values());

        // Creating bins
        if (binNo>log2intensities.size()) binNo = log2intensities.size();
        int binSize = (int) Math.ceil((double)log2intensities.size()/binNo);
        for (int i=0; i<binNo; i++) {
            int startpoint = binSize*i;
            int endpoint = binSize*i+binSize;
            if (endpoint>log2intensities.size()) endpoint = log2intensities.size();

            // The bin weight is 1 divided by the median variance
            double binWeight = 1.0/Utils.median(log2variances.subList(startpoint, endpoint));

            weightBins.add(new WeightBin(log2intensities.get(endpoint-1), binWeight));
        }

        // Normalising weights (highest = 100%)
        double maxWeight = 0;
        for (WeightBin bin : weightBins) {
            if (bin.getWeight() > maxWeight) {
                maxWeight = bin.getWeight();
            }
        }
        for (WeightBin bin : weightBins) {
            bin.setWeight(100*bin.getWeight()/maxWeight);
        }

        calculated = true;
    }

    /*
     * Returns weights for a list of peptides
     */
    public ArrayList<Double> getPeptideWeights(ArrayList<xPeptide> peptides) {
        ArrayList<Double> weights = new ArrayList<Double>();
        for (xPeptide peptide:peptides) {
            weights.add(getPeptideWeight(peptide));
        }
        return weights;
    }

    /*
     * Returns weight for a single peptide
     */
    public Double getPeptideWeight(xPeptide peptide) {
        HashMap<String, Double> intensities = peptide.getQuantities("reporter ion intensity");
        // Returning 100% weight if no reporter ion intensities available
        if (intensities.isEmpty()) return 100.0;

        // Retrieving reference (lowest) intensity for peptide
        Double referenceIntensity = Utils.log2(getReferenceIntensity(intensities.values()));

        // Retrieving weight for a given reference intensity from weight bins
        for (WeightBin bin:weightBins) {
            if (referenceIntensity<bin.getMaxValue()) {
                return bin.getWeight();
            }
        }
        // Returning 100% weight if intensity doesn't fit into any lower bin
        return 100.0;
    }

    /*
     * Retrieves reference intensity
     */
    private double getReferenceIntensity(Collection<Double> intensities) {
        // Hultin-Rosenberg et al method
        // (reference - lowest intensity)
        return Collections.min(intensities);

        // Onsongo et al method (to be selected via parameter)
        // (reference - product of intensities)
//        Double product = 1.0;
//        for (Double intensity : intensities) {
//            product *= intensity;
//        }
//        return product;
    }

    public boolean isCalculated() {
        return calculated;
    }

    /*
     * Helper class implementing a single weight bin
     */
    class WeightBin {
        private double maxValue;
        private double minValue;
        private double weight;

        public double getMaxValue() {
            return maxValue;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public WeightBin(double value, double weight) {
            this.minValue = value;
            this.maxValue = value;
            this.weight = weight;
        }

        public WeightBin(double minValue, double maxValue, double weight) {
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.weight = weight;
        }

        public boolean contains(double value) {
            if(value >= minValue && value < maxValue) return true;
            else return false;
        }
    }
}
