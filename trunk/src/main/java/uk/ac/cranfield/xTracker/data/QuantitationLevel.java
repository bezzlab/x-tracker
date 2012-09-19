package uk.ac.cranfield.xTracker.data;

import java.util.HashMap;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
    public static final int SUM = 0;
    public static final int MEAN = 1;
    public static final int MEDIAN = 2;
    public static final int WEIGHTED_AVERAGE = 3;
    
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
                double result = 0d;
                
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
                double numerator = svValues.get(ratio.getNumerator());
                double denominator = svValues.get(ratio.getDenominator());
                double ratioValue;
                if (denominator == 0) {
                    ratioValue = Double.NaN;
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
            Method m = this.getClass().getDeclaredMethod(misc.getMethodName(this.getClass()), new Class[0]);
            ArrayList<QuantitationLevel> results = (ArrayList<QuantitationLevel>)m.invoke(this, new Object[0]);
            ArrayList<Integer> count = new ArrayList<Integer>();
            for(String name:quantitationNames){
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
                double numerator = getQuantity(quantitationName, ratio.getNumerator());
                double denominator = getQuantity(quantitationName, ratio.getDenominator());
                double ratioValue;
                if (denominator == 0) {
                    ratioValue = Double.NaN;
                } else {
                    ratioValue = numerator / denominator;
                }
                ratioValues.put(ratio.getId(), ratioValue);
            }
            setRatios(quantitationName, ratioValues);
        }
    }

    public void calculateRatioFromPeptideRatios(HashSet<String> quantitationNames){
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
                HashMap<String,Double> proRatios = GenericInferenceMethod.median(ratioValues, ratioIDs);
                setRatios(quantitationName, proRatios);
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